# sort

经典排序算法的 Java 工具类库：**21 种排序算法按算法划分为独立类**（`BubbleSort.sort(a)`、`QuickSort.sort(a, 1, 5)`……），门面 `Sort.sort(a)` 像 JDK 一样**自动选择最优算法**；支持 `Comparable` / `Comparator` 对象与**全部数字原始类型**。

[![CI](https://github.com/cnzeropro/sort/actions/workflows/ci.yml/badge.svg)](https://github.com/cnzeropro/sort/actions/workflows/ci.yml)
[![Release](https://img.shields.io/github/v/release/cnzeropro/sort.svg)](https://github.com/cnzeropro/sort/releases)
[![License](https://img.shields.io/github/license/cnzeropro/sort.svg)](./LICENSE)
[![Java](https://img.shields.io/badge/Java-8%2B-blue.svg)](https://openjdk.org/)

## 特性

- **按算法组织**：每种算法一个类（`BubbleSort`、`TimSort`、`RadixSort`……共 21 个），同一算法类内提供全类型重载，教学与对照实验一目了然
- **一行门面**：`Sort.sort(a)` 不指定算法，与 JDK `Arrays.sort` 同理念——根据数据结构与长度自适应选择（对象走稳定 Tim 排序；原始类型小数组插入、近有序 Tim、否则双轴快排）
- **统一重载形态**：每个类型固定三种区间形态 `sort(a)` / `sort(a, fromIndex)` / `sort(a, fromIndex, toIndex)`，另加 `List` 与 `Comparator` 支持
- **类型全覆盖**：`Comparable` 对象（含父类实现 Comparable）、任意 `Comparator`、byte / short / int / long / float / double / char 原始类型全手写特化（零装箱）
- **Java 8 基线**：最低支持 Java 8；以 **Multi-Release JAR** 打包，Java 9+ 自动启用版本化实现（JEP 238），各版本行为一致
- **JDK 惯例**：区间 `[from, to)` 左闭右开、异常行为与 `Arrays.sort` 一致
- **质量门禁**：780+ 矩阵测试（算法 × 类型参数化展开）、JaCoCo 覆盖率门槛、Spotless、JDK 8/17/21 三矩阵 CI

## 支持的算法

| 算法类 | 稳定性 | 适用类型 | 最好 | 平均 | 最坏 | 空间 |
|------|:---:|------|------|------|------|------|
| `BubbleSort`（冒泡） | 稳定 | 全部 | O(n) | O(n²) | O(n²) | O(1) |
| `SelectionSort`（选择） | 不稳定 | 全部 | O(n²) | O(n²) | O(n²) | O(1) |
| `InsertionSort`（插入） | 稳定 | 全部 | O(n) | O(n²) | O(n²) | O(1) |
| `ShellSort`（希尔） | 不稳定 | 全部 | O(n log n) | ~O(n^1.25) | O(n²) | O(1) |
| `MergeSort`（归并） | 稳定 | 全部 | O(n log n) | O(n log n) | O(n log n) | O(n) |
| `QuickSort`（快速） | 不稳定 | 全部 | O(n log n) | O(n log n) | O(n²) | O(log n) |
| `HeapSort`（堆） | 不稳定 | 全部 | O(n log n) | O(n log n) | O(n log n) | O(1) |
| `TimSort`（Tim） | 稳定 | 全部 | O(n) | O(n log n) | O(n log n) | O(n) |
| `CombSort`（梳） | 不稳定 | 全部 | O(n log n) | O(n²/2^p) | O(n²) | O(1) |
| `GnomeSort`（地精） | 稳定 | 全部 | O(n) | O(n²) | O(n²) | O(1) |
| `CocktailSort`（鸡尾酒） | 稳定 | 全部 | O(n) | O(n²) | O(n²) | O(1) |
| `CycleSort`（循环） | 不稳定 | 全部 | O(n²) | O(n²) | O(n²) | O(1) |
| `OddEvenSort`（奇偶） | 稳定 | 全部 | O(n) | O(n²) | O(n²) | O(1) |
| `PancakeSort`（煎饼） | 不稳定 | 全部 | O(n²) | O(n²) | O(n²) | O(1) |
| `StoogeSort`（臭皮匠） | 不稳定 | 全部 | O(n^2.71) | O(n^2.71) | O(n^2.71) | O(n) |
| `BitonicSort`（双调） | 不稳定 | 全部 | O(n log²n) | O(n log²n) | O(n log²n) | O(n) |
| `TreeSort`（树） | 稳定 | 全部 | O(n log n) | O(n log n) | O(n²) | O(n) |
| `CountingSort`（计数） | 稳定 | 仅整数类型 | O(n+k) | O(n+k) | O(n+k) | O(k) |
| `RadixSort`（基数） | 稳定 | 仅整数类型 | O(n·w) | O(n·w) | O(n·w) | O(n) |
| `PigeonholeSort`（鸽巢） | 稳定 | 仅整数类型 | O(n+k) | O(n+k) | O(n+k) | O(k) |
| `BucketSort`（桶） | 稳定 | 仅 float/double | O(n) | O(n+k) | O(n²) | O(n+k) |

说明：

- "全部" = 任意 Comparable/Comparator 对象 + 全部数字原始类型；"仅整数类型" = byte/short/int/long/char
- 快速排序：**双轴快排**（五点等距取样选主元 + 相等元素三路分区），有序/逆序/全相等不退化
- Tim 排序：对象版为完整实现（含 **galloping** 优化），原始类型版为简化实现；run 栈不变量采用 JDK 2015 修正版
- 树排序：对象实现基于红黑树；原始类型为朴素 BST（不装箱的代价），有序输入退化为 O(n²)
- 计数/鸽巢：值域超 `1 << 24` 抛 `IllegalArgumentException`
- 桶排序：NaN 排最后、±Infinity 归首尾桶（与其他算法比较语义一致）
- 刻意不含 bogo/sleep 等恶搞算法

## 快速开始

要求：JDK 8+（推荐 17+）、Maven 3.9+（推荐使用项目自带的 [Maven Wrapper](https://maven.apache.org/wrapper/)）

```bash
./mvnw test        # 运行测试（JDK 8/17/21 均可用，jdk8 profile 自动激活）
./mvnw package     # 打包 Multi-Release JAR
./mvnw verify      # 完整验证（测试 + 覆盖率门槛 + 代码卫生检查 + MR-JAR IT）
```

## 使用示例

### 自动选择（推荐）

```java
Integer[] a = {5, 3, 8, 1, 9};
Sort.sort(a);                    // 对象：稳定 Tim 排序 → [1, 3, 5, 8, 9]
Sort.sort(a, 1, 4);              // 区间 [1, 4)

int[] c = {5, 3, 8, 1, 9};
Sort.sort(c);                    // 原始类型：小数组插入、近有序 Tim、否则双轴快排
Sort.sort(c, 1, 4);

double[] d = {2.5, 0.1, Double.NaN, -0.0};
Sort.sort(d);                    // NaN 排最后、-0.0 < 0.0
```

### 指定算法（算法类）

```java
BubbleSort.sort(a);              // 每种算法一个类，类内全类型重载
QuickSort.sort(a, 1, 4);         // 区间
RadixSort.sort(c);               // 整数专用：byte/short/int/long/char
BucketSort.sort(d);              // 浮点专用：float/double
TimSort.sort(list);              // List 也支持
```

### Comparator 自定义排序

```java
Sort.sort(a, Comparator.reverseOrder());            // 门面 + 比较器（Tim）
HeapSort.sort(a, Comparator.reverseOrder());        // 指定算法 + 比较器
HeapSort.sort(a, 1, 4, Comparator.reverseOrder());  // + 区间
Sort.sort(a, Comparator.nullsFirst(...));           // 与 JDK 一致：Comparator 版本允许 null 元素
```

### List

```java
List<String> list = new ArrayList<>(...);
Sort.sort(list);                              // 默认 Tim，原地排序
Sort.sort(list, Comparator.reverseOrder());
ShellSort.sort(list);                         // 指定算法
Sort.sort(list.subList(1, 4));                // 区间：用 subList
```

### API 形态一览

每个类型统一三种区间形态（以 `BubbleSort` 为例）：

| 方法 | 语义 |
|------|------|
| `sort(a)` | 整个数组 / List |
| `sort(a, fromIndex)` | `[fromIndex, a.length)` 排到末尾 |
| `sort(a, fromIndex, toIndex)` | `[fromIndex, toIndex)` 区间 |

**17 个比较类算法**（冒泡→树排序）：上述三形态 × `T[]`（Comparable）/ `T[]` + `Comparator`，加 `sort(List)` / `sort(List, Comparator)`，共 29 个重载。

**3 个整数专用算法**（`CountingSort` / `RadixSort` / `PigeonholeSort`）：byte / short / int / long / char 各三形态。

**`BucketSort`**：float / double 各三形态。

**`Sort` 门面**：与比较类算法同形态的全类型重载（不含算法参数，自动选择）。

## 约定

### 区间

**[fromIndex, toIndex) 左闭右开**，与 `Arrays.sort`、`String.substring` 一致；空区间合法。

### 异常

与 `Arrays.sort` 一致：

| 情况 | 异常 |
|------|------|
| 空数组 / 空 List | 合法，无操作 |
| null 数组/List/比较器；Comparable 区间内 null 元素 | `NullPointerException`（Comparator 版本允许 null 元素） |
| fromIndex < 0、toIndex > 长度、fromIndex > toIndex | `IndexOutOfBoundsException` |
| 计数/鸽巢值域超 `1 << 24` | `IllegalArgumentException` |

校验顺序：null → 索引。

### 比较语义

- 对象：`compareTo` 自然顺序或自定义 `Comparator`
- float / double：`Float.compare` / `Double.compare` **全序**——NaN 排最后、-0.0 < 0.0
- char：无符号 16 位整数序（`'\u0000'` 最小，`'\uFFFF'` 最大）

### Multi-Release JAR

- 基础层（`src/main/java`）以 `release 8` 编译，Java 8 可运行
- 版本化层（`src/main/java9`）以 `release 9` 编译进 `META-INF/versions/9`，Java 9+ 自动加载（区间校验委托 `Objects.checkFromToIndex`）
- 两层**行为与异常消息契约完全一致**（由 failsafe IT 锁死）
- JDK 8 构建（jdk8 profile）不含版本化层；发布用的 MR jar 由 JDK 9+ 构建（CI 17/21 job 已验证）

## 测试与质量

- **780+ 参数化矩阵测试**：21 算法类 × {对象/int/byte/short/long/char/float/double/List} × {随机/含负值/有序/逆序/全等/空/单双元素}，浮点另含 NaN/±Infinity/±0.0 专项，char 含全值域端点；区间重载（前缀后缀不受影响）、非法入参、null 元素契约逐类验证
- **门面三分支**：小数组插入 / 近有序 Tim / 随机双轴快排，以结果正确性锁定
- **稳定性专项**：稳定算法（相等键保持原始顺序）与不稳定算法（仅有序性）分组验证
- **List / Comparator 专项**：ArrayList 与 LinkedList、逆序与忽略大小写比较器、泛型边界（父类 Comparable 子类数组）
- **JaCoCo 门槛**：行 ≥ 80%、分支 ≥ 70%（verify 阶段强制）
- **Spotless**：导入顺序/未用导入/行尾（JDK 17+ 运行，JDK 8 job 自动跳过）
- **CI**：JDK 8/17/21 三矩阵；JDK 8 job 以 javac 8 + JDK 8 bootclasspath 编译，验证无 9+ API 泄漏

## 性能基准（JMH）

基准源码位于 `src/jmh/java`（21 种算法 × 5 种输入形态 + JDK `Arrays.sort` 基线对照），运行方式：

```bash
# 编译基准并导出依赖类路径
./mvnw -Pbenchmarks compile dependency:build-classpath -Dmdep.outputFile=target/benchmark-cp.txt

# 运行全部基准（Linux/macOS 用 : 分隔类路径）
java -cp "target/benchmark-classes;target/classes;$(cat target/benchmark-cp.txt)" \
     org.openjdk.jmh.Main -f 1 -wi 2 -i 3 -w 1s -r 1s

# 运行单个基准（如快速排序）
java -cp "target/benchmark-classes;target/classes;$(cat target/benchmark-cp.txt)" \
     org.openjdk.jmh.Main SortIntBenchmark.quick
```

输入形态：`RANDOM`（均匀随机）/ `SORTED`（升序）/ `REVERSE`（降序）/ `DUPLICATES`（值域 16）/ `NEAR_SORTED`（近有序）。规模按算法复杂度分档（O(n log n) 类 n=10⁴、O(n²) 类 n=10³、STOOGE n=200）。

<!-- 基准结果表 -->
### int[]（µs/op，越小越好）

| 算法 | RANDOM | SORTED | REVERSE | DUPLICATES | NEAR_SORTED |
|------|---:|---:|---:|---:|---:
| shell | 646.4 | 14.4 | 82.4 | 306.6 | 53.4 |
| merge | 467.0 | 9.0 | 73.7 | 270.2 | 13.9 |
| quick | 352.6 | 23.1 | 36.2 | 120.4 | 33.0 |
| heap | 603.9 | 392.8 | 438.3 | 472.3 | 425.3 |
| tim | 727.4 | 5.3 | 12.8 | 457.4 | 7.4 |
| comb | 695.0 | 64.0 | 85.3 | 254.8 | 68.8 |
| bitonic | 2770.7 | 3710.4 | 3534.9 | 5076.6 | 1810.2 |
| tree | 618.8 | 59099.9 | 58304.0 | 6084.2 | 59910.0 |
| counting | 105.9 | 17.3 | 16.1 | 11.9 | 15.5 |
| radix | 50.5 | 96.9 | 98.7 | 120.1 | 96.6 |
| pigeonhole | 129.0 | 16.4 | 16.4 | 11.3 | 15.9 |
| **Sort.sort（自动）** | 354.2 | 6.6 | 37.2 | 112.0 | 8.6 |
| **Arrays.sort**（JDK 基线） | 344.4 | 2.3 | 5.1 | 112.3 | 25.9 |
| bubble | 736.0 | 0.2 | 182.4 | 518.1 | 0.4 |
| selection | 169.0 | 77.6 | 775.2 | 159.2 | 137.3 |
| insertion | 40.1 | 0.2 | 110.0 | 41.1 | 0.8 |
| gnome | 457.5 | 0.2 | 909.8 | 420.8 | 0.7 |
| cocktail | 642.3 | 0.2 | 249.4 | 607.8 | 0.4 |
| oddEven | 552.2 | 0.6 | 337.0 | 524.7 | 1.4 |
| cycle | 1007.7 | 77.0 | 223.0 | 1052.6 | 122.0 |
| pancake | 402.6 | 88.2 | 142.7 | 348.2 | 196.9 |
| stooge | 1116.6 | 1053.6 | 1121.3 | 1108.0 | 1200.5 |

> n=10⁴；bubble/selection/insertion/gnome/cocktail/oddEven/cycle/pancake 为 n=10³；stooge 为 n=200；counting/pigeonhole 使用值域 0..65535 的有界数据。

### Integer[]（µs/op，越小越好）

| 算法 | RANDOM | SORTED | REVERSE | DUPLICATES | NEAR_SORTED |
|------|---:|---:|---:|---:|---:
| shell | 1447.2 | 142.8 | 327.7 | 826.6 | 167.7 |
| merge | 976.6 | 27.7 | 324.0 | 675.6 | 38.7 |
| quick | 730.0 | 73.8 | 120.9 | 313.5 | 87.9 |
| heap | 1348.5 | 1013.0 | 1008.6 | 1062.4 | 996.9 |
| tim | 1059.8 | 10.3 | 28.6 | 790.9 | 14.6 |
| comb | 1285.9 | 165.8 | 477.0 | 1033.9 | 394.0 |
| bitonic | 2816.7 | 1769.4 | 1818.5 | 2038.5 | 1940.0 |
| tree | 2086.4 | 827.2 | 800.6 | 220.2 | 957.0 |
| **Sort.sort（自动）** | 1197.0 | 13.5 | 36.9 | 812.5 | 15.7 |
| **Arrays.sort**（JDK 基线） | 938.6 | 8.8 | 22.7 | 655.2 | 17.5 |
| bubble | 1093.5 | 0.5 | 1606.7 | 1358.6 | 1.9 |
| selection | 655.2 | 199.4 | 565.1 | 470.6 | 328.1 |
| insertion | 446.9 | 2.0 | 827.7 | 383.3 | 3.0 |
| gnome | 1422.3 | 0.5 | 2899.6 | 1197.1 | 1.4 |
| cocktail | 1175.1 | 0.5 | 2404.2 | 1315.1 | 1.5 |
| oddEven | 1389.1 | 0.8 | 1775.5 | 1651.4 | 2.8 |
| cycle | 1839.3 | 204.1 | 501.4 | 774.3 | 239.4 |
| pancake | 1588.0 | 349.5 | 308.4 | 1691.4 | 522.8 |
| stooge | 1584.6 | 1344.4 | 1658.5 | 1873.6 | 1586.7 |

> n=10⁴；bubble/selection/insertion/gnome/cocktail/oddEven/cycle/pancake 为 n=10³；stooge 为 n=200；counting/pigeonhole 使用值域 0..65535 的有界数据。

> 数据为 v1.2.0 时期一次性运行（fork=1, warmup=1×1s, measurement=2×1s）的结果，仅供参考（1.3.0 算法内核未变，仅重组 API）；精确对比请在本机按上述命令复测。测试环境：JDK 26.0.2（OpenJDK）、Windows 11、x64。

## 升级到 v1.3.0（破坏性变更）

API 重组为**按算法类**组织，语义（区间/异常/比较/稳定性）与 1.2.0 完全一致：

| 旧 API（≤ 1.2.0） | 新 API（1.3.0） |
|------|------|
| `Sort.sort(a, Algorithm.MERGE)` | `MergeSort.sort(a)` |
| `Sort.sort(a, Algorithm.MERGE, 1, 5)` | `MergeSort.sort(a, 1, 5)` |
| `Sort.sort(a, Algorithm.HEAP, cmp)` | `HeapSort.sort(a, cmp)` |
| `Sort.mergeSort(a)` 等命名方法 | `MergeSort.sort(a)` |
| `Sort.sort(a)`（自动选择） | `Sort.sort(a)`（不变） |
| `Algorithm` 枚举 / `GenericSorts` / `IntSorts` 等类型分发类 | 已移除，由 21 个算法类取代 |

历史行为变更（相对 v1.0）：对象 `sort(a)` 默认算法由 QUICK 改为 TIM（稳定、自适应）；原始类型默认改为自适应调度。

## 版本

- 当前版本：**1.3.0**
- 版本策略：[SemVer](https://semver.org/)；发布见 [Releases](https://github.com/cnzeropro/sort/releases)

## 许可证

[MIT](./LICENSE) © 2021-2026 Zero
