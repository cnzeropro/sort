/**
 * 经典排序算法的 Java 工具库
 * <p>
 * 提供六大经典排序算法（冒泡、选择、插入、希尔、归并、快速排序）的统一实现，
 * 支持任意 {@link java.lang.Comparable} 对象数组与全部数字原始类型
 * （byte / short / int / long / float / double / char）。
 *
 * <h2>核心入口</h2>
 * <ul>
 *   <li>{@link org.zero.sort.Sort}：静态工具方法门面，{@code Sort.sort(a)} 一行排序；
 *       对象数组提供 {@code quickSort}/{@code mergeSort} 等命名便捷方法；</li>
 *   <li>{@link org.zero.sort.Algorithm}：排序算法枚举，可通过 {@code Sort.sort(a, Algorithm.MERGE)}
 *       以编程方式指定算法。</li>
 * </ul>
 *
 * <h2>区间约定</h2>
 * 所有带索引参数的方法遵循 JDK 惯例：区间为 <b>[fromIndex, toIndex)</b> 左闭右开。
 *
 * <h2>异常约定</h2>
 * 与 {@link java.util.Arrays} 一致：空数组合法；null 数组与区间内 null 元素抛
 * {@link java.lang.NullPointerException}；非法索引抛 {@link java.lang.IndexOutOfBoundsException}。
 *
 * <h2>Java 版本</h2>
 * 最低支持 Java 8；以 Multi-Release JAR 打包，Java 9+ 运行时自动加载版本化类
 * （JEP 238），各版本行为完全一致。
 *
 * <h2>原始类型实现</h2>
 * 包内的原始类型特化实现（{@code IntSorts}、{@code DoubleSorts} 等，包私有）为
 * 手工维护代码，与对象数组版算法（{@code GenericSorts}）行为一致。
 *
 * @author Zero
 * @see org.zero.sort.Sort
 * @see org.zero.sort.Algorithm
 */
package org.zero.sort;
