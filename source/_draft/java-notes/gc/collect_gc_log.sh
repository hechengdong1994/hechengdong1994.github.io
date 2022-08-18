#!/bin/bash

javac GCLogGenerator.java

gc_type_array=("Serial" "Parallel" "ConcMarkSweep" "G1")
mem_array=(256 512 1024 2048 4096)
duration_array=(1 5 10)
times_array=({0..9})

# gc类型
for gc_type in ${gc_type_array[*]}
do
    # 堆内存
    for mem in ${mem_array[*]}
    do
        # 运行时间
        for duration in ${duration_array[*]}
        do
            # 执行次数
            for a in ${times_array[*]}
            do
                java -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+Use${gc_type}GC -Xms${mem}m -Xmx${mem}m GCLogGenerator duration=${duration} >> gc_log_${gc_type}_${duration}_${mem}.txt 2>> gc_log_${gc_type}_${duration}_${mem}.txt
            done
        done
    done
done
