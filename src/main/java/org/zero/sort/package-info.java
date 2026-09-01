/**
 * 经典排序算法工具类库：21 种算法按算法划分为独立类，另含自动选择算法的门面类。
 * <p>
 * <b>按算法组织</b>：每种算法一个类，类内提供全类型重载——
 * <ul>
 *   <li>17 种比较类算法（{@link org.zero.sort.BubbleSort 冒泡}、{@link org.zero.sort.SelectionSort 选择}、
 *       {@link org.zero.sort.InsertionSort 插入}、{@link org.zero.sort.ShellSort 希尔}、
 *       {@link org.zero.sort.MergeSort 归并}、{@link org.zero.sort.QuickSort 快速}、
 *       {@link org.zero.sort.HeapSort 堆}、{@link org.zero.sort.TimSort Tim}、
 *       {@link org.zero.sort.CombSort 梳}、{@link org.zero.sort.GnomeSort 地精}、
 *       {@link org.zero.sort.CocktailSort 鸡尾酒}、{@link org.zero.sort.CycleSort 循环}、
 *       {@link org.zero.sort.OddEvenSort 奇偶}、{@link org.zero.sort.PancakeSort 煎饼}、
 *       {@link org.zero.sort.StoogeSort 臭皮匠}、{@link org.zero.sort.BitonicSort 双调}、
 *       {@link org.zero.sort.TreeSort 树}）：支持 {@code Comparable} 对象数组 / {@code List}、
 *       {@code Comparator} 对象数组 / {@code List}，以及全部 7 种数字原始类型；</li>
 *   <li>3 种整数专用算法（{@link org.zero.sort.CountingSort 计数}、{@link org.zero.sort.RadixSort 基数}、
 *       {@link org.zero.sort.PigeonholeSort 鸽巢}）：支持 byte / short / int / long / char；</li>
 *   <li>1 种浮点专用算法（{@link org.zero.sort.BucketSort 桶}）：支持 float / double。</li>
 * </ul>
 * 每个类型统一提供三种形态：{@code sort(a)} 全量、{@code sort(a, fromIndex)} 从某位置到末尾、
 * {@code sort(a, fromIndex, toIndex)} 区间 [fromIndex, toIndex)。
 * <p>
 * <b>自动选择</b>：{@link org.zero.sort.Sort} 门面类不指定算法，与 JDK {@code Arrays.sort} 同理念，
 * 根据数据结构与长度自适应选择（对象走稳定 Tim 排序；原始类型小数组插入、近有序 Tim、否则双轴快排）。
 * <p>
 * 比较语义与 JDK 一致：float / double 使用全序（NaN 最后、-0.0 &lt; 0.0），
 * char 按无符号 16 位整数序，其余整数类型按有符号自然序。
 *
 * @author Zero
 */
package org.zero.sort;
