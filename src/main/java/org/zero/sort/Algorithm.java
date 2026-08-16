package org.zero.sort;

/**
 * 排序算法枚举
 * <p>
 * 每种算法的稳定性、时间复杂度与空间复杂度如下（n 为元素个数）：
 *
 * <table border="1">
 *   <caption>算法特性一览</caption>
 *   <tr><th>算法</th><th>稳定性</th><th>最好时间复杂度</th><th>平均时间复杂度</th><th>最坏时间复杂度</th><th>空间复杂度</th></tr>
 *   <tr><td>{@link #BUBBLE}</td><td>稳定</td><td>O(n)</td><td>O(n^2)</td><td>O(n^2)</td><td>O(1)</td></tr>
 *   <tr><td>{@link #SELECTION}</td><td>不稳定</td><td>O(n^2)</td><td>O(n^2)</td><td>O(n^2)</td><td>O(1)</td></tr>
 *   <tr><td>{@link #INSERTION}</td><td>稳定</td><td>O(n)</td><td>O(n^2)</td><td>O(n^2)</td><td>O(1)</td></tr>
 *   <tr><td>{@link #SHELL}</td><td>不稳定</td><td>O(n log n)</td><td>约 O(n^1.25)</td><td>O(n^2)</td><td>O(1)</td></tr>
 *   <tr><td>{@link #MERGE}</td><td>稳定</td><td>O(n log n)</td><td>O(n log n)</td><td>O(n log n)</td><td>O(n)</td></tr>
 *   <tr><td>{@link #QUICK}</td><td>不稳定</td><td>O(n log n)</td><td>O(n log n)</td><td>O(n^2)</td><td>O(log n)</td></tr>
 * </table>
 *
 * <p>说明：
 * <ul>
 *   <li>稳定排序保证相等元素的相对顺序在排序前后保持不变。</li>
 *   <li>{@link #QUICK} 使用三数取中（median-of-three）选枢轴并优先递归较小分区，
 *       常见输入（有序、逆序、全相等）不会退化，但构造性敌手输入仍可触发最坏 O(n^2)。</li>
 * </ul>
 *
 * @author Zero
 * @see Sort
 */
public enum Algorithm {

    /**
     * 冒泡排序
     * <p>
     * 稳定；最好 O(n)（提前退出优化），平均/最坏 O(n^2)；空间 O(1)。
     */
    BUBBLE(true),

    /**
     * 选择排序
     * <p>
     * 不稳定；最好/平均/最坏均为 O(n^2)；空间 O(1)。
     */
    SELECTION(false),

    /**
     * 插入排序
     * <p>
     * 稳定；最好 O(n)（已有序输入），平均/最坏 O(n^2)；空间 O(1)。
     * 对接近有序的输入表现优异，同时作为快速/归并排序的小数组底层实现。
     */
    INSERTION(true),

    /**
     * 希尔排序
     * <p>
     * 不稳定；采用 Knuth 增量序列 h = 3h + 1；
     * 最好 O(n log n)，平均约 O(n^1.25)，最坏 O(n^2)；空间 O(1)。
     */
    SHELL(false),

    /**
     * 归并排序
     * <p>
     * 稳定；最好/平均/最坏均保证 O(n log n)；空间 O(n)。
     * 辅助数组只在入口分配一次，小数组回退插入排序，已有序片段跳过合并。
     */
    MERGE(true),

    /**
     * 快速排序
     * <p>
     * 不稳定；最好/平均 O(n log n)，最坏 O(n^2)；空间 O(log n)（递归栈）。
     * 使用三数取中选枢轴 + Hoare 交叉指针划分 + 小数组（&lt; 16）回退插入排序 +
     * 优先递归较小分区限制栈深；有序/逆序/全相等输入不退化。
     */
    QUICK(false);

    /** 是否为稳定排序算法 */
    private final boolean stable;

    Algorithm(boolean stable) {
        this.stable = stable;
    }

    /**
     * 返回该算法是否为稳定排序
     *
     * @return 稳定排序返回 true，否则 false
     */
    public boolean isStable() {
        return stable;
    }
}
