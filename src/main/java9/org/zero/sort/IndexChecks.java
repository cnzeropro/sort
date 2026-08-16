package org.zero.sort;

import java.util.Objects;

/**
 * 区间索引校验工具（Java 9+ 版本，Multi-Release）
 * <p>
 * 本类在打包时进入 META-INF/versions/9/（见 pom.xml 的 compile-java9 execution），
 * Java 9+ 运行时加载 jar 时自动以本类替代基础层的同名类（JEP 238）。
 * 直接委托 JDK 的 {@link Objects#checkFromToIndex}，行为与异常消息
 * 与基础层（Java 8 手写实现）完全一致。
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
     * @return fromIndex
     * @throws IndexOutOfBoundsException fromIndex &lt; 0、toIndex &gt; length 或 fromIndex &gt; toIndex
     */
    static int checkFromToIndex(int fromIndex, int toIndex, int length) {
        return Objects.checkFromToIndex(fromIndex, toIndex, length);
    }
}
