package org.zero.sort;

/**
 * long[] 专用排序实现（21 种算法中的适用子集）
 * <p>
 * 与 {@link GenericSorts}（对象数组版）算法一致：严格比较、相同稳定性。
 * 比较语义：有符号 64 位整数自然序。
 *
 * @author Zero
 */
final class LongSorts {

    private LongSorts() {
    }

    /** 插入排序阈值：小于该长度的区间使用插入排序 */
    static final int INSERTION_THRESHOLD = 16;

    /** 自适应默认排序的插入阈值 */
    private static final int DEFAULT_INSERTION_THRESHOLD = 47;

    /** Tim 排序的最小 run 长度基准 */
    private static final int TIM_MIN_MERGE = 32;

    /** 严格大于比较 */
    private static boolean gt(long[] a, int i, int j) {
        return a[i] > a[j];
    }

    /** 元素与基准值的严格大于比较 */
    private static boolean gtKey(long[] a, int i, long key) {
        return a[i] > key;
    }

    /** 元素与基准值的严格小于比较 */
    private static boolean ltKey(long[] a, int i, long key) {
        return a[i] < key;
    }

    /** 元素与元素的严格小于比较 */
    private static boolean lt(long[] a, int i, int j) {
        return a[i] < a[j];
    }

    /** 基准值与元素的严格小于比较 */
    private static boolean ltItem(long[] a, int i, long value) {
        return a[i] < value;
    }

    /** 基准值与元素的相等比较（compare 语义，float/double 下 -0.0/+0.0 与 NaN 正确） */
    private static boolean eqVal(long value, long[] a, int i) {
        return value == a[i];
    }

    /** 跨数组严格小于：a[i] &lt; b[j] */
    private static boolean ltCross(long[] a, int i, long[] b, int j) {
        return a[i] < b[j];
    }

    /** 交换数组 a 的 i、j 位置元素 */
    private static void swap(long[] a, int i, int j) {
        long tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    /**
     * 冒泡排序：稳定，带提前退出优化
     */
    static void bubble(long[] a, int from, int to) {
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
    static void selection(long[] a, int from, int to) {
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
    static void insertion(long[] a, int from, int to) {
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

    /**
     * 希尔排序：不稳定，Knuth 增量序列 h = 3h + 1
     */
    static void shell(long[] a, int from, int to) {
        int n = to - from;
        int h = 1;
        while (h < n / 3) {
            h = 3 * h + 1;
        }
        while (h >= 1) {
            for (int i = from + h; i < to; i++) {
                long key = a[i];
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
    static void merge(long[] a, int from, int to) {
        long[] aux = new long[to - from];
        mergeRec(a, from, to, aux);
    }

    private static void mergeRec(long[] a, int from, int to, long[] aux) {
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
     * 快速排序：不稳定，三数取中 + Hoare 交叉指针 + 小数组回退插入
     */
    static void quick(long[] a, int from, int to) {
        quickRec(a, from, to);
    }

    private static void quickRec(long[] a, int from, int to) {
        int n = to - from;
        if (n < INSERTION_THRESHOLD) {
            insertion(a, from, to);
            return;
        }
        int mid = from + n / 2;
        if (gt(a, from, mid)) {
            swap(a, from, mid);
        }
        if (gt(a, from, to - 1)) {
            swap(a, from, to - 1);
        }
        if (gt(a, mid, to - 1)) {
            swap(a, mid, to - 1);
        }
        swap(a, from, mid);

        long pivot = a[from];
        int i = from;
        int j = to;
        while (true) {
            do {
                i++;
            } while (ltKey(a, i, pivot));
            do {
                j--;
            } while (gtKey(a, j, pivot));
            if (i >= j) {
                break;
            }
            swap(a, i, j);
        }
        swap(a, from, j);
        if (j - from < to - (j + 1)) {
            quickRec(a, from, j);
            quickRec(a, j + 1, to);
        } else {
            quickRec(a, j + 1, to);
            quickRec(a, from, j);
        }
    }

    /**
     * 堆排序：不稳定，原地最大堆
     */
    static void heap(long[] a, int from, int to) {
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

    /**
     * 梳排序：不稳定，收缩因子 1.3
     */
    static void comb(long[] a, int from, int to) {
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
    static void gnome(long[] a, int from, int to) {
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
    static void cocktail(long[] a, int from, int to) {
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
    static void cycle(long[] a, int from, int to) {
        for (int cycleStart = from; cycleStart < to - 1; cycleStart++) {
            long item = a[cycleStart];
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
            long tmp = a[pos];
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
    static void oddEven(long[] a, int from, int to) {
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
    static void pancake(long[] a, int from, int to) {
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

    private static void flip(long[] a, int from, int toIdx) {
        while (from < toIdx) {
            swap(a, from, toIdx);
            from++;
            toIdx--;
        }
    }

    /**
     * 臭皮匠排序：不稳定，纯教育用途
     */
    static void stooge(long[] a, int from, int to) {
        if (to - from < 2) {
            return;
        }
        stoogeRec(a, from, to - 1);
    }

    private static void stoogeRec(long[] a, int lo, int hi) {
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
    static void bitonic(long[] a, int from, int to) {
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
        long[] aux = new long[n];
        System.arraycopy(a, from, aux, 0, n);
        for (int i = 0; i < n; i++) {
            a[from + i] = aux[idx[i]];
        }
    }

    private static boolean gtIdx(long[] a, int from, int[] idx, int x, int y) {
        if (idx[x] < 0) {
            return idx[y] >= 0;
        }
        if (idx[y] < 0) {
            return false;
        }
        return a[from + idx[x]] > a[from + idx[y]];
    }

    private static boolean ltIdx(long[] a, int from, int[] idx, int x, int y) {
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
    static void tree(long[] a, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return;
        }
        long[] keys = new long[n];
        int[] left = new int[n];
        int[] right = new int[n];
        java.util.Arrays.fill(left, -1);
        java.util.Arrays.fill(right, -1);
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
    static void tim(long[] a, int from, int to) {
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
        if (!gt(a, base2 - 1, base2)) {
            return stackSize - 1;
        }
        System.arraycopy(a, base1, tmp, 0, len1);
        int i1 = 0;
        int i2 = base2;
        int k = base1;
        int end2 = base2 + len2;
        while (i1 < len1 && i2 < end2) {
            if (ltCross(a, i2, tmp, i1)) {
                a[k++] = a[i2++];
            } else {
                a[k++] = tmp[i1++];
            }
        }
        while (i1 < len1) {
            a[k++] = tmp[i1++];
        }
        return stackSize - 1;
    }

    /**
     * 自适应默认排序：小数组插入、近有序归并、否则快排（Sort.sort(a) 系列的默认策略）
     */
    static void sortDefault(long[] a, int from, int to) {
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
            merge(a, from, to);
        } else {
            quick(a, from, to);
        }
    }

    /** 计数/鸽巢排序值域上限：超限抛 IllegalArgumentException */
    private static final long MAX_COUNTING_RANGE = 1L << 24;

    /**
     * 计数排序：稳定，O(n+k)；值域超上限抛 IllegalArgumentException
     */
    static void counting(long[] a, int from, int to) {
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

    /**
     * 鸽巢排序：稳定，O(n+k)，计数排序的变体（值域超上限抛 IllegalArgumentException）
     */
    static void pigeonhole(long[] a, int from, int to) {
        counting(a, from, to);
    }

    /**
     * 基数排序：稳定，LSD base-256，负数按补码正确排序
     */
    static void radix(long[] a, int from, int to) {
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
}
