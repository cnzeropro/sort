# sort

经典排序算法的 Java 工具类库：**21 种排序算法**，一行 API，支持 `Comparable` / `Comparator` 对象与**全部数字原始类型**。

## 特性

- **21 种算法**：冒泡 / 选择 / 插入 / 希尔 / 归并 / 快速 / 堆 / Tim / 梳 / 地精 / 鸡尾酒 / 循环 / 奇偶 / 煎饼 / 臭皮匠 / 双调 / 树 / 计数 / 基数 / 鸽巢 / 桶
- **API 简单**：`Sort.sort(a)` 一行排序；`Sort.sort(a, Algorithm.MERGE, 1, 5)` 指定算法与区间；Comparator 与 List 全支持
- **自适应默认**：对象默认 **Tim 排序**（检测有序片段，已有序 O(n)、稳定，与 JDK `Arrays.sort(Object[])` 同款）；原始类型默认自适应调度（小数组插入、近有序 Tim、否则双轴快排）
- **类型全覆盖**：`Comparable` 对象（含父类实现 Comparable）、任意 `Comparator`、byte / short / int / long / float / double / char 原始类型全手写特化（零装箱）
- **Java 8 基线**：最低支持 Java 8；以 **Multi-Release JAR** 打包，Java 9+ 自动启用版本化实现（JEP 238），各版本行为一致
- **JDK 惯例**：区间 `[from, to)` 左闭右开、异常行为与 `Arrays.sort` 一致
- **质量门禁**：3100+ 矩阵测试、JaCoCo 覆盖率门槛、Spotless、JDK 8/17/21 三矩阵 CI

## 支持的算法

| 算法 | 稳定性 | 适用类型 | 最好 | 平均 | 最坏 | 空间 |
|------|:---:|------|------|------|------|------|
| 冒泡（BUBBLE） | 稳定 | 全部 | O(n) | O(n²) | O(n²) | O(1) |
| 选择（SELECTION） | 不稳定 | 全部 | O(n²) | O(n²) | O(n²) | O(1) |
| 插入（INSERTION） | 稳定 | 全部 | O(n) | O(n²) | O(n²) | O(1) |
| 希尔（SHELL） | 不稳定 | 全部 | O(n log n) | ~O(n^1.25) | O(n²) | O(1) |
| 归并（MERGE） | 稳定 | 全部 | O(n log n) | O(n log n) | O(n log n) | O(n) |
| 快速（QUICK） | 不稳定 | 全部 | O(n log n) | O(n log n) | O(n²) | O(log n) |
| 堆（HEAP） | 不稳定 | 全部 | O(n log n) | O(n log n) | O(n log n) | O(1) |
| Tim（TIM） | 稳定 | 全部 | O(n) | O(n log n) | O(n log n) | O(n) |
| 梳（COMB） | 不稳定 | 全部 | O(n log n) | O(n²/2^p) | O(n²) | O(1) |
| 地精（GNOME） | 稳定 | 全部 | O(n) | O(n²) | O(n²) | O(1) |
| 鸡尾酒（COCKTAIL） | 稳定 | 全部 | O(n) | O(n²) | O(n²) | O(1) |
| 循环（CYCLE） | 不稳定 | 全部 | O(n²) | O(n²) | O(n²) | O(1) |
| 奇偶（ODD_EVEN） | 稳定 | 全部 | O(n) | O(n²) | O(n²) | O(1) |
| 煎饼（PANCAKE） | 不稳定 | 全部 | O(n²) | O(n²) | O(n²) | O(1) |
| 臭皮匠（STOOGE） | 不稳定 | 全部 | O(n^2.71) | O(n^2.71) | O(n^2.71) | O(n) |
| 双调（BITONIC） | 不稳定 | 全部 | O(n log²n) | O(n log²n) | O(n log²n) | O(n) |
| 树（TREE） | 稳定 | 全部 | O(n log n) | O(n log n) | O(n²) | O(n) |
| 计数（COUNTING） | 稳定 | 仅积分类型 | O(n+k) | O(n+k) | O(n+k) | O(k) |
| 基数（RADIX） | 稳定 | 仅积分类型 | O(n·w) | O(n·w) | O(n·w) | O(n) |
| 鸽巢（PIGEONHOLE） | 稳定 | 仅积分类型 | O(n+k) | O(n+k) | O(n+k) | O(k) |
| 桶（BUCKET） | 稳定 | 仅 float/double | O(n) | O(n+k) | O(n²) | O(n+k) |

说明：

- "全部" = 任意 Comparable/Comparator 对象 + 全部数字原始类型；"仅积分类型" = byte/short/int/long/char
- 快速排序：三数取中 + Hoare 交叉指针，有序/逆序/全相等不退化
- Tim 排序：简化版（无 galloping），run 栈不变量采用 JDK 2015 修正版
- 树排序：对象实现基于红黑树；原始类型为朴素 BST（不装箱的代价），有序输入退化为 O(n²)
- 计数/鸽巢：值域超 `1 << 24` 抛 `IllegalArgumentException`
- 桶排序：NaN 排最后、±Infinity 归首尾桶（与其他算法比较语义一致）
- 刻意不含 bogo/sleep 等恶搞算法

## 快速开始

要求：JDK 8+（推荐 17+）、Maven 3.9+（推荐使用项目自带的 [Maven Wrapper](https://maven.apache.org/wrapper/)）

```bash
./mvnw test        # 运行测试（JDK 8/17/21 均可用，jdk8 profile 自动激活）
./mvnw package     # 打包 Multi-Release JAR
./mvnw verify      # 完整验证（测试 + 覆盖率门槛 + 代码卫生检查）
```

## 使用示例

### 对象数组（Comparable）

```java
Integer[] a = {5, 3, 8, 1, 9};
Sort.sort(a);                         // 默认 Tim 排序 → [1, 3, 5, 8, 9]
Sort.mergeSort(a, 1, 4);              // 命名方法 + 区间 [1, 4)
Sort.sort(a, Algorithm.SHELL);        // 枚举指定算法
Sort.sort(a, Algorithm.INSERTION, 1, 4);
```

### Comparator 自定义排序

```java
Sort.sort(a, Comparator.reverseOrder());                    // 降序（默认 Tim）
Sort.sort(a, Algorithm.HEAP, Comparator.reverseOrder());    // 指定算法 + 比较器
Sort.sort(a, Comparator.nullsFirst(...), 0, 5);             // 区间 + 比较器
// 与 JDK 一致：Comparator 版本允许 null 元素（由比较器自行处理）
```

### List

```java
List<String> list = new ArrayList<>(...);
Sort.sort(list);                        // 默认 Tim，原地排序
Sort.sort(list, Algorithm.HEAP);       // 指定算法
Sort.sort(list, Comparator.reverseOrder());
Sort.sort(list.subList(1, 4));         // 区间：用 subList
```

### 原始类型（byte/short/int/long/float/double/char）

```java
int[] c = {5, 3, 8, 1, 9};
Sort.sort(c);                           // 默认自适应策略
Sort.sort(c, Algorithm.RADIX, 1, 4);   // 基数排序 + 区间

double[] d = {2.5, 0.1, Double.NaN, -0.0};
Sort.sort(d, Algorithm.BUCKET);        // NaN 排最后、-0.0 < 0.0
```

### API 一览

**对象数组**：

| 方法 | 说明 |
|------|------|
| `sort(T[] a)` / `sort(T[], from, to)` | 默认算法（TIM） |
| `sort(T[], Algorithm)` / `sort(T[], Algorithm, from, to)` | 指定算法 |
| `sort(T[], Comparator)` / `sort(T[], Comparator, from, to)` | 比较器（默认 TIM） |
| `sort(T[], Algorithm, Comparator)` / `sort(T[], Algorithm, Comparator, from, to)` | 算法 + 比较器 |
| `bubbleSort / selectionSort / insertionSort / shellSort / mergeSort / quickSort(T[] a[, from, to])` | 命名便捷方法 |

**List**：`sort(List)` / `sort(List, Algorithm)` / `sort(List, Comparator)` / `sort(List, Algorithm, Comparator)`

**原始类型**（int 为例，其余 6 种相同）：`sort(int[])` / `sort(int[], Algorithm)` / `sort(int[], from, to)` / `sort(int[], Algorithm, from, to)`

## 约定

### 区间

**[fromIndex, toIndex) 左闭右开**，与 `Arrays.sort`、`String.substring` 一致；空区间合法。

### 异常

与 `Arrays.sort` 一致：

| 情况 | 异常 |
|------|------|
| 空数组 / 空 List | 合法，无操作 |
| null 数组/List/算法/比较器；Comparable 区间内 null 元素 | `NullPointerException`（Comparator 版本允许 null 元素） |
| fromIndex < 0、toIndex > 长度、fromIndex > toIndex | `IndexOutOfBoundsException` |
| 算法与类型不适用（如对象数组用 COUNTING） | `IllegalArgumentException` |

校验顺序：null → 索引 → 适用性。

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

- **3100+ 矩阵测试**：适用算法 × 7 类型 × {随机/含负值/有序/逆序/重复/单双元素/空数组/区间/极值/非法入参}；double/float 另含 NaN/±0.0/±Infinity/次正规数；对象测试含**稳定性专项**、**防退化比较计数**、**TimSort run 栈交替输入**、命名方法与枚举分派等价性、默认算法等价性
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
| **default**（默认策略） | 354.2 | 6.6 | 37.2 | 112.0 | 8.6 |
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
| comb | 1285.9 | 165.8 | 477.8 | 1033.9 | 394.0 |
| bitonic | 2816.7 | 1769.4 | 1818.5 | 2038.5 | 1940.0 |
| tree | 2086.4 | 827.2 | 800.6 | 220.2 | 957.0 |
| **default**（默认策略） | 1197.0 | 13.5 | 36.9 | 812.5 | 15.7 |
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

> 数据为一次性运行（fork=1, warmup=1×1s, measurement=2×1s）的结果，仅供参考；精确对比请在本机按上述命令复测。测试环境：JDK 26.0.2（OpenJDK）、Windows 11、x64。

## 从 v1.0 升级（行为变更）

- **默认算法**：对象 `sort(a)` 由 QUICK 改为 **TIM**（稳定、自适应）；原始类型改为**自适应调度**
- **新增**：Comparator 重载、List 重载、15 个新算法（HEAP/TIM/COMB/GNOME/COCKTAIL/CYCLE/ODD_EVEN/PANCAKE/STOOGE/BITONIC/TREE/COUNTING/RADIX/PIGEONHOLE/BUCKET）
- **源码不兼容**：`sort(a, null)` 因新增 Comparator 重载产生编译歧义，需写 `sort(a, (Algorithm) null)`
- 原始类型实现不再由脚本生成（手工维护），生成器已移除

## 版本

- 当前版本：**1.1.0-SNAPSHOT**（开发中）
- 版本策略：[SemVer](https://semver.org/)；发布见 [Releases](https://github.com/cnzeropro/sort/releases)

## 许可证

[MIT](./LICENSE) © 2021-2026 Zero
