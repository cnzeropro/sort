package org.zero.sort;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Comparator;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * 比较类排序算法的稳定性测试。
 * <p>
 * 构造 300 个 key 值域为 30 的随机 {@link Item}（固定种子 {@value #SEED}，平均每个 key
 * 重复约 10 次），用只比较 key 的 {@link Comparator} 排序后验证：
 * <ul>
 *   <li><b>稳定算法</b>（冒泡、插入、归并、Tim、侏儒、鸡尾酒、奇偶、树排序）：
 *       相同 key 的 seq 必须严格递增（即保持输入相对顺序）；</li>
 *   <li><b>不稳定算法</b>（选择、希尔、快速、堆、梳、循环、煎饼、Stooge、双调）：
 *       不检查相等元素顺序，仅验证按 key 非降序。</li>
 * </ul>
 * 算法通过 {@code @MethodSource} 以"算法名 + 方法引用"的方式注入，逐算法展开为独立用例。
 *
 * @author Zero
 */
class StabilityTest {

    /** 固定随机种子，保证测试可复现 */
    private static final long SEED = 42L;

    /** 只比较 key、忽略 seq 的比较器 */
    private static final Comparator<Item> BY_KEY = (x, y) -> Integer.compare(x.key, y.key);

    /** 稳定性测试载体：key 为排序键，seq 为全局唯一的输入序号 */
    static class Item {
        final int key;
        final int seq;

        Item(int key, int seq) {
            this.key = key;
            this.seq = seq;
        }
    }

    /** 稳定算法集合 */
    static Stream<Arguments> stableAlgorithms() {
        return Stream.of(
                Arguments.of("BubbleSort", (BiConsumer<Item[], Comparator<Item>>) BubbleSort::sort),
                Arguments.of("InsertionSort", (BiConsumer<Item[], Comparator<Item>>) InsertionSort::sort),
                Arguments.of("MergeSort", (BiConsumer<Item[], Comparator<Item>>) MergeSort::sort),
                Arguments.of("TimSort", (BiConsumer<Item[], Comparator<Item>>) TimSort::sort),
                Arguments.of("GnomeSort", (BiConsumer<Item[], Comparator<Item>>) GnomeSort::sort),
                Arguments.of("CocktailSort", (BiConsumer<Item[], Comparator<Item>>) CocktailSort::sort),
                Arguments.of("OddEvenSort", (BiConsumer<Item[], Comparator<Item>>) OddEvenSort::sort),
                Arguments.of("TreeSort", (BiConsumer<Item[], Comparator<Item>>) TreeSort::sort));
    }

    /** 不稳定算法集合 */
    static Stream<Arguments> unstableAlgorithms() {
        return Stream.of(
                Arguments.of("SelectionSort", (BiConsumer<Item[], Comparator<Item>>) SelectionSort::sort),
                Arguments.of("ShellSort", (BiConsumer<Item[], Comparator<Item>>) ShellSort::sort),
                Arguments.of("QuickSort", (BiConsumer<Item[], Comparator<Item>>) QuickSort::sort),
                Arguments.of("HeapSort", (BiConsumer<Item[], Comparator<Item>>) HeapSort::sort),
                Arguments.of("CombSort", (BiConsumer<Item[], Comparator<Item>>) CombSort::sort),
                Arguments.of("CycleSort", (BiConsumer<Item[], Comparator<Item>>) CycleSort::sort),
                Arguments.of("PancakeSort", (BiConsumer<Item[], Comparator<Item>>) PancakeSort::sort),
                Arguments.of("StoogeSort", (BiConsumer<Item[], Comparator<Item>>) StoogeSort::sort),
                Arguments.of("BitonicSort", (BiConsumer<Item[], Comparator<Item>>) BitonicSort::sort));
    }

    /**
     * 稳定算法：排序后相同 key 的元素必须保持输入顺序（seq 严格递增）。
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("stableAlgorithms")
    void stableAlgorithmKeepsRelativeOrder(String name, BiConsumer<Item[], Comparator<Item>> algorithm) {
        Item[] a = items();
        algorithm.accept(a, BY_KEY);
        assertSortedByKey(name, a);
        for (int i = 0; i + 1 < a.length; i++) {
            if (a[i].key == a[i + 1].key) {
                assertTrue(a[i].seq < a[i + 1].seq,
                        name + " 不稳定：key=" + a[i].key + " 的相同元素相对顺序被打乱");
            }
        }
    }

    /**
     * 不稳定算法：仅验证排序后按 key 非降序（不检查相等元素的先后顺序）。
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("unstableAlgorithms")
    void unstableAlgorithmSortedByKey(String name, BiConsumer<Item[], Comparator<Item>> algorithm) {
        Item[] a = items();
        algorithm.accept(a, BY_KEY);
        assertSortedByKey(name, a);
    }

    /** 构造测试数据：300 个 Item，key 随机取值于 [0, 30)，seq 按输入位置递增 */
    private static Item[] items() {
        Random rnd = new Random(SEED);
        Item[] a = new Item[300];
        for (int i = 0; i < a.length; i++) {
            a[i] = new Item(rnd.nextInt(30), i);
        }
        return a;
    }

    /** 断言数组按 key 非降序排列 */
    private static void assertSortedByKey(String name, Item[] a) {
        for (int i = 0; i + 1 < a.length; i++) {
            assertTrue(a[i].key <= a[i + 1].key, name + " 未按 key 非降序排列，位置 " + i);
        }
    }
}
