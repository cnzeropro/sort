package org.zero.sort;

import java.util.Comparator;
import java.util.List;

/**
 * Tim 排序（稳定，自适应）。
 * <p>
 * 检测并利用已有序片段（run），不足 minrun 的小片段用二分插入排序补齐，
 * 再按 run 栈不变量归并（JDK 2015 修正版，JDK-8072909）。
 * 最好 O(n)（已有序）、平均/最坏 O(n log n)；空间 O(n)。
 * 对象版为完整版（带 galloping 优化，{@code MIN_GALLOP = 7}），
 * 原始类型版为简化实现；是 {@link Sort} 门面类对对象排序的默认算法。
 * <p>
 * 支持任意 {@link Comparable} 对象数组 / {@link List}、任意 {@link Comparator} 对象数组 /
 * {@link List}，以及 byte / short / int / long / float / double / char 原始类型。
 * float / double 使用 {@link Float#compare} / {@link Double#compare} 全序（NaN 最后），
 * char 按无符号 16 位整数序。
 *
 * @author Zero
 */
public final class TimSort {

    private TimSort() {
    }

    /** Tim 排序的最小 run 长度基准 */
    private static final int TIM_MIN_MERGE = 32;

    /** Tim 排序进入 galloping 模式的连胜阈值（JDK 同款） */
    private static final int MIN_GALLOP = 7;

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
        tim(a, from, to, Comparator.naturalOrder());
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
        tim(a, from, to, comparator);
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

    /** 严格大于：a[i] &gt; a[j] */
    private static <T> boolean gt(T[] a, int i, int j, Comparator<? super T> cmp) {
        return cmp.compare(a[i], a[j]) > 0;
    }

    /** 严格小于：a[i] &lt; a[j] */
    private static <T> boolean lt(T[] a, int i, int j, Comparator<? super T> cmp) {
        return cmp.compare(a[i], a[j]) < 0;
    }

    /** 元素与基准值的严格大于：a[i] &gt; key */
    private static <T> boolean gtKey(T[] a, int i, T key, Comparator<? super T> cmp) {
        return cmp.compare(a[i], key) > 0;
    }

    /** 元素与基准值的严格小于：a[i] &lt; key */
    private static <T> boolean ltKey(T[] a, int i, T key, Comparator<? super T> cmp) {
        return cmp.compare(a[i], key) < 0;
    }

    /** 交换数组 a 的 i、j 位置元素 */
    private static <T> void swap(T[] a, int i, int j) {
        T tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    /** 插入排序（稳定，移位式，作为 Tim 的小数组底层实现） */
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

    /**
     * Tim 排序（稳定，自适应）
     */
    private static <T> void tim(T[] a, int from, int to, Comparator<? super T> cmp) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        if (n < TIM_MIN_MERGE) {
            insertion(a, from, to, cmp);
            return;
        }
        int minRun = minRunLength(n);
        int capacity = stackCapacity(n);
        int[] runBase = new int[capacity];
        int[] runLen = new int[capacity];
        @SuppressWarnings("unchecked")
        T[] tmp = (T[]) new Object[n];
        int stackSize = 0;
        int lo = from;
        int remaining = n;
        while (remaining > 0) {
            int run = countRunAndMakeAscending(a, lo, to, cmp);
            if (run < minRun) {
                int force = Math.min(minRun, remaining);
                binarySort(a, lo, lo + force, lo + run, cmp);
                run = force;
            }
            runBase[stackSize] = lo;
            runLen[stackSize] = run;
            stackSize++;
            stackSize = mergeCollapse(a, from, tmp, runBase, runLen, stackSize, cmp);
            lo += run;
            remaining -= run;
        }
        mergeForceCollapse(a, from, tmp, runBase, runLen, stackSize, cmp);
    }

    /**
     * 计算 minrun（JDK 位技巧，结果落在 [16, 32]）
     */
    private static int minRunLength(int n) {
        int r = 0;
        while (n >= TIM_MIN_MERGE) {
            r |= (n & 1);
            n >>= 1;
        }
        return n + r;
    }

    /**
     * run 栈容量（JDK 表，与 run 栈不变量配套）
     */
    private static int stackCapacity(int len) {
        if (len < 120) {
            return 5;
        }
        if (len < 1542) {
            return 10;
        }
        if (len < 119151) {
            return 24;
        }
        return 40;
    }

    /**
     * 统计 [lo, hi) 开头最长的升/降序 run：严格降序则反转为升序；
     * 升序（含相等）直接沿用。返回 run 长度。
     */
    private static <T> int countRunAndMakeAscending(T[] a, int lo, int hi, Comparator<? super T> cmp) {
        int runHi = lo + 1;
        if (runHi == hi) {
            return 1;
        }
        if (lt(a, runHi, lo, cmp)) {
            while (runHi < hi && lt(a, runHi, runHi - 1, cmp)) {
                runHi++;
            }
            reverseRange(a, lo, runHi - 1);
        } else {
            while (runHi < hi && !lt(a, runHi, runHi - 1, cmp)) {
                runHi++;
            }
        }
        return runHi - lo;
    }

    /**
     * 反转 [lo, hi] 闭区间
     */
    private static <T> void reverseRange(T[] a, int lo, int hi) {
        while (lo < hi) {
            swap(a, lo, hi);
            lo++;
            hi--;
        }
    }

    /**
     * 稳定二分插入排序：把 [start, hi) 依次二分插入已有序的 [lo, start)
     */
    private static <T> void binarySort(T[] a, int lo, int hi, int start, Comparator<? super T> cmp) {
        if (start == lo) {
            start++;
        }
        for (; start < hi; start++) {
            T pivot = a[start];
            int left = lo;
            int right = start;
            while (left < right) {
                int mid = (left + right) >>> 1;
                if (gtKey(a, mid, pivot, cmp)) {
                    right = mid;
                } else {
                    left = mid + 1;
                }
            }
            int n = start - left;
            System.arraycopy(a, left, a, left + 1, n);
            a[left] = pivot;
        }
    }

    /**
     * 维护 run 栈不变量（JDK-8072909 修正版）
     */
    private static <T> int mergeCollapse(
            T[] a, int from, T[] tmp, int[] runBase, int[] runLen, int stackSize, Comparator<? super T> cmp) {
        while (stackSize > 1) {
            int n = stackSize - 2;
            if (n > 0 && runLen[n - 1] <= runLen[n] + runLen[n + 1]) {
                if (runLen[n - 1] < runLen[n + 1]) {
                    n--;
                }
                stackSize = mergeAt(a, from, tmp, runBase, runLen, stackSize, n, cmp);
            } else if (runLen[n] <= runLen[n + 1]) {
                stackSize = mergeAt(a, from, tmp, runBase, runLen, stackSize, n, cmp);
            } else {
                break;
            }
        }
        return stackSize;
    }

    /**
     * 强制合并 run 栈上的全部 run（排序收尾）
     */
    private static <T> void mergeForceCollapse(
            T[] a, int from, T[] tmp, int[] runBase, int[] runLen, int stackSize, Comparator<? super T> cmp) {
        while (stackSize > 1) {
            int n = stackSize - 2;
            if (n > 0 && runLen[n - 1] < runLen[n + 1]) {
                n--;
            }
            stackSize = mergeAt(a, from, tmp, runBase, runLen, stackSize, n, cmp);
        }
    }

    /**
     * 归并 run 栈上相邻的两个 run（i 与 i+1），把栈顶下移一位
     *
     * @return 归并后的 run 栈大小（stackSize - 1）
     */
    private static <T> int mergeAt(
            T[] a, int from, T[] tmp, int[] runBase, int[] runLen, int stackSize, int i,
            Comparator<? super T> cmp) {
        int base1 = runBase[i];
        int len1 = runLen[i];
        int base2 = runBase[i + 1];
        int len2 = runLen[i + 1];
        runLen[i] = len1 + len2;
        if (i == stackSize - 3) {
            runBase[i + 1] = runBase[i + 2];
            runLen[i + 1] = runLen[i + 2];
        }
        int k = gallopRight(a[base2], a, base1, len1, 0, cmp);
        base1 += k;
        len1 -= k;
        if (len1 == 0) {
            return stackSize - 1;
        }
        len2 = gallopLeft(a[base1 + len1 - 1], a, base2, len2, len2 - 1, cmp);
        if (len2 == 0) {
            return stackSize - 1;
        }
        if (len1 <= len2) {
            System.arraycopy(a, base1, tmp, 0, len1);
            mergeLo(a, tmp, base1, len1, base2, len2, cmp);
        } else {
            System.arraycopy(a, base2, tmp, 0, len2);
            mergeHi(a, tmp, base1, len1, base2, len2, cmp);
        }
        return stackSize - 1;
    }

    /**
     * 左 run 在 tmp（就地回写 a）、右 run 在原数组的 galloping 归并
     */
    private static <T> void mergeLo(
            T[] a, T[] tmp, int base1, int len1, int base2, int len2, Comparator<? super T> cmp) {
        int i1 = 0;
        int i2 = base2;
        int k = base1;
        int end2 = base2 + len2;
        int minGallop = MIN_GALLOP;
        while (true) {
            int count1 = 0;
            int count2 = 0;
            while (i1 < len1 && i2 < end2 && count1 < minGallop && count2 < minGallop) {
                if (ltCross(a, i2, tmp, i1, cmp)) {
                    a[k++] = a[i2++];
                    count2++;
                    count1 = 0;
                } else {
                    a[k++] = tmp[i1++];
                    count1++;
                    count2 = 0;
                }
            }
            if (i1 == len1 || i2 == end2) {
                break;
            }
            int jumped;
            if (count1 >= minGallop) {
                jumped = gallopRight(a[i2], tmp, i1, len1 - i1, 0, cmp);
                System.arraycopy(tmp, i1, a, k, jumped);
                i1 += jumped;
                k += jumped;
            } else {
                jumped = gallopLeft(tmp[i1], a, i2, end2 - i2, 0, cmp);
                System.arraycopy(a, i2, a, k, jumped);
                i2 += jumped;
                k += jumped;
            }
            if (i1 == len1 || i2 == end2) {
                break;
            }
            minGallop++;
        }
        while (i1 < len1) {
            a[k++] = tmp[i1++];
        }
    }

    /**
     * 右 run 在 tmp、左 run 在原数组的 galloping 归并（从右往左回写，保证稳定）
     */
    private static <T> void mergeHi(
            T[] a, T[] tmp, int base1, int len1, int base2, int len2, Comparator<? super T> cmp) {
        int i1 = base1 + len1 - 1;
        int i2 = len2 - 1;
        int k = base2 + len2 - 1;
        int start1 = base1;
        int minGallop = MIN_GALLOP;
        while (true) {
            int count1 = 0;
            int count2 = 0;
            while (i1 >= start1 && i2 >= 0 && count1 < minGallop && count2 < minGallop) {
                if (!ltCross(tmp, i2, a, i1, cmp)) {
                    a[k--] = tmp[i2--];
                    count2++;
                    count1 = 0;
                } else {
                    a[k--] = a[i1--];
                    count1++;
                    count2 = 0;
                }
            }
            if (i1 < start1 || i2 < 0) {
                break;
            }
            int jumped;
            if (count1 >= minGallop) {
                jumped = i1 - start1 + 1 - gallopRight(tmp[i2], a, start1, i1 - start1 + 1, i1 - start1, cmp);
                while (jumped-- > 0) {
                    a[k--] = a[i1--];
                }
            } else {
                jumped = i2 + 1 - gallopLeft(a[i1], tmp, 0, i2 + 1, i2, cmp);
                while (jumped-- > 0) {
                    a[k--] = tmp[i2--];
                }
            }
            if (i1 < start1 || i2 < 0) {
                break;
            }
            minGallop++;
        }
        while (i2 >= 0) {
            a[k--] = tmp[i2--];
        }
    }

    /**
     * gallopRight：在已排序区间 a[base, base+len) 中返回 key 应插入的位置——
     * 位于所有等于 key 的元素之后（right of equal keys，JDK 同款）
     */
    private static <T> int gallopRight(
            T key, T[] a, int base, int len, int hint, Comparator<? super T> cmp) {
        int lastOfs = 0;
        int ofs = 1;
        if (cmp.compare(key, a[base + hint]) < 0) {
            int maxOfs = hint + 1;
            while (ofs < maxOfs && cmp.compare(key, a[base + hint - ofs]) < 0) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            int tmpOfs = lastOfs;
            lastOfs = hint - ofs;
            ofs = hint - tmpOfs;
        } else {
            int maxOfs = len - hint;
            while (ofs < maxOfs && cmp.compare(key, a[base + hint + ofs]) >= 0) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            lastOfs += hint;
            ofs += hint;
        }
        lastOfs++;
        while (lastOfs < ofs) {
            int m = lastOfs + ((ofs - lastOfs) >>> 1);
            if (cmp.compare(key, a[base + m]) < 0) {
                ofs = m;
            } else {
                lastOfs = m + 1;
            }
        }
        return ofs;
    }

    /**
     * gallopLeft：在已排序区间 a[base, base+len) 中返回 key 应插入的位置——
     * 位于所有等于 key 的元素之前（left of equal keys，JDK 同款）
     */
    private static <T> int gallopLeft(
            T key, T[] a, int base, int len, int hint, Comparator<? super T> cmp) {
        int lastOfs = 0;
        int ofs = 1;
        if (cmp.compare(key, a[base + hint]) > 0) {
            int maxOfs = len - hint;
            while (ofs < maxOfs && cmp.compare(key, a[base + hint + ofs]) > 0) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            lastOfs += hint;
            ofs += hint;
        } else {
            int maxOfs = hint + 1;
            while (ofs < maxOfs && cmp.compare(key, a[base + hint - ofs]) <= 0) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            int tmpOfs = lastOfs;
            lastOfs = hint - ofs;
            ofs = hint - tmpOfs;
        }
        lastOfs++;
        while (lastOfs < ofs) {
            int m = lastOfs + ((ofs - lastOfs) >>> 1);
            if (cmp.compare(key, a[base + m]) > 0) {
                lastOfs = m + 1;
            } else {
                ofs = m;
            }
        }
        return ofs;
    }

    /**
     * 跨数组严格小于：a[i] &lt; b[j]
     */
    private static <T> boolean ltCross(T[] a, int i, T[] b, int j, Comparator<? super T> cmp) {
        return cmp.compare(a[i], b[j]) < 0;
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
        tim(a, from, to);
    }

    private static boolean gt(byte[] a, int i, int j) {
        return a[i] > a[j];
    }

    private static boolean gtKey(byte[] a, int i, byte key) {
        return a[i] > key;
    }

    private static boolean ltKey(byte[] a, int i, byte key) {
        return a[i] < key;
    }

    private static boolean lt(byte[] a, int i, int j) {
        return a[i] < a[j];
    }

    private static boolean ltCross(byte[] a, int i, byte[] b, int j) {
        return a[i] < b[j];
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

    private static void tim(byte[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        if (n < TIM_MIN_MERGE) {
            insertion(a, from, to);
            return;
        }
        int minRun = minRunLength(n);
        int capacity = stackCapacity(n);
        int[] runBase = new int[capacity];
        int[] runLen = new int[capacity];
        byte[] tmp = new byte[n];
        int stackSize = 0;
        int lo = from;
        int remaining = n;
        while (remaining > 0) {
            int run = countRunAndMakeAscending(a, lo, to);
            if (run < minRun) {
                int force = Math.min(minRun, remaining);
                binarySort(a, lo, lo + force, lo + run);
                run = force;
            }
            runBase[stackSize] = lo;
            runLen[stackSize] = run;
            stackSize++;
            stackSize = mergeCollapse(a, tmp, runBase, runLen, stackSize);
            lo += run;
            remaining -= run;
        }
        mergeForceCollapse(a, tmp, runBase, runLen, stackSize);
    }

    private static int countRunAndMakeAscending(byte[] a, int lo, int hi) {
        int runHi = lo + 1;
        if (runHi == hi) {
            return 1;
        }
        if (lt(a, runHi, lo)) {
            while (runHi < hi && lt(a, runHi, runHi - 1)) {
                runHi++;
            }
            reverseRange(a, lo, runHi - 1);
        } else {
            while (runHi < hi && !lt(a, runHi, runHi - 1)) {
                runHi++;
            }
        }
        return runHi - lo;
    }

    private static void reverseRange(byte[] a, int lo, int hi) {
        while (lo < hi) {
            swap(a, lo, hi);
            lo++;
            hi--;
        }
    }

    private static void binarySort(byte[] a, int lo, int hi, int start) {
        if (start == lo) {
            start++;
        }
        for (; start < hi; start++) {
            byte pivot = a[start];
            int left = lo;
            int right = start;
            while (left < right) {
                int mid = (left + right) >>> 1;
                if (gtKey(a, mid, pivot)) {
                    right = mid;
                } else {
                    left = mid + 1;
                }
            }
            int n = start - left;
            System.arraycopy(a, left, a, left + 1, n);
            a[left] = pivot;
        }
    }

    private static int mergeCollapse(byte[] a, byte[] tmp, int[] runBase, int[] runLen, int stackSize) {
        while (stackSize > 1) {
            int n = stackSize - 2;
            if (n > 0 && runLen[n - 1] <= runLen[n] + runLen[n + 1]) {
                if (runLen[n - 1] < runLen[n + 1]) {
                    n--;
                }
                stackSize = mergeAt(a, tmp, runBase, runLen, stackSize, n);
            } else if (runLen[n] <= runLen[n + 1]) {
                stackSize = mergeAt(a, tmp, runBase, runLen, stackSize, n);
            } else {
                break;
            }
        }
        return stackSize;
    }

    private static void mergeForceCollapse(byte[] a, byte[] tmp, int[] runBase, int[] runLen, int stackSize) {
        while (stackSize > 1) {
            int n = stackSize - 2;
            if (n > 0 && runLen[n - 1] < runLen[n + 1]) {
                n--;
            }
            stackSize = mergeAt(a, tmp, runBase, runLen, stackSize, n);
        }
    }

    private static int mergeAt(byte[] a, byte[] tmp, int[] runBase, int[] runLen, int stackSize, int i) {
        int base1 = runBase[i];
        int len1 = runLen[i];
        int base2 = runBase[i + 1];
        int len2 = runLen[i + 1];
        runLen[i] = len1 + len2;
        if (i == stackSize - 3) {
            runBase[i + 1] = runBase[i + 2];
            runLen[i + 1] = runLen[i + 2];
        }
        int k = gallopRight(a[base2], a, base1, len1, 0);
        base1 += k;
        len1 -= k;
        if (len1 == 0) {
            return stackSize - 1;
        }
        len2 = gallopLeft(a[base1 + len1 - 1], a, base2, len2, len2 - 1);
        if (len2 == 0) {
            return stackSize - 1;
        }
        if (len1 <= len2) {
            System.arraycopy(a, base1, tmp, 0, len1);
            mergeLo(a, tmp, base1, len1, base2, len2);
        } else {
            System.arraycopy(a, base2, tmp, 0, len2);
            mergeHi(a, tmp, base1, len1, base2, len2);
        }
        return stackSize - 1;
    }

    private static void mergeLo(byte[] a, byte[] tmp, int base1, int len1, int base2, int len2) {
        int i1 = 0;
        int i2 = base2;
        int k = base1;
        int end2 = base2 + len2;
        int minGallop = MIN_GALLOP;
        while (true) {
            int count1 = 0;
            int count2 = 0;
            while (i1 < len1 && i2 < end2 && count1 < minGallop && count2 < minGallop) {
                if (ltCross(a, i2, tmp, i1)) {
                    a[k++] = a[i2++];
                    count2++;
                    count1 = 0;
                } else {
                    a[k++] = tmp[i1++];
                    count1++;
                    count2 = 0;
                }
            }
            if (i1 == len1 || i2 == end2) {
                break;
            }
            int jumped;
            if (count1 >= minGallop) {
                jumped = gallopRight(a[i2], tmp, i1, len1 - i1, 0);
                System.arraycopy(tmp, i1, a, k, jumped);
                i1 += jumped;
                k += jumped;
            } else {
                jumped = gallopLeft(tmp[i1], a, i2, end2 - i2, 0);
                System.arraycopy(a, i2, a, k, jumped);
                i2 += jumped;
                k += jumped;
            }
            if (i1 == len1 || i2 == end2) {
                break;
            }
            minGallop++;
        }
        while (i1 < len1) {
            a[k++] = tmp[i1++];
        }
    }

    private static void mergeHi(byte[] a, byte[] tmp, int base1, int len1, int base2, int len2) {
        int i1 = base1 + len1 - 1;
        int i2 = len2 - 1;
        int k = base2 + len2 - 1;
        int start1 = base1;
        int minGallop = MIN_GALLOP;
        while (true) {
            int count1 = 0;
            int count2 = 0;
            while (i1 >= start1 && i2 >= 0 && count1 < minGallop && count2 < minGallop) {
                if (gtCross(tmp, i2, a, i1)) {
                    a[k--] = tmp[i2--];
                    count2++;
                    count1 = 0;
                } else {
                    a[k--] = a[i1--];
                    count1++;
                    count2 = 0;
                }
            }
            if (i1 < start1 || i2 < 0) {
                break;
            }
            int jumped;
            if (count1 >= minGallop) {
                jumped = i1 - start1 + 1 - gallopRight(tmp[i2], a, start1, i1 - start1 + 1, i1 - start1);
                while (jumped-- > 0) {
                    a[k--] = a[i1--];
                }
            } else {
                jumped = i2 + 1 - gallopLeft(a[i1], tmp, 0, i2 + 1, i2);
                while (jumped-- > 0) {
                    a[k--] = tmp[i2--];
                }
            }
            if (i1 < start1 || i2 < 0) {
                break;
            }
            minGallop++;
        }
        while (i2 >= 0) {
            a[k--] = tmp[i2--];
        }
    }

    private static int gallopRight(byte key, byte[] a, int base, int len, int hint) {
        int lastOfs = 0;
        int ofs = 1;
        if (ltKey(a, base + hint, key)) {
            int maxOfs = len - hint;
            while (ofs < maxOfs && ltKey(a, base + hint + ofs, key)) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            lastOfs += hint;
            ofs += hint;
        } else {
            int maxOfs = hint + 1;
            while (ofs < maxOfs && !ltKey(a, base + hint - ofs, key)) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            int tmpOfs = lastOfs;
            lastOfs = hint - ofs;
            ofs = hint - tmpOfs;
        }
        lastOfs++;
        while (lastOfs < ofs) {
            int m = lastOfs + ((ofs - lastOfs) >>> 1);
            if (ltKey(a, base + m, key)) {
                lastOfs = m + 1;
            } else {
                ofs = m;
            }
        }
        return ofs;
    }

    private static int gallopLeft(byte key, byte[] a, int base, int len, int hint) {
        int lastOfs = 0;
        int ofs = 1;
        if (ltKey(a, base + hint, key)) {
            int maxOfs = len - hint;
            while (ofs < maxOfs && ltKey(a, base + hint + ofs, key)) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            lastOfs += hint;
            ofs += hint;
        } else {
            int maxOfs = hint + 1;
            while (ofs < maxOfs && !ltKey(a, base + hint - ofs, key)) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            int tmpOfs = lastOfs;
            lastOfs = hint - ofs;
            ofs = hint - tmpOfs;
        }
        lastOfs++;
        while (lastOfs < ofs) {
            int m = lastOfs + ((ofs - lastOfs) >>> 1);
            if (ltKey(a, base + m, key)) {
                lastOfs = m + 1;
            } else {
                ofs = m;
            }
        }
        return ofs;
    }

    private static boolean gtCross(byte[] a, int i, byte[] b, int j) {
        return ltCross(b, j, a, i);
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
        tim(a, from, to);
    }

    private static boolean gt(short[] a, int i, int j) {
        return a[i] > a[j];
    }

    private static boolean gtKey(short[] a, int i, short key) {
        return a[i] > key;
    }

    private static boolean ltKey(short[] a, int i, short key) {
        return a[i] < key;
    }

    private static boolean lt(short[] a, int i, int j) {
        return a[i] < a[j];
    }

    private static boolean ltCross(short[] a, int i, short[] b, int j) {
        return a[i] < b[j];
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

    private static void tim(short[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        if (n < TIM_MIN_MERGE) {
            insertion(a, from, to);
            return;
        }
        int minRun = minRunLength(n);
        int capacity = stackCapacity(n);
        int[] runBase = new int[capacity];
        int[] runLen = new int[capacity];
        short[] tmp = new short[n];
        int stackSize = 0;
        int lo = from;
        int remaining = n;
        while (remaining > 0) {
            int run = countRunAndMakeAscending(a, lo, to);
            if (run < minRun) {
                int force = Math.min(minRun, remaining);
                binarySort(a, lo, lo + force, lo + run);
                run = force;
            }
            runBase[stackSize] = lo;
            runLen[stackSize] = run;
            stackSize++;
            stackSize = mergeCollapse(a, tmp, runBase, runLen, stackSize);
            lo += run;
            remaining -= run;
        }
        mergeForceCollapse(a, tmp, runBase, runLen, stackSize);
    }

    private static int countRunAndMakeAscending(short[] a, int lo, int hi) {
        int runHi = lo + 1;
        if (runHi == hi) {
            return 1;
        }
        if (lt(a, runHi, lo)) {
            while (runHi < hi && lt(a, runHi, runHi - 1)) {
                runHi++;
            }
            reverseRange(a, lo, runHi - 1);
        } else {
            while (runHi < hi && !lt(a, runHi, runHi - 1)) {
                runHi++;
            }
        }
        return runHi - lo;
    }

    private static void reverseRange(short[] a, int lo, int hi) {
        while (lo < hi) {
            swap(a, lo, hi);
            lo++;
            hi--;
        }
    }

    private static void binarySort(short[] a, int lo, int hi, int start) {
        if (start == lo) {
            start++;
        }
        for (; start < hi; start++) {
            short pivot = a[start];
            int left = lo;
            int right = start;
            while (left < right) {
                int mid = (left + right) >>> 1;
                if (gtKey(a, mid, pivot)) {
                    right = mid;
                } else {
                    left = mid + 1;
                }
            }
            int n = start - left;
            System.arraycopy(a, left, a, left + 1, n);
            a[left] = pivot;
        }
    }

    private static int mergeCollapse(short[] a, short[] tmp, int[] runBase, int[] runLen, int stackSize) {
        while (stackSize > 1) {
            int n = stackSize - 2;
            if (n > 0 && runLen[n - 1] <= runLen[n] + runLen[n + 1]) {
                if (runLen[n - 1] < runLen[n + 1]) {
                    n--;
                }
                stackSize = mergeAt(a, tmp, runBase, runLen, stackSize, n);
            } else if (runLen[n] <= runLen[n + 1]) {
                stackSize = mergeAt(a, tmp, runBase, runLen, stackSize, n);
            } else {
                break;
            }
        }
        return stackSize;
    }

    private static void mergeForceCollapse(short[] a, short[] tmp, int[] runBase, int[] runLen, int stackSize) {
        while (stackSize > 1) {
            int n = stackSize - 2;
            if (n > 0 && runLen[n - 1] < runLen[n + 1]) {
                n--;
            }
            stackSize = mergeAt(a, tmp, runBase, runLen, stackSize, n);
        }
    }

    private static int mergeAt(short[] a, short[] tmp, int[] runBase, int[] runLen, int stackSize, int i) {
        int base1 = runBase[i];
        int len1 = runLen[i];
        int base2 = runBase[i + 1];
        int len2 = runLen[i + 1];
        runLen[i] = len1 + len2;
        if (i == stackSize - 3) {
            runBase[i + 1] = runBase[i + 2];
            runLen[i + 1] = runLen[i + 2];
        }
        int k = gallopRight(a[base2], a, base1, len1, 0);
        base1 += k;
        len1 -= k;
        if (len1 == 0) {
            return stackSize - 1;
        }
        len2 = gallopLeft(a[base1 + len1 - 1], a, base2, len2, len2 - 1);
        if (len2 == 0) {
            return stackSize - 1;
        }
        if (len1 <= len2) {
            System.arraycopy(a, base1, tmp, 0, len1);
            mergeLo(a, tmp, base1, len1, base2, len2);
        } else {
            System.arraycopy(a, base2, tmp, 0, len2);
            mergeHi(a, tmp, base1, len1, base2, len2);
        }
        return stackSize - 1;
    }

    private static void mergeLo(short[] a, short[] tmp, int base1, int len1, int base2, int len2) {
        int i1 = 0;
        int i2 = base2;
        int k = base1;
        int end2 = base2 + len2;
        int minGallop = MIN_GALLOP;
        while (true) {
            int count1 = 0;
            int count2 = 0;
            while (i1 < len1 && i2 < end2 && count1 < minGallop && count2 < minGallop) {
                if (ltCross(a, i2, tmp, i1)) {
                    a[k++] = a[i2++];
                    count2++;
                    count1 = 0;
                } else {
                    a[k++] = tmp[i1++];
                    count1++;
                    count2 = 0;
                }
            }
            if (i1 == len1 || i2 == end2) {
                break;
            }
            int jumped;
            if (count1 >= minGallop) {
                jumped = gallopRight(a[i2], tmp, i1, len1 - i1, 0);
                System.arraycopy(tmp, i1, a, k, jumped);
                i1 += jumped;
                k += jumped;
            } else {
                jumped = gallopLeft(tmp[i1], a, i2, end2 - i2, 0);
                System.arraycopy(a, i2, a, k, jumped);
                i2 += jumped;
                k += jumped;
            }
            if (i1 == len1 || i2 == end2) {
                break;
            }
            minGallop++;
        }
        while (i1 < len1) {
            a[k++] = tmp[i1++];
        }
    }

    private static void mergeHi(short[] a, short[] tmp, int base1, int len1, int base2, int len2) {
        int i1 = base1 + len1 - 1;
        int i2 = len2 - 1;
        int k = base2 + len2 - 1;
        int start1 = base1;
        int minGallop = MIN_GALLOP;
        while (true) {
            int count1 = 0;
            int count2 = 0;
            while (i1 >= start1 && i2 >= 0 && count1 < minGallop && count2 < minGallop) {
                if (gtCross(tmp, i2, a, i1)) {
                    a[k--] = tmp[i2--];
                    count2++;
                    count1 = 0;
                } else {
                    a[k--] = a[i1--];
                    count1++;
                    count2 = 0;
                }
            }
            if (i1 < start1 || i2 < 0) {
                break;
            }
            int jumped;
            if (count1 >= minGallop) {
                jumped = i1 - start1 + 1 - gallopRight(tmp[i2], a, start1, i1 - start1 + 1, i1 - start1);
                while (jumped-- > 0) {
                    a[k--] = a[i1--];
                }
            } else {
                jumped = i2 + 1 - gallopLeft(a[i1], tmp, 0, i2 + 1, i2);
                while (jumped-- > 0) {
                    a[k--] = tmp[i2--];
                }
            }
            if (i1 < start1 || i2 < 0) {
                break;
            }
            minGallop++;
        }
        while (i2 >= 0) {
            a[k--] = tmp[i2--];
        }
    }

    private static int gallopRight(short key, short[] a, int base, int len, int hint) {
        int lastOfs = 0;
        int ofs = 1;
        if (ltKey(a, base + hint, key)) {
            int maxOfs = len - hint;
            while (ofs < maxOfs && ltKey(a, base + hint + ofs, key)) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            lastOfs += hint;
            ofs += hint;
        } else {
            int maxOfs = hint + 1;
            while (ofs < maxOfs && !ltKey(a, base + hint - ofs, key)) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            int tmpOfs = lastOfs;
            lastOfs = hint - ofs;
            ofs = hint - tmpOfs;
        }
        lastOfs++;
        while (lastOfs < ofs) {
            int m = lastOfs + ((ofs - lastOfs) >>> 1);
            if (ltKey(a, base + m, key)) {
                lastOfs = m + 1;
            } else {
                ofs = m;
            }
        }
        return ofs;
    }

    private static int gallopLeft(short key, short[] a, int base, int len, int hint) {
        int lastOfs = 0;
        int ofs = 1;
        if (ltKey(a, base + hint, key)) {
            int maxOfs = len - hint;
            while (ofs < maxOfs && ltKey(a, base + hint + ofs, key)) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            lastOfs += hint;
            ofs += hint;
        } else {
            int maxOfs = hint + 1;
            while (ofs < maxOfs && !ltKey(a, base + hint - ofs, key)) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            int tmpOfs = lastOfs;
            lastOfs = hint - ofs;
            ofs = hint - tmpOfs;
        }
        lastOfs++;
        while (lastOfs < ofs) {
            int m = lastOfs + ((ofs - lastOfs) >>> 1);
            if (ltKey(a, base + m, key)) {
                lastOfs = m + 1;
            } else {
                ofs = m;
            }
        }
        return ofs;
    }

    private static boolean gtCross(short[] a, int i, short[] b, int j) {
        return ltCross(b, j, a, i);
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
        tim(a, from, to);
    }

    private static boolean gt(int[] a, int i, int j) {
        return a[i] > a[j];
    }

    private static boolean gtKey(int[] a, int i, int key) {
        return a[i] > key;
    }

    private static boolean ltKey(int[] a, int i, int key) {
        return a[i] < key;
    }

    private static boolean lt(int[] a, int i, int j) {
        return a[i] < a[j];
    }

    private static boolean ltCross(int[] a, int i, int[] b, int j) {
        return a[i] < b[j];
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

    private static void tim(int[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        if (n < TIM_MIN_MERGE) {
            insertion(a, from, to);
            return;
        }
        int minRun = minRunLength(n);
        int capacity = stackCapacity(n);
        int[] runBase = new int[capacity];
        int[] runLen = new int[capacity];
        int[] tmp = new int[n];
        int stackSize = 0;
        int lo = from;
        int remaining = n;
        while (remaining > 0) {
            int run = countRunAndMakeAscending(a, lo, to);
            if (run < minRun) {
                int force = Math.min(minRun, remaining);
                binarySort(a, lo, lo + force, lo + run);
                run = force;
            }
            runBase[stackSize] = lo;
            runLen[stackSize] = run;
            stackSize++;
            stackSize = mergeCollapse(a, tmp, runBase, runLen, stackSize);
            lo += run;
            remaining -= run;
        }
        mergeForceCollapse(a, tmp, runBase, runLen, stackSize);
    }

    private static int countRunAndMakeAscending(int[] a, int lo, int hi) {
        int runHi = lo + 1;
        if (runHi == hi) {
            return 1;
        }
        if (lt(a, runHi, lo)) {
            while (runHi < hi && lt(a, runHi, runHi - 1)) {
                runHi++;
            }
            reverseRange(a, lo, runHi - 1);
        } else {
            while (runHi < hi && !lt(a, runHi, runHi - 1)) {
                runHi++;
            }
        }
        return runHi - lo;
    }

    private static void reverseRange(int[] a, int lo, int hi) {
        while (lo < hi) {
            swap(a, lo, hi);
            lo++;
            hi--;
        }
    }

    private static void binarySort(int[] a, int lo, int hi, int start) {
        if (start == lo) {
            start++;
        }
        for (; start < hi; start++) {
            int pivot = a[start];
            int left = lo;
            int right = start;
            while (left < right) {
                int mid = (left + right) >>> 1;
                if (gtKey(a, mid, pivot)) {
                    right = mid;
                } else {
                    left = mid + 1;
                }
            }
            int n = start - left;
            System.arraycopy(a, left, a, left + 1, n);
            a[left] = pivot;
        }
    }

    private static int mergeCollapse(int[] a, int[] tmp, int[] runBase, int[] runLen, int stackSize) {
        while (stackSize > 1) {
            int n = stackSize - 2;
            if (n > 0 && runLen[n - 1] <= runLen[n] + runLen[n + 1]) {
                if (runLen[n - 1] < runLen[n + 1]) {
                    n--;
                }
                stackSize = mergeAt(a, tmp, runBase, runLen, stackSize, n);
            } else if (runLen[n] <= runLen[n + 1]) {
                stackSize = mergeAt(a, tmp, runBase, runLen, stackSize, n);
            } else {
                break;
            }
        }
        return stackSize;
    }

    private static void mergeForceCollapse(int[] a, int[] tmp, int[] runBase, int[] runLen, int stackSize) {
        while (stackSize > 1) {
            int n = stackSize - 2;
            if (n > 0 && runLen[n - 1] < runLen[n + 1]) {
                n--;
            }
            stackSize = mergeAt(a, tmp, runBase, runLen, stackSize, n);
        }
    }

    private static int mergeAt(int[] a, int[] tmp, int[] runBase, int[] runLen, int stackSize, int i) {
        int base1 = runBase[i];
        int len1 = runLen[i];
        int base2 = runBase[i + 1];
        int len2 = runLen[i + 1];
        runLen[i] = len1 + len2;
        if (i == stackSize - 3) {
            runBase[i + 1] = runBase[i + 2];
            runLen[i + 1] = runLen[i + 2];
        }
        int k = gallopRight(a[base2], a, base1, len1, 0);
        base1 += k;
        len1 -= k;
        if (len1 == 0) {
            return stackSize - 1;
        }
        len2 = gallopLeft(a[base1 + len1 - 1], a, base2, len2, len2 - 1);
        if (len2 == 0) {
            return stackSize - 1;
        }
        if (len1 <= len2) {
            System.arraycopy(a, base1, tmp, 0, len1);
            mergeLo(a, tmp, base1, len1, base2, len2);
        } else {
            System.arraycopy(a, base2, tmp, 0, len2);
            mergeHi(a, tmp, base1, len1, base2, len2);
        }
        return stackSize - 1;
    }

    private static void mergeLo(int[] a, int[] tmp, int base1, int len1, int base2, int len2) {
        int i1 = 0;
        int i2 = base2;
        int k = base1;
        int end2 = base2 + len2;
        int minGallop = MIN_GALLOP;
        while (true) {
            int count1 = 0;
            int count2 = 0;
            while (i1 < len1 && i2 < end2 && count1 < minGallop && count2 < minGallop) {
                if (ltCross(a, i2, tmp, i1)) {
                    a[k++] = a[i2++];
                    count2++;
                    count1 = 0;
                } else {
                    a[k++] = tmp[i1++];
                    count1++;
                    count2 = 0;
                }
            }
            if (i1 == len1 || i2 == end2) {
                break;
            }
            int jumped;
            if (count1 >= minGallop) {
                jumped = gallopRight(a[i2], tmp, i1, len1 - i1, 0);
                System.arraycopy(tmp, i1, a, k, jumped);
                i1 += jumped;
                k += jumped;
            } else {
                jumped = gallopLeft(tmp[i1], a, i2, end2 - i2, 0);
                System.arraycopy(a, i2, a, k, jumped);
                i2 += jumped;
                k += jumped;
            }
            if (i1 == len1 || i2 == end2) {
                break;
            }
            minGallop++;
        }
        while (i1 < len1) {
            a[k++] = tmp[i1++];
        }
    }

    private static void mergeHi(int[] a, int[] tmp, int base1, int len1, int base2, int len2) {
        int i1 = base1 + len1 - 1;
        int i2 = len2 - 1;
        int k = base2 + len2 - 1;
        int start1 = base1;
        int minGallop = MIN_GALLOP;
        while (true) {
            int count1 = 0;
            int count2 = 0;
            while (i1 >= start1 && i2 >= 0 && count1 < minGallop && count2 < minGallop) {
                if (gtCross(tmp, i2, a, i1)) {
                    a[k--] = tmp[i2--];
                    count2++;
                    count1 = 0;
                } else {
                    a[k--] = a[i1--];
                    count1++;
                    count2 = 0;
                }
            }
            if (i1 < start1 || i2 < 0) {
                break;
            }
            int jumped;
            if (count1 >= minGallop) {
                jumped = i1 - start1 + 1 - gallopRight(tmp[i2], a, start1, i1 - start1 + 1, i1 - start1);
                while (jumped-- > 0) {
                    a[k--] = a[i1--];
                }
            } else {
                jumped = i2 + 1 - gallopLeft(a[i1], tmp, 0, i2 + 1, i2);
                while (jumped-- > 0) {
                    a[k--] = tmp[i2--];
                }
            }
            if (i1 < start1 || i2 < 0) {
                break;
            }
            minGallop++;
        }
        while (i2 >= 0) {
            a[k--] = tmp[i2--];
        }
    }

    private static int gallopRight(int key, int[] a, int base, int len, int hint) {
        int lastOfs = 0;
        int ofs = 1;
        if (ltKey(a, base + hint, key)) {
            int maxOfs = len - hint;
            while (ofs < maxOfs && ltKey(a, base + hint + ofs, key)) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            lastOfs += hint;
            ofs += hint;
        } else {
            int maxOfs = hint + 1;
            while (ofs < maxOfs && !ltKey(a, base + hint - ofs, key)) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            int tmpOfs = lastOfs;
            lastOfs = hint - ofs;
            ofs = hint - tmpOfs;
        }
        lastOfs++;
        while (lastOfs < ofs) {
            int m = lastOfs + ((ofs - lastOfs) >>> 1);
            if (ltKey(a, base + m, key)) {
                lastOfs = m + 1;
            } else {
                ofs = m;
            }
        }
        return ofs;
    }

    private static int gallopLeft(int key, int[] a, int base, int len, int hint) {
        int lastOfs = 0;
        int ofs = 1;
        if (ltKey(a, base + hint, key)) {
            int maxOfs = len - hint;
            while (ofs < maxOfs && ltKey(a, base + hint + ofs, key)) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            lastOfs += hint;
            ofs += hint;
        } else {
            int maxOfs = hint + 1;
            while (ofs < maxOfs && !ltKey(a, base + hint - ofs, key)) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            int tmpOfs = lastOfs;
            lastOfs = hint - ofs;
            ofs = hint - tmpOfs;
        }
        lastOfs++;
        while (lastOfs < ofs) {
            int m = lastOfs + ((ofs - lastOfs) >>> 1);
            if (ltKey(a, base + m, key)) {
                lastOfs = m + 1;
            } else {
                ofs = m;
            }
        }
        return ofs;
    }

    private static boolean gtCross(int[] a, int i, int[] b, int j) {
        return ltCross(b, j, a, i);
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
        tim(a, from, to);
    }

    private static boolean gt(long[] a, int i, int j) {
        return a[i] > a[j];
    }

    private static boolean gtKey(long[] a, int i, long key) {
        return a[i] > key;
    }

    private static boolean ltKey(long[] a, int i, long key) {
        return a[i] < key;
    }

    private static boolean lt(long[] a, int i, int j) {
        return a[i] < a[j];
    }

    private static boolean ltCross(long[] a, int i, long[] b, int j) {
        return a[i] < b[j];
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

    private static void tim(long[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        if (n < TIM_MIN_MERGE) {
            insertion(a, from, to);
            return;
        }
        int minRun = minRunLength(n);
        int capacity = stackCapacity(n);
        int[] runBase = new int[capacity];
        int[] runLen = new int[capacity];
        long[] tmp = new long[n];
        int stackSize = 0;
        int lo = from;
        int remaining = n;
        while (remaining > 0) {
            int run = countRunAndMakeAscending(a, lo, to);
            if (run < minRun) {
                int force = Math.min(minRun, remaining);
                binarySort(a, lo, lo + force, lo + run);
                run = force;
            }
            runBase[stackSize] = lo;
            runLen[stackSize] = run;
            stackSize++;
            stackSize = mergeCollapse(a, tmp, runBase, runLen, stackSize);
            lo += run;
            remaining -= run;
        }
        mergeForceCollapse(a, tmp, runBase, runLen, stackSize);
    }

    private static int countRunAndMakeAscending(long[] a, int lo, int hi) {
        int runHi = lo + 1;
        if (runHi == hi) {
            return 1;
        }
        if (lt(a, runHi, lo)) {
            while (runHi < hi && lt(a, runHi, runHi - 1)) {
                runHi++;
            }
            reverseRange(a, lo, runHi - 1);
        } else {
            while (runHi < hi && !lt(a, runHi, runHi - 1)) {
                runHi++;
            }
        }
        return runHi - lo;
    }

    private static void reverseRange(long[] a, int lo, int hi) {
        while (lo < hi) {
            swap(a, lo, hi);
            lo++;
            hi--;
        }
    }

    private static void binarySort(long[] a, int lo, int hi, int start) {
        if (start == lo) {
            start++;
        }
        for (; start < hi; start++) {
            long pivot = a[start];
            int left = lo;
            int right = start;
            while (left < right) {
                int mid = (left + right) >>> 1;
                if (gtKey(a, mid, pivot)) {
                    right = mid;
                } else {
                    left = mid + 1;
                }
            }
            int n = start - left;
            System.arraycopy(a, left, a, left + 1, n);
            a[left] = pivot;
        }
    }

    private static int mergeCollapse(long[] a, long[] tmp, int[] runBase, int[] runLen, int stackSize) {
        while (stackSize > 1) {
            int n = stackSize - 2;
            if (n > 0 && runLen[n - 1] <= runLen[n] + runLen[n + 1]) {
                if (runLen[n - 1] < runLen[n + 1]) {
                    n--;
                }
                stackSize = mergeAt(a, tmp, runBase, runLen, stackSize, n);
            } else if (runLen[n] <= runLen[n + 1]) {
                stackSize = mergeAt(a, tmp, runBase, runLen, stackSize, n);
            } else {
                break;
            }
        }
        return stackSize;
    }

    private static void mergeForceCollapse(long[] a, long[] tmp, int[] runBase, int[] runLen, int stackSize) {
        while (stackSize > 1) {
            int n = stackSize - 2;
            if (n > 0 && runLen[n - 1] < runLen[n + 1]) {
                n--;
            }
            stackSize = mergeAt(a, tmp, runBase, runLen, stackSize, n);
        }
    }

    private static int mergeAt(long[] a, long[] tmp, int[] runBase, int[] runLen, int stackSize, int i) {
        int base1 = runBase[i];
        int len1 = runLen[i];
        int base2 = runBase[i + 1];
        int len2 = runLen[i + 1];
        runLen[i] = len1 + len2;
        if (i == stackSize - 3) {
            runBase[i + 1] = runBase[i + 2];
            runLen[i + 1] = runLen[i + 2];
        }
        int k = gallopRight(a[base2], a, base1, len1, 0);
        base1 += k;
        len1 -= k;
        if (len1 == 0) {
            return stackSize - 1;
        }
        len2 = gallopLeft(a[base1 + len1 - 1], a, base2, len2, len2 - 1);
        if (len2 == 0) {
            return stackSize - 1;
        }
        if (len1 <= len2) {
            System.arraycopy(a, base1, tmp, 0, len1);
            mergeLo(a, tmp, base1, len1, base2, len2);
        } else {
            System.arraycopy(a, base2, tmp, 0, len2);
            mergeHi(a, tmp, base1, len1, base2, len2);
        }
        return stackSize - 1;
    }

    private static void mergeLo(long[] a, long[] tmp, int base1, int len1, int base2, int len2) {
        int i1 = 0;
        int i2 = base2;
        int k = base1;
        int end2 = base2 + len2;
        int minGallop = MIN_GALLOP;
        while (true) {
            int count1 = 0;
            int count2 = 0;
            while (i1 < len1 && i2 < end2 && count1 < minGallop && count2 < minGallop) {
                if (ltCross(a, i2, tmp, i1)) {
                    a[k++] = a[i2++];
                    count2++;
                    count1 = 0;
                } else {
                    a[k++] = tmp[i1++];
                    count1++;
                    count2 = 0;
                }
            }
            if (i1 == len1 || i2 == end2) {
                break;
            }
            int jumped;
            if (count1 >= minGallop) {
                jumped = gallopRight(a[i2], tmp, i1, len1 - i1, 0);
                System.arraycopy(tmp, i1, a, k, jumped);
                i1 += jumped;
                k += jumped;
            } else {
                jumped = gallopLeft(tmp[i1], a, i2, end2 - i2, 0);
                System.arraycopy(a, i2, a, k, jumped);
                i2 += jumped;
                k += jumped;
            }
            if (i1 == len1 || i2 == end2) {
                break;
            }
            minGallop++;
        }
        while (i1 < len1) {
            a[k++] = tmp[i1++];
        }
    }

    private static void mergeHi(long[] a, long[] tmp, int base1, int len1, int base2, int len2) {
        int i1 = base1 + len1 - 1;
        int i2 = len2 - 1;
        int k = base2 + len2 - 1;
        int start1 = base1;
        int minGallop = MIN_GALLOP;
        while (true) {
            int count1 = 0;
            int count2 = 0;
            while (i1 >= start1 && i2 >= 0 && count1 < minGallop && count2 < minGallop) {
                if (gtCross(tmp, i2, a, i1)) {
                    a[k--] = tmp[i2--];
                    count2++;
                    count1 = 0;
                } else {
                    a[k--] = a[i1--];
                    count1++;
                    count2 = 0;
                }
            }
            if (i1 < start1 || i2 < 0) {
                break;
            }
            int jumped;
            if (count1 >= minGallop) {
                jumped = i1 - start1 + 1 - gallopRight(tmp[i2], a, start1, i1 - start1 + 1, i1 - start1);
                while (jumped-- > 0) {
                    a[k--] = a[i1--];
                }
            } else {
                jumped = i2 + 1 - gallopLeft(a[i1], tmp, 0, i2 + 1, i2);
                while (jumped-- > 0) {
                    a[k--] = tmp[i2--];
                }
            }
            if (i1 < start1 || i2 < 0) {
                break;
            }
            minGallop++;
        }
        while (i2 >= 0) {
            a[k--] = tmp[i2--];
        }
    }

    private static int gallopRight(long key, long[] a, int base, int len, int hint) {
        int lastOfs = 0;
        int ofs = 1;
        if (ltKey(a, base + hint, key)) {
            int maxOfs = len - hint;
            while (ofs < maxOfs && ltKey(a, base + hint + ofs, key)) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            lastOfs += hint;
            ofs += hint;
        } else {
            int maxOfs = hint + 1;
            while (ofs < maxOfs && !ltKey(a, base + hint - ofs, key)) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            int tmpOfs = lastOfs;
            lastOfs = hint - ofs;
            ofs = hint - tmpOfs;
        }
        lastOfs++;
        while (lastOfs < ofs) {
            int m = lastOfs + ((ofs - lastOfs) >>> 1);
            if (ltKey(a, base + m, key)) {
                lastOfs = m + 1;
            } else {
                ofs = m;
            }
        }
        return ofs;
    }

    private static int gallopLeft(long key, long[] a, int base, int len, int hint) {
        int lastOfs = 0;
        int ofs = 1;
        if (ltKey(a, base + hint, key)) {
            int maxOfs = len - hint;
            while (ofs < maxOfs && ltKey(a, base + hint + ofs, key)) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            lastOfs += hint;
            ofs += hint;
        } else {
            int maxOfs = hint + 1;
            while (ofs < maxOfs && !ltKey(a, base + hint - ofs, key)) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            int tmpOfs = lastOfs;
            lastOfs = hint - ofs;
            ofs = hint - tmpOfs;
        }
        lastOfs++;
        while (lastOfs < ofs) {
            int m = lastOfs + ((ofs - lastOfs) >>> 1);
            if (ltKey(a, base + m, key)) {
                lastOfs = m + 1;
            } else {
                ofs = m;
            }
        }
        return ofs;
    }

    private static boolean gtCross(long[] a, int i, long[] b, int j) {
        return ltCross(b, j, a, i);
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
        tim(a, from, to);
    }

    private static boolean gt(float[] a, int i, int j) {
        return Float.compare(a[i], a[j]) > 0;
    }

    private static boolean gtKey(float[] a, int i, float key) {
        return Float.compare(a[i], key) > 0;
    }

    private static boolean ltKey(float[] a, int i, float key) {
        return Float.compare(a[i], key) < 0;
    }

    private static boolean lt(float[] a, int i, int j) {
        return Float.compare(a[i], a[j]) < 0;
    }

    private static boolean ltCross(float[] a, int i, float[] b, int j) {
        return Float.compare(a[i], b[j]) < 0;
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

    private static void tim(float[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        if (n < TIM_MIN_MERGE) {
            insertion(a, from, to);
            return;
        }
        int minRun = minRunLength(n);
        int capacity = stackCapacity(n);
        int[] runBase = new int[capacity];
        int[] runLen = new int[capacity];
        float[] tmp = new float[n];
        int stackSize = 0;
        int lo = from;
        int remaining = n;
        while (remaining > 0) {
            int run = countRunAndMakeAscending(a, lo, to);
            if (run < minRun) {
                int force = Math.min(minRun, remaining);
                binarySort(a, lo, lo + force, lo + run);
                run = force;
            }
            runBase[stackSize] = lo;
            runLen[stackSize] = run;
            stackSize++;
            stackSize = mergeCollapse(a, tmp, runBase, runLen, stackSize);
            lo += run;
            remaining -= run;
        }
        mergeForceCollapse(a, tmp, runBase, runLen, stackSize);
    }

    private static int countRunAndMakeAscending(float[] a, int lo, int hi) {
        int runHi = lo + 1;
        if (runHi == hi) {
            return 1;
        }
        if (lt(a, runHi, lo)) {
            while (runHi < hi && lt(a, runHi, runHi - 1)) {
                runHi++;
            }
            reverseRange(a, lo, runHi - 1);
        } else {
            while (runHi < hi && !lt(a, runHi, runHi - 1)) {
                runHi++;
            }
        }
        return runHi - lo;
    }

    private static void reverseRange(float[] a, int lo, int hi) {
        while (lo < hi) {
            swap(a, lo, hi);
            lo++;
            hi--;
        }
    }

    private static void binarySort(float[] a, int lo, int hi, int start) {
        if (start == lo) {
            start++;
        }
        for (; start < hi; start++) {
            float pivot = a[start];
            int left = lo;
            int right = start;
            while (left < right) {
                int mid = (left + right) >>> 1;
                if (gtKey(a, mid, pivot)) {
                    right = mid;
                } else {
                    left = mid + 1;
                }
            }
            int n = start - left;
            System.arraycopy(a, left, a, left + 1, n);
            a[left] = pivot;
        }
    }

    private static int mergeCollapse(float[] a, float[] tmp, int[] runBase, int[] runLen, int stackSize) {
        while (stackSize > 1) {
            int n = stackSize - 2;
            if (n > 0 && runLen[n - 1] <= runLen[n] + runLen[n + 1]) {
                if (runLen[n - 1] < runLen[n + 1]) {
                    n--;
                }
                stackSize = mergeAt(a, tmp, runBase, runLen, stackSize, n);
            } else if (runLen[n] <= runLen[n + 1]) {
                stackSize = mergeAt(a, tmp, runBase, runLen, stackSize, n);
            } else {
                break;
            }
        }
        return stackSize;
    }

    private static void mergeForceCollapse(float[] a, float[] tmp, int[] runBase, int[] runLen, int stackSize) {
        while (stackSize > 1) {
            int n = stackSize - 2;
            if (n > 0 && runLen[n - 1] < runLen[n + 1]) {
                n--;
            }
            stackSize = mergeAt(a, tmp, runBase, runLen, stackSize, n);
        }
    }

    private static int mergeAt(float[] a, float[] tmp, int[] runBase, int[] runLen, int stackSize, int i) {
        int base1 = runBase[i];
        int len1 = runLen[i];
        int base2 = runBase[i + 1];
        int len2 = runLen[i + 1];
        runLen[i] = len1 + len2;
        if (i == stackSize - 3) {
            runBase[i + 1] = runBase[i + 2];
            runLen[i + 1] = runLen[i + 2];
        }
        int k = gallopRight(a[base2], a, base1, len1, 0);
        base1 += k;
        len1 -= k;
        if (len1 == 0) {
            return stackSize - 1;
        }
        len2 = gallopLeft(a[base1 + len1 - 1], a, base2, len2, len2 - 1);
        if (len2 == 0) {
            return stackSize - 1;
        }
        if (len1 <= len2) {
            System.arraycopy(a, base1, tmp, 0, len1);
            mergeLo(a, tmp, base1, len1, base2, len2);
        } else {
            System.arraycopy(a, base2, tmp, 0, len2);
            mergeHi(a, tmp, base1, len1, base2, len2);
        }
        return stackSize - 1;
    }

    private static void mergeLo(float[] a, float[] tmp, int base1, int len1, int base2, int len2) {
        int i1 = 0;
        int i2 = base2;
        int k = base1;
        int end2 = base2 + len2;
        int minGallop = MIN_GALLOP;
        while (true) {
            int count1 = 0;
            int count2 = 0;
            while (i1 < len1 && i2 < end2 && count1 < minGallop && count2 < minGallop) {
                if (ltCross(a, i2, tmp, i1)) {
                    a[k++] = a[i2++];
                    count2++;
                    count1 = 0;
                } else {
                    a[k++] = tmp[i1++];
                    count1++;
                    count2 = 0;
                }
            }
            if (i1 == len1 || i2 == end2) {
                break;
            }
            int jumped;
            if (count1 >= minGallop) {
                jumped = gallopRight(a[i2], tmp, i1, len1 - i1, 0);
                System.arraycopy(tmp, i1, a, k, jumped);
                i1 += jumped;
                k += jumped;
            } else {
                jumped = gallopLeft(tmp[i1], a, i2, end2 - i2, 0);
                System.arraycopy(a, i2, a, k, jumped);
                i2 += jumped;
                k += jumped;
            }
            if (i1 == len1 || i2 == end2) {
                break;
            }
            minGallop++;
        }
        while (i1 < len1) {
            a[k++] = tmp[i1++];
        }
    }

    private static void mergeHi(float[] a, float[] tmp, int base1, int len1, int base2, int len2) {
        int i1 = base1 + len1 - 1;
        int i2 = len2 - 1;
        int k = base2 + len2 - 1;
        int start1 = base1;
        int minGallop = MIN_GALLOP;
        while (true) {
            int count1 = 0;
            int count2 = 0;
            while (i1 >= start1 && i2 >= 0 && count1 < minGallop && count2 < minGallop) {
                if (gtCross(tmp, i2, a, i1)) {
                    a[k--] = tmp[i2--];
                    count2++;
                    count1 = 0;
                } else {
                    a[k--] = a[i1--];
                    count1++;
                    count2 = 0;
                }
            }
            if (i1 < start1 || i2 < 0) {
                break;
            }
            int jumped;
            if (count1 >= minGallop) {
                jumped = i1 - start1 + 1 - gallopRight(tmp[i2], a, start1, i1 - start1 + 1, i1 - start1);
                while (jumped-- > 0) {
                    a[k--] = a[i1--];
                }
            } else {
                jumped = i2 + 1 - gallopLeft(a[i1], tmp, 0, i2 + 1, i2);
                while (jumped-- > 0) {
                    a[k--] = tmp[i2--];
                }
            }
            if (i1 < start1 || i2 < 0) {
                break;
            }
            minGallop++;
        }
        while (i2 >= 0) {
            a[k--] = tmp[i2--];
        }
    }

    private static int gallopRight(float key, float[] a, int base, int len, int hint) {
        int lastOfs = 0;
        int ofs = 1;
        if (ltKey(a, base + hint, key)) {
            int maxOfs = len - hint;
            while (ofs < maxOfs && ltKey(a, base + hint + ofs, key)) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            lastOfs += hint;
            ofs += hint;
        } else {
            int maxOfs = hint + 1;
            while (ofs < maxOfs && !ltKey(a, base + hint - ofs, key)) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            int tmpOfs = lastOfs;
            lastOfs = hint - ofs;
            ofs = hint - tmpOfs;
        }
        lastOfs++;
        while (lastOfs < ofs) {
            int m = lastOfs + ((ofs - lastOfs) >>> 1);
            if (ltKey(a, base + m, key)) {
                lastOfs = m + 1;
            } else {
                ofs = m;
            }
        }
        return ofs;
    }

    private static int gallopLeft(float key, float[] a, int base, int len, int hint) {
        int lastOfs = 0;
        int ofs = 1;
        if (ltKey(a, base + hint, key)) {
            int maxOfs = len - hint;
            while (ofs < maxOfs && ltKey(a, base + hint + ofs, key)) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            lastOfs += hint;
            ofs += hint;
        } else {
            int maxOfs = hint + 1;
            while (ofs < maxOfs && !ltKey(a, base + hint - ofs, key)) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            int tmpOfs = lastOfs;
            lastOfs = hint - ofs;
            ofs = hint - tmpOfs;
        }
        lastOfs++;
        while (lastOfs < ofs) {
            int m = lastOfs + ((ofs - lastOfs) >>> 1);
            if (ltKey(a, base + m, key)) {
                lastOfs = m + 1;
            } else {
                ofs = m;
            }
        }
        return ofs;
    }

    private static boolean gtCross(float[] a, int i, float[] b, int j) {
        return ltCross(b, j, a, i);
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
        tim(a, from, to);
    }

    private static boolean gt(double[] a, int i, int j) {
        return Double.compare(a[i], a[j]) > 0;
    }

    private static boolean gtKey(double[] a, int i, double key) {
        return Double.compare(a[i], key) > 0;
    }

    private static boolean ltKey(double[] a, int i, double key) {
        return Double.compare(a[i], key) < 0;
    }

    private static boolean lt(double[] a, int i, int j) {
        return Double.compare(a[i], a[j]) < 0;
    }

    private static boolean ltCross(double[] a, int i, double[] b, int j) {
        return Double.compare(a[i], b[j]) < 0;
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

    private static void tim(double[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        if (n < TIM_MIN_MERGE) {
            insertion(a, from, to);
            return;
        }
        int minRun = minRunLength(n);
        int capacity = stackCapacity(n);
        int[] runBase = new int[capacity];
        int[] runLen = new int[capacity];
        double[] tmp = new double[n];
        int stackSize = 0;
        int lo = from;
        int remaining = n;
        while (remaining > 0) {
            int run = countRunAndMakeAscending(a, lo, to);
            if (run < minRun) {
                int force = Math.min(minRun, remaining);
                binarySort(a, lo, lo + force, lo + run);
                run = force;
            }
            runBase[stackSize] = lo;
            runLen[stackSize] = run;
            stackSize++;
            stackSize = mergeCollapse(a, tmp, runBase, runLen, stackSize);
            lo += run;
            remaining -= run;
        }
        mergeForceCollapse(a, tmp, runBase, runLen, stackSize);
    }

    private static int countRunAndMakeAscending(double[] a, int lo, int hi) {
        int runHi = lo + 1;
        if (runHi == hi) {
            return 1;
        }
        if (lt(a, runHi, lo)) {
            while (runHi < hi && lt(a, runHi, runHi - 1)) {
                runHi++;
            }
            reverseRange(a, lo, runHi - 1);
        } else {
            while (runHi < hi && !lt(a, runHi, runHi - 1)) {
                runHi++;
            }
        }
        return runHi - lo;
    }

    private static void reverseRange(double[] a, int lo, int hi) {
        while (lo < hi) {
            swap(a, lo, hi);
            lo++;
            hi--;
        }
    }

    private static void binarySort(double[] a, int lo, int hi, int start) {
        if (start == lo) {
            start++;
        }
        for (; start < hi; start++) {
            double pivot = a[start];
            int left = lo;
            int right = start;
            while (left < right) {
                int mid = (left + right) >>> 1;
                if (gtKey(a, mid, pivot)) {
                    right = mid;
                } else {
                    left = mid + 1;
                }
            }
            int n = start - left;
            System.arraycopy(a, left, a, left + 1, n);
            a[left] = pivot;
        }
    }

    private static int mergeCollapse(double[] a, double[] tmp, int[] runBase, int[] runLen, int stackSize) {
        while (stackSize > 1) {
            int n = stackSize - 2;
            if (n > 0 && runLen[n - 1] <= runLen[n] + runLen[n + 1]) {
                if (runLen[n - 1] < runLen[n + 1]) {
                    n--;
                }
                stackSize = mergeAt(a, tmp, runBase, runLen, stackSize, n);
            } else if (runLen[n] <= runLen[n + 1]) {
                stackSize = mergeAt(a, tmp, runBase, runLen, stackSize, n);
            } else {
                break;
            }
        }
        return stackSize;
    }

    private static void mergeForceCollapse(double[] a, double[] tmp, int[] runBase, int[] runLen, int stackSize) {
        while (stackSize > 1) {
            int n = stackSize - 2;
            if (n > 0 && runLen[n - 1] < runLen[n + 1]) {
                n--;
            }
            stackSize = mergeAt(a, tmp, runBase, runLen, stackSize, n);
        }
    }

    private static int mergeAt(double[] a, double[] tmp, int[] runBase, int[] runLen, int stackSize, int i) {
        int base1 = runBase[i];
        int len1 = runLen[i];
        int base2 = runBase[i + 1];
        int len2 = runLen[i + 1];
        runLen[i] = len1 + len2;
        if (i == stackSize - 3) {
            runBase[i + 1] = runBase[i + 2];
            runLen[i + 1] = runLen[i + 2];
        }
        int k = gallopRight(a[base2], a, base1, len1, 0);
        base1 += k;
        len1 -= k;
        if (len1 == 0) {
            return stackSize - 1;
        }
        len2 = gallopLeft(a[base1 + len1 - 1], a, base2, len2, len2 - 1);
        if (len2 == 0) {
            return stackSize - 1;
        }
        if (len1 <= len2) {
            System.arraycopy(a, base1, tmp, 0, len1);
            mergeLo(a, tmp, base1, len1, base2, len2);
        } else {
            System.arraycopy(a, base2, tmp, 0, len2);
            mergeHi(a, tmp, base1, len1, base2, len2);
        }
        return stackSize - 1;
    }

    private static void mergeLo(double[] a, double[] tmp, int base1, int len1, int base2, int len2) {
        int i1 = 0;
        int i2 = base2;
        int k = base1;
        int end2 = base2 + len2;
        int minGallop = MIN_GALLOP;
        while (true) {
            int count1 = 0;
            int count2 = 0;
            while (i1 < len1 && i2 < end2 && count1 < minGallop && count2 < minGallop) {
                if (ltCross(a, i2, tmp, i1)) {
                    a[k++] = a[i2++];
                    count2++;
                    count1 = 0;
                } else {
                    a[k++] = tmp[i1++];
                    count1++;
                    count2 = 0;
                }
            }
            if (i1 == len1 || i2 == end2) {
                break;
            }
            int jumped;
            if (count1 >= minGallop) {
                jumped = gallopRight(a[i2], tmp, i1, len1 - i1, 0);
                System.arraycopy(tmp, i1, a, k, jumped);
                i1 += jumped;
                k += jumped;
            } else {
                jumped = gallopLeft(tmp[i1], a, i2, end2 - i2, 0);
                System.arraycopy(a, i2, a, k, jumped);
                i2 += jumped;
                k += jumped;
            }
            if (i1 == len1 || i2 == end2) {
                break;
            }
            minGallop++;
        }
        while (i1 < len1) {
            a[k++] = tmp[i1++];
        }
    }

    private static void mergeHi(double[] a, double[] tmp, int base1, int len1, int base2, int len2) {
        int i1 = base1 + len1 - 1;
        int i2 = len2 - 1;
        int k = base2 + len2 - 1;
        int start1 = base1;
        int minGallop = MIN_GALLOP;
        while (true) {
            int count1 = 0;
            int count2 = 0;
            while (i1 >= start1 && i2 >= 0 && count1 < minGallop && count2 < minGallop) {
                if (gtCross(tmp, i2, a, i1)) {
                    a[k--] = tmp[i2--];
                    count2++;
                    count1 = 0;
                } else {
                    a[k--] = a[i1--];
                    count1++;
                    count2 = 0;
                }
            }
            if (i1 < start1 || i2 < 0) {
                break;
            }
            int jumped;
            if (count1 >= minGallop) {
                jumped = i1 - start1 + 1 - gallopRight(tmp[i2], a, start1, i1 - start1 + 1, i1 - start1);
                while (jumped-- > 0) {
                    a[k--] = a[i1--];
                }
            } else {
                jumped = i2 + 1 - gallopLeft(a[i1], tmp, 0, i2 + 1, i2);
                while (jumped-- > 0) {
                    a[k--] = tmp[i2--];
                }
            }
            if (i1 < start1 || i2 < 0) {
                break;
            }
            minGallop++;
        }
        while (i2 >= 0) {
            a[k--] = tmp[i2--];
        }
    }

    private static int gallopRight(double key, double[] a, int base, int len, int hint) {
        int lastOfs = 0;
        int ofs = 1;
        if (ltKey(a, base + hint, key)) {
            int maxOfs = len - hint;
            while (ofs < maxOfs && ltKey(a, base + hint + ofs, key)) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            lastOfs += hint;
            ofs += hint;
        } else {
            int maxOfs = hint + 1;
            while (ofs < maxOfs && !ltKey(a, base + hint - ofs, key)) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            int tmpOfs = lastOfs;
            lastOfs = hint - ofs;
            ofs = hint - tmpOfs;
        }
        lastOfs++;
        while (lastOfs < ofs) {
            int m = lastOfs + ((ofs - lastOfs) >>> 1);
            if (ltKey(a, base + m, key)) {
                lastOfs = m + 1;
            } else {
                ofs = m;
            }
        }
        return ofs;
    }

    private static int gallopLeft(double key, double[] a, int base, int len, int hint) {
        int lastOfs = 0;
        int ofs = 1;
        if (ltKey(a, base + hint, key)) {
            int maxOfs = len - hint;
            while (ofs < maxOfs && ltKey(a, base + hint + ofs, key)) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            lastOfs += hint;
            ofs += hint;
        } else {
            int maxOfs = hint + 1;
            while (ofs < maxOfs && !ltKey(a, base + hint - ofs, key)) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            int tmpOfs = lastOfs;
            lastOfs = hint - ofs;
            ofs = hint - tmpOfs;
        }
        lastOfs++;
        while (lastOfs < ofs) {
            int m = lastOfs + ((ofs - lastOfs) >>> 1);
            if (ltKey(a, base + m, key)) {
                lastOfs = m + 1;
            } else {
                ofs = m;
            }
        }
        return ofs;
    }

    private static boolean gtCross(double[] a, int i, double[] b, int j) {
        return ltCross(b, j, a, i);
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
        tim(a, from, to);
    }

    private static boolean gt(char[] a, int i, int j) {
        return a[i] > a[j];
    }

    private static boolean gtKey(char[] a, int i, char key) {
        return a[i] > key;
    }

    private static boolean ltKey(char[] a, int i, char key) {
        return a[i] < key;
    }

    private static boolean lt(char[] a, int i, int j) {
        return a[i] < a[j];
    }

    private static boolean ltCross(char[] a, int i, char[] b, int j) {
        return a[i] < b[j];
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

    private static void tim(char[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        if (n < TIM_MIN_MERGE) {
            insertion(a, from, to);
            return;
        }
        int minRun = minRunLength(n);
        int capacity = stackCapacity(n);
        int[] runBase = new int[capacity];
        int[] runLen = new int[capacity];
        char[] tmp = new char[n];
        int stackSize = 0;
        int lo = from;
        int remaining = n;
        while (remaining > 0) {
            int run = countRunAndMakeAscending(a, lo, to);
            if (run < minRun) {
                int force = Math.min(minRun, remaining);
                binarySort(a, lo, lo + force, lo + run);
                run = force;
            }
            runBase[stackSize] = lo;
            runLen[stackSize] = run;
            stackSize++;
            stackSize = mergeCollapse(a, tmp, runBase, runLen, stackSize);
            lo += run;
            remaining -= run;
        }
        mergeForceCollapse(a, tmp, runBase, runLen, stackSize);
    }

    private static int countRunAndMakeAscending(char[] a, int lo, int hi) {
        int runHi = lo + 1;
        if (runHi == hi) {
            return 1;
        }
        if (lt(a, runHi, lo)) {
            while (runHi < hi && lt(a, runHi, runHi - 1)) {
                runHi++;
            }
            reverseRange(a, lo, runHi - 1);
        } else {
            while (runHi < hi && !lt(a, runHi, runHi - 1)) {
                runHi++;
            }
        }
        return runHi - lo;
    }

    private static void reverseRange(char[] a, int lo, int hi) {
        while (lo < hi) {
            swap(a, lo, hi);
            lo++;
            hi--;
        }
    }

    private static void binarySort(char[] a, int lo, int hi, int start) {
        if (start == lo) {
            start++;
        }
        for (; start < hi; start++) {
            char pivot = a[start];
            int left = lo;
            int right = start;
            while (left < right) {
                int mid = (left + right) >>> 1;
                if (gtKey(a, mid, pivot)) {
                    right = mid;
                } else {
                    left = mid + 1;
                }
            }
            int n = start - left;
            System.arraycopy(a, left, a, left + 1, n);
            a[left] = pivot;
        }
    }

    private static int mergeCollapse(char[] a, char[] tmp, int[] runBase, int[] runLen, int stackSize) {
        while (stackSize > 1) {
            int n = stackSize - 2;
            if (n > 0 && runLen[n - 1] <= runLen[n] + runLen[n + 1]) {
                if (runLen[n - 1] < runLen[n + 1]) {
                    n--;
                }
                stackSize = mergeAt(a, tmp, runBase, runLen, stackSize, n);
            } else if (runLen[n] <= runLen[n + 1]) {
                stackSize = mergeAt(a, tmp, runBase, runLen, stackSize, n);
            } else {
                break;
            }
        }
        return stackSize;
    }

    private static void mergeForceCollapse(char[] a, char[] tmp, int[] runBase, int[] runLen, int stackSize) {
        while (stackSize > 1) {
            int n = stackSize - 2;
            if (n > 0 && runLen[n - 1] < runLen[n + 1]) {
                n--;
            }
            stackSize = mergeAt(a, tmp, runBase, runLen, stackSize, n);
        }
    }

    private static int mergeAt(char[] a, char[] tmp, int[] runBase, int[] runLen, int stackSize, int i) {
        int base1 = runBase[i];
        int len1 = runLen[i];
        int base2 = runBase[i + 1];
        int len2 = runLen[i + 1];
        runLen[i] = len1 + len2;
        if (i == stackSize - 3) {
            runBase[i + 1] = runBase[i + 2];
            runLen[i + 1] = runLen[i + 2];
        }
        int k = gallopRight(a[base2], a, base1, len1, 0);
        base1 += k;
        len1 -= k;
        if (len1 == 0) {
            return stackSize - 1;
        }
        len2 = gallopLeft(a[base1 + len1 - 1], a, base2, len2, len2 - 1);
        if (len2 == 0) {
            return stackSize - 1;
        }
        if (len1 <= len2) {
            System.arraycopy(a, base1, tmp, 0, len1);
            mergeLo(a, tmp, base1, len1, base2, len2);
        } else {
            System.arraycopy(a, base2, tmp, 0, len2);
            mergeHi(a, tmp, base1, len1, base2, len2);
        }
        return stackSize - 1;
    }

    private static void mergeLo(char[] a, char[] tmp, int base1, int len1, int base2, int len2) {
        int i1 = 0;
        int i2 = base2;
        int k = base1;
        int end2 = base2 + len2;
        int minGallop = MIN_GALLOP;
        while (true) {
            int count1 = 0;
            int count2 = 0;
            while (i1 < len1 && i2 < end2 && count1 < minGallop && count2 < minGallop) {
                if (ltCross(a, i2, tmp, i1)) {
                    a[k++] = a[i2++];
                    count2++;
                    count1 = 0;
                } else {
                    a[k++] = tmp[i1++];
                    count1++;
                    count2 = 0;
                }
            }
            if (i1 == len1 || i2 == end2) {
                break;
            }
            int jumped;
            if (count1 >= minGallop) {
                jumped = gallopRight(a[i2], tmp, i1, len1 - i1, 0);
                System.arraycopy(tmp, i1, a, k, jumped);
                i1 += jumped;
                k += jumped;
            } else {
                jumped = gallopLeft(tmp[i1], a, i2, end2 - i2, 0);
                System.arraycopy(a, i2, a, k, jumped);
                i2 += jumped;
                k += jumped;
            }
            if (i1 == len1 || i2 == end2) {
                break;
            }
            minGallop++;
        }
        while (i1 < len1) {
            a[k++] = tmp[i1++];
        }
    }

    private static void mergeHi(char[] a, char[] tmp, int base1, int len1, int base2, int len2) {
        int i1 = base1 + len1 - 1;
        int i2 = len2 - 1;
        int k = base2 + len2 - 1;
        int start1 = base1;
        int minGallop = MIN_GALLOP;
        while (true) {
            int count1 = 0;
            int count2 = 0;
            while (i1 >= start1 && i2 >= 0 && count1 < minGallop && count2 < minGallop) {
                if (gtCross(tmp, i2, a, i1)) {
                    a[k--] = tmp[i2--];
                    count2++;
                    count1 = 0;
                } else {
                    a[k--] = a[i1--];
                    count1++;
                    count2 = 0;
                }
            }
            if (i1 < start1 || i2 < 0) {
                break;
            }
            int jumped;
            if (count1 >= minGallop) {
                jumped = i1 - start1 + 1 - gallopRight(tmp[i2], a, start1, i1 - start1 + 1, i1 - start1);
                while (jumped-- > 0) {
                    a[k--] = a[i1--];
                }
            } else {
                jumped = i2 + 1 - gallopLeft(a[i1], tmp, 0, i2 + 1, i2);
                while (jumped-- > 0) {
                    a[k--] = tmp[i2--];
                }
            }
            if (i1 < start1 || i2 < 0) {
                break;
            }
            minGallop++;
        }
        while (i2 >= 0) {
            a[k--] = tmp[i2--];
        }
    }

    private static int gallopRight(char key, char[] a, int base, int len, int hint) {
        int lastOfs = 0;
        int ofs = 1;
        if (ltKey(a, base + hint, key)) {
            int maxOfs = len - hint;
            while (ofs < maxOfs && ltKey(a, base + hint + ofs, key)) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            lastOfs += hint;
            ofs += hint;
        } else {
            int maxOfs = hint + 1;
            while (ofs < maxOfs && !ltKey(a, base + hint - ofs, key)) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            int tmpOfs = lastOfs;
            lastOfs = hint - ofs;
            ofs = hint - tmpOfs;
        }
        lastOfs++;
        while (lastOfs < ofs) {
            int m = lastOfs + ((ofs - lastOfs) >>> 1);
            if (ltKey(a, base + m, key)) {
                lastOfs = m + 1;
            } else {
                ofs = m;
            }
        }
        return ofs;
    }

    private static int gallopLeft(char key, char[] a, int base, int len, int hint) {
        int lastOfs = 0;
        int ofs = 1;
        if (ltKey(a, base + hint, key)) {
            int maxOfs = len - hint;
            while (ofs < maxOfs && ltKey(a, base + hint + ofs, key)) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            lastOfs += hint;
            ofs += hint;
        } else {
            int maxOfs = hint + 1;
            while (ofs < maxOfs && !ltKey(a, base + hint - ofs, key)) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            int tmpOfs = lastOfs;
            lastOfs = hint - ofs;
            ofs = hint - tmpOfs;
        }
        lastOfs++;
        while (lastOfs < ofs) {
            int m = lastOfs + ((ofs - lastOfs) >>> 1);
            if (ltKey(a, base + m, key)) {
                lastOfs = m + 1;
            } else {
                ofs = m;
            }
        }
        return ofs;
    }

    private static boolean gtCross(char[] a, int i, char[] b, int j) {
        return ltCross(b, j, a, i);
    }
}
