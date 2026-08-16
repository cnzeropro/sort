package org.zero.sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

/**
 * 对象数组的排序算法实现（21 种算法中的比较类算法，共 17 种）
 * <p>
 * 约定：
 * <ul>
 *   <li>全部方法操作 [from, to) 左闭右开区间，入参由公共门面 {@link Sort} 完成校验；</li>
 *   <li>比较一律通过 {@link Comparator}（门面的 Comparable 版本传
 *       {@link Comparator#naturalOrder()}）；</li>
 *   <li>一律使用严格比较（&gt; / &lt;），相等元素不交换，保证稳定算法的稳定性成立；</li>
 *   <li>插入排序采用移位式，作为快速/归并/Tim 排序的小数组底层实现。</li>
 * </ul>
 * 各原始类型的特化实现见同包的 {@code XxxSorts}（byte/short/int/long/float/double/char）。
 *
 * @author Zero
 */
final class GenericSorts {

    /** 插入排序阈值：小于该长度的区间直接使用插入排序 */
    static final int INSERTION_THRESHOLD = 16;

    /** Tim 排序的最小 run 长度基准 */
    private static final int TIM_MIN_MERGE = 32;

    private GenericSorts() {
    }

    // ==================== 基础助手 ====================

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

    /** 元素与基准值的严格小于：a[i] &lt; value */
    private static <T> boolean ltItem(T[] a, int i, T value, Comparator<? super T> cmp) {
        return cmp.compare(a[i], value) < 0;
    }

    /** 交换数组 a 的 i、j 位置元素 */
    private static <T> void swap(T[] a, int i, int j) {
        T tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    // ==================== 冒泡 / 选择 / 插入 / 希尔 ====================

    /**
     * 冒泡排序（稳定）
     */
    static <T> void bubble(T[] a, int from, int to, Comparator<? super T> cmp) {
        for (int i = to - 1; i > from; i--) {
            // 本轮无交换说明已有序，提前退出
            boolean swapped = false;
            for (int j = from; j < i; j++) {
                if (gt(a, j, j + 1, cmp)) {
                    swap(a, j, j + 1);
                    swapped = true;
                }
            }
            if (!swapped) {
                break;
            }
        }
    }

    /**
     * 选择排序（不稳定）
     */
    static <T> void selection(T[] a, int from, int to, Comparator<? super T> cmp) {
        for (int i = from; i < to - 1; i++) {
            int min = i;
            for (int j = i + 1; j < to; j++) {
                if (lt(a, j, min, cmp)) {
                    min = j;
                }
            }
            if (min != i) {
                swap(a, i, min);
            }
        }
    }

    /**
     * 插入排序（稳定，移位式）
     * <p>
     * 同时作为快速/归并/Tim 排序的小数组底层实现。
     */
    static <T> void insertion(T[] a, int from, int to, Comparator<? super T> cmp) {
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
     * 希尔排序（不稳定，Knuth 增量序列 h = 3h + 1，移位式）
     */
    static <T> void shell(T[] a, int from, int to, Comparator<? super T> cmp) {
        int n = to - from;
        int h = 1;
        while (h < n / 3) {
            h = 3 * h + 1;
        }
        while (h >= 1) {
            for (int i = from + h; i < to; i++) {
                T key = a[i];
                int j = i;
                while (j - h >= from && gtKey(a, j - h, key, cmp)) {
                    a[j] = a[j - h];
                    j -= h;
                }
                a[j] = key;
            }
            h /= 3;
        }
    }

    // ==================== 归并 / 快速 ====================

    /**
     * 归并排序（稳定）
     * <p>
     * 辅助数组只在入口分配一次并沿递归传递；小数组回退插入排序；
     * 左侧最大值不超过右侧最小值时跳过合并。
     */
    static <T> void merge(T[] a, int from, int to, Comparator<? super T> cmp) {
        @SuppressWarnings("unchecked")
        T[] aux = (T[]) new Object[to - from];
        mergeRec(a, from, to, aux, cmp);
    }

    /**
     * 归并排序递归实现
     * <p>
     * aux 与 [from, to) 等长且相对 a 偏移 -from：aux 索引 = 数组索引 - from。
     */
    private static <T> void mergeRec(T[] a, int from, int to, T[] aux, Comparator<? super T> cmp) {
        int n = to - from;
        if (n < INSERTION_THRESHOLD) {
            insertion(a, from, to, cmp);
            return;
        }
        int mid = from + n / 2;
        mergeRec(a, from, mid, aux, cmp);
        mergeRec(a, mid, to, aux, cmp);
        // 左侧最大值 <= 右侧最小值：区间已整体有序，无需合并
        if (!gt(a, mid - 1, mid, cmp)) {
            return;
        }
        System.arraycopy(a, from, aux, 0, n);
        int i = 0;
        int j = mid - from;
        int k = from;
        int midOffset = mid - from;
        while (i < midOffset && j < n) {
            // 相等时取左侧，保证稳定性
            if (gt(aux, i, j, cmp)) {
                a[k++] = aux[j++];
            } else {
                a[k++] = aux[i++];
            }
        }
        while (i < midOffset) {
            a[k++] = aux[i++];
        }
        // 右侧剩余元素已就位，无需回拷
    }

    /**
     * 快速排序（不稳定）
     * <p>
     * 三数取中选枢轴 + Hoare 交叉指针划分 + 小数组回退插入排序 +
     * 优先递归较小分区；有序/逆序/全相等输入不退化，敌手输入最坏 O(n^2)。
     */
    static <T> void quick(T[] a, int from, int to, Comparator<? super T> cmp) {
        quickRec(a, from, to, cmp);
    }

    /**
     * 快速排序递归实现
     */
    private static <T> void quickRec(T[] a, int from, int to, Comparator<? super T> cmp) {
        int n = to - from;
        if (n < INSERTION_THRESHOLD) {
            insertion(a, from, to, cmp);
            return;
        }
        // 三数取中：排序网络使 a[from] <= a[mid] <= a[to-1]，
        // 再把中值换到枢轴位 a[from]，规避有序/逆序输入退化
        int mid = from + n / 2;
        if (lt(a, mid, from, cmp)) {
            swap(a, from, mid);
        }
        if (lt(a, to - 1, from, cmp)) {
            swap(a, from, to - 1);
        }
        if (lt(a, to - 1, mid, cmp)) {
            swap(a, mid, to - 1);
        }
        swap(a, from, mid);

        T pivot = a[from];
        // Hoare 交叉指针划分：相等元素两端都前进，全相等输入在中间交叉、近似对半
        int i = from;
        int j = to;
        while (true) {
            do {
                i++;
            } while (ltKey(a, i, pivot, cmp));
            do {
                j--;
            } while (gtKey(a, j, pivot, cmp));
            if (i >= j) {
                break;
            }
            swap(a, i, j);
        }
        swap(a, from, j);
        // 优先递归较小的一侧，限制递归深度
        if (j - from < to - (j + 1)) {
            quickRec(a, from, j, cmp);
            quickRec(a, j + 1, to, cmp);
        } else {
            quickRec(a, j + 1, to, cmp);
            quickRec(a, from, j, cmp);
        }
    }

    // ==================== 堆排序 ====================

    /**
     * 堆排序（不稳定，原地最大堆）
     */
    static <T> void heap(T[] a, int from, int to, Comparator<? super T> cmp) {
        int n = to - from;
        // 建堆：从最后一个非叶节点向下调整
        for (int i = n / 2 - 1; i >= 0; i--) {
            siftDown(a, from, n, i, cmp);
        }
        // 反复把堆顶（最大值）交换到区间尾部并缩小堆
        for (int end = n - 1; end > 0; end--) {
            swap(a, from, from + end);
            siftDown(a, from, end, 0, cmp);
        }
    }

    /**
     * 最大堆向下调整
     */
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

    // ==================== 梳排序 ====================

    /**
     * 梳排序（不稳定，收缩因子 1.3）
     */
    static <T> void comb(T[] a, int from, int to, Comparator<? super T> cmp) {
        int n = to - from;
        int gap = n;
        boolean swapped = true;
        while (gap > 1 || swapped) {
            gap = gap * 10 / 13;
            if (gap < 1) {
                gap = 1;
            }
            swapped = false;
            for (int i = from; i + gap < to; i++) {
                if (gt(a, i, i + gap, cmp)) {
                    swap(a, i, i + gap);
                    swapped = true;
                }
            }
        }
    }

    // ==================== 地精排序 ====================

    /**
     * 地精排序（稳定）
     */
    static <T> void gnome(T[] a, int from, int to, Comparator<? super T> cmp) {
        int i = from;
        while (i < to) {
            if (i == from || !lt(a, i, i - 1, cmp)) {
                i++;
            } else {
                swap(a, i, i - 1);
                i--;
            }
        }
    }

    // ==================== 鸡尾酒排序 ====================

    /**
     * 鸡尾酒排序（稳定，双向冒泡）
     */
    static <T> void cocktail(T[] a, int from, int to, Comparator<? super T> cmp) {
        int lo = from;
        int hi = to - 1;
        while (lo < hi) {
            boolean swapped = false;
            for (int i = lo; i < hi; i++) {
                if (gt(a, i, i + 1, cmp)) {
                    swap(a, i, i + 1);
                    swapped = true;
                }
            }
            hi--;
            if (!swapped) {
                break;
            }
            swapped = false;
            for (int i = hi; i > lo; i--) {
                if (lt(a, i, i - 1, cmp)) {
                    swap(a, i, i - 1);
                    swapped = true;
                }
            }
            lo++;
            if (!swapped) {
                break;
            }
        }
    }

    // ==================== 循环排序 ====================

    /**
     * 循环排序（不稳定，最少写入次数）
     */
    static <T> void cycle(T[] a, int from, int to, Comparator<? super T> cmp) {
        for (int cycleStart = from; cycleStart < to - 1; cycleStart++) {
            T item = a[cycleStart];
            // 计算 item 的最终位置：区间内严格小于 item 的元素个数
            int pos = cycleStart;
            for (int i = cycleStart + 1; i < to; i++) {
                if (ltItem(a, i, item, cmp)) {
                    pos++;
                }
            }
            if (pos == cycleStart) {
                continue;
            }
            // 跳过相等元素（重复值处理关键）
            while (pos < to && cmp.compare(item, a[pos]) == 0) {
                pos++;
            }
            T tmp = a[pos];
            a[pos] = item;
            item = tmp;
            // 沿环旋转剩余元素
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

    // ==================== 奇偶排序 ====================

    /**
     * 奇偶排序（稳定，可并行化的冒泡变体）
     */
    static <T> void oddEven(T[] a, int from, int to, Comparator<? super T> cmp) {
        boolean sorted = false;
        while (!sorted) {
            sorted = true;
            for (int i = from + 1; i + 1 < to; i += 2) {
                if (gt(a, i, i + 1, cmp)) {
                    swap(a, i, i + 1);
                    sorted = false;
                }
            }
            for (int i = from; i + 1 < to; i += 2) {
                if (gt(a, i, i + 1, cmp)) {
                    swap(a, i, i + 1);
                    sorted = false;
                }
            }
        }
    }

    // ==================== 煎饼排序 ====================

    /**
     * 煎饼排序（不稳定，前缀翻转）
     */
    static <T> void pancake(T[] a, int from, int to, Comparator<? super T> cmp) {
        for (int size = to - from; size > 1; size--) {
            int maxIdx = from;
            for (int i = from + 1; i < from + size; i++) {
                if (lt(a, maxIdx, i, cmp)) {
                    maxIdx = i;
                }
            }
            if (maxIdx == from + size - 1) {
                continue;
            }
            if (maxIdx != from) {
                flip(a, from, maxIdx);
            }
            flip(a, from, from + size - 1);
        }
    }

    /**
     * 翻转 [from, toIdx] 闭区间
     */
    private static <T> void flip(T[] a, int from, int toIdx) {
        while (from < toIdx) {
            swap(a, from, toIdx);
            from++;
            toIdx--;
        }
    }

    // ==================== 臭皮匠排序 ====================

    /**
     * 臭皮匠排序（不稳定，纯教育用途）
     */
    static <T> void stooge(T[] a, int from, int to, Comparator<? super T> cmp) {
        if (to - from < 2) {
            return;
        }
        stoogeRec(a, from, to - 1, cmp);
    }

    /**
     * 臭皮匠排序递归实现（闭区间 [lo, hi]）
     */
    private static <T> void stoogeRec(T[] a, int lo, int hi, Comparator<? super T> cmp) {
        if (gt(a, lo, hi, cmp)) {
            swap(a, lo, hi);
        }
        if (hi - lo + 1 > 2) {
            int t = (hi - lo + 1) / 3;
            stoogeRec(a, lo, hi - t, cmp);
            stoogeRec(a, lo + t, hi, cmp);
            stoogeRec(a, lo, hi - t, cmp);
        }
    }

    // ==================== 双调排序 ====================

    /**
     * 双调排序（不稳定，比较网络，索引置换实现）
     * <p>
     * 内部把长度补齐到 2 的幂（n ≤ 2^30），虚拟索引按"大于一切真实元素"处理，
     * 因此对任意 Comparator（含 float/double 的 NaN 全序）都正确；
     * 排序只在索引层进行，最终按索引置换回写。
     */
    static <T> void bitonic(T[] a, int from, int to, Comparator<? super T> cmp) {
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
                            // 升序块：较小者在位置 i
                            if (gtIdx(a, from, idx, i, l, cmp)) {
                                swapIdx(idx, i, l);
                            }
                        } else {
                            // 降序块：较大者在位置 i
                            if (ltIdx(a, from, idx, i, l, cmp)) {
                                swapIdx(idx, i, l);
                            }
                        }
                    }
                }
            }
        }
        // 按排序后的索引置换回写
        T[] aux = Arrays.copyOfRange(a, from, to);
        for (int i = 0; i < n; i++) {
            a[from + i] = aux[idx[i]];
        }
    }

    /**
     * 索引层严格大于：虚拟索引（-1）按 +∞ 处理
     */
    private static <T> boolean gtIdx(T[] a, int from, int[] idx, int x, int y, Comparator<? super T> cmp) {
        if (idx[x] < 0) {
            return idx[y] >= 0;
        }
        if (idx[y] < 0) {
            return false;
        }
        return cmp.compare(a[from + idx[x]], a[from + idx[y]]) > 0;
    }

    /**
     * 索引层严格小于：虚拟索引（-1）按 +∞ 处理
     */
    private static <T> boolean ltIdx(T[] a, int from, int[] idx, int x, int y, Comparator<? super T> cmp) {
        if (idx[y] < 0) {
            return idx[x] >= 0;
        }
        if (idx[x] < 0) {
            return false;
        }
        return cmp.compare(a[from + idx[x]], a[from + idx[y]]) < 0;
    }

    /**
     * 交换索引数组的 x、y 位置
     */
    private static void swapIdx(int[] idx, int x, int y) {
        int tmp = idx[x];
        idx[x] = idx[y];
        idx[y] = tmp;
    }

    // ==================== 树排序 ====================

    /**
     * 树排序（稳定）
     * <p>
     * 对象实现基于红黑树（{@link TreeMap}）：比较相等但身份不同的元素
     * 以列表聚合保序，保证稳定且不丢失元素身份；最坏 O(n log n)。
     */
    static <T> void tree(T[] a, int from, int to, Comparator<? super T> cmp) {
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

    // ==================== Tim 排序 ====================

    /**
     * Tim 排序（稳定，自适应）
     * <p>
     * 简化版 TimSort（不做 galloping）：检测并利用已有序片段（run），
     * 已有序输入 O(n)，最坏 O(n log n)。run 栈不变量采用 JDK 2015 修正版
     * （JDK-8072909）。为 {@link Sort#sort(Object[])} 系列的默认算法。
     */
    static <T> void tim(T[] a, int from, int to, Comparator<? super T> cmp) {
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
            // 严格降序 run：反转为升序
            while (runHi < hi && lt(a, runHi, runHi - 1, cmp)) {
                runHi++;
            }
            reverseRange(a, lo, runHi - 1);
        } else {
            // 升序 run（相等元素延续，保证稳定性）
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
            // 找到第一个大于 pivot 的位置（相等元素保留在左侧，保证稳定性）
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
     * 维护 run 栈不变量（JDK-8072909 修正版）：
     * <ol>
     *   <li>runLen[i-3] &gt; runLen[i-2] + runLen[i-1]</li>
     *   <li>runLen[i-2] &gt; runLen[i-1]</li>
     * </ol>
     * 违规时归并较小的相邻 run。
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
        // 左侧整体已在最终位置：跳过归并
        if (!gt(a, base2 - 1, base2, cmp)) {
            return stackSize - 1;
        }
        // 无 gallop 的稳定归并：拷贝左侧到 tmp，相等取左（右侧仅当严格更小时取）
        System.arraycopy(a, base1, tmp, 0, len1);
        int i1 = 0;
        int i2 = base2;
        int k = base1;
        int end2 = base2 + len2;
        while (i1 < len1 && i2 < end2) {
            if (ltCross(a, i2, tmp, i1, cmp)) {
                a[k++] = a[i2++];
            } else {
                a[k++] = tmp[i1++];
            }
        }
        while (i1 < len1) {
            a[k++] = tmp[i1++];
        }
        // 右侧剩余元素已就位
        return stackSize - 1;
    }

    /**
     * 跨数组严格小于：a[i] &lt; b[j]
     */
    private static <T> boolean ltCross(T[] a, int i, T[] b, int j, Comparator<? super T> cmp) {
        return cmp.compare(a[i], b[j]) < 0;
    }
}
