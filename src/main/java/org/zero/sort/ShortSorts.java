package org.zero.sort;

/**
 * short[] 专用排序实现
 * <p>
 * ★ 本文件由 tools/gen-primitives.py 自动生成，请勿手改；如需修改请编辑脚本后重新生成。
 * <p>
 * 算法与 {@link GenericSorts}（对象数组版）完全一致：严格比较、相同稳定性、
 * 三数取中快速排序、单次分配归并排序。比较语义：有符号 16 位整数自然序。
 *
 * @author Zero
 */
final class ShortSorts {

    private ShortSorts() {
    }

    /** 插入排序阈值：小于该长度的区间使用插入排序 */
    static final int INSERTION_THRESHOLD = 16;

    /** 严格大于比较：有符号 16 位整数自然序 */
    private static boolean gt(short[] a, int i, int j) {
        return a[i] > a[j];
    }

    /** 元素与基准值的严格大于比较 */
    private static boolean gtKey(short[] a, int i, short key) {
        return a[i] > key;
    }

    /** 元素与基准值的严格小于比较 */
    private static boolean ltKey(short[] a, int i, short key) {
        return a[i] < key;
    }

    /** 交换数组 a 的 i、j 位置元素 */
    private static void swap(short[] a, int i, int j) {
        short tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    /**
     * 冒泡排序：稳定，最好 O(n)，平均/最坏 O(n^2)，空间 O(1)
     */
    static void bubble(short[] a, int from, int to) {
        for (int i = to - 1; i > from; i--) {
            // 本轮无交换说明已有序，提前退出
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
     * 选择排序：不稳定，最好/平均/最坏均为 O(n^2)，空间 O(1)
     */
    static void selection(short[] a, int from, int to) {
        for (int i = from; i < to - 1; i++) {
            int min = i;
            for (int j = i + 1; j < to; j++) {
                if (gt(a, min, j)) {
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
     * 同时作为快速/归并排序的小数组底层实现。
     */
    static void insertion(short[] a, int from, int to) {
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

    /**
     * 希尔排序（不稳定，Knuth 增量序列 h = 3h + 1，移位式）
     */
    static void shell(short[] a, int from, int to) {
        int n = to - from;
        int h = 1;
        while (h < n / 3) {
            h = 3 * h + 1;
        }
        while (h >= 1) {
            for (int i = from + h; i < to; i++) {
                short key = a[i];
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
     * 归并排序（稳定）
     * <p>
     * 辅助数组只在入口分配一次并沿递归传递；小数组回退插入排序；
     * 左侧最大值不超过右侧最小值时跳过合并。
     */
    static void merge(short[] a, int from, int to) {
        short[] aux = new short[to - from];
        mergeRec(a, from, to, aux);
    }

    /**
     * 归并排序递归实现
     * <p>
     * aux 与 [from, to) 等长且相对 a 偏移 -from：aux 索引 = 数组索引 - from。
     */
    private static void mergeRec(short[] a, int from, int to, short[] aux) {
        int n = to - from;
        if (n < INSERTION_THRESHOLD) {
            insertion(a, from, to);
            return;
        }
        int mid = from + n / 2;
        mergeRec(a, from, mid, aux);
        mergeRec(a, mid, to, aux);
        // 左侧最大值 <= 右侧最小值：区间已整体有序，无需合并
        if (!gt(a, mid - 1, mid)) {
            return;
        }
        System.arraycopy(a, from, aux, 0, n);
        int i = 0;
        int j = mid - from;
        int k = from;
        int midOffset = mid - from;
        while (i < midOffset && j < n) {
            // 相等时取左侧，保证稳定性
            if (gt(aux, i, j)) {
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
    static void quick(short[] a, int from, int to) {
        quickRec(a, from, to);
    }

    /**
     * 快速排序递归实现
     */
    private static void quickRec(short[] a, int from, int to) {
        int n = to - from;
        if (n < INSERTION_THRESHOLD) {
            insertion(a, from, to);
            return;
        }
        // 三数取中：排序网络使 a[from] <= a[mid] <= a[to-1]，
        // 再把中值换到枢轴位 a[from]，规避有序/逆序输入退化
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

        short pivot = a[from];
        // Hoare 交叉指针划分：相等元素两端都前进，全相等输入在中间交叉、近似对半
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
        // 优先递归较小的一侧，限制递归深度
        if (j - from < to - (j + 1)) {
            quickRec(a, from, j);
            quickRec(a, j + 1, to);
        } else {
            quickRec(a, j + 1, to);
            quickRec(a, from, j);
        }
    }
}
