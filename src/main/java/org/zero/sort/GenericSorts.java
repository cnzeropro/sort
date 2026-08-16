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

    /** 双轴快排的插入排序阈值（JDK 同款 47） */
    private static final int QUICK_INSERTION_THRESHOLD = 47;

    /** Tim 排序的最小 run 长度基准 */
    private static final int TIM_MIN_MERGE = 32;

    /** Tim 排序进入 galloping 模式的连胜阈值（JDK 同款） */
    private static final int MIN_GALLOP = 7;

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
     * 快速排序（不稳定，双轴）
     * <p>
     * Yaroslavskiy 双轴划分（与 JDK {@link java.util.Arrays#sort(int[])} 同款算法思想）：
     * 三取样选双枢轴、相等枢轴回退单轴 Hoare 交叉划分、小数组回退插入排序；
     * 有序/逆序/全相等输入不退化，敌手输入最坏 O(n^2)。
     */
    static <T> void quick(T[] a, int from, int to, Comparator<? super T> cmp) {
        quickRec(a, from, to - 1, cmp);
    }

    /**
     * 双轴快速排序递归实现（闭区间 [lo, hi]）
     */
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
        // 5 元素排序网络
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
            // 保存两端旧值到取样位，避免枢轴落位丢元素
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
     * 带 galloping 的 TimSort（MIN_GALLOP = 7）：检测并利用已有序片段（run），
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
     * <p>
     * 带 galloping 的稳定归并：单侧连续取胜 MIN_GALLOP 次后切换为指数搜索跳过
     * 长片段（长相等/有序片段场景下显著减少比较次数，JDK TimSort 同款策略）。
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
        // gallop 跳过左 run 头部整体小于右 run 首元素的部分
        int k = gallopRight(a[base2], a, base1, len1, 0, cmp);
        base1 += k;
        len1 -= k;
        if (len1 == 0) {
            return stackSize - 1;
        }
        // gallop 跳过右 run 尾部整体大于左 run 末元素的部分
        len2 = gallopLeft(a[base1 + len1 - 1], a, base2, len2, len2 - 1, cmp);
        if (len2 == 0) {
            return stackSize - 1;
        }
        // 拷贝较小的一侧到 tmp，另一侧从原数组就地合并（相等取左，稳定）
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
            // 线性归并，统计单侧连胜
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
            // 某侧连胜达到阈值：gallop 跳过该侧的长片段
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
            // 提高 gallop 触发门槛，避免小归并里频繁切换
            minGallop++;
        }
        while (i1 < len1) {
            a[k++] = tmp[i1++];
        }
        // 右侧剩余已就位
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
                if (gtCross(tmp, i2, a, i1, cmp)) {
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
        // 左侧剩余已就位
    }

    /**
     * gallopRight：在已排序区间 a[base, base+len) 中返回第一个 &gt; key 的位置
     * （即 key 的插入点右侧；hint 为猜测位置，用于就近指数搜索）
     */
    private static <T> int gallopRight(
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
     * gallopLeft：在已排序区间 a[base, base+len) 中返回第一个 &gt;= key 的位置
     * （即 key 的插入点左侧；hint 为猜测位置）
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
            if (cmp.compare(key, a[base + m]) < 0) {
                ofs = m;
            } else {
                lastOfs = m + 1;
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

    /**
     * 跨数组严格大于：a[i] &gt; b[j]
     */
    private static <T> boolean gtCross(T[] a, int i, T[] b, int j, Comparator<? super T> cmp) {
        return cmp.compare(a[i], b[j]) > 0;
    }
}
