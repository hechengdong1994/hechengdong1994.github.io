import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * GC日志生成
 * <p>
 * java -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xms256m -Xmx256m GCLogGenerator duration=1
 * -XX:+UseSerialGC
 * -XX:+UseParralelGC
 * -XX:+UseConcMarkSweepGC
 * -XX:+UseG1GC
 *
 * @author Administrator
 */
public class GCLogGenerator {
    private static final Random RANDOM = new Random();

    public static void main(String[] args) {
        // 运行时长参数
        long duration = 1;
        for (String arg : args) {
            if (arg.startsWith("duration=")) {
                String durationStr = arg.substring("duration=".length());
                try {
                    duration = Long.parseLong(durationStr);
                } catch (Exception e) {
                    // arg error. do nothing
                }
            }
        }

        // 根据运行时长计算结束时间戳
        long endMillis = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(duration);

        long counter = 0;
        System.out.println("开始执行.......");
        // 保存一部分对象，模拟进入老年代的长生命周期对象
        int cacheSize = 2000;
        Object[] cachedGarbage = new Object[cacheSize];
        // 在此时间范围内，持续循环
        while (System.currentTimeMillis() < endMillis) {
            // 生成对象
            Object garbage = generateGarbage(100 * 1024);
            counter++;
            int randomIndex = RANDOM.nextInt(2 * cacheSize);
            // 随机保存对象
            if (randomIndex < cacheSize) {
                cachedGarbage[randomIndex] = garbage;
            }
        }
        System.out.println("执行结束。共生成对象次数:" + counter);
    }

    /**
     * 生成对象
     */
    private static Object generateGarbage(int maxSize) {
        int randomSize = RANDOM.nextInt(maxSize);
        int type = randomSize % 4;
        Object result;
        // 不同类型表示了不同大小的对象
        switch (type) {
            case 0:
                result = new byte[randomSize];
                break;
            case 1:
                result = new int[randomSize];
                break;
            case 2:
                result = new double[randomSize];
                break;
            default:
                StringBuilder builder = new StringBuilder();
                String randomString = "randomString";
                while (builder.length() < randomSize) {
                    builder.append(randomString);
                    builder.append(maxSize);
                    builder.append(randomSize);
                }
                result = builder.toString();
                break;
        }
        return result;
    }
}
