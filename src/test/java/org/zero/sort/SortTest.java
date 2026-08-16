package org.zero.sort;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 排序算法单元测试
 * <p>
 * 每个算法均需通过以下用例：随机数组、已有序数组、逆序数组、含重复元素数组、
 * 单元素数组、双元素数组、区间排序，以及非法入参校验。
 *
 * @author Zero
 */
public class SortTest {

    /** 固定随机种子，保证测试结果可复现 */
    private static final long SEED = 42L;

    /** 区间排序函数式接口，对应各算法的 sort(T[] a, int startIndex, int endIndex) 重载 */
    @FunctionalInterface
    private interface RangeSort {
        void sort(Double[] a, int startIndex, int endIndex);
    }

    /** 全部算法（整数组排序入口） */
    static Stream<Arguments> algorithms() {
        return Stream.of(
                Arguments.of("Bubble", (Consumer<Double[]>) Sort.Bubble::sort),
                Arguments.of("Selection", (Consumer<Double[]>) Sort.Selection::sort),
                Arguments.of("Insertion", (Consumer<Double[]>) Sort.Insertion::sort),
                Arguments.of("Shell", (Consumer<Double[]>) Sort.Shell::sort),
                Arguments.of("Shell-general", (Consumer<Double[]>) Sort.Shell::generalSort),
                Arguments.of("Merge", (Consumer<Double[]>) Sort.Merge::sort),
                Arguments.of("Quick", (Consumer<Double[]>) Sort.Quick::sort));
    }

    /** 全部算法（区间排序入口） */
    static Stream<Arguments> rangeAlgorithms() {
        return Stream.of(
                Arguments.of("Bubble", (RangeSort) Sort.Bubble::sort),
                Arguments.of("Selection", (RangeSort) Sort.Selection::sort),
                Arguments.of("Insertion", (RangeSort) Sort.Insertion::sort),
                Arguments.of("Shell", (RangeSort) Sort.Shell::sort),
                Arguments.of("Shell-general", (RangeSort) Sort.Shell::generalSort),
                Arguments.of("Merge", (RangeSort) Sort.Merge::sort),
                Arguments.of("Quick", (RangeSort) Sort.Quick::sort));
    }

    /** 生成固定种子的随机数组，bound 越小重复元素越多 */
    private static Double[] randomArray(int size, int bound) {
        Double[] a = new Double[size];
        Random random = new Random(SEED);
        for (int i = 0; i < size; i++) {
            a[i] = (double) random.nextInt(bound);
        }
        return a;
    }

    /** 返回数组排序后的副本，作为期望结果 */
    private static Double[] sortedCopy(Double[] a) {
        Double[] copy = a.clone();
        Arrays.sort(copy);
        return copy;
    }

    @ParameterizedTest(name = "{0} - 随机数组")
    @MethodSource("algorithms")
    void sortsRandomArray(String name, Consumer<Double[]> sort) {
        Double[] a = randomArray(1000, 10000);
        Double[] expected = sortedCopy(a);
        sort.accept(a);
        assertArrayEquals(expected, a, name + " failed on random array");
    }

    @ParameterizedTest(name = "{0} - 已有序数组")
    @MethodSource("algorithms")
    void sortsAlreadySortedArray(String name, Consumer<Double[]> sort) {
        Double[] a = randomArray(100, 1000);
        Arrays.sort(a);
        Double[] expected = a.clone();
        sort.accept(a);
        assertArrayEquals(expected, a, name + " failed on sorted array");
    }

    @ParameterizedTest(name = "{0} - 逆序数组")
    @MethodSource("algorithms")
    void sortsReverseSortedArray(String name, Consumer<Double[]> sort) {
        Double[] a = new Double[100];
        for (int i = 0; i < a.length; i++) {
            a[i] = (double) (a.length - i);
        }
        Double[] expected = sortedCopy(a);
        sort.accept(a);
        assertArrayEquals(expected, a, name + " failed on reverse sorted array");
    }

    @ParameterizedTest(name = "{0} - 含重复元素数组")
    @MethodSource("algorithms")
    void sortsArrayWithDuplicates(String name, Consumer<Double[]> sort) {
        Double[] a = randomArray(100, 5);
        Double[] expected = sortedCopy(a);
        sort.accept(a);
        assertArrayEquals(expected, a, name + " failed on array with duplicates");
    }

    @ParameterizedTest(name = "{0} - 单元素数组")
    @MethodSource("algorithms")
    void sortsSingleElementArray(String name, Consumer<Double[]> sort) {
        Double[] a = new Double[] {7.5};
        sort.accept(a);
        assertArrayEquals(new Double[] {7.5}, a, name + " failed on single element array");
    }

    @ParameterizedTest(name = "{0} - 双元素数组")
    @MethodSource("algorithms")
    void sortsTwoElementArray(String name, Consumer<Double[]> sort) {
        Double[] a = new Double[] {9.0, 2.0};
        sort.accept(a);
        assertArrayEquals(new Double[] {2.0, 9.0}, a, name + " failed on two element array");

        Double[] b = new Double[] {2.0, 9.0};
        sort.accept(b);
        assertArrayEquals(new Double[] {2.0, 9.0}, b, name + " failed on sorted two element array");
    }

    @ParameterizedTest(name = "{0} - 区间排序")
    @MethodSource("rangeAlgorithms")
    void sortsSubrangeOnly(String name, RangeSort sort) {
        Double[] a = new Double[] {9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0};
        // 仅对 [2, 5] 区间排序，区间外元素应保持原样
        sort.sort(a, 2, 5);
        assertArrayEquals(new Double[] {9.0, 8.0, 4.0, 5.0, 6.0, 7.0, 3.0, 2.0, 1.0}, a,
                name + " failed on subrange sort");
    }

    @ParameterizedTest(name = "{0} - null 数组校验")
    @MethodSource("algorithms")
    void rejectsNullArray(String name, Consumer<Double[]> sort) {
        assertThrows(IllegalArgumentException.class, () -> sort.accept(null),
                name + " should reject null array");
    }

    @ParameterizedTest(name = "{0} - null 元素校验")
    @MethodSource("algorithms")
    void rejectsNullElement(String name, Consumer<Double[]> sort) {
        Double[] a = new Double[] {1.0, null, 3.0};
        assertThrows(IllegalArgumentException.class, () -> sort.accept(a),
                name + " should reject array containing null");
    }

    @ParameterizedTest(name = "{0} - 空数组校验")
    @MethodSource("algorithms")
    void rejectsEmptyArray(String name, Consumer<Double[]> sort) {
        Double[] a = new Double[0];
        assertThrows(IllegalArgumentException.class, () -> sort.accept(a),
                name + " should reject empty array");
    }

    @ParameterizedTest(name = "{0} - startIndex 为负校验")
    @MethodSource("rangeAlgorithms")
    void rejectsNegativeStartIndex(String name, RangeSort sort) {
        Double[] a = new Double[] {1.0, 2.0, 3.0};
        assertThrows(IllegalArgumentException.class, () -> sort.sort(a, -1, 2),
                name + " should reject negative startIndex");
    }

    @ParameterizedTest(name = "{0} - endIndex 越界校验")
    @MethodSource("rangeAlgorithms")
    void rejectsOutOfBoundsEndIndex(String name, RangeSort sort) {
        Double[] a = new Double[] {1.0, 2.0, 3.0};
        assertThrows(IllegalArgumentException.class, () -> sort.sort(a, 0, 3),
                name + " should reject out-of-bounds endIndex");
    }

    @ParameterizedTest(name = "{0} - startIndex 大于 endIndex 校验")
    @MethodSource("rangeAlgorithms")
    void rejectsStartGreaterThanEnd(String name, RangeSort sort) {
        Double[] a = new Double[] {1.0, 2.0, 3.0};
        assertThrows(IllegalArgumentException.class, () -> sort.sort(a, 2, 1),
                name + " should reject startIndex > endIndex");
    }
}
