package org.zero.sort;

import java.util.Random;
import java.util.function.Consumer;

/**
 * 排序算法耗时演示
 *
 * <p>运行方式：在 IDE 中直接运行 main 方法，或执行
 *
 * <p>mvn -q exec:java -Dexec.mainClass=org.zero.sort.SortDemo -Dexec.classpathScope=test
 *
 * @author Zero
 */
public class SortDemo {

    private SortDemo() {}

    /** 生成随机数组 */
    private static Integer[] randomArray(int size) {
        Integer[] a = new Integer[size];
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            a[i] = random.nextInt(size);
        }
        return a;
    }

    /** 对指定算法计时并输出结果 */
    private static void demo(String name, Consumer<Integer[]> sort, int size) {
        Integer[] a = randomArray(size);
        long start = System.nanoTime();
        sort.accept(a);
        long timeMs = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("%-14s size=%-7d time=%d ms%n", name, size, timeMs);
    }

    public static void main(String[] args) {
        // O(n^2) 算法使用较小规模，避免耗时过长
        demo("Bubble", Sort.Bubble::sort, 10_000);
        demo("Selection", Sort.Selection::sort, 10_000);
        demo("Insertion", Sort.Insertion::sort, 10_000);
        demo("Shell", Sort.Shell::sort, 100_000);
        demo("Shell-general", a -> Sort.Shell.generalSort(a, 0, a.length - 1), 100_000);
        demo("Merge", Sort.Merge::sort, 100_000);
        demo("Quick", Sort.Quick::sort, 100_000);
    }
}
