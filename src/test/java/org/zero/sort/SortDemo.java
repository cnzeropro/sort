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

    /** 生成随机 Integer 数组 */
    private static Integer[] randomInts(int size) {
        Integer[] a = new Integer[size];
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            a[i] = random.nextInt(size);
        }
        return a;
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

    /** 对对象数组计时 */
    private static void demoObjects(String name, Algorithm algorithm, int size) {
        Integer[] a = randomInts(size);
        long start = System.nanoTime();
        Sort.sort(a, algorithm);
        long timeMs = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("%-12s Integer[%-7d] time=%d ms%n", name, size, timeMs);
    }

    /** 对 int 数组计时 */
    private static void demoInts(String name, Algorithm algorithm, int size) {
        int[] a = randomIntArray(size);
        long start = System.nanoTime();
        Sort.sort(a, algorithm);
        long timeMs = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("%-12s int[%-10d] time=%d ms%n", name, size, timeMs);
    }

    public static void main(String[] args) {
        // O(n^2) 算法使用较小规模，避免耗时过长
        System.out.println("=== Integer[] ===");
        for (Algorithm algorithm : Algorithm.values()) {
            demoObjects(algorithm.name(), algorithm, 10_000);
        }
        System.out.println("=== int[] ===");
        for (Algorithm algorithm : Algorithm.values()) {
            demoInts(algorithm.name(), algorithm, 10_000);
        }
    }
}
