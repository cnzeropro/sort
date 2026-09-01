package org.zero.sort;

import java.util.Comparator;
import java.util.List;

/**
 * 插入排序（稳定，移位式）。
 * <p>
 * 从左到右把每个元素移位插入左侧已有序区间的正确位置；相等元素不跨越，保证稳定。
 * 最好 O(n)（已有序）、平均/最坏 O(n^2)；空间 O(1)。
 * <p>
 * 支持任意 {@link Comparable} 对象数组 / {@link List}、任意 {@link Comparator} 对象数组 /
 * {@link List}，以及 byte / short / int / long / float / double / char 原始类型。
 * float / double 使用 {@link Float#compare} / {@link Double#compare} 全序（NaN 最后），
 * char 按无符号 16 位整数序。
 *
 * @author Zero
 */
public final class InsertionSort {

    private InsertionSort() {
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
        insertion(a, from, to, Comparator.naturalOrder());
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
        insertion(a, from, to, comparator);
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

    private static <T> void insertion(T[] a, int from, int to, Comparator<? super T> cmp) {
        for (int i = from + 1; i < to; i++) {
            T key = a[i];
            int j = i - 1;
            while (j >= from && cmp.compare(a[j], key) > 0) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
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
        insertion(a, from, to);
    }

    private static void insertion(byte[] a, int from, int to) {
        for (int i = from + 1; i < to; i++) {
            byte key = a[i];
            int j = i - 1;
            while (j >= from && a[j] > key) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
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
        insertion(a, from, to);
    }

    private static void insertion(short[] a, int from, int to) {
        for (int i = from + 1; i < to; i++) {
            short key = a[i];
            int j = i - 1;
            while (j >= from && a[j] > key) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
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
        insertion(a, from, to);
    }

    private static void insertion(int[] a, int from, int to) {
        for (int i = from + 1; i < to; i++) {
            int key = a[i];
            int j = i - 1;
            while (j >= from && a[j] > key) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
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
        insertion(a, from, to);
    }

    private static void insertion(long[] a, int from, int to) {
        for (int i = from + 1; i < to; i++) {
            long key = a[i];
            int j = i - 1;
            while (j >= from && a[j] > key) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
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
        insertion(a, from, to);
    }

    private static void insertion(float[] a, int from, int to) {
        for (int i = from + 1; i < to; i++) {
            float key = a[i];
            int j = i - 1;
            while (j >= from && Float.compare(a[j], key) > 0) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
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
        insertion(a, from, to);
    }

    private static void insertion(double[] a, int from, int to) {
        for (int i = from + 1; i < to; i++) {
            double key = a[i];
            int j = i - 1;
            while (j >= from && Double.compare(a[j], key) > 0) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
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
        insertion(a, from, to);
    }

    private static void insertion(char[] a, int from, int to) {
        for (int i = from + 1; i < to; i++) {
            char key = a[i];
            int j = i - 1;
            while (j >= from && a[j] > key) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }
}
