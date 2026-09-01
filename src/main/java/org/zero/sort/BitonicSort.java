package org.zero.sort;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * 双调排序（不稳定）。
 * <p>
 * 比较网络式分治：递归构造双调序列后按位归并。内部把长度补齐到 2 的幂（n ≤ 2^30），
 * 补齐出的虚拟索引按"大于一切真实元素"（+∞）处理，因此对任意 Comparator
 * （含 float/double 的 NaN 全序）都正确；排序只在索引层进行，最终按索引置换回写。
 * 比较次数 O(n log^2 n) 且与输入数据无关，比较模式固定，天然适合并行场景。
 * <p>
 * 支持任意 {@link Comparable} 对象数组 / {@link List}、任意 {@link Comparator} 对象数组 /
 * {@link List}，以及 byte / short / int / long / float / double / char 原始类型。
 * float / double 使用 {@link Float#compare} / {@link Double#compare} 全序（NaN 最后），
 * char 按无符号 16 位整数序。
 *
 * @author Zero
 */
public final class BitonicSort {

    private BitonicSort() {
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
        bitonic(a, from, to, Comparator.naturalOrder());
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
        bitonic(a, from, to, comparator);
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

    private static <T> void bitonic(T[] a, int from, int to, Comparator<? super T> cmp) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        int size = 1;
        while (size < n) {
            size <<= 1;
        }
        int[] idx = new int[size];
        for (int i = 0; i < size; i++) {
            idx[i] = i < n ? i : -1;
        }
        for (int k = 2; k <= size; k <<= 1) {
            for (int j = k >> 1; j > 0; j >>= 1) {
                for (int i = 0; i < size; i++) {
                    int l = i ^ j;
                    if (l > i) {
                        if ((i & k) == 0) {
                            if (gtIdx(a, from, idx, i, l, cmp)) {
                                swapIdx(idx, i, l);
                            }
                        } else {
                            if (ltIdx(a, from, idx, i, l, cmp)) {
                                swapIdx(idx, i, l);
                            }
                        }
                    }
                }
            }
        }
        T[] aux = Arrays.copyOfRange(a, from, to);
        for (int i = 0; i < n; i++) {
            a[from + i] = aux[idx[i]];
        }
    }

    private static <T> boolean gtIdx(T[] a, int from, int[] idx, int x, int y, Comparator<? super T> cmp) {
        if (idx[x] < 0) {
            return idx[y] >= 0;
        }
        if (idx[y] < 0) {
            return false;
        }
        return cmp.compare(a[from + idx[x]], a[from + idx[y]]) > 0;
    }

    private static <T> boolean ltIdx(T[] a, int from, int[] idx, int x, int y, Comparator<? super T> cmp) {
        if (idx[y] < 0) {
            return idx[x] >= 0;
        }
        if (idx[x] < 0) {
            return false;
        }
        return cmp.compare(a[from + idx[x]], a[from + idx[y]]) < 0;
    }

    private static void swapIdx(int[] idx, int x, int y) {
        int tmp = idx[x];
        idx[x] = idx[y];
        idx[y] = tmp;
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
        bitonic(a, from, to);
    }

    private static void bitonic(byte[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        int size = 1;
        while (size < n) {
            size <<= 1;
        }
        int[] idx = new int[size];
        for (int i = 0; i < size; i++) {
            idx[i] = i < n ? i : -1;
        }
        for (int k = 2; k <= size; k <<= 1) {
            for (int j = k >> 1; j > 0; j >>= 1) {
                for (int i = 0; i < size; i++) {
                    int l = i ^ j;
                    if (l > i) {
                        if ((i & k) == 0) {
                            if (gtIdx(a, from, idx, i, l)) {
                                swapIdx(idx, i, l);
                            }
                        } else {
                            if (ltIdx(a, from, idx, i, l)) {
                                swapIdx(idx, i, l);
                            }
                        }
                    }
                }
            }
        }
        byte[] aux = new byte[n];
        System.arraycopy(a, from, aux, 0, n);
        for (int i = 0; i < n; i++) {
            a[from + i] = aux[idx[i]];
        }
    }

    private static boolean gtIdx(byte[] a, int from, int[] idx, int x, int y) {
        if (idx[x] < 0) {
            return idx[y] >= 0;
        }
        if (idx[y] < 0) {
            return false;
        }
        return a[from + idx[x]] > a[from + idx[y]];
    }

    private static boolean ltIdx(byte[] a, int from, int[] idx, int x, int y) {
        if (idx[y] < 0) {
            return idx[x] >= 0;
        }
        if (idx[x] < 0) {
            return false;
        }
        return a[from + idx[x]] < a[from + idx[y]];
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
        bitonic(a, from, to);
    }

    private static void bitonic(short[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        int size = 1;
        while (size < n) {
            size <<= 1;
        }
        int[] idx = new int[size];
        for (int i = 0; i < size; i++) {
            idx[i] = i < n ? i : -1;
        }
        for (int k = 2; k <= size; k <<= 1) {
            for (int j = k >> 1; j > 0; j >>= 1) {
                for (int i = 0; i < size; i++) {
                    int l = i ^ j;
                    if (l > i) {
                        if ((i & k) == 0) {
                            if (gtIdx(a, from, idx, i, l)) {
                                swapIdx(idx, i, l);
                            }
                        } else {
                            if (ltIdx(a, from, idx, i, l)) {
                                swapIdx(idx, i, l);
                            }
                        }
                    }
                }
            }
        }
        short[] aux = new short[n];
        System.arraycopy(a, from, aux, 0, n);
        for (int i = 0; i < n; i++) {
            a[from + i] = aux[idx[i]];
        }
    }

    private static boolean gtIdx(short[] a, int from, int[] idx, int x, int y) {
        if (idx[x] < 0) {
            return idx[y] >= 0;
        }
        if (idx[y] < 0) {
            return false;
        }
        return a[from + idx[x]] > a[from + idx[y]];
    }

    private static boolean ltIdx(short[] a, int from, int[] idx, int x, int y) {
        if (idx[y] < 0) {
            return idx[x] >= 0;
        }
        if (idx[x] < 0) {
            return false;
        }
        return a[from + idx[x]] < a[from + idx[y]];
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
        bitonic(a, from, to);
    }

    private static void bitonic(int[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        int size = 1;
        while (size < n) {
            size <<= 1;
        }
        int[] idx = new int[size];
        for (int i = 0; i < size; i++) {
            idx[i] = i < n ? i : -1;
        }
        for (int k = 2; k <= size; k <<= 1) {
            for (int j = k >> 1; j > 0; j >>= 1) {
                for (int i = 0; i < size; i++) {
                    int l = i ^ j;
                    if (l > i) {
                        if ((i & k) == 0) {
                            if (gtIdx(a, from, idx, i, l)) {
                                swapIdx(idx, i, l);
                            }
                        } else {
                            if (ltIdx(a, from, idx, i, l)) {
                                swapIdx(idx, i, l);
                            }
                        }
                    }
                }
            }
        }
        int[] aux = new int[n];
        System.arraycopy(a, from, aux, 0, n);
        for (int i = 0; i < n; i++) {
            a[from + i] = aux[idx[i]];
        }
    }

    private static boolean gtIdx(int[] a, int from, int[] idx, int x, int y) {
        if (idx[x] < 0) {
            return idx[y] >= 0;
        }
        if (idx[y] < 0) {
            return false;
        }
        return a[from + idx[x]] > a[from + idx[y]];
    }

    private static boolean ltIdx(int[] a, int from, int[] idx, int x, int y) {
        if (idx[y] < 0) {
            return idx[x] >= 0;
        }
        if (idx[x] < 0) {
            return false;
        }
        return a[from + idx[x]] < a[from + idx[y]];
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
        bitonic(a, from, to);
    }

    private static void bitonic(long[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        int size = 1;
        while (size < n) {
            size <<= 1;
        }
        int[] idx = new int[size];
        for (int i = 0; i < size; i++) {
            idx[i] = i < n ? i : -1;
        }
        for (int k = 2; k <= size; k <<= 1) {
            for (int j = k >> 1; j > 0; j >>= 1) {
                for (int i = 0; i < size; i++) {
                    int l = i ^ j;
                    if (l > i) {
                        if ((i & k) == 0) {
                            if (gtIdx(a, from, idx, i, l)) {
                                swapIdx(idx, i, l);
                            }
                        } else {
                            if (ltIdx(a, from, idx, i, l)) {
                                swapIdx(idx, i, l);
                            }
                        }
                    }
                }
            }
        }
        long[] aux = new long[n];
        System.arraycopy(a, from, aux, 0, n);
        for (int i = 0; i < n; i++) {
            a[from + i] = aux[idx[i]];
        }
    }

    private static boolean gtIdx(long[] a, int from, int[] idx, int x, int y) {
        if (idx[x] < 0) {
            return idx[y] >= 0;
        }
        if (idx[y] < 0) {
            return false;
        }
        return a[from + idx[x]] > a[from + idx[y]];
    }

    private static boolean ltIdx(long[] a, int from, int[] idx, int x, int y) {
        if (idx[y] < 0) {
            return idx[x] >= 0;
        }
        if (idx[x] < 0) {
            return false;
        }
        return a[from + idx[x]] < a[from + idx[y]];
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
        bitonic(a, from, to);
    }

    private static void bitonic(float[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        int size = 1;
        while (size < n) {
            size <<= 1;
        }
        int[] idx = new int[size];
        for (int i = 0; i < size; i++) {
            idx[i] = i < n ? i : -1;
        }
        for (int k = 2; k <= size; k <<= 1) {
            for (int j = k >> 1; j > 0; j >>= 1) {
                for (int i = 0; i < size; i++) {
                    int l = i ^ j;
                    if (l > i) {
                        if ((i & k) == 0) {
                            if (gtIdx(a, from, idx, i, l)) {
                                swapIdx(idx, i, l);
                            }
                        } else {
                            if (ltIdx(a, from, idx, i, l)) {
                                swapIdx(idx, i, l);
                            }
                        }
                    }
                }
            }
        }
        float[] aux = new float[n];
        System.arraycopy(a, from, aux, 0, n);
        for (int i = 0; i < n; i++) {
            a[from + i] = aux[idx[i]];
        }
    }

    private static boolean gtIdx(float[] a, int from, int[] idx, int x, int y) {
        if (idx[x] < 0) {
            return idx[y] >= 0;
        }
        if (idx[y] < 0) {
            return false;
        }
        return Float.compare(a[from + idx[x]], a[from + idx[y]]) > 0;
    }

    private static boolean ltIdx(float[] a, int from, int[] idx, int x, int y) {
        if (idx[y] < 0) {
            return idx[x] >= 0;
        }
        if (idx[x] < 0) {
            return false;
        }
        return Float.compare(a[from + idx[x]], a[from + idx[y]]) < 0;
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
        bitonic(a, from, to);
    }

    private static void bitonic(double[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        int size = 1;
        while (size < n) {
            size <<= 1;
        }
        int[] idx = new int[size];
        for (int i = 0; i < size; i++) {
            idx[i] = i < n ? i : -1;
        }
        for (int k = 2; k <= size; k <<= 1) {
            for (int j = k >> 1; j > 0; j >>= 1) {
                for (int i = 0; i < size; i++) {
                    int l = i ^ j;
                    if (l > i) {
                        if ((i & k) == 0) {
                            if (gtIdx(a, from, idx, i, l)) {
                                swapIdx(idx, i, l);
                            }
                        } else {
                            if (ltIdx(a, from, idx, i, l)) {
                                swapIdx(idx, i, l);
                            }
                        }
                    }
                }
            }
        }
        double[] aux = new double[n];
        System.arraycopy(a, from, aux, 0, n);
        for (int i = 0; i < n; i++) {
            a[from + i] = aux[idx[i]];
        }
    }

    private static boolean gtIdx(double[] a, int from, int[] idx, int x, int y) {
        if (idx[x] < 0) {
            return idx[y] >= 0;
        }
        if (idx[y] < 0) {
            return false;
        }
        return Double.compare(a[from + idx[x]], a[from + idx[y]]) > 0;
    }

    private static boolean ltIdx(double[] a, int from, int[] idx, int x, int y) {
        if (idx[y] < 0) {
            return idx[x] >= 0;
        }
        if (idx[x] < 0) {
            return false;
        }
        return Double.compare(a[from + idx[x]], a[from + idx[y]]) < 0;
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
        bitonic(a, from, to);
    }

    private static void bitonic(char[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        int size = 1;
        while (size < n) {
            size <<= 1;
        }
        int[] idx = new int[size];
        for (int i = 0; i < size; i++) {
            idx[i] = i < n ? i : -1;
        }
        for (int k = 2; k <= size; k <<= 1) {
            for (int j = k >> 1; j > 0; j >>= 1) {
                for (int i = 0; i < size; i++) {
                    int l = i ^ j;
                    if (l > i) {
                        if ((i & k) == 0) {
                            if (gtIdx(a, from, idx, i, l)) {
                                swapIdx(idx, i, l);
                            }
                        } else {
                            if (ltIdx(a, from, idx, i, l)) {
                                swapIdx(idx, i, l);
                            }
                        }
                    }
                }
            }
        }
        char[] aux = new char[n];
        System.arraycopy(a, from, aux, 0, n);
        for (int i = 0; i < n; i++) {
            a[from + i] = aux[idx[i]];
        }
    }

    private static boolean gtIdx(char[] a, int from, int[] idx, int x, int y) {
        if (idx[x] < 0) {
            return idx[y] >= 0;
        }
        if (idx[y] < 0) {
            return false;
        }
        return a[from + idx[x]] > a[from + idx[y]];
    }

    private static boolean ltIdx(char[] a, int from, int[] idx, int x, int y) {
        if (idx[y] < 0) {
            return idx[x] >= 0;
        }
        if (idx[x] < 0) {
            return false;
        }
        return a[from + idx[x]] < a[from + idx[y]];
    }
}
