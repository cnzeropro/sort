package org.zero.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * List 路径正确性测试。
 * <p>
 * 覆盖内容：
 * <ol>
 *   <li>{@link Sort#sort(List)}（Comparable 路径）与 {@link Collections#sort} 一致：
 *       ArrayList 与 LinkedList 两种实现 × 随机含重复 / 已有序 / 逆序 / 空 / 单元素五种形态；</li>
 *   <li>{@link Sort#sort(List, Comparator)} 逆序比较器与 oracle 一致（两种 List 实现）；</li>
 *   <li>17 个比较类算法的 {@code sort(List)} 与 {@code sort(List, Comparator)} 参数化矩阵，
 *       与 oracle（{@code Collections.sort}）一致；</li>
 *   <li>List 含 null 元素时 Comparable 路径 {@code sort(List)} → {@link NullPointerException}；</li>
 *   <li>{@code List<String>} 与 {@code List<Integer>} 混合形态验证。</li>
 * </ol>
 * 随机数据使用固定种子 {@link #SEED}，保证失败可复现；断言信息带算法名 / List 类型 / 数据形态便于定位。
 *
 * @author Zero
 */
class SortListTest {

    /** 随机数据固定种子，保证测试可复现 */
    private static final long SEED = 42L;

    // ==================== 门面：Sort.sort(List) vs Collections.sort ====================

    /**
     * List 实现工厂（ArrayList / LinkedList）
     *
     * @return List 类型名 + 对应构造工厂
     */
    static Stream<Arguments> listFactories() {
        return Stream.of(
                Arguments.of("ArrayList", (Supplier<List<Integer>>) ArrayList::new),
                Arguments.of("LinkedList", (Supplier<List<Integer>>) LinkedList::new));
    }

    /**
     * Sort.sort(List) 与 Collections.sort 一致：ArrayList / LinkedList × 五种数据形态
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("listFactories")
    void testSortListMatchesCollectionsSort(String listName, Supplier<List<Integer>> factory) {
        String[] shapeNames = {"随机含重复", "已有序", "逆序", "空", "单元素"};
        List<List<Integer>> shapes = Arrays.asList(
                randomIntegers(SEED, 80, 25),
                new ArrayList<>(Arrays.asList(1, 2, 2, 3, 5, 5, 8)),
                new ArrayList<>(Arrays.asList(9, 7, 5, 4, 3, 1, 0)),
                new ArrayList<Integer>(),
                new ArrayList<>(Arrays.asList(42)));

        for (int i = 0; i < shapes.size(); i++) {
            List<Integer> actual = newList(factory, shapes.get(i));
            List<Integer> expected = newList(factory, shapes.get(i));

            Sort.sort(actual);
            Collections.sort(expected);

            assertEquals(expected, actual, listName + " / " + shapeNames[i]
                    + "：Sort.sort(List) 与 Collections.sort 结果不一致");
        }
    }

    /**
     * Sort.sort(List, Comparator) 逆序比较器与 Collections.sort 一致：ArrayList / LinkedList
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("listFactories")
    void testSortListWithReverseComparatorMatchesOracle(String listName, Supplier<List<Integer>> factory) {
        List<Integer> data = randomIntegers(SEED, 80, 25);
        List<Integer> actual = newList(factory, data);
        List<Integer> expected = newList(factory, data);

        Sort.sort(actual, Comparator.reverseOrder());
        Collections.sort(expected, Comparator.reverseOrder());

        assertEquals(expected, actual, listName
                + "：Sort.sort(List,Comparator) 逆序比较器与 Collections.sort 结果不一致");
    }

    // ==================== 17 个比较类算法的 List 路径 ====================

    /**
     * 17 个比较类算法的 sort(List) 与 sort(List, Comparator) 入口
     *
     * @return 算法名 + sort(List) 调用器 + sort(List, Comparator) 调用器
     */
    static Stream<Arguments> comparisonAlgorithmsForList() {
        return Stream.of(
                Arguments.of("BubbleSort",
                        (Consumer<List<Integer>>) BubbleSort::sort,
                        (BiConsumer<List<Integer>, Comparator<Integer>>) BubbleSort::sort),
                Arguments.of("SelectionSort",
                        (Consumer<List<Integer>>) SelectionSort::sort,
                        (BiConsumer<List<Integer>, Comparator<Integer>>) SelectionSort::sort),
                Arguments.of("InsertionSort",
                        (Consumer<List<Integer>>) InsertionSort::sort,
                        (BiConsumer<List<Integer>, Comparator<Integer>>) InsertionSort::sort),
                Arguments.of("ShellSort",
                        (Consumer<List<Integer>>) ShellSort::sort,
                        (BiConsumer<List<Integer>, Comparator<Integer>>) ShellSort::sort),
                Arguments.of("MergeSort",
                        (Consumer<List<Integer>>) MergeSort::sort,
                        (BiConsumer<List<Integer>, Comparator<Integer>>) MergeSort::sort),
                Arguments.of("QuickSort",
                        (Consumer<List<Integer>>) QuickSort::sort,
                        (BiConsumer<List<Integer>, Comparator<Integer>>) QuickSort::sort),
                Arguments.of("HeapSort",
                        (Consumer<List<Integer>>) HeapSort::sort,
                        (BiConsumer<List<Integer>, Comparator<Integer>>) HeapSort::sort),
                Arguments.of("TimSort",
                        (Consumer<List<Integer>>) TimSort::sort,
                        (BiConsumer<List<Integer>, Comparator<Integer>>) TimSort::sort),
                Arguments.of("CombSort",
                        (Consumer<List<Integer>>) CombSort::sort,
                        (BiConsumer<List<Integer>, Comparator<Integer>>) CombSort::sort),
                Arguments.of("GnomeSort",
                        (Consumer<List<Integer>>) GnomeSort::sort,
                        (BiConsumer<List<Integer>, Comparator<Integer>>) GnomeSort::sort),
                Arguments.of("CocktailSort",
                        (Consumer<List<Integer>>) CocktailSort::sort,
                        (BiConsumer<List<Integer>, Comparator<Integer>>) CocktailSort::sort),
                Arguments.of("CycleSort",
                        (Consumer<List<Integer>>) CycleSort::sort,
                        (BiConsumer<List<Integer>, Comparator<Integer>>) CycleSort::sort),
                Arguments.of("OddEvenSort",
                        (Consumer<List<Integer>>) OddEvenSort::sort,
                        (BiConsumer<List<Integer>, Comparator<Integer>>) OddEvenSort::sort),
                Arguments.of("PancakeSort",
                        (Consumer<List<Integer>>) PancakeSort::sort,
                        (BiConsumer<List<Integer>, Comparator<Integer>>) PancakeSort::sort),
                Arguments.of("StoogeSort",
                        (Consumer<List<Integer>>) StoogeSort::sort,
                        (BiConsumer<List<Integer>, Comparator<Integer>>) StoogeSort::sort),
                Arguments.of("BitonicSort",
                        (Consumer<List<Integer>>) BitonicSort::sort,
                        (BiConsumer<List<Integer>, Comparator<Integer>>) BitonicSort::sort),
                Arguments.of("TreeSort",
                        (Consumer<List<Integer>>) TreeSort::sort,
                        (BiConsumer<List<Integer>, Comparator<Integer>>) TreeSort::sort));
    }

    /**
     * 17 个比较类算法的 sort(List)：随机含重复数据与 Collections.sort 一致
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("comparisonAlgorithmsForList")
    void testComparisonAlgorithmsSortList(String name, Consumer<List<Integer>> algorithm,
            BiConsumer<List<Integer>, Comparator<Integer>> comparatorAlgorithm) {
        List<Integer> data = randomIntegers(SEED, 100, 30);

        List<Integer> actual = new ArrayList<>(data);
        List<Integer> expected = new ArrayList<>(data);
        algorithm.accept(actual);
        Collections.sort(expected);
        assertEquals(expected, actual, name + ".sort(List)：排序结果与 Collections.sort 不一致");

        List<Integer> actualReverse = new ArrayList<>(data);
        List<Integer> expectedReverse = new ArrayList<>(data);
        comparatorAlgorithm.accept(actualReverse, Comparator.reverseOrder());
        Collections.sort(expectedReverse, Comparator.reverseOrder());
        assertEquals(expectedReverse, actualReverse,
                name + ".sort(List,Comparator) 逆序比较器：排序结果与 Collections.sort 不一致");
    }

    // ==================== null 元素 ====================

    /**
     * List 含 null 元素时 Comparable 路径 sort(List) → NPE
     */
    @Test
    void testSortListNullElementThrowsNpe() {
        List<Integer> list = new ArrayList<>(Arrays.asList(3, null, 1, null, 2));

        assertThrows(NullPointerException.class, () -> Sort.sort(list),
                "Sort.sort(List)（Comparable 路径）遇 null 元素应抛出 NullPointerException");
    }

    // ==================== 混合形态 ====================

    /**
     * List<String>（大小写混合）与 List<Integer>（含负数与重复）混合形态
     */
    @Test
    void testMixedElementShapes() {
        List<String> words = new ArrayList<>(Arrays.asList("banana", "Apple", "cherry", "apple", "Banana", "date"));
        List<String> expectedWords = new ArrayList<>(words);
        Sort.sort(words);
        Collections.sort(expectedWords);
        assertEquals(expectedWords, words, "List<String> 大小写混合：Sort.sort(List) 与 Collections.sort 结果不一致");

        List<Integer> numbers = new ArrayList<>(Arrays.asList(-5, 3, 0, -1, 3, 7, -5, 12, 3));
        List<Integer> expectedNumbers = new ArrayList<>(numbers);
        Sort.sort(numbers);
        Collections.sort(expectedNumbers);
        assertEquals(expectedNumbers, numbers, "List<Integer> 含负数与重复：Sort.sort(List) 与 Collections.sort 结果不一致");
    }

    // ==================== 辅助 ====================

    private static List<Integer> randomIntegers(long seed, int size, int bound) {
        Random random = new Random(seed);
        List<Integer> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(random.nextInt(bound));
        }
        return list;
    }

    private static List<Integer> newList(Supplier<List<Integer>> factory, List<Integer> data) {
        List<Integer> list = factory.get();
        list.addAll(data);
        return list;
    }
}
