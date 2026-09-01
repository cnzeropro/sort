package org.zero.sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

/**
 * 树排序（稳定）。
 * <p>
 * 对象版基于红黑树 {@link TreeMap}：比较相等的元素以列表聚合保序，再展平回写；
 * 原始类型版为朴素二叉搜索树的并行数组实现（零装箱），相等元素进右子树，
 * 插入全部元素后中序遍历回写。
 * 平均 O(n log n)、最坏 O(n^2)（树退化为链）；空间 O(n)。
 * <p>
 * 支持任意 {@link Comparable} 对象数组 / {@link List}、任意 {@link Comparator} 对象数组 /
 * {@link List}，以及 byte / short / int / long / float / double / char 原始类型。
 * float / double 使用 {@link Float#compare} / {@link Double#compare} 全序（NaN 最后），
 * char 按无符号 16 位整数序。
 *
 * @author Zero
 */
public final class TreeSort {

    private TreeSort() {
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
        tree(a, from, to, Comparator.naturalOrder());
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
        tree(a, from, to, comparator);
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

    private static <T> void tree(T[] a, int from, int to, Comparator<? super T> cmp) {
        TreeMap<T, List<T>> map = new TreeMap<T, List<T>>(cmp);
        for (int i = from; i < to; i++) {
            List<T> bucket = map.get(a[i]);
            if (bucket == null) {
                bucket = new ArrayList<T>();
                map.put(a[i], bucket);
            }
            bucket.add(a[i]);
        }
        int k = from;
        for (List<T> bucket : map.values()) {
            for (T item : bucket) {
                a[k++] = item;
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
        tree(a, from, to);
    }

    private static void tree(byte[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        byte[] keys = new byte[n];
        int[] left = new int[n];
        int[] right = new int[n];
        Arrays.fill(left, -1);
        Arrays.fill(right, -1);
        int root = -1;
        for (int i = from; i < to; i++) {
            int node = i - from;
            byte key = a[i];
            keys[node] = key;
            if (root < 0) {
                root = node;
                continue;
            }
            int cur = root;
            while (true) {
                if (key < keys[cur]) {
                    if (left[cur] < 0) {
                        left[cur] = node;
                        break;
                    }
                    cur = left[cur];
                } else {
                    if (right[cur] < 0) {
                        right[cur] = node;
                        break;
                    }
                    cur = right[cur];
                }
            }
        }
        int[] stack = new int[n];
        int sp = 0;
        int cur = root;
        int k = from;
        while (cur >= 0 || sp > 0) {
            while (cur >= 0) {
                stack[sp++] = cur;
                cur = left[cur];
            }
            cur = stack[--sp];
            a[k++] = keys[cur];
            cur = right[cur];
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
        tree(a, from, to);
    }

    private static void tree(short[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        short[] keys = new short[n];
        int[] left = new int[n];
        int[] right = new int[n];
        Arrays.fill(left, -1);
        Arrays.fill(right, -1);
        int root = -1;
        for (int i = from; i < to; i++) {
            int node = i - from;
            short key = a[i];
            keys[node] = key;
            if (root < 0) {
                root = node;
                continue;
            }
            int cur = root;
            while (true) {
                if (key < keys[cur]) {
                    if (left[cur] < 0) {
                        left[cur] = node;
                        break;
                    }
                    cur = left[cur];
                } else {
                    if (right[cur] < 0) {
                        right[cur] = node;
                        break;
                    }
                    cur = right[cur];
                }
            }
        }
        int[] stack = new int[n];
        int sp = 0;
        int cur = root;
        int k = from;
        while (cur >= 0 || sp > 0) {
            while (cur >= 0) {
                stack[sp++] = cur;
                cur = left[cur];
            }
            cur = stack[--sp];
            a[k++] = keys[cur];
            cur = right[cur];
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
        tree(a, from, to);
    }

    private static void tree(int[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        int[] keys = new int[n];
        int[] left = new int[n];
        int[] right = new int[n];
        Arrays.fill(left, -1);
        Arrays.fill(right, -1);
        int root = -1;
        for (int i = from; i < to; i++) {
            int node = i - from;
            int key = a[i];
            keys[node] = key;
            if (root < 0) {
                root = node;
                continue;
            }
            int cur = root;
            while (true) {
                if (key < keys[cur]) {
                    if (left[cur] < 0) {
                        left[cur] = node;
                        break;
                    }
                    cur = left[cur];
                } else {
                    if (right[cur] < 0) {
                        right[cur] = node;
                        break;
                    }
                    cur = right[cur];
                }
            }
        }
        int[] stack = new int[n];
        int sp = 0;
        int cur = root;
        int k = from;
        while (cur >= 0 || sp > 0) {
            while (cur >= 0) {
                stack[sp++] = cur;
                cur = left[cur];
            }
            cur = stack[--sp];
            a[k++] = keys[cur];
            cur = right[cur];
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
        tree(a, from, to);
    }

    private static void tree(long[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        long[] keys = new long[n];
        int[] left = new int[n];
        int[] right = new int[n];
        Arrays.fill(left, -1);
        Arrays.fill(right, -1);
        int root = -1;
        for (int i = from; i < to; i++) {
            int node = i - from;
            long key = a[i];
            keys[node] = key;
            if (root < 0) {
                root = node;
                continue;
            }
            int cur = root;
            while (true) {
                if (key < keys[cur]) {
                    if (left[cur] < 0) {
                        left[cur] = node;
                        break;
                    }
                    cur = left[cur];
                } else {
                    if (right[cur] < 0) {
                        right[cur] = node;
                        break;
                    }
                    cur = right[cur];
                }
            }
        }
        int[] stack = new int[n];
        int sp = 0;
        int cur = root;
        int k = from;
        while (cur >= 0 || sp > 0) {
            while (cur >= 0) {
                stack[sp++] = cur;
                cur = left[cur];
            }
            cur = stack[--sp];
            a[k++] = keys[cur];
            cur = right[cur];
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
        tree(a, from, to);
    }

    private static void tree(float[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        float[] keys = new float[n];
        int[] left = new int[n];
        int[] right = new int[n];
        Arrays.fill(left, -1);
        Arrays.fill(right, -1);
        int root = -1;
        for (int i = from; i < to; i++) {
            int node = i - from;
            float key = a[i];
            keys[node] = key;
            if (root < 0) {
                root = node;
                continue;
            }
            int cur = root;
            while (true) {
                if (Float.compare(key, keys[cur]) < 0) {
                    if (left[cur] < 0) {
                        left[cur] = node;
                        break;
                    }
                    cur = left[cur];
                } else {
                    if (right[cur] < 0) {
                        right[cur] = node;
                        break;
                    }
                    cur = right[cur];
                }
            }
        }
        int[] stack = new int[n];
        int sp = 0;
        int cur = root;
        int k = from;
        while (cur >= 0 || sp > 0) {
            while (cur >= 0) {
                stack[sp++] = cur;
                cur = left[cur];
            }
            cur = stack[--sp];
            a[k++] = keys[cur];
            cur = right[cur];
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
        tree(a, from, to);
    }

    private static void tree(double[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        double[] keys = new double[n];
        int[] left = new int[n];
        int[] right = new int[n];
        Arrays.fill(left, -1);
        Arrays.fill(right, -1);
        int root = -1;
        for (int i = from; i < to; i++) {
            int node = i - from;
            double key = a[i];
            keys[node] = key;
            if (root < 0) {
                root = node;
                continue;
            }
            int cur = root;
            while (true) {
                if (Double.compare(key, keys[cur]) < 0) {
                    if (left[cur] < 0) {
                        left[cur] = node;
                        break;
                    }
                    cur = left[cur];
                } else {
                    if (right[cur] < 0) {
                        right[cur] = node;
                        break;
                    }
                    cur = right[cur];
                }
            }
        }
        int[] stack = new int[n];
        int sp = 0;
        int cur = root;
        int k = from;
        while (cur >= 0 || sp > 0) {
            while (cur >= 0) {
                stack[sp++] = cur;
                cur = left[cur];
            }
            cur = stack[--sp];
            a[k++] = keys[cur];
            cur = right[cur];
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
        tree(a, from, to);
    }

    private static void tree(char[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        char[] keys = new char[n];
        int[] left = new int[n];
        int[] right = new int[n];
        Arrays.fill(left, -1);
        Arrays.fill(right, -1);
        int root = -1;
        for (int i = from; i < to; i++) {
            int node = i - from;
            char key = a[i];
            keys[node] = key;
            if (root < 0) {
                root = node;
                continue;
            }
            int cur = root;
            while (true) {
                if (key < keys[cur]) {
                    if (left[cur] < 0) {
                        left[cur] = node;
                        break;
                    }
                    cur = left[cur];
                } else {
                    if (right[cur] < 0) {
                        right[cur] = node;
                        break;
                    }
                    cur = right[cur];
                }
            }
        }
        int[] stack = new int[n];
        int sp = 0;
        int cur = root;
        int k = from;
        while (cur >= 0 || sp > 0) {
            while (cur >= 0) {
                stack[sp++] = cur;
                cur = left[cur];
            }
            cur = stack[--sp];
            a[k++] = keys[cur];
            cur = right[cur];
        }
    }
}
