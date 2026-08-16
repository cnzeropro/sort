package org.zero.sort.benchmark;

/**
 * 基准输入形态
 *
 * @author Zero
 */
public enum BenchShape {

    /** 均匀随机 */
    RANDOM,

    /** 已升序 */
    SORTED,

    /** 降序 */
    REVERSE,

    /** 大量重复（值域 16） */
    DUPLICATES,

    /** 近有序（升序 + 少量相邻交换） */
    NEAR_SORTED
}
