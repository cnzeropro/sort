package org.zero.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * List 重载测试
 * <p>
 * 验证 List 版本（默认/算法/Comparator/算法+Comparator）的原地排序正确性、
 * subList 区间用法、空 List 合法性、null 校验与不可变 List 回写失败行为。
 *
 * @author Zero
 */
public class SortListTest {

    private static final long SEED = 42L;

    private static List<Integer> randomList(int size, int bound) {
        List<Integer> list = new ArrayList<Integer>(size);
        Random random = new Random(SEED);
        for (int i = 0; i < size; i++) {
            list.add(random.nextInt(bound));
        }
        return list;
    }

    private static List<Integer> sortedCopy(List<Integer> list) {
        List<Integer> copy = new ArrayList<Integer>(list);
        Collections.sort(copy);
        return copy;
    }

    @Test
    void sortsListWithDefaultAlgorithm() {
        List<Integer> list = randomList(500, 5000);
        List<Integer> expected = sortedCopy(list);
        Sort.sort(list);
        assertEquals(expected, list, "默认 List 排序失败");
    }

    @Test
    void sortsListWithEveryApplicableAlgorithm() {
        for (Algorithm algorithm : Algorithm.values()) {
            if (algorithm.applicability() != Algorithm.Applicability.ALL) {
                continue;
            }
            int size = algorithm == Algorithm.STOOGE ? 60 : 200;
            List<Integer> list = randomList(size, 1000);
            List<Integer> expected = sortedCopy(list);
            Sort.sort(list, algorithm);
            assertEquals(expected, list, algorithm + " List 排序失败");
        }
    }

    @Test
    void sortsListWithComparator() {
        List<Integer> list = randomList(300, 1000);
        List<Integer> expected = new ArrayList<Integer>(list);
        Collections.sort(expected, Comparator.reverseOrder());
        Sort.sort(list, Comparator.reverseOrder());
        assertEquals(expected, list, "Comparator List 排序失败");
    }

    @Test
    void sortsListWithAlgorithmAndComparator() {
        List<Integer> list = randomList(200, 1000);
        List<Integer> expected = new ArrayList<Integer>(list);
        Collections.sort(expected, Comparator.reverseOrder());
        Sort.sort(list, Algorithm.HEAP, Comparator.reverseOrder());
        assertEquals(expected, list, "算法 + Comparator List 排序失败");
    }

    @Test
    void sortsSublist() {
        List<Integer> list = new ArrayList<Integer>(Arrays.asList(9, 8, 7, 6, 5, 4, 3, 2, 1));
        List<Integer> expected = new ArrayList<Integer>(list);
        Collections.sort(expected.subList(2, 6));
        Sort.sort(list.subList(2, 6));
        assertEquals(expected, list, "subList 区间排序失败");
    }

    @Test
    void sortsEmptyList() {
        List<Integer> list = new ArrayList<Integer>();
        Sort.sort(list);
        assertEquals(0, list.size());
    }

    @Test
    void rejectsNullList() {
        assertThrows(NullPointerException.class, () -> Sort.sort((List<Integer>) null));
        assertThrows(NullPointerException.class, () -> Sort.sort((List<Integer>) null, Algorithm.MERGE));
    }

    @Test
    void rejectsNullAlgorithmOnList() {
        List<Integer> list = randomList(10, 100);
        assertThrows(NullPointerException.class, () -> Sort.sort(list, (Algorithm) null));
    }

    @Test
    void rejectsNullComparatorOnList() {
        List<Integer> list = randomList(10, 100);
        assertThrows(NullPointerException.class, () -> Sort.sort(list, (Comparator<Integer>) null));
    }

    @Test
    void rejectsInapplicableAlgorithmOnList() {
        List<Integer> list = randomList(10, 100);
        assertThrows(IllegalArgumentException.class, () -> Sort.sort(list, Algorithm.COUNTING));
    }

    @Test
    void unmodifiableListThrowsOnWriteBack() {
        List<Integer> list = Collections.unmodifiableList(new ArrayList<Integer>(Arrays.asList(3, 1, 2)));
        // 不可变 List：排序计算完成但回写抛 UnsupportedOperationException（JDK 惯例）
        assertThrows(UnsupportedOperationException.class, () -> Sort.sort(list));
    }
}
