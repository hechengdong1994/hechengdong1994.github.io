并发标记扫描（CMS）收集器

并发标记扫描（CMS）收集器是为那些喜欢较短的垃圾收集暂停时间，并且能够在应用程序运行时与垃圾收集器共享处理器资源的应用程序设计的。通常情况下，那些拥有相对较大的长寿命数据集（一个大的老年代）并在有两个或更多处理器的机器上运行的应用程序往往从使用这种收集器中受益。然而，任何对暂停时间要求不高的应用程序都应该考虑使用这种收集器。CMS收集器是通过命令行选项-XX:+UseConcMarkSweepGC启用的。

与其他可用的收集器类似，CMS收集器是分代的；因此，小收集和大收集都会发生。CMS收集器试图通过使用单独的垃圾收集器线程来追踪与应用线程执行同时进行的可达对象，从而减少由于主要收集而导致的暂停时间。在每个主要的收集周期中，CMS收集器会在收集开始时短暂地暂停所有的应用线程，并在收集的中期再次暂停。第二次暂停往往是这两次暂停中较长的一次。在两次暂停期间，多个线程被用来做收集工作。剩余的收集工作（包括对实时对象的大部分追踪和对无法到达的对象的清扫）是由一个或多个与应用程序同时运行的垃圾收集器线程完成。ygc可以与正在进行的fgc交错进行，并且以类似于并行收集器的方式进行（特别是在ygc期间，应用程序线程被停止）。

并发模式故障
CMS收集器使用一个或多个垃圾收集器线程，与应用线程同时运行，目的是在其满载之前完成对永久生成的收集。如前所述，在正常运行中，CMS收集器在应用线程仍在运行的情况下完成大部分的跟踪和清扫工作，所以应用线程只看到短暂的暂停。然而，如果CMS收集器无法在老年代填满之前完成对无法到达的对象的回收，或者如果老年代中的可用空间块无法满足分配，那么应用程序就会暂停，并且在所有应用程序线程停止的情况下完成收集。无法并发完成收集被称为并发模式失败，表明需要调整CMS收集器的参数。如果并发收集被显式垃圾收集（System.gc()）或为诊断工具提供信息所需的垃圾收集打断，则报告为并发模式中断。

过多的GC时间和OutOfMemoryError
如果垃圾收集时间过长，CMS收集器会抛出OutOfMemoryError：如果垃圾收集的时间超过总时间的98%，而恢复的堆不到2%，那么会抛出OutOfMemoryError。这个功能是为了防止应用程序在长时间运行的同时，由于堆太小，几乎没有进展。如果有必要，可以通过在命令行中添加选项-XX:-UseGCOverheadLimit来禁用该功能。

该策略与并行收集器中的策略相同，只是执行并发收集的时间不计入98%的时间限制。换句话说，只有在应用程序停止时执行的收集才会被计入过度的GC时间。这种收集通常是由于并发模式失败或明确的收集请求（例如，对System.gc的调用）。

浮动垃圾
CMS收集器，就像Java HotSpot VM中的所有其他收集器一样，是一个跟踪收集器，它至少可以识别堆中的所有可到达对象。用Richard Jones和Rafael D. Lins在他们的出版物Garbage Collection中的说法。用Richard Jones和Rafael D. Lins在他们的出版物Garbage Collection: Algorithms for Automated Dynamic Memory中的说法，它是一个递增的更新收集器。由于应用程序线程和垃圾收集器线程在fgc过程中同时运行，被垃圾收集器线程追踪的对象随后可能在收集过程结束时变得不可及。这种尚未被回收的无法到达的对象被称为浮动垃圾。浮动垃圾的数量取决于并发收集周期的持续时间和应用程序的引用更新频率，也被称为突变。此外，由于年轻代和老年代是独立收集的，因此它们互相是对方的toor来源。作为一个粗略的指导方针，尝试将老一代的大小增加20%，以考虑到浮动垃圾。在一个并发的收集周期结束时，堆中的浮动垃圾在下一个收集周期中被收集。

暂停
在一个并发的收集周期中，CMS收集器会暂停一个应用程序两次。第一次暂停是将从root**直接到达**的对象（例如，来自应用程序线程堆栈和寄存器的对象引用，静态对象等）和来自堆中其他地方的对象（例如，年轻代）标记为存活。这个暂停被称为初始标记暂停。第二个暂停是在并发追踪阶段结束时出现的，它可以找到并发追踪所遗漏的对象，这些对象是在CMS收集器完成对某个对象的追踪后，由于应用线程对该对象的引用进行更新而遗漏的。这个暂停被称为remark暂停。

并发阶段
可达对象图的并发追踪发生在初始标记暂停和备注暂停之间。在这个并发追踪阶段，一个或多个并发的垃圾收集器线程可能会使用处理器资源，而这些资源本来是可以用于应用程序的。因此，在这个阶段和其他并发阶段，即使应用程序线程没有暂停，计算型的应用程序可能会看到应用程序吞吐量相应的下降。在备注暂停之后，一个并发的清扫阶段会收集被确认为不可达的对象。一旦一个收集周期完成，CMS收集器就会等待，几乎不消耗任何计算资源，直到下一个fgc周期的开始。

开始一个并发的收集周期
在串行收集器中，只要老年代已满，就会发生fcg，并且在收集完成时，所有的应用线程都会停止。相比之下，并发收集的开始必须在时间上保证收集能在老年代满之前完成；否则，应用程序将观察到由于并发模式失败而导致的较长时间的暂停。有几种方法来启动一个并发的收集。

根据最近的历史记录，CMS收集器会对老年代耗尽之前的剩余时间以及并发收集周期所需的时间进行估计。利用这些动态的估计，一个并发的收集周期开始了，目的是要在老年代用完之前完成收集周期。为了安全起见，对这些估计进行了补充，因为并发模式故障可能会造成很大的损失。

如果老年代的占用率超过了启动占用率（老年代的一个百分比），也会启动并发采集。这个启动占用率阈值的默认值约为92%，但这个值会随着版本的变化而变化。这个值可以通过命令行选项-XX:CMSInitiatingOccupancyFraction=<N>来手动调整，其中<N>是老年代的一个整数百分比（0到100）。

安排暂停
年轻代和老年的停顿是独立发生的。它们不会重叠，但可能会快速地连续发生，这样一来，一个收集的暂停，紧接着另一个收集的暂停，就会显得是一个单一的、更长的暂停。为了避免这种情况，CMS收集器尝试将备注暂停大致地安排在上一个和下一个ygc暂停时间的中间。目前还没有对初始标记暂停进行这种安排，因为初始标记暂停通常比注释暂停短得多。

递增模式
请注意，增量模式在 Java SE 8 中被弃用，并可能在未来的主要版本中被删除。

CMS收集器可以在并发阶段以增量方式完成的模式下使用。回想一下，在并发阶段，垃圾收集器线程正在使用一个或多个处理器。增量模式旨在通过周期性地停止并发阶段来减少长并发阶段的影响，从而将处理器返回给应用程序。这种模式在这里被称为i-cms，它将收集器同时完成的工作划分为小块时间，在年轻一代的集合之间安排。当需要CMS提供的低暂停时间的应用程序在具有少量处理器（例如1或2）的机器上运行时，此功能非常有用。

并发收集周期通常包括以下步骤：

停止所有应用程序线程，确定可从根目录访问的对象集，然后恢复所有应用程序线程。

在应用程序线程执行时，使用一个或多个处理器并发跟踪可访问对象图。

使用一个处理器同时回溯自上一步跟踪以来修改的对象图部分。

停止所有应用程序线程，回溯自上次检查以来可能已被修改的根和对象图部分，然后恢复所有应用程序线程。

同时使用一个处理器将无法访问的对象清理（sweep）到用于分配的空闲列表中。

使用一个处理器同时调整堆的大小，并为下一个收集周期准备支持数据结构。

通常，CMS收集器在整个并发跟踪阶段使用一个或多个处理器，而不会自动放弃它们。类似地，一个处理器用于整个并发清理阶段，同样不会放弃它。对于具有响应时间限制的应用程序来说，这种开销可能会造成太多的中断，尤其是在只有一个或两个处理器的系统上运行时。增量模式通过将并发阶段分解为短时间的活动突发来解决这个问题，这些活动计划在小暂停之间的中间进行。

i-cms模式使用占空比来控制cms采集器在自愿放弃处理器之前可以完成的工作量。占空比是允许CMS收集器运行的年轻一代收集之间的时间百分比。i-cms模式可以根据应用程序的行为自动计算占空比（推荐的方式，称为自动调整pacing），或者可以在命令行上将占空比设置为固定值。

Table 8-1 Command-Line Options for i-cms

Option	Description	Default Value, Java SE 5 and Earlier	Default Value, Java SE 6 and Later

`-XX:+CMSIncrementalMode`	启用增量模式。请注意，CMS收集器也必须启用（使用-XX:+UseConMarkSweepGC）才能使此选项工作。	disabled	disabled

`-XX:+CMSIncrementalPacing`	启用自动调整。增量模式占空比根据JVM运行时收集的统计信息自动调整。	disabled	disabled

`-XX:CMSIncrementalDutyCycle=``<N>`	CMS收集器允许运行的次要收集之间的时间百分比（0到100）。如果启用了CMSIncrementalPacing，则这只是初始值。	50	10

`-XX:CMSIncrementalDutyCycleMin=``<N>`	启用CMSIncrementalPacing时占空比下限的百分比（0到100）。	10	0

`-XX:CMSIncrementalSafetyFactor=``<N>	计算占空比时用于增加稳健性conservatism 的百分比（0到100）	10	10

`-XX:CMSIncrementalOffset=``<N>`	在两次收集之间的时间段内，增量模式占空比向右移动的百分比（0到100）。	0	0

`-XX:CMSExpAvgFactor=``<N>`The percentage (0 to 100) used to weight the current sample when computing exponential averages for the CMS collection statistics.	25	25

推荐选项

要在Java SE 8中使用i-cms，请使用以下命令行选项：

`-XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode \ -XX:+PrintGCDetails -XX:+PrintGCTimeStamps `The first two options enable the CMS collector and i-cms, respectively. The last two options are not required; they simply cause diagnostic information about garbage collection to be written to standard output, so that garbage collection behavior can be seen and later analyzed.For Java SE 5 and earlier releases, Oracle recommends using the following as an initial set of command-line options for i-cms:`-XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode \ -XX:+PrintGCDetails -XX:+PrintGCTimeStamps \ -XX:+CMSIncrementalPacing -XX:CMSIncrementalDutyCycleMin=0 -XX:CMSIncrementalDutyCycle=10 `The same values are recommended for JavaSE8 although the values for the three options that control i-cms automatic pacing became the default in JavaSE6.

基本故障排除
The i-cms automatic pacing feature uses statistics gathered while the program is running to compute a duty cycle so that concurrent collections complete before the heap becomes full. However, past behavior is not a perfect predictor of future behavior and the estimates may not always be accurate enough to prevent the heap from becoming full. If too many full collections occur, then try the steps in Table 8-2, "Troubleshooting the i-cms Automatic Pacing Feature", one at a time.

Table 8-2 Troubleshooting the i-cms Automatic Pacing Feature

Step	Options
1. Increase the safety factor.

-XX:CMSIncrementalSafetyFactor=<N>

2. Increase the minimum duty cycle.

-XX:CMSIncrementalDutyCycleMin=<N>

3. Disable automatic pacing and use a fixed duty cycle.

-XX:-CMSIncrementalPacing -XX:CMSIncrementalDutyCycle=<N>

测量结果

请注意，CMS采集器的输出是与次要采集的输出穿插在一起的；通常在一个同时进行的采集周期中会发生许多次要采集。CMS-initial-mark表示并发收集周期的开始，CMS-concurrent-mark表示并发标记阶段的结束，而CMS-concurrent-sweep表示并发清扫阶段的结束。前面没有讨论的是由CMS-concurrent-preclean表示的预清扫阶段。预清扫表示可以同时进行的工作，为备注阶段的CMS-remark做准备。最后一个阶段是由CMS-concurrent-reset表示的，是为下一次并发采集做准备。

例8-1 CMS采集器的输出

[GC [1 CMS-initial-mark: 13991K(20288K)] 14103K(22400K), 0.0023781 secs] 。
[GC [DefNew: 2112K->64K(2112K), 0.0837052 secs] 16103K->15476K(22400K), 0.0838519 secs]。
...
[GC [DefNew: 2077K->63K(2112K), 0.0126205 secs] 17552K->15855K(22400K), 0.0127482 secs] 。
[CMS-current-mark: 0.267/0.374 secs] 。
[GC [DefNew: 2111K->64K(2112K), 0.0190851 secs] 17903K->16154K(22400K), 0.0191903 secs] 。
[CMS-current-preclean: 0.044/0.064 secs] 。
[GC [1 CMS-remark: 16090K(20288K)] 17242K(22400K), 0.0210460 secs]
[GC [DefNew: 2112K->63K(2112K), 0.0716116 secs] 18177K->17382K(22400K), 0.0718204 secs].
[GC [DefNew: 2111K->63K(2112K), 0.0830392 secs] 19363K->18757K(22400K), 0.0832943 secs]。
...
[GC [DefNew: 2111K->0K(2112K), 0.0035190 secs] 17527K->15479K(22400K), 0.0036052 secs] 。
[CMS-current-sweep: 0.291/0.662 secs]。
[GC [DefNew: 2048K->0K(2112K), 0.0013347 secs] 17527K->15479K(27912K), 0.0014231 secs] 。
[CMS-current-reset: 0.016/0.016秒] 。
[GC [DefNew: 2048K->1K(2112K), 0.0013936 secs] 17527K->15479K(27912K), 0.0014814 secs
]
相对于次要收集的暂停时间，初始标记的暂停时间通常很短。并发阶段（并发标记、并发预清洁和并发清扫）通常比小规模收集暂停时间长得多，如例8-1，"CMS收集器的输出 "所示。然而，请注意，在这些并发阶段，应用程序并没有暂停。remark暂停的长度通常与ygc的长度相当。注释暂停受到某些应用程序特性的影响（例如，对象的高修改率会增加这个暂停），以及自上次小规模收集以来的时间（例如，年轻一代的更多对象会增加这个暂停）。
