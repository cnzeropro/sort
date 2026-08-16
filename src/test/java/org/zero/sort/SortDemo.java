package org.zero.sort;

import java.util.Random;

/**
 * 排序算法耗时演示
 * <p>
 * 运行方式：在 IDE 中直接运行 main 方法，或执行
 * <p>
 * mvn -q exec:java -Dexec.mainClass=org.zero.sort.SortDemo -Dexec.classpathScope=test
 *
 * @author Zero
 */
public class SortDemo {

    private SortDemo() {
    }

    /** 生成随机 int 数组 */
    private static int[] randomIntArray(int size) {
        int[] a = new int[size];
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            a[i] = random.nextInt(size);
        }
        return a;
    }

    /** 生成随机 Integer 数组 */
    private static Integer[] randomInts(int size) {
        Integer[] a = new Integer[size];
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            a[i] = random.nextInt(size);
        }
        return a;
    }

    /** 对 int 数组计时 */
    private static void demoInts(Algorithm algorithm, int size) {
        int[] a = randomIntArray(size);
        long start = System.nanoTime();
        Sort.sort(a, algorithm);
        long timeMs = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("%-12s int[%-8d] time=%d ms%n", algorithm.name(), size, timeMs);
    }

    /** 对 Integer 数组计时 */
    private static void demoObjects(Algorithm algorithm, int size) {
        Integer[] a = randomInts(size);
        long start = System.nanoTime();
        Sort.sort(a, algorithm);
        long timeMs = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("%-12s Integer[%-5d] time=%d ms%n", algorithm.name(), size, timeMs);
    }

    /** 慢算法的演示规模 */
    private static int sizeFor(Algorithm algorithm) {
        switch (algorithm) {
            case STOOGE:
                return 200;
            case PANCAKE:
            case BUBBLE:
            case SELECTION:
            case INSERTION:
            case GNOME:
            case COCKTAIL:
            case ODD_EVEN:
            case CYCLE:
                return 2_000;
            default:
                return 20_000;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Integer[] ===");
        for (Algorithm algorithm : Algorithm.values()) {
            if (algorithm.applicability() == Algorithm.Applicability.ALL) {
                demoObjects(algorithm, sizeFor(algorithm));
            }
        }
        System.out.println("=== int[] ===");
        for (Algorithm algorithm : Algorithm.values()) {
            if (algorithm.applicability() != Algorithm.Applicability.FLOATS_ONLY) {
                demoInts(algorithm, sizeFor(algorithm));
            }
        }
        System.out.println("=== double[]（浮点专属算法）===");
        demoDoubles(Algorithm.BUCKET, 20_000);
    }

    /** 对 double 数组计时（桶排序演示） */
    private static void demoDoubles(Algorithm algorithm, int size) {
        double[] a = new double[size];
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            a[i] = random.nextDouble();
        }
        long start = System.nanoTime();
        Sort.sort(a, algorithm);
        long timeMs = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("%-12s double[%-6d] time=%d ms%n", algorithm.name(), size, timeMs);
    }
}
