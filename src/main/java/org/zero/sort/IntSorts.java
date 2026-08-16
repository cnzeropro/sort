package org.zero.sort;

/**
 * int[] 专用排序实现（21 种算法中的适用子集）
 * <p>
 * 与 {@link GenericSorts}（对象数组版）算法一致：严格比较、相同稳定性。
 * 比较语义：有符号 32 位整数自然序。
 *
 * @author Zero
 */
final class IntSorts {

    private IntSorts() {
    }

    /** 插入排序阈值：小于该长度的区间使用插入排序 */
    static final int INSERTION_THRESHOLD = 16;

    /** 自适应默认排序的插入阈值 */
    private static final int DEFAULT_INSERTION_THRESHOLD = 47;

    /** Tim 排序的最小 run 长度基准 */
    private static final int TIM_MIN_MERGE = 32;

    /** 双轴快排的插入排序阈值（JDK 同款 47） */
    private static final int QUICK_INSERTION_THRESHOLD = 47;

    /** Tim 排序进入 galloping 模式的连胜阈值（JDK 同款） */
    private static final int MIN_GALLOP = 7;

    /** 严格大于比较 */
    private static boolean gt(int[] a, int i, int j) {
        return a[i] > a[j];
    }

    /** 元素与基准值的严格大于比较 */
    private static boolean gtKey(int[] a, int i, int key) {
        return a[i] > key;
    }

    /** 元素与基准值的严格小于比较 */
    private static boolean ltKey(int[] a, int i, int key) {
        return a[i] < key;
    }

    /** 元素与元素的严格小于比较 */
    private static boolean lt(int[] a, int i, int j) {
        return a[i] < a[j];
    }

    /** 基准值与元素的严格小于比较 */
    private static boolean ltItem(int[] a, int i, int value) {
        return a[i] < value;
    }

    /** 基准值与元素的相等比较（compare 语义，float/double 下 -0.0/+0.0 与 NaN 正确） */
    private static boolean eqVal(int value, int[] a, int i) {
        return value == a[i];
    }

    /** 跨数组严格小于：a[i] &lt; b[j] */
    private static boolean ltCross(int[] a, int i, int[] b, int j) {
        return a[i] < b[j];
    }

    /** 交换数组 a 的 i、j 位置元素 */
    private static void swap(int[] a, int i, int j) {
        int tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    /**
     * 冒泡排序：稳定，带提前退出优化
     */
    static void bubble(int[] a, int from, int to) {
        for (int i = to - 1; i > from; i--) {
            boolean swapped = false;
            for (int j = from; j < i; j++) {
                if (gt(a, j, j + 1)) {
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
     * 选择排序：不稳定
     */
    static void selection(int[] a, int from, int to) {
        for (int i = from; i < to - 1; i++) {
            int min = i;
            for (int j = i + 1; j < to; j++) {
                if (lt(a, j, min)) {
                    min = j;
                }
            }
            if (min != i) {
                swap(a, i, min);
            }
        }
    }

    /**
     * 插入排序：稳定，移位式（快速/归并/Tim 的小数组底层实现）
     */
    static void insertion(int[] a, int from, int to) {
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

    /**
     * 希尔排序：不稳定，Knuth 增量序列 h = 3h + 1
     */
    static void shell(int[] a, int from, int to) {
        int n = to - from;
        int h = 1;
        while (h < n / 3) {
            h = 3 * h + 1;
        }
        while (h >= 1) {
            for (int i = from + h; i < to; i++) {
                int key = a[i];
                int j = i;
                while (j - h >= from && gtKey(a, j - h, key)) {
                    a[j] = a[j - h];
                    j -= h;
                }
                a[j] = key;
            }
            h /= 3;
        }
    }

    /**
     * 归并排序：稳定，辅助数组只分配一次，小数组回退插入
     */
    static void merge(int[] a, int from, int to) {
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

    /**
     * 快速排序：不稳定，双轴（Yaroslavskiy，与 JDK 双轴快排同款算法思想）
     */
    static void quick(int[] a, int from, int to) {
        quickRec(a, from, to - 1);
    }

    /**
     * 双轴快速排序递归实现（闭区间 [lo, hi]）
     */
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
        // 5 元素排序网络
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
            // 保存两端旧值到取样位，避免枢轴落位丢元素
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

    /**
     * 堆排序：不稳定，原地最大堆
     */
    static void heap(int[] a, int from, int to) {
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

    /**
     * 梳排序：不稳定，收缩因子 1.3
     */
    static void comb(int[] a, int from, int to) {
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
                if (gt(a, i, i + gap)) {
                    swap(a, i, i + gap);
                    swapped = true;
                }
            }
        }
    }

    /**
     * 地精排序：稳定
     */
    static void gnome(int[] a, int from, int to) {
        int i = from;
        while (i < to) {
            if (i == from || !lt(a, i, i - 1)) {
                i++;
            } else {
                swap(a, i, i - 1);
                i--;
            }
        }
    }

    /**
     * 鸡尾酒排序：稳定，双向冒泡
     */
    static void cocktail(int[] a, int from, int to) {
        int lo = from;
        int hi = to - 1;
        while (lo < hi) {
            boolean swapped = false;
            for (int i = lo; i < hi; i++) {
                if (gt(a, i, i + 1)) {
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
                if (lt(a, i, i - 1)) {
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

    /**
     * 循环排序：不稳定，最少写入次数（重复值用 compare 相等判断跳过）
     */
    static void cycle(int[] a, int from, int to) {
        for (int cycleStart = from; cycleStart < to - 1; cycleStart++) {
            int item = a[cycleStart];
            int pos = cycleStart;
            for (int i = cycleStart + 1; i < to; i++) {
                if (ltItem(a, i, item)) {
                    pos++;
                }
            }
            if (pos == cycleStart) {
                continue;
            }
            while (pos < to && eqVal(item, a, pos)) {
                pos++;
            }
            int tmp = a[pos];
            a[pos] = item;
            item = tmp;
            while (pos != cycleStart) {
                pos = cycleStart;
                for (int i = cycleStart + 1; i < to; i++) {
                    if (ltItem(a, i, item)) {
                        pos++;
                    }
                }
                while (pos < to && eqVal(item, a, pos)) {
                    pos++;
                }
                tmp = a[pos];
                a[pos] = item;
                item = tmp;
            }
        }
    }

    /**
     * 奇偶排序：稳定，可并行化的冒泡变体
     */
    static void oddEven(int[] a, int from, int to) {
        boolean sorted = false;
        while (!sorted) {
            sorted = true;
            for (int i = from + 1; i + 1 < to; i += 2) {
                if (gt(a, i, i + 1)) {
                    swap(a, i, i + 1);
                    sorted = false;
                }
            }
            for (int i = from; i + 1 < to; i += 2) {
                if (gt(a, i, i + 1)) {
                    swap(a, i, i + 1);
                    sorted = false;
                }
            }
        }
    }

    /**
     * 煎饼排序：不稳定，前缀翻转
     */
    static void pancake(int[] a, int from, int to) {
        for (int size = to - from; size > 1; size--) {
            int maxIdx = from;
            for (int i = from + 1; i < from + size; i++) {
                if (lt(a, maxIdx, i)) {
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

    private static void flip(int[] a, int from, int toIdx) {
        while (from < toIdx) {
            swap(a, from, toIdx);
            from++;
            toIdx--;
        }
    }

    /**
     * 臭皮匠排序：不稳定，纯教育用途
     */
    static void stooge(int[] a, int from, int to) {
        if (to - from < 2) {
            return;
        }
        stoogeRec(a, from, to - 1);
    }

    private static void stoogeRec(int[] a, int lo, int hi) {
        if (gt(a, lo, hi)) {
            swap(a, lo, hi);
        }
        if (hi - lo + 1 > 2) {
            int t = (hi - lo + 1) / 3;
            stoogeRec(a, lo, hi - t);
            stoogeRec(a, lo + t, hi);
            stoogeRec(a, lo, hi - t);
        }
    }

    /**
     * 双调排序：不稳定，比较网络（索引置换实现，虚拟索引按 +∞ 处理）
     */
    static void bitonic(int[] a, int from, int to) {
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
                            if (gtIdx(a, from, idx, i, l)) {
                                swapIdx(idx, i, l);
                            }
                        } else {
                            if (ltIdx(a, from, idx, i, l)) {
                                swapIdx(idx, i, l);
                            }
                        }
                    }
                }
            }
        }
        int[] aux = new int[n];
        System.arraycopy(a, from, aux, 0, n);
        for (int i = 0; i < n; i++) {
            a[from + i] = aux[idx[i]];
        }
    }

    private static boolean gtIdx(int[] a, int from, int[] idx, int x, int y) {
        if (idx[x] < 0) {
            return idx[y] >= 0;
        }
        if (idx[y] < 0) {
            return false;
        }
        return a[from + idx[x]] > a[from + idx[y]];
    }

    private static boolean ltIdx(int[] a, int from, int[] idx, int x, int y) {
        if (idx[y] < 0) {
            return idx[x] >= 0;
        }
        if (idx[x] < 0) {
            return false;
        }
        return a[from + idx[x]] < a[from + idx[y]];
    }

    private static void swapIdx(int[] idx, int x, int y) {
        int tmp = idx[x];
        idx[x] = idx[y];
        idx[y] = tmp;
    }

    /**
     * 树排序：稳定，朴素二叉搜索树（并行数组，不装箱）；有序输入退化为最坏 O(n^2)
     */
    static void tree(int[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        int[] keys = new int[n];
        int[] left = new int[n];
        int[] right = new int[n];
        java.util.Arrays.fill(left, -1);
        java.util.Arrays.fill(right, -1);
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
        // 显式栈中序遍历（相等元素进右子树、根先于右，稳定）
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

    /**
     * Tim 排序：稳定，自适应（简化版，无 galloping）
     */
    static void tim(int[] a, int from, int to) {
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

    private static int minRunLength(int n) {
        int r = 0;
        while (n >= TIM_MIN_MERGE) {
            r |= (n & 1);
            n >>= 1;
        }
        return n + r;
    }

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

    /**
     * 归并 run 栈上相邻的两个 run（i 与 i+1），把栈顶下移一位
     * <p>
     * 带 galloping 的稳定归并：单侧连续取胜 MIN_GALLOP 次后切换为指数搜索跳过
     * 长片段（长相等/有序片段场景下显著减少比较次数，JDK TimSort 同款策略）。
     *
     * @return 归并后的 run 栈大小（stackSize - 1）
     */
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
        // gallop 跳过左 run 头部整体小于右 run 首元素的部分
        int k = gallopRight(a[base2], a, base1, len1, 0);
        base1 += k;
        len1 -= k;
        if (len1 == 0) {
            return stackSize - 1;
        }
        // gallop 跳过右 run 尾部整体大于左 run 末元素的部分
        len2 = gallopLeft(a[base1 + len1 - 1], a, base2, len2, len2 - 1);
        if (len2 == 0) {
            return stackSize - 1;
        }
        // 拷贝较小的一侧到 tmp，另一侧从原数组就地合并（相等取左，稳定）
        if (len1 <= len2) {
            System.arraycopy(a, base1, tmp, 0, len1);
            mergeLo(a, tmp, base1, len1, base2, len2);
        } else {
            System.arraycopy(a, base2, tmp, 0, len2);
            mergeHi(a, tmp, base1, len1, base2, len2);
        }
        return stackSize - 1;
    }

    /**
     * 左 run 在 tmp（就地回写 a）、右 run 在原数组的 galloping 归并
     */
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

    /**
     * 右 run 在 tmp、左 run 在原数组的 galloping 归并（从右往左回写，保证稳定）
     */
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

    /**
     * gallopRight：在已排序区间 a[base, base+len) 中返回第一个 &gt; key 的位置
     */
    private static int gallopRight(int key, int[] a, int base, int len, int hint) {
        int lastOfs = 0;
        int ofs = 1;
        if (ltKey(a, base + hint, key)) {
            // 向后 gallop：key > a[hint]，找第一个 > key 的位置
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
            // 向前 gallop：key <= a[hint]
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

    /**
     * gallopLeft：在已排序区间 a[base, base+len) 中返回第一个 &gt;= key 的位置
     */
    private static int gallopLeft(int key, int[] a, int base, int len, int hint) {
        int lastOfs = 0;
        int ofs = 1;
        if (ltKey(a, base + hint, key)) {
            // 向后 gallop：key > a[hint]
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
            // 向前 gallop：key <= a[hint]
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

    /** 跨数组严格大于：a[i] &gt; b[j] */
    private static boolean gtCross(int[] a, int i, int[] b, int j) {
        return ltCross(b, j, a, i);
    }

    /**
     * 自适应默认排序：小数组插入、近有序归并、否则快排（Sort.sort(a) 系列的默认策略）
     */
    static void sortDefault(int[] a, int from, int to) {
        int n = to - from;
        if (n < DEFAULT_INSERTION_THRESHOLD) {
            insertion(a, from, to);
            return;
        }
        long inversions = 0;
        for (int i = from + 1; i < to; i++) {
            if (gt(a, i - 1, i)) {
                inversions++;
            }
        }
        if (inversions < n / 8) {
            tim(a, from, to);
        } else {
            quick(a, from, to);
        }
    }

    /** 计数/鸽巢排序值域上限：超限抛 IllegalArgumentException */
    private static final long MAX_COUNTING_RANGE = 1L << 24;

    /**
     * 计数排序：稳定，O(n+k)；值域超上限抛 IllegalArgumentException
     */
    static void counting(int[] a, int from, int to) {
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

    /**
     * 鸽巢排序：稳定，O(n+k)，计数排序的变体（值域超上限抛 IllegalArgumentException）
     */
    static void pigeonhole(int[] a, int from, int to) {
        counting(a, from, to);
    }

    /**
     * 基数排序：稳定，LSD base-256，负数按补码正确排序
     */
    static void radix(int[] a, int from, int to) {
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
}
