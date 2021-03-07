/**
 * 排序算法类
 * <p>
 * C：比较次数
 * <p>
 * M：移动次数
 *
 * @author Zero
 */
public class Sort {

    private Sort() {
    }

    /**
     * 冒泡排序算法类
     * <p>
     * <b><i>稳定</i></b> 排序算法
     * <p>
     * Cmin = n-1;
     * <p>
     * Mmin = 0;
     * <p>
     * Cmin + Mmin = n-1;
     * <p>
     * 根据大O推导法则，最好时间复杂度为O(n)
     * <p>
     * Cmax = (n-1)+(n-2)+...+2+1 = n(n-1)/2;
     * <p>
     * Mmax = 3(n-1)+(n-2)+...+2+1 =3n(n-1)/2;
     * <p>
     * Cmax + Mmax = 2n(n-1);
     * <p>
     * 根据大O推导法则，最坏时间复杂度为O(n^2)
     * <p>
     * 平均时间复杂度为<b>O(n^2)</b>
     *
     * @author Zero
     */
    public static class Bubble {
        private Bubble() {
        }

        /**
         * @param <T>        实现了Comparable接口的类型
         * @param a          待排序数组
         * @param startIndex 起始索引
         * @param endIndex   结束索引
         */
        public static <T extends Comparable<T>> void sort(T[] a, int startIndex, int endIndex) {
            check(a);
            check(a, startIndex, endIndex);

            for (int i = endIndex; i > startIndex; i--) {
                for (int j = startIndex; j < i; j++) {
                    // 如果索引j比索引j+1处的值的大
                    if (compare(a[j], a[j + 1])) {
                        // 交换索引j和索引j+1处的值
                        swap(a, j, j + 1);
                    }
                }
            }
        }

        /**
         * @param <T>        实现了Comparable接口的类型
         * @param a          待排序数组
         * @param startIndex 起始索引
         */
        public static <T extends Comparable<T>> void sortBack(T[] a, int startIndex) {
            check(a);

            sort(a, startIndex, a.length - 1);
        }

        /**
         * @param <T>      实现了Comparable接口的类型
         * @param a        待排序数组
         * @param endIndex 结束索引
         */
        public static <T extends Comparable<T>> void sortFront(T[] a, int endIndex) {
            sort(a, 0, endIndex);
        }

        /**
         * @param <T> 实现了Comparable接口的类型
         * @param a   待排序数组
         */
        public static <T extends Comparable<T>> void sort(T[] a) {
            sortBack(a, 0);
        }
    }

    /**
     * 选择排序算法类
     * <p>
     * <b><i>不稳定</i></b> 排序算法
     * <p>
     * Cmin = (n-1)+(n-2)+...+2+1 = n(n-1)/2;
     * <p>
     * Mmin = 0;
     * <p>
     * Cmin + Mmin = n(n-1)/2;
     * <p>
     * 根据大O推导法则，最好时间复杂度为O(n^2)
     * <p>
     * Cmax = (n-1)+(n-2)+...+2+1 = n(n-1)/2;
     * <p>
     * Mmax = n-1; Cmax + Mmax = (n^2+n-2)/2;
     * <p>
     * 根据大O推导法则，最坏时间复杂度为O(n^2)
     * <p>
     * 平均时间复杂度为<b>O(n^2)</b>
     *
     * @author Zero
     */
    public static class Selection {
        private Selection() {
        }

        /**
         * @param <T>        实现了Comparable接口的类型
         * @param a          待排序数组
         * @param startIndex 起始索引
         * @param endIndex   结束索引
         */
        public static <T extends Comparable<T>> void sort(T[] a, int startIndex, int endIndex) {
            check(a);
            check(a, startIndex, endIndex);

            for (int i = startIndex; i < endIndex; i++) {
                // 记录最小元素所在的索引，默认为参与排序的第一个元素所在位置
                int minIndex = i;
                for (int j = i + 1; j <= endIndex; j++) {
                    // 比较最小元素索引处的值和j索引处的值
                    if (compare(a[minIndex], a[j])) {
                        minIndex = j;
                    }
                }
                // 交换最小元素索引处的值和i索引处的值
                if (minIndex != i) {
                    swap(a, i, minIndex);
                }
            }
        }

        /**
         * @param <T>        实现了Comparable接口的类型
         * @param a          待排序数组
         * @param startIndex 起始索引
         */
        public static <T extends Comparable<T>> void sortBack(T[] a, int startIndex) {
            check(a);

            sort(a, startIndex, a.length - 1);
        }

        /**
         * @param <T>      实现了Comparable接口的类型
         * @param a        待排序数组
         * @param endIndex 结束索引
         */
        public static <T extends Comparable<T>> void sortFront(T[] a, int endIndex) {
            sort(a, 0, endIndex);
        }

        /**
         * @param <T> 实现了Comparable接口的类型
         * @param a   待排序数组
         */
        public static <T extends Comparable<T>> void sort(T[] a) {
            sortBack(a, 0);
        }
    }

    /**
     * 插入排序算法类
     * <p>
     * <b><i>稳定</b></i> 排序算法
     * <p>
     * Cmin = n-1;
     * <p>
     * Mmin = 0;
     * <p>
     * Cmin + Mmin = n-1;
     * <p>
     * 根据大O推导法则，最好时间复杂度为O(n)
     * <p>
     * Cmax = (n-1)+(n-2)+...+2+1 = n(n-1)/2;
     * <p>
     * Mmax = 3(n-1)+(n-2)+...+2+1 = 3n(n-1)/2;
     * <p>
     * Cmax + Mmax = 2n(n-1);
     * <p>
     * 根据大O推导法则，最坏时间复杂度为O(n^2)
     * <p>
     * 平均时间复杂度为<b>O(n^2)</b>
     *
     * @author Zero
     */
    public static class Insertion {
        private Insertion() {

        }

        /**
         * @param <T>        实现了Comparable接口的类型
         * @param a          待排序数组
         * @param startIndex 起始索引
         * @param endIndex   结束索引
         */
        public static <T extends Comparable<T>> void sort(T[] a, int startIndex, int endIndex) {
            check(a);
            check(a, startIndex, endIndex);

            for (int i = startIndex + 1; i <= endIndex; i++) {
                for (int j = i; j > startIndex && compare(a[j - 1], a[j]); j--) {
                    swap(a, j - 1, j);
                }
//				for (int j = i; j > startIndex; j--) {
//					if (compare(a[j - 1], a[j])) {
//						swap(a, j - 1, j);
//					} else {
//						break;
//					}
//				}
            }
        }

        /**
         * @param <T>        实现了Comparable接口的类型
         * @param a          待排序数组
         * @param startIndex 起始索引
         */
        public static <T extends Comparable<T>> void sortBack(T[] a, int startIndex) {
            check(a);

            sort(a, startIndex, a.length - 1);
        }

        /**
         * @param <T>      实现了Comparable接口的类型
         * @param a        待排序数组
         * @param endIndex 结束索引
         */
        public static <T extends Comparable<T>> void sortFront(T[] a, int endIndex) {
            sort(a, 0, endIndex);
        }

        /**
         * @param <T> 实现了Comparable接口的类型
         * @param a   待排序数组
         */
        public static <T extends Comparable<T>> void sort(T[] a) {
            sortBack(a, 0);
        }
    }

    /**
     * 希尔排序算法类
     * <p>
     * 希尔排序是插入排序改良版，又称增量排序
     * <p>
     * <b><i>不稳定</b></i> 排序算法
     * <p>
     * 希尔排序的分析是一个复杂的问题，它的时间是所取“增量”序列决定的，目前好像还没有严谨的数学分析
     * <p>
     * 时间复杂度的范围是：O(n^1.3)~O(n^2)
     * <p>
     * 平均时间复杂度大致是：<b>O(n^1.5)</b>
     *
     * @author Zero
     */
    public static class Shell {
        /**
         * 严谨增量序列的希尔排序
         *
         * @param <T>        实现了Comparable接口的类型
         * @param a          待排序数组
         * @param startIndex 起始索引
         * @param endIndex   结束索引
         */
        public static <T extends Comparable<T>> void sort(T[] a, int startIndex, int endIndex) {
            check(a);
            check(a, startIndex, endIndex);

            int increment = 1;
            while (increment < (endIndex - startIndex + 1) / 2) {
                increment = 2 * increment + 1;
            }

            // increment >= 1
            while (increment > 0) {
                for (int i = startIndex + increment; i <= endIndex; i++) {
                    for (int j = i; (j >= startIndex + increment) && compare(a[j - increment], a[j]); j -= increment) {
                        swap(a, j - increment, j);
                    }
//					for (int j = i; j >= startIndex + increment; j -= increment) {
//						if (compare(a[j - increment], a[j])) {
//							swap(a, j - increment, j);
//						} else {
//							break;
//						}
//					}
                }
                increment /= 2;
            }
        }

        /**
         * @param <T>        实现了Comparable接口的类型
         * @param a          待排序数组
         * @param startIndex 起始索引
         */
        public static <T extends Comparable<T>> void sortBack(T[] a, int startIndex) {
            check(a);

            sort(a, startIndex, a.length - 1);
        }

        /**
         * @param <T>      实现了Comparable接口的类型
         * @param a        待排序数组
         * @param endIndex 结束索引
         */
        public static <T extends Comparable<T>> void sortFront(T[] a, int endIndex) {
            sort(a, 0, endIndex);
        }

        /**
         * @param <T> 实现了Comparable接口的类型
         * @param a   待排序数组
         */
        public static <T extends Comparable<T>> void sort(T[] a) {
            sortBack(a, 0);
        }

        /**
         * 普通增量序列的希尔排序
         * <p>
         * 运行时间更长
         *
         * @param <T>        实现了Comparable接口的类型
         * @param a          待排序数组
         * @param startIndex 起始索引
         * @param endIndex   结束索引
         */
        public static <T extends Comparable<T>> void generalSort(T[] a, int startIndex, int endIndex) {
            check(a);
            check(a, startIndex, endIndex);

            for (int increment = (endIndex - startIndex + 1) / 2; increment > 0; increment /= 2) {
                for (int i = startIndex + increment; i <= endIndex; i++) {
                    for (int j = i; (j >= startIndex + increment) && compare(a[j - increment], a[j]); j -= increment) {
                        swap(a, j - increment, j);
                    }
                }
            }
        }
    }

    /**
     * 归并排序算法类
     * <p>
     * <b><i>稳定</i></b> 排序算法
     * <p>
     * 最好时间复杂度O(n)
     * <p>
     * 最坏时间复杂度O(nlogn)
     * <p>
     * 平均时间复杂度<b>O(nlogn)</b>
     *
     * @author Zero
     */
    public static class Merge {
        /**
         * @param <T>        实现了Comparable接口的类型
         * @param a          待排序数组
         * @param startIndex 起始索引
         * @param endIndex   结束索引
         */
        public static <T extends Comparable<T>> void sort(T[] a, int startIndex, int endIndex) {
            check(a);
            check(a, startIndex, endIndex);

            if (endIndex > startIndex) {
                int midIndex = startIndex + (endIndex - startIndex) / 2;
                sort(a, startIndex, midIndex);
                sort(a, midIndex + 1, endIndex);
                merge(a, startIndex, midIndex, endIndex);
            }
        }

        /**
         * @param <T>        实现了Comparable接口的类型
         * @param a          待排序数组
         * @param startIndex 起始索引
         */
        public static <T extends Comparable<T>> void sortBack(T[] a, int startIndex) {
            check(a);

            sort(a, startIndex, a.length - 1);
        }

        /**
         * @param <T>      实现了Comparable接口的类型
         * @param a        待排序数组
         * @param endIndex 结束索引
         */
        public static <T extends Comparable<T>> void sortFront(T[] a, int endIndex) {
            sort(a, 0, endIndex);
        }

        /**
         * @param <T> 实现了Comparable接口的类型
         * @param a   待排序数组
         */
        public static <T extends Comparable<T>> void sort(T[] a) {
            sortBack(a, 0);
        }

        /**
         * @param <T>  实现了Comparable接口的类型
         * @param a    待排序数组
         * @param low  起始位置
         * @param mid  中间位置
         * @param high 结束位置
         */
        private static <T extends Comparable<T>> void merge(T[] a, int low, int mid, int high) {
            Object[] temp = new Comparable[a.length];
            // p1、p2是检测指针，i是存放指针
            int p1 = low, p2 = mid + 1, i = low;
            // 把较小的数先移到新数组中
            while (p1 <= mid && p2 <= high) {
                if (compare(a[p2], a[p1])) {
                    temp[i++] = a[p1++];
                } else {
                    temp[i++] = a[p2++];
                }
            }
            // 把左边剩余的数移入数组
            while (p1 <= mid) {
                temp[i++] = a[p1++];
            }
            // 把右边边剩余的数移入数组
            while (p2 <= high) {
                temp[i++] = a[p2++];
            }
            // 把新数组中的数覆盖a数组
            for (int j = low; j <= high; j++) {
                a[j] = (T) temp[j];
            }
        }
    }

    /**
     * 快速排序算法类
     * <p>
     * <b><i>不稳定</i></b> 排序算法
     * <p>
     * 最好时间复杂度O(nlog2n)
     * <p>
     * 最坏时间复杂度O(n^2)
     * <p>
     * 平均时间复杂度<b>O(nlog2n)</b>
     *
     * @author Zero
     */
    public static class Quick {
        /**
         * @param <T>        实现了Comparable接口的类型
         * @param a          待排序数组
         * @param startIndex 起始索引
         * @param endIndex   结束索引
         */
        public static <T extends Comparable<T>> void sort(T[] a, int startIndex, int endIndex) {
            check(a);
            check(a, startIndex, endIndex);

            if (startIndex < endIndex) {
                int pivot = partition(a, startIndex, endIndex);
                if (pivot - 1 > startIndex) {
                    sort(a, startIndex, pivot - 1);
                }
                if (pivot + 1 < endIndex) {
                    sort(a, pivot + 1, endIndex);
                }
            }
        }

        /**
         * @param <T>        实现了Comparable接口的类型
         * @param a          待排序数组
         * @param startIndex 起始索引
         */
        public static <T extends Comparable<T>> void sortBack(T[] a, int startIndex) {
            check(a);

            sort(a, startIndex, a.length - 1);
        }

        /**
         * @param <T>      实现了Comparable接口的类型
         * @param a        待排序数组
         * @param endIndex 结束索引
         */
        public static <T extends Comparable<T>> void sortFront(T[] a, int endIndex) {
            sort(a, 0, endIndex);
        }

        /**
         * @param <T> 实现了Comparable接口的类型
         * @param a   待排序数组
         */
        public static <T extends Comparable<T>> void sort(T[] a) {
            sortBack(a, 0);
        }

        /**
         * @param <T>  实现了Comparable接口的类型
         * @param a    待排序数组
         * @param low  开始位置
         * @param high 终止位置
         * @return 中间元素所在位置
         */
        private static <T extends Comparable<T>> int partition(T[] a, int low, int high) {
            T key = a[low];
            while (low < high) {
                // 当队尾的元素大于等于基准数据时，向后挪动high指针
                while (low < high && compare(a[high], key)) {
                    high--;
                }
                // 当队尾元素小于key时，需要将其赋值给a[low]
                a[low] = a[high];
                // 当队首元素小于等于tmp时，向前挪动low指针
                while (low < high && compare(key, a[low])) {
                    low++;
                }
                // 当队首元素大于key时，需要将其赋值给a[high]
                a[high] = a[low];
            }
            // low位置的值并不是key，所以需要将key赋值给a[low]
            a[low] = key;
            // 跳出循环时low和high相等，此时的low或high就是tmp的正确索引位置
            return low;

//            while (low < high) {
//                while (low < high && compare(a[high], key)) {
//                    high--;
//                }
//                while (low < high && compare(key, a[low])) {
//                    low++;
//                }
//                if (a[low].equals(a[high]) && low < high) {
//                    low++;
//                } else {
//                    swap(a, low, high);
//                }
//            }
//            return low;
        }
    }

    /**
     * 比较大小
     *
     * @param <T> 实现了Comparable接口的类型
     * @param o1  元素1
     * @param o2  元素2
     * @return 如果o1比o2大或相等，返回true，反之则反
     */
    public static <T extends Comparable<T>> boolean compare(T o1, T o2) {
        return o1.compareTo(o2) >= 0;
    }

    /**
     * 交换数组a的i，j位置元素
     *
     * @param <T> 实现了Comparable接口的类型
     * @param a   待排序数组
     * @param i   位置i
     * @param j   位置j
     */
    public static <T> void swap(T[] a, int i, int j) {
        T tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    /**
     * 检查传入的数组是否为NULL或数组中是否包含NULL
     *
     * @param <T> 实现了Comparable接口的类型
     * @param a   待排序数组
     */
    private static <T> void check(T[] a) {
        if (a == null) {
            throw new IllegalArgumentException("禁止对未分配内存的数组排序！！！(数组为NULL)");
        }

        for (int i = 0; i < a.length; i++) {
            if (a[i] == null) {
                throw new IllegalArgumentException("无法对缺值数组排序！！！（数组中包含NULL）");
            }
        }
    }

    /**
     * 检查传入的参数是否符合要求
     *
     * @param <T>        实现了Comparable接口的类型
     * @param a          待排序数组
     * @param startIndex 起始索引
     * @param endIndex   结束索引
     */
    private static <T> void check(T[] a, int startIndex, int endIndex) {
        if (a.length == 0) {
            throw new IllegalArgumentException("禁止对空数组排序！！！(数组长度为0)");
        }

        if (startIndex < 0) {
            throw new IllegalArgumentException("起始索引需大于0！！！");
        }

        if (endIndex >= a.length) {
            throw new IllegalArgumentException("结束索引需小于数组长度！！！");
        }

        if (startIndex > endIndex) {
            throw new IllegalArgumentException("起始索引需小于或等于结束索引！！！");
        }
    }
}
