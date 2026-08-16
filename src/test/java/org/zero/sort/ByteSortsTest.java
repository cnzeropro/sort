package org.zero.sort;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * byte[] 排序测试
 * <p>
 * ★ 本文件由 tools/gen-primitives.py 自动生成，请勿手改；如需修改请编辑脚本后重新生成。
 * <p>
 * 通过公共 API（{@link Sort}）对 byte[] 的全部 6 种算法做矩阵式验证，
 * 期望结果以 {@link Arrays#sort} 为 oracle（比较语义与实现一致：有符号 8 位整数自然序）。
 *
 * @author Zero
 */
public class ByteSortsTest {

    /** 固定随机种子，保证测试结果可复现 */
    private static final long SEED = 42L;

    /** 生成固定种子的随机数组，bound 越小重复元素越多 */
    private static byte[] randomArray(int size, int bound) {
        byte[] a = new byte[size];
        Random random = new Random(SEED);
        for (int i = 0; i < size; i++) {
            a[i] = (byte) random.nextInt(bound);
        }
        return a;
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsRandomArray(Algorithm algorithm) {
        byte[] a = randomArray(1000, 10000);
        byte[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on random array");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsAlreadySortedArray(Algorithm algorithm) {
        byte[] a = randomArray(100, 1000);
        Arrays.sort(a);
        byte[] expected = a.clone();
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on sorted array");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsReverseSortedArray(Algorithm algorithm) {
        byte[] a = new byte[100];
        for (int i = 0; i < a.length; i++) {
            a[i] = (byte) (a.length - i);
        }
        byte[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on reverse sorted array");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsArrayWithDuplicates(Algorithm algorithm) {
        byte[] a = randomArray(100, 5);
        byte[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on array with duplicates");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsSingleElementArray(Algorithm algorithm) {
        byte[] a = new byte[] {7};
        Sort.sort(a, algorithm);
        assertArrayEquals(new byte[] {7}, a, algorithm + " failed on single element array");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsTwoElementArray(Algorithm algorithm) {
        byte[] a = new byte[] {9, 2};
        Sort.sort(a, algorithm);
        assertArrayEquals(new byte[] {2, 9}, a, algorithm + " failed on two element array");

        byte[] b = new byte[] {2, 9};
        Sort.sort(b, algorithm);
        assertArrayEquals(new byte[] {2, 9}, b, algorithm + " failed on sorted two element array");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsEmptyArray(Algorithm algorithm) {
        // 空数组合法，排序为空操作（与 Arrays.sort 一致）
        byte[] a = new byte[0];
        Sort.sort(a, algorithm);
        assertArrayEquals(new byte[0], a, algorithm + " failed on empty array");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsSubrangeOnly(Algorithm algorithm) {
        byte[] a = new byte[] {9, 8, 7, 6, 5, 4, 3, 2, 1};
        byte[] expected = a.clone();
        Arrays.sort(expected, 2, 6);
        // 仅对 [2, 6) 排序，区间外元素保持原样
        Sort.sort(a, algorithm, 2, 6);
        assertArrayEquals(expected, a, algorithm + " failed on subrange sort");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsEmptyRange(Algorithm algorithm) {
        // 空区间（from == to）合法，数组保持原样
        byte[] a = new byte[] {9, 8, 7, 6, 5, 4, 3, 2, 1};
        byte[] expected = a.clone();
        Sort.sort(a, algorithm, 3, 3);
        assertArrayEquals(expected, a, algorithm + " failed on empty range");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsFullRange(Algorithm algorithm) {
        // [0, length) 等价于全数组排序
        byte[] a = randomArray(100, 1000);
        byte[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm, 0, a.length);
        assertArrayEquals(expected, a, algorithm + " failed on full range");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsLastSingleElementRange(Algorithm algorithm) {
        // 末元素单元素区间 [length - 1, length) 合法
        byte[] a = new byte[] {5, 3, 8, 1, 9};
        byte[] expected = a.clone();
        Sort.sort(a, algorithm, 4, 5);
        assertArrayEquals(expected, a, algorithm + " failed on last single element range");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void rejectsNegativeFromIndex(Algorithm algorithm) {
        byte[] a = new byte[] {1, 2, 3};
        assertThrows(
                IndexOutOfBoundsException.class,
                () -> Sort.sort(a, algorithm, -1, 3),
                algorithm + " should reject negative fromIndex");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void rejectsToIndexTooLarge(Algorithm algorithm) {
        byte[] a = new byte[] {1, 2, 3};
        assertThrows(
                IndexOutOfBoundsException.class,
                () -> Sort.sort(a, algorithm, 0, 4),
                algorithm + " should reject out-of-bounds toIndex");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void rejectsFromGreaterThanTo(Algorithm algorithm) {
        byte[] a = new byte[] {1, 2, 3};
        assertThrows(
                IndexOutOfBoundsException.class,
                () -> Sort.sort(a, algorithm, 3, 2),
                algorithm + " should reject fromIndex > toIndex");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void rejectsHugeIndexes(Algorithm algorithm) {
        byte[] a = new byte[] {1, 2, 3};
        assertThrows(
                IndexOutOfBoundsException.class,
                () -> Sort.sort(a, algorithm, Integer.MAX_VALUE - 1, Integer.MAX_VALUE),
                algorithm + " should reject huge indexes");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void rejectsNullAlgorithm(Algorithm algorithm) {
        byte[] a = new byte[] {1, 2, 3};
        assertThrows(
                NullPointerException.class,
                () -> Sort.sort(a, (Algorithm) null),
                algorithm + " should reject null algorithm");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void rejectsNullArray(Algorithm algorithm) {
        byte[] a = null;
        assertThrows(
                NullPointerException.class,
                () -> Sort.sort(a, algorithm),
                algorithm + " should reject null array");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsExtremeValues(Algorithm algorithm) {
        byte[] a = new byte[] {Byte.MAX_VALUE, Byte.MIN_VALUE, 0, 1, -1, Byte.MAX_VALUE, Byte.MIN_VALUE};
        byte[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on extreme values");
    }
}
