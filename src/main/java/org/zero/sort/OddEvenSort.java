package org.zero.sort;

import java.util.Comparator;
import java.util.List;

/**
 * 奇偶排序（稳定，奇偶轮换）。
 * <p>
 * 交替对相对区间起点索引为奇、偶的相邻元素对进行比较交换，整轮奇偶两趟均无交换即完成。
 * 最好 O(n)（已有序）、平均/最坏 O(n^2)；空间 O(1)。
 * <p>
 * 支持任意 {@link Comparable} 对象数组 / {@link List}、任意 {@link Comparator} 对象数组 /
 * {@link List}，以及 byte / short / int / long / float / double / char 原始类型。
 * float / double 使用 {@link Float#compare} / {@link Double#compare} 全序（NaN 最后），
 * char 按无符号 16 位整数序。
 *
 * @author Zero
 */
public final class OddEvenSort {

    private OddEvenSort() {
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
        oddEven(a, from, to, Comparator.naturalOrder());
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
        oddEven(a, from, to, comparator);
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

    private static <T> void oddEven(T[] a, int from, int to, Comparator<? super T> cmp) {
        boolean sorted = false;
        while (!sorted) {
            sorted = true;
            for (int i = from + 1; i + 1 < to; i += 2) {
                if (cmp.compare(a[i], a[i + 1]) > 0) {
                    T tmp = a[i];
                    a[i] = a[i + 1];
                    a[i + 1] = tmp;
                    sorted = false;
                }
            }
            for (int i = from; i + 1 < to; i += 2) {
                if (cmp.compare(a[i], a[i + 1]) > 0) {
                    T tmp = a[i];
                    a[i] = a[i + 1];
                    a[i + 1] = tmp;
                    sorted = false;
                }
            }
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
        oddEven(a, from, to);
    }

    private static void oddEven(byte[] a, int from, int to) {
        boolean sorted = false;
        while (!sorted) {
            sorted = true;
            for (int i = from + 1; i + 1 < to; i += 2) {
                if (a[i] > a[i + 1]) {
                    byte tmp = a[i];
                    a[i] = a[i + 1];
                    a[i + 1] = tmp;
                    sorted = false;
                }
            }
            for (int i = from; i + 1 < to; i += 2) {
                if (a[i] > a[i + 1]) {
                    byte tmp = a[i];
                    a[i] = a[i + 1];
                    a[i + 1] = tmp;
                    sorted = false;
                }
            }
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
        oddEven(a, from, to);
    }

    private static void oddEven(short[] a, int from, int to) {
        boolean sorted = false;
        while (!sorted) {
            sorted = true;
            for (int i = from + 1; i + 1 < to; i += 2) {
                if (a[i] > a[i + 1]) {
                    short tmp = a[i];
                    a[i] = a[i + 1];
                    a[i + 1] = tmp;
                    sorted = false;
                }
            }
            for (int i = from; i + 1 < to; i += 2) {
                if (a[i] > a[i + 1]) {
                    short tmp = a[i];
                    a[i] = a[i + 1];
                    a[i + 1] = tmp;
                    sorted = false;
                }
            }
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
        oddEven(a, from, to);
    }

    private static void oddEven(int[] a, int from, int to) {
        boolean sorted = false;
        while (!sorted) {
            sorted = true;
            for (int i = from + 1; i + 1 < to; i += 2) {
                if (a[i] > a[i + 1]) {
                    int tmp = a[i];
                    a[i] = a[i + 1];
                    a[i + 1] = tmp;
                    sorted = false;
                }
            }
            for (int i = from; i + 1 < to; i += 2) {
                if (a[i] > a[i + 1]) {
                    int tmp = a[i];
                    a[i] = a[i + 1];
                    a[i + 1] = tmp;
                    sorted = false;
                }
            }
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
        oddEven(a, from, to);
    }

    private static void oddEven(long[] a, int from, int to) {
        boolean sorted = false;
        while (!sorted) {
            sorted = true;
            for (int i = from + 1; i + 1 < to; i += 2) {
                if (a[i] > a[i + 1]) {
                    long tmp = a[i];
                    a[i] = a[i + 1];
                    a[i + 1] = tmp;
                    sorted = false;
                }
            }
            for (int i = from; i + 1 < to; i += 2) {
                if (a[i] > a[i + 1]) {
                    long tmp = a[i];
                    a[i] = a[i + 1];
                    a[i + 1] = tmp;
                    sorted = false;
                }
            }
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
        oddEven(a, from, to);
    }

    private static void oddEven(float[] a, int from, int to) {
        boolean sorted = false;
        while (!sorted) {
            sorted = true;
            for (int i = from + 1; i + 1 < to; i += 2) {
                if (Float.compare(a[i], a[i + 1]) > 0) {
                    float tmp = a[i];
                    a[i] = a[i + 1];
                    a[i + 1] = tmp;
                    sorted = false;
                }
            }
            for (int i = from; i + 1 < to; i += 2) {
                if (Float.compare(a[i], a[i + 1]) > 0) {
                    float tmp = a[i];
                    a[i] = a[i + 1];
                    a[i + 1] = tmp;
                    sorted = false;
                }
            }
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
        oddEven(a, from, to);
    }

    private static void oddEven(double[] a, int from, int to) {
        boolean sorted = false;
        while (!sorted) {
            sorted = true;
            for (int i = from + 1; i + 1 < to; i += 2) {
                if (Double.compare(a[i], a[i + 1]) > 0) {
                    double tmp = a[i];
                    a[i] = a[i + 1];
                    a[i + 1] = tmp;
                    sorted = false;
                }
            }
            for (int i = from; i + 1 < to; i += 2) {
                if (Double.compare(a[i], a[i + 1]) > 0) {
                    double tmp = a[i];
                    a[i] = a[i + 1];
                    a[i + 1] = tmp;
                    sorted = false;
                }
            }
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
        oddEven(a, from, to);
    }

    private static void oddEven(char[] a, int from, int to) {
        boolean sorted = false;
        while (!sorted) {
            sorted = true;
            for (int i = from + 1; i + 1 < to; i += 2) {
                if (a[i] > a[i + 1]) {
                    char tmp = a[i];
                    a[i] = a[i + 1];
                    a[i + 1] = tmp;
                    sorted = false;
                }
            }
            for (int i = from; i + 1 < to; i += 2) {
                if (a[i] > a[i + 1]) {
                    char tmp = a[i];
                    a[i] = a[i + 1];
                    a[i + 1] = tmp;
                    sorted = false;
                }
            }
        }
    }
}
