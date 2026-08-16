# sort

经典排序算法的 Java 实现库。基于泛型与 `Comparable` 接口，任何实现了 `Comparable` 的类型均可直接使用。

## 支持的算法

| 算法 | 稳定性 | 最好时间复杂度 | 平均时间复杂度 | 最坏时间复杂度 | 空间复杂度 |
|------|:------:|:-------------:|:-------------:|:-------------:|:---------:|
| 冒泡排序（Bubble） | 稳定 | O(n) | O(n²) | O(n²) | O(1) |
| 选择排序（Selection） | 不稳定 | O(n²) | O(n²) | O(n²) | O(1) |
| 插入排序（Insertion） | 稳定 | O(n) | O(n²) | O(n²) | O(1) |
| 希尔排序（Shell） | 不稳定 | O(n^1.3) | 约 O(n^1.5) | O(n²) | O(1) |
| 归并排序（Merge） | 稳定 | O(n log n) | O(n log n) | O(n log n) | O(n) |
| 快速排序（Quick） | 不稳定 | O(n log n) | O(n log n) | O(n²) | O(log n) |

## 快速开始

要求：JDK 17+、Maven 3.9+（推荐使用项目自带的 [Maven Wrapper](https://maven.apache.org/wrapper/)，无需本地安装 Maven）

```bash
# 运行测试
./mvnw test

# 打包
./mvnw package
```

## 使用示例

```java
import org.zero.sort.Sort;

Integer[] a = {5, 3, 8, 1, 9, 2};
Sort.Quick.sort(a); // a 变为 [1, 2, 3, 5, 8, 9]

String[] b = {"banana", "apple", "cherry"};
Sort.Merge.sort(b, 1, 2); // 仅对 b[1..2] 区间排序
```

每种算法（`Sort.Bubble` / `Sort.Selection` / `Sort.Insertion` / `Sort.Shell` / `Sort.Merge` / `Sort.Quick`）均提供以下重载：

| 方法 | 说明 |
|------|------|
| `sort(T[] a)` | 对整个数组排序 |
| `sortBack(T[] a, int startIndex)` | 从 `startIndex` 排序到数组末尾 |
| `sortFront(T[] a, int endIndex)` | 从数组开头排序到 `endIndex` |
| `sort(T[] a, int startIndex, int endIndex)` | 对 `[startIndex, endIndex]` 区间排序 |

希尔排序额外提供 `generalSort`（普通增量序列）与 `sort`（严谨增量序列 2^k-1，速度更快）。

## 边界约定

以下情况会抛出 `IllegalArgumentException`：

- 数组为 `null`，或数组中包含 `null` 元素
- 数组长度为 0
- `startIndex < 0`、`endIndex >= a.length`、`startIndex > endIndex`

## 测试与性能演示

```bash
# 运行单元测试（含随机、有序、逆序、重复元素等用例）
./mvnw test

# 运行各算法的耗时演示（在 IDE 中直接运行，或）
./mvnw -q exec:java -Dexec.mainClass=org.zero.sort.SortDemo -Dexec.classpathScope=test
```

## 许可证

[MIT](./LICENSE) © 2021-2026 Zero
