package org.zero.sort;

import java.util.Comparator;
import java.util.List;

/**
 * 循环排序（不稳定，最少写入次数）。
 * <p>
 * 对每个元素统计区间内严格小于它的元素个数以确定最终位置，再沿"环"逐个旋转放置；
 * 已在最终位置的元素零写入，其余每个元素至多被写入一次最终位置，
 * 相等元素通过相等跳过避免重复占位，是写入次数最少的原址排序。
 * 比较最好/最坏均为 O(n^2)（已有序时零写入）；空间 O(1)。
 * <p>
 * 支持任意 {@link Comparable} 对象数组 / {@link List}、任意 {@link Comparator} 对象数组 /
 * {@link List}，以及 byte / short / int / long / float / double / char 原始类型。
 * float / double 使用 {@link Float#compare} / {@link Double#compare} 全序（NaN 最后），
 * char 按无符号 16 位整数序。
 *
 * @author Zero
 */
public final class CycleSort {

    private CycleSort() {
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
        cycle(a, from, to, Comparator.naturalOrder());
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
        cycle(a, from, to, comparator);
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

    private static <T> boolean ltItem(T[] a, int i, T value, Comparator<? super T> cmp) {
        return cmp.compare(a[i], value) < 0;
    }

    private static <T> void cycle(T[] a, int from, int to, Comparator<? super T> cmp) {
        for (int cycleStart = from; cycleStart < to - 1; cycleStart++) {
            T item = a[cycleStart];
            int pos = cycleStart;
            for (int i = cycleStart + 1; i < to; i++) {
                if (ltItem(a, i, item, cmp)) {
                    pos++;
                }
            }
            if (pos == cycleStart) {
                continue;
            }
            while (pos < to && cmp.compare(item, a[pos]) == 0) {
                pos++;
            }
            T tmp = a[pos];
            a[pos] = item;
            item = tmp;
            while (pos != cycleStart) {
                pos = cycleStart;
                for (int i = cycleStart + 1; i < to; i++) {
                    if (ltItem(a, i, item, cmp)) {
                        pos++;
                    }
                }
                while (pos < to && cmp.compare(item, a[pos]) == 0) {
                    pos++;
                }
                tmp = a[pos];
                a[pos] = item;
                item = tmp;
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
        cycle(a, from, to);
    }

    private static boolean ltItem(byte[] a, int i, byte value) {
        return a[i] < value;
    }

    private static boolean eqVal(byte value, byte[] a, int i) {
        return value == a[i];
    }

    private static void cycle(byte[] a, int from, int to) {
        for (int cycleStart = from; cycleStart < to - 1; cycleStart++) {
            byte item = a[cycleStart];
            int pos = cycleStart;
            for (int i = cycleStart + 1; i < to; i++) {
                if (ltItem(a, i, item)) {
                    pos++;
                }
            }
            if (pos == cycleStart) {
                continue;
            }
            while (pos < to && eqVal(item, a, pos)) {
                pos++;
            }
            byte tmp = a[pos];
            a[pos] = item;
            item = tmp;
            while (pos != cycleStart) {
                pos = cycleStart;
                for (int i = cycleStart + 1; i < to; i++) {
                    if (ltItem(a, i, item)) {
                        pos++;
                    }
                }
                while (pos < to && eqVal(item, a, pos)) {
                    pos++;
                }
                tmp = a[pos];
                a[pos] = item;
                item = tmp;
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
        cycle(a, from, to);
    }

    private static boolean ltItem(short[] a, int i, short value) {
        return a[i] < value;
    }

    private static boolean eqVal(short value, short[] a, int i) {
        return value == a[i];
    }

    private static void cycle(short[] a, int from, int to) {
        for (int cycleStart = from; cycleStart < to - 1; cycleStart++) {
            short item = a[cycleStart];
            int pos = cycleStart;
            for (int i = cycleStart + 1; i < to; i++) {
                if (ltItem(a, i, item)) {
                    pos++;
                }
            }
            if (pos == cycleStart) {
                continue;
            }
            while (pos < to && eqVal(item, a, pos)) {
                pos++;
            }
            short tmp = a[pos];
            a[pos] = item;
            item = tmp;
            while (pos != cycleStart) {
                pos = cycleStart;
                for (int i = cycleStart + 1; i < to; i++) {
                    if (ltItem(a, i, item)) {
                        pos++;
                    }
                }
                while (pos < to && eqVal(item, a, pos)) {
                    pos++;
                }
                tmp = a[pos];
                a[pos] = item;
                item = tmp;
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
        cycle(a, from, to);
    }

    private static boolean ltItem(int[] a, int i, int value) {
        return a[i] < value;
    }

    private static boolean eqVal(int value, int[] a, int i) {
        return value == a[i];
    }

    private static void cycle(int[] a, int from, int to) {
        for (int cycleStart = from; cycleStart < to - 1; cycleStart++) {
            int item = a[cycleStart];
            int pos = cycleStart;
            for (int i = cycleStart + 1; i < to; i++) {
                if (ltItem(a, i, item)) {
                    pos++;
                }
            }
            if (pos == cycleStart) {
                continue;
            }
            while (pos < to && eqVal(item, a, pos)) {
                pos++;
            }
            int tmp = a[pos];
            a[pos] = item;
            item = tmp;
            while (pos != cycleStart) {
                pos = cycleStart;
                for (int i = cycleStart + 1; i < to; i++) {
                    if (ltItem(a, i, item)) {
                        pos++;
                    }
                }
                while (pos < to && eqVal(item, a, pos)) {
                    pos++;
                }
                tmp = a[pos];
                a[pos] = item;
                item = tmp;
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
        cycle(a, from, to);
    }

    private static boolean ltItem(long[] a, int i, long value) {
        return a[i] < value;
    }

    private static boolean eqVal(long value, long[] a, int i) {
        return value == a[i];
    }

    private static void cycle(long[] a, int from, int to) {
        for (int cycleStart = from; cycleStart < to - 1; cycleStart++) {
            long item = a[cycleStart];
            int pos = cycleStart;
            for (int i = cycleStart + 1; i < to; i++) {
                if (ltItem(a, i, item)) {
                    pos++;
                }
            }
            if (pos == cycleStart) {
                continue;
            }
            while (pos < to && eqVal(item, a, pos)) {
                pos++;
            }
            long tmp = a[pos];
            a[pos] = item;
            item = tmp;
            while (pos != cycleStart) {
                pos = cycleStart;
                for (int i = cycleStart + 1; i < to; i++) {
                    if (ltItem(a, i, item)) {
                        pos++;
                    }
                }
                while (pos < to && eqVal(item, a, pos)) {
                    pos++;
                }
                tmp = a[pos];
                a[pos] = item;
                item = tmp;
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
        cycle(a, from, to);
    }

    private static boolean ltItem(float[] a, int i, float value) {
        return Float.compare(a[i], value) < 0;
    }

    private static boolean eqVal(float value, float[] a, int i) {
        return Float.compare(value, a[i]) == 0;
    }

    private static void cycle(float[] a, int from, int to) {
        for (int cycleStart = from; cycleStart < to - 1; cycleStart++) {
            float item = a[cycleStart];
            int pos = cycleStart;
            for (int i = cycleStart + 1; i < to; i++) {
                if (ltItem(a, i, item)) {
                    pos++;
                }
            }
            if (pos == cycleStart) {
                continue;
            }
            while (pos < to && eqVal(item, a, pos)) {
                pos++;
            }
            float tmp = a[pos];
            a[pos] = item;
            item = tmp;
            while (pos != cycleStart) {
                pos = cycleStart;
                for (int i = cycleStart + 1; i < to; i++) {
                    if (ltItem(a, i, item)) {
                        pos++;
                    }
                }
                while (pos < to && eqVal(item, a, pos)) {
                    pos++;
                }
                tmp = a[pos];
                a[pos] = item;
                item = tmp;
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
        cycle(a, from, to);
    }

    private static boolean ltItem(double[] a, int i, double value) {
        return Double.compare(a[i], value) < 0;
    }

    private static boolean eqVal(double value, double[] a, int i) {
        return Double.compare(value, a[i]) == 0;
    }

    private static void cycle(double[] a, int from, int to) {
        for (int cycleStart = from; cycleStart < to - 1; cycleStart++) {
            double item = a[cycleStart];
            int pos = cycleStart;
            for (int i = cycleStart + 1; i < to; i++) {
                if (ltItem(a, i, item)) {
                    pos++;
                }
            }
            if (pos == cycleStart) {
                continue;
            }
            while (pos < to && eqVal(item, a, pos)) {
                pos++;
            }
            double tmp = a[pos];
            a[pos] = item;
            item = tmp;
            while (pos != cycleStart) {
                pos = cycleStart;
                for (int i = cycleStart + 1; i < to; i++) {
                    if (ltItem(a, i, item)) {
                        pos++;
                    }
                }
                while (pos < to && eqVal(item, a, pos)) {
                    pos++;
                }
                tmp = a[pos];
                a[pos] = item;
                item = tmp;
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
        cycle(a, from, to);
    }

    private static boolean ltItem(char[] a, int i, char value) {
        return a[i] < value;
    }

    private static boolean eqVal(char value, char[] a, int i) {
        return value == a[i];
    }

    private static void cycle(char[] a, int from, int to) {
        for (int cycleStart = from; cycleStart < to - 1; cycleStart++) {
            char item = a[cycleStart];
            int pos = cycleStart;
            for (int i = cycleStart + 1; i < to; i++) {
                if (ltItem(a, i, item)) {
                    pos++;
                }
            }
            if (pos == cycleStart) {
                continue;
            }
            while (pos < to && eqVal(item, a, pos)) {
                pos++;
            }
            char tmp = a[pos];
            a[pos] = item;
            item = tmp;
            while (pos != cycleStart) {
                pos = cycleStart;
                for (int i = cycleStart + 1; i < to; i++) {
                    if (ltItem(a, i, item)) {
                        pos++;
                    }
                }
                while (pos < to && eqVal(item, a, pos)) {
                    pos++;
                }
                tmp = a[pos];
                a[pos] = item;
                item = tmp;
            }
        }
    }
}
