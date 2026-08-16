package org.zero.sort;

/**
 * 排序算法枚举
 * <p>
 * 每种算法的稳定性、适用类型、时间复杂度与空间复杂度如下（n 为元素个数）：
 *
 * <table border="1">
 *   <caption>算法特性一览</caption>
 *   <tr><th>算法</th><th>稳定性</th><th>适用类型</th><th>最好</th><th>平均</th><th>最坏</th><th>空间</th></tr>
 *   <tr><td>{@link #BUBBLE}</td><td>稳定</td><td>全部</td><td>O(n)</td><td>O(n^2)</td><td>O(n^2)</td><td>O(1)</td></tr>
 *   <tr><td>{@link #SELECTION}</td><td>不稳定</td><td>全部</td><td>O(n^2)</td><td>O(n^2)</td><td>O(n^2)</td><td>O(1)</td></tr>
 *   <tr><td>{@link #INSERTION}</td><td>稳定</td><td>全部</td><td>O(n)</td><td>O(n^2)</td><td>O(n^2)</td><td>O(1)</td></tr>
 *   <tr><td>{@link #SHELL}</td><td>不稳定</td><td>全部</td><td>O(n log n)</td><td>约 O(n^1.25)</td><td>O(n^2)</td><td>O(1)</td></tr>
 *   <tr><td>{@link #MERGE}</td><td>稳定</td><td>全部</td><td>O(n log n)</td><td>O(n log n)</td><td>O(n log n)</td><td>O(n)</td></tr>
 *   <tr><td>{@link #QUICK}</td><td>不稳定</td><td>全部</td><td>O(n log n)</td><td>O(n log n)</td><td>O(n^2)</td><td>O(log n)</td></tr>
 *   <tr><td>{@link #HEAP}</td><td>不稳定</td><td>全部</td><td>O(n log n)</td><td>O(n log n)</td><td>O(n log n)</td><td>O(1)</td></tr>
 *   <tr><td>{@link #TIM}</td><td>稳定</td><td>全部</td><td>O(n)</td><td>O(n log n)</td><td>O(n log n)</td><td>O(n)</td></tr>
 *   <tr><td>{@link #COMB}</td><td>不稳定</td><td>全部</td><td>O(n log n)</td><td>O(n^2/2^p)</td><td>O(n^2)</td><td>O(1)</td></tr>
 *   <tr><td>{@link #GNOME}</td><td>稳定</td><td>全部</td><td>O(n)</td><td>O(n^2)</td><td>O(n^2)</td><td>O(1)</td></tr>
 *   <tr><td>{@link #COCKTAIL}</td><td>稳定</td><td>全部</td><td>O(n)</td><td>O(n^2)</td><td>O(n^2)</td><td>O(1)</td></tr>
 *   <tr><td>{@link #CYCLE}</td><td>不稳定</td><td>全部</td><td>O(n^2)</td><td>O(n^2)</td><td>O(n^2)</td><td>O(1)</td></tr>
 *   <tr><td>{@link #ODD_EVEN}</td><td>稳定</td><td>全部</td><td>O(n)</td><td>O(n^2)</td><td>O(n^2)</td><td>O(1)</td></tr>
 *   <tr><td>{@link #PANCAKE}</td><td>不稳定</td><td>全部</td><td>O(n^2)</td><td>O(n^2)</td><td>O(n^2)</td><td>O(1)</td></tr>
 *   <tr><td>{@link #STOOGE}</td><td>不稳定</td><td>全部</td><td>O(n^2.71)</td><td>O(n^2.71)</td><td>O(n^2.71)</td><td>O(n)</td></tr>
 *   <tr><td>{@link #BITONIC}</td><td>不稳定</td><td>全部</td><td>O(n log^2 n)</td><td>O(n log^2 n)</td><td>O(n log^2 n)</td><td>O(n)</td></tr>
 *   <tr><td>{@link #TREE}</td><td>稳定</td><td>全部</td><td>O(n log n)</td><td>O(n log n)</td><td>O(n^2)</td><td>O(n)</td></tr>
 *   <tr><td>{@link #COUNTING}</td><td>稳定</td><td>仅积分原始类型</td><td>O(n+k)</td><td>O(n+k)</td><td>O(n+k)</td><td>O(k)</td></tr>
 *   <tr><td>{@link #RADIX}</td><td>稳定</td><td>仅积分原始类型</td><td>O(n·w)</td><td>O(n·w)</td><td>O(n·w)</td><td>O(n)</td></tr>
 *   <tr><td>{@link #PIGEONHOLE}</td><td>稳定</td><td>仅积分原始类型</td><td>O(n+k)</td><td>O(n+k)</td><td>O(n+k)</td><td>O(k)</td></tr>
 *   <tr><td>{@link #BUCKET}</td><td>稳定</td><td>仅 float/double</td><td>O(n)</td><td>O(n+k)</td><td>O(n^2)</td><td>O(n+k)</td></tr>
 * </table>
 *
 * <p>说明：
 * <ul>
 *   <li>"全部" = 任意 Comparable/Comparator 对象数组与全部数字原始类型；
 *       "仅积分原始类型" = byte/short/int/long/char；"仅 float/double" = float 与 double。</li>
 *   <li>稳定排序保证相等元素的相对顺序在排序前后保持不变。</li>
 *   <li>{@link #QUICK} 使用双轴划分（JDK 双轴快排同款思想），常见输入不退化；
 *       构造性敌手输入仍可触发最坏 O(n^2)。</li>
 *   <li>{@link #TREE} 的对象实现基于红黑树（TreeMap，保证 O(n log n)）；
 *       原始类型实现为朴素二叉搜索树（不装箱的代价），有序输入会退化为 O(n^2)。</li>
 *   <li>{@link #COUNTING}/{@link #PIGEONHOLE} 要求值域 range = max - min + 1
 *       不超过 {@code 1 << 24}，超限抛 {@link IllegalArgumentException}。</li>
 *   <li>{@link #BITONIC} 对任意长度可用（内部补到 2 的幂），要求 n ≤ 2^30。</li>
 *   <li>{@link #BUCKET} 对 NaN/±Infinity 有专门处理，比较语义与其他算法一致（NaN 最后）。</li>
 *   <li>刻意不含 bogo/sleep 等恶搞算法（无界耗时，无法可靠使用与测试）。</li>
 * </ul>
 *
 * @author Zero
 * @see Sort
 */
public enum Algorithm {

    /**
     * 冒泡排序：稳定，带提前退出优化；最好 O(n)，平均/最坏 O(n^2)；空间 O(1)。
     */
    BUBBLE(true, Applicability.ALL),

    /**
     * 选择排序：不稳定；最好/平均/最坏均为 O(n^2)；空间 O(1)。
     */
    SELECTION(false, Applicability.ALL),

    /**
     * 插入排序：稳定；最好 O(n)（已有序输入），平均/最坏 O(n^2)；空间 O(1)。
     * 对接近有序的输入表现优异，同时作为快速/归并/Tim 排序的小数组底层实现。
     */
    INSERTION(true, Applicability.ALL),

    /**
     * 希尔排序：不稳定；Knuth 增量序列 h = 3h + 1；
     * 最好 O(n log n)，平均约 O(n^1.25)，最坏 O(n^2)；空间 O(1)。
     */
    SHELL(false, Applicability.ALL),

    /**
     * 归并排序：稳定；最好/平均/最坏均保证 O(n log n)；空间 O(n)。
     * 辅助数组只在入口分配一次，小数组回退插入排序，已有序片段跳过合并。
     */
    MERGE(true, Applicability.ALL),

    /**
     * 快速排序：不稳定；双轴划分（Yaroslavskiy，三取样选双枢轴、相等枢轴回退单轴）+ 小数组回退插入排序；
     * 最好/平均 O(n log n)，最坏 O(n^2)；空间 O(log n)。有序/逆序/全相等输入不退化。
     */
    QUICK(false, Applicability.ALL),

    /**
     * 堆排序：不稳定；原地最大堆；最好/平均/最坏均 O(n log n)；空间 O(1)。
     */
    HEAP(false, Applicability.ALL),

    /**
     * Tim 排序：稳定；自适应——检测并利用已有序片段（run），已有序输入 O(n)，
     * 最坏 O(n log n)；空间 O(n)。为 {@code Sort.sort(a)} 系列的默认算法
     * （与 JDK {@link java.util.Arrays#sort(Object[])} 的算法一致）。
     */
    TIM(true, Applicability.ALL),

    /**
     * 梳排序：不稳定；收缩因子 1.3 的冒泡改进；最好 O(n log n)，最坏 O(n^2)；空间 O(1)。
     */
    COMB(false, Applicability.ALL),

    /**
     * 地精排序：稳定；最好 O(n)，平均/最坏 O(n^2)；空间 O(1)。
     */
    GNOME(true, Applicability.ALL),

    /**
     * 鸡尾酒排序（双向冒泡）：稳定；最好 O(n)，平均/最坏 O(n^2)；空间 O(1)。
     */
    COCKTAIL(true, Applicability.ALL),

    /**
     * 循环排序：不稳定；以最少写入次数著称（每个元素最多写一次）；
     * 最好/平均/最坏 O(n^2)；空间 O(1)。
     */
    CYCLE(false, Applicability.ALL),

    /**
     * 奇偶排序：稳定；奇偶相两阶段冒泡，可并行化；最好 O(n)，平均/最坏 O(n^2)；空间 O(1)。
     */
    ODD_EVEN(true, Applicability.ALL),

    /**
     * 煎饼排序：不稳定；通过前缀翻转排序；最好/平均/最坏 O(n^2)；空间 O(1)。
     */
    PANCAKE(false, Applicability.ALL),

    /**
     * 臭皮匠排序：不稳定；递归三分法，纯教育用途；
     * 最好/平均/最坏 O(n^2.71)；空间 O(n)（递归栈）。
     */
    STOOGE(false, Applicability.ALL),

    /**
     * 双调排序：不稳定；比较网络，天然适合并行硬件；
     * 最好/平均/最坏 O(n log^2 n)；空间 O(n)。
     * 对任意长度可用（内部补齐到 2 的幂），要求 n ≤ 2^30。
     */
    BITONIC(false, Applicability.ALL),

    /**
     * 树排序：稳定；对象实现基于红黑树（保证 O(n log n)）；
     * 原始类型实现为朴素二叉搜索树，有序输入退化为最坏 O(n^2)；空间 O(n)。
     */
    TREE(true, Applicability.ALL),

    /**
     * 计数排序：稳定；仅适用于 byte/short/int/long/char；
     * O(n+k)（k = 值域大小）；值域上限 1 &lt;&lt; 24，超限抛 IllegalArgumentException。
     */
    COUNTING(true, Applicability.INTEGRALS_ONLY),

    /**
     * 基数排序（LSD，base-256）：稳定；仅适用于 byte/short/int/long/char；
     * O(n·w)（w = 字节宽度，1~8）；空间 O(n)。负数按二进制补码正确排序。
     */
    RADIX(true, Applicability.INTEGRALS_ONLY),

    /**
     * 鸽巢排序：稳定；仅适用于 byte/short/int/long/char；
     * O(n+k)（k = 值域大小），计数排序的变体；值域上限 1 &lt;&lt; 24，
     * 超限抛 IllegalArgumentException。
     */
    PIGEONHOLE(true, Applicability.INTEGRALS_ONLY),

    /**
     * 桶排序：稳定；仅适用于 float/double；均匀分布输入接近 O(n)，最坏 O(n^2)；
     * 空间 O(n+k)。NaN 排在最后、±Infinity 归首尾桶，与其他算法的比较语义一致。
     */
    BUCKET(true, Applicability.FLOATS_ONLY);

    /** 是否为稳定排序算法 */
    private final boolean stable;

    /** 适用类型范围 */
    private final Applicability applicability;

    Algorithm(boolean stable, Applicability applicability) {
        this.stable = stable;
        this.applicability = applicability;
    }

    /**
     * 返回该算法是否为稳定排序
     *
     * @return 稳定排序返回 true，否则 false
     */
    public boolean isStable() {
        return stable;
    }

    /**
     * 返回该算法的适用类型范围
     *
     * @return 适用类型范围枚举
     */
    public Applicability applicability() {
        return applicability;
    }

    /**
     * 算法适用类型范围
     *
     * @author Zero
     */
    public enum Applicability {

        /** 任意 Comparable/Comparator 对象数组与全部数字原始类型 */
        ALL,

        /** 仅积分原始类型：byte / short / int / long / char */
        INTEGRALS_ONLY,

        /** 仅浮点原始类型：float / double */
        FLOATS_ONLY
    }
}
