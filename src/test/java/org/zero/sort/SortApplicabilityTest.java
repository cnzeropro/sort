package org.zero.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * 算法适用性校验测试
 * <p>
 * 验证 {@link Algorithm.Applicability} 契约：
 * <ul>
 *   <li>对象数组与 List 拒绝仅积分/仅浮点算法；</li>
 *   <li>积分原始类型拒绝仅浮点算法；</li>
 *   <li>浮点原始类型拒绝仅积分算法；</li>
 *   <li>异常优先级：null 数组/算法/索引校验在前，适用性校验在后。</li>
 * </ul>
 *
 * @author Zero
 */
public class SortApplicabilityTest {

    @Test
    void objectArraysRejectIntegralOnlyAlgorithms() {
        Integer[] a = new Integer[] {3, 1, 2};
        for (Algorithm algorithm : new Algorithm[] {
            Algorithm.COUNTING, Algorithm.RADIX, Algorithm.PIGEONHOLE, Algorithm.BUCKET
        }) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> Sort.sort(a, algorithm),
                    algorithm + " 应被对象数组拒绝");
        }
    }

    @Test
    void listsRejectIntegralOnlyAlgorithms() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(3);
        list.add(1);
        assertThrows(IllegalArgumentException.class, () -> Sort.sort(list, Algorithm.RADIX));
        assertThrows(IllegalArgumentException.class, () -> Sort.sort(list, Algorithm.BUCKET));
    }

    @Test
    void integralPrimitivesRejectFloatOnlyAlgorithms() {
        int[] a = new int[] {3, 1, 2};
        long[] b = new long[] {3, 1, 2};
        short[] c = new short[] {3, 1, 2};
        byte[] d = new byte[] {3, 1, 2};
        char[] e = new char[] {'c', 'a', 'b'};
        assertThrows(IllegalArgumentException.class, () -> Sort.sort(a, Algorithm.BUCKET));
        assertThrows(IllegalArgumentException.class, () -> Sort.sort(b, Algorithm.BUCKET));
        assertThrows(IllegalArgumentException.class, () -> Sort.sort(c, Algorithm.BUCKET));
        assertThrows(IllegalArgumentException.class, () -> Sort.sort(d, Algorithm.BUCKET));
        assertThrows(IllegalArgumentException.class, () -> Sort.sort(e, Algorithm.BUCKET));
    }

    @Test
    void floatingPrimitivesRejectIntegralOnlyAlgorithms() {
        float[] a = new float[] {3f, 1f, 2f};
        double[] b = new double[] {3d, 1d, 2d};
        for (Algorithm algorithm : new Algorithm[] {
            Algorithm.COUNTING, Algorithm.RADIX, Algorithm.PIGEONHOLE
        }) {
            assertThrows(IllegalArgumentException.class, () -> Sort.sort(a, algorithm));
            assertThrows(IllegalArgumentException.class, () -> Sort.sort(b, algorithm));
        }
    }

    @Test
    void allAlgorithmsApplyToIntegralAndFloatingPrimitives() {
        // ALL 类算法在两个原始类型族上都可用（冒烟）
        int[] ints = new int[] {5, 3, 8, 1};
        double[] doubles = new double[] {5d, 3d, 8d, 1d};
        for (Algorithm algorithm : Algorithm.values()) {
            if (algorithm.applicability() != Algorithm.Applicability.ALL) {
                continue;
            }
            Sort.sort(ints, algorithm);
            Sort.sort(doubles, algorithm);
            assertEquals(1, ints[0]);
            assertEquals(1d, doubles[0]);
        }
    }

    @Test
    void exceptionPriorityIndexBeforeApplicability() {
        // 校验顺序：NPE/IOOBE 先于适用性 IAE（与 JDK "先边界"惯例一致）
        int[] a = new int[] {3, 1, 2};
        assertThrows(
                IndexOutOfBoundsException.class,
                () -> Sort.sort(a, Algorithm.BUCKET, -1, 3),
                "非法索引应先于适用性抛 IOOBE");

        Integer[] b = new Integer[] {3, 1, 2};
        assertThrows(
                IndexOutOfBoundsException.class,
                () -> Sort.sort(b, Algorithm.COUNTING, 0, 4),
                "非法索引应先于适用性抛 IOOBE");
    }

    @Test
    void applicabilityGetterIsPublicApi() {
        assertEquals(Algorithm.Applicability.ALL, Algorithm.QUICK.applicability());
        assertEquals(Algorithm.Applicability.INTEGRALS_ONLY, Algorithm.RADIX.applicability());
        assertEquals(Algorithm.Applicability.FLOATS_ONLY, Algorithm.BUCKET.applicability());
    }
}
