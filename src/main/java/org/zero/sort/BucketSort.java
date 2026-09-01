package org.zero.sort;

/**
 * 桶排序（稳定）。
 * <p>
 * NaN 先移到区间末尾，±Infinity 归入首/尾；有限值用单调位键（float 取 32 位原始位模式、
 * double 取 64 位原始位模式，负数按位取反、非负数最高位置 1，区分 -0.0 与 +0.0）
 * 均匀划分到至多 1024 个桶，桶内做插入排序后依桶序回写。
 * 平均 O(n+k)、最坏 O(n^2)；空间 O(n)。
 * <p>
 * 仅适用于浮点类型 float / double（排序结果与 {@link Float#compare} /
 * {@link Double#compare} 全序一致，NaN 最后），不提供对象 / List / Comparator / 整数类型重载。
 *
 * @author Zero
 */
public final class BucketSort {

    private BucketSort() {
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
        bucket(a, from, to);
    }

    private static void bucket(float[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        float[] buf = new float[n];
        int hi = to;
        for (int i = hi - 1; i >= from; i--) {
            if (Float.isNaN(a[i])) {
                swap(a, i, hi - 1);
                hi--;
            }
        }
        if (hi == from) {
            return;
        }
        int lo = from;
        int realHi = hi;
        for (int i = lo; i < realHi; ) {
            if (a[i] == Float.NEGATIVE_INFINITY) {
                swap(a, i, lo);
                lo++;
                i++;
            } else if (a[i] == Float.POSITIVE_INFINITY) {
                swap(a, i, realHi - 1);
                realHi--;
            } else {
                i++;
            }
        }
        int m = realHi - lo;
        if (m < 2) {
            return;
        }
        long[] keys = new long[m];
        long minKey = Long.MAX_VALUE;
        long maxKey = Long.MIN_VALUE;
        for (int i = 0; i < m; i++) {
            int bits = Float.floatToRawIntBits(a[lo + i]);
            long key = (bits < 0 ? ~bits : (bits | 0x80000000)) & 0xFFFFFFFFL;
            keys[i] = key;
            if (Long.compareUnsigned(key, minKey) < 0) {
                minKey = key;
            }
            if (Long.compareUnsigned(key, maxKey) > 0) {
                maxKey = key;
            }
        }
        if (minKey == maxKey) {
            return;
        }
        int bucketCount = Math.min(m, 1024);
        long bucketSpan = Long.divideUnsigned(maxKey - minKey, bucketCount) + 1;
        long[] offsets = new long[bucketCount];
        for (int i = 0; i < m; i++) {
            int idx = (int) Long.divideUnsigned(keys[i] - minKey, bucketSpan);
            offsets[idx]++;
        }
        long sum = 0;
        for (int b = 0; b < bucketCount; b++) {
            long c = offsets[b];
            offsets[b] = sum;
            sum += c;
        }
        for (int i = 0; i < m; i++) {
            int idx = (int) Long.divideUnsigned(keys[i] - minKey, bucketSpan);
            buf[(int) (offsets[idx]++)] = a[lo + i];
        }
        long start = 0;
        for (int b = 0; b < bucketCount; b++) {
            long end = offsets[b];
            insertionRange(buf, (int) start, (int) end);
            start = end;
        }
        System.arraycopy(buf, 0, a, lo, m);
    }

    private static void insertionRange(float[] buf, int from, int to) {
        for (int i = from + 1; i < to; i++) {
            float key = buf[i];
            int j = i - 1;
            while (j >= from && gtKey(buf, j, key)) {
                buf[j + 1] = buf[j];
                j--;
            }
            buf[j + 1] = key;
        }
    }

    private static boolean gtKey(float[] a, int i, float key) {
        return Float.compare(a[i], key) > 0;
    }

    private static void swap(float[] a, int i, int j) {
        float tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
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
        bucket(a, from, to);
    }

    private static void bucket(double[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        double[] buf = new double[n];
        int hi = to;
        for (int i = hi - 1; i >= from; i--) {
            if (Double.isNaN(a[i])) {
                swap(a, i, hi - 1);
                hi--;
            }
        }
        if (hi == from) {
            return;
        }
        int lo = from;
        int realHi = hi;
        for (int i = lo; i < realHi; ) {
            if (a[i] == Double.NEGATIVE_INFINITY) {
                swap(a, i, lo);
                lo++;
                i++;
            } else if (a[i] == Double.POSITIVE_INFINITY) {
                swap(a, i, realHi - 1);
                realHi--;
            } else {
                i++;
            }
        }
        int m = realHi - lo;
        if (m < 2) {
            return;
        }
        long[] keys = new long[m];
        long minKey = Long.MAX_VALUE;
        long maxKey = Long.MIN_VALUE;
        for (int i = 0; i < m; i++) {
            long bits = Double.doubleToRawLongBits(a[lo + i]);
            long key = bits < 0 ? ~bits : bits | 0x8000000000000000L;
            keys[i] = key;
            if (Long.compareUnsigned(key, minKey) < 0) {
                minKey = key;
            }
            if (Long.compareUnsigned(key, maxKey) > 0) {
                maxKey = key;
            }
        }
        if (minKey == maxKey) {
            return;
        }
        int bucketCount = Math.min(m, 1024);
        long bucketSpan = Long.divideUnsigned(maxKey - minKey, bucketCount) + 1;
        long[] offsets = new long[bucketCount];
        for (int i = 0; i < m; i++) {
            int idx = (int) Long.divideUnsigned(keys[i] - minKey, bucketSpan);
            offsets[idx]++;
        }
        long sum = 0;
        for (int b = 0; b < bucketCount; b++) {
            long c = offsets[b];
            offsets[b] = sum;
            sum += c;
        }
        for (int i = 0; i < m; i++) {
            int idx = (int) Long.divideUnsigned(keys[i] - minKey, bucketSpan);
            buf[(int) (offsets[idx]++)] = a[lo + i];
        }
        long start = 0;
        for (int b = 0; b < bucketCount; b++) {
            long end = offsets[b];
            insertionRange(buf, (int) start, (int) end);
            start = end;
        }
        System.arraycopy(buf, 0, a, lo, m);
    }

    private static void insertionRange(double[] buf, int from, int to) {
        for (int i = from + 1; i < to; i++) {
            double key = buf[i];
            int j = i - 1;
            while (j >= from && gtKey(buf, j, key)) {
                buf[j + 1] = buf[j];
                j--;
            }
            buf[j + 1] = key;
        }
    }

    private static boolean gtKey(double[] a, int i, double key) {
        return Double.compare(a[i], key) > 0;
    }

    private static void swap(double[] a, int i, int j) {
        double tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }
}
