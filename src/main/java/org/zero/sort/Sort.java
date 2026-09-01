package org.zero.sort;

import java.util.Comparator;
import java.util.List;

/**
 * 排序门面类（自动选择算法）
 * <p>
 * 与 JDK {@link java.util.Arrays#sort} 同理念：调用方不指定算法，
 * 由本类根据数据长度与结构自适应选择当前场景效率最好的算法：
 * <ul>
 *   <li><b>对象数组 / List（Comparable 与 Comparator）</b>：{@link TimSort Tim 排序}——
 *       检测并利用已有序片段，已有序输入 O(n)、最坏 O(n log n)、<b>稳定</b>，
 *       与 JDK {@code Arrays.sort(Object[])} 同款策略；</li>
 *   <li><b>原始类型（byte / short / int / long / float / double / char）</b>：
 *       <ol>
 *         <li>小区间（长度 &lt; 47）：{@link InsertionSort 插入排序}——小数组上常数最小；</li>
 *         <li>近有序（相邻逆序对 &lt; n/8）：{@link TimSort Tim 排序}——自适应利用已有序片段；</li>
 *         <li>其余：{@link QuickSort 双轴快速排序}——随机数据平均性能最好。</li>
 *       </ol></li>
 * </ul>
 * 需要指定具体算法（教学、对照实验等场景）时，直接使用各算法类：
 * {@code BubbleSort.sort(a)}、{@code QuickSort.sort(a, from, to)}、{@code RadixSort.sort(a)} 等，
 * 每个算法类提供与本类一致形态的全类型重载。
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * // 对象数组：自动选择（Tim 排序，稳定、自适应）
 * Integer[] a = {5, 3, 8, 1, 9};
 * Sort.sort(a);
 *
 * // 区间 [1, 4)：只排 a[1]..a[3]
 * Sort.sort(a, 1, 4);
 *
 * // Comparator 自定义排序（数组与 List）
 * Sort.sort(a, Comparator.reverseOrder());
 * List<String> list = new ArrayList<>();
 * Sort.sort(list);
 *
 * // 原始类型：自动选择（小数组插入、近有序 Tim、否则双轴快排）
 * int[] b = {5, 3, 8, 1, 9};
 * Sort.sort(b);
 * Sort.sort(b, 1, 4);
 * }</pre>
 *
 * <h2>区间约定</h2>
 * 所有带 {@code fromIndex}/{@code toIndex} 的方法遵循 JDK 惯例：
 * 区间为 <b>[fromIndex, toIndex)</b> 左闭右开，与 {@link java.util.Arrays#sort}、
 * {@link String#substring} 一致。
 *
 * <h2>异常约定</h2>
 * 与 {@link java.util.Arrays} 一致：
 * <ul>
 *   <li>空数组/空 List 合法，排序为空操作；</li>
 *   <li>null 数组/List、Comparable 排序区间内包含 null 元素 → {@link NullPointerException}
 *       （区间外的 null 不影响；Comparator 版本允许 null 元素，由比较器自行处理）；</li>
 *   <li>非法索引（fromIndex &lt; 0、toIndex &gt; 数组长度、fromIndex &gt; toIndex）
 *       → {@link IndexOutOfBoundsException}。</li>
 * </ul>
 *
 * @author Zero
 */
public final class Sort {

    /** 原始类型切换插入排序的长度阈值（与 JDK 双轴快排一致） */
    private static final int INSERTION_THRESHOLD = 47;

    private Sort() {
    }

    // ==================== 对象数组（Comparable）====================

    /**
     * 对整个数组排序（自动选择算法）
     */
    public static <T extends Comparable<? super T>> void sort(T[] a) {
        TimSort.sort(a);
    }

    /**
     * 对 [fromIndex, a.length) 区间排序（自动选择算法）
     */
    public static <T extends Comparable<? super T>> void sort(T[] a, int fromIndex) {
        TimSort.sort(a, fromIndex);
    }

    /**
     * 对 [fromIndex, toIndex) 区间排序（自动选择算法）
     */
    public static <T extends Comparable<? super T>> void sort(T[] a, int fromIndex, int toIndex) {
        TimSort.sort(a, fromIndex, toIndex);
    }

    /**
     * 对整个 List 原地排序（自动选择算法）
     */
    public static <T extends Comparable<? super T>> void sort(List<T> list) {
        TimSort.sort(list);
    }

    // ==================== 对象数组（Comparator）====================

    /**
     * 使用自定义比较器对整个数组排序（自动选择算法）
     */
    public static <T> void sort(T[] a, Comparator<? super T> comparator) {
        TimSort.sort(a, comparator);
    }

    /**
     * 使用自定义比较器对 [fromIndex, a.length) 区间排序（自动选择算法）
     */
    public static <T> void sort(T[] a, int fromIndex, Comparator<? super T> comparator) {
        TimSort.sort(a, fromIndex, comparator);
    }

    /**
     * 使用自定义比较器对 [fromIndex, toIndex) 区间排序（自动选择算法）
     */
    public static <T> void sort(T[] a, int fromIndex, int toIndex, Comparator<? super T> comparator) {
        TimSort.sort(a, fromIndex, toIndex, comparator);
    }

    /**
     * 使用自定义比较器对整个 List 原地排序（自动选择算法）
     */
    public static <T> void sort(List<T> list, Comparator<? super T> comparator) {
        TimSort.sort(list, comparator);
    }

    // ==================== byte ====================

    /**
     * 对整个 byte 数组排序（自动选择算法）
     */
    public static void sort(byte[] a) {
        sortByte(a, 0, a.length);
    }

    /**
     * 对 [fromIndex, a.length) 区间排序（自动选择算法）
     */
    public static void sort(byte[] a, int fromIndex) {
        sortByte(a, fromIndex, a.length);
    }

    /**
     * 对 [fromIndex, toIndex) 区间排序（自动选择算法）
     */
    public static void sort(byte[] a, int fromIndex, int toIndex) {
        sortByte(a, fromIndex, toIndex);
    }

    private static void sortByte(byte[] a, int from, int to) {
        IndexChecks.checkFromToIndex(from, to, a.length);
        int n = to - from;
        if (n < 2) {
            return;
        }
        if (n < INSERTION_THRESHOLD) {
            InsertionSort.sort(a, from, to);
            return;
        }
        if (nearlySorted(a, from, to)) {
            TimSort.sort(a, from, to);
            return;
        }
        QuickSort.sort(a, from, to);
    }

    private static boolean nearlySorted(byte[] a, int from, int to) {
        int limit = (to - from) >>> 3;
        int inversions = 0;
        for (int i = from; i < to - 1; i++) {
            if (a[i] > a[i + 1]) {
                inversions++;
                if (inversions >= limit) {
                    return false;
                }
            }
        }
        return true;
    }

    // ==================== short ====================

    /**
     * 对整个 short 数组排序（自动选择算法）
     */
    public static void sort(short[] a) {
        sortShort(a, 0, a.length);
    }

    /**
     * 对 [fromIndex, a.length) 区间排序（自动选择算法）
     */
    public static void sort(short[] a, int fromIndex) {
        sortShort(a, fromIndex, a.length);
    }

    /**
     * 对 [fromIndex, toIndex) 区间排序（自动选择算法）
     */
    public static void sort(short[] a, int fromIndex, int toIndex) {
        sortShort(a, fromIndex, toIndex);
    }

    private static void sortShort(short[] a, int from, int to) {
        IndexChecks.checkFromToIndex(from, to, a.length);
        int n = to - from;
        if (n < 2) {
            return;
        }
        if (n < INSERTION_THRESHOLD) {
            InsertionSort.sort(a, from, to);
            return;
        }
        if (nearlySorted(a, from, to)) {
            TimSort.sort(a, from, to);
            return;
        }
        QuickSort.sort(a, from, to);
    }

    private static boolean nearlySorted(short[] a, int from, int to) {
        int limit = (to - from) >>> 3;
        int inversions = 0;
        for (int i = from; i < to - 1; i++) {
            if (a[i] > a[i + 1]) {
                inversions++;
                if (inversions >= limit) {
                    return false;
                }
            }
        }
        return true;
    }

    // ==================== int ====================

    /**
     * 对整个 int 数组排序（自动选择算法）
     */
    public static void sort(int[] a) {
        sortInt(a, 0, a.length);
    }

    /**
     * 对 [fromIndex, a.length) 区间排序（自动选择算法）
     */
    public static void sort(int[] a, int fromIndex) {
        sortInt(a, fromIndex, a.length);
    }

    /**
     * 对 [fromIndex, toIndex) 区间排序（自动选择算法）
     */
    public static void sort(int[] a, int fromIndex, int toIndex) {
        sortInt(a, fromIndex, toIndex);
    }

    private static void sortInt(int[] a, int from, int to) {
        IndexChecks.checkFromToIndex(from, to, a.length);
        int n = to - from;
        if (n < 2) {
            return;
        }
        if (n < INSERTION_THRESHOLD) {
            InsertionSort.sort(a, from, to);
            return;
        }
        if (nearlySorted(a, from, to)) {
            TimSort.sort(a, from, to);
            return;
        }
        QuickSort.sort(a, from, to);
    }

    private static boolean nearlySorted(int[] a, int from, int to) {
        int limit = (to - from) >>> 3;
        int inversions = 0;
        for (int i = from; i < to - 1; i++) {
            if (a[i] > a[i + 1]) {
                inversions++;
                if (inversions >= limit) {
                    return false;
                }
            }
        }
        return true;
    }

    // ==================== long ====================

    /**
     * 对整个 long 数组排序（自动选择算法）
     */
    public static void sort(long[] a) {
        sortLong(a, 0, a.length);
    }

    /**
     * 对 [fromIndex, a.length) 区间排序（自动选择算法）
     */
    public static void sort(long[] a, int fromIndex) {
        sortLong(a, fromIndex, a.length);
    }

    /**
     * 对 [fromIndex, toIndex) 区间排序（自动选择算法）
     */
    public static void sort(long[] a, int fromIndex, int toIndex) {
        sortLong(a, fromIndex, toIndex);
    }

    private static void sortLong(long[] a, int from, int to) {
        IndexChecks.checkFromToIndex(from, to, a.length);
        int n = to - from;
        if (n < 2) {
            return;
        }
        if (n < INSERTION_THRESHOLD) {
            InsertionSort.sort(a, from, to);
            return;
        }
        if (nearlySorted(a, from, to)) {
            TimSort.sort(a, from, to);
            return;
        }
        QuickSort.sort(a, from, to);
    }

    private static boolean nearlySorted(long[] a, int from, int to) {
        int limit = (to - from) >>> 3;
        int inversions = 0;
        for (int i = from; i < to - 1; i++) {
            if (a[i] > a[i + 1]) {
                inversions++;
                if (inversions >= limit) {
                    return false;
                }
            }
        }
        return true;
    }

    // ==================== float ====================

    /**
     * 对整个 float 数组排序（自动选择算法）
     */
    public static void sort(float[] a) {
        sortFloat(a, 0, a.length);
    }

    /**
     * 对 [fromIndex, a.length) 区间排序（自动选择算法）
     */
    public static void sort(float[] a, int fromIndex) {
        sortFloat(a, fromIndex, a.length);
    }

    /**
     * 对 [fromIndex, toIndex) 区间排序（自动选择算法）
     */
    public static void sort(float[] a, int fromIndex, int toIndex) {
        sortFloat(a, fromIndex, toIndex);
    }

    private static void sortFloat(float[] a, int from, int to) {
        IndexChecks.checkFromToIndex(from, to, a.length);
        int n = to - from;
        if (n < 2) {
            return;
        }
        if (n < INSERTION_THRESHOLD) {
            InsertionSort.sort(a, from, to);
            return;
        }
        if (nearlySorted(a, from, to)) {
            TimSort.sort(a, from, to);
            return;
        }
        QuickSort.sort(a, from, to);
    }

    private static boolean nearlySorted(float[] a, int from, int to) {
        int limit = (to - from) >>> 3;
        int inversions = 0;
        for (int i = from; i < to - 1; i++) {
            if (Float.compare(a[i], a[i + 1]) > 0) {
                inversions++;
                if (inversions >= limit) {
                    return false;
                }
            }
        }
        return true;
    }

    // ==================== double ====================

    /**
     * 对整个 double 数组排序（自动选择算法）
     */
    public static void sort(double[] a) {
        sortDouble(a, 0, a.length);
    }

    /**
     * 对 [fromIndex, a.length) 区间排序（自动选择算法）
     */
    public static void sort(double[] a, int fromIndex) {
        sortDouble(a, fromIndex, a.length);
    }

    /**
     * 对 [fromIndex, toIndex) 区间排序（自动选择算法）
     */
    public static void sort(double[] a, int fromIndex, int toIndex) {
        sortDouble(a, fromIndex, toIndex);
    }

    private static void sortDouble(double[] a, int from, int to) {
        IndexChecks.checkFromToIndex(from, to, a.length);
        int n = to - from;
        if (n < 2) {
            return;
        }
        if (n < INSERTION_THRESHOLD) {
            InsertionSort.sort(a, from, to);
            return;
        }
        if (nearlySorted(a, from, to)) {
            TimSort.sort(a, from, to);
            return;
        }
        QuickSort.sort(a, from, to);
    }

    private static boolean nearlySorted(double[] a, int from, int to) {
        int limit = (to - from) >>> 3;
        int inversions = 0;
        for (int i = from; i < to - 1; i++) {
            if (Double.compare(a[i], a[i + 1]) > 0) {
                inversions++;
                if (inversions >= limit) {
                    return false;
                }
            }
        }
        return true;
    }

    // ==================== char ====================

    /**
     * 对整个 char 数组排序（无符号 16 位整数序，自动选择算法）
     */
    public static void sort(char[] a) {
        sortChar(a, 0, a.length);
    }

    /**
     * 对 [fromIndex, a.length) 区间排序（无符号 16 位整数序，自动选择算法）
     */
    public static void sort(char[] a, int fromIndex) {
        sortChar(a, fromIndex, a.length);
    }

    /**
     * 对 [fromIndex, toIndex) 区间排序（无符号 16 位整数序，自动选择算法）
     */
    public static void sort(char[] a, int fromIndex, int toIndex) {
        sortChar(a, fromIndex, toIndex);
    }

    private static void sortChar(char[] a, int from, int to) {
        IndexChecks.checkFromToIndex(from, to, a.length);
        int n = to - from;
        if (n < 2) {
            return;
        }
        if (n < INSERTION_THRESHOLD) {
            InsertionSort.sort(a, from, to);
            return;
        }
        if (nearlySorted(a, from, to)) {
            TimSort.sort(a, from, to);
            return;
        }
        QuickSort.sort(a, from, to);
    }

    private static boolean nearlySorted(char[] a, int from, int to) {
        int limit = (to - from) >>> 3;
        int inversions = 0;
        for (int i = from; i < to - 1; i++) {
            if (a[i] > a[i + 1]) {
                inversions++;
                if (inversions >= limit) {
                    return false;
                }
            }
        }
        return true;
    }
}
