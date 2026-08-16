package org.zero.sort;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * float[] 排序测试
 * <p>
 * 通过公共 API（{@link Sort}）对 float[] 的全部适用算法做矩阵式验证，
 * 期望结果以 {@link Arrays#sort} 为 oracle（比较语义与实现一致）。
 *
 * @author Zero
 */
public class FloatSortsTest {

    /** 固定随机种子，保证测试结果可复现 */
    private static final long SEED = 42L;

    /** 适用算法 + 建议规模：慢算法用较小规模避免超时 */
    static Stream<Arguments> algorithms() {
        Stream.Builder<Arguments> b = Stream.builder();
        for (Algorithm algorithm : Algorithm.values()) {
            if (algorithm.applicability() != Algorithm.Applicability.INTEGRALS_ONLY) {
                b.add(Arguments.of(algorithm, sizeFor(algorithm)));
            }
        }
        return b.build();
    }

    /** 按算法复杂度映射测试规模 */
    private static int sizeFor(Algorithm algorithm) {
        switch (algorithm) {
            case STOOGE:
                return 60;
            case PANCAKE:
            case BUBBLE:
            case SELECTION:
            case INSERTION:
            case GNOME:
            case COCKTAIL:
            case ODD_EVEN:
            case CYCLE:
                return 200;
            default:
                return 1000;
        }
    }

    /** 生成固定种子的随机数组，bound 越小重复元素越多 */
    private static float[] randomArray(int size, int bound) {
        float[] a = new float[size];
        Random random = new Random(SEED);
        for (int i = 0; i < size; i++) {
            a[i] = (float) random.nextInt(bound);
        }
        return a;
    }

    /** 生成含负值的随机数组（覆盖基数排序符号翻转等分支） */
    private static float[] randomSignedArray(int size, int bound) {
        float[] a = new float[size];
        Random random = new Random(SEED);
        for (int i = 0; i < size; i++) {
            a[i] = (float) (random.nextInt(2 * bound) - bound);
        }
        return a;
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsRandomArray(Algorithm algorithm, int size) {
        float[] a = randomArray(size, 10000);
        float[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on random array");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsSignedArray(Algorithm algorithm, int size) {
        float[] a = randomSignedArray(size, 5000);
        float[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on signed array");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsAlreadySortedArray(Algorithm algorithm, int size) {
        float[] a = randomSignedArray(size, 1000);
        Arrays.sort(a);
        float[] expected = a.clone();
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on sorted array");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsReverseSortedArray(Algorithm algorithm, int size) {
        float[] a = new float[size];
        for (int i = 0; i < a.length; i++) {
            a[i] = (float) (a.length - i);
        }
        float[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on reverse sorted array");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsArrayWithDuplicates(Algorithm algorithm, int size) {
        float[] a = randomArray(size, 5);
        float[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on array with duplicates");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsSingleElementArray(Algorithm algorithm, int size) {
        float[] a = new float[] {7};
        Sort.sort(a, algorithm);
        assertArrayEquals(new float[] {7}, a, algorithm + " failed on single element array");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsTwoElementArray(Algorithm algorithm, int size) {
        float[] a = new float[] {9, 2};
        Sort.sort(a, algorithm);
        assertArrayEquals(new float[] {2, 9}, a, algorithm + " failed on two element array");

        float[] b = new float[] {2, 9};
        Sort.sort(b, algorithm);
        assertArrayEquals(new float[] {2, 9}, b, algorithm + " failed on sorted two element array");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsEmptyArray(Algorithm algorithm, int size) {
        float[] a = new float[0];
        Sort.sort(a, algorithm);
        assertArrayEquals(new float[0], a, algorithm + " failed on empty array");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsSubrangeOnly(Algorithm algorithm, int size) {
        float[] a = new float[] {9, 8, 7, 6, 5, 4, 3, 2, 1};
        float[] expected = a.clone();
        Arrays.sort(expected, 2, 6);
        Sort.sort(a, algorithm, 2, 6);
        assertArrayEquals(expected, a, algorithm + " failed on subrange sort");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsEmptyRange(Algorithm algorithm, int size) {
        float[] a = new float[] {9, 8, 7, 6, 5, 4, 3, 2, 1};
        float[] expected = a.clone();
        Sort.sort(a, algorithm, 3, 3);
        assertArrayEquals(expected, a, algorithm + " failed on empty range");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsFullRange(Algorithm algorithm, int size) {
        float[] a = randomSignedArray(100, 1000);
        float[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm, 0, a.length);
        assertArrayEquals(expected, a, algorithm + " failed on full range");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsLastSingleElementRange(Algorithm algorithm, int size) {
        float[] a = new float[] {5, 3, 8, 1, 9};
        float[] expected = a.clone();
        Sort.sort(a, algorithm, 4, 5);
        assertArrayEquals(expected, a, algorithm + " failed on last single element range");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsExtremeValues(Algorithm algorithm, int size) {
        float[] a = new float[] {Float.MAX_VALUE, Float.MIN_VALUE, 0f, 1f, -1f, Float.MAX_VALUE, Float.MIN_VALUE};
        float[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on extreme values");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void rejectsNegativeFromIndex(Algorithm algorithm, int size) {
        float[] a = new float[] {1, 2, 3};
        assertThrows(IndexOutOfBoundsException.class, () -> Sort.sort(a, algorithm, -1, 3));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void rejectsToIndexTooLarge(Algorithm algorithm, int size) {
        float[] a = new float[] {1, 2, 3};
        assertThrows(IndexOutOfBoundsException.class, () -> Sort.sort(a, algorithm, 0, 4));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void rejectsFromGreaterThanTo(Algorithm algorithm, int size) {
        float[] a = new float[] {1, 2, 3};
        assertThrows(IndexOutOfBoundsException.class, () -> Sort.sort(a, algorithm, 3, 2));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void rejectsHugeIndexes(Algorithm algorithm, int size) {
        float[] a = new float[] {1, 2, 3};
        assertThrows(
                IndexOutOfBoundsException.class,
                () -> Sort.sort(a, algorithm, Integer.MAX_VALUE - 1, Integer.MAX_VALUE));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void rejectsNullAlgorithm(Algorithm algorithm, int size) {
        float[] a = new float[] {1, 2, 3};
        assertThrows(NullPointerException.class, () -> Sort.sort(a, (Algorithm) null));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void rejectsNullArray(Algorithm algorithm, int size) {
        float[] a = null;
        assertThrows(NullPointerException.class, () -> Sort.sort(a, algorithm));
    }

    @Test
    void defaultSortProducesSortedArray() {
        // 大数组随机输入（走快速/归并分派）
        float[] a = randomSignedArray(1000, 5000);
        float[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a);
        assertArrayEquals(expected, a, "default sort failed on random input");

        // 小数组输入（< 47，走插入排序分派）
        float[] b = randomSignedArray(46, 100);
        float[] expectedB = b.clone();
        Arrays.sort(expectedB);
        Sort.sort(b);
        assertArrayEquals(expectedB, b, "default sort failed on small input");

        // 近有序输入（走归并分派）：先排序再少量相邻交换
        float[] c = randomSignedArray(500, 1000);
        Arrays.sort(c);
        for (int i = 0; i < 10; i++) {
            int x = (i * 37) % (c.length - 1);
            float tmp = c[x];
            c[x] = c[x + 1];
            c[x + 1] = tmp;
        }
        float[] expectedC = c.clone();
        Arrays.sort(expectedC);
        Sort.sort(c);
        assertArrayEquals(expectedC, c, "default sort failed on nearly sorted input");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsSpecialValues(Algorithm algorithm, int size) {
        // NaN 排在最后、-0.0 < 0.0、±Infinity 与次正规数按 compare 全序
        float[] a = new float[] {
            Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, 0f, -0.0f, 1.5f, -1.5f,
            Float.MIN_NORMAL, Float.MAX_VALUE, 1.0f, -1.0f, Float.MIN_VALUE, Float.NaN
        };
        float[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on special values");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsAllNaNArray(Algorithm algorithm, int size) {
        // 桶排序的全 NaN 退化路径
        float[] a = new float[] {Float.NaN, Float.NaN, Float.NaN};
        float[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on all-NaN array");
    }
}
