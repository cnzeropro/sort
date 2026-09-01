package org.zero.sort;

import java.util.Comparator;
import java.util.List;

/**
 * 臭皮匠排序（不稳定，纯教育用途）。
 * <p>
 * 首元素大于尾元素则交换；区间长度大于 2 时依次递归排序前 2/3、后 2/3、再前 2/3。
 * 比较次数与输入无关，最好/最坏均为 O(n^(log 3 / log 1.5)) ≈ O(n^2.71)；
 * 空间 O(log n)（递归栈）。
 * <p>
 * 支持任意 {@link Comparable} 对象数组 / {@link List}、任意 {@link Comparator} 对象数组 /
 * {@link List}，以及 byte / short / int / long / float / double / char 原始类型。
 * float / double 使用 {@link Float#compare} / {@link Double#compare} 全序（NaN 最后），
 * char 按无符号 16 位整数序。
 *
 * @author Zero
 */
public final class StoogeSort {

    private StoogeSort() {
    }

    // ==================== 对象数组（Comparable）====================

    /**
     * 对整个数组排序
     */
    public static <T extends Comparable<? super T>> void sort(T[] a) {
        sortComparable(a, 0, a.length);
    }

    /**
     * 对 [fromIndex, a.length) 区间排序
     */
    public static <T extends Comparable<? super T>> void sort(T[] a, int fromIndex) {
        sortComparable(a, fromIndex, a.length);
    }

    /**
     * 对 [fromIndex, toIndex) 区间排序
     */
    public static <T extends Comparable<? super T>> void sort(T[] a, int fromIndex, int toIndex) {
        sortComparable(a, fromIndex, toIndex);
    }

    private static <T extends Comparable<? super T>> void sortComparable(T[] a, int from, int to) {
        ArrayChecks.requireArray(a);
        IndexChecks.checkFromToIndex(from, to, a.length);
        ArrayChecks.requireNoNullInRange(a, from, to);
        stooge(a, from, to, Comparator.naturalOrder());
    }

    /**
     * 对整个 List 原地排序
     */
    public static <T extends Comparable<? super T>> void sort(List<T> list) {
        T[] a = SortSupport.comparableListToArray(list);
        sort(a);
        SortSupport.copyBack(list, a);
    }

    // ==================== 对象数组（Comparator）====================

    /**
     * 使用自定义比较器对整个数组排序
     */
    public static <T> void sort(T[] a, Comparator<? super T> comparator) {
        sortComparator(a, 0, a.length, comparator);
    }

    /**
     * 使用自定义比较器对 [fromIndex, a.length) 区间排序
     */
    public static <T> void sort(T[] a, int fromIndex, Comparator<? super T> comparator) {
        sortComparator(a, fromIndex, a.length, comparator);
    }

    /**
     * 使用自定义比较器对 [fromIndex, toIndex) 区间排序
     */
    public static <T> void sort(T[] a, int fromIndex, int toIndex, Comparator<? super T> comparator) {
        sortComparator(a, fromIndex, toIndex, comparator);
    }

    private static <T> void sortComparator(T[] a, int from, int to, Comparator<? super T> comparator) {
        ArrayChecks.requireArray(a);
        if (comparator == null) {
            throw new NullPointerException("comparator must not be null");
        }
        IndexChecks.checkFromToIndex(from, to, a.length);
        stooge(a, from, to, comparator);
    }

    /**
     * 使用自定义比较器对整个 List 原地排序
     */
    public static <T> void sort(List<T> list, Comparator<? super T> comparator) {
        T[] a = SortSupport.listToArray(list);
        sort(a, comparator);
        SortSupport.copyBack(list, a);
    }

    // ==================== 对象实现 ====================

    private static <T> boolean gt(T[] a, int i, int j, Comparator<? super T> cmp) {
        return cmp.compare(a[i], a[j]) > 0;
    }

    private static <T> void swap(T[] a, int i, int j) {
        T tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    private static <T> void stooge(T[] a, int from, int to, Comparator<? super T> cmp) {
        if (to - from < 2) {
            return;
        }
        stoogeRec(a, from, to - 1, cmp);
    }

    private static <T> void stoogeRec(T[] a, int lo, int hi, Comparator<? super T> cmp) {
        if (gt(a, lo, hi, cmp)) {
            swap(a, lo, hi);
        }
        if (hi - lo + 1 > 2) {
            int t = (hi - lo + 1) / 3;
            stoogeRec(a, lo, hi - t, cmp);
            stoogeRec(a, lo + t, hi, cmp);
            stoogeRec(a, lo, hi - t, cmp);
        }
    }

    // ==================== byte ====================

    /**
     * 对整个 byte 数组排序
     */
    public static void sort(byte[] a) {
        sortByte(a, 0, a.length);
    }

    /**
     * 对 [fromIndex, a.length) 区间排序
     */
    public static void sort(byte[] a, int fromIndex) {
        sortByte(a, fromIndex, a.length);
    }

    /**
     * 对 [fromIndex, toIndex) 区间排序
     */
    public static void sort(byte[] a, int fromIndex, int toIndex) {
        sortByte(a, fromIndex, toIndex);
    }

    private static void sortByte(byte[] a, int from, int to) {
        IndexChecks.checkFromToIndex(from, to, a.length);
        stooge(a, from, to);
    }

    private static boolean gt(byte[] a, int i, int j) {
        return a[i] > a[j];
    }

    private static void swap(byte[] a, int i, int j) {
        byte tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    private static void stooge(byte[] a, int from, int to) {
        if (to - from < 2) {
            return;
        }
        stoogeRec(a, from, to - 1);
    }

    private static void stoogeRec(byte[] a, int lo, int hi) {
        if (gt(a, lo, hi)) {
            swap(a, lo, hi);
        }
        if (hi - lo + 1 > 2) {
            int t = (hi - lo + 1) / 3;
            stoogeRec(a, lo, hi - t);
            stoogeRec(a, lo + t, hi);
            stoogeRec(a, lo, hi - t);
        }
    }

    // ==================== short ====================

    /**
     * 对整个 short 数组排序
     */
    public static void sort(short[] a) {
        sortShort(a, 0, a.length);
    }

    /**
     * 对 [fromIndex, a.length) 区间排序
     */
    public static void sort(short[] a, int fromIndex) {
        sortShort(a, fromIndex, a.length);
    }

    /**
     * 对 [fromIndex, toIndex) 区间排序
     */
    public static void sort(short[] a, int fromIndex, int toIndex) {
        sortShort(a, fromIndex, toIndex);
    }

    private static void sortShort(short[] a, int from, int to) {
        IndexChecks.checkFromToIndex(from, to, a.length);
        stooge(a, from, to);
    }

    private static boolean gt(short[] a, int i, int j) {
        return a[i] > a[j];
    }

    private static void swap(short[] a, int i, int j) {
        short tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    private static void stooge(short[] a, int from, int to) {
        if (to - from < 2) {
            return;
        }
        stoogeRec(a, from, to - 1);
    }

    private static void stoogeRec(short[] a, int lo, int hi) {
        if (gt(a, lo, hi)) {
            swap(a, lo, hi);
        }
        if (hi - lo + 1 > 2) {
            int t = (hi - lo + 1) / 3;
            stoogeRec(a, lo, hi - t);
            stoogeRec(a, lo + t, hi);
            stoogeRec(a, lo, hi - t);
        }
    }

    // ==================== int ====================

    /**
     * 对整个 int 数组排序
     */
    public static void sort(int[] a) {
        sortInt(a, 0, a.length);
    }

    /**
     * 对 [fromIndex, a.length) 区间排序
     */
    public static void sort(int[] a, int fromIndex) {
        sortInt(a, fromIndex, a.length);
    }

    /**
     * 对 [fromIndex, toIndex) 区间排序
     */
    public static void sort(int[] a, int fromIndex, int toIndex) {
        sortInt(a, fromIndex, toIndex);
    }

    private static void sortInt(int[] a, int from, int to) {
        IndexChecks.checkFromToIndex(from, to, a.length);
        stooge(a, from, to);
    }

    private static boolean gt(int[] a, int i, int j) {
        return a[i] > a[j];
    }

    private static void swap(int[] a, int i, int j) {
        int tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    private static void stooge(int[] a, int from, int to) {
        if (to - from < 2) {
            return;
        }
        stoogeRec(a, from, to - 1);
    }

    private static void stoogeRec(int[] a, int lo, int hi) {
        if (gt(a, lo, hi)) {
            swap(a, lo, hi);
        }
        if (hi - lo + 1 > 2) {
            int t = (hi - lo + 1) / 3;
            stoogeRec(a, lo, hi - t);
            stoogeRec(a, lo + t, hi);
            stoogeRec(a, lo, hi - t);
        }
    }

    // ==================== long ====================

    /**
     * 对整个 long 数组排序
     */
    public static void sort(long[] a) {
        sortLong(a, 0, a.length);
    }

    /**
     * 对 [fromIndex, a.length) 区间排序
     */
    public static void sort(long[] a, int fromIndex) {
        sortLong(a, fromIndex, a.length);
    }

    /**
     * 对 [fromIndex, toIndex) 区间排序
     */
    public static void sort(long[] a, int fromIndex, int toIndex) {
        sortLong(a, fromIndex, toIndex);
    }

    private static void sortLong(long[] a, int from, int to) {
        IndexChecks.checkFromToIndex(from, to, a.length);
        stooge(a, from, to);
    }

    private static boolean gt(long[] a, int i, int j) {
        return a[i] > a[j];
    }

    private static void swap(long[] a, int i, int j) {
        long tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    private static void stooge(long[] a, int from, int to) {
        if (to - from < 2) {
            return;
        }
        stoogeRec(a, from, to - 1);
    }

    private static void stoogeRec(long[] a, int lo, int hi) {
        if (gt(a, lo, hi)) {
            swap(a, lo, hi);
        }
        if (hi - lo + 1 > 2) {
            int t = (hi - lo + 1) / 3;
            stoogeRec(a, lo, hi - t);
            stoogeRec(a, lo + t, hi);
            stoogeRec(a, lo, hi - t);
        }
    }

    // ==================== float ====================

    /**
     * 对整个 float 数组排序
     */
    public static void sort(float[] a) {
        sortFloat(a, 0, a.length);
    }

    /**
     * 对 [fromIndex, a.length) 区间排序
     */
    public static void sort(float[] a, int fromIndex) {
        sortFloat(a, fromIndex, a.length);
    }

    /**
     * 对 [fromIndex, toIndex) 区间排序
     */
    public static void sort(float[] a, int fromIndex, int toIndex) {
        sortFloat(a, fromIndex, toIndex);
    }

    private static void sortFloat(float[] a, int from, int to) {
        IndexChecks.checkFromToIndex(from, to, a.length);
        stooge(a, from, to);
    }

    private static boolean gt(float[] a, int i, int j) {
        return Float.compare(a[i], a[j]) > 0;
    }

    private static void swap(float[] a, int i, int j) {
        float tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    private static void stooge(float[] a, int from, int to) {
        if (to - from < 2) {
            return;
        }
        stoogeRec(a, from, to - 1);
    }

    private static void stoogeRec(float[] a, int lo, int hi) {
        if (gt(a, lo, hi)) {
            swap(a, lo, hi);
        }
        if (hi - lo + 1 > 2) {
            int t = (hi - lo + 1) / 3;
            stoogeRec(a, lo, hi - t);
            stoogeRec(a, lo + t, hi);
            stoogeRec(a, lo, hi - t);
        }
    }

    // ==================== double ====================

    /**
     * 对整个 double 数组排序
     */
    public static void sort(double[] a) {
        sortDouble(a, 0, a.length);
    }

    /**
     * 对 [fromIndex, a.length) 区间排序
     */
    public static void sort(double[] a, int fromIndex) {
        sortDouble(a, fromIndex, a.length);
    }

    /**
     * 对 [fromIndex, toIndex) 区间排序
     */
    public static void sort(double[] a, int fromIndex, int toIndex) {
        sortDouble(a, fromIndex, toIndex);
    }

    private static void sortDouble(double[] a, int from, int to) {
        IndexChecks.checkFromToIndex(from, to, a.length);
        stooge(a, from, to);
    }

    private static boolean gt(double[] a, int i, int j) {
        return Double.compare(a[i], a[j]) > 0;
    }

    private static void swap(double[] a, int i, int j) {
        double tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    private static void stooge(double[] a, int from, int to) {
        if (to - from < 2) {
            return;
        }
        stoogeRec(a, from, to - 1);
    }

    private static void stoogeRec(double[] a, int lo, int hi) {
        if (gt(a, lo, hi)) {
            swap(a, lo, hi);
        }
        if (hi - lo + 1 > 2) {
            int t = (hi - lo + 1) / 3;
            stoogeRec(a, lo, hi - t);
            stoogeRec(a, lo + t, hi);
            stoogeRec(a, lo, hi - t);
        }
    }

    // ==================== char ====================

    /**
     * 对整个 char 数组排序（无符号 16 位整数序）
     */
    public static void sort(char[] a) {
        sortChar(a, 0, a.length);
    }

    /**
     * 对 [fromIndex, a.length) 区间排序（无符号 16 位整数序）
     */
    public static void sort(char[] a, int fromIndex) {
        sortChar(a, fromIndex, a.length);
    }

    /**
     * 对 [fromIndex, toIndex) 区间排序（无符号 16 位整数序）
     */
    public static void sort(char[] a, int fromIndex, int toIndex) {
        sortChar(a, fromIndex, toIndex);
    }

    private static void sortChar(char[] a, int from, int to) {
        IndexChecks.checkFromToIndex(from, to, a.length);
        stooge(a, from, to);
    }

    private static boolean gt(char[] a, int i, int j) {
        return a[i] > a[j];
    }

    private static void swap(char[] a, int i, int j) {
        char tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    private static void stooge(char[] a, int from, int to) {
        if (to - from < 2) {
            return;
        }
        stoogeRec(a, from, to - 1);
    }

    private static void stoogeRec(char[] a, int lo, int hi) {
        if (gt(a, lo, hi)) {
            swap(a, lo, hi);
        }
        if (hi - lo + 1 > 2) {
            int t = (hi - lo + 1) / 3;
            stoogeRec(a, lo, hi - t);
            stoogeRec(a, lo + t, hi);
            stoogeRec(a, lo, hi - t);
        }
    }
}
