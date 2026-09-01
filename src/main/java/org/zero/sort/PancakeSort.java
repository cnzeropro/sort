package org.zero.sort;

import java.util.Comparator;
import java.util.List;

/**
 * 煎饼排序（不稳定，前缀翻转）。
 * <p>
 * 每轮在未排序区间内找最大值，若不在队首则先翻转使其到队首，再整段翻转送到区间末尾；
 * 全程只用"前缀翻转"这一种移动操作。比较最好/平均/最坏 O(n^2)，
 * 翻转至多 2(n-1) 次、单次翻转 O(n)，数据移动 O(n^2)；空间 O(1)。
 * <p>
 * 支持任意 {@link Comparable} 对象数组 / {@link List}、任意 {@link Comparator} 对象数组 /
 * {@link List}，以及 byte / short / int / long / float / double / char 原始类型。
 * float / double 使用 {@link Float#compare} / {@link Double#compare} 全序（NaN 最后），
 * char 按无符号 16 位整数序。
 *
 * @author Zero
 */
public final class PancakeSort {

    private PancakeSort() {
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
        pancake(a, from, to, Comparator.naturalOrder());
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
        pancake(a, from, to, comparator);
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

    private static <T> boolean lt(T[] a, int i, int j, Comparator<? super T> cmp) {
        return cmp.compare(a[i], a[j]) < 0;
    }

    private static <T> void swap(T[] a, int i, int j) {
        T tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    private static <T> void pancake(T[] a, int from, int to, Comparator<? super T> cmp) {
        for (int size = to - from; size > 1; size--) {
            int maxIdx = from;
            for (int i = from + 1; i < from + size; i++) {
                if (lt(a, maxIdx, i, cmp)) {
                    maxIdx = i;
                }
            }
            if (maxIdx == from + size - 1) {
                continue;
            }
            if (maxIdx != from) {
                flip(a, from, maxIdx);
            }
            flip(a, from, from + size - 1);
        }
    }

    private static <T> void flip(T[] a, int from, int toIdx) {
        while (from < toIdx) {
            swap(a, from, toIdx);
            from++;
            toIdx--;
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
        pancake(a, from, to);
    }

    private static boolean lt(byte[] a, int i, int j) {
        return a[i] < a[j];
    }

    private static void swap(byte[] a, int i, int j) {
        byte tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    private static void pancake(byte[] a, int from, int to) {
        for (int size = to - from; size > 1; size--) {
            int maxIdx = from;
            for (int i = from + 1; i < from + size; i++) {
                if (lt(a, maxIdx, i)) {
                    maxIdx = i;
                }
            }
            if (maxIdx == from + size - 1) {
                continue;
            }
            if (maxIdx != from) {
                flip(a, from, maxIdx);
            }
            flip(a, from, from + size - 1);
        }
    }

    private static void flip(byte[] a, int from, int toIdx) {
        while (from < toIdx) {
            swap(a, from, toIdx);
            from++;
            toIdx--;
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
        pancake(a, from, to);
    }

    private static boolean lt(short[] a, int i, int j) {
        return a[i] < a[j];
    }

    private static void swap(short[] a, int i, int j) {
        short tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    private static void pancake(short[] a, int from, int to) {
        for (int size = to - from; size > 1; size--) {
            int maxIdx = from;
            for (int i = from + 1; i < from + size; i++) {
                if (lt(a, maxIdx, i)) {
                    maxIdx = i;
                }
            }
            if (maxIdx == from + size - 1) {
                continue;
            }
            if (maxIdx != from) {
                flip(a, from, maxIdx);
            }
            flip(a, from, from + size - 1);
        }
    }

    private static void flip(short[] a, int from, int toIdx) {
        while (from < toIdx) {
            swap(a, from, toIdx);
            from++;
            toIdx--;
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
        pancake(a, from, to);
    }

    private static boolean lt(int[] a, int i, int j) {
        return a[i] < a[j];
    }

    private static void swap(int[] a, int i, int j) {
        int tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    private static void pancake(int[] a, int from, int to) {
        for (int size = to - from; size > 1; size--) {
            int maxIdx = from;
            for (int i = from + 1; i < from + size; i++) {
                if (lt(a, maxIdx, i)) {
                    maxIdx = i;
                }
            }
            if (maxIdx == from + size - 1) {
                continue;
            }
            if (maxIdx != from) {
                flip(a, from, maxIdx);
            }
            flip(a, from, from + size - 1);
        }
    }

    private static void flip(int[] a, int from, int toIdx) {
        while (from < toIdx) {
            swap(a, from, toIdx);
            from++;
            toIdx--;
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
        pancake(a, from, to);
    }

    private static boolean lt(long[] a, int i, int j) {
        return a[i] < a[j];
    }

    private static void swap(long[] a, int i, int j) {
        long tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    private static void pancake(long[] a, int from, int to) {
        for (int size = to - from; size > 1; size--) {
            int maxIdx = from;
            for (int i = from + 1; i < from + size; i++) {
                if (lt(a, maxIdx, i)) {
                    maxIdx = i;
                }
            }
            if (maxIdx == from + size - 1) {
                continue;
            }
            if (maxIdx != from) {
                flip(a, from, maxIdx);
            }
            flip(a, from, from + size - 1);
        }
    }

    private static void flip(long[] a, int from, int toIdx) {
        while (from < toIdx) {
            swap(a, from, toIdx);
            from++;
            toIdx--;
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
        pancake(a, from, to);
    }

    private static boolean lt(float[] a, int i, int j) {
        return Float.compare(a[i], a[j]) < 0;
    }

    private static void swap(float[] a, int i, int j) {
        float tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    private static void pancake(float[] a, int from, int to) {
        for (int size = to - from; size > 1; size--) {
            int maxIdx = from;
            for (int i = from + 1; i < from + size; i++) {
                if (lt(a, maxIdx, i)) {
                    maxIdx = i;
                }
            }
            if (maxIdx == from + size - 1) {
                continue;
            }
            if (maxIdx != from) {
                flip(a, from, maxIdx);
            }
            flip(a, from, from + size - 1);
        }
    }

    private static void flip(float[] a, int from, int toIdx) {
        while (from < toIdx) {
            swap(a, from, toIdx);
            from++;
            toIdx--;
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
        pancake(a, from, to);
    }

    private static boolean lt(double[] a, int i, int j) {
        return Double.compare(a[i], a[j]) < 0;
    }

    private static void swap(double[] a, int i, int j) {
        double tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    private static void pancake(double[] a, int from, int to) {
        for (int size = to - from; size > 1; size--) {
            int maxIdx = from;
            for (int i = from + 1; i < from + size; i++) {
                if (lt(a, maxIdx, i)) {
                    maxIdx = i;
                }
            }
            if (maxIdx == from + size - 1) {
                continue;
            }
            if (maxIdx != from) {
                flip(a, from, maxIdx);
            }
            flip(a, from, from + size - 1);
        }
    }

    private static void flip(double[] a, int from, int toIdx) {
        while (from < toIdx) {
            swap(a, from, toIdx);
            from++;
            toIdx--;
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
        pancake(a, from, to);
    }

    private static boolean lt(char[] a, int i, int j) {
        return a[i] < a[j];
    }

    private static void swap(char[] a, int i, int j) {
        char tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    private static void pancake(char[] a, int from, int to) {
        for (int size = to - from; size > 1; size--) {
            int maxIdx = from;
            for (int i = from + 1; i < from + size; i++) {
                if (lt(a, maxIdx, i)) {
                    maxIdx = i;
                }
            }
            if (maxIdx == from + size - 1) {
                continue;
            }
            if (maxIdx != from) {
                flip(a, from, maxIdx);
            }
            flip(a, from, from + size - 1);
        }
    }

    private static void flip(char[] a, int from, int toIdx) {
        while (from < toIdx) {
            swap(a, from, toIdx);
            from++;
            toIdx--;
        }
    }
}
