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
 * long[] 排序测试
 * <p>
 * 通过公共 API（{@link Sort}）对 long[] 的全部适用算法做矩阵式验证，
 * 期望结果以 {@link Arrays#sort} 为 oracle（比较语义与实现一致）。
 *
 * @author Zero
 */
public class LongSortsTest {

    /** 固定随机种子，保证测试结果可复现 */
    private static final long SEED = 42L;

    /** 适用算法 + 建议规模：慢算法用较小规模避免超时 */
    static Stream<Arguments> algorithms() {
        Stream.Builder<Arguments> b = Stream.builder();
        for (Algorithm algorithm : Algorithm.values()) {
            if (algorithm.applicability() != Algorithm.Applicability.FLOATS_ONLY) {
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
    private static long[] randomArray(int size, int bound) {
        long[] a = new long[size];
        Random random = new Random(SEED);
        for (int i = 0; i < size; i++) {
            a[i] = (long) random.nextInt(bound);
        }
        return a;
    }

    /** 生成含负值的随机数组（覆盖基数排序符号翻转等分支） */
    private static long[] randomSignedArray(int size, int bound) {
        long[] a = new long[size];
        Random random = new Random(SEED);
        for (int i = 0; i < size; i++) {
            a[i] = (long) (random.nextInt(2 * bound) - bound);
        }
        return a;
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsRandomArray(Algorithm algorithm, int size) {
        long[] a = randomArray(size, 10000);
        long[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on random array");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsSignedArray(Algorithm algorithm, int size) {
        long[] a = randomSignedArray(size, 5000);
        long[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on signed array");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsAlreadySortedArray(Algorithm algorithm, int size) {
        long[] a = randomSignedArray(size, 1000);
        Arrays.sort(a);
        long[] expected = a.clone();
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on sorted array");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsReverseSortedArray(Algorithm algorithm, int size) {
        long[] a = new long[size];
        for (int i = 0; i < a.length; i++) {
            a[i] = (long) (a.length - i);
        }
        long[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on reverse sorted array");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsArrayWithDuplicates(Algorithm algorithm, int size) {
        long[] a = randomArray(size, 5);
        long[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on array with duplicates");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsSingleElementArray(Algorithm algorithm, int size) {
        long[] a = new long[] {7};
        Sort.sort(a, algorithm);
        assertArrayEquals(new long[] {7}, a, algorithm + " failed on single element array");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsTwoElementArray(Algorithm algorithm, int size) {
        long[] a = new long[] {9, 2};
        Sort.sort(a, algorithm);
        assertArrayEquals(new long[] {2, 9}, a, algorithm + " failed on two element array");

        long[] b = new long[] {2, 9};
        Sort.sort(b, algorithm);
        assertArrayEquals(new long[] {2, 9}, b, algorithm + " failed on sorted two element array");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsEmptyArray(Algorithm algorithm, int size) {
        long[] a = new long[0];
        Sort.sort(a, algorithm);
        assertArrayEquals(new long[0], a, algorithm + " failed on empty array");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsSubrangeOnly(Algorithm algorithm, int size) {
        long[] a = new long[] {9, 8, 7, 6, 5, 4, 3, 2, 1};
        long[] expected = a.clone();
        Arrays.sort(expected, 2, 6);
        Sort.sort(a, algorithm, 2, 6);
        assertArrayEquals(expected, a, algorithm + " failed on subrange sort");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsEmptyRange(Algorithm algorithm, int size) {
        long[] a = new long[] {9, 8, 7, 6, 5, 4, 3, 2, 1};
        long[] expected = a.clone();
        Sort.sort(a, algorithm, 3, 3);
        assertArrayEquals(expected, a, algorithm + " failed on empty range");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsFullRange(Algorithm algorithm, int size) {
        long[] a = randomSignedArray(100, 1000);
        long[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm, 0, a.length);
        assertArrayEquals(expected, a, algorithm + " failed on full range");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsLastSingleElementRange(Algorithm algorithm, int size) {
        long[] a = new long[] {5, 3, 8, 1, 9};
        long[] expected = a.clone();
        Sort.sort(a, algorithm, 4, 5);
        assertArrayEquals(expected, a, algorithm + " failed on last single element range");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsExtremeValues(Algorithm algorithm, int size) {
        // 计数/鸽巢对 MIN~MAX 值域超上限抛 IAE 属设计行为，由 countingSortRejectsHugeRange 专门验证
        if (algorithm == Algorithm.COUNTING || algorithm == Algorithm.PIGEONHOLE) {
            return;
        }
        long[] a = new long[] {Long.MAX_VALUE, Long.MIN_VALUE, 0, 1, -1, Long.MAX_VALUE, Long.MIN_VALUE};
        long[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on extreme values");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void rejectsNegativeFromIndex(Algorithm algorithm, int size) {
        long[] a = new long[] {1, 2, 3};
        assertThrows(IndexOutOfBoundsException.class, () -> Sort.sort(a, algorithm, -1, 3));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void rejectsToIndexTooLarge(Algorithm algorithm, int size) {
        long[] a = new long[] {1, 2, 3};
        assertThrows(IndexOutOfBoundsException.class, () -> Sort.sort(a, algorithm, 0, 4));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void rejectsFromGreaterThanTo(Algorithm algorithm, int size) {
        long[] a = new long[] {1, 2, 3};
        assertThrows(IndexOutOfBoundsException.class, () -> Sort.sort(a, algorithm, 3, 2));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void rejectsHugeIndexes(Algorithm algorithm, int size) {
        long[] a = new long[] {1, 2, 3};
        assertThrows(
                IndexOutOfBoundsException.class,
                () -> Sort.sort(a, algorithm, Integer.MAX_VALUE - 1, Integer.MAX_VALUE));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void rejectsNullAlgorithm(Algorithm algorithm, int size) {
        long[] a = new long[] {1, 2, 3};
        assertThrows(NullPointerException.class, () -> Sort.sort(a, (Algorithm) null));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void rejectsNullArray(Algorithm algorithm, int size) {
        long[] a = null;
        assertThrows(NullPointerException.class, () -> Sort.sort(a, algorithm));
    }

    @Test
    void defaultSortProducesSortedArray() {
        // 大数组随机输入（走快速/归并分派）
        long[] a = randomSignedArray(1000, 5000);
        long[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a);
        assertArrayEquals(expected, a, "default sort failed on random input");

        // 小数组输入（< 47，走插入排序分派）
        long[] b = randomSignedArray(46, 100);
        long[] expectedB = b.clone();
        Arrays.sort(expectedB);
        Sort.sort(b);
        assertArrayEquals(expectedB, b, "default sort failed on small input");

        // 近有序输入（走归并分派）：先排序再少量相邻交换
        long[] c = randomSignedArray(500, 1000);
        Arrays.sort(c);
        for (int i = 0; i < 10; i++) {
            int x = (i * 37) % (c.length - 1);
            long tmp = c[x];
            c[x] = c[x + 1];
            c[x + 1] = tmp;
        }
        long[] expectedC = c.clone();
        Arrays.sort(expectedC);
        Sort.sort(c);
        assertArrayEquals(expectedC, c, "default sort failed on nearly sorted input");
    }

    @Test
    void countingSortRejectsHugeRange() {
        long[] a = new long[] {Long.MAX_VALUE, Long.MIN_VALUE};
        assertThrows(IllegalArgumentException.class, () -> Sort.sort(a, Algorithm.COUNTING));
        assertThrows(IllegalArgumentException.class, () -> Sort.sort(a, Algorithm.PIGEONHOLE));
    }
}
