package org.zero.sort;

/**
 * 计数排序（稳定）。
 * <p>
 * 统计 [min, max] 值域内每个值的出现次数，再按值序回写，k = max - min + 1 为值域大小。
 * 时间 O(n+k)、空间 O(k)；值域超过上限 2^24 时抛 IllegalArgumentException
 * （long 版对 max - min 溢出另做同样保护）。
 * <p>
 * 仅适用于整数类型 byte / short / int / long / char（char 按无符号 16 位整数序，
 * 其余按有符号自然序），不提供对象 / List / Comparator / float / double 重载。
 *
 * @author Zero
 */
public final class CountingSort {

    private CountingSort() {
    }

    /** 计数/鸽巢排序值域上限：超限抛 IllegalArgumentException */
    private static final long MAX_COUNTING_RANGE = 1L << 24;

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
        counting(a, from, to);
    }

    private static void counting(byte[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        byte min = a[from];
        byte max = a[from];
        for (int i = from + 1; i < to; i++) {
            if (a[i] < min) {
                min = a[i];
            } else if (a[i] > max) {
                max = a[i];
            }
        }
        long range = (long) max - min + 1;
        if (range > MAX_COUNTING_RANGE) {
            throw new IllegalArgumentException(
                    "value range " + range + " exceeds counting sort limit " + MAX_COUNTING_RANGE);
        }
        int size = (int) range;
        int[] counts = new int[size];
        for (int i = from; i < to; i++) {
            counts[(int) ((a[i] - min))]++;
        }
        int k = from;
        for (int v = 0; v < size; v++) {
            int c = counts[v];
            while (c-- > 0) {
                a[k++] = (byte) (v + min);
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
        counting(a, from, to);
    }

    private static void counting(short[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        short min = a[from];
        short max = a[from];
        for (int i = from + 1; i < to; i++) {
            if (a[i] < min) {
                min = a[i];
            } else if (a[i] > max) {
                max = a[i];
            }
        }
        long range = (long) max - min + 1;
        if (range > MAX_COUNTING_RANGE) {
            throw new IllegalArgumentException(
                    "value range " + range + " exceeds counting sort limit " + MAX_COUNTING_RANGE);
        }
        int size = (int) range;
        int[] counts = new int[size];
        for (int i = from; i < to; i++) {
            counts[(int) ((a[i] - min))]++;
        }
        int k = from;
        for (int v = 0; v < size; v++) {
            int c = counts[v];
            while (c-- > 0) {
                a[k++] = (short) (v + min);
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
        counting(a, from, to);
    }

    private static void counting(int[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        int min = a[from];
        int max = a[from];
        for (int i = from + 1; i < to; i++) {
            if (a[i] < min) {
                min = a[i];
            } else if (a[i] > max) {
                max = a[i];
            }
        }
        long range = (long) max - min + 1;
        if (range > MAX_COUNTING_RANGE) {
            throw new IllegalArgumentException(
                    "value range " + range + " exceeds counting sort limit " + MAX_COUNTING_RANGE);
        }
        int size = (int) range;
        int[] counts = new int[size];
        for (int i = from; i < to; i++) {
            counts[(int) ((a[i] - min))]++;
        }
        int k = from;
        for (int v = 0; v < size; v++) {
            int c = counts[v];
            while (c-- > 0) {
                a[k++] = (int) (v + min);
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
        counting(a, from, to);
    }

    private static void counting(long[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        long min = a[from];
        long max = a[from];
        for (int i = from + 1; i < to; i++) {
            if (a[i] < min) {
                min = a[i];
            } else if (a[i] > max) {
                max = a[i];
            }
        }
        long diff = max - min;
        if (diff < 0) {
            throw new IllegalArgumentException("value range too large for counting sort");
        }
        long range = diff + 1;
        if (range > MAX_COUNTING_RANGE) {
            throw new IllegalArgumentException(
                    "value range " + range + " exceeds counting sort limit " + MAX_COUNTING_RANGE);
        }
        int size = (int) range;
        int[] counts = new int[size];
        for (int i = from; i < to; i++) {
            counts[(int) ((a[i] - min))]++;
        }
        int k = from;
        for (int v = 0; v < size; v++) {
            int c = counts[v];
            while (c-- > 0) {
                a[k++] = (long) (v + min);
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
        counting(a, from, to);
    }

    private static void counting(char[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        char min = a[from];
        char max = a[from];
        for (int i = from + 1; i < to; i++) {
            if (a[i] < min) {
                min = a[i];
            } else if (a[i] > max) {
                max = a[i];
            }
        }
        long range = (long) max - min + 1;
        if (range > MAX_COUNTING_RANGE) {
            throw new IllegalArgumentException(
                    "value range " + range + " exceeds counting sort limit " + MAX_COUNTING_RANGE);
        }
        int size = (int) range;
        int[] counts = new int[size];
        for (int i = from; i < to; i++) {
            counts[(int) ((a[i] - min))]++;
        }
        int k = from;
        for (int v = 0; v < size; v++) {
            int c = counts[v];
            while (c-- > 0) {
                a[k++] = (char) (v + min);
            }
        }
    }
}
