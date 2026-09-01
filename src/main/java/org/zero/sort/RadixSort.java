package org.zero.sort;

/**
 * 基数排序（稳定）。
 * <p>
 * LSD 基 256：从最低字节到最高字节逐轮做稳定计数分发，轮数 d 按类型取
 * byte 1 轮、short 2 轮、char 2 轮、int 4 轮、long 8 轮；
 * 有符号类型最后一轮对取出的最高字节做符号位翻转（b ^= 0x80），
 * 使负数按补码排在正数之前，char 按无符号序故不翻转。
 * 时间 O(d·(n+k))、空间 O(n+k)。
 * <p>
 * 仅适用于整数类型 byte / short / int / long / char（char 按无符号 16 位整数序，
 * 其余按有符号自然序），不提供对象 / List / Comparator / float / double 重载。
 *
 * @author Zero
 */
public final class RadixSort {

    private RadixSort() {
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
        radix(a, from, to);
    }

    private static void radix(byte[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        byte[] aux = new byte[n];
        int[] cnt = new int[256];
        for (int pass = 0; pass < 1; pass++) {
            int shift = 8 * pass;
            boolean last = pass == 1 - 1;
            java.util.Arrays.fill(cnt, 0);
            for (int i = from; i < to; i++) {
                int b = (int) ((a[i] >>> shift) & 0xFF);
                if (last) {
                    b ^= 0x80;
                }
                cnt[b]++;
            }
            int sum = 0;
            for (int b = 0; b < 256; b++) {
                int c = cnt[b];
                cnt[b] = sum;
                sum += c;
            }
            for (int i = from; i < to; i++) {
                int b = (int) ((a[i] >>> shift) & 0xFF);
                if (last) {
                    b ^= 0x80;
                }
                aux[cnt[b]++] = a[i];
            }
            System.arraycopy(aux, 0, a, from, n);
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
        radix(a, from, to);
    }

    private static void radix(short[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        short[] aux = new short[n];
        int[] cnt = new int[256];
        for (int pass = 0; pass < 2; pass++) {
            int shift = 8 * pass;
            boolean last = pass == 2 - 1;
            java.util.Arrays.fill(cnt, 0);
            for (int i = from; i < to; i++) {
                int b = (int) ((a[i] >>> shift) & 0xFF);
                if (last) {
                    b ^= 0x80;
                }
                cnt[b]++;
            }
            int sum = 0;
            for (int b = 0; b < 256; b++) {
                int c = cnt[b];
                cnt[b] = sum;
                sum += c;
            }
            for (int i = from; i < to; i++) {
                int b = (int) ((a[i] >>> shift) & 0xFF);
                if (last) {
                    b ^= 0x80;
                }
                aux[cnt[b]++] = a[i];
            }
            System.arraycopy(aux, 0, a, from, n);
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
        radix(a, from, to);
    }

    private static void radix(int[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        int[] aux = new int[n];
        int[] cnt = new int[256];
        for (int pass = 0; pass < 4; pass++) {
            int shift = 8 * pass;
            boolean last = pass == 4 - 1;
            java.util.Arrays.fill(cnt, 0);
            for (int i = from; i < to; i++) {
                int b = (int) ((a[i] >>> shift) & 0xFF);
                if (last) {
                    b ^= 0x80;
                }
                cnt[b]++;
            }
            int sum = 0;
            for (int b = 0; b < 256; b++) {
                int c = cnt[b];
                cnt[b] = sum;
                sum += c;
            }
            for (int i = from; i < to; i++) {
                int b = (int) ((a[i] >>> shift) & 0xFF);
                if (last) {
                    b ^= 0x80;
                }
                aux[cnt[b]++] = a[i];
            }
            System.arraycopy(aux, 0, a, from, n);
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
        radix(a, from, to);
    }

    private static void radix(long[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        long[] aux = new long[n];
        int[] cnt = new int[256];
        for (int pass = 0; pass < 8; pass++) {
            int shift = 8 * pass;
            boolean last = pass == 8 - 1;
            java.util.Arrays.fill(cnt, 0);
            for (int i = from; i < to; i++) {
                int b = (int) ((a[i] >>> shift) & 0xFF);
                if (last) {
                    b ^= 0x80;
                }
                cnt[b]++;
            }
            int sum = 0;
            for (int b = 0; b < 256; b++) {
                int c = cnt[b];
                cnt[b] = sum;
                sum += c;
            }
            for (int i = from; i < to; i++) {
                int b = (int) ((a[i] >>> shift) & 0xFF);
                if (last) {
                    b ^= 0x80;
                }
                aux[cnt[b]++] = a[i];
            }
            System.arraycopy(aux, 0, a, from, n);
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
        radix(a, from, to);
    }

    private static void radix(char[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        char[] aux = new char[n];
        int[] cnt = new int[256];
        for (int pass = 0; pass < 2; pass++) {
            int shift = 8 * pass;
            boolean last = pass == 2 - 1;
            java.util.Arrays.fill(cnt, 0);
            for (int i = from; i < to; i++) {
                int b = (int) ((a[i] >>> shift) & 0xFF);

                cnt[b]++;
            }
            int sum = 0;
            for (int b = 0; b < 256; b++) {
                int c = cnt[b];
                cnt[b] = sum;
                sum += c;
            }
            for (int i = from; i < to; i++) {
                int b = (int) ((a[i] >>> shift) & 0xFF);

                aux[cnt[b]++] = a[i];
            }
            System.arraycopy(aux, 0, a, from, n);
        }
    }
}
