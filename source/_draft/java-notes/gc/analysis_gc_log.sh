for file_name in $(ls gc_log*)
do
    echo "${file_name}"
    grep "\[GC" ${file_name} | wc -l
    grep "\[Full GC" ${file_name} | wc -l
    grep "执行结束。共生成对象次数" ${file_name} | awk -F: '{print $2}' | sort -n | head -n 9 | sort -n -r | head -n 8 | awk '{sum+=$1} END {print "Average = ", sum/NR}'
done

for file_name in $(ls gc_log_G1*)
do
    echo "${file_name}"
    grep "\[Eden:" ${file_name} | wc -l
    grep "\[Full GC" ${file_name} | wc -l
done
