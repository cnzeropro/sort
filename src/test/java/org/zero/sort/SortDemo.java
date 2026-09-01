package org.zero.sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 排序库使用演示（非测试类，含 main 入口）。
 * <p>
 * 依次演示：
 * <ul>
 *   <li>门面 {@link Sort} 自动选择算法：对象数组、原始类型 int[]、List&lt;String&gt;、
 *       含 NaN 的 double[]（NaN 按全序视为最大、排在末尾）、区间排序 [from, to)；</li>
 *   <li>指定算法直调：{@link BubbleSort}（int[]）、{@link RadixSort}（int[]，支持负数）、
 *       {@link BucketSort}（double[]）、{@link QuickSort}（Integer[] 区间 [1, 4)）。</li>
 * </ul>
 * 每段输出中文说明与排序前后的 {@link Arrays#toString} 结果。
 * <p>
 * 运行方式：mvn test-compile 后 java -cp target/classes org.zero.sort.SortDemo
 * （Windows 多模块路径用分号分隔；本库无第三方依赖，仅需 target/classes）。
 *
 * @author Zero
 */
public class SortDemo {

    public static void main(String[] args) {
        System.out.println("========== org.zero.sort 排序库演示 ==========");

        System.out.println();
        System.out.println("【1】Sort.sort(Integer[])：自动选择（对象数组走 Tim 排序，稳定且自适应）");
        Integer[] boxed = {5, 3, 8, 1, 9, 2, 7};
        System.out.println("排序前：" + Arrays.toString(boxed));
        Sort.sort(boxed);
        System.out.println("排序后：" + Arrays.toString(boxed));

        System.out.println();
        System.out.println("【2】Sort.sort(int[])：自动选择（小数组走插入排序，大数组走双轴快排）");
        int[] ints = {42, 7, 19, 3, 88, 1, 55, 23, 0, 99, 61, 14};
        System.out.println("排序前：" + Arrays.toString(ints));
        Sort.sort(ints);
        System.out.println("排序后：" + Arrays.toString(ints));

        System.out.println();
        System.out.println("【3】Sort.sort(List<String>)：自动选择（List 原地排序，按字典序）");
        List<String> words = new ArrayList<>(Arrays.asList("banana", "Apple", "cherry", "date", "Elderberry"));
        System.out.println("排序前：" + words);
        Sort.sort(words);
        System.out.println("排序后：" + words);

        System.out.println();
        System.out.println("【4】Sort.sort(double[])：自动选择（Double.compare 全序，NaN 视为最大、排在末尾）");
        double[] doubles = {3.14, Double.NaN, -1.5, 0.0, 2.71, Double.NaN, -0.5, 99.9};
        System.out.println("排序前：" + Arrays.toString(doubles));
        Sort.sort(doubles);
        System.out.println("排序后：" + Arrays.toString(doubles) + "（两个 NaN 稳定排在末尾）");

        System.out.println();
        System.out.println("【5】Sort.sort(T[], from, to)：区间排序 [1, 6)，区间外元素保持原样");
        Integer[] ranged = {100, 9, 30, 20, 10, 40, 200};
        System.out.println("排序前：" + Arrays.toString(ranged));
        Sort.sort(ranged, 1, 6);
        System.out.println("排序后：" + Arrays.toString(ranged) + "（首尾 100、200 未参与排序）");

        System.out.println();
        System.out.println("【6】指定算法 BubbleSort.sort(int[])：冒泡排序");
        int[] bubbleData = {64, 25, 12, 22, 11, 90, 3};
        System.out.println("排序前：" + Arrays.toString(bubbleData));
        BubbleSort.sort(bubbleData);
        System.out.println("排序后：" + Arrays.toString(bubbleData));

        System.out.println();
        System.out.println("【7】指定算法 RadixSort.sort(int[])：LSD 基数排序（非比较类，负数按补码排在最前）");
        int[] radixData = {170, -45, 75, -90, 802, 24, 2, 66};
        System.out.println("排序前：" + Arrays.toString(radixData));
        RadixSort.sort(radixData);
        System.out.println("排序后：" + Arrays.toString(radixData));

        System.out.println();
        System.out.println("【8】指定算法 BucketSort.sort(double[])：桶排序（非比较类，按值域分桶）");
        double[] bucketData = {0.42, 0.32, 0.33, 0.52, 0.37, 0.47, 0.51, 0.02};
        System.out.println("排序前：" + Arrays.toString(bucketData));
        BucketSort.sort(bucketData);
        System.out.println("排序后：" + Arrays.toString(bucketData));

        System.out.println();
        System.out.println("【9】指定算法 QuickSort.sort(Integer[], 1, 4)：只排区间 [1, 4)");
        Integer[] quickData = {50, 30, 20, 10, 40};
        System.out.println("排序前：" + Arrays.toString(quickData));
        QuickSort.sort(quickData, 1, 4);
        System.out.println("排序后：" + Arrays.toString(quickData) + "（仅下标 1~3 参与，50、40 保持原样）");

        System.out.println();
        System.out.println("========== 演示结束 ==========");
    }
}
