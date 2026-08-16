package org.zero.sort;

/**
 * 对象数组（Comparable）的六大排序算法实现
 * <p>
 * 约定：
 * <ul>
 *   <li>全部方法操作 [from, to) 左闭右开区间，入参由公共门面 {@link org.zero.sort.Sort} 完成校验；</li>
 *   <li>一律使用严格比较（&gt; / &lt;），相等元素不交换，保证稳定算法的稳定性成立；</li>
 *   <li>插入排序采用移位式，作为快速/归并排序的小数组底层实现。</li>
 * </ul>
 *
 * @author Zero
 */
final class GenericSorts {

    /** 插入排序阈值：小于该长度的区间直接使用插入排序 */
    static final int INSERTION_THRESHOLD = 16;

    private GenericSorts() {
    }

    /**
     * 冒泡排序（稳定）
     *
     * @param <T>  实现了 Comparable 的类型
     * @param a    待排序数组
     * @param from 起始索引（含）
     * @param to   结束索引（不含）
     */
    static <T extends Comparable<? super T>> void bubble(T[] a, int from, int to) {
        for (int i = to - 1; i > from; i--) {
            // 本轮无交换说明已有序，提前退出
            boolean swapped = false;
            for (int j = from; j < i; j++) {
                if (a[j].compareTo(a[j + 1]) > 0) {
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
     *
     * @param <T>  实现了 Comparable 的类型
     * @param a    待排序数组
     * @param from 起始索引（含）
     * @param to   结束索引（不含）
     */
    static <T extends Comparable<? super T>> void selection(T[] a, int from, int to) {
        for (int i = from; i < to - 1; i++) {
            int min = i;
            for (int j = i + 1; j < to; j++) {
                if (a[j].compareTo(a[min]) < 0) {
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
     *
     * @param <T>  实现了 Comparable 的类型
     * @param a    待排序数组
     * @param from 起始索引（含）
     * @param to   结束索引（不含）
     */
    static <T extends Comparable<? super T>> void insertion(T[] a, int from, int to) {
        for (int i = from + 1; i < to; i++) {
            T key = a[i];
            int j = i - 1;
            while (j >= from && a[j].compareTo(key) > 0) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }

    /**
     * 希尔排序（不稳定，Knuth 增量序列 h = 3h + 1，移位式）
     *
     * @param <T>  实现了 Comparable 的类型
     * @param a    待排序数组
     * @param from 起始索引（含）
     * @param to   结束索引（不含）
     */
    static <T extends Comparable<? super T>> void shell(T[] a, int from, int to) {
        int n = to - from;
        int h = 1;
        while (h < n / 3) {
            h = 3 * h + 1;
        }
        while (h >= 1) {
            for (int i = from + h; i < to; i++) {
                T key = a[i];
                int j = i;
                while (j - h >= from && a[j - h].compareTo(key) > 0) {
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
     *
     * @param <T>  实现了 Comparable 的类型
     * @param a    待排序数组
     * @param from 起始索引（含）
     * @param to   结束索引（不含）
     */
    static <T extends Comparable<? super T>> void merge(T[] a, int from, int to) {
        @SuppressWarnings("unchecked")
        T[] aux = (T[]) new Comparable[to - from];
        mergeRec(a, from, to, aux);
    }

    /**
     * 归并排序递归实现
     * <p>
     * aux 与 [from, to) 等长且相对 a 偏移 -from：aux 索引 = 数组索引 - from。
     */
    private static <T extends Comparable<? super T>> void mergeRec(T[] a, int from, int to, T[] aux) {
        int n = to - from;
        if (n < INSERTION_THRESHOLD) {
            insertion(a, from, to);
            return;
        }
        int mid = from + n / 2;
        mergeRec(a, from, mid, aux);
        mergeRec(a, mid, to, aux);
        // 左侧最大值 <= 右侧最小值：区间已整体有序，无需合并
        if (a[mid - 1].compareTo(a[mid]) <= 0) {
            return;
        }
        System.arraycopy(a, from, aux, 0, n);
        int i = 0;
        int j = mid - from;
        int k = from;
        int midOffset = mid - from;
        while (i < midOffset && j < n) {
            // 相等时取左侧，保证稳定性
            if (aux[j].compareTo(aux[i]) < 0) {
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
     *
     * @param <T>  实现了 Comparable 的类型
     * @param a    待排序数组
     * @param from 起始索引（含）
     * @param to   结束索引（不含）
     */
    static <T extends Comparable<? super T>> void quick(T[] a, int from, int to) {
        quickRec(a, from, to);
    }

    /**
     * 快速排序递归实现
     */
    private static <T extends Comparable<? super T>> void quickRec(T[] a, int from, int to) {
        int n = to - from;
        if (n < INSERTION_THRESHOLD) {
            insertion(a, from, to);
            return;
        }
        // 三数取中：排序网络使 a[from] <= a[mid] <= a[to-1]，
        // 再把中值换到枢轴位 a[from]，规避有序/逆序输入退化
        int mid = from + n / 2;
        if (a[mid].compareTo(a[from]) < 0) {
            swap(a, from, mid);
        }
        if (a[to - 1].compareTo(a[from]) < 0) {
            swap(a, from, to - 1);
        }
        if (a[to - 1].compareTo(a[mid]) < 0) {
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
            } while (a[i].compareTo(pivot) < 0);
            do {
                j--;
            } while (a[j].compareTo(pivot) > 0);
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

    /**
     * 交换数组 a 的 i、j 位置元素
     */
    private static <T> void swap(T[] a, int i, int j) {
        T tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }
}
