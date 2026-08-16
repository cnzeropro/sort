package org.zero.sort;

/**
 * 区间索引校验工具（基础层，Java 8）
 * <p>
 * 本类是 Multi-Release 类：Java 9+ 运行时从 jar 包加载
 * META-INF/versions/9 下的同名类（委托 {@code java.util.Objects.checkFromToIndex}），
 * 行为与异常消息与本类完全一致；本类为 Java 8 的手写等价实现。
 *
 * @author Zero
 */
final class IndexChecks {

    private IndexChecks() {
    }

    /**
     * 校验 [fromIndex, toIndex) 区间对长度为 length 的数组是否合法
     *
     * @param fromIndex 起始索引（含）
     * @param toIndex   结束索引（不含）
     * @param length    数组长度
     * @return fromIndex（与 {@code java.util.Objects.checkFromToIndex} 一致的返回值约定）
     * @throws IndexOutOfBoundsException fromIndex &lt; 0、toIndex &gt; length 或 fromIndex &gt; toIndex
     */
    static int checkFromToIndex(int fromIndex, int toIndex, int length) {
        if (fromIndex < 0 || fromIndex > toIndex || toIndex > length) {
            throw new IndexOutOfBoundsException(
                    "Range [" + fromIndex + ", " + toIndex + ") out of bounds for length " + length);
        }
        return fromIndex;
    }
}
