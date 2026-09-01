package org.zero.sort;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * 「按算法分类」重构后的全量矩阵式验证测试。
 * <p>
 * 覆盖维度：
 * <ul>
 *   <li>17 个比较类算法 × {Integer[]、List&lt;Integer&gt;、byte/short/int/long/float/double/char}</li>
 *   <li>整数专用算法 CountingSort / RadixSort / PigeonholeSort × byte/short/int/long/char</li>
 *   <li>浮点专用算法 BucketSort × float/double</li>
 *   <li>输入形态：随机重复（小值域）、含负值（整数）/ 全值域（char）/ NaN·±Infinity·±0.0（浮点）、
 *       已有序、逆序、全等值、空数组、单元素、两元素</li>
 *   <li>区间重载 sort(a, from, to) / sort(a, from) 与 oracle 的子区间一致性</li>
 *   <li>非法入参与 null 元素异常契约、char 无符号 16 位序、float/double 全序语义专项</li>
 * </ul>
 * oracle 一律取输入克隆后交 {@link Arrays#sort} 排序，断言统一使用 assertArrayEquals
 * （浮点无 delta 重载为位级比较：NaN 视为相等、-0.0 与 +0.0 区分，与 Arrays.sort 语义一致）。
 * 所有随机数据由固定种子 {@link #SEED} 派生，保证完全可复现。
 *
 * @author Zero
 */
class AlgorithmClassesTest {

    /** 固定随机种子，保证全部随机用例可复现 */
    private static final long SEED = 42L;

    // ==================== 区间排序动作的函数式接口 ====================

    /** int 数组三参区间排序动作：sort(a, fromIndex, toIndex) */
    @FunctionalInterface
    private interface IntRangeSort {
        void sort(int[] a, int fromIndex, int toIndex);
    }

    /** Integer 数组三参区间排序动作：sort(a, fromIndex, toIndex) */
    @FunctionalInterface
    private interface ObjRangeSort {
        void sort(Integer[] a, int fromIndex, int toIndex);
    }

    /** int 数组两参区间排序动作：sort(a, fromIndex) */
    @FunctionalInterface
    private interface IntFromSort {
        void sort(int[] a, int fromIndex);
    }

    /** Integer 数组两参区间排序动作：sort(a, fromIndex) */
    @FunctionalInterface
    private interface ObjFromSort {
        void sort(Integer[] a, int fromIndex);
    }

    // ==================== 参数源 ====================

    /** 对象路径参数源：17 个比较类算法，动作为 Integer[] 整表排序 */
    static Stream<Arguments> objectSorters() {
        return Stream.of(
                Arguments.of("BitonicSort", (Consumer<Integer[]>) BitonicSort::sort),
                Arguments.of("BubbleSort", (Consumer<Integer[]>) BubbleSort::sort),
                Arguments.of("CocktailSort", (Consumer<Integer[]>) CocktailSort::sort),
                Arguments.of("CombSort", (Consumer<Integer[]>) CombSort::sort),
                Arguments.of("CycleSort", (Consumer<Integer[]>) CycleSort::sort),
                Arguments.of("GnomeSort", (Consumer<Integer[]>) GnomeSort::sort),
                Arguments.of("HeapSort", (Consumer<Integer[]>) HeapSort::sort),
                Arguments.of("InsertionSort", (Consumer<Integer[]>) InsertionSort::sort),
                Arguments.of("MergeSort", (Consumer<Integer[]>) MergeSort::sort),
                Arguments.of("OddEvenSort", (Consumer<Integer[]>) OddEvenSort::sort),
                Arguments.of("PancakeSort", (Consumer<Integer[]>) PancakeSort::sort),
                Arguments.of("QuickSort", (Consumer<Integer[]>) QuickSort::sort),
                Arguments.of("SelectionSort", (Consumer<Integer[]>) SelectionSort::sort),
                Arguments.of("ShellSort", (Consumer<Integer[]>) ShellSort::sort),
                Arguments.of("StoogeSort", (Consumer<Integer[]>) StoogeSort::sort),
                Arguments.of("TimSort", (Consumer<Integer[]>) TimSort::sort),
                Arguments.of("TreeSort", (Consumer<Integer[]>) TreeSort::sort));
    }

    /** int 参数源：17 个比较类算法 + CountingSort / RadixSort / PigeonholeSort，共 20 个 */
    static Stream<Arguments> intSorters() {
        return Stream.of(
                Arguments.of("BitonicSort", (Consumer<int[]>) BitonicSort::sort),
                Arguments.of("BubbleSort", (Consumer<int[]>) BubbleSort::sort),
                Arguments.of("CocktailSort", (Consumer<int[]>) CocktailSort::sort),
                Arguments.of("CombSort", (Consumer<int[]>) CombSort::sort),
                Arguments.of("CountingSort", (Consumer<int[]>) CountingSort::sort),
                Arguments.of("CycleSort", (Consumer<int[]>) CycleSort::sort),
                Arguments.of("GnomeSort", (Consumer<int[]>) GnomeSort::sort),
                Arguments.of("HeapSort", (Consumer<int[]>) HeapSort::sort),
                Arguments.of("InsertionSort", (Consumer<int[]>) InsertionSort::sort),
                Arguments.of("MergeSort", (Consumer<int[]>) MergeSort::sort),
                Arguments.of("OddEvenSort", (Consumer<int[]>) OddEvenSort::sort),
                Arguments.of("PancakeSort", (Consumer<int[]>) PancakeSort::sort),
                Arguments.of("PigeonholeSort", (Consumer<int[]>) PigeonholeSort::sort),
                Arguments.of("QuickSort", (Consumer<int[]>) QuickSort::sort),
                Arguments.of("RadixSort", (Consumer<int[]>) RadixSort::sort),
                Arguments.of("SelectionSort", (Consumer<int[]>) SelectionSort::sort),
                Arguments.of("ShellSort", (Consumer<int[]>) ShellSort::sort),
                Arguments.of("StoogeSort", (Consumer<int[]>) StoogeSort::sort),
                Arguments.of("TimSort", (Consumer<int[]>) TimSort::sort),
                Arguments.of("TreeSort", (Consumer<int[]>) TreeSort::sort));
    }

    /** byte 参数源：17 个比较类算法 + CountingSort / RadixSort / PigeonholeSort，共 20 个 */
    static Stream<Arguments> byteSorters() {
        return Stream.of(
                Arguments.of("BitonicSort", (Consumer<byte[]>) BitonicSort::sort),
                Arguments.of("BubbleSort", (Consumer<byte[]>) BubbleSort::sort),
                Arguments.of("CocktailSort", (Consumer<byte[]>) CocktailSort::sort),
                Arguments.of("CombSort", (Consumer<byte[]>) CombSort::sort),
                Arguments.of("CountingSort", (Consumer<byte[]>) CountingSort::sort),
                Arguments.of("CycleSort", (Consumer<byte[]>) CycleSort::sort),
                Arguments.of("GnomeSort", (Consumer<byte[]>) GnomeSort::sort),
                Arguments.of("HeapSort", (Consumer<byte[]>) HeapSort::sort),
                Arguments.of("InsertionSort", (Consumer<byte[]>) InsertionSort::sort),
                Arguments.of("MergeSort", (Consumer<byte[]>) MergeSort::sort),
                Arguments.of("OddEvenSort", (Consumer<byte[]>) OddEvenSort::sort),
                Arguments.of("PancakeSort", (Consumer<byte[]>) PancakeSort::sort),
                Arguments.of("PigeonholeSort", (Consumer<byte[]>) PigeonholeSort::sort),
                Arguments.of("QuickSort", (Consumer<byte[]>) QuickSort::sort),
                Arguments.of("RadixSort", (Consumer<byte[]>) RadixSort::sort),
                Arguments.of("SelectionSort", (Consumer<byte[]>) SelectionSort::sort),
                Arguments.of("ShellSort", (Consumer<byte[]>) ShellSort::sort),
                Arguments.of("StoogeSort", (Consumer<byte[]>) StoogeSort::sort),
                Arguments.of("TimSort", (Consumer<byte[]>) TimSort::sort),
                Arguments.of("TreeSort", (Consumer<byte[]>) TreeSort::sort));
    }

    /** short 参数源：17 个比较类算法 + CountingSort / RadixSort / PigeonholeSort，共 20 个 */
    static Stream<Arguments> shortSorters() {
        return Stream.of(
                Arguments.of("BitonicSort", (Consumer<short[]>) BitonicSort::sort),
                Arguments.of("BubbleSort", (Consumer<short[]>) BubbleSort::sort),
                Arguments.of("CocktailSort", (Consumer<short[]>) CocktailSort::sort),
                Arguments.of("CombSort", (Consumer<short[]>) CombSort::sort),
                Arguments.of("CountingSort", (Consumer<short[]>) CountingSort::sort),
                Arguments.of("CycleSort", (Consumer<short[]>) CycleSort::sort),
                Arguments.of("GnomeSort", (Consumer<short[]>) GnomeSort::sort),
                Arguments.of("HeapSort", (Consumer<short[]>) HeapSort::sort),
                Arguments.of("InsertionSort", (Consumer<short[]>) InsertionSort::sort),
                Arguments.of("MergeSort", (Consumer<short[]>) MergeSort::sort),
                Arguments.of("OddEvenSort", (Consumer<short[]>) OddEvenSort::sort),
                Arguments.of("PancakeSort", (Consumer<short[]>) PancakeSort::sort),
                Arguments.of("PigeonholeSort", (Consumer<short[]>) PigeonholeSort::sort),
                Arguments.of("QuickSort", (Consumer<short[]>) QuickSort::sort),
                Arguments.of("RadixSort", (Consumer<short[]>) RadixSort::sort),
                Arguments.of("SelectionSort", (Consumer<short[]>) SelectionSort::sort),
                Arguments.of("ShellSort", (Consumer<short[]>) ShellSort::sort),
                Arguments.of("StoogeSort", (Consumer<short[]>) StoogeSort::sort),
                Arguments.of("TimSort", (Consumer<short[]>) TimSort::sort),
                Arguments.of("TreeSort", (Consumer<short[]>) TreeSort::sort));
    }

    /** long 参数源：17 个比较类算法 + CountingSort / RadixSort / PigeonholeSort，共 20 个 */
    static Stream<Arguments> longSorters() {
        return Stream.of(
                Arguments.of("BitonicSort", (Consumer<long[]>) BitonicSort::sort),
                Arguments.of("BubbleSort", (Consumer<long[]>) BubbleSort::sort),
                Arguments.of("CocktailSort", (Consumer<long[]>) CocktailSort::sort),
                Arguments.of("CombSort", (Consumer<long[]>) CombSort::sort),
                Arguments.of("CountingSort", (Consumer<long[]>) CountingSort::sort),
                Arguments.of("CycleSort", (Consumer<long[]>) CycleSort::sort),
                Arguments.of("GnomeSort", (Consumer<long[]>) GnomeSort::sort),
                Arguments.of("HeapSort", (Consumer<long[]>) HeapSort::sort),
                Arguments.of("InsertionSort", (Consumer<long[]>) InsertionSort::sort),
                Arguments.of("MergeSort", (Consumer<long[]>) MergeSort::sort),
                Arguments.of("OddEvenSort", (Consumer<long[]>) OddEvenSort::sort),
                Arguments.of("PancakeSort", (Consumer<long[]>) PancakeSort::sort),
                Arguments.of("PigeonholeSort", (Consumer<long[]>) PigeonholeSort::sort),
                Arguments.of("QuickSort", (Consumer<long[]>) QuickSort::sort),
                Arguments.of("RadixSort", (Consumer<long[]>) RadixSort::sort),
                Arguments.of("SelectionSort", (Consumer<long[]>) SelectionSort::sort),
                Arguments.of("ShellSort", (Consumer<long[]>) ShellSort::sort),
                Arguments.of("StoogeSort", (Consumer<long[]>) StoogeSort::sort),
                Arguments.of("TimSort", (Consumer<long[]>) TimSort::sort),
                Arguments.of("TreeSort", (Consumer<long[]>) TreeSort::sort));
    }

    /** char 参数源：17 个比较类算法 + CountingSort / RadixSort / PigeonholeSort，共 20 个 */
    static Stream<Arguments> charSorters() {
        return Stream.of(
                Arguments.of("BitonicSort", (Consumer<char[]>) BitonicSort::sort),
                Arguments.of("BubbleSort", (Consumer<char[]>) BubbleSort::sort),
                Arguments.of("CocktailSort", (Consumer<char[]>) CocktailSort::sort),
                Arguments.of("CombSort", (Consumer<char[]>) CombSort::sort),
                Arguments.of("CountingSort", (Consumer<char[]>) CountingSort::sort),
                Arguments.of("CycleSort", (Consumer<char[]>) CycleSort::sort),
                Arguments.of("GnomeSort", (Consumer<char[]>) GnomeSort::sort),
                Arguments.of("HeapSort", (Consumer<char[]>) HeapSort::sort),
                Arguments.of("InsertionSort", (Consumer<char[]>) InsertionSort::sort),
                Arguments.of("MergeSort", (Consumer<char[]>) MergeSort::sort),
                Arguments.of("OddEvenSort", (Consumer<char[]>) OddEvenSort::sort),
                Arguments.of("PancakeSort", (Consumer<char[]>) PancakeSort::sort),
                Arguments.of("PigeonholeSort", (Consumer<char[]>) PigeonholeSort::sort),
                Arguments.of("QuickSort", (Consumer<char[]>) QuickSort::sort),
                Arguments.of("RadixSort", (Consumer<char[]>) RadixSort::sort),
                Arguments.of("SelectionSort", (Consumer<char[]>) SelectionSort::sort),
                Arguments.of("ShellSort", (Consumer<char[]>) ShellSort::sort),
                Arguments.of("StoogeSort", (Consumer<char[]>) StoogeSort::sort),
                Arguments.of("TimSort", (Consumer<char[]>) TimSort::sort),
                Arguments.of("TreeSort", (Consumer<char[]>) TreeSort::sort));
    }

    /** float 参数源：17 个比较类算法 + BucketSort，共 18 个 */
    static Stream<Arguments> floatSorters() {
        return Stream.of(
                Arguments.of("BitonicSort", (Consumer<float[]>) BitonicSort::sort),
                Arguments.of("BucketSort", (Consumer<float[]>) BucketSort::sort),
                Arguments.of("BubbleSort", (Consumer<float[]>) BubbleSort::sort),
                Arguments.of("CocktailSort", (Consumer<float[]>) CocktailSort::sort),
                Arguments.of("CombSort", (Consumer<float[]>) CombSort::sort),
                Arguments.of("CycleSort", (Consumer<float[]>) CycleSort::sort),
                Arguments.of("GnomeSort", (Consumer<float[]>) GnomeSort::sort),
                Arguments.of("HeapSort", (Consumer<float[]>) HeapSort::sort),
                Arguments.of("InsertionSort", (Consumer<float[]>) InsertionSort::sort),
                Arguments.of("MergeSort", (Consumer<float[]>) MergeSort::sort),
                Arguments.of("OddEvenSort", (Consumer<float[]>) OddEvenSort::sort),
                Arguments.of("PancakeSort", (Consumer<float[]>) PancakeSort::sort),
                Arguments.of("QuickSort", (Consumer<float[]>) QuickSort::sort),
                Arguments.of("SelectionSort", (Consumer<float[]>) SelectionSort::sort),
                Arguments.of("ShellSort", (Consumer<float[]>) ShellSort::sort),
                Arguments.of("StoogeSort", (Consumer<float[]>) StoogeSort::sort),
                Arguments.of("TimSort", (Consumer<float[]>) TimSort::sort),
                Arguments.of("TreeSort", (Consumer<float[]>) TreeSort::sort));
    }

    /** double 参数源：17 个比较类算法 + BucketSort，共 18 个 */
    static Stream<Arguments> doubleSorters() {
        return Stream.of(
                Arguments.of("BitonicSort", (Consumer<double[]>) BitonicSort::sort),
                Arguments.of("BucketSort", (Consumer<double[]>) BucketSort::sort),
                Arguments.of("BubbleSort", (Consumer<double[]>) BubbleSort::sort),
                Arguments.of("CocktailSort", (Consumer<double[]>) CocktailSort::sort),
                Arguments.of("CombSort", (Consumer<double[]>) CombSort::sort),
                Arguments.of("CycleSort", (Consumer<double[]>) CycleSort::sort),
                Arguments.of("GnomeSort", (Consumer<double[]>) GnomeSort::sort),
                Arguments.of("HeapSort", (Consumer<double[]>) HeapSort::sort),
                Arguments.of("InsertionSort", (Consumer<double[]>) InsertionSort::sort),
                Arguments.of("MergeSort", (Consumer<double[]>) MergeSort::sort),
                Arguments.of("OddEvenSort", (Consumer<double[]>) OddEvenSort::sort),
                Arguments.of("PancakeSort", (Consumer<double[]>) PancakeSort::sort),
                Arguments.of("QuickSort", (Consumer<double[]>) QuickSort::sort),
                Arguments.of("SelectionSort", (Consumer<double[]>) SelectionSort::sort),
                Arguments.of("ShellSort", (Consumer<double[]>) ShellSort::sort),
                Arguments.of("StoogeSort", (Consumer<double[]>) StoogeSort::sort),
                Arguments.of("TimSort", (Consumer<double[]>) TimSort::sort),
                Arguments.of("TreeSort", (Consumer<double[]>) TreeSort::sort));
    }

    /** List 路径参数源：17 个比较类算法，动作为 List&lt;Integer&gt; 原地排序 */
    static Stream<Arguments> listSorters() {
        return Stream.of(
                Arguments.of("BitonicSort", (Consumer<List<Integer>>) BitonicSort::sort),
                Arguments.of("BubbleSort", (Consumer<List<Integer>>) BubbleSort::sort),
                Arguments.of("CocktailSort", (Consumer<List<Integer>>) CocktailSort::sort),
                Arguments.of("CombSort", (Consumer<List<Integer>>) CombSort::sort),
                Arguments.of("CycleSort", (Consumer<List<Integer>>) CycleSort::sort),
                Arguments.of("GnomeSort", (Consumer<List<Integer>>) GnomeSort::sort),
                Arguments.of("HeapSort", (Consumer<List<Integer>>) HeapSort::sort),
                Arguments.of("InsertionSort", (Consumer<List<Integer>>) InsertionSort::sort),
                Arguments.of("MergeSort", (Consumer<List<Integer>>) MergeSort::sort),
                Arguments.of("OddEvenSort", (Consumer<List<Integer>>) OddEvenSort::sort),
                Arguments.of("PancakeSort", (Consumer<List<Integer>>) PancakeSort::sort),
                Arguments.of("QuickSort", (Consumer<List<Integer>>) QuickSort::sort),
                Arguments.of("SelectionSort", (Consumer<List<Integer>>) SelectionSort::sort),
                Arguments.of("ShellSort", (Consumer<List<Integer>>) ShellSort::sort),
                Arguments.of("StoogeSort", (Consumer<List<Integer>>) StoogeSort::sort),
                Arguments.of("TimSort", (Consumer<List<Integer>>) TimSort::sort),
                Arguments.of("TreeSort", (Consumer<List<Integer>>) TreeSort::sort));
    }

    /** int[] 区间动作参数源：20 个整数算法的 sort(a, fromIndex, toIndex) */
    static Stream<Arguments> intRangeSorters() {
        return Stream.of(
                Arguments.of("BitonicSort", (IntRangeSort) BitonicSort::sort),
                Arguments.of("BubbleSort", (IntRangeSort) BubbleSort::sort),
                Arguments.of("CocktailSort", (IntRangeSort) CocktailSort::sort),
                Arguments.of("CombSort", (IntRangeSort) CombSort::sort),
                Arguments.of("CountingSort", (IntRangeSort) CountingSort::sort),
                Arguments.of("CycleSort", (IntRangeSort) CycleSort::sort),
                Arguments.of("GnomeSort", (IntRangeSort) GnomeSort::sort),
                Arguments.of("HeapSort", (IntRangeSort) HeapSort::sort),
                Arguments.of("InsertionSort", (IntRangeSort) InsertionSort::sort),
                Arguments.of("MergeSort", (IntRangeSort) MergeSort::sort),
                Arguments.of("OddEvenSort", (IntRangeSort) OddEvenSort::sort),
                Arguments.of("PancakeSort", (IntRangeSort) PancakeSort::sort),
                Arguments.of("PigeonholeSort", (IntRangeSort) PigeonholeSort::sort),
                Arguments.of("QuickSort", (IntRangeSort) QuickSort::sort),
                Arguments.of("RadixSort", (IntRangeSort) RadixSort::sort),
                Arguments.of("SelectionSort", (IntRangeSort) SelectionSort::sort),
                Arguments.of("ShellSort", (IntRangeSort) ShellSort::sort),
                Arguments.of("StoogeSort", (IntRangeSort) StoogeSort::sort),
                Arguments.of("TimSort", (IntRangeSort) TimSort::sort),
                Arguments.of("TreeSort", (IntRangeSort) TreeSort::sort));
    }

    /** Integer[] 区间动作参数源：17 个比较类算法的 sort(a, fromIndex, toIndex) */
    static Stream<Arguments> objectRangeSorters() {
        return Stream.of(
                Arguments.of("BitonicSort", (ObjRangeSort) BitonicSort::sort),
                Arguments.of("BubbleSort", (ObjRangeSort) BubbleSort::sort),
                Arguments.of("CocktailSort", (ObjRangeSort) CocktailSort::sort),
                Arguments.of("CombSort", (ObjRangeSort) CombSort::sort),
                Arguments.of("CycleSort", (ObjRangeSort) CycleSort::sort),
                Arguments.of("GnomeSort", (ObjRangeSort) GnomeSort::sort),
                Arguments.of("HeapSort", (ObjRangeSort) HeapSort::sort),
                Arguments.of("InsertionSort", (ObjRangeSort) InsertionSort::sort),
                Arguments.of("MergeSort", (ObjRangeSort) MergeSort::sort),
                Arguments.of("OddEvenSort", (ObjRangeSort) OddEvenSort::sort),
                Arguments.of("PancakeSort", (ObjRangeSort) PancakeSort::sort),
                Arguments.of("QuickSort", (ObjRangeSort) QuickSort::sort),
                Arguments.of("SelectionSort", (ObjRangeSort) SelectionSort::sort),
                Arguments.of("ShellSort", (ObjRangeSort) ShellSort::sort),
                Arguments.of("StoogeSort", (ObjRangeSort) StoogeSort::sort),
                Arguments.of("TimSort", (ObjRangeSort) TimSort::sort),
                Arguments.of("TreeSort", (ObjRangeSort) TreeSort::sort));
    }

    /** int[] 两参区间动作参数源：20 个整数算法的 sort(a, fromIndex) */
    static Stream<Arguments> intFromSorters() {
        return Stream.of(
                Arguments.of("BitonicSort", (IntFromSort) BitonicSort::sort),
                Arguments.of("BubbleSort", (IntFromSort) BubbleSort::sort),
                Arguments.of("CocktailSort", (IntFromSort) CocktailSort::sort),
                Arguments.of("CombSort", (IntFromSort) CombSort::sort),
                Arguments.of("CountingSort", (IntFromSort) CountingSort::sort),
                Arguments.of("CycleSort", (IntFromSort) CycleSort::sort),
                Arguments.of("GnomeSort", (IntFromSort) GnomeSort::sort),
                Arguments.of("HeapSort", (IntFromSort) HeapSort::sort),
                Arguments.of("InsertionSort", (IntFromSort) InsertionSort::sort),
                Arguments.of("MergeSort", (IntFromSort) MergeSort::sort),
                Arguments.of("OddEvenSort", (IntFromSort) OddEvenSort::sort),
                Arguments.of("PancakeSort", (IntFromSort) PancakeSort::sort),
                Arguments.of("PigeonholeSort", (IntFromSort) PigeonholeSort::sort),
                Arguments.of("QuickSort", (IntFromSort) QuickSort::sort),
                Arguments.of("RadixSort", (IntFromSort) RadixSort::sort),
                Arguments.of("SelectionSort", (IntFromSort) SelectionSort::sort),
                Arguments.of("ShellSort", (IntFromSort) ShellSort::sort),
                Arguments.of("StoogeSort", (IntFromSort) StoogeSort::sort),
                Arguments.of("TimSort", (IntFromSort) TimSort::sort),
                Arguments.of("TreeSort", (IntFromSort) TreeSort::sort));
    }

    /** Integer[] 两参区间动作参数源：17 个比较类算法的 sort(a, fromIndex) */
    static Stream<Arguments> objectFromSorters() {
        return Stream.of(
                Arguments.of("BitonicSort", (ObjFromSort) BitonicSort::sort),
                Arguments.of("BubbleSort", (ObjFromSort) BubbleSort::sort),
                Arguments.of("CocktailSort", (ObjFromSort) CocktailSort::sort),
                Arguments.of("CombSort", (ObjFromSort) CombSort::sort),
                Arguments.of("CycleSort", (ObjFromSort) CycleSort::sort),
                Arguments.of("GnomeSort", (ObjFromSort) GnomeSort::sort),
                Arguments.of("HeapSort", (ObjFromSort) HeapSort::sort),
                Arguments.of("InsertionSort", (ObjFromSort) InsertionSort::sort),
                Arguments.of("MergeSort", (ObjFromSort) MergeSort::sort),
                Arguments.of("OddEvenSort", (ObjFromSort) OddEvenSort::sort),
                Arguments.of("PancakeSort", (ObjFromSort) PancakeSort::sort),
                Arguments.of("QuickSort", (ObjFromSort) QuickSort::sort),
                Arguments.of("SelectionSort", (ObjFromSort) SelectionSort::sort),
                Arguments.of("ShellSort", (ObjFromSort) ShellSort::sort),
                Arguments.of("StoogeSort", (ObjFromSort) StoogeSort::sort),
                Arguments.of("TimSort", (ObjFromSort) TimSort::sort),
                Arguments.of("TreeSort", (ObjFromSort) TreeSort::sort));
    }

    // ==================== 辅助方法 ====================

    /**
     * 算法规模映射：StoogeSort 取 60（O(n^2.7) 过慢）；
     * 八个 O(n^2) 档算法取 200；其余取 1000
     */
    private static int sizeFor(String name) {
        switch (name) {
            case "StoogeSort":
                return 60;
            case "BubbleSort":
            case "SelectionSort":
            case "InsertionSort":
            case "GnomeSort":
            case "CocktailSort":
            case "OddEvenSort":
            case "CycleSort":
            case "PancakeSort":
                return 200;
            default:
                return 1000;
        }
    }

    /** 由固定种子派生算法专属 Random：同算法同盐可复现，不同算法数据互异 */
    private static Random randomFor(String algorithm, long salt) {
        return new Random(SEED + salt * 1_000_003L + algorithm.hashCode());
    }

    /** 生成 [origin, bound) 内的随机 int 数组 */
    private static int[] randomInts(Random rnd, int n, int origin, int bound) {
        int[] a = new int[n];
        int span = bound - origin;
        for (int i = 0; i < n; i++) {
            a[i] = origin + rnd.nextInt(span);
        }
        return a;
    }

    /** int[] 装箱为 Integer[] */
    private static Integer[] boxed(int[] src) {
        Integer[] dst = new Integer[src.length];
        for (int i = 0; i < src.length; i++) {
            dst[i] = src[i];
        }
        return dst;
    }

    /** int[] 转 List&lt;Integer&gt; */
    private static List<Integer> intList(int[] src) {
        List<Integer> list = new ArrayList<>(src.length);
        for (int v : src) {
            list.add(v);
        }
        return list;
    }

    /** int[] 按窄化转换复制为 byte[] */
    private static byte[] toBytes(int[] src) {
        byte[] dst = new byte[src.length];
        for (int i = 0; i < src.length; i++) {
            dst[i] = (byte) src[i];
        }
        return dst;
    }

    /** int[] 按窄化转换复制为 short[] */
    private static short[] toShorts(int[] src) {
        short[] dst = new short[src.length];
        for (int i = 0; i < src.length; i++) {
            dst[i] = (short) src[i];
        }
        return dst;
    }

    /** int[] 按宽化转换复制为 long[] */
    private static long[] toLongs(int[] src) {
        long[] dst = new long[src.length];
        for (int i = 0; i < src.length; i++) {
            dst[i] = src[i];
        }
        return dst;
    }

    /** int[]（取值 [0, 0x10000)）转换为 char[] */
    private static char[] toChars(int[] src) {
        char[] dst = new char[src.length];
        for (int i = 0; i < src.length; i++) {
            dst[i] = (char) src[i];
        }
        return dst;
    }

    /** int[] 转换为 float[] */
    private static float[] toFloats(int[] src) {
        float[] dst = new float[src.length];
        for (int i = 0; i < src.length; i++) {
            dst[i] = src[i];
        }
        return dst;
    }

    /** int[] 转换为 double[] */
    private static double[] toDoubles(int[] src) {
        double[] dst = new double[src.length];
        for (int i = 0; i < src.length; i++) {
            dst[i] = src[i];
        }
        return dst;
    }

    /** 以 Arrays.sort 为 oracle 校验 Integer[] 排序结果 */
    private static void checkInteger(Integer[] original, Consumer<Integer[]> sorter, String name) {
        Integer[] actual = original.clone();
        sorter.accept(actual);
        Integer[] expected = original.clone();
        Arrays.sort(expected);
        assertArrayEquals(expected, actual, name);
    }

    /** 以 Arrays.sort 为 oracle 校验 int[] 排序结果 */
    private static void checkInt(int[] original, Consumer<int[]> sorter, String name) {
        int[] actual = original.clone();
        sorter.accept(actual);
        int[] expected = original.clone();
        Arrays.sort(expected);
        assertArrayEquals(expected, actual, name);
    }

    /** 以 Arrays.sort 为 oracle 校验 byte[] 排序结果 */
    private static void checkByte(byte[] original, Consumer<byte[]> sorter, String name) {
        byte[] actual = original.clone();
        sorter.accept(actual);
        byte[] expected = original.clone();
        Arrays.sort(expected);
        assertArrayEquals(expected, actual, name);
    }

    /** 以 Arrays.sort 为 oracle 校验 short[] 排序结果 */
    private static void checkShort(short[] original, Consumer<short[]> sorter, String name) {
        short[] actual = original.clone();
        sorter.accept(actual);
        short[] expected = original.clone();
        Arrays.sort(expected);
        assertArrayEquals(expected, actual, name);
    }

    /** 以 Arrays.sort 为 oracle 校验 long[] 排序结果 */
    private static void checkLong(long[] original, Consumer<long[]> sorter, String name) {
        long[] actual = original.clone();
        sorter.accept(actual);
        long[] expected = original.clone();
        Arrays.sort(expected);
        assertArrayEquals(expected, actual, name);
    }

    /** 以 Arrays.sort 为 oracle 校验 char[] 排序结果 */
    private static void checkChar(char[] original, Consumer<char[]> sorter, String name) {
        char[] actual = original.clone();
        sorter.accept(actual);
        char[] expected = original.clone();
        Arrays.sort(expected);
        assertArrayEquals(expected, actual, name);
    }

    /** 以 Arrays.sort 为 oracle 校验 float[] 排序结果 */
    private static void checkFloat(float[] original, Consumer<float[]> sorter, String name) {
        float[] actual = original.clone();
        sorter.accept(actual);
        float[] expected = original.clone();
        Arrays.sort(expected);
        assertArrayEquals(expected, actual, name);
    }

    /** 以 Arrays.sort 为 oracle 校验 double[] 排序结果 */
    private static void checkDouble(double[] original, Consumer<double[]> sorter, String name) {
        double[] actual = original.clone();
        sorter.accept(actual);
        double[] expected = original.clone();
        Arrays.sort(expected);
        assertArrayEquals(expected, actual, name);
    }

    /** 以 Arrays.sort 为 oracle 校验 List&lt;Integer&gt; 原地排序结果 */
    private static void checkList(List<Integer> original, Consumer<List<Integer>> sorter, String name) {
        List<Integer> actual = new ArrayList<>(original);
        sorter.accept(actual);
        Integer[] expected = original.toArray(new Integer[0]);
        Arrays.sort(expected);
        assertArrayEquals(expected, actual.toArray(new Integer[0]), name);
    }

    // ==================== 矩阵用例：Integer[] ====================

    /** 随机重复元素（小值域产生大量重复值） */
    @ParameterizedTest(name = "{0}")
    @MethodSource("objectSorters")
    void objectRandomWithDuplicates(String name, Consumer<Integer[]> sorter) {
        Random rnd = randomFor(name, 1);
        int n = sizeFor(name);
        checkInteger(boxed(randomInts(rnd, n, 0, 4)), sorter, name);
        checkInteger(boxed(randomInts(rnd, Math.max(2, n / 10), 0, 2)), sorter, name);
    }

    /** 随机含负值的 Integer 数组 */
    @ParameterizedTest(name = "{0}")
    @MethodSource("objectSorters")
    void objectRandomWithNegatives(String name, Consumer<Integer[]> sorter) {
        Random rnd = randomFor(name, 2);
        int n = sizeFor(name);
        checkInteger(boxed(randomInts(rnd, n, -1000, 1001)), sorter, name);
        checkInteger(boxed(randomInts(rnd, n / 4 + 1, -3, 4)), sorter, name);
    }

    /** 退化形态：已有序 / 逆序 / 全等值 / 空 / 单元素 / 两元素 */
    @ParameterizedTest(name = "{0}")
    @MethodSource("objectSorters")
    void objectDegenerateShapes(String name, Consumer<Integer[]> sorter) {
        int n = sizeFor(name);
        Integer[] sorted = new Integer[n];
        Integer[] reverse = new Integer[n];
        Integer[] equal = new Integer[n];
        for (int i = 0; i < n; i++) {
            sorted[i] = i - n / 2;
            reverse[i] = n - i;
            equal[i] = 42;
        }
        checkInteger(sorted, sorter, name);
        checkInteger(reverse, sorter, name);
        checkInteger(equal, sorter, name);
        checkInteger(new Integer[0], sorter, name);
        checkInteger(new Integer[] {42}, sorter, name);
        checkInteger(new Integer[] {2, 1}, sorter, name);
        checkInteger(new Integer[] {1, 2}, sorter, name);
    }

    // ==================== 矩阵用例：int[] ====================

    /** 随机重复元素（小值域产生大量重复值） */
    @ParameterizedTest(name = "{0}")
    @MethodSource("intSorters")
    void intRandomWithDuplicates(String name, Consumer<int[]> sorter) {
        Random rnd = randomFor(name, 1);
        int n = sizeFor(name);
        checkInt(randomInts(rnd, n, 0, 4), sorter, name);
        checkInt(randomInts(rnd, Math.max(2, n / 10), 0, 2), sorter, name);
    }

    /** 随机含负值（值域 ±1000，兼顾 CountingSort/PigeonholeSort 的 2^24 值域上限） */
    @ParameterizedTest(name = "{0}")
    @MethodSource("intSorters")
    void intRandomWithNegatives(String name, Consumer<int[]> sorter) {
        Random rnd = randomFor(name, 2);
        int n = sizeFor(name);
        checkInt(randomInts(rnd, n, -1000, 1001), sorter, name);
        checkInt(randomInts(rnd, n / 4 + 1, -3, 4), sorter, name);
    }

    /** 退化形态：已有序 / 逆序 / 全等值 / 空 / 单元素 / 两元素 */
    @ParameterizedTest(name = "{0}")
    @MethodSource("intSorters")
    void intDegenerateShapes(String name, Consumer<int[]> sorter) {
        int n = sizeFor(name);
        int[] sorted = new int[n];
        int[] reverse = new int[n];
        int[] equal = new int[n];
        for (int i = 0; i < n; i++) {
            sorted[i] = i - n / 2;
            reverse[i] = n - i;
            equal[i] = 42;
        }
        checkInt(sorted, sorter, name);
        checkInt(reverse, sorter, name);
        checkInt(equal, sorter, name);
        checkInt(new int[0], sorter, name);
        checkInt(new int[] {42}, sorter, name);
        checkInt(new int[] {2, 1}, sorter, name);
        checkInt(new int[] {1, 2}, sorter, name);
    }

    // ==================== 矩阵用例：byte[] ====================

    /** 随机重复元素（小值域产生大量重复值） */
    @ParameterizedTest(name = "{0}")
    @MethodSource("byteSorters")
    void byteRandomWithDuplicates(String name, Consumer<byte[]> sorter) {
        Random rnd = randomFor(name, 1);
        int n = sizeFor(name);
        checkByte(toBytes(randomInts(rnd, n, 0, 4)), sorter, name);
        checkByte(toBytes(randomInts(rnd, Math.max(2, n / 10), 0, 2)), sorter, name);
    }

    /** 随机有符号全值域（覆盖 Byte.MIN_VALUE..Byte.MAX_VALUE） */
    @ParameterizedTest(name = "{0}")
    @MethodSource("byteSorters")
    void byteRandomWithSignedFullRange(String name, Consumer<byte[]> sorter) {
        Random rnd = randomFor(name, 2);
        int n = sizeFor(name);
        checkByte(toBytes(randomInts(rnd, n, Byte.MIN_VALUE, Byte.MAX_VALUE + 1)), sorter, name);
        checkByte(toBytes(randomInts(rnd, n / 4 + 1, Byte.MIN_VALUE, Byte.MIN_VALUE + 3)), sorter, name);
    }

    /** 退化形态：已有序 / 逆序 / 全等值 / 空 / 单元素 / 两元素 */
    @ParameterizedTest(name = "{0}")
    @MethodSource("byteSorters")
    void byteDegenerateShapes(String name, Consumer<byte[]> sorter) {
        int n = sizeFor(name);
        byte[] sorted = new byte[n];
        byte[] reverse = new byte[n];
        byte[] equal = new byte[n];
        for (int i = 0; i < n; i++) {
            sorted[i] = (byte) (i / 4 - 128);
            reverse[i] = (byte) (127 - i / 4);
            equal[i] = 42;
        }
        checkByte(sorted, sorter, name);
        checkByte(reverse, sorter, name);
        checkByte(equal, sorter, name);
        checkByte(new byte[0], sorter, name);
        checkByte(new byte[] {42}, sorter, name);
        checkByte(new byte[] {2, 1}, sorter, name);
        checkByte(new byte[] {1, 2}, sorter, name);
    }

    // ==================== 矩阵用例：short[] ====================

    /** 随机重复元素（小值域产生大量重复值） */
    @ParameterizedTest(name = "{0}")
    @MethodSource("shortSorters")
    void shortRandomWithDuplicates(String name, Consumer<short[]> sorter) {
        Random rnd = randomFor(name, 1);
        int n = sizeFor(name);
        checkShort(toShorts(randomInts(rnd, n, 0, 4)), sorter, name);
        checkShort(toShorts(randomInts(rnd, Math.max(2, n / 10), 0, 2)), sorter, name);
    }

    /** 随机有符号全值域（覆盖 Short.MIN_VALUE..Short.MAX_VALUE） */
    @ParameterizedTest(name = "{0}")
    @MethodSource("shortSorters")
    void shortRandomWithSignedFullRange(String name, Consumer<short[]> sorter) {
        Random rnd = randomFor(name, 2);
        int n = sizeFor(name);
        checkShort(toShorts(randomInts(rnd, n, Short.MIN_VALUE, Short.MAX_VALUE + 1)), sorter, name);
        checkShort(toShorts(randomInts(rnd, n / 4 + 1, Short.MIN_VALUE, Short.MIN_VALUE + 3)), sorter, name);
    }

    /** 退化形态：已有序 / 逆序 / 全等值 / 空 / 单元素 / 两元素 */
    @ParameterizedTest(name = "{0}")
    @MethodSource("shortSorters")
    void shortDegenerateShapes(String name, Consumer<short[]> sorter) {
        int n = sizeFor(name);
        short[] sorted = new short[n];
        short[] reverse = new short[n];
        short[] equal = new short[n];
        for (int i = 0; i < n; i++) {
            sorted[i] = (short) (i - n / 2);
            reverse[i] = (short) (n - i);
            equal[i] = 42;
        }
        checkShort(sorted, sorter, name);
        checkShort(reverse, sorter, name);
        checkShort(equal, sorter, name);
        checkShort(new short[0], sorter, name);
        checkShort(new short[] {42}, sorter, name);
        checkShort(new short[] {2, 1}, sorter, name);
        checkShort(new short[] {1, 2}, sorter, name);
    }

    // ==================== 矩阵用例：long[] ====================

    /** 随机重复元素（小值域产生大量重复值） */
    @ParameterizedTest(name = "{0}")
    @MethodSource("longSorters")
    void longRandomWithDuplicates(String name, Consumer<long[]> sorter) {
        Random rnd = randomFor(name, 1);
        int n = sizeFor(name);
        checkLong(toLongs(randomInts(rnd, n, 0, 4)), sorter, name);
        checkLong(toLongs(randomInts(rnd, Math.max(2, n / 10), 0, 2)), sorter, name);
    }

    /** 随机含负值（有界值域，兼顾 CountingSort/PigeonholeSort 的 2^24 值域上限） */
    @ParameterizedTest(name = "{0}")
    @MethodSource("longSorters")
    void longRandomWithNegatives(String name, Consumer<long[]> sorter) {
        Random rnd = randomFor(name, 2);
        int n = sizeFor(name);
        checkLong(toLongs(randomInts(rnd, n, -1000, 1001)), sorter, name);
        checkLong(toLongs(randomInts(rnd, n / 4 + 1, -100000, 100001)), sorter, name);
    }

    /** 退化形态：已有序 / 逆序 / 全等值 / 空 / 单元素 / 两元素 */
    @ParameterizedTest(name = "{0}")
    @MethodSource("longSorters")
    void longDegenerateShapes(String name, Consumer<long[]> sorter) {
        int n = sizeFor(name);
        long[] sorted = new long[n];
        long[] reverse = new long[n];
        long[] equal = new long[n];
        for (int i = 0; i < n; i++) {
            sorted[i] = i - n / 2;
            reverse[i] = n - i;
            equal[i] = 42L;
        }
        checkLong(sorted, sorter, name);
        checkLong(reverse, sorter, name);
        checkLong(equal, sorter, name);
        checkLong(new long[0], sorter, name);
        checkLong(new long[] {42L}, sorter, name);
        checkLong(new long[] {2L, 1L}, sorter, name);
        checkLong(new long[] {1L, 2L}, sorter, name);
    }

    // ==================== 矩阵用例：char[] ====================

    /** 随机重复元素（小值域产生大量重复值，取值从 0 起） */
    @ParameterizedTest(name = "{0}")
    @MethodSource("charSorters")
    void charRandomWithDuplicates(String name, Consumer<char[]> sorter) {
        Random rnd = randomFor(name, 1);
        int n = sizeFor(name);
        checkChar(toChars(randomInts(rnd, n, 0, 4)), sorter, name);
        checkChar(toChars(randomInts(rnd, Math.max(2, n / 10), 0, 2)), sorter, name);
    }

    /** 随机全值域 [0, 0xFFFF]，并强制掺入 '\u0000' 与 '\uFFFF' 端点值 */
    @ParameterizedTest(name = "{0}")
    @MethodSource("charSorters")
    void charRandomWithFullRange(String name, Consumer<char[]> sorter) {
        Random rnd = randomFor(name, 2);
        int n = sizeFor(name);
        char[] a = toChars(randomInts(rnd, n, 0, Character.MAX_VALUE + 1));
        a[0] = '\u0000';
        a[1] = '\uFFFF';
        a[n - 1] = '\u0000';
        checkChar(a, sorter, name);
        checkChar(toChars(randomInts(rnd, n / 2 + 1, 0, 3)), sorter, name);
    }

    /** 退化形态：已有序 / 逆序 / 全等值 / 空 / 单元素 / 两元素 */
    @ParameterizedTest(name = "{0}")
    @MethodSource("charSorters")
    void charDegenerateShapes(String name, Consumer<char[]> sorter) {
        int n = sizeFor(name);
        char[] sorted = new char[n];
        char[] reverse = new char[n];
        char[] equal = new char[n];
        for (int i = 0; i < n; i++) {
            sorted[i] = (char) i;
            reverse[i] = (char) (n - 1 - i);
            equal[i] = '中';
        }
        checkChar(sorted, sorter, name);
        checkChar(reverse, sorter, name);
        checkChar(equal, sorter, name);
        checkChar(new char[0], sorter, name);
        checkChar(new char[] {'a'}, sorter, name);
        checkChar(new char[] {'b', 'a'}, sorter, name);
        checkChar(new char[] {'a', 'b'}, sorter, name);
    }

    // ==================== 矩阵用例：float[] ====================

    /** 随机重复元素（整数值小值域产生大量重复值） */
    @ParameterizedTest(name = "{0}")
    @MethodSource("floatSorters")
    void floatRandomWithDuplicates(String name, Consumer<float[]> sorter) {
        Random rnd = randomFor(name, 1);
        int n = sizeFor(name);
        checkFloat(toFloats(randomInts(rnd, n, 0, 4)), sorter, name);
        checkFloat(toFloats(randomInts(rnd, n / 4 + 1, -3, 4)), sorter, name);
    }

    /** 含 NaN、±Infinity、±0.0 的混合随机数组（固定种子生成有限值后手动掺入特殊值） */
    @ParameterizedTest(name = "{0}")
    @MethodSource("floatSorters")
    void floatRandomWithSpecialValues(String name, Consumer<float[]> sorter) {
        Random rnd = randomFor(name, 2);
        int n = sizeFor(name);
        float[] a = new float[n];
        for (int i = 0; i < n; i++) {
            a[i] = (float) (rnd.nextDouble() * 2_000 - 1_000);
        }
        a[0] = Float.NaN;
        a[1] = Float.NEGATIVE_INFINITY;
        a[2] = Float.POSITIVE_INFINITY;
        a[3] = -0.0f;
        a[4] = 0.0f;
        a[5] = Float.NaN;
        checkFloat(a, sorter, name);
    }

    /** 退化形态：已有序 / 逆序 / 全等值 / 空 / 单元素 / 两元素 / ±0.0 对 */
    @ParameterizedTest(name = "{0}")
    @MethodSource("floatSorters")
    void floatDegenerateShapes(String name, Consumer<float[]> sorter) {
        int n = sizeFor(name);
        float[] sorted = new float[n];
        float[] reverse = new float[n];
        float[] equal = new float[n];
        for (int i = 0; i < n; i++) {
            sorted[i] = i * 1.5f - n;
            reverse[i] = n - i;
            equal[i] = 3.14f;
        }
        checkFloat(sorted, sorter, name);
        checkFloat(reverse, sorter, name);
        checkFloat(equal, sorter, name);
        checkFloat(new float[0], sorter, name);
        checkFloat(new float[] {42.5f}, sorter, name);
        checkFloat(new float[] {2.5f, -1.5f}, sorter, name);
        checkFloat(new float[] {1.5f, 2.5f}, sorter, name);
        checkFloat(new float[] {0.0f, -0.0f}, sorter, name);
    }

    // ==================== 矩阵用例：double[] ====================

    /** 随机重复元素（整数值小值域产生大量重复值） */
    @ParameterizedTest(name = "{0}")
    @MethodSource("doubleSorters")
    void doubleRandomWithDuplicates(String name, Consumer<double[]> sorter) {
        Random rnd = randomFor(name, 1);
        int n = sizeFor(name);
        checkDouble(toDoubles(randomInts(rnd, n, 0, 4)), sorter, name);
        checkDouble(toDoubles(randomInts(rnd, n / 4 + 1, -3, 4)), sorter, name);
    }

    /** 含 NaN、±Infinity、±0.0 的混合随机数组（固定种子生成有限值后手动掺入特殊值） */
    @ParameterizedTest(name = "{0}")
    @MethodSource("doubleSorters")
    void doubleRandomWithSpecialValues(String name, Consumer<double[]> sorter) {
        Random rnd = randomFor(name, 2);
        int n = sizeFor(name);
        double[] a = new double[n];
        for (int i = 0; i < n; i++) {
            a[i] = rnd.nextDouble() * 2_000 - 1_000;
        }
        a[0] = Double.NaN;
        a[1] = Double.NEGATIVE_INFINITY;
        a[2] = Double.POSITIVE_INFINITY;
        a[3] = -0.0;
        a[4] = 0.0;
        a[5] = Double.NaN;
        checkDouble(a, sorter, name);
    }

    /** 退化形态：已有序 / 逆序 / 全等值 / 空 / 单元素 / 两元素 / ±0.0 对 */
    @ParameterizedTest(name = "{0}")
    @MethodSource("doubleSorters")
    void doubleDegenerateShapes(String name, Consumer<double[]> sorter) {
        int n = sizeFor(name);
        double[] sorted = new double[n];
        double[] reverse = new double[n];
        double[] equal = new double[n];
        for (int i = 0; i < n; i++) {
            sorted[i] = i * 1.5 - n;
            reverse[i] = n - i;
            equal[i] = 3.14;
        }
        checkDouble(sorted, sorter, name);
        checkDouble(reverse, sorter, name);
        checkDouble(equal, sorter, name);
        checkDouble(new double[0], sorter, name);
        checkDouble(new double[] {42.5}, sorter, name);
        checkDouble(new double[] {2.5, -1.5}, sorter, name);
        checkDouble(new double[] {1.5, 2.5}, sorter, name);
        checkDouble(new double[] {0.0, -0.0}, sorter, name);
    }

    // ==================== 矩阵用例：List<Integer> ====================

    /** 随机重复元素（小值域产生大量重复值） */
    @ParameterizedTest(name = "{0}")
    @MethodSource("listSorters")
    void listRandomWithDuplicates(String name, Consumer<List<Integer>> sorter) {
        Random rnd = randomFor(name, 1);
        int n = sizeFor(name);
        checkList(intList(randomInts(rnd, n, 0, 4)), sorter, name);
        checkList(intList(randomInts(rnd, Math.max(2, n / 10), 0, 2)), sorter, name);
    }

    /** 随机含负 Integer 的列表 */
    @ParameterizedTest(name = "{0}")
    @MethodSource("listSorters")
    void listRandomWithNegatives(String name, Consumer<List<Integer>> sorter) {
        Random rnd = randomFor(name, 2);
        int n = sizeFor(name);
        checkList(intList(randomInts(rnd, n, -1000, 1001)), sorter, name);
        checkList(intList(randomInts(rnd, n / 4 + 1, -3, 4)), sorter, name);
    }

    /** 退化形态：已有序 / 逆序 / 全等值 / 空 / 单元素 / 两元素 */
    @ParameterizedTest(name = "{0}")
    @MethodSource("listSorters")
    void listDegenerateShapes(String name, Consumer<List<Integer>> sorter) {
        int n = sizeFor(name);
        List<Integer> sorted = new ArrayList<>(n);
        List<Integer> reverse = new ArrayList<>(n);
        List<Integer> equal = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            sorted.add(i - n / 2);
            reverse.add(n - i);
            equal.add(42);
        }
        checkList(sorted, sorter, name);
        checkList(reverse, sorter, name);
        checkList(equal, sorter, name);
        checkList(new ArrayList<Integer>(), sorter, name);
        checkList(intList(new int[] {42}), sorter, name);
        checkList(intList(new int[] {2, 1}), sorter, name);
        checkList(intList(new int[] {1, 2}), sorter, name);
    }

    // ==================== 区间用例 ====================

    /** int[] 区间排序：sort(a, from, to) 与 oracle 子区间一致，且 [0,from) 与 [to,n) 保持原值 */
    @ParameterizedTest(name = "{0}")
    @MethodSource("intRangeSorters")
    void intRangeSortMatchesOracle(String name, IntRangeSort sorter) {
        Random rnd = randomFor(name, 3);
        int n = sizeFor(name);
        int[] original = randomInts(rnd, n, -1000, 1001);
        int[][] ranges = {{0, n}, {0, n / 2}, {n / 4, 3 * n / 4}, {n / 2, n}, {n - 2, n}, {n / 3, n / 3}};
        for (int[] range : ranges) {
            int from = range[0];
            int to = range[1];
            int[] actual = original.clone();
            sorter.sort(actual, from, to);
            int[] expected = original.clone();
            Arrays.sort(expected, from, to);
            assertArrayEquals(expected, actual, name + " 区间[" + from + "," + to + ")");
            for (int i = 0; i < from; i++) {
                assertEquals(original[i], actual[i], name + " 前缀被破坏 @" + i);
            }
            for (int i = to; i < n; i++) {
                assertEquals(original[i], actual[i], name + " 后缀被破坏 @" + i);
            }
        }
    }

    /** Integer[] 区间排序：sort(a, from, to) 与 oracle 子区间一致，且区间外保持原值 */
    @ParameterizedTest(name = "{0}")
    @MethodSource("objectRangeSorters")
    void objectRangeSortMatchesOracle(String name, ObjRangeSort sorter) {
        Random rnd = randomFor(name, 3);
        int n = sizeFor(name);
        Integer[] original = boxed(randomInts(rnd, n, -1000, 1001));
        int[][] ranges = {{0, n}, {0, n / 2}, {n / 4, 3 * n / 4}, {n / 2, n}, {n - 2, n}, {n / 3, n / 3}};
        for (int[] range : ranges) {
            int from = range[0];
            int to = range[1];
            Integer[] actual = original.clone();
            sorter.sort(actual, from, to);
            Integer[] expected = original.clone();
            Arrays.sort(expected, from, to);
            assertArrayEquals(expected, actual, name + " 区间[" + from + "," + to + ")");
            for (int i = 0; i < from; i++) {
                assertEquals(original[i], actual[i], name + " 前缀被破坏 @" + i);
            }
            for (int i = to; i < n; i++) {
                assertEquals(original[i], actual[i], name + " 后缀被破坏 @" + i);
            }
        }
    }

    /** int[] 两参区间排序：sort(a, from) 等价于 sort(a, from, n)（以 oracle 子区间排序为准） */
    @ParameterizedTest(name = "{0}")
    @MethodSource("intFromSorters")
    void intFromOnlySortMatchesOracle(String name, IntFromSort sorter) {
        Random rnd = randomFor(name, 4);
        int n = sizeFor(name);
        int[] original = randomInts(rnd, n, -1000, 1001);
        int[] froms = {0, 1, n / 2, n - 1, n};
        for (int from : froms) {
            int[] actual = original.clone();
            sorter.sort(actual, from);
            int[] expected = original.clone();
            Arrays.sort(expected, from, n);
            assertArrayEquals(expected, actual, name + " from=" + from);
        }
    }

    /** Integer[] 两参区间排序：sort(a, from) 等价于 sort(a, from, n)（以 oracle 子区间排序为准） */
    @ParameterizedTest(name = "{0}")
    @MethodSource("objectFromSorters")
    void objectFromOnlySortMatchesOracle(String name, ObjFromSort sorter) {
        Random rnd = randomFor(name, 4);
        int n = sizeFor(name);
        Integer[] original = boxed(randomInts(rnd, n, -1000, 1001));
        int[] froms = {0, 1, n / 2, n - 1, n};
        for (int from : froms) {
            Integer[] actual = original.clone();
            sorter.sort(actual, from);
            Integer[] expected = original.clone();
            Arrays.sort(expected, from, n);
            assertArrayEquals(expected, actual, name + " from=" + from);
        }
    }

    // ==================== 非法入参用例 ====================

    /** BubbleSort 代表性验证：null 数组 / null List 抛 NPE，非法区间抛 IndexOutOfBoundsException */
    @Test
    void bubbleSortIllegalArguments() {
        Integer[] objs = {3, 1, 2};
        int[] ints = {3, 1, 2};
        assertThrows(NullPointerException.class, () -> BubbleSort.sort((Integer[]) null));
        assertThrows(NullPointerException.class, () -> BubbleSort.sort((Integer[]) null, 1));
        assertThrows(NullPointerException.class, () -> BubbleSort.sort((Integer[]) null, 0, 2));
        assertThrows(NullPointerException.class, () -> BubbleSort.sort((int[]) null));
        assertThrows(NullPointerException.class, () -> BubbleSort.sort((int[]) null, 0, 2));
        assertThrows(NullPointerException.class, () -> BubbleSort.sort((List<Integer>) null));
        assertThrows(IndexOutOfBoundsException.class, () -> BubbleSort.sort(objs, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> BubbleSort.sort(objs, 0, 4));
        assertThrows(IndexOutOfBoundsException.class, () -> BubbleSort.sort(objs, 2, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> BubbleSort.sort(ints, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> BubbleSort.sort(ints, 0, 4));
        assertThrows(IndexOutOfBoundsException.class, () -> BubbleSort.sort(ints, 2, 1));
    }

    /** QuickSort 代表性验证：null 数组 / null List 抛 NPE，非法区间抛 IndexOutOfBoundsException */
    @Test
    void quickSortIllegalArguments() {
        Integer[] objs = {3, 1, 2};
        int[] ints = {3, 1, 2};
        assertThrows(NullPointerException.class, () -> QuickSort.sort((Integer[]) null));
        assertThrows(NullPointerException.class, () -> QuickSort.sort((Integer[]) null, 1));
        assertThrows(NullPointerException.class, () -> QuickSort.sort((Integer[]) null, 0, 2));
        assertThrows(NullPointerException.class, () -> QuickSort.sort((int[]) null));
        assertThrows(NullPointerException.class, () -> QuickSort.sort((int[]) null, 0, 2));
        assertThrows(NullPointerException.class, () -> QuickSort.sort((List<Integer>) null));
        assertThrows(IndexOutOfBoundsException.class, () -> QuickSort.sort(objs, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> QuickSort.sort(objs, 0, 4));
        assertThrows(IndexOutOfBoundsException.class, () -> QuickSort.sort(objs, 2, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> QuickSort.sort(ints, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> QuickSort.sort(ints, 0, 4));
        assertThrows(IndexOutOfBoundsException.class, () -> QuickSort.sort(ints, 2, 1));
    }

    /** CountingSort 代表性验证（仅原始类型重载）：null 数组抛 NPE，非法区间抛 IndexOutOfBoundsException */
    @Test
    void countingSortIllegalArguments() {
        int[] ints = {3, 1, 2};
        assertThrows(NullPointerException.class, () -> CountingSort.sort((int[]) null));
        assertThrows(NullPointerException.class, () -> CountingSort.sort((int[]) null, 1));
        assertThrows(NullPointerException.class, () -> CountingSort.sort((int[]) null, 0, 2));
        assertThrows(IndexOutOfBoundsException.class, () -> CountingSort.sort(ints, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> CountingSort.sort(ints, 0, 4));
        assertThrows(IndexOutOfBoundsException.class, () -> CountingSort.sort(ints, 2, 1));
    }

    // ==================== null 元素用例 ====================

    /** Comparable 路径：整表排序遇到 null 元素必须抛 NullPointerException */
    @ParameterizedTest(name = "{0}")
    @MethodSource("objectSorters")
    void nullElementInFullSortThrowsNpe(String name, Consumer<Integer[]> sorter) {
        Integer[] a = {5, null, 7};
        assertThrows(NullPointerException.class, () -> sorter.accept(a.clone()), name);
    }

    /** Comparable 路径：排序区间 [from, to) 内含 null 元素必须抛 NullPointerException */
    @ParameterizedTest(name = "{0}")
    @MethodSource("objectRangeSorters")
    void nullElementInsideRangeThrowsNpe(String name, ObjRangeSort sorter) {
        assertThrows(NullPointerException.class,
                () -> sorter.sort(new Integer[] {5, null, 7}, 0, 3), name);
        assertThrows(NullPointerException.class,
                () -> sorter.sort(new Integer[] {5, null, 7}, 0, 2), name);
        assertThrows(NullPointerException.class,
                () -> sorter.sort(new Integer[] {5, null, 7}, 1, 3), name);
    }

    /** Comparable 路径：null 元素位于排序区间之外时不抛异常且不被触碰 */
    @ParameterizedTest(name = "{0}")
    @MethodSource("objectRangeSorters")
    void nullElementOutsideRangeNotTouched(String name, ObjRangeSort sorter) {
        Integer[] head = {5, null, 5};
        sorter.sort(head, 0, 1);
        assertArrayEquals(new Integer[] {5, null, 5}, head, name);
        Integer[] tail = {5, null, 5};
        sorter.sort(tail, 2, 3);
        assertArrayEquals(new Integer[] {5, null, 5}, tail, name);
        Integer[] last = {9, 5, null};
        sorter.sort(last, 0, 2);
        assertArrayEquals(new Integer[] {5, 9, null}, last, name);
        BubbleSort.sort(new Integer[] {5, null, 5}, 2);
    }

    // ==================== 语义专项 ====================

    /** char 无符号 16 位整数序专项：含 'a'/'Z'/'0'/'\u0000'/'\uFFFF'/'中' 等字面量，与 Arrays.sort(char[]) 一致 */
    @ParameterizedTest(name = "{0}")
    @MethodSource("charSorters")
    void charUnsignedOrderingMatchesJdk(String name, Consumer<char[]> sorter) {
        char[] literals = {'a', 'Z', '0', '\u0000', '\uFFFF', '中', 'A', 'z', '9', ' ', '\u0080', '阿'};
        checkChar(literals, sorter, name);
        char[] mixed = toChars(randomInts(randomFor(name, 5), sizeFor(name), 0, Character.MAX_VALUE + 1));
        mixed[0] = '\u0000';
        mixed[mixed.length - 1] = '\uFFFF';
        checkChar(mixed, sorter, name);
    }

    /** float 全序语义专项：排序后 NaN 全在末尾、-0.0 排在 +0.0 之前（Float.compare 相邻遍历） */
    @ParameterizedTest(name = "{0}")
    @MethodSource("floatSorters")
    void floatTotalOrderSemantics(String name, Consumer<float[]> sorter) {
        float[] a = {Float.NaN, -0.0f, 1.5f, Float.POSITIVE_INFINITY, -2.5f, 0.0f,
                Float.NEGATIVE_INFINITY, -0.0f, 0.0f, Float.NaN, 7.75f, -7.75f};
        sorter.accept(a);
        for (int i = 0; i + 1 < a.length; i++) {
            assertTrue(Float.compare(a[i], a[i + 1]) <= 0, name + " 相邻全序破坏 @" + i);
        }
        assertTrue(Float.isNaN(a[a.length - 1]), name + " NaN 未排在末尾");
    }

    /** double 全序语义专项：排序后 NaN 全在末尾、-0.0 排在 +0.0 之前（Double.compare 相邻遍历） */
    @ParameterizedTest(name = "{0}")
    @MethodSource("doubleSorters")
    void doubleTotalOrderSemantics(String name, Consumer<double[]> sorter) {
        double[] a = {Double.NaN, -0.0, 1.5, Double.POSITIVE_INFINITY, -2.5, 0.0,
                Double.NEGATIVE_INFINITY, -0.0, 0.0, Double.NaN, 7.75, -7.75};
        sorter.accept(a);
        for (int i = 0; i + 1 < a.length; i++) {
            assertTrue(Double.compare(a[i], a[i + 1]) <= 0, name + " 相邻全序破坏 @" + i);
        }
        assertTrue(Double.isNaN(a[a.length - 1]), name + " NaN 未排在末尾");
    }
}
