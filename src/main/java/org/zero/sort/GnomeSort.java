package org.zero.sort;

import java.util.Comparator;
import java.util.List;

/**
 * 地精排序（稳定）。
 * <p>
 * 每步比较相邻元素：逆序则交换并回退一格，否则前进一格；等价于交换式的插入排序。
 * 最好 O(n)（已有序）、平均/最坏 O(n^2)；空间 O(1)。
 * <p>
 * 支持任意 {@link Comparable} 对象数组 / {@link List}、任意 {@link Comparator} 对象数组 /
 * {@link List}，以及 byte / short / int / long / float / double / char 原始类型。
 * float / double 使用 {@link Float#compare} / {@link Double#compare} 全序（NaN 最后），
 * char 按无符号 16 位整数序。
 *
 * @author Zero
 */
public final class GnomeSort {

    private GnomeSort() {
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
        gnome(a, from, to, Comparator.naturalOrder());
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
        gnome(a, from, to, comparator);
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

    private static <T> void gnome(T[] a, int from, int to, Comparator<? super T> cmp) {
        int i = from;
        while (i < to) {
            if (i == from || cmp.compare(a[i], a[i - 1]) >= 0) {
                i++;
            } else {
                T tmp = a[i];
                a[i] = a[i - 1];
                a[i - 1] = tmp;
                i--;
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
        gnome(a, from, to);
    }

    private static void gnome(byte[] a, int from, int to) {
        int i = from;
        while (i < to) {
            if (i == from || a[i] >= a[i - 1]) {
                i++;
            } else {
                byte tmp = a[i];
                a[i] = a[i - 1];
                a[i - 1] = tmp;
                i--;
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
        gnome(a, from, to);
    }

    private static void gnome(short[] a, int from, int to) {
        int i = from;
        while (i < to) {
            if (i == from || a[i] >= a[i - 1]) {
                i++;
            } else {
                short tmp = a[i];
                a[i] = a[i - 1];
                a[i - 1] = tmp;
                i--;
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
        gnome(a, from, to);
    }

    private static void gnome(int[] a, int from, int to) {
        int i = from;
        while (i < to) {
            if (i == from || a[i] >= a[i - 1]) {
                i++;
            } else {
                int tmp = a[i];
                a[i] = a[i - 1];
                a[i - 1] = tmp;
                i--;
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
        gnome(a, from, to);
    }

    private static void gnome(long[] a, int from, int to) {
        int i = from;
        while (i < to) {
            if (i == from || a[i] >= a[i - 1]) {
                i++;
            } else {
                long tmp = a[i];
                a[i] = a[i - 1];
                a[i - 1] = tmp;
                i--;
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
        gnome(a, from, to);
    }

    private static void gnome(float[] a, int from, int to) {
        int i = from;
        while (i < to) {
            if (i == from || Float.compare(a[i], a[i - 1]) >= 0) {
                i++;
            } else {
                float tmp = a[i];
                a[i] = a[i - 1];
                a[i - 1] = tmp;
                i--;
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
        gnome(a, from, to);
    }

    private static void gnome(double[] a, int from, int to) {
        int i = from;
        while (i < to) {
            if (i == from || Double.compare(a[i], a[i - 1]) >= 0) {
                i++;
            } else {
                double tmp = a[i];
                a[i] = a[i - 1];
                a[i - 1] = tmp;
                i--;
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
        gnome(a, from, to);
    }

    private static void gnome(char[] a, int from, int to) {
        int i = from;
        while (i < to) {
            if (i == from || a[i] >= a[i - 1]) {
                i++;
            } else {
                char tmp = a[i];
                a[i] = a[i - 1];
                a[i - 1] = tmp;
                i--;
            }
        }
    }
}
