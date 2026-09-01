package org.zero.sort;

import java.util.Comparator;
import java.util.List;

/**
 * 快速排序（不稳定，双轴）。
 * <p>
 * Yaroslavskiy 双轴划分（与 JDK 双轴快排同款算法思想）：五元素取样网络选双枢轴，
 * 相等枢轴回退单轴三路划分，小于 47 的区间回退插入排序；
 * 有序/逆序/全相等输入不退化，敌手输入最坏 O(n^2)。
 * 最好/平均 O(n log n)、最坏 O(n^2)；空间 O(log n)（递归栈）。
 * <p>
 * 支持任意 {@link Comparable} 对象数组 / {@link List}、任意 {@link Comparator} 对象数组 /
 * {@link List}，以及 byte / short / int / long / float / double / char 原始类型。
 * float / double 使用 {@link Float#compare} / {@link Double#compare} 全序（NaN 最后），
 * char 按无符号 16 位整数序。
 *
 * @author Zero
 */
public final class QuickSort {

    /** 双轴快排的插入排序阈值（JDK 同款 47） */
    private static final int QUICK_INSERTION_THRESHOLD = 47;

    private QuickSort() {
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
        quick(a, from, to, Comparator.naturalOrder());
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
        quick(a, from, to, comparator);
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

    private static <T> boolean gtKey(T[] a, int i, T key, Comparator<? super T> cmp) {
        return cmp.compare(a[i], key) > 0;
    }

    private static <T> void swap(T[] a, int i, int j) {
        T tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
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

    private static <T> void quick(T[] a, int from, int to, Comparator<? super T> cmp) {
        quickRec(a, from, to - 1, cmp);
    }

    private static <T> void quickRec(T[] a, int lo, int hi, Comparator<? super T> cmp) {
        int len = hi - lo + 1;
        if (len < QUICK_INSERTION_THRESHOLD) {
            insertion(a, lo, hi + 1, cmp);
            return;
        }
        int seventh = (len >> 3) + (len >> 6) + 1;
        int e3 = (lo + hi) >>> 1;
        int e2 = e3 - seventh;
        int e1 = e2 - seventh;
        int e4 = e3 + seventh;
        int e5 = e4 + seventh;
        if (lt(a, e2, e1, cmp)) {
            swap(a, e1, e2);
        }
        if (lt(a, e3, e2, cmp)) {
            swap(a, e2, e3);
        }
        if (lt(a, e4, e3, cmp)) {
            swap(a, e3, e4);
        }
        if (lt(a, e5, e4, cmp)) {
            swap(a, e4, e5);
        }
        if (lt(a, e2, e1, cmp)) {
            swap(a, e1, e2);
        }
        if (lt(a, e3, e2, cmp)) {
            swap(a, e2, e3);
        }
        if (lt(a, e4, e3, cmp)) {
            swap(a, e3, e4);
        }
        if (lt(a, e2, e1, cmp)) {
            swap(a, e1, e2);
        }
        if (lt(a, e3, e2, cmp)) {
            swap(a, e2, e3);
        }
        T pivot1 = a[e2];
        T pivot2 = a[e4];
        if (cmp.compare(pivot1, pivot2) != 0) {
            a[e2] = a[lo];
            a[e4] = a[hi];
            int less = lo;
            int great = hi;
            while (cmp.compare(a[less + 1], pivot1) < 0) {
                less++;
            }
            while (cmp.compare(a[great - 1], pivot2) > 0) {
                great--;
            }
            less++;
            great--;
            outer:
            for (int k = less; k <= great; k++) {
                T ak = a[k];
                if (cmp.compare(ak, pivot1) < 0) {
                    a[k] = a[less];
                    a[less] = ak;
                    less++;
                } else if (cmp.compare(ak, pivot2) > 0) {
                    while (cmp.compare(a[great], pivot2) > 0) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (cmp.compare(a[great], pivot1) < 0) {
                        a[k] = a[less];
                        a[less] = a[great];
                        less++;
                    } else {
                        a[k] = a[great];
                    }
                    a[great] = ak;
                    great--;
                }
            }
            a[lo] = a[less - 1];
            a[less - 1] = pivot1;
            a[hi] = a[great + 1];
            a[great + 1] = pivot2;
            quickRec(a, lo, less - 2, cmp);
            quickRec(a, great + 2, hi, cmp);
            if (less < e1 && e5 < great) {
                while (cmp.compare(a[less], pivot1) == 0) {
                    less++;
                }
                while (cmp.compare(a[great], pivot2) == 0) {
                    great--;
                }
            }
            quickRec(a, less, great, cmp);
        } else {
            T pivot = a[e3];
            int ltIdx = lo;
            int i = lo;
            int gtIdx = hi;
            while (i <= gtIdx) {
                int c = cmp.compare(a[i], pivot);
                if (c < 0) {
                    swap(a, ltIdx++, i++);
                } else if (c > 0) {
                    swap(a, i, gtIdx--);
                } else {
                    i++;
                }
            }
            quickRec(a, lo, ltIdx - 1, cmp);
            quickRec(a, gtIdx + 1, hi, cmp);
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
        quick(a, from, to);
    }

    private static boolean lt(byte[] a, int i, int j) {
        return a[i] < a[j];
    }

    private static boolean gtKey(byte[] a, int i, byte key) {
        return a[i] > key;
    }

    private static boolean ltKey(byte[] a, int i, byte key) {
        return a[i] < key;
    }

    private static void swap(byte[] a, int i, int j) {
        byte tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
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

    private static void quick(byte[] a, int from, int to) {
        quickRec(a, from, to - 1);
    }

    private static void quickRec(byte[] a, int lo, int hi) {
        int len = hi - lo + 1;
        if (len < QUICK_INSERTION_THRESHOLD) {
            insertion(a, lo, hi + 1);
            return;
        }
        int seventh = (len >> 3) + (len >> 6) + 1;
        int e3 = (lo + hi) >>> 1;
        int e2 = e3 - seventh;
        int e1 = e2 - seventh;
        int e4 = e3 + seventh;
        int e5 = e4 + seventh;
        if (lt(a, e2, e1)) {
            swap(a, e1, e2);
        }
        if (lt(a, e3, e2)) {
            swap(a, e2, e3);
        }
        if (lt(a, e4, e3)) {
            swap(a, e3, e4);
        }
        if (lt(a, e5, e4)) {
            swap(a, e4, e5);
        }
        if (lt(a, e2, e1)) {
            swap(a, e1, e2);
        }
        if (lt(a, e3, e2)) {
            swap(a, e2, e3);
        }
        if (lt(a, e4, e3)) {
            swap(a, e3, e4);
        }
        if (lt(a, e2, e1)) {
            swap(a, e1, e2);
        }
        if (lt(a, e3, e2)) {
            swap(a, e2, e3);
        }
        byte pivot1 = a[e2];
        byte pivot2 = a[e4];
        if (pivot1 != pivot2) {
            a[e2] = a[lo];
            a[e4] = a[hi];
            int less = lo;
            int great = hi;
            while (a[less + 1] < pivot1) {
                less++;
            }
            while (a[great - 1] > pivot2) {
                great--;
            }
            less++;
            great--;
            outer:
            for (int k = less; k <= great; k++) {
                byte ak = a[k];
                if (ak < pivot1) {
                    a[k] = a[less];
                    a[less] = ak;
                    less++;
                } else if (ak > pivot2) {
                    while (a[great] > pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] < pivot1) {
                        a[k] = a[less];
                        a[less] = a[great];
                        less++;
                    } else {
                        a[k] = a[great];
                    }
                    a[great] = ak;
                    great--;
                }
            }
            a[lo] = a[less - 1];
            a[less - 1] = pivot1;
            a[hi] = a[great + 1];
            a[great + 1] = pivot2;
            quickRec(a, lo, less - 2);
            quickRec(a, great + 2, hi);
            if (less < e1 && e5 < great) {
                while (a[less] == pivot1) {
                    less++;
                }
                while (a[great] == pivot2) {
                    great--;
                }
            }
            quickRec(a, less, great);
        } else {
            byte pivot = a[e3];
            int ltIdx = lo;
            int i = lo;
            int gtIdx = hi;
            while (i <= gtIdx) {
                if (ltKey(a, i, pivot)) {
                    swap(a, ltIdx++, i++);
                } else if (gtKey(a, i, pivot)) {
                    swap(a, i, gtIdx--);
                } else {
                    i++;
                }
            }
            quickRec(a, lo, ltIdx - 1);
            quickRec(a, gtIdx + 1, hi);
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
        quick(a, from, to);
    }

    private static boolean lt(short[] a, int i, int j) {
        return a[i] < a[j];
    }

    private static boolean gtKey(short[] a, int i, short key) {
        return a[i] > key;
    }

    private static boolean ltKey(short[] a, int i, short key) {
        return a[i] < key;
    }

    private static void swap(short[] a, int i, int j) {
        short tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
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

    private static void quick(short[] a, int from, int to) {
        quickRec(a, from, to - 1);
    }

    private static void quickRec(short[] a, int lo, int hi) {
        int len = hi - lo + 1;
        if (len < QUICK_INSERTION_THRESHOLD) {
            insertion(a, lo, hi + 1);
            return;
        }
        int seventh = (len >> 3) + (len >> 6) + 1;
        int e3 = (lo + hi) >>> 1;
        int e2 = e3 - seventh;
        int e1 = e2 - seventh;
        int e4 = e3 + seventh;
        int e5 = e4 + seventh;
        if (lt(a, e2, e1)) {
            swap(a, e1, e2);
        }
        if (lt(a, e3, e2)) {
            swap(a, e2, e3);
        }
        if (lt(a, e4, e3)) {
            swap(a, e3, e4);
        }
        if (lt(a, e5, e4)) {
            swap(a, e4, e5);
        }
        if (lt(a, e2, e1)) {
            swap(a, e1, e2);
        }
        if (lt(a, e3, e2)) {
            swap(a, e2, e3);
        }
        if (lt(a, e4, e3)) {
            swap(a, e3, e4);
        }
        if (lt(a, e2, e1)) {
            swap(a, e1, e2);
        }
        if (lt(a, e3, e2)) {
            swap(a, e2, e3);
        }
        short pivot1 = a[e2];
        short pivot2 = a[e4];
        if (pivot1 != pivot2) {
            a[e2] = a[lo];
            a[e4] = a[hi];
            int less = lo;
            int great = hi;
            while (a[less + 1] < pivot1) {
                less++;
            }
            while (a[great - 1] > pivot2) {
                great--;
            }
            less++;
            great--;
            outer:
            for (int k = less; k <= great; k++) {
                short ak = a[k];
                if (ak < pivot1) {
                    a[k] = a[less];
                    a[less] = ak;
                    less++;
                } else if (ak > pivot2) {
                    while (a[great] > pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] < pivot1) {
                        a[k] = a[less];
                        a[less] = a[great];
                        less++;
                    } else {
                        a[k] = a[great];
                    }
                    a[great] = ak;
                    great--;
                }
            }
            a[lo] = a[less - 1];
            a[less - 1] = pivot1;
            a[hi] = a[great + 1];
            a[great + 1] = pivot2;
            quickRec(a, lo, less - 2);
            quickRec(a, great + 2, hi);
            if (less < e1 && e5 < great) {
                while (a[less] == pivot1) {
                    less++;
                }
                while (a[great] == pivot2) {
                    great--;
                }
            }
            quickRec(a, less, great);
        } else {
            short pivot = a[e3];
            int ltIdx = lo;
            int i = lo;
            int gtIdx = hi;
            while (i <= gtIdx) {
                if (ltKey(a, i, pivot)) {
                    swap(a, ltIdx++, i++);
                } else if (gtKey(a, i, pivot)) {
                    swap(a, i, gtIdx--);
                } else {
                    i++;
                }
            }
            quickRec(a, lo, ltIdx - 1);
            quickRec(a, gtIdx + 1, hi);
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
        quick(a, from, to);
    }

    private static boolean lt(int[] a, int i, int j) {
        return a[i] < a[j];
    }

    private static boolean gtKey(int[] a, int i, int key) {
        return a[i] > key;
    }

    private static boolean ltKey(int[] a, int i, int key) {
        return a[i] < key;
    }

    private static void swap(int[] a, int i, int j) {
        int tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
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

    private static void quick(int[] a, int from, int to) {
        quickRec(a, from, to - 1);
    }

    private static void quickRec(int[] a, int lo, int hi) {
        int len = hi - lo + 1;
        if (len < QUICK_INSERTION_THRESHOLD) {
            insertion(a, lo, hi + 1);
            return;
        }
        int seventh = (len >> 3) + (len >> 6) + 1;
        int e3 = (lo + hi) >>> 1;
        int e2 = e3 - seventh;
        int e1 = e2 - seventh;
        int e4 = e3 + seventh;
        int e5 = e4 + seventh;
        if (lt(a, e2, e1)) {
            swap(a, e1, e2);
        }
        if (lt(a, e3, e2)) {
            swap(a, e2, e3);
        }
        if (lt(a, e4, e3)) {
            swap(a, e3, e4);
        }
        if (lt(a, e5, e4)) {
            swap(a, e4, e5);
        }
        if (lt(a, e2, e1)) {
            swap(a, e1, e2);
        }
        if (lt(a, e3, e2)) {
            swap(a, e2, e3);
        }
        if (lt(a, e4, e3)) {
            swap(a, e3, e4);
        }
        if (lt(a, e2, e1)) {
            swap(a, e1, e2);
        }
        if (lt(a, e3, e2)) {
            swap(a, e2, e3);
        }
        int pivot1 = a[e2];
        int pivot2 = a[e4];
        if (pivot1 != pivot2) {
            a[e2] = a[lo];
            a[e4] = a[hi];
            int less = lo;
            int great = hi;
            while (a[less + 1] < pivot1) {
                less++;
            }
            while (a[great - 1] > pivot2) {
                great--;
            }
            less++;
            great--;
            outer:
            for (int k = less; k <= great; k++) {
                int ak = a[k];
                if (ak < pivot1) {
                    a[k] = a[less];
                    a[less] = ak;
                    less++;
                } else if (ak > pivot2) {
                    while (a[great] > pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] < pivot1) {
                        a[k] = a[less];
                        a[less] = a[great];
                        less++;
                    } else {
                        a[k] = a[great];
                    }
                    a[great] = ak;
                    great--;
                }
            }
            a[lo] = a[less - 1];
            a[less - 1] = pivot1;
            a[hi] = a[great + 1];
            a[great + 1] = pivot2;
            quickRec(a, lo, less - 2);
            quickRec(a, great + 2, hi);
            if (less < e1 && e5 < great) {
                while (a[less] == pivot1) {
                    less++;
                }
                while (a[great] == pivot2) {
                    great--;
                }
            }
            quickRec(a, less, great);
        } else {
            int pivot = a[e3];
            int ltIdx = lo;
            int i = lo;
            int gtIdx = hi;
            while (i <= gtIdx) {
                if (ltKey(a, i, pivot)) {
                    swap(a, ltIdx++, i++);
                } else if (gtKey(a, i, pivot)) {
                    swap(a, i, gtIdx--);
                } else {
                    i++;
                }
            }
            quickRec(a, lo, ltIdx - 1);
            quickRec(a, gtIdx + 1, hi);
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
        quick(a, from, to);
    }

    private static boolean lt(long[] a, int i, int j) {
        return a[i] < a[j];
    }

    private static boolean gtKey(long[] a, int i, long key) {
        return a[i] > key;
    }

    private static boolean ltKey(long[] a, int i, long key) {
        return a[i] < key;
    }

    private static void swap(long[] a, int i, int j) {
        long tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
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

    private static void quick(long[] a, int from, int to) {
        quickRec(a, from, to - 1);
    }

    private static void quickRec(long[] a, int lo, int hi) {
        int len = hi - lo + 1;
        if (len < QUICK_INSERTION_THRESHOLD) {
            insertion(a, lo, hi + 1);
            return;
        }
        int seventh = (len >> 3) + (len >> 6) + 1;
        int e3 = (lo + hi) >>> 1;
        int e2 = e3 - seventh;
        int e1 = e2 - seventh;
        int e4 = e3 + seventh;
        int e5 = e4 + seventh;
        if (lt(a, e2, e1)) {
            swap(a, e1, e2);
        }
        if (lt(a, e3, e2)) {
            swap(a, e2, e3);
        }
        if (lt(a, e4, e3)) {
            swap(a, e3, e4);
        }
        if (lt(a, e5, e4)) {
            swap(a, e4, e5);
        }
        if (lt(a, e2, e1)) {
            swap(a, e1, e2);
        }
        if (lt(a, e3, e2)) {
            swap(a, e2, e3);
        }
        if (lt(a, e4, e3)) {
            swap(a, e3, e4);
        }
        if (lt(a, e2, e1)) {
            swap(a, e1, e2);
        }
        if (lt(a, e3, e2)) {
            swap(a, e2, e3);
        }
        long pivot1 = a[e2];
        long pivot2 = a[e4];
        if (pivot1 != pivot2) {
            a[e2] = a[lo];
            a[e4] = a[hi];
            int less = lo;
            int great = hi;
            while (a[less + 1] < pivot1) {
                less++;
            }
            while (a[great - 1] > pivot2) {
                great--;
            }
            less++;
            great--;
            outer:
            for (int k = less; k <= great; k++) {
                long ak = a[k];
                if (ak < pivot1) {
                    a[k] = a[less];
                    a[less] = ak;
                    less++;
                } else if (ak > pivot2) {
                    while (a[great] > pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] < pivot1) {
                        a[k] = a[less];
                        a[less] = a[great];
                        less++;
                    } else {
                        a[k] = a[great];
                    }
                    a[great] = ak;
                    great--;
                }
            }
            a[lo] = a[less - 1];
            a[less - 1] = pivot1;
            a[hi] = a[great + 1];
            a[great + 1] = pivot2;
            quickRec(a, lo, less - 2);
            quickRec(a, great + 2, hi);
            if (less < e1 && e5 < great) {
                while (a[less] == pivot1) {
                    less++;
                }
                while (a[great] == pivot2) {
                    great--;
                }
            }
            quickRec(a, less, great);
        } else {
            long pivot = a[e3];
            int ltIdx = lo;
            int i = lo;
            int gtIdx = hi;
            while (i <= gtIdx) {
                if (ltKey(a, i, pivot)) {
                    swap(a, ltIdx++, i++);
                } else if (gtKey(a, i, pivot)) {
                    swap(a, i, gtIdx--);
                } else {
                    i++;
                }
            }
            quickRec(a, lo, ltIdx - 1);
            quickRec(a, gtIdx + 1, hi);
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
        quick(a, from, to);
    }

    private static boolean lt(float[] a, int i, int j) {
        return Float.compare(a[i], a[j]) < 0;
    }

    private static boolean gtKey(float[] a, int i, float key) {
        return Float.compare(a[i], key) > 0;
    }

    private static boolean ltKey(float[] a, int i, float key) {
        return Float.compare(a[i], key) < 0;
    }

    private static void swap(float[] a, int i, int j) {
        float tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
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

    private static void quick(float[] a, int from, int to) {
        quickRec(a, from, to - 1);
    }

    private static void quickRec(float[] a, int lo, int hi) {
        int len = hi - lo + 1;
        if (len < QUICK_INSERTION_THRESHOLD) {
            insertion(a, lo, hi + 1);
            return;
        }
        int seventh = (len >> 3) + (len >> 6) + 1;
        int e3 = (lo + hi) >>> 1;
        int e2 = e3 - seventh;
        int e1 = e2 - seventh;
        int e4 = e3 + seventh;
        int e5 = e4 + seventh;
        if (lt(a, e2, e1)) {
            swap(a, e1, e2);
        }
        if (lt(a, e3, e2)) {
            swap(a, e2, e3);
        }
        if (lt(a, e4, e3)) {
            swap(a, e3, e4);
        }
        if (lt(a, e5, e4)) {
            swap(a, e4, e5);
        }
        if (lt(a, e2, e1)) {
            swap(a, e1, e2);
        }
        if (lt(a, e3, e2)) {
            swap(a, e2, e3);
        }
        if (lt(a, e4, e3)) {
            swap(a, e3, e4);
        }
        if (lt(a, e2, e1)) {
            swap(a, e1, e2);
        }
        if (lt(a, e3, e2)) {
            swap(a, e2, e3);
        }
        float pivot1 = a[e2];
        float pivot2 = a[e4];
        if (pivot1 != pivot2) {
            a[e2] = a[lo];
            a[e4] = a[hi];
            int less = lo;
            int great = hi;
            while (Float.compare(a[less + 1], pivot1) < 0) {
                less++;
            }
            while (Float.compare(a[great - 1], pivot2) > 0) {
                great--;
            }
            less++;
            great--;
            outer:
            for (int k = less; k <= great; k++) {
                float ak = a[k];
                if (Float.compare(ak, pivot1) < 0) {
                    a[k] = a[less];
                    a[less] = ak;
                    less++;
                } else if (Float.compare(ak, pivot2) > 0) {
                    while (Float.compare(a[great], pivot2) > 0) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (Float.compare(a[great], pivot1) < 0) {
                        a[k] = a[less];
                        a[less] = a[great];
                        less++;
                    } else {
                        a[k] = a[great];
                    }
                    a[great] = ak;
                    great--;
                }
            }
            a[lo] = a[less - 1];
            a[less - 1] = pivot1;
            a[hi] = a[great + 1];
            a[great + 1] = pivot2;
            quickRec(a, lo, less - 2);
            quickRec(a, great + 2, hi);
            if (less < e1 && e5 < great) {
                while (Float.compare(a[less], pivot1) == 0) {
                    less++;
                }
                while (Float.compare(a[great], pivot2) == 0) {
                    great--;
                }
            }
            quickRec(a, less, great);
        } else {
            float pivot = a[e3];
            int ltIdx = lo;
            int i = lo;
            int gtIdx = hi;
            while (i <= gtIdx) {
                if (ltKey(a, i, pivot)) {
                    swap(a, ltIdx++, i++);
                } else if (gtKey(a, i, pivot)) {
                    swap(a, i, gtIdx--);
                } else {
                    i++;
                }
            }
            quickRec(a, lo, ltIdx - 1);
            quickRec(a, gtIdx + 1, hi);
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
        quick(a, from, to);
    }

    private static boolean lt(double[] a, int i, int j) {
        return Double.compare(a[i], a[j]) < 0;
    }

    private static boolean gtKey(double[] a, int i, double key) {
        return Double.compare(a[i], key) > 0;
    }

    private static boolean ltKey(double[] a, int i, double key) {
        return Double.compare(a[i], key) < 0;
    }

    private static void swap(double[] a, int i, int j) {
        double tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
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

    private static void quick(double[] a, int from, int to) {
        quickRec(a, from, to - 1);
    }

    private static void quickRec(double[] a, int lo, int hi) {
        int len = hi - lo + 1;
        if (len < QUICK_INSERTION_THRESHOLD) {
            insertion(a, lo, hi + 1);
            return;
        }
        int seventh = (len >> 3) + (len >> 6) + 1;
        int e3 = (lo + hi) >>> 1;
        int e2 = e3 - seventh;
        int e1 = e2 - seventh;
        int e4 = e3 + seventh;
        int e5 = e4 + seventh;
        if (lt(a, e2, e1)) {
            swap(a, e1, e2);
        }
        if (lt(a, e3, e2)) {
            swap(a, e2, e3);
        }
        if (lt(a, e4, e3)) {
            swap(a, e3, e4);
        }
        if (lt(a, e5, e4)) {
            swap(a, e4, e5);
        }
        if (lt(a, e2, e1)) {
            swap(a, e1, e2);
        }
        if (lt(a, e3, e2)) {
            swap(a, e2, e3);
        }
        if (lt(a, e4, e3)) {
            swap(a, e3, e4);
        }
        if (lt(a, e2, e1)) {
            swap(a, e1, e2);
        }
        if (lt(a, e3, e2)) {
            swap(a, e2, e3);
        }
        double pivot1 = a[e2];
        double pivot2 = a[e4];
        if (pivot1 != pivot2) {
            a[e2] = a[lo];
            a[e4] = a[hi];
            int less = lo;
            int great = hi;
            while (Double.compare(a[less + 1], pivot1) < 0) {
                less++;
            }
            while (Double.compare(a[great - 1], pivot2) > 0) {
                great--;
            }
            less++;
            great--;
            outer:
            for (int k = less; k <= great; k++) {
                double ak = a[k];
                if (Double.compare(ak, pivot1) < 0) {
                    a[k] = a[less];
                    a[less] = ak;
                    less++;
                } else if (Double.compare(ak, pivot2) > 0) {
                    while (Double.compare(a[great], pivot2) > 0) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (Double.compare(a[great], pivot1) < 0) {
                        a[k] = a[less];
                        a[less] = a[great];
                        less++;
                    } else {
                        a[k] = a[great];
                    }
                    a[great] = ak;
                    great--;
                }
            }
            a[lo] = a[less - 1];
            a[less - 1] = pivot1;
            a[hi] = a[great + 1];
            a[great + 1] = pivot2;
            quickRec(a, lo, less - 2);
            quickRec(a, great + 2, hi);
            if (less < e1 && e5 < great) {
                while (Double.compare(a[less], pivot1) == 0) {
                    less++;
                }
                while (Double.compare(a[great], pivot2) == 0) {
                    great--;
                }
            }
            quickRec(a, less, great);
        } else {
            double pivot = a[e3];
            int ltIdx = lo;
            int i = lo;
            int gtIdx = hi;
            while (i <= gtIdx) {
                if (ltKey(a, i, pivot)) {
                    swap(a, ltIdx++, i++);
                } else if (gtKey(a, i, pivot)) {
                    swap(a, i, gtIdx--);
                } else {
                    i++;
                }
            }
            quickRec(a, lo, ltIdx - 1);
            quickRec(a, gtIdx + 1, hi);
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
        quick(a, from, to);
    }

    private static boolean lt(char[] a, int i, int j) {
        return a[i] < a[j];
    }

    private static boolean gtKey(char[] a, int i, char key) {
        return a[i] > key;
    }

    private static boolean ltKey(char[] a, int i, char key) {
        return a[i] < key;
    }

    private static void swap(char[] a, int i, int j) {
        char tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
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

    private static void quick(char[] a, int from, int to) {
        quickRec(a, from, to - 1);
    }

    private static void quickRec(char[] a, int lo, int hi) {
        int len = hi - lo + 1;
        if (len < QUICK_INSERTION_THRESHOLD) {
            insertion(a, lo, hi + 1);
            return;
        }
        int seventh = (len >> 3) + (len >> 6) + 1;
        int e3 = (lo + hi) >>> 1;
        int e2 = e3 - seventh;
        int e1 = e2 - seventh;
        int e4 = e3 + seventh;
        int e5 = e4 + seventh;
        if (lt(a, e2, e1)) {
            swap(a, e1, e2);
        }
        if (lt(a, e3, e2)) {
            swap(a, e2, e3);
        }
        if (lt(a, e4, e3)) {
            swap(a, e3, e4);
        }
        if (lt(a, e5, e4)) {
            swap(a, e4, e5);
        }
        if (lt(a, e2, e1)) {
            swap(a, e1, e2);
        }
        if (lt(a, e3, e2)) {
            swap(a, e2, e3);
        }
        if (lt(a, e4, e3)) {
            swap(a, e3, e4);
        }
        if (lt(a, e2, e1)) {
            swap(a, e1, e2);
        }
        if (lt(a, e3, e2)) {
            swap(a, e2, e3);
        }
        char pivot1 = a[e2];
        char pivot2 = a[e4];
        if (pivot1 != pivot2) {
            a[e2] = a[lo];
            a[e4] = a[hi];
            int less = lo;
            int great = hi;
            while (a[less + 1] < pivot1) {
                less++;
            }
            while (a[great - 1] > pivot2) {
                great--;
            }
            less++;
            great--;
            outer:
            for (int k = less; k <= great; k++) {
                char ak = a[k];
                if (ak < pivot1) {
                    a[k] = a[less];
                    a[less] = ak;
                    less++;
                } else if (ak > pivot2) {
                    while (a[great] > pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] < pivot1) {
                        a[k] = a[less];
                        a[less] = a[great];
                        less++;
                    } else {
                        a[k] = a[great];
                    }
                    a[great] = ak;
                    great--;
                }
            }
            a[lo] = a[less - 1];
            a[less - 1] = pivot1;
            a[hi] = a[great + 1];
            a[great + 1] = pivot2;
            quickRec(a, lo, less - 2);
            quickRec(a, great + 2, hi);
            if (less < e1 && e5 < great) {
                while (a[less] == pivot1) {
                    less++;
                }
                while (a[great] == pivot2) {
                    great--;
                }
            }
            quickRec(a, less, great);
        } else {
            char pivot = a[e3];
            int ltIdx = lo;
            int i = lo;
            int gtIdx = hi;
            while (i <= gtIdx) {
                if (ltKey(a, i, pivot)) {
                    swap(a, ltIdx++, i++);
                } else if (gtKey(a, i, pivot)) {
                    swap(a, i, gtIdx--);
                } else {
                    i++;
                }
            }
            quickRec(a, lo, ltIdx - 1);
            quickRec(a, gtIdx + 1, hi);
        }
    }
}
