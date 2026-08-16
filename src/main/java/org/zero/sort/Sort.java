package org.zero.sort;


/**
 * 排序工具类
 * <p>
 * 提供六大经典排序算法（冒泡、选择、插入、希尔、归并、快速排序），支持：
 * <ul>
 *   <li>任意 {@link Comparable} 对象数组（含父类实现 Comparable 的场景）；</li>
 *   <li>全部数字原始类型：byte / short / int / long / float / double / char。</li>
 * </ul>
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * // 对象数组：默认快速排序
 * Integer[] a = {5, 3, 8, 1, 9};
 * Sort.sort(a);
 *
 * // 指定算法 + 区间 [1, 4)：只排 a[1]..a[3]
 * Sort.mergeSort(a, 1, 4);
 *
 * // 原始类型
 * int[] b = {5, 3, 8, 1, 9};
 * Sort.sort(b);                              // 默认快速排序
 * Sort.sort(b, Algorithm.SHELL, 1, 4);      // 指定算法 + 区间
 * }</pre>
 *
 * <h2>区间约定</h2>
 * 所有带 {@code fromIndex}/{@code toIndex} 的方法遵循 JDK 惯例：
 * 区间为 <b>[fromIndex, toIndex)</b> 左闭右开，与 {@link java.util.Arrays#sort}、
 * {@link String#substring} 一致。
 *
 * <h2>异常约定</h2>
 * 与 {@link java.util.Arrays} 一致：
 * <ul>
 *   <li>空数组合法，排序为空操作；</li>
 *   <li>null 数组、排序区间内包含 null 元素（对象数组）→ {@link NullPointerException}
 *       （区间外的 null 不影响）；</li>
 *   <li>非法索引（fromIndex &lt; 0、toIndex &gt; 数组长度、fromIndex &gt; toIndex）
 *       → {@link IndexOutOfBoundsException}；</li>
 *   <li>null 算法 → {@link NullPointerException}。</li>
 * </ul>
 *
 * <h2>比较语义</h2>
 * <ul>
 *   <li>对象：使用 {@link Comparable#compareTo} 的自然顺序；</li>
 *   <li>float / double：使用 {@link Float#compare} / {@link Double#compare} 的全序语义——
 *       NaN 排在最后（-0.0 &lt; 0.0），与 {@link java.util.Arrays#sort(double[])} 一致；</li>
 *   <li>char：按无符号 16 位整数比较（'\u0000' 最小，'\uFFFF' 最大）。</li>
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
     * 使用默认算法（{@link Algorithm#QUICK} 快速排序）对整个数组排序
     *
     * @param <T> 实现了 Comparable 的类型（允许父类实现）
     * @param a   待排序数组
     * @throws NullPointerException      数组为 null 或区间内包含 null 元素
     * @throws IndexOutOfBoundsException 区间非法
     */
    public static <T extends Comparable<? super T>> void sort(T[] a) {
        sort(a, Algorithm.QUICK);
    }

    /**
     * 使用指定算法对整个数组排序
     *
     * @param <T>       实现了 Comparable 的类型（允许父类实现）
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @throws NullPointerException      数组或 algorithm 为 null，或区间内包含 null 元素
     * @throws IndexOutOfBoundsException 区间非法
     */
    public static <T extends Comparable<? super T>> void sort(T[] a, Algorithm algorithm) {
        sort(a, algorithm, 0, a.length);
    }

    /**
     * 使用默认算法（{@link Algorithm#QUICK}）对区间 [fromIndex, toIndex) 排序
     *
     * @param <T>       实现了 Comparable 的类型（允许父类实现）
     * @param a         待排序数组
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     * @throws NullPointerException      数组为 null 或区间内包含 null 元素
     * @throws IndexOutOfBoundsException 区间非法
     */
    public static <T extends Comparable<? super T>> void sort(T[] a, int fromIndex, int toIndex) {
        sort(a, Algorithm.QUICK, fromIndex, toIndex);
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
     */
    public static <T extends Comparable<? super T>> void sort(
            T[] a, Algorithm algorithm, int fromIndex, int toIndex) {
        ArrayChecks.requireArray(a);
        if (algorithm == null) {
            throw new NullPointerException("algorithm must not be null");
        }
        IndexChecks.checkFromToIndex(fromIndex, toIndex, a.length);
        ArrayChecks.requireNoNullInRange(a, fromIndex, toIndex);
        sortValidated(a, algorithm, fromIndex, toIndex);
    }

    /**
     * 分派到具体算法实现（入参已完成校验）
     */
    private static <T extends Comparable<? super T>> void sortValidated(
            T[] a, Algorithm algorithm, int fromIndex, int toIndex) {
        switch (algorithm) {
            case BUBBLE:
                GenericSorts.bubble(a, fromIndex, toIndex);
                break;
            case SELECTION:
                GenericSorts.selection(a, fromIndex, toIndex);
                break;
            case INSERTION:
                GenericSorts.insertion(a, fromIndex, toIndex);
                break;
            case SHELL:
                GenericSorts.shell(a, fromIndex, toIndex);
                break;
            case MERGE:
                GenericSorts.merge(a, fromIndex, toIndex);
                break;
            case QUICK:
                GenericSorts.quick(a, fromIndex, toIndex);
                break;
            default:
                throw new IllegalStateException("Unknown algorithm: " + algorithm);
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
     * 快速排序：不稳定，三数取中选枢轴，最好/平均 O(n log n)，最坏 O(n^2)，空间 O(log n)
     * <p>
     * 有序、逆序、全相等输入均不退化，是无算法参数入口 {@code Sort.sort(a)} 使用的默认算法。
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

    // ==================== 原始类型 ====================
    // 全部数字原始类型 × 4 种重载；float/double 使用 Float.compare/Double.compare 全序（NaN 最后），
    // char 按无符号 16 位整数比较。行为约定与对象数组版本一致。

    /**
     * 使用默认算法（快速排序）对整个 int 数组排序
     *
     * @param a 待排序数组
     * @throws NullPointerException 数组为 null
     */
    public static void sort(int[] a) {
        sort(a, Algorithm.QUICK);
    }

    /**
     * 使用指定算法对整个 int 数组排序
     *
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @throws NullPointerException      数组或 algorithm 为 null
     * @throws IndexOutOfBoundsException 区间非法
     */
    public static void sort(int[] a, Algorithm algorithm) {
        sort(a, algorithm, 0, a.length);
    }

    /**
     * 使用默认算法（快速排序）对区间 [fromIndex, toIndex) 排序
     *
     * @param a         待排序数组
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     * @throws NullPointerException      数组为 null
     * @throws IndexOutOfBoundsException 区间非法
     */
    public static void sort(int[] a, int fromIndex, int toIndex) {
        sort(a, Algorithm.QUICK, fromIndex, toIndex);
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
     */
    public static void sort(int[] a, Algorithm algorithm, int fromIndex, int toIndex) {
        if (algorithm == null) {
            throw new NullPointerException("algorithm must not be null");
        }
        IndexChecks.checkFromToIndex(fromIndex, toIndex, a.length);
        PrimitiveDispatcher.sort(a, algorithm, fromIndex, toIndex);
    }

    /**
     * 使用默认算法（快速排序）对整个 long 数组排序
     *
     * @param a 待排序数组
     * @throws NullPointerException 数组为 null
     */
    public static void sort(long[] a) {
        sort(a, Algorithm.QUICK);
    }

    /**
     * 使用指定算法对整个 long 数组排序
     *
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @throws NullPointerException      数组或 algorithm 为 null
     * @throws IndexOutOfBoundsException 区间非法
     */
    public static void sort(long[] a, Algorithm algorithm) {
        sort(a, algorithm, 0, a.length);
    }

    /**
     * 使用默认算法（快速排序）对区间 [fromIndex, toIndex) 排序
     *
     * @param a         待排序数组
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     * @throws NullPointerException      数组为 null
     * @throws IndexOutOfBoundsException 区间非法
     */
    public static void sort(long[] a, int fromIndex, int toIndex) {
        sort(a, Algorithm.QUICK, fromIndex, toIndex);
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
     */
    public static void sort(long[] a, Algorithm algorithm, int fromIndex, int toIndex) {
        if (algorithm == null) {
            throw new NullPointerException("algorithm must not be null");
        }
        IndexChecks.checkFromToIndex(fromIndex, toIndex, a.length);
        PrimitiveDispatcher.sort(a, algorithm, fromIndex, toIndex);
    }

    /**
     * 使用默认算法（快速排序）对整个 double 数组排序
     * <p>
     * 使用 {@link Double#compare} 全序：NaN 排在最后，-0.0 &lt; 0.0。
     *
     * @param a 待排序数组
     * @throws NullPointerException 数组为 null
     */
    public static void sort(double[] a) {
        sort(a, Algorithm.QUICK);
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
     */
    public static void sort(double[] a, Algorithm algorithm) {
        sort(a, algorithm, 0, a.length);
    }

    /**
     * 使用默认算法（快速排序）对区间 [fromIndex, toIndex) 排序
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
        sort(a, Algorithm.QUICK, fromIndex, toIndex);
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
     */
    public static void sort(double[] a, Algorithm algorithm, int fromIndex, int toIndex) {
        if (algorithm == null) {
            throw new NullPointerException("algorithm must not be null");
        }
        IndexChecks.checkFromToIndex(fromIndex, toIndex, a.length);
        PrimitiveDispatcher.sort(a, algorithm, fromIndex, toIndex);
    }

    /**
     * 使用默认算法（快速排序）对整个 float 数组排序
     * <p>
     * 使用 {@link Float#compare} 全序：NaN 排在最后，-0.0 &lt; 0.0。
     *
     * @param a 待排序数组
     * @throws NullPointerException 数组为 null
     */
    public static void sort(float[] a) {
        sort(a, Algorithm.QUICK);
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
     */
    public static void sort(float[] a, Algorithm algorithm) {
        sort(a, algorithm, 0, a.length);
    }

    /**
     * 使用默认算法（快速排序）对区间 [fromIndex, toIndex) 排序
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
        sort(a, Algorithm.QUICK, fromIndex, toIndex);
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
     */
    public static void sort(float[] a, Algorithm algorithm, int fromIndex, int toIndex) {
        if (algorithm == null) {
            throw new NullPointerException("algorithm must not be null");
        }
        IndexChecks.checkFromToIndex(fromIndex, toIndex, a.length);
        PrimitiveDispatcher.sort(a, algorithm, fromIndex, toIndex);
    }

    /**
     * 使用默认算法（快速排序）对整个 short 数组排序
     *
     * @param a 待排序数组
     * @throws NullPointerException 数组为 null
     */
    public static void sort(short[] a) {
        sort(a, Algorithm.QUICK);
    }

    /**
     * 使用指定算法对整个 short 数组排序
     *
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @throws NullPointerException      数组或 algorithm 为 null
     * @throws IndexOutOfBoundsException 区间非法
     */
    public static void sort(short[] a, Algorithm algorithm) {
        sort(a, algorithm, 0, a.length);
    }

    /**
     * 使用默认算法（快速排序）对区间 [fromIndex, toIndex) 排序
     *
     * @param a         待排序数组
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     * @throws NullPointerException      数组为 null
     * @throws IndexOutOfBoundsException 区间非法
     */
    public static void sort(short[] a, int fromIndex, int toIndex) {
        sort(a, Algorithm.QUICK, fromIndex, toIndex);
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
     */
    public static void sort(short[] a, Algorithm algorithm, int fromIndex, int toIndex) {
        if (algorithm == null) {
            throw new NullPointerException("algorithm must not be null");
        }
        IndexChecks.checkFromToIndex(fromIndex, toIndex, a.length);
        PrimitiveDispatcher.sort(a, algorithm, fromIndex, toIndex);
    }

    /**
     * 使用默认算法（快速排序）对整个 byte 数组排序
     *
     * @param a 待排序数组
     * @throws NullPointerException 数组为 null
     */
    public static void sort(byte[] a) {
        sort(a, Algorithm.QUICK);
    }

    /**
     * 使用指定算法对整个 byte 数组排序
     *
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @throws NullPointerException      数组或 algorithm 为 null
     * @throws IndexOutOfBoundsException 区间非法
     */
    public static void sort(byte[] a, Algorithm algorithm) {
        sort(a, algorithm, 0, a.length);
    }

    /**
     * 使用默认算法（快速排序）对区间 [fromIndex, toIndex) 排序
     *
     * @param a         待排序数组
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     * @throws NullPointerException      数组为 null
     * @throws IndexOutOfBoundsException 区间非法
     */
    public static void sort(byte[] a, int fromIndex, int toIndex) {
        sort(a, Algorithm.QUICK, fromIndex, toIndex);
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
     */
    public static void sort(byte[] a, Algorithm algorithm, int fromIndex, int toIndex) {
        if (algorithm == null) {
            throw new NullPointerException("algorithm must not be null");
        }
        IndexChecks.checkFromToIndex(fromIndex, toIndex, a.length);
        PrimitiveDispatcher.sort(a, algorithm, fromIndex, toIndex);
    }

    /**
     * 使用默认算法（快速排序）对整个 char 数组排序
     * <p>
     * 按无符号 16 位整数比较：'\u0000' 最小，'\uFFFF' 最大。
     *
     * @param a 待排序数组
     * @throws NullPointerException 数组为 null
     */
    public static void sort(char[] a) {
        sort(a, Algorithm.QUICK);
    }

    /**
     * 使用指定算法对整个 char 数组排序
     * <p>
     * 按无符号 16 位整数比较：'\u0000' 最小，'\uFFFF' 最大。
     *
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @throws NullPointerException      数组或 algorithm 为 null
     * @throws IndexOutOfBoundsException 区间非法
     */
    public static void sort(char[] a, Algorithm algorithm) {
        sort(a, algorithm, 0, a.length);
    }

    /**
     * 使用默认算法（快速排序）对区间 [fromIndex, toIndex) 排序
     * <p>
     * 按无符号 16 位整数比较：'\u0000' 最小，'\uFFFF' 最大。
     *
     * @param a         待排序数组
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     * @throws NullPointerException      数组为 null
     * @throws IndexOutOfBoundsException 区间非法
     */
    public static void sort(char[] a, int fromIndex, int toIndex) {
        sort(a, Algorithm.QUICK, fromIndex, toIndex);
    }

    /**
     * 使用指定算法对 char 数组区间 [fromIndex, toIndex) 排序
     * <p>
     * 按无符号 16 位整数比较：'\u0000' 最小，'\uFFFF' 最大。
     *
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     * @throws NullPointerException      数组或 algorithm 为 null
     * @throws IndexOutOfBoundsException fromIndex &lt; 0、toIndex &gt; 数组长度或 fromIndex &gt; toIndex
     */
    public static void sort(char[] a, Algorithm algorithm, int fromIndex, int toIndex) {
        if (algorithm == null) {
            throw new NullPointerException("algorithm must not be null");
        }
        IndexChecks.checkFromToIndex(fromIndex, toIndex, a.length);
        PrimitiveDispatcher.sort(a, algorithm, fromIndex, toIndex);
    }
}
