#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""生成原始类型专用的排序实现与测试。

产出文件（勿手改，修改请编辑本脚本后重新生成）：
- src/main/java/org/zero/sort/{Byte,Short,Int,Long,Float,Double,Char}Sorts.java
- src/test/java/org/zero/sort/{Byte,Short,Int,Long,Float,Double,Char}SortsTest.java
- src/main/java/org/zero/sort/PrimitiveDispatcher.java

用法：
    python tools/gen-primitives.py

生成结果确定性可重复（无时间戳）；CI 有"重跑脚本 + git diff --exit-code"一致性门禁。

比较语义（严禁混用两族比较，±0.0 下非传递比较会导致排序错误）：
- byte/short/int/long/char：原生 > < 比较（char 无符号提升为 int 后天然正确）
- float/double：Float.compare / Double.compare 全序（NaN 最后，-0.0 < 0.0）
"""
import os

ROOT = os.path.normpath(os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))
MAIN_DIR = os.path.join(ROOT, "src", "main", "java", "org", "zero", "sort", "internal")
TEST_DIR = os.path.join(ROOT, "src", "test", "java", "org", "zero", "sort")

# 类型表：类名前缀 / 原始类型 / 比较体 / 比较语义说明 / 极值 / 字面量后缀 / 负一样本 / 特殊值测试
TYPES = [
    ("Byte", "byte", "a[i] > a[j]", "a[i] > key", "a[i] < key",
     "有符号 8 位整数自然序", "Byte.MAX_VALUE", "Byte.MIN_VALUE", "", "-1", None),
    ("Short", "short", "a[i] > a[j]", "a[i] > key", "a[i] < key",
     "有符号 16 位整数自然序", "Short.MAX_VALUE", "Short.MIN_VALUE", "", "-1", None),
    ("Int", "int", "a[i] > a[j]", "a[i] > key", "a[i] < key",
     "有符号 32 位整数自然序", "Integer.MAX_VALUE", "Integer.MIN_VALUE", "", "-1", None),
    ("Long", "long", "a[i] > a[j]", "a[i] > key", "a[i] < key",
     "有符号 64 位整数自然序", "Long.MAX_VALUE", "Long.MIN_VALUE", "", "-1", None),
    ("Float", "float", "Float.compare(a[i], a[j]) > 0", "Float.compare(a[i], key) > 0",
     "Float.compare(a[i], key) < 0",
     "Float.compare 全序：NaN 排在最后，-0.0 < 0.0", "Float.MAX_VALUE", "Float.MIN_VALUE", "f", "-1f",
     ("Float.NaN", "Float.POSITIVE_INFINITY", "Float.NEGATIVE_INFINITY", "Float.MIN_NORMAL")),
    ("Double", "double", "Double.compare(a[i], a[j]) > 0", "Double.compare(a[i], key) > 0",
     "Double.compare(a[i], key) < 0",
     "Double.compare 全序：NaN 排在最后，-0.0 < 0.0", "Double.MAX_VALUE", "Double.MIN_VALUE", "d", "-1d",
     ("Double.NaN", "Double.POSITIVE_INFINITY", "Double.NEGATIVE_INFINITY", "Double.MIN_NORMAL")),
    ("Char", "char", "a[i] > a[j]", "a[i] > key", "a[i] < key",
     "无符号 16 位整数序（'\\u0000' 最小，'\\uFFFF' 最大）", "'\\uFFFF'", "'\\u0000'", "", "'a'", None),
]

SORTS_TEMPLATE = """package org.zero.sort;

/**
 * @TYPE@[] 专用排序实现
 * <p>
 * ★ 本文件由 tools/gen-primitives.py 自动生成，请勿手改；如需修改请编辑脚本后重新生成。
 * <p>
 * 算法与 {@link GenericSorts}（对象数组版）完全一致：严格比较、相同稳定性、
 * 三数取中快速排序、单次分配归并排序。比较语义：@CMP_DOC@。
 *
 * @author Zero
 */
final class @PREFIX@Sorts {

    private @PREFIX@Sorts() {
    }

    /** 插入排序阈值：小于该长度的区间使用插入排序 */
    static final int INSERTION_THRESHOLD = 16;

    /** 严格大于比较：@CMP_DOC@ */
    private static boolean gt(@TYPE@[] a, int i, int j) {
        return @GT_BODY@;
    }

    /** 元素与基准值的严格大于比较 */
    private static boolean gtKey(@TYPE@[] a, int i, @TYPE@ key) {
        return @GT_KEY_BODY@;
    }

    /** 元素与基准值的严格小于比较 */
    private static boolean ltKey(@TYPE@[] a, int i, @TYPE@ key) {
        return @LT_KEY_BODY@;
    }

    /** 交换数组 a 的 i、j 位置元素 */
    private static void swap(@TYPE@[] a, int i, int j) {
        @TYPE@ tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    /**
     * 冒泡排序：稳定，最好 O(n)，平均/最坏 O(n^2)，空间 O(1)
     */
    static void bubble(@TYPE@[] a, int from, int to) {
        for (int i = to - 1; i > from; i--) {
            // 本轮无交换说明已有序，提前退出
            boolean swapped = false;
            for (int j = from; j < i; j++) {
                if (gt(a, j, j + 1)) {
                    swap(a, j, j + 1);
                    swapped = true;
                }
            }
            if (!swapped) {
                break;
            }
        }
    }

    /**
     * 选择排序：不稳定，最好/平均/最坏均为 O(n^2)，空间 O(1)
     */
    static void selection(@TYPE@[] a, int from, int to) {
        for (int i = from; i < to - 1; i++) {
            int min = i;
            for (int j = i + 1; j < to; j++) {
                if (gt(a, min, j)) {
                    min = j;
                }
            }
            if (min != i) {
                swap(a, i, min);
            }
        }
    }

    /**
     * 插入排序（稳定，移位式）
     * <p>
     * 同时作为快速/归并排序的小数组底层实现。
     */
    static void insertion(@TYPE@[] a, int from, int to) {
        for (int i = from + 1; i < to; i++) {
            @TYPE@ key = a[i];
            int j = i - 1;
            while (j >= from && gtKey(a, j, key)) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }

    /**
     * 希尔排序（不稳定，Knuth 增量序列 h = 3h + 1，移位式）
     */
    static void shell(@TYPE@[] a, int from, int to) {
        int n = to - from;
        int h = 1;
        while (h < n / 3) {
            h = 3 * h + 1;
        }
        while (h >= 1) {
            for (int i = from + h; i < to; i++) {
                @TYPE@ key = a[i];
                int j = i;
                while (j - h >= from && gtKey(a, j - h, key)) {
                    a[j] = a[j - h];
                    j -= h;
                }
                a[j] = key;
            }
            h /= 3;
        }
    }

    /**
     * 归并排序（稳定）
     * <p>
     * 辅助数组只在入口分配一次并沿递归传递；小数组回退插入排序；
     * 左侧最大值不超过右侧最小值时跳过合并。
     */
    static void merge(@TYPE@[] a, int from, int to) {
        @TYPE@[] aux = new @TYPE@[to - from];
        mergeRec(a, from, to, aux);
    }

    /**
     * 归并排序递归实现
     * <p>
     * aux 与 [from, to) 等长且相对 a 偏移 -from：aux 索引 = 数组索引 - from。
     */
    private static void mergeRec(@TYPE@[] a, int from, int to, @TYPE@[] aux) {
        int n = to - from;
        if (n < INSERTION_THRESHOLD) {
            insertion(a, from, to);
            return;
        }
        int mid = from + n / 2;
        mergeRec(a, from, mid, aux);
        mergeRec(a, mid, to, aux);
        // 左侧最大值 <= 右侧最小值：区间已整体有序，无需合并
        if (!gt(a, mid - 1, mid)) {
            return;
        }
        System.arraycopy(a, from, aux, 0, n);
        int i = 0;
        int j = mid - from;
        int k = from;
        int midOffset = mid - from;
        while (i < midOffset && j < n) {
            // 相等时取左侧，保证稳定性
            if (gt(aux, i, j)) {
                a[k++] = aux[j++];
            } else {
                a[k++] = aux[i++];
            }
        }
        while (i < midOffset) {
            a[k++] = aux[i++];
        }
        // 右侧剩余元素已就位，无需回拷
    }

    /**
     * 快速排序（不稳定）
     * <p>
     * 三数取中选枢轴 + Hoare 交叉指针划分 + 小数组回退插入排序 +
     * 优先递归较小分区；有序/逆序/全相等输入不退化，敌手输入最坏 O(n^2)。
     */
    static void quick(@TYPE@[] a, int from, int to) {
        quickRec(a, from, to);
    }

    /**
     * 快速排序递归实现
     */
    private static void quickRec(@TYPE@[] a, int from, int to) {
        int n = to - from;
        if (n < INSERTION_THRESHOLD) {
            insertion(a, from, to);
            return;
        }
        // 三数取中：排序网络使 a[from] <= a[mid] <= a[to-1]，
        // 再把中值换到枢轴位 a[from]，规避有序/逆序输入退化
        int mid = from + n / 2;
        if (gt(a, from, mid)) {
            swap(a, from, mid);
        }
        if (gt(a, from, to - 1)) {
            swap(a, from, to - 1);
        }
        if (gt(a, mid, to - 1)) {
            swap(a, mid, to - 1);
        }
        swap(a, from, mid);

        @TYPE@ pivot = a[from];
        // Hoare 交叉指针划分：相等元素两端都前进，全相等输入在中间交叉、近似对半
        int i = from;
        int j = to;
        while (true) {
            do {
                i++;
            } while (ltKey(a, i, pivot));
            do {
                j--;
            } while (gtKey(a, j, pivot));
            if (i >= j) {
                break;
            }
            swap(a, i, j);
        }
        swap(a, from, j);
        // 优先递归较小的一侧，限制递归深度
        if (j - from < to - (j + 1)) {
            quickRec(a, from, j);
            quickRec(a, j + 1, to);
        } else {
            quickRec(a, j + 1, to);
            quickRec(a, from, j);
        }
    }
}
"""

TEST_TEMPLATE = """package org.zero.sort;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * @TYPE@[] 排序测试
 * <p>
 * ★ 本文件由 tools/gen-primitives.py 自动生成，请勿手改；如需修改请编辑脚本后重新生成。
 * <p>
 * 通过公共 API（{@link Sort}）对 @TYPE@[] 的全部 6 种算法做矩阵式验证，
 * 期望结果以 {@link Arrays#sort} 为 oracle（比较语义与实现一致：@CMP_DOC@）。
 *
 * @author Zero
 */
public class @PREFIX@SortsTest {

    /** 固定随机种子，保证测试结果可复现 */
    private static final long SEED = 42L;

    /** 生成固定种子的随机数组，bound 越小重复元素越多 */
    private static @TYPE@[] randomArray(int size, int bound) {
        @TYPE@[] a = new @TYPE@[size];
        Random random = new Random(SEED);
        for (int i = 0; i < size; i++) {
            a[i] = (@TYPE@) random.nextInt(bound);
        }
        return a;
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsRandomArray(Algorithm algorithm) {
        @TYPE@[] a = randomArray(1000, 10000);
        @TYPE@[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on random array");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsAlreadySortedArray(Algorithm algorithm) {
        @TYPE@[] a = randomArray(100, 1000);
        Arrays.sort(a);
        @TYPE@[] expected = a.clone();
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on sorted array");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsReverseSortedArray(Algorithm algorithm) {
        @TYPE@[] a = new @TYPE@[100];
        for (int i = 0; i < a.length; i++) {
            a[i] = (@TYPE@) (a.length - i);
        }
        @TYPE@[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on reverse sorted array");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsArrayWithDuplicates(Algorithm algorithm) {
        @TYPE@[] a = randomArray(100, 5);
        @TYPE@[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on array with duplicates");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsSingleElementArray(Algorithm algorithm) {
        @TYPE@[] a = new @TYPE@[] {7};
        Sort.sort(a, algorithm);
        assertArrayEquals(new @TYPE@[] {7}, a, algorithm + " failed on single element array");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsTwoElementArray(Algorithm algorithm) {
        @TYPE@[] a = new @TYPE@[] {9, 2};
        Sort.sort(a, algorithm);
        assertArrayEquals(new @TYPE@[] {2, 9}, a, algorithm + " failed on two element array");

        @TYPE@[] b = new @TYPE@[] {2, 9};
        Sort.sort(b, algorithm);
        assertArrayEquals(new @TYPE@[] {2, 9}, b, algorithm + " failed on sorted two element array");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsEmptyArray(Algorithm algorithm) {
        // 空数组合法，排序为空操作（与 Arrays.sort 一致）
        @TYPE@[] a = new @TYPE@[0];
        Sort.sort(a, algorithm);
        assertArrayEquals(new @TYPE@[0], a, algorithm + " failed on empty array");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsSubrangeOnly(Algorithm algorithm) {
        @TYPE@[] a = new @TYPE@[] {9, 8, 7, 6, 5, 4, 3, 2, 1};
        @TYPE@[] expected = a.clone();
        Arrays.sort(expected, 2, 6);
        // 仅对 [2, 6) 排序，区间外元素保持原样
        Sort.sort(a, algorithm, 2, 6);
        assertArrayEquals(expected, a, algorithm + " failed on subrange sort");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsEmptyRange(Algorithm algorithm) {
        // 空区间（from == to）合法，数组保持原样
        @TYPE@[] a = new @TYPE@[] {9, 8, 7, 6, 5, 4, 3, 2, 1};
        @TYPE@[] expected = a.clone();
        Sort.sort(a, algorithm, 3, 3);
        assertArrayEquals(expected, a, algorithm + " failed on empty range");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsFullRange(Algorithm algorithm) {
        // [0, length) 等价于全数组排序
        @TYPE@[] a = randomArray(100, 1000);
        @TYPE@[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm, 0, a.length);
        assertArrayEquals(expected, a, algorithm + " failed on full range");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsLastSingleElementRange(Algorithm algorithm) {
        // 末元素单元素区间 [length - 1, length) 合法
        @TYPE@[] a = new @TYPE@[] {5, 3, 8, 1, 9};
        @TYPE@[] expected = a.clone();
        Sort.sort(a, algorithm, 4, 5);
        assertArrayEquals(expected, a, algorithm + " failed on last single element range");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void rejectsNegativeFromIndex(Algorithm algorithm) {
        @TYPE@[] a = new @TYPE@[] {1, 2, 3};
        assertThrows(
                IndexOutOfBoundsException.class,
                () -> Sort.sort(a, algorithm, -1, 3),
                algorithm + " should reject negative fromIndex");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void rejectsToIndexTooLarge(Algorithm algorithm) {
        @TYPE@[] a = new @TYPE@[] {1, 2, 3};
        assertThrows(
                IndexOutOfBoundsException.class,
                () -> Sort.sort(a, algorithm, 0, 4),
                algorithm + " should reject out-of-bounds toIndex");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void rejectsFromGreaterThanTo(Algorithm algorithm) {
        @TYPE@[] a = new @TYPE@[] {1, 2, 3};
        assertThrows(
                IndexOutOfBoundsException.class,
                () -> Sort.sort(a, algorithm, 3, 2),
                algorithm + " should reject fromIndex > toIndex");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void rejectsHugeIndexes(Algorithm algorithm) {
        @TYPE@[] a = new @TYPE@[] {1, 2, 3};
        assertThrows(
                IndexOutOfBoundsException.class,
                () -> Sort.sort(a, algorithm, Integer.MAX_VALUE - 1, Integer.MAX_VALUE),
                algorithm + " should reject huge indexes");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void rejectsNullAlgorithm(Algorithm algorithm) {
        @TYPE@[] a = new @TYPE@[] {1, 2, 3};
        assertThrows(
                NullPointerException.class,
                () -> Sort.sort(a, (Algorithm) null),
                algorithm + " should reject null algorithm");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void rejectsNullArray(Algorithm algorithm) {
        @TYPE@[] a = null;
        assertThrows(
                NullPointerException.class,
                () -> Sort.sort(a, algorithm),
                algorithm + " should reject null array");
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsExtremeValues(Algorithm algorithm) {
        @TYPE@[] a = new @TYPE@[] {@MAX@, @MIN@, 0@SUFFIX@, 1@SUFFIX@, @NEG_ONE@, @MAX@, @MIN@};
        @TYPE@[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on extreme values");
    }
@SPECIAL_BLOCK@}
"""

SPECIAL_TEMPLATE = """
    @ParameterizedTest(name = "{0}")
    @EnumSource(Algorithm.class)
    void sortsSpecialValues(Algorithm algorithm) {
        // NaN 排在最后、-0.0 < 0.0、±Infinity 与次正规数按 compare 全序
        @TYPE@[] a = new @TYPE@[] {
            @NAN@, @POS_INF@, @NEG_INF@, 0@SUFFIX@, -0.0@SUFFIX@, 1.5@SUFFIX@, -1.5@SUFFIX@,
            @MIN_NORMAL@, @MAX@, 1.0@SUFFIX@, -1.0@SUFFIX@
        };
        @TYPE@[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a, algorithm);
        assertArrayEquals(expected, a, algorithm + " failed on special values");
    }
"""

DISPATCHER_TEMPLATE = """package org.zero.sort;



/**
 * 原始类型排序分派
 * <p>
 * ★ 本文件由 tools/gen-primitives.py 自动生成，请勿手改；如需修改请编辑脚本后重新生成。
 * <p>
 * 将 {@link Algorithm} 枚举分派到各原始类型的专用实现（{@code XxxSorts}）。
 *
 * @author Zero
 */
final class PrimitiveDispatcher {

    private PrimitiveDispatcher() {
    }
@BODY@}
"""

DISPATCH_METHOD_TEMPLATE = """
    /**
     * 对 @TYPE@[] 的 [from, to) 区间按指定算法排序
     *
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @param from      起始索引（含）
     * @param to        结束索引（不含）
     */
    static void sort(@TYPE@[] a, Algorithm algorithm, int from, int to) {
        switch (algorithm) {
            case BUBBLE:
                @PREFIX@Sorts.bubble(a, from, to);
                break;
            case SELECTION:
                @PREFIX@Sorts.selection(a, from, to);
                break;
            case INSERTION:
                @PREFIX@Sorts.insertion(a, from, to);
                break;
            case SHELL:
                @PREFIX@Sorts.shell(a, from, to);
                break;
            case MERGE:
                @PREFIX@Sorts.merge(a, from, to);
                break;
            case QUICK:
                @PREFIX@Sorts.quick(a, from, to);
                break;
            default:
                throw new IllegalStateException("Unknown algorithm: " + algorithm);
        }
    }
"""


def write_file(rel_path, content):
    """以 utf-8、LF 写文件（Windows 下防止 GBK/CRLF 污染中文与行尾）"""
    path = os.path.join(ROOT, rel_path)
    directory = os.path.dirname(path)
    if not os.path.isdir(directory):
        os.makedirs(directory)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write(content)
    print("generated: %s" % rel_path)


def render_sorts(t):
    (prefix, type_, gt, gt_key, lt_key, cmp_doc, _, _, _, _, _) = t
    content = (SORTS_TEMPLATE
               .replace("@PREFIX@", prefix)
               .replace("@TYPE@", type_)
               .replace("@GT_BODY@", gt)
               .replace("@GT_KEY_BODY@", gt_key)
               .replace("@LT_KEY_BODY@", lt_key)
               .replace("@CMP_DOC@", cmp_doc))
    return content


def render_test(t):
    (prefix, type_, gt, gt_key, lt_key, cmp_doc, max_val, min_val, suffix, neg_one, special) = t
    special_block = ""
    if special is not None:
        nan, pos_inf, neg_inf, min_normal = special
        special_block = (SPECIAL_TEMPLATE
                         .replace("@PREFIX@", prefix)
                         .replace("@TYPE@", type_)
                         .replace("@NAN@", nan)
                         .replace("@POS_INF@", pos_inf)
                         .replace("@NEG_INF@", neg_inf)
                         .replace("@MIN_NORMAL@", min_normal)
                         .replace("@MAX@", max_val)
                         .replace("@SUFFIX@", suffix))
    content = (TEST_TEMPLATE
               .replace("@PREFIX@", prefix)
               .replace("@TYPE@", type_)
               .replace("@CMP_DOC@", cmp_doc)
               .replace("@MAX@", max_val)
               .replace("@MIN@", min_val)
               .replace("@SUFFIX@", suffix)
               .replace("@NEG_ONE@", neg_one)
               .replace("@SPECIAL_BLOCK@", special_block))
    return content


def render_dispatcher():
    body = ""
    for (prefix, type_, _, _, _, _, _, _, _, _, _) in TYPES:
        body += (DISPATCH_METHOD_TEMPLATE
                 .replace("@PREFIX@", prefix)
                 .replace("@TYPE@", type_))
    return DISPATCHER_TEMPLATE.replace("@BODY@", body)


def main():
    for t in TYPES:
        write_file("src/main/java/org/zero/sort/%sSorts.java" % t[0], render_sorts(t))
        write_file("src/test/java/org/zero/sort/%sSortsTest.java" % t[0], render_test(t))
    write_file("src/main/java/org/zero/sort/PrimitiveDispatcher.java", render_dispatcher())
    print("done: %d primitive types generated" % len(TYPES))


if __name__ == "__main__":
    main()
