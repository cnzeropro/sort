package org.zero.sort;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Comparator 路径正确性测试。
 * <p>
 * 覆盖内容：
 * <ol>
 *   <li>{@link Sort#sort(Object[], Comparator)} 门面：逆序比较器、忽略大小写字符串比较器，
 *       与 oracle（{@code Arrays.sort(clone, cmp)}）逐元素一致；</li>
 *   <li>区间重载 {@code sort(a, fromIndex, toIndex, cmp)}：区间内与 oracle 一致，
 *       区间外元素保持原样；</li>
 *   <li>17 个比较类算法 × Comparator 参数化矩阵：固定种子的随机 {@code Integer[]}
 *       （含重复元素）用逆序比较器排序，结果与 oracle 一致；</li>
 *   <li>null Comparator → {@link NullPointerException}（{@link Sort} 与 {@link BubbleSort} 各一例）；</li>
 *   <li>泛型边界：父类实现 {@code Comparable}、子类未实现时，Comparator 路径
 *       （依赖 {@code Comparator<? super T>} 通配）可用性验证。</li>
 * </ol>
 * 随机数据使用固定种子 {@link #SEED}，保证失败可复现；断言信息带算法名便于定位。
 *
 * @author Zero
 */
class SortComparatorTest {

    /** 随机数据固定种子，保证测试可复现 */
    private static final long SEED = 42L;

    // ==================== 门面：Sort + Comparator ====================

    /**
     * Sort.sort(T[], Comparator) 逆序比较器与 oracle 一致
     */
    @Test
    void testSortWithReverseComparatorMatchesOracle() {
        Integer[] a = randomIntegers(100, 30, SEED);
        Integer[] expected = oracle(a, Comparator.reverseOrder());

        Sort.sort(a, Comparator.reverseOrder());

        assertArrayEquals(expected, a, "Sort.sort(T[],Comparator) 逆序比较器：排序结果与 oracle 不一致");
    }

    /**
     * Sort.sort(T[], Comparator) 忽略大小写字符串比较器与 oracle 一致
     */
    @Test
    void testSortStringCaseInsensitiveMatchesOracle() {
        String[] a = {"banana", "Apple", "cherry", "apple", "Banana", "CHERRY", "date", "Date", "elderberry", "Elderberry"};
        String[] expected = a.clone();
        Arrays.sort(expected, String.CASE_INSENSITIVE_ORDER);

        Sort.sort(a, String.CASE_INSENSITIVE_ORDER);

        assertArrayEquals(expected, a, "Sort.sort(T[],Comparator) 忽略大小写：排序结果与 oracle 不一致");
    }

    /**
     * 区间重载 sort(a, fromIndex, toIndex, cmp)：区间内与 oracle 一致、区间外不受影响
     */
    @Test
    void testSortRangeWithComparatorKeepsOutsideUntouched() {
        Integer[] a = {100, 99, 5, 3, 9, 1, -100, -99};
        Integer[] expected = a.clone();
        Arrays.sort(expected, 2, 6, Comparator.reverseOrder());

        Sort.sort(a, 2, 6, Comparator.reverseOrder());

        assertArrayEquals(expected, a, "Sort.sort(T[],int,int,Comparator) 区间 [2,6)：区间内与 oracle 不一致或区间外被改动");
    }

    // ==================== 17 个比较类算法 × Comparator ====================

    /**
     * 17 个比较类算法的 sort(T[], Comparator) 入口（方法引用形式，方便逐一调用）
     *
     * @return 算法名 + 对应的 sort(T[], Comparator) 调用器
     */
    static Stream<Arguments> comparisonAlgorithms() {
        return Stream.of(
                Arguments.of("BubbleSort", (BiConsumer<Integer[], Comparator<Integer>>) BubbleSort::sort),
                Arguments.of("SelectionSort", (BiConsumer<Integer[], Comparator<Integer>>) SelectionSort::sort),
                Arguments.of("InsertionSort", (BiConsumer<Integer[], Comparator<Integer>>) InsertionSort::sort),
                Arguments.of("ShellSort", (BiConsumer<Integer[], Comparator<Integer>>) ShellSort::sort),
                Arguments.of("MergeSort", (BiConsumer<Integer[], Comparator<Integer>>) MergeSort::sort),
                Arguments.of("QuickSort", (BiConsumer<Integer[], Comparator<Integer>>) QuickSort::sort),
                Arguments.of("HeapSort", (BiConsumer<Integer[], Comparator<Integer>>) HeapSort::sort),
                Arguments.of("TimSort", (BiConsumer<Integer[], Comparator<Integer>>) TimSort::sort),
                Arguments.of("CombSort", (BiConsumer<Integer[], Comparator<Integer>>) CombSort::sort),
                Arguments.of("GnomeSort", (BiConsumer<Integer[], Comparator<Integer>>) GnomeSort::sort),
                Arguments.of("CocktailSort", (BiConsumer<Integer[], Comparator<Integer>>) CocktailSort::sort),
                Arguments.of("CycleSort", (BiConsumer<Integer[], Comparator<Integer>>) CycleSort::sort),
                Arguments.of("OddEvenSort", (BiConsumer<Integer[], Comparator<Integer>>) OddEvenSort::sort),
                Arguments.of("PancakeSort", (BiConsumer<Integer[], Comparator<Integer>>) PancakeSort::sort),
                Arguments.of("StoogeSort", (BiConsumer<Integer[], Comparator<Integer>>) StoogeSort::sort),
                Arguments.of("BitonicSort", (BiConsumer<Integer[], Comparator<Integer>>) BitonicSort::sort),
                Arguments.of("TreeSort", (BiConsumer<Integer[], Comparator<Integer>>) TreeSort::sort));
    }

    /**
     * 17 个比较类算法 × 逆序 Comparator：随机含重复 Integer[] 与 oracle 一致
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("comparisonAlgorithms")
    void testComparisonAlgorithmsWithReverseComparator(String name,
            BiConsumer<Integer[], Comparator<Integer>> algorithm) {
        Integer[] a = randomIntegers(100, 30, SEED);
        Integer[] expected = oracle(a, Comparator.reverseOrder());

        algorithm.accept(a, Comparator.reverseOrder());

        assertArrayEquals(expected, a, name + ".sort(T[],Comparator) 逆序比较器：排序结果与 oracle 不一致");
    }

    // ==================== null Comparator ====================

    /**
     * null Comparator → NPE（Sort 门面与 BubbleSort 各一例）
     */
    @Test
    void testNullComparatorThrowsNpe() {
        Integer[] a = {3, 1, 2};

        assertThrows(NullPointerException.class, () -> Sort.sort(a, null),
                "Sort.sort(T[],Comparator) 传入 null 比较器应抛出 NullPointerException");
        assertThrows(NullPointerException.class, () -> BubbleSort.sort(a, null),
                "BubbleSort.sort(T[],Comparator) 传入 null 比较器应抛出 NullPointerException");
    }

    // ==================== 泛型边界 ====================

    /**
     * 父类实现 Comparable 的子类类型在 Comparator 路径可用（Comparator<? super T> 通配）
     */
    @Test
    void testSubclassOfComparableParentWorksOnComparatorPath() {
        Shape[] shapes = {new Circle(5), new Shape(3), new Circle(9), new Circle(3), new Shape(7)};
        Shape[] expectedShapes = shapes.clone();
        Comparator<Shape> bySizeDesc = Comparator.<Shape>comparingInt(s -> s.size).reversed();
        Arrays.sort(expectedShapes, bySizeDesc);

        Sort.sort(shapes, bySizeDesc);

        assertArrayEquals(expectedShapes, shapes, "Comparator<Shape> 作用于 Shape[]：排序结果与 oracle 不一致");

        Circle[] circles = {new Circle(5), new Circle(3), new Circle(9), new Circle(1)};
        Circle[] expectedCircles = circles.clone();
        Arrays.sort(expectedCircles, Comparator.<Shape>naturalOrder());

        Sort.sort(circles, Comparator.<Shape>naturalOrder());

        assertArrayEquals(expectedCircles, circles, "Comparator<Shape> 作用于子类数组 Circle[]：排序结果与 oracle 不一致");
    }

    // ==================== 辅助 ====================

    private static Integer[] randomIntegers(int size, int bound, long seed) {
        Random random = new Random(seed);
        Integer[] a = new Integer[size];
        for (int i = 0; i < a.length; i++) {
            a[i] = random.nextInt(bound);
        }
        return a;
    }

    private static Integer[] oracle(Integer[] a, Comparator<Integer> comparator) {
        Integer[] expected = a.clone();
        Arrays.sort(expected, comparator);
        return expected;
    }

    /** 泛型边界测试用：实现 Comparable 的父类 */
    private static class Shape implements Comparable<Shape> {

        final int size;

        Shape(int size) {
            this.size = size;
        }

        @Override
        public int compareTo(Shape o) {
            return Integer.compare(size, o.size);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "(" + size + ")";
        }
    }

    /** 泛型边界测试用：继承父类 Comparable 的子类（自身未直接实现 Comparable） */
    private static class Circle extends Shape {

        Circle(int size) {
            super(size);
        }
    }
}
