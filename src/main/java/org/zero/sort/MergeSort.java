package org.zero.sort;

import java.util.Comparator;
import java.util.List;

/**
 * 归并排序（稳定）。
 * <p>
 * 自顶向下二分归并：临时缓冲数组只在入口分配一次并沿递归传递；小于 16 的区间
 * 回退插入排序；左半区最大值不超过右半区最小值时跳过合并；相等元素先取左半。
 * 最好/平均/最坏 O(n log n)；空间 O(n)。
 * <p>
 * 支持任意 {@link Comparable} 对象数组 / {@link List}、任意 {@link Comparator} 对象数组 /
 * {@link List}，以及 byte / short / int / long / float / double / char 原始类型。
 * float / double 使用 {@link Float#compare} / {@link Double#compare} 全序（NaN 最后），
 * char 按无符号 16 位整数序。
 *
 * @author Zero
 */
public final class MergeSort {

    /** 插入排序阈值：小于该长度的区间直接使用插入排序 */
    private static final int INSERTION_THRESHOLD = 16;

    private MergeSort() {
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
        merge(a, from, to, Comparator.naturalOrder());
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
        merge(a, from, to, comparator);
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

    private static <T> boolean gtKey(T[] a, int i, T key, Comparator<? super T> cmp) {
        return cmp.compare(a[i], key) > 0;
    }

    private static <T> void insertion(T[] a, int from, int to, Comparator<? super T> cmp) {
        for (int i = from + 1; i < to; i++) {
            T key = a[i];
            int j = i - 1;
            while (j >= from && gtKey(a, j, key, cmp)) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }

    private static <T> void merge(T[] a, int from, int to, Comparator<? super T> cmp) {
        @SuppressWarnings("unchecked")
        T[] aux = (T[]) new Object[to - from];
        mergeRec(a, from, to, aux, cmp);
    }

    private static <T> void mergeRec(T[] a, int from, int to, T[] aux, Comparator<? super T> cmp) {
        int n = to - from;
        if (n < INSERTION_THRESHOLD) {
            insertion(a, from, to, cmp);
            return;
        }
        int mid = from + n / 2;
        mergeRec(a, from, mid, aux, cmp);
        mergeRec(a, mid, to, aux, cmp);
        if (!gt(a, mid - 1, mid, cmp)) {
            return;
        }
        System.arraycopy(a, from, aux, 0, n);
        int i = 0;
        int j = mid - from;
        int k = from;
        int midOffset = mid - from;
        while (i < midOffset && j < n) {
            if (gt(aux, i, j, cmp)) {
                a[k++] = aux[j++];
            } else {
                a[k++] = aux[i++];
            }
        }
        while (i < midOffset) {
            a[k++] = aux[i++];
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
        merge(a, from, to);
    }

    private static boolean gt(byte[] a, int i, int j) {
        return a[i] > a[j];
    }

    private static boolean gtKey(byte[] a, int i, byte key) {
        return a[i] > key;
    }

    private static void insertion(byte[] a, int from, int to) {
        for (int i = from + 1; i < to; i++) {
            byte key = a[i];
            int j = i - 1;
            while (j >= from && gtKey(a, j, key)) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }

    private static void merge(byte[] a, int from, int to) {
        byte[] aux = new byte[to - from];
        mergeRec(a, from, to, aux);
    }

    private static void mergeRec(byte[] a, int from, int to, byte[] aux) {
        int n = to - from;
        if (n < INSERTION_THRESHOLD) {
            insertion(a, from, to);
            return;
        }
        int mid = from + n / 2;
        mergeRec(a, from, mid, aux);
        mergeRec(a, mid, to, aux);
        if (!gt(a, mid - 1, mid)) {
            return;
        }
        System.arraycopy(a, from, aux, 0, n);
        int i = 0;
        int j = mid - from;
        int k = from;
        int midOffset = mid - from;
        while (i < midOffset && j < n) {
            if (gt(aux, i, j)) {
                a[k++] = aux[j++];
            } else {
                a[k++] = aux[i++];
            }
        }
        while (i < midOffset) {
            a[k++] = aux[i++];
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
        merge(a, from, to);
    }

    private static boolean gt(short[] a, int i, int j) {
        return a[i] > a[j];
    }

    private static boolean gtKey(short[] a, int i, short key) {
        return a[i] > key;
    }

    private static void insertion(short[] a, int from, int to) {
        for (int i = from + 1; i < to; i++) {
            short key = a[i];
            int j = i - 1;
            while (j >= from && gtKey(a, j, key)) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }

    private static void merge(short[] a, int from, int to) {
        short[] aux = new short[to - from];
        mergeRec(a, from, to, aux);
    }

    private static void mergeRec(short[] a, int from, int to, short[] aux) {
        int n = to - from;
        if (n < INSERTION_THRESHOLD) {
            insertion(a, from, to);
            return;
        }
        int mid = from + n / 2;
        mergeRec(a, from, mid, aux);
        mergeRec(a, mid, to, aux);
        if (!gt(a, mid - 1, mid)) {
            return;
        }
        System.arraycopy(a, from, aux, 0, n);
        int i = 0;
        int j = mid - from;
        int k = from;
        int midOffset = mid - from;
        while (i < midOffset && j < n) {
            if (gt(aux, i, j)) {
                a[k++] = aux[j++];
            } else {
                a[k++] = aux[i++];
            }
        }
        while (i < midOffset) {
            a[k++] = aux[i++];
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
        merge(a, from, to);
    }

    private static boolean gt(int[] a, int i, int j) {
        return a[i] > a[j];
    }

    private static boolean gtKey(int[] a, int i, int key) {
        return a[i] > key;
    }

    private static void insertion(int[] a, int from, int to) {
        for (int i = from + 1; i < to; i++) {
            int key = a[i];
            int j = i - 1;
            while (j >= from && gtKey(a, j, key)) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }

    private static void merge(int[] a, int from, int to) {
        int[] aux = new int[to - from];
        mergeRec(a, from, to, aux);
    }

    private static void mergeRec(int[] a, int from, int to, int[] aux) {
        int n = to - from;
        if (n < INSERTION_THRESHOLD) {
            insertion(a, from, to);
            return;
        }
        int mid = from + n / 2;
        mergeRec(a, from, mid, aux);
        mergeRec(a, mid, to, aux);
        if (!gt(a, mid - 1, mid)) {
            return;
        }
        System.arraycopy(a, from, aux, 0, n);
        int i = 0;
        int j = mid - from;
        int k = from;
        int midOffset = mid - from;
        while (i < midOffset && j < n) {
            if (gt(aux, i, j)) {
                a[k++] = aux[j++];
            } else {
                a[k++] = aux[i++];
            }
        }
        while (i < midOffset) {
            a[k++] = aux[i++];
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
        merge(a, from, to);
    }

    private static boolean gt(long[] a, int i, int j) {
        return a[i] > a[j];
    }

    private static boolean gtKey(long[] a, int i, long key) {
        return a[i] > key;
    }

    private static void insertion(long[] a, int from, int to) {
        for (int i = from + 1; i < to; i++) {
            long key = a[i];
            int j = i - 1;
            while (j >= from && gtKey(a, j, key)) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }

    private static void merge(long[] a, int from, int to) {
        long[] aux = new long[to - from];
        mergeRec(a, from, to, aux);
    }

    private static void mergeRec(long[] a, int from, int to, long[] aux) {
        int n = to - from;
        if (n < INSERTION_THRESHOLD) {
            insertion(a, from, to);
            return;
        }
        int mid = from + n / 2;
        mergeRec(a, from, mid, aux);
        mergeRec(a, mid, to, aux);
        if (!gt(a, mid - 1, mid)) {
            return;
        }
        System.arraycopy(a, from, aux, 0, n);
        int i = 0;
        int j = mid - from;
        int k = from;
        int midOffset = mid - from;
        while (i < midOffset && j < n) {
            if (gt(aux, i, j)) {
                a[k++] = aux[j++];
            } else {
                a[k++] = aux[i++];
            }
        }
        while (i < midOffset) {
            a[k++] = aux[i++];
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
        merge(a, from, to);
    }

    private static boolean gt(float[] a, int i, int j) {
        return Float.compare(a[i], a[j]) > 0;
    }

    private static boolean gtKey(float[] a, int i, float key) {
        return Float.compare(a[i], key) > 0;
    }

    private static void insertion(float[] a, int from, int to) {
        for (int i = from + 1; i < to; i++) {
            float key = a[i];
            int j = i - 1;
            while (j >= from && gtKey(a, j, key)) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }

    private static void merge(float[] a, int from, int to) {
        float[] aux = new float[to - from];
        mergeRec(a, from, to, aux);
    }

    private static void mergeRec(float[] a, int from, int to, float[] aux) {
        int n = to - from;
        if (n < INSERTION_THRESHOLD) {
            insertion(a, from, to);
            return;
        }
        int mid = from + n / 2;
        mergeRec(a, from, mid, aux);
        mergeRec(a, mid, to, aux);
        if (!gt(a, mid - 1, mid)) {
            return;
        }
        System.arraycopy(a, from, aux, 0, n);
        int i = 0;
        int j = mid - from;
        int k = from;
        int midOffset = mid - from;
        while (i < midOffset && j < n) {
            if (gt(aux, i, j)) {
                a[k++] = aux[j++];
            } else {
                a[k++] = aux[i++];
            }
        }
        while (i < midOffset) {
            a[k++] = aux[i++];
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
        merge(a, from, to);
    }

    private static boolean gt(double[] a, int i, int j) {
        return Double.compare(a[i], a[j]) > 0;
    }

    private static boolean gtKey(double[] a, int i, double key) {
        return Double.compare(a[i], key) > 0;
    }

    private static void insertion(double[] a, int from, int to) {
        for (int i = from + 1; i < to; i++) {
            double key = a[i];
            int j = i - 1;
            while (j >= from && gtKey(a, j, key)) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }

    private static void merge(double[] a, int from, int to) {
        double[] aux = new double[to - from];
        mergeRec(a, from, to, aux);
    }

    private static void mergeRec(double[] a, int from, int to, double[] aux) {
        int n = to - from;
        if (n < INSERTION_THRESHOLD) {
            insertion(a, from, to);
            return;
        }
        int mid = from + n / 2;
        mergeRec(a, from, mid, aux);
        mergeRec(a, mid, to, aux);
        if (!gt(a, mid - 1, mid)) {
            return;
        }
        System.arraycopy(a, from, aux, 0, n);
        int i = 0;
        int j = mid - from;
        int k = from;
        int midOffset = mid - from;
        while (i < midOffset && j < n) {
            if (gt(aux, i, j)) {
                a[k++] = aux[j++];
            } else {
                a[k++] = aux[i++];
            }
        }
        while (i < midOffset) {
            a[k++] = aux[i++];
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
        merge(a, from, to);
    }

    private static boolean gt(char[] a, int i, int j) {
        return a[i] > a[j];
    }

    private static boolean gtKey(char[] a, int i, char key) {
        return a[i] > key;
    }

    private static void insertion(char[] a, int from, int to) {
        for (int i = from + 1; i < to; i++) {
            char key = a[i];
            int j = i - 1;
            while (j >= from && gtKey(a, j, key)) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }

    private static void merge(char[] a, int from, int to) {
        char[] aux = new char[to - from];
        mergeRec(a, from, to, aux);
    }

    private static void mergeRec(char[] a, int from, int to, char[] aux) {
        int n = to - from;
        if (n < INSERTION_THRESHOLD) {
            insertion(a, from, to);
            return;
        }
        int mid = from + n / 2;
        mergeRec(a, from, mid, aux);
        mergeRec(a, mid, to, aux);
        if (!gt(a, mid - 1, mid)) {
            return;
        }
        System.arraycopy(a, from, aux, 0, n);
        int i = 0;
        int j = mid - from;
        int k = from;
        int midOffset = mid - from;
        while (i < midOffset && j < n) {
            if (gt(aux, i, j)) {
                a[k++] = aux[j++];
            } else {
                a[k++] = aux[i++];
            }
        }
        while (i < midOffset) {
            a[k++] = aux[i++];
        }
    }
}
