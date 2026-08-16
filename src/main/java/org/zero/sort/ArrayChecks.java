package org.zero.sort;

/**
 * 数组参数校验工具
 * <p>
 * 行为契约（与 {@link java.util.Arrays} 一致）：
 * <ul>
 *   <li>null 数组 → {@link NullPointerException}</li>
 *   <li>排序区间内包含 null 元素 → {@link NullPointerException}
 *       （区间外的 null 元素不影响，与 JDK 行为一致）</li>
 * </ul>
 * 非法索引的校验见 {@link IndexChecks}（该类为 Multi-Release 类）。
 *
 * @author Zero
 */
final class ArrayChecks {

    private ArrayChecks() {
    }

    /**
     * 校验数组非 null
     *
     * @param <T> 数组元素类型
     * @param a   待校验数组
     * @return 原数组（便于链式使用）
     * @throws NullPointerException 数组为 null
     */
    static <T> T[] requireArray(T[] a) {
        if (a == null) {
            throw new NullPointerException("array must not be null");
        }
        return a;
    }

    /**
     * 校验 [fromIndex, toIndex) 区间内不含 null 元素
     * <p>
     * 只扫描排序区间，与 {@link java.util.Arrays#sort(Object[], int, int)} 行为一致：
     * 区间外的 null 元素不影响本次排序。
     *
     * @param a         待校验数组（调用方保证非 null）
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     * @throws NullPointerException 区间内存在 null 元素
     */
    static void requireNoNullInRange(Object[] a, int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++) {
            if (a[i] == null) {
                throw new NullPointerException(
                        "array must not contain null elements in range [" + fromIndex + ", " + toIndex + ")");
            }
        }
    }
}
