package org.zero.sort;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * 对象数组（Comparable）排序测试
 * <p>
 * 全部适用算法（21 种中的 17 种比较类算法）× 完整输入矩阵（随机/有序/逆序/重复/
 * 单双元素/空数组/等值数组/区间），另含稳定性专项、命名方法与枚举分派等价性、
 * 比较次数防退化、泛型边界（父类实现 Comparable）与非法入参校验。
 * 期望结果以 {@link Arrays#sort(Object[])}（TimSort 稳定）为 oracle。
 *
 * @author Zero
 */
public class SortObjectTest {

    /** 固定随机种子，保证测试结果可复现 */
    private static final long SEED = 42L;

    /** 防退化比较次数上限系数：count < C * n * log2(n) */
    private static final long COUNT_LIMIT_FACTOR = 50L;

    /** 适用算法（对象数组只支持 ALL 类算法）+ 建议规模 */
    static Stream<Arguments> algorithms() {
        Stream.Builder<Arguments> b = Stream.builder();
        for (Algorithm algorithm : Algorithm.values()) {
            if (algorithm.applicability() == Algorithm.Applicability.ALL) {
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

    /** 生成固定种子的随机 Integer 数组 */
    private static Integer[] randomInts(int size, int bound) {
        Integer[] a = new Integer[size];
        Random random = new Random(SEED);
        for (int i = 0; i < size; i++) {
            a[i] = random.nextInt(bound);
        }
        return a;
    }

    /** 排序后的副本，作为期望结果 */
    private static Integer[] sortedCopy(Integer[] a) {
        Integer[] copy = a.clone();
        Arrays.sort(copy);
        return copy;
    }

    /** log2 上取整（n >= 1） */
    private static int log2Ceil(int n) {
        return 32 - Integer.numberOfLeadingZeros(n - 1);
    }

    // ==================== 排序正确性矩阵 ====================

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsRandomArray(Algorithm algorithm, int size) {
        Integer[] a = randomInts(size, 10000);
        Integer[] expected = sortedCopy(a);
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on random array");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsAlreadySortedArray(Algorithm algorithm, int size) {
        Integer[] a = randomInts(size, 1000);
        Arrays.sort(a);
        Integer[] expected = a.clone();
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on sorted array");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsReverseSortedArray(Algorithm algorithm, int size) {
        Integer[] a = new Integer[size];
        for (int i = 0; i < a.length; i++) {
            a[i] = a.length - i;
        }
        Integer[] expected = sortedCopy(a);
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on reverse sorted array");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsArrayWithDuplicates(Algorithm algorithm, int size) {
        Integer[] a = randomInts(size, 5);
        Integer[] expected = sortedCopy(a);
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on array with duplicates");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsSingleElementArray(Algorithm algorithm, int size) {
        Integer[] a = new Integer[] {7};
        Sort.sort(a, algorithm);
        assertArrayEquals(new Integer[] {7}, a, algorithm + " failed on single element array");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsTwoElementArray(Algorithm algorithm, int size) {
        Integer[] a = new Integer[] {9, 2};
        Sort.sort(a, algorithm);
        assertArrayEquals(new Integer[] {2, 9}, a, algorithm + " failed on two element array");

        Integer[] b = new Integer[] {2, 9};
        Sort.sort(b, algorithm);
        assertArrayEquals(new Integer[] {2, 9}, b, algorithm + " failed on sorted two element array");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsEmptyArray(Algorithm algorithm, int size) {
        Integer[] a = new Integer[0];
        Sort.sort(a, algorithm);
        assertArrayEquals(new Integer[0], a, algorithm + " failed on empty array");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsAllEqualArray(Algorithm algorithm, int size) {
        Integer[] a = new Integer[20];
        Arrays.fill(a, 5);
        Integer[] expected = a.clone();
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on all-equal array");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsSubrangeOnly(Algorithm algorithm, int size) {
        Integer[] a = new Integer[] {9, 8, 7, 6, 5, 4, 3, 2, 1};
        Integer[] expected = a.clone();
        Arrays.sort(expected, 2, 6);
        Sort.sort(a, algorithm, 2, 6);
        assertArrayEquals(expected, a, algorithm + " failed on subrange sort");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsEmptyRange(Algorithm algorithm, int size) {
        Integer[] a = new Integer[] {9, 8, 7, 6, 5, 4, 3, 2, 1};
        Integer[] expected = a.clone();
        Sort.sort(a, algorithm, 3, 3);
        assertArrayEquals(expected, a, algorithm + " failed on empty range");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsFullRange(Algorithm algorithm, int size) {
        Integer[] a = randomInts(100, 1000);
        Integer[] expected = sortedCopy(a);
        Sort.sort(a, algorithm, 0, a.length);
        assertArrayEquals(expected, a, algorithm + " failed on full range");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsLastSingleElementRange(Algorithm algorithm, int size) {
        Integer[] a = new Integer[] {5, 3, 8, 1, 9};
        Integer[] expected = a.clone();
        Sort.sort(a, algorithm, 4, 5);
        assertArrayEquals(expected, a, algorithm + " failed on last single element range");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void sortsStrings(Algorithm algorithm, int size) {
        String[] a = new String[] {"banana", "apple", "cherry", "avocado"};
        String[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on string array");
    }

    @Test
    void sortsTypesWhoseSupertypeImplementsComparable() {
        // 泛型边界为 Comparable<? super T>：Comparable 定义在父类上也必须可用
        Child[] a = new Child[] {new Child(3), new Child(1), new Child(2)};
        Sort.sort(a);
        assertEquals(1, a[0].value);
        assertEquals(2, a[1].value);
        assertEquals(3, a[2].value);
    }

    // ==================== 稳定性专项 ====================

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void stabilityContract(Algorithm algorithm, int size) {
        int[] keys = {3, 1, 2, 1, 3, 2, 1, 3};
        Keyed[] a = new Keyed[keys.length];
        for (int i = 0; i < keys.length; i++) {
            a[i] = new Keyed(keys[i], i);
        }
        // 期望：按 key 升序，同 key 按原始 id 升序（即稳定排序结果）
        Keyed[] expected = a.clone();
        Arrays.sort(expected, new Comparator<Keyed>() {
            @Override
            public int compare(Keyed x, Keyed y) {
                int c = Integer.compare(x.key, y.key);
                return c != 0 ? c : Integer.compare(x.id, y.id);
            }
        });
        Sort.sort(a, algorithm);
        if (algorithm.isStable()) {
            assertArrayEquals(expected, a, algorithm + " 声明为稳定算法，但相等元素相对顺序被改变");
        } else {
            // 不稳定算法：只断言 key 投影有序
            for (int i = 1; i < a.length; i++) {
                assertTrue(a[i - 1].key <= a[i].key, algorithm + " 排序结果无序");
            }
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void stableAlgorithmsKeepAllEqualArrayUntouched(Algorithm algorithm, int size) {
        if (!algorithm.isStable()) {
            return;
        }
        // 全相等输入：稳定算法必须保持原始相对顺序（恒等置换）
        Keyed[] a = new Keyed[20];
        for (int i = 0; i < a.length; i++) {
            a[i] = new Keyed(7, i);
        }
        Keyed[] before = a.clone();
        Sort.sort(a, algorithm);
        assertArrayEquals(before, a, algorithm + " 全相等输入时不应移动任何元素");
    }

    // ==================== 默认算法（Tim）等价性 ====================

    @Test
    void defaultSortMatchesTimSort() {
        Integer[] shapes1 = randomInts(1000, 10000);
        Integer[] shapes2 = randomInts(100, 5);
        Integer[] shapes3 = new Integer[200];
        Arrays.fill(shapes3, 7);
        Integer[] shapes4 = new Integer[500];
        for (int i = 0; i < shapes4.length; i++) {
            shapes4[i] = shapes4.length - i;
        }
        Integer[][] shapes = {shapes1, shapes2, shapes3, shapes4};
        for (Integer[] shape : shapes) {
            Integer[] a = shape.clone();
            Integer[] b = shape.clone();
            Sort.sort(a);
            Sort.sort(b, Algorithm.TIM);
            assertArrayEquals(b, a, "默认 sort(a) 应与 sort(a, Algorithm.TIM) 一致");
        }
    }

    // ==================== 命名方法与枚举分派等价性 ====================

    @Test
    void namedMethodsMatchEnumDispatch() {
        // 仅覆盖有命名便捷方法的 6 个算法（其余算法通过枚举分派使用）
        Algorithm[] named = {
            Algorithm.BUBBLE,
            Algorithm.SELECTION,
            Algorithm.INSERTION,
            Algorithm.SHELL,
            Algorithm.MERGE,
            Algorithm.QUICK
        };
        for (Algorithm algorithm : named) {
            Integer[] a = randomInts(200, 1000);
            Integer[] b = a.clone();
            sortNamed(a, algorithm);
            Sort.sort(b, algorithm);
            assertArrayEquals(b, a, algorithm + " 命名方法与枚举分派结果不一致");
        }
    }

    /** 通过命名便捷方法排序 */
    private static void sortNamed(Integer[] a, Algorithm algorithm) {
        switch (algorithm) {
            case BUBBLE:
                Sort.bubbleSort(a);
                break;
            case SELECTION:
                Sort.selectionSort(a);
                break;
            case INSERTION:
                Sort.insertionSort(a);
                break;
            case SHELL:
                Sort.shellSort(a);
                break;
            case MERGE:
                Sort.mergeSort(a);
                break;
            case QUICK:
                Sort.quickSort(a);
                break;
            default:
                throw new IllegalStateException("No named method for " + algorithm);
        }
    }

    // ==================== 防退化（比较次数上限） ====================

    @Test
    void subQuadraticAlgorithmsDoNotDegrade() {
        int n = 10_000;
        long limit = COUNT_LIMIT_FACTOR * n * log2Ceil(n);
        Algorithm[] algorithms = {Algorithm.SHELL, Algorithm.MERGE, Algorithm.QUICK, Algorithm.HEAP, Algorithm.TIM};
        for (Algorithm algorithm : algorithms) {
            for (int shape = 0; shape < 4; shape++) {
                Counting[] a = buildShape(n, shape);
                Counting.comparisons = 0;
                Sort.sort(a, algorithm);
                assertTrue(
                        Counting.comparisons < limit,
                        algorithm + " shape=" + shape + " 比较次数 " + Counting.comparisons
                                + " 超过上限 " + limit + "（疑似退化）");
                for (int i = 1; i < n; i++) {
                    assertTrue(a[i - 1].value <= a[i].value, algorithm + " shape=" + shape + " 排序结果无序");
                }
            }
        }
    }

    /** shape：0 有序、1 逆序、2 全相等、3 随机 */
    private static Counting[] buildShape(int n, int shape) {
        Counting[] a = new Counting[n];
        switch (shape) {
            case 0:
                for (int i = 0; i < n; i++) {
                    a[i] = new Counting(i);
                }
                break;
            case 1:
                for (int i = 0; i < n; i++) {
                    a[i] = new Counting(n - i);
                }
                break;
            case 2:
                for (int i = 0; i < n; i++) {
                    a[i] = new Counting(7);
                }
                break;
            default:
                Random random = new Random(SEED);
                for (int i = 0; i < n; i++) {
                    a[i] = new Counting(random.nextInt(1_000_000));
                }
                break;
        }
        return a;
    }

    // ==================== Tim 排序 run 栈专项 ====================

    @Test
    void timSortHandlesAlternatingRuns() {
        // 交替升降 run 输入，覆盖 mergeCollapse 的栈不变量各分支
        for (int size : new int[] {200, 1000, 5000}) {
            Integer[] a = alternatingRuns(size, 40);
            Integer[] expected = sortedCopy(a);
            Sort.sort(a, Algorithm.TIM);
            assertArrayEquals(expected, a, "TimSort failed on alternating runs, size=" + size);
        }
    }

    /** 构造交替升降 run 的数组（run 长度约 runLen） */
    private static Integer[] alternatingRuns(int size, int runLen) {
        Integer[] a = new Integer[size];
        int value = 0;
        int i = 0;
        boolean ascending = true;
        while (i < size) {
            int len = Math.min(runLen, size - i);
            for (int j = 0; j < len; j++) {
                a[i++] = ascending ? value + j : value + len - 1 - j;
            }
            value += len;
            ascending = !ascending;
        }
        return a;
    }

    // ==================== 阈值边界 ====================

    @Test
    void sortsThresholdBoundarySizes() {
        int[] sizes = {0, 1, 2, 14, 15, 16, 17, 31, 32, 33};
        for (int n : sizes) {
            Integer[] a = randomInts(n, 100);
            Integer[] expected = sortedCopy(a);
            Sort.quickSort(a);
            assertArrayEquals(expected, a, "quickSort failed at size " + n);

            Integer[] b = randomInts(n, 100);
            Integer[] expectedB = sortedCopy(b);
            Sort.mergeSort(b);
            assertArrayEquals(expectedB, b, "mergeSort failed at size " + n);
        }
    }

    // ==================== 非法入参校验 ====================

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void rejectsNullArray(Algorithm algorithm, int size) {
        assertThrows(NullPointerException.class, () -> Sort.sort((Integer[]) null, algorithm));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void rejectsNullElementInRange(Algorithm algorithm, int size) {
        Integer[] a = new Integer[] {1, null, 3};
        assertThrows(NullPointerException.class, () -> Sort.sort(a, algorithm));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void ignoresNullElementOutsideRange(Algorithm algorithm, int size) {
        Integer[] a = new Integer[] {3, 1, null, 2};
        Sort.sort(a, algorithm, 0, 2);
        assertArrayEquals(new Integer[] {1, 3, null, 2}, a, algorithm + " failed on range with null outside");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void rejectsNegativeFromIndex(Algorithm algorithm, int size) {
        Integer[] a = new Integer[] {1, 2, 3};
        assertThrows(IndexOutOfBoundsException.class, () -> Sort.sort(a, algorithm, -1, 3));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void rejectsToIndexTooLarge(Algorithm algorithm, int size) {
        Integer[] a = new Integer[] {1, 2, 3};
        assertThrows(IndexOutOfBoundsException.class, () -> Sort.sort(a, algorithm, 0, 4));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void rejectsFromGreaterThanTo(Algorithm algorithm, int size) {
        Integer[] a = new Integer[] {1, 2, 3};
        assertThrows(IndexOutOfBoundsException.class, () -> Sort.sort(a, algorithm, 3, 2));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void rejectsHugeIndexes(Algorithm algorithm, int size) {
        Integer[] a = new Integer[] {1, 2, 3};
        assertThrows(
                IndexOutOfBoundsException.class,
                () -> Sort.sort(a, algorithm, Integer.MAX_VALUE - 1, Integer.MAX_VALUE));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("algorithms")
    void rejectsNullAlgorithm(Algorithm algorithm, int size) {
        Integer[] a = new Integer[] {1, 2, 3};
        assertThrows(NullPointerException.class, () -> Sort.sort(a, (Algorithm) null));
    }

    // ==================== 测试辅助类型 ====================

    /** 携带 key 与原始 id 的元素：用于稳定性验证（compareTo 只比较 key） */
    private static final class Keyed implements Comparable<Keyed> {
        final int key;
        final int id;

        Keyed(int key, int id) {
            this.key = key;
            this.id = id;
        }

        @Override
        public int compareTo(Keyed o) {
            return Integer.compare(key, o.key);
        }

        @Override
        public boolean equals(Object obj) {
            Keyed other = (Keyed) obj;
            return key == other.key && id == other.id;
        }

        @Override
        public int hashCode() {
            return 31 * key + id;
        }
    }

    /** 统计 compareTo 调用次数的元素：用于防退化测试 */
    private static final class Counting implements Comparable<Counting> {
        static long comparisons;

        final int value;

        Counting(int value) {
            this.value = value;
        }

        @Override
        public int compareTo(Counting o) {
            comparisons++;
            return Integer.compare(value, o.value);
        }
    }

    /** 父类实现 Comparable，验证泛型边界 Comparable&lt;? super T&gt; */
    private static class Base implements Comparable<Base> {
        final int value;

        Base(int value) {
            this.value = value;
        }

        @Override
        public int compareTo(Base o) {
            return Integer.compare(value, o.value);
        }
    }

    private static final class Child extends Base {
        Child(int value) {
            super(value);
        }
    }
}
