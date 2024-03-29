9 垃圾收集器（Garbag-First Garbage Collector
垃圾优先（G1）垃圾收集器是一个服务器式的垃圾收集器，针对具有大内存的多处理器机器。它试图以高概率满足垃圾收集（GC）暂停时间的目标，同时实现高吞吐量。全堆操作，如全局标记，是与应用线程同时进行的。这防止了与堆或实时数据大小成比例的中断。

G1收集器通过几种技术实现了高性能和暂停时间的目标。

堆被划分为一组大小相等的堆区域，每个区域都是虚拟内存的连续范围。G1执行一个并发的全局标记阶段，以确定整个堆内对象的存活性。在标记阶段完成后，G1知道哪些区域大部分是空的。它首先收集这些区域，这往往会产生大量的自由空间。这就是为什么这种垃圾收集方法被称为 "垃圾优先"。顾名思义，G1将其收集和压缩活动集中在堆中可能充满可回收对象的区域，也就是垃圾。G1使用暂停预测模型来满足用户定义的暂停时间目标，并根据指定的暂停时间目标来选择收集的区域数量。

G1将对象从堆的一个或多个区域复制到堆上的一个区域，在这个过程中既压缩又释放了内存。这种疏散是在多处理器上并行进行的，以减少暂停时间并增加吞吐量。因此，在每次垃圾收集时，G1都会持续工作以减少碎片化。这是前两种方法无法做到的。CMS（并发标记扫除）垃圾收集不做压缩。并行压实只执行全堆压实，这导致了相当长的暂停时间。

需要注意的是，G1不是一个实时收集器。它以很高的概率达到设定的暂停时间目标，但不是绝对确定。根据之前收集的数据，G1估计在目标时间内可以收集多少个区域。因此，收集器对收集区域的成本有一个相当准确的模型，它使用这个模型来决定在暂停时间目标内收集哪些和多少区域。

G1的第一个重点是为运行需要大堆和有限GC延迟的应用程序的用户提供一个解决方案。这意味着堆的大小约为6GB或更大，并且稳定和可预测的暂停时间低于0.5秒。

如果应用程序具有以下一个或多个特征，那么使用CMS或并行压缩运行的应用程序将从切换到G1中受益。

超过50%的Java堆被实时数据占据。

对象分配率allocation rate或晋升率promotion变化很大。

应用程序不希望出现的长时间垃圾收集或压实停顿（超过0.5到1秒）。

G1被计划作为并行标记收集器（CMS）的长期替代品。将G1与CMS进行比较，可以发现使G1成为更好的解决方案的差异。一个区别是，G1是一个压缩型收集器。另外，G1提供了比CMS收集器更可预测的垃圾收集暂停，并允许用户指定所需的暂停目标。

与CMS一样，G1是为那些需要较短GC暂停的应用而设计的。

G1是逻辑意义上的世代。一组空区域被指定为逻辑上的年轻代。在图中，年轻代为浅蓝色。分配是从该逻辑年轻代中进行的，当年轻代满了之后，这组区域就会被垃圾收集（ygc）。在某些情况下，年轻区域集以外的区域（深蓝色的老年代区域）也可以同时被垃圾收集。这被称为混合收集。在图中，被收集的区域用红框标记。该图说明了混合收集，因为年轻区域和老年区域都被收集。垃圾收集是一个压缩的收集，它将活的对象复制到选定的、最初是空的区域。根据存活对象的年龄，该对象可以被复制到一个存活区域（用 "S "标记）或一个老年区域（没有具体显示）。标记为 "H "的区域包含了大于半个区域的巨大对象，并被特殊对待；请参见 "垃圾优先 "垃圾收集器中的巨大对象和巨大分配部分。

分配（疏散）失败
与CMS一样，G1收集器在应用程序继续运行时运行其部分收集，存在应用程序分配对象的速度快于垃圾收集器恢复空闲空间的风险。关于类似的CMS行为，请参见并发标记扫描（CMS）收集器中的并发模式失败一节。在G1中，失败（Java堆的耗尽）发生在G1将实时数据从一个区域（疏散）复制到另一个区域时。拷贝是为了压缩实时数据。如果在被垃圾收集的区域的疏散过程中不能找到一个空的区域，那么就会发生分配失败（因为没有空间来分配被疏散区域的活对象），并且会进行停止世界（STW）的完全收集。

浮动的垃圾
对象可能在G1收集过程中死亡而不被收集。G1使用一种叫做snapshot-at-the-beginning（SATB）的技术来保证垃圾收集器能找到所有活着的对象。SATB指出，在并发标记（对整个堆的标记）开始时，为了收集的目的，任何对象都被认为是活的。SATB允许以一种类似于CMS增量更新的方式来浮动垃圾。

暂停
G1暂停应用程序来复制活的对象到新的区域。这些暂停可以是只收集年轻区域的ygc暂停，也可以是疏散年轻和老年区域的混合收集暂停。与CMS一样，在应用程序停止时，有一个最后的标记或注释暂停来完成标记。而CMS也有一个初始标记暂停，G1将初始标记工作作为疏散暂停的一部分。G1在收集结束时有一个清理阶段，部分是STW，部分是并发的。清理阶段的STW部分识别空区域，并确定旧区域为下一次采集的候选区域。

卡表和并发阶段
如果垃圾收集器不收集整个堆（增量收集），垃圾收集器需要知道哪里有从未收集的堆部分进入正在收集的堆部分的指针。这通常是针对代际垃圾收集器而言的，其中堆的未收集部分通常是老一代的，而堆的被收集部分是年轻一代的。保存这些信息的数据结构（老一代指向年轻一代对象的指针），是一个记忆集。卡片表是一种特殊类型的记忆集。Java HotSpot VM使用一个字节数组作为卡表。每个字节都被称为一个卡。一个卡对应于堆中的一个地址范围。弄脏一个卡片意味着将字节的值改为一个脏值；一个脏值可能包含一个新的指针，从卡片所覆盖的地址范围内的老一代到年轻一代。

处理一个卡片意味着查看该卡片是否有一个从老一代到年轻一代的指针，也许会对该信息做一些处理，比如将其转移到另一个数据结构中。

G1有并发标记阶段，标记从应用中发现的活对象。并发标记从疏散暂停（初始标记工作完成的地方）的结束延伸到remark。并发的清理阶段将被集合清空的区域添加到自由区域的列表中，并清除这些区域的记忆集。此外，一个并发的细化refinement线程根据需要运行，以处理被应用程序写入的卡表条目，这些条目可能有跨区域的引用。

开始一个并发的收集周期
如前所述，在混合收集中，新区域和旧区域都被收集。为了收集旧区域，G1对堆中的活对象做了一个完整的标记。这种标记是由一个并发标记阶段完成的。当整个Java堆的占用率达到参数InitiatingHeapOccupancyPercent的值时，并发标记阶段开始。用命令行选项-XX:InitiatingHeapOccupancyPercent=<NN>设置该参数的值。InitiatingHeapOccupancyPercent的默认值是45。

暂停时间目标
用标志MaxGCPauseMillis为G1设置一个暂停时间目标。G1使用一个预测模型来决定在该目标暂停时间内可以完成多少垃圾收集工作。在一次收集结束时，G1选择在下一次收集中要收集的区域（收集集）。收集集将包含年轻区域（其大小之和决定了逻辑年轻一代的大小）。一部分是通过选择收集集中的年轻区域的数量来控制GC暂停的长度。你可以像其他垃圾收集器一样在命令行中指定年轻一代的大小，但这样做可能会妨碍G1达到目标暂停时间的能力。除了暂停时间目标外，你还可以指定暂停发生的时间段的长度。你可以在指定暂停时间目标的同时，指定这个时间跨度的最小突变器用量（GCPauseIntervalMillis）。MaxGCPauseMillis的默认值是200毫秒。GCPauseIntervalMillis的默认值（0）相当于对时间跨度没有要求。

垃圾优先垃圾收集器的调整
本节描述了如何适应和调整Garbag-First垃圾收集器（G1 GC）的评估、分析和性能。

正如在 "垃圾优先 "一节中所描述的，G1 GC是一个区域化和世代化的垃圾收集器，这意味着Java对象堆（heap）被划分为若干大小相等的区域。启动时，Java虚拟机（JVM）会设置区域大小。区域大小可以从1MB到32MB不等，取决于堆的大小。目标是拥有不超过2048个区域。eden、survivor和old三代是这些区域的逻辑集合，并不是连续的。

G1 GC有一个它试图满足的暂停时间目标（软实时）。在ygc期间，G1 GC调整其young区（Eden和survivor大小）以满足软实时目标。关于G1 GC为什么要暂停以及如何设置暂停时间目标的信息，请参见 "垃圾优先垃圾收集器中的暂停和暂停时间目标 "部分。

在混合收集过程中，G1 GC会根据混合垃圾收集的目标数量、堆中每个区域的活对象百分比以及总体可接受的堆浪费百分比来调整被收集的旧区域数量。

G1 GC通过从一组或多组区域（称为集合集（CSet））中增量地平行复制实时对象到一个或多个不同的新区域来减少堆的碎片，以实现压缩。目标是尽可能多地回收堆空间，从那些包含最多可回收空间的区域开始，同时试图不超过暂停时间目标（垃圾优先）。

G1 GC使用独立的记忆集（RSets）来跟踪对区域的引用。独立的RSets使得区域的并行和独立的收集成为可能，因为只有一个区域的RSet必须被扫描以获得对该区域的引用，而不是整个堆。G1 GC使用一个写后屏障post-write barrier来记录对堆的改变并更新RSets。

垃圾收集阶段
除了构成 "停止世界"（STW）的年轻垃圾收集和混合垃圾收集的疏散暂停（见 "垃圾优先垃圾收集器中的分配（疏散）失败 "一节），G1 GC还具有并行parallel、并发concurrent和多阶段的标记周期multiphase marking cycles.。G1 GC使用snapshot-at-the-beginning（SATB）算法，该算法在逻辑上是在一个标记周期开始时对堆中的活对象集进行快照。活对象集还包括自打标周期开始以来分配的对象。G1 GC标记算法使用一个预写屏障pre-write barrier来记录和标记属于逻辑快照的对象。

混合垃圾收集
在成功完成一个并发标记周期后，G1 GC会从执行年轻的垃圾收集切换到执行混合垃圾收集。在混合垃圾收集中，G1 GC有选择地将一些旧区域添加到将被收集的Eden和survivor区域的集合中。添加的旧区域的确切数量由一些标志控制（参见建议部分的 "驯服混合垃圾收集器"）。在G1 GC收集了足够数量的旧区域后（在多次混合垃圾收集中），G1恢复到执行年轻的垃圾收集，直到下一个标记周期完成。

打分周期的各个阶段
打标周期有以下几个阶段。

初始标记阶段。G1 GC在这个阶段对根部进行标记。这个阶段是在正常的(STW)年轻的垃圾收集上捎带的。

根部区域扫描阶段。G1 GC扫描在初始标记阶段标记的幸存者区域，以寻找对老一代的引用，并标记被引用的对象。这个阶段与应用程序同时运行（不是STW），并且必须在下一次STW的ygc开始之前完成。

并行标记阶段。G1 GC在整个堆中寻找可到达的（活）对象。这个阶段与应用程序同时发生，并且可以被STW的年轻垃圾收集打断。

Remark阶段。这个阶段是STW收集，有助于标记周期的完成。G1 GC耗尽SATB缓冲区，追踪未访问的活对象，并进行引用处理。

清理阶段。在这个最后阶段，G1 GC执行STW操作的核算和RSet刷新。在核算过程中，G1 GC识别完全自由的区域和混合的垃圾收集候选区。清理阶段是部分并发的，当它重置并将空区域返回到自由列表中时。

重要的默认值
G1 GC是一个自适应的垃圾收集器，它的默认值使它能够在不修改的情况下有效地工作。表10-1，"G1垃圾收集器的重要选项的默认值 "列出了Java HotSpot VM，build 24中的重要选项及其默认值。你可以通过在JVM命令行中输入表10-1 "G1垃圾收集器重要选项的默认值 "中的选项，并改变设置，来调整G1垃圾收集器，使其符合你的应用性能需求。

表10-1 G1垃圾收集器的重要选项的默认值

选项和默认值 选项
-XX:G1HeapRegionSize=n

设置G1区域的大小。这个值将是2的幂，范围从1 MB到32 MB。我们的目标是根据最小的Java堆大小，拥有大约2048个区域。

-XX:MaxGCPauseMillis=200

为期望的最大暂停时间设置一个目标值。默认值是200毫秒。指定的值不适应你的堆大小。

这是一个实验性标志。请参阅 "如何解锁实验性虚拟机标志"，了解一个例子。这个设置取代了-XX:DefaultMinNewGenPercent设置。

-XX:G1MaxNewSizePercent=60

设置堆大小的百分比，作为年轻一代大小的最大值。默认值是你的Java堆的60%。

这是一个实验性的标志。请参阅如何解锁实验性虚拟机标志的例子。这个设置取代了 -XX:DefaultMaxNewGenPercent 设置。

-XX:ParallelGCThreads=n

设置STW工作线程的值。将n的值设置为逻辑处理器的数量。n的值与逻辑处理器的数量相同，最高值为8。

如果有超过8个逻辑处理器，将n的值设置为大约5/8的逻辑处理器。这在大多数情况下是可行的，除了较大的SPARC系统，n的值可以是逻辑处理器的大约5/16。

-XX:ConcGCThreads=n

设置并行标记线程的数量。将n设置为并行垃圾收集线程数（ParallelGCThreads）的大约1/4。

-XX:InitiatingHeapOccupancyPercent=45

设置触发标记周期的Java堆占有率阈值。默认的占用率是整个Java堆的45%。

-XX:G1MixedGCLiveThresholdPercent=85

设置旧区域的占用阈值，以包括在混合垃圾收集周期中。默认的占用率是85%.Footref1

这是一个实验性标志。请参阅 "如何解锁实验性虚拟机标志"，了解一个例子。这个设置取代了 -XX:G1OldCSetRegionLiveThresholdPercent 的设置。

-XX:G1HeapWastePercent=5

设置你愿意浪费的堆的百分比。当可回收百分比小于堆浪费百分比时，Java HotSpot VM不会启动混合垃圾收集周期。默认是5%。Footref1

-XX:G1MixedGCCountTarget=8

设置一个标记周期后混合垃圾收集的目标数量，以收集最多具有G1MixedGCLIveThresholdPercent活数据的旧区域。默认是8次混合垃圾收集。混合收集的目标是在这个目标数量之内。

-XX:G1OldCSetRegionThresholdPercent=10

设置在混合垃圾收集周期中要收集的旧区域的数量上限。默认是Java堆的10%。

-XX:G1ReservePercent=10

设置保留内存的百分比，以保持空闲，从而减少to-space溢出的风险。默认值是10%。当你增加或减少这个百分比时，请确保以相同的数量调整总的Java堆。

如何解锁实验性虚拟机标志
要改变实验性标志的值，你必须先解锁它们。你可以通过在命令行中在任何实验性标志之前明确设置-XX:+UnlockExperimentalVMOptions来做到这一点。例如。

java -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=10 -XX:G1MaxNewSizePercent=75 G1test.jar

建议
当你评估和微调G1 GC时，请牢记以下建议。

​	年轻一代的大小。避免用-Xmn选项或任何或其他相关选项（如-XX:NewRatio）明确设置年轻一代的大小。固定年轻一代的大小会覆盖目标停顿时间目标。

​	暂停时间目标。当你评估或调整任何垃圾收集时，总是有一个延迟与吞吐量的权衡。G1 GC是一个增量的垃圾收集器，具有统一的暂停时间，但在应用线程上也有更多的开销。G1 GC的吞吐量目标是90%的应用时间和10%的垃圾收集时间。将此与Java HotSpot VM并行收集器进行比较。并行收集器的吞吐量目标是99%的应用时间和1%的垃圾收集时间。因此，当你评估G1 GC的吞吐量时，放松你的暂停时间目标。设置过于激进的目标表明你愿意承受垃圾收集开销的增加，这对吞吐量有直接影响。当你评估G1 GC的延迟时，你设置了你所期望的（软）实时目标，G1 GC将试图满足它。作为一个副作用，吞吐量可能会受到影响。参见 "垃圾优先垃圾收集器中的暂停时间目标 "一节以了解更多信息。驯服混合垃圾收集。当你调整混合垃圾收集时，可以尝试使用以下选项。关于这些选项的信息，请看重要默认值一节。

-XX:InitiatingHeapOccupancyPercent。用来改变标记阈值。

-XX:G1MixedGCLiveThresholdPercent和-XX:G1HeapWastePercent。用来改变混合垃圾收集的决定。

-XX:G1MixedGCCountTarget和-XX:G1OldCSetRegionThresholdPercent。用来调整旧区域的CSet。

溢出和耗尽的日志信息
当你在日志中看到to-space overflow或to-space exhausted的信息时，G1 GC没有足够的内存用于生存者或提升对象，或用于两者。Java堆不能，因为它已经达到了最大值。示例信息。

```
924.897: [GC pause (G1 Evacuation Pause) (mixed) (to-space exhausted), 0.1957310 secs]``924.897: [GC pause (G1 Evacuation Pause) (mixed) (to-space overflow), 0.1957310 secs]
```

为了缓解这个问题，尝试进行以下调整。

增加-XX:G1ReservePercent选项的值（以及相应的总堆）以增加 "to-space "的储备内存量。

通过减少-XX:InitiatingHeapOccupancyPercent的值，提前开始标记周期。

增加-XX:ConcGCThreads选项的值，以增加并行标记线程的数量。

关于这些选项的描述，请参见重要默认值一节。

庞大的对象和庞大的分配
对于G1 GC来说，任何超过半个区域大小的对象都被认为是一个巨大的对象。这样的对象在老一代中被直接分配到humongous区域。这些巨大的区域是一组连续的区域。StartsHumongous标志着该连续集合的开始，ContinuesHumongous标志着该集合的继续。

在分配任何humongous区域之前，会检查标记阈值，如果有必要，会启动一个并发循环。

死亡的Humongous对象在标记周期结束后的清理阶段和完整的垃圾回收周期中被释放。

为了减少复制的开销，humongous对象不包括在任何疏散暂停中。一个完整的垃圾收集周期将humongous对象压缩在原地。

因为每个单独的StartsHumongous和ContinuesHumongous区域集只包含一个humongous对象，humongous对象的末端和该对象所跨越的最后一个区域的末端之间的空间是未使用的。对于那些仅仅比堆区域大小的倍数稍大的对象，这个未使用的空间会导致堆变得支离破碎。

如果你看到由于humongous分配而启动的背对背的并发周期back-to-back concurrent cycles，并且如果这种分配正在使你的老一代产生碎片，那么增加-XX:G1HeapRegionSize的值，使以前的humongous对象不再是humongous，并将遵循常规分配路径。
