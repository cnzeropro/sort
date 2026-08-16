package org.zero.sort;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Comparator 重载测试
 * <p>
 * 验证 21 种算法中适用对象数组的算法在自定义比较器（升序/降序）下的正确性、
 * null 元素（由比较器处理）语义、null 比较器校验、以及 Comparator 版本
 * 默认算法（TIM）与显式 TIM 的等价性。
 *
 * @author Zero
 */
public class SortComparatorTest {

    private static final long SEED = 42L;

    /** 适用算法 + 建议规模 */
    static Stream<Arguments> algorithms() {
        Stream.Builder<Arguments> b = Stream.builder();
        for (Algorithm algorithm : Algorithm.values()) {
            if (algorithm.applicability() == Algorithm.Applicability.ALL) {
                int size = algorithm == Algorithm.STOOGE ? 60 : 200;
                b.add(Arguments.of(algorithm, size));
            }
        }
        return b.build();
    }

    private static Integer[] randomInts(int size, int bound) {
        Integer[] a = new Integer[size];
        Random random = new Random(SEED);
        for (int i = 0; i < size; i++) {
            a[i] = random.nextInt(bound);
        }
        return a;
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsWithReverseOrder(Algorithm algorithm, int size) {
        Integer[] a = randomInts(size, 1000);
        Integer[] expected = a.clone();
        Arrays.sort(expected, Comparator.reverseOrder());
        Sort.sort(a, algorithm, Comparator.reverseOrder());
        assertArrayEquals(expected, a, algorithm + " failed with reverseOrder comparator");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsWithCustomComparatorOnStrings(Algorithm algorithm, int size) {
        // 按字符串长度排序的比较器
        // 全序比较器（长度优先、自然序破平），保证所有算法的期望结果唯一
        Comparator<String> byLength = new Comparator<String>() {
            @Override
            public int compare(String x, String y) {
                int c = Integer.compare(x.length(), y.length());
                return c != 0 ? c : x.compareTo(y);
            }
        };
        String[] a = new String[] {"aaa", "b", "cc", "dddd", "e"};
        String[] expected = a.clone();
        Arrays.sort(expected, byLength);
        Sort.sort(a, algorithm, byLength);
        assertArrayEquals(expected, a, algorithm + " failed with custom comparator");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsRangeWithComparator(Algorithm algorithm, int size) {
        Integer[] a = new Integer[] {9, 8, 7, 6, 5, 4, 3, 2, 1};
        Integer[] expected = a.clone();
        Arrays.sort(expected, 2, 6, Comparator.naturalOrder());
        Sort.sort(a, algorithm, Comparator.naturalOrder(), 2, 6);
        assertArrayEquals(expected, a, algorithm + " failed on comparator range sort");
    }

    @Test
    void allowsNullElementsWhenComparatorHandlesThem() {
        // 与 JDK Arrays.sort(T[], Comparator) 一致：null 元素由比较器处理
        Comparator<Integer> nullsFirst = new Comparator<Integer>() {
            @Override
            public int compare(Integer x, Integer y) {
                if (x == null) {
                    return y == null ? 0 : -1;
                }
                if (y == null) {
                    return 1;
                }
                return Integer.compare(x, y);
            }
        };
        Integer[] a = new Integer[] {3, null, 1, null, 2};
        Integer[] expected = a.clone();
        Arrays.sort(expected, nullsFirst);
        Sort.sort(a, nullsFirst);
        assertArrayEquals(expected, a, "comparator 版本应允许 null 元素");
    }

    @Test
    void comparatorDefaultMatchesTimSort() {
        Integer[] a = randomInts(500, 5000);
        Integer[] b = a.clone();
        Sort.sort(a, Comparator.reverseOrder());
        Sort.sort(b, Algorithm.TIM, Comparator.reverseOrder());
        assertArrayEquals(b, a, "Comparator 默认算法应与 TIM 一致");
    }

    @Test
    void rejectsNullComparator() {
        Integer[] a = new Integer[] {1, 2, 3};
        assertThrows(NullPointerException.class, () -> Sort.sort(a, (Comparator<Integer>) null));
        assertThrows(
                NullPointerException.class,
                () -> Sort.sort(a, Algorithm.MERGE, (Comparator<Integer>) null));
    }
}
