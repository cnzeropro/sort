package org.zero.sort;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * long[] 排序测试
 * <p>
 * ★ 本文件由 tools/gen-primitives.py 自动生成，请勿手改；如需修改请编辑脚本后重新生成。
 * <p>
 * 通过公共 API（{@link Sort}）对 long[] 的全部 6 种算法做矩阵式验证，
 * 期望结果以 {@link Arrays#sort} 为 oracle（比较语义与实现一致：有符号 64 位整数自然序）。
 *
 * @author Zero
 */
public class LongSortsTest {

    /** 固定随机种子，保证测试结果可复现 */
    private static final long SEED = 42L;

    /** 生成固定种子的随机数组，bound 越小重复元素越多 */
    private static long[] randomArray(int size, int bound) {
        long[] a = new long[size];
        Random random = new Random(SEED);
        for (int i = 0; i < size; i++) {
            a[i] = (long) random.nextInt(bound);
        }
        return a;
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsRandomArray(Algorithm algorithm) {
        long[] a = randomArray(1000, 10000);
        long[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on random array");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsAlreadySortedArray(Algorithm algorithm) {
        long[] a = randomArray(100, 1000);
        Arrays.sort(a);
        long[] expected = a.clone();
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on sorted array");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsReverseSortedArray(Algorithm algorithm) {
        long[] a = new long[100];
        for (int i = 0; i < a.length; i++) {
            a[i] = (long) (a.length - i);
        }
        long[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on reverse sorted array");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsArrayWithDuplicates(Algorithm algorithm) {
        long[] a = randomArray(100, 5);
        long[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on array with duplicates");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsSingleElementArray(Algorithm algorithm) {
        long[] a = new long[] {7};
        Sort.sort(a, algorithm);
        assertArrayEquals(new long[] {7}, a, algorithm + " failed on single element array");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsTwoElementArray(Algorithm algorithm) {
        long[] a = new long[] {9, 2};
        Sort.sort(a, algorithm);
        assertArrayEquals(new long[] {2, 9}, a, algorithm + " failed on two element array");

        long[] b = new long[] {2, 9};
        Sort.sort(b, algorithm);
        assertArrayEquals(new long[] {2, 9}, b, algorithm + " failed on sorted two element array");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsEmptyArray(Algorithm algorithm) {
        // 空数组合法，排序为空操作（与 Arrays.sort 一致）
        long[] a = new long[0];
        Sort.sort(a, algorithm);
        assertArrayEquals(new long[0], a, algorithm + " failed on empty array");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsSubrangeOnly(Algorithm algorithm) {
        long[] a = new long[] {9, 8, 7, 6, 5, 4, 3, 2, 1};
        long[] expected = a.clone();
        Arrays.sort(expected, 2, 6);
        // 仅对 [2, 6) 排序，区间外元素保持原样
        Sort.sort(a, algorithm, 2, 6);
        assertArrayEquals(expected, a, algorithm + " failed on subrange sort");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsEmptyRange(Algorithm algorithm) {
        // 空区间（from == to）合法，数组保持原样
        long[] a = new long[] {9, 8, 7, 6, 5, 4, 3, 2, 1};
        long[] expected = a.clone();
        Sort.sort(a, algorithm, 3, 3);
        assertArrayEquals(expected, a, algorithm + " failed on empty range");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsFullRange(Algorithm algorithm) {
        // [0, length) 等价于全数组排序
        long[] a = randomArray(100, 1000);
        long[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm, 0, a.length);
        assertArrayEquals(expected, a, algorithm + " failed on full range");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsLastSingleElementRange(Algorithm algorithm) {
        // 末元素单元素区间 [length - 1, length) 合法
        long[] a = new long[] {5, 3, 8, 1, 9};
        long[] expected = a.clone();
        Sort.sort(a, algorithm, 4, 5);
        assertArrayEquals(expected, a, algorithm + " failed on last single element range");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void rejectsNegativeFromIndex(Algorithm algorithm) {
        long[] a = new long[] {1, 2, 3};
        assertThrows(
                IndexOutOfBoundsException.class,
                () -> Sort.sort(a, algorithm, -1, 3),
                algorithm + " should reject negative fromIndex");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void rejectsToIndexTooLarge(Algorithm algorithm) {
        long[] a = new long[] {1, 2, 3};
        assertThrows(
                IndexOutOfBoundsException.class,
                () -> Sort.sort(a, algorithm, 0, 4),
                algorithm + " should reject out-of-bounds toIndex");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void rejectsFromGreaterThanTo(Algorithm algorithm) {
        long[] a = new long[] {1, 2, 3};
        assertThrows(
                IndexOutOfBoundsException.class,
                () -> Sort.sort(a, algorithm, 3, 2),
                algorithm + " should reject fromIndex > toIndex");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void rejectsHugeIndexes(Algorithm algorithm) {
        long[] a = new long[] {1, 2, 3};
        assertThrows(
                IndexOutOfBoundsException.class,
                () -> Sort.sort(a, algorithm, Integer.MAX_VALUE - 1, Integer.MAX_VALUE),
                algorithm + " should reject huge indexes");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void rejectsNullAlgorithm(Algorithm algorithm) {
        long[] a = new long[] {1, 2, 3};
        assertThrows(
                NullPointerException.class,
                () -> Sort.sort(a, (Algorithm) null),
                algorithm + " should reject null algorithm");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void rejectsNullArray(Algorithm algorithm) {
        long[] a = null;
        assertThrows(
                NullPointerException.class,
                () -> Sort.sort(a, algorithm),
                algorithm + " should reject null array");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsExtremeValues(Algorithm algorithm) {
        long[] a = new long[] {Long.MAX_VALUE, Long.MIN_VALUE, 0, 1, -1, Long.MAX_VALUE, Long.MIN_VALUE};
        long[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on extreme values");
    }
}
