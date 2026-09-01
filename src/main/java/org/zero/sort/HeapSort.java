package org.zero.sort;

import java.util.Comparator;
import java.util.List;

/**
 * 堆排序（不稳定）。
 * <p>
 * 原地构建最大堆：从最后一个非叶节点起逐个向下调整建堆，随后反复把堆顶
 * （最大值）交换到区间尾部并缩小堆继续下沉调整。
 * 最好/平均/最坏 O(n log n)；空间 O(1)。
 * <p>
 * 支持任意 {@link Comparable} 对象数组 / {@link List}、任意 {@link Comparator} 对象数组 /
 * {@link List}，以及 byte / short / int / long / float / double / char 原始类型。
 * float / double 使用 {@link Float#compare} / {@link Double#compare} 全序（NaN 最后），
 * char 按无符号 16 位整数序。
 *
 * @author Zero
 */
public final class HeapSort {

    private HeapSort() {
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
        heap(a, from, to, Comparator.naturalOrder());
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
        heap(a, from, to, comparator);
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

    private static <T> void heap(T[] a, int from, int to, Comparator<? super T> cmp) {
        int n = to - from;
        for (int i = n / 2 - 1; i >= 0; i--) {
            siftDown(a, from, n, i, cmp);
        }
        for (int end = n - 1; end > 0; end--) {
            swap(a, from, from + end);
            siftDown(a, from, end, 0, cmp);
        }
    }

    private static <T> void siftDown(T[] a, int from, int n, int i, Comparator<? super T> cmp) {
        while (true) {
            int left = 2 * i + 1;
            if (left >= n) {
                return;
            }
            int right = left + 1;
            int largest = (right < n && lt(a, from + left, from + right, cmp)) ? right : left;
            if (lt(a, from + i, from + largest, cmp)) {
                swap(a, from + i, from + largest);
                i = largest;
            } else {
                return;
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
        heap(a, from, to);
    }

    private static boolean lt(byte[] a, int i, int j) {
        return a[i] < a[j];
    }

    private static void swap(byte[] a, int i, int j) {
        byte tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    private static void heap(byte[] a, int from, int to) {
        int n = to - from;
        for (int i = n / 2 - 1; i >= 0; i--) {
            siftDown(a, from, n, i);
        }
        for (int end = n - 1; end > 0; end--) {
            swap(a, from, from + end);
            siftDown(a, from, end, 0);
        }
    }

    private static void siftDown(byte[] a, int from, int n, int i) {
        while (true) {
            int left = 2 * i + 1;
            if (left >= n) {
                return;
            }
            int right = left + 1;
            int largest = (right < n && lt(a, from + left, from + right)) ? right : left;
            if (lt(a, from + i, from + largest)) {
                swap(a, from + i, from + largest);
                i = largest;
            } else {
                return;
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
        heap(a, from, to);
    }

    private static boolean lt(short[] a, int i, int j) {
        return a[i] < a[j];
    }

    private static void swap(short[] a, int i, int j) {
        short tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    private static void heap(short[] a, int from, int to) {
        int n = to - from;
        for (int i = n / 2 - 1; i >= 0; i--) {
            siftDown(a, from, n, i);
        }
        for (int end = n - 1; end > 0; end--) {
            swap(a, from, from + end);
            siftDown(a, from, end, 0);
        }
    }

    private static void siftDown(short[] a, int from, int n, int i) {
        while (true) {
            int left = 2 * i + 1;
            if (left >= n) {
                return;
            }
            int right = left + 1;
            int largest = (right < n && lt(a, from + left, from + right)) ? right : left;
            if (lt(a, from + i, from + largest)) {
                swap(a, from + i, from + largest);
                i = largest;
            } else {
                return;
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
        heap(a, from, to);
    }

    private static boolean lt(int[] a, int i, int j) {
        return a[i] < a[j];
    }

    private static void swap(int[] a, int i, int j) {
        int tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    private static void heap(int[] a, int from, int to) {
        int n = to - from;
        for (int i = n / 2 - 1; i >= 0; i--) {
            siftDown(a, from, n, i);
        }
        for (int end = n - 1; end > 0; end--) {
            swap(a, from, from + end);
            siftDown(a, from, end, 0);
        }
    }

    private static void siftDown(int[] a, int from, int n, int i) {
        while (true) {
            int left = 2 * i + 1;
            if (left >= n) {
                return;
            }
            int right = left + 1;
            int largest = (right < n && lt(a, from + left, from + right)) ? right : left;
            if (lt(a, from + i, from + largest)) {
                swap(a, from + i, from + largest);
                i = largest;
            } else {
                return;
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
        heap(a, from, to);
    }

    private static boolean lt(long[] a, int i, int j) {
        return a[i] < a[j];
    }

    private static void swap(long[] a, int i, int j) {
        long tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    private static void heap(long[] a, int from, int to) {
        int n = to - from;
        for (int i = n / 2 - 1; i >= 0; i--) {
            siftDown(a, from, n, i);
        }
        for (int end = n - 1; end > 0; end--) {
            swap(a, from, from + end);
            siftDown(a, from, end, 0);
        }
    }

    private static void siftDown(long[] a, int from, int n, int i) {
        while (true) {
            int left = 2 * i + 1;
            if (left >= n) {
                return;
            }
            int right = left + 1;
            int largest = (right < n && lt(a, from + left, from + right)) ? right : left;
            if (lt(a, from + i, from + largest)) {
                swap(a, from + i, from + largest);
                i = largest;
            } else {
                return;
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
        heap(a, from, to);
    }

    private static boolean lt(float[] a, int i, int j) {
        return Float.compare(a[i], a[j]) < 0;
    }

    private static void swap(float[] a, int i, int j) {
        float tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    private static void heap(float[] a, int from, int to) {
        int n = to - from;
        for (int i = n / 2 - 1; i >= 0; i--) {
            siftDown(a, from, n, i);
        }
        for (int end = n - 1; end > 0; end--) {
            swap(a, from, from + end);
            siftDown(a, from, end, 0);
        }
    }

    private static void siftDown(float[] a, int from, int n, int i) {
        while (true) {
            int left = 2 * i + 1;
            if (left >= n) {
                return;
            }
            int right = left + 1;
            int largest = (right < n && lt(a, from + left, from + right)) ? right : left;
            if (lt(a, from + i, from + largest)) {
                swap(a, from + i, from + largest);
                i = largest;
            } else {
                return;
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
        heap(a, from, to);
    }

    private static boolean lt(double[] a, int i, int j) {
        return Double.compare(a[i], a[j]) < 0;
    }

    private static void swap(double[] a, int i, int j) {
        double tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    private static void heap(double[] a, int from, int to) {
        int n = to - from;
        for (int i = n / 2 - 1; i >= 0; i--) {
            siftDown(a, from, n, i);
        }
        for (int end = n - 1; end > 0; end--) {
            swap(a, from, from + end);
            siftDown(a, from, end, 0);
        }
    }

    private static void siftDown(double[] a, int from, int n, int i) {
        while (true) {
            int left = 2 * i + 1;
            if (left >= n) {
                return;
            }
            int right = left + 1;
            int largest = (right < n && lt(a, from + left, from + right)) ? right : left;
            if (lt(a, from + i, from + largest)) {
                swap(a, from + i, from + largest);
                i = largest;
            } else {
                return;
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
        heap(a, from, to);
    }

    private static boolean lt(char[] a, int i, int j) {
        return a[i] < a[j];
    }

    private static void swap(char[] a, int i, int j) {
        char tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    private static void heap(char[] a, int from, int to) {
        int n = to - from;
        for (int i = n / 2 - 1; i >= 0; i--) {
            siftDown(a, from, n, i);
        }
        for (int end = n - 1; end > 0; end--) {
            swap(a, from, from + end);
            siftDown(a, from, end, 0);
        }
    }

    private static void siftDown(char[] a, int from, int n, int i) {
        while (true) {
            int left = 2 * i + 1;
            if (left >= n) {
                return;
            }
            int right = left + 1;
            int largest = (right < n && lt(a, from + left, from + right)) ? right : left;
            if (lt(a, from + i, from + largest)) {
                swap(a, from + i, from + largest);
                i = largest;
            } else {
                return;
            }
        }
    }
}
