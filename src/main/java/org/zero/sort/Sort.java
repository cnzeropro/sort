package org.zero.sort;

import java.util.Comparator;
import java.util.List;

/**
 * 排序工具类
 * <p>
 * 提供 21 种排序算法（冒泡、选择、插入、希尔、归并、快速、堆、Tim、梳、地精、
 * 鸡尾酒、循环、奇偶、煎饼、臭皮匠、双调、树、计数、基数、鸽巢、桶），支持：
 * <ul>
 *   <li>任意 {@link Comparable} 对象数组 / {@link List}（含父类实现 Comparable 的场景）；</li>
 *   <li>任意对象的 {@link Comparator} 自定义比较排序（数组与 List）；</li>
 *   <li>全部数字原始类型：byte / short / int / long / float / double / char。</li>
 * </ul>
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * // 对象数组：默认 Tim 排序（自适应，与 JDK Arrays.sort(Object[]) 同款算法）
 * Integer[] a = {5, 3, 8, 1, 9};
 * Sort.sort(a);
 *
 * // 指定算法 + 区间 [1, 4)：只排 a[1]..a[3]
 * Sort.mergeSort(a, 1, 4);
 *
 * // Comparator 自定义排序
 * Sort.sort(a, Comparator.reverseOrder());
 *
 * // List
 * List<String> list = new ArrayList<>();
 * Sort.sort(list, Algorithm.HEAP);
 *
 * // 原始类型：默认自适应策略（小数组插入、近有序归并、否则快排）
 * int[] b = {5, 3, 8, 1, 9};
 * Sort.sort(b);
 * Sort.sort(b, Algorithm.RADIX, 1, 4);
 * }</pre>
 *
 * <h2>默认算法</h2>
 * <ul>
 *   <li>对象数组与 List：{@link Algorithm#TIM Tim 排序}——检测并利用已有序片段，
 *       已有序输入 O(n)、最坏 O(n log n)、稳定（与 JDK {@link java.util.Arrays#sort(Object[])}
 *       一致）；</li>
 *   <li>原始类型：自适应调度——小数组（&lt; 47）插入排序；近有序（逆序对 &lt; n/8）归并排序；
 *       否则快速排序。</li>
 * </ul>
 *
 * <h2>区间约定</h2>
 * 所有带 {@code fromIndex}/{@code toIndex} 的方法遵循 JDK 惯例：
 * 区间为 <b>[fromIndex, toIndex)</b> 左闭右开，与 {@link java.util.Arrays#sort}、
 * {@link String#substring} 一致。
 *
 * <h2>异常约定</h2>
 * 与 {@link java.util.Arrays} 一致：
 * <ul>
 *   <li>空数组/空 List 合法，排序为空操作；</li>
 *   <li>null 数组/List、Comparable 排序区间内包含 null 元素 → {@link NullPointerException}
 *       （区间外的 null 不影响；Comparator 版本允许 null 元素，由比较器自行处理）；</li>
 *   <li>非法索引（fromIndex &lt; 0、toIndex &gt; 数组长度、fromIndex &gt; toIndex）
 *       → {@link IndexOutOfBoundsException}；</li>
 *   <li>null 算法或 null 比较器 → {@link NullPointerException}；</li>
 *   <li>算法与类型不适用（如对象数组使用 {@link Algorithm#COUNTING}）→
 *       {@link IllegalArgumentException}，见 {@link Algorithm.Applicability}。</li>
 * </ul>
 *
 * <h2>比较语义</h2>
 * <ul>
 *   <li>对象：{@link Comparable#compareTo} 自然顺序或自定义 {@link Comparator}；</li>
 *   <li>float / double：使用 {@link Float#compare} / {@link Double#compare} 的全序语义——
 *       NaN 排在最后（-0.0 &lt; 0.0），与 {@link java.util.Arrays#sort(double[])} 一致；</li>
 *   <li>char：按无符号 16 位整数比较（'\\u0000' 最小，'\\uFFFF' 最大）。</li>
 * </ul>
 *
 * <h2>Java 版本与 Multi-Release</h2>
 * 最低支持 Java 8。本库以 Multi-Release JAR 打包：Java 9+ 运行时会自动加载
 * {@code META-INF/versions/9} 下的版本化类（如区间校验委托 JDK 的
 * {@code java.util.Objects.checkFromToIndex}），Java 8 使用基础实现，行为完全一致。
 *
 * @author Zero
 * @see Algorithm
 */
public final class Sort {

    private Sort() {
    }

    // ==================== 对象数组（Comparable）====================

    /**
     * 使用默认算法（{@link Algorithm#TIM Tim 排序}）对整个数组排序
     *
     * @param <T> 实现了 Comparable 的类型（允许父类实现）
     * @param a   待排序数组
     * @throws NullPointerException      数组为 null 或区间内包含 null 元素
     * @throws IndexOutOfBoundsException 区间非法
     */
    public static <T extends Comparable<? super T>> void sort(T[] a) {
        sort(a, Algorithm.TIM);
    }

    /**
     * 使用指定算法对整个数组排序
     *
     * @param <T>       实现了 Comparable 的类型（允许父类实现）
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @throws NullPointerException      数组或 algorithm 为 null，或区间内包含 null 元素
     * @throws IndexOutOfBoundsException 区间非法
     * @throws IllegalArgumentException  算法不适用于对象数组
     */
    public static <T extends Comparable<? super T>> void sort(T[] a, Algorithm algorithm) {
        sort(a, algorithm, 0, a.length);
    }

    /**
     * 使用默认算法（{@link Algorithm#TIM}）对区间 [fromIndex, toIndex) 排序
     *
     * @param <T>       实现了 Comparable 的类型（允许父类实现）
     * @param a         待排序数组
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     * @throws NullPointerException      数组为 null 或区间内包含 null 元素
     * @throws IndexOutOfBoundsException 区间非法
     */
    public static <T extends Comparable<? super T>> void sort(T[] a, int fromIndex, int toIndex) {
        sort(a, Algorithm.TIM, fromIndex, toIndex);
    }

    /**
     * 使用指定算法对区间 [fromIndex, toIndex) 排序
     *
     * @param <T>       实现了 Comparable 的类型（允许父类实现）
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     * @throws NullPointerException      数组或 algorithm 为 null，或区间内包含 null 元素
     * @throws IndexOutOfBoundsException fromIndex &lt; 0、toIndex &gt; 数组长度或 fromIndex &gt; toIndex
     * @throws IllegalArgumentException  算法不适用于对象数组
     */
    public static <T extends Comparable<? super T>> void sort(
            T[] a, Algorithm algorithm, int fromIndex, int toIndex) {
        ArrayChecks.requireArray(a);
        if (algorithm == null) {
            throw new NullPointerException("algorithm must not be null");
        }
        IndexChecks.checkFromToIndex(fromIndex, toIndex, a.length);
        ArrayChecks.requireNoNullInRange(a, fromIndex, toIndex);
        checkObjectApplicability(algorithm);
        sortValidated(a, algorithm, fromIndex, toIndex, Comparator.naturalOrder());
    }

    /**
     * 分派到具体算法实现（入参已完成校验）
     */
    private static <T> void sortValidated(
            T[] a, Algorithm algorithm, int fromIndex, int toIndex, Comparator<? super T> cmp) {
        switch (algorithm) {
            case BUBBLE:
                GenericSorts.bubble(a, fromIndex, toIndex, cmp);
                break;
            case SELECTION:
                GenericSorts.selection(a, fromIndex, toIndex, cmp);
                break;
            case INSERTION:
                GenericSorts.insertion(a, fromIndex, toIndex, cmp);
                break;
            case SHELL:
                GenericSorts.shell(a, fromIndex, toIndex, cmp);
                break;
            case MERGE:
                GenericSorts.merge(a, fromIndex, toIndex, cmp);
                break;
            case QUICK:
                GenericSorts.quick(a, fromIndex, toIndex, cmp);
                break;
            case HEAP:
                GenericSorts.heap(a, fromIndex, toIndex, cmp);
                break;
            case TIM:
                GenericSorts.tim(a, fromIndex, toIndex, cmp);
                break;
            case COMB:
                GenericSorts.comb(a, fromIndex, toIndex, cmp);
                break;
            case GNOME:
                GenericSorts.gnome(a, fromIndex, toIndex, cmp);
                break;
            case COCKTAIL:
                GenericSorts.cocktail(a, fromIndex, toIndex, cmp);
                break;
            case CYCLE:
                GenericSorts.cycle(a, fromIndex, toIndex, cmp);
                break;
            case ODD_EVEN:
                GenericSorts.oddEven(a, fromIndex, toIndex, cmp);
                break;
            case PANCAKE:
                GenericSorts.pancake(a, fromIndex, toIndex, cmp);
                break;
            case STOOGE:
                GenericSorts.stooge(a, fromIndex, toIndex, cmp);
                break;
            case BITONIC:
                GenericSorts.bitonic(a, fromIndex, toIndex, cmp);
                break;
            case TREE:
                GenericSorts.tree(a, fromIndex, toIndex, cmp);
                break;
            default:
                throw new IllegalStateException("Unknown algorithm: " + algorithm);
        }
    }

    /**
     * 校验算法是否适用于对象数组
     */
    private static void checkObjectApplicability(Algorithm algorithm) {
        if (algorithm.applicability() != Algorithm.Applicability.ALL) {
            throw new IllegalArgumentException(algorithm + " does not support object arrays");
        }
    }

    /**
     * 冒泡排序：稳定，最好 O(n)，平均/最坏 O(n^2)，空间 O(1)
     *
     * @param <T> 实现了 Comparable 的类型（允许父类实现）
     * @param a   待排序数组
     */
    public static <T extends Comparable<? super T>> void bubbleSort(T[] a) {
        sort(a, Algorithm.BUBBLE);
    }

    /**
     * 冒泡排序（区间版）：对 [fromIndex, toIndex) 排序
     *
     * @param <T>       实现了 Comparable 的类型（允许父类实现）
     * @param a         待排序数组
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     */
    public static <T extends Comparable<? super T>> void bubbleSort(T[] a, int fromIndex, int toIndex) {
        sort(a, Algorithm.BUBBLE, fromIndex, toIndex);
    }

    /**
     * 选择排序：不稳定，最好/平均/最坏均为 O(n^2)，空间 O(1)
     *
     * @param <T> 实现了 Comparable 的类型（允许父类实现）
     * @param a   待排序数组
     */
    public static <T extends Comparable<? super T>> void selectionSort(T[] a) {
        sort(a, Algorithm.SELECTION);
    }

    /**
     * 选择排序（区间版）：对 [fromIndex, toIndex) 排序
     *
     * @param <T>       实现了 Comparable 的类型（允许父类实现）
     * @param a         待排序数组
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     */
    public static <T extends Comparable<? super T>> void selectionSort(T[] a, int fromIndex, int toIndex) {
        sort(a, Algorithm.SELECTION, fromIndex, toIndex);
    }

    /**
     * 插入排序：稳定，最好 O(n)，平均/最坏 O(n^2)，空间 O(1)；适合接近有序的输入
     *
     * @param <T> 实现了 Comparable 的类型（允许父类实现）
     * @param a   待排序数组
     */
    public static <T extends Comparable<? super T>> void insertionSort(T[] a) {
        sort(a, Algorithm.INSERTION);
    }

    /**
     * 插入排序（区间版）：对 [fromIndex, toIndex) 排序
     *
     * @param <T>       实现了 Comparable 的类型（允许父类实现）
     * @param a         待排序数组
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     */
    public static <T extends Comparable<? super T>> void insertionSort(T[] a, int fromIndex, int toIndex) {
        sort(a, Algorithm.INSERTION, fromIndex, toIndex);
    }

    /**
     * 希尔排序：不稳定，Knuth 增量序列，最好 O(n log n)，平均约 O(n^1.25)，最坏 O(n^2)，空间 O(1)
     *
     * @param <T> 实现了 Comparable 的类型（允许父类实现）
     * @param a   待排序数组
     */
    public static <T extends Comparable<? super T>> void shellSort(T[] a) {
        sort(a, Algorithm.SHELL);
    }

    /**
     * 希尔排序（区间版）：对 [fromIndex, toIndex) 排序
     *
     * @param <T>       实现了 Comparable 的类型（允许父类实现）
     * @param a         待排序数组
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     */
    public static <T extends Comparable<? super T>> void shellSort(T[] a, int fromIndex, int toIndex) {
        sort(a, Algorithm.SHELL, fromIndex, toIndex);
    }

    /**
     * 归并排序：稳定，最好/平均/最坏均保证 O(n log n)，空间 O(n)
     *
     * @param <T> 实现了 Comparable 的类型（允许父类实现）
     * @param a   待排序数组
     */
    public static <T extends Comparable<? super T>> void mergeSort(T[] a) {
        sort(a, Algorithm.MERGE);
    }

    /**
     * 归并排序（区间版）：对 [fromIndex, toIndex) 排序
     *
     * @param <T>       实现了 Comparable 的类型（允许父类实现）
     * @param a         待排序数组
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     */
    public static <T extends Comparable<? super T>> void mergeSort(T[] a, int fromIndex, int toIndex) {
        sort(a, Algorithm.MERGE, fromIndex, toIndex);
    }

    /**
     * 快速排序：不稳定，双轴划分（Yaroslavskiy），最好/平均 O(n log n)，最坏 O(n^2)，空间 O(log n)
     * <p>
     * 有序、逆序、全相等输入均不退化。注意：无算法参数的入口 {@code Sort.sort(a)}
     * 使用 Tim 排序而非快速排序。
     *
     * @param <T> 实现了 Comparable 的类型（允许父类实现）
     * @param a   待排序数组
     */
    public static <T extends Comparable<? super T>> void quickSort(T[] a) {
        sort(a, Algorithm.QUICK);
    }

    /**
     * 快速排序（区间版）：对 [fromIndex, toIndex) 排序
     *
     * @param <T>       实现了 Comparable 的类型（允许父类实现）
     * @param a         待排序数组
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     */
    public static <T extends Comparable<? super T>> void quickSort(T[] a, int fromIndex, int toIndex) {
        sort(a, Algorithm.QUICK, fromIndex, toIndex);
    }

    // ==================== 对象数组（Comparator）====================

    /**
     * 使用默认算法（{@link Algorithm#TIM}）与自定义比较器对整个数组排序
     * <p>
     * 与 JDK {@link java.util.Arrays#sort(Object[], Comparator)} 一致：允许 null 元素
     * （由比较器自行处理）。
     *
     * @param <T>        数组元素类型
     * @param a          待排序数组
     * @param comparator 比较器
     * @throws NullPointerException      数组或 comparator 为 null
     * @throws IndexOutOfBoundsException 区间非法
     */
    public static <T> void sort(T[] a, Comparator<? super T> comparator) {
        sort(a, Algorithm.TIM, comparator);
    }

    /**
     * 使用指定算法与自定义比较器对整个数组排序
     * <p>
     * 允许 null 元素（由比较器自行处理）。
     *
     * @param <T>        数组元素类型
     * @param a          待排序数组
     * @param algorithm  排序算法
     * @param comparator 比较器
     * @throws NullPointerException      数组、algorithm 或 comparator 为 null
     * @throws IndexOutOfBoundsException 区间非法
     * @throws IllegalArgumentException  算法不适用于对象数组
     */
    public static <T> void sort(T[] a, Algorithm algorithm, Comparator<? super T> comparator) {
        sort(a, algorithm, comparator, 0, a.length);
    }

    /**
     * 使用默认算法（{@link Algorithm#TIM}）与自定义比较器对区间 [fromIndex, toIndex) 排序
     * <p>
     * 允许 null 元素（由比较器自行处理）。
     *
     * @param <T>        数组元素类型
     * @param a          待排序数组
     * @param comparator 比较器
     * @param fromIndex  起始索引（含）
     * @param toIndex    结束索引（不含）
     * @throws NullPointerException      数组或 comparator 为 null
     * @throws IndexOutOfBoundsException 区间非法
     */
    public static <T> void sort(T[] a, Comparator<? super T> comparator, int fromIndex, int toIndex) {
        sort(a, Algorithm.TIM, comparator, fromIndex, toIndex);
    }

    /**
     * 使用指定算法与自定义比较器对区间 [fromIndex, toIndex) 排序
     * <p>
     * 允许 null 元素（由比较器自行处理）。
     *
     * @param <T>        数组元素类型
     * @param a          待排序数组
     * @param algorithm  排序算法
     * @param comparator 比较器
     * @param fromIndex  起始索引（含）
     * @param toIndex    结束索引（不含）
     * @throws NullPointerException      数组、algorithm 或 comparator 为 null
     * @throws IndexOutOfBoundsException fromIndex &lt; 0、toIndex &gt; 数组长度或 fromIndex &gt; toIndex
     * @throws IllegalArgumentException  算法不适用于对象数组
     */
    public static <T> void sort(
            T[] a, Algorithm algorithm, Comparator<? super T> comparator, int fromIndex, int toIndex) {
        ArrayChecks.requireArray(a);
        if (algorithm == null) {
            throw new NullPointerException("algorithm must not be null");
        }
        if (comparator == null) {
            throw new NullPointerException("comparator must not be null");
        }
        IndexChecks.checkFromToIndex(fromIndex, toIndex, a.length);
        checkObjectApplicability(algorithm);
        sortValidated(a, algorithm, fromIndex, toIndex, comparator);
    }

    // ==================== List（Comparable / Comparator）====================

    /**
     * 使用默认算法（{@link Algorithm#TIM}）对整个 List 原地排序
     * <p>
     * 区间排序请使用 {@code list.subList(from, to)} 传入。不可变 List 回写时抛
     * {@link UnsupportedOperationException}（与 JDK 惯例一致）。
     *
     * @param <T>  实现了 Comparable 的类型（允许父类实现）
     * @param list 待排序 List
     * @throws NullPointerException      list 为 null 或区间内包含 null 元素
     * @throws IllegalArgumentException 算法不适用于对象
     */
    public static <T extends Comparable<? super T>> void sort(List<T> list) {
        sort(list, Algorithm.TIM);
    }

    /**
     * 使用指定算法对整个 List 原地排序
     * <p>
     * 区间排序请使用 {@code list.subList(from, to)} 传入。不可变 List 回写时抛
     * {@link UnsupportedOperationException}（与 JDK 惯例一致）。
     *
     * @param <T>       实现了 Comparable 的类型（允许父类实现）
     * @param list      待排序 List
     * @param algorithm 排序算法
     * @throws NullPointerException      list 或 algorithm 为 null，或区间内包含 null 元素
     * @throws IllegalArgumentException 算法不适用于对象
     */
    public static <T extends Comparable<? super T>> void sort(List<T> list, Algorithm algorithm) {
        if (list == null) {
            throw new NullPointerException("list must not be null");
        }
        if (algorithm == null) {
            throw new NullPointerException("algorithm must not be null");
        }
        @SuppressWarnings({"unchecked", "rawtypes"})
        T[] a = (T[]) list.toArray(new Comparable[list.size()]);
        sort(a, algorithm);
        copyBack(list, a);
    }

    /**
     * 使用默认算法（{@link Algorithm#TIM}）与自定义比较器对整个 List 原地排序
     * <p>
     * 允许 null 元素（由比较器自行处理）。不可变 List 回写时抛
     * {@link UnsupportedOperationException}。
     *
     * @param <T>        元素类型
     * @param list       待排序 List
     * @param comparator 比较器
     * @throws NullPointerException list 或 comparator 为 null
     */
    public static <T> void sort(List<T> list, Comparator<? super T> comparator) {
        sort(list, Algorithm.TIM, comparator);
    }

    /**
     * 使用指定算法与自定义比较器对整个 List 原地排序
     * <p>
     * 允许 null 元素（由比较器自行处理）。不可变 List 回写时抛
     * {@link UnsupportedOperationException}。
     *
     * @param <T>        元素类型
     * @param list       待排序 List
     * @param algorithm  排序算法
     * @param comparator 比较器
     * @throws NullPointerException      list、algorithm 或 comparator 为 null
     * @throws IllegalArgumentException 算法不适用于对象
     */
    public static <T> void sort(List<T> list, Algorithm algorithm, Comparator<? super T> comparator) {
        if (list == null) {
            throw new NullPointerException("list must not be null");
        }
        if (algorithm == null) {
            throw new NullPointerException("algorithm must not be null");
        }
        if (comparator == null) {
            throw new NullPointerException("comparator must not be null");
        }
        @SuppressWarnings("unchecked")
        T[] a = (T[]) list.toArray(new Object[list.size()]);
        sort(a, algorithm, comparator);
        copyBack(list, a);
    }

    /**
     * 把排序结果回写到 List
     */
    private static <T> void copyBack(List<T> list, T[] a) {
        for (int i = 0; i < a.length; i++) {
            list.set(i, a[i]);
        }
    }

    // ==================== 原始类型 ====================
    // 全部数字原始类型 × 4 种重载；无算法参数的入口使用自适应默认策略
    // （小数组插入、近有序归并、否则快排）；float/double 使用 compare 全序（NaN 最后），
    // char 按无符号 16 位整数比较。行为约定与对象数组版本一致。

    /**
     * 使用默认自适应策略对整个 int 数组排序
     *
     * @param a 待排序数组
     * @throws NullPointerException 数组为 null
     */
    public static void sort(int[] a) {
        sortDefault(a, 0, a.length);
    }

    /**
     * 使用指定算法对整个 int 数组排序
     *
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @throws NullPointerException      数组或 algorithm 为 null
     * @throws IndexOutOfBoundsException 区间非法
     * @throws IllegalArgumentException  算法不适用于 int 数组
     */
    public static void sort(int[] a, Algorithm algorithm) {
        sort(a, algorithm, 0, a.length);
    }

    /**
     * 使用默认自适应策略对区间 [fromIndex, toIndex) 排序
     *
     * @param a         待排序数组
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     * @throws NullPointerException      数组为 null
     * @throws IndexOutOfBoundsException 区间非法
     */
    public static void sort(int[] a, int fromIndex, int toIndex) {
        sortDefault(a, fromIndex, toIndex);
    }

    /**
     * 使用指定算法对 int 数组区间 [fromIndex, toIndex) 排序
     *
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     * @throws NullPointerException      数组或 algorithm 为 null
     * @throws IndexOutOfBoundsException fromIndex &lt; 0、toIndex &gt; 数组长度或 fromIndex &gt; toIndex
     * @throws IllegalArgumentException  算法不适用于 int 数组
     */
    public static void sort(int[] a, Algorithm algorithm, int fromIndex, int toIndex) {
        if (algorithm == null) {
            throw new NullPointerException("algorithm must not be null");
        }
        IndexChecks.checkFromToIndex(fromIndex, toIndex, a.length);
        checkPrimitiveApplicability(algorithm, false);
        PrimitiveDispatcher.sort(a, algorithm, fromIndex, toIndex);
    }

    /**
     * 自适应默认策略入口（校验后分派到各类型的 sortDefault 实现）
     */
    private static void sortDefault(int[] a, int fromIndex, int toIndex) {
        IndexChecks.checkFromToIndex(fromIndex, toIndex, a.length);
        PrimitiveDispatcher.sortDefault(a, fromIndex, toIndex);
    }

    /**
     * 使用默认自适应策略对整个 long 数组排序
     *
     * @param a 待排序数组
     * @throws NullPointerException 数组为 null
     */
    public static void sort(long[] a) {
        sortDefault(a, 0, a.length);
    }

    /**
     * 使用指定算法对整个 long 数组排序
     *
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @throws NullPointerException      数组或 algorithm 为 null
     * @throws IndexOutOfBoundsException 区间非法
     * @throws IllegalArgumentException  算法不适用于 long 数组
     */
    public static void sort(long[] a, Algorithm algorithm) {
        sort(a, algorithm, 0, a.length);
    }

    /**
     * 使用默认自适应策略对区间 [fromIndex, toIndex) 排序
     *
     * @param a         待排序数组
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     * @throws NullPointerException      数组为 null
     * @throws IndexOutOfBoundsException 区间非法
     */
    public static void sort(long[] a, int fromIndex, int toIndex) {
        sortDefault(a, fromIndex, toIndex);
    }

    /**
     * 使用指定算法对 long 数组区间 [fromIndex, toIndex) 排序
     *
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     * @throws NullPointerException      数组或 algorithm 为 null
     * @throws IndexOutOfBoundsException fromIndex &lt; 0、toIndex &gt; 数组长度或 fromIndex &gt; toIndex
     * @throws IllegalArgumentException  算法不适用于 long 数组
     */
    public static void sort(long[] a, Algorithm algorithm, int fromIndex, int toIndex) {
        if (algorithm == null) {
            throw new NullPointerException("algorithm must not be null");
        }
        IndexChecks.checkFromToIndex(fromIndex, toIndex, a.length);
        checkPrimitiveApplicability(algorithm, false);
        PrimitiveDispatcher.sort(a, algorithm, fromIndex, toIndex);
    }

    /**
     * 自适应默认策略入口（校验后分派到各类型的 sortDefault 实现）
     */
    private static void sortDefault(long[] a, int fromIndex, int toIndex) {
        IndexChecks.checkFromToIndex(fromIndex, toIndex, a.length);
        PrimitiveDispatcher.sortDefault(a, fromIndex, toIndex);
    }

    /**
     * 使用默认自适应策略对整个 double 数组排序
     * <p>
     * 使用 {@link Double#compare} 全序：NaN 排在最后，-0.0 &lt; 0.0。
     *
     * @param a 待排序数组
     * @throws NullPointerException 数组为 null
     */
    public static void sort(double[] a) {
        sortDefault(a, 0, a.length);
    }

    /**
     * 使用指定算法对整个 double 数组排序
     * <p>
     * 使用 {@link Double#compare} 全序：NaN 排在最后，-0.0 &lt; 0.0。
     *
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @throws NullPointerException      数组或 algorithm 为 null
     * @throws IndexOutOfBoundsException 区间非法
     * @throws IllegalArgumentException  算法不适用于 double 数组
     */
    public static void sort(double[] a, Algorithm algorithm) {
        sort(a, algorithm, 0, a.length);
    }

    /**
     * 使用默认自适应策略对区间 [fromIndex, toIndex) 排序
     * <p>
     * 使用 {@link Double#compare} 全序：NaN 排在最后，-0.0 &lt; 0.0。
     *
     * @param a         待排序数组
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     * @throws NullPointerException      数组为 null
     * @throws IndexOutOfBoundsException 区间非法
     */
    public static void sort(double[] a, int fromIndex, int toIndex) {
        sortDefault(a, fromIndex, toIndex);
    }

    /**
     * 使用指定算法对 double 数组区间 [fromIndex, toIndex) 排序
     * <p>
     * 使用 {@link Double#compare} 全序：NaN 排在最后，-0.0 &lt; 0.0。
     *
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     * @throws NullPointerException      数组或 algorithm 为 null
     * @throws IndexOutOfBoundsException fromIndex &lt; 0、toIndex &gt; 数组长度或 fromIndex &gt; toIndex
     * @throws IllegalArgumentException  算法不适用于 double 数组
     */
    public static void sort(double[] a, Algorithm algorithm, int fromIndex, int toIndex) {
        if (algorithm == null) {
            throw new NullPointerException("algorithm must not be null");
        }
        IndexChecks.checkFromToIndex(fromIndex, toIndex, a.length);
        checkPrimitiveApplicability(algorithm, true);
        PrimitiveDispatcher.sort(a, algorithm, fromIndex, toIndex);
    }

    /**
     * 自适应默认策略入口（校验后分派到各类型的 sortDefault 实现）
     */
    private static void sortDefault(double[] a, int fromIndex, int toIndex) {
        IndexChecks.checkFromToIndex(fromIndex, toIndex, a.length);
        PrimitiveDispatcher.sortDefault(a, fromIndex, toIndex);
    }

    /**
     * 使用默认自适应策略对整个 float 数组排序
     * <p>
     * 使用 {@link Float#compare} 全序：NaN 排在最后，-0.0 &lt; 0.0。
     *
     * @param a 待排序数组
     * @throws NullPointerException 数组为 null
     */
    public static void sort(float[] a) {
        sortDefault(a, 0, a.length);
    }

    /**
     * 使用指定算法对整个 float 数组排序
     * <p>
     * 使用 {@link Float#compare} 全序：NaN 排在最后，-0.0 &lt; 0.0。
     *
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @throws NullPointerException      数组或 algorithm 为 null
     * @throws IndexOutOfBoundsException 区间非法
     * @throws IllegalArgumentException  算法不适用于 float 数组
     */
    public static void sort(float[] a, Algorithm algorithm) {
        sort(a, algorithm, 0, a.length);
    }

    /**
     * 使用默认自适应策略对区间 [fromIndex, toIndex) 排序
     * <p>
     * 使用 {@link Float#compare} 全序：NaN 排在最后，-0.0 &lt; 0.0。
     *
     * @param a         待排序数组
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     * @throws NullPointerException      数组为 null
     * @throws IndexOutOfBoundsException 区间非法
     */
    public static void sort(float[] a, int fromIndex, int toIndex) {
        sortDefault(a, fromIndex, toIndex);
    }

    /**
     * 使用指定算法对 float 数组区间 [fromIndex, toIndex) 排序
     * <p>
     * 使用 {@link Float#compare} 全序：NaN 排在最后，-0.0 &lt; 0.0。
     *
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     * @throws NullPointerException      数组或 algorithm 为 null
     * @throws IndexOutOfBoundsException fromIndex &lt; 0、toIndex &gt; 数组长度或 fromIndex &gt; toIndex
     * @throws IllegalArgumentException  算法不适用于 float 数组
     */
    public static void sort(float[] a, Algorithm algorithm, int fromIndex, int toIndex) {
        if (algorithm == null) {
            throw new NullPointerException("algorithm must not be null");
        }
        IndexChecks.checkFromToIndex(fromIndex, toIndex, a.length);
        checkPrimitiveApplicability(algorithm, true);
        PrimitiveDispatcher.sort(a, algorithm, fromIndex, toIndex);
    }

    /**
     * 自适应默认策略入口（校验后分派到各类型的 sortDefault 实现）
     */
    private static void sortDefault(float[] a, int fromIndex, int toIndex) {
        IndexChecks.checkFromToIndex(fromIndex, toIndex, a.length);
        PrimitiveDispatcher.sortDefault(a, fromIndex, toIndex);
    }

    /**
     * 使用默认自适应策略对整个 short 数组排序
     *
     * @param a 待排序数组
     * @throws NullPointerException 数组为 null
     */
    public static void sort(short[] a) {
        sortDefault(a, 0, a.length);
    }

    /**
     * 使用指定算法对整个 short 数组排序
     *
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @throws NullPointerException      数组或 algorithm 为 null
     * @throws IndexOutOfBoundsException 区间非法
     * @throws IllegalArgumentException  算法不适用于 short 数组
     */
    public static void sort(short[] a, Algorithm algorithm) {
        sort(a, algorithm, 0, a.length);
    }

    /**
     * 使用默认自适应策略对区间 [fromIndex, toIndex) 排序
     *
     * @param a         待排序数组
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     * @throws NullPointerException      数组为 null
     * @throws IndexOutOfBoundsException 区间非法
     */
    public static void sort(short[] a, int fromIndex, int toIndex) {
        sortDefault(a, fromIndex, toIndex);
    }

    /**
     * 使用指定算法对 short 数组区间 [fromIndex, toIndex) 排序
     *
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     * @throws NullPointerException      数组或 algorithm 为 null
     * @throws IndexOutOfBoundsException fromIndex &lt; 0、toIndex &gt; 数组长度或 fromIndex &gt; toIndex
     * @throws IllegalArgumentException  算法不适用于 short 数组
     */
    public static void sort(short[] a, Algorithm algorithm, int fromIndex, int toIndex) {
        if (algorithm == null) {
            throw new NullPointerException("algorithm must not be null");
        }
        IndexChecks.checkFromToIndex(fromIndex, toIndex, a.length);
        checkPrimitiveApplicability(algorithm, false);
        PrimitiveDispatcher.sort(a, algorithm, fromIndex, toIndex);
    }

    /**
     * 自适应默认策略入口（校验后分派到各类型的 sortDefault 实现）
     */
    private static void sortDefault(short[] a, int fromIndex, int toIndex) {
        IndexChecks.checkFromToIndex(fromIndex, toIndex, a.length);
        PrimitiveDispatcher.sortDefault(a, fromIndex, toIndex);
    }

    /**
     * 使用默认自适应策略对整个 byte 数组排序
     *
     * @param a 待排序数组
     * @throws NullPointerException 数组为 null
     */
    public static void sort(byte[] a) {
        sortDefault(a, 0, a.length);
    }

    /**
     * 使用指定算法对整个 byte 数组排序
     *
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @throws NullPointerException      数组或 algorithm 为 null
     * @throws IndexOutOfBoundsException 区间非法
     * @throws IllegalArgumentException  算法不适用于 byte 数组
     */
    public static void sort(byte[] a, Algorithm algorithm) {
        sort(a, algorithm, 0, a.length);
    }

    /**
     * 使用默认自适应策略对区间 [fromIndex, toIndex) 排序
     *
     * @param a         待排序数组
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     * @throws NullPointerException      数组为 null
     * @throws IndexOutOfBoundsException 区间非法
     */
    public static void sort(byte[] a, int fromIndex, int toIndex) {
        sortDefault(a, fromIndex, toIndex);
    }

    /**
     * 使用指定算法对 byte 数组区间 [fromIndex, toIndex) 排序
     *
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     * @throws NullPointerException      数组或 algorithm 为 null
     * @throws IndexOutOfBoundsException fromIndex &lt; 0、toIndex &gt; 数组长度或 fromIndex &gt; toIndex
     * @throws IllegalArgumentException  算法不适用于 byte 数组
     */
    public static void sort(byte[] a, Algorithm algorithm, int fromIndex, int toIndex) {
        if (algorithm == null) {
            throw new NullPointerException("algorithm must not be null");
        }
        IndexChecks.checkFromToIndex(fromIndex, toIndex, a.length);
        checkPrimitiveApplicability(algorithm, false);
        PrimitiveDispatcher.sort(a, algorithm, fromIndex, toIndex);
    }

    /**
     * 自适应默认策略入口（校验后分派到各类型的 sortDefault 实现）
     */
    private static void sortDefault(byte[] a, int fromIndex, int toIndex) {
        IndexChecks.checkFromToIndex(fromIndex, toIndex, a.length);
        PrimitiveDispatcher.sortDefault(a, fromIndex, toIndex);
    }

    /**
     * 使用默认自适应策略对整个 char 数组排序
     * <p>
     * 按无符号 16 位整数比较：'\\u0000' 最小，'\\uFFFF' 最大。
     *
     * @param a 待排序数组
     * @throws NullPointerException 数组为 null
     */
    public static void sort(char[] a) {
        sortDefault(a, 0, a.length);
    }

    /**
     * 使用指定算法对整个 char 数组排序
     * <p>
     * 按无符号 16 位整数比较：'\\u0000' 最小，'\\uFFFF' 最大。
     *
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @throws NullPointerException      数组或 algorithm 为 null
     * @throws IndexOutOfBoundsException 区间非法
     * @throws IllegalArgumentException  算法不适用于 char 数组
     */
    public static void sort(char[] a, Algorithm algorithm) {
        sort(a, algorithm, 0, a.length);
    }

    /**
     * 使用默认自适应策略对区间 [fromIndex, toIndex) 排序
     * <p>
     * 按无符号 16 位整数比较：'\\u0000' 最小，'\\uFFFF' 最大。
     *
     * @param a         待排序数组
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     * @throws NullPointerException      数组为 null
     * @throws IndexOutOfBoundsException 区间非法
     */
    public static void sort(char[] a, int fromIndex, int toIndex) {
        sortDefault(a, fromIndex, toIndex);
    }

    /**
     * 使用指定算法对 char 数组区间 [fromIndex, toIndex) 排序
     * <p>
     * 按无符号 16 位整数比较：'\\u0000' 最小，'\\uFFFF' 最大。
     *
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     * @throws NullPointerException      数组或 algorithm 为 null
     * @throws IndexOutOfBoundsException fromIndex &lt; 0、toIndex &gt; 数组长度或 fromIndex &gt; toIndex
     * @throws IllegalArgumentException  算法不适用于 char 数组
     */
    public static void sort(char[] a, Algorithm algorithm, int fromIndex, int toIndex) {
        if (algorithm == null) {
            throw new NullPointerException("algorithm must not be null");
        }
        IndexChecks.checkFromToIndex(fromIndex, toIndex, a.length);
        checkPrimitiveApplicability(algorithm, false);
        PrimitiveDispatcher.sort(a, algorithm, fromIndex, toIndex);
    }

    /**
     * 自适应默认策略入口（校验后分派到各类型的 sortDefault 实现）
     */
    private static void sortDefault(char[] a, int fromIndex, int toIndex) {
        IndexChecks.checkFromToIndex(fromIndex, toIndex, a.length);
        PrimitiveDispatcher.sortDefault(a, fromIndex, toIndex);
    }

    /**
     * 校验算法是否适用于原始类型数组
     *
     * @param algorithm 排序算法
     * @param floating  目标数组是否为浮点类型（float/double）
     */
    private static void checkPrimitiveApplicability(Algorithm algorithm, boolean floating) {
        Algorithm.Applicability applicability = algorithm.applicability();
        if (applicability == Algorithm.Applicability.ALL) {
            return;
        }
        if (floating && applicability == Algorithm.Applicability.FLOATS_ONLY) {
            return;
        }
        if (!floating && applicability == Algorithm.Applicability.INTEGRALS_ONLY) {
            return;
        }
        throw new IllegalArgumentException(
                algorithm + " does not support " + (floating ? "float/double" : "integral primitive") + " arrays");
    }
}
