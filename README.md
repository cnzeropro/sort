# sort

经典排序算法的 Java 工具类库：**21 种排序算法**，一行 API，支持 `Comparable` / `Comparator` 对象与**全部数字原始类型**。

## 特性

- **21 种算法**：冒泡 / 选择 / 插入 / 希尔 / 归并 / 快速 / 堆 / Tim / 梳 / 地精 / 鸡尾酒 / 循环 / 奇偶 / 煎饼 / 臭皮匠 / 双调 / 树 / 计数 / 基数 / 鸽巢 / 桶
- **API 简单**：`Sort.sort(a)` 一行排序；`Sort.sort(a, Algorithm.MERGE, 1, 5)` 指定算法与区间；Comparator 与 List 全支持
- **自适应默认**：对象默认 **Tim 排序**（检测有序片段，已有序 O(n)、稳定，与 JDK `Arrays.sort(Object[])` 同款）；原始类型默认自适应调度（小数组插入、近有序归并、否则快排）
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
| shell | 695.6 | 17.1 | 92.0 | 346.3 | 61.3 |
| merge | 508.8 | 10.5 | 80.7 | 294.0 | 15.4 |
| quick | 500.2 | 68.0 | 77.8 | 210.7 | 74.7 |
| heap | 614.5 | 404.1 | 431.8 | 541.2 | 456.1 |
| tim | 694.1 | 5.3 | 14.1 | 456.3 | 23.3 |
| comb | 646.0 | 62.6 | 101.1 | 224.2 | 93.2 |
| bitonic | 2819.3 | 1854.6 | 1737.6 | 1927.0 | 1601.1 |
| tree | 683.2 | 63608.0 | 65128.8 | 6326.0 | 67350.0 |
| counting | 130.4 | 46.0 | 43.8 | 31.0 | 16.5 |
| radix | 61.0 | 111.8 | 109.8 | 128.3 | 117.5 |
| pigeonhole | 137.4 | 19.3 | 18.0 | 12.0 | 16.9 |
| **default**（默认策略） | 449.0 | 11.1 | 73.4 | 222.7 | 23.8 |
| **Arrays.sort**（JDK 基线） | 392.6 | 2.5 | 5.5 | 130.5 | 27.8 |
| bubble | 685.5 | 0.2 | 183.3 | 530.6 | 0.4 |
| selection | 194.6 | 89.4 | 855.9 | 170.6 | 214.8 |
| insertion | 43.6 | 0.2 | 132.5 | 41.8 | 0.8 |
| gnome | 605.0 | 0.2 | 1036.9 | 469.2 | 0.6 |
| cocktail | 661.7 | 0.2 | 255.2 | 602.0 | 0.4 |
| oddEven | 566.4 | 0.8 | 401.4 | 624.7 | 1.5 |
| cycle | 1011.4 | 78.7 | 223.1 | 1110.6 | 141.0 |
| pancake | 439.9 | 94.0 | 166.0 | 379.4 | 304.5 |
| stooge | 1481.6 | 1137.6 | 1242.3 | 1235.3 | 1271.0 |

> n=10⁴；bubble/selection/insertion/gnome/cocktail/oddEven/cycle/pancake 为 n=10³；stooge 为 n=200；counting/pigeonhole 使用值域 0..65535 的有界数据。

### Integer[]（µs/op，越小越好）

| 算法 | RANDOM | SORTED | REVERSE | DUPLICATES | NEAR_SORTED |
|------|---:|---:|---:|---:|---:
| shell | 1589.3 | 156.4 | 382.3 | 783.0 | 175.2 |
| merge | 979.7 | 28.2 | 324.8 | 648.4 | 37.0 |
| quick | 863.5 | 139.4 | 148.9 | 542.9 | 136.3 |
| heap | 1462.6 | 1111.3 | 1157.4 | 1188.2 | 1104.3 |
| tim | 1145.9 | 12.2 | 32.7 | 722.4 | 73.6 |
| comb | 1974.8 | 170.8 | 411.4 | 958.8 | 329.0 |
| bitonic | 3069.7 | 1950.3 | 1944.4 | 2495.0 | 2843.8 |
| tree | 2578.4 | 1488.3 | 924.2 | 247.8 | 1008.6 |
| **default**（默认策略） | 1148.4 | 11.6 | 33.4 | 723.3 | 74.5 |
| **Arrays.sort**（JDK 基线） | 1026.3 | 9.7 | 26.3 | 645.1 | 18.4 |
| bubble | 1359.1 | 0.5 | 1491.4 | 1518.4 | 2.1 |
| selection | 704.3 | 212.1 | 640.5 | 560.0 | 672.4 |
| insertion | 370.9 | 2.0 | 914.8 | 425.0 | 3.1 |
| gnome | 1416.4 | 0.6 | 2731.0 | 1328.5 | 1.5 |
| cocktail | 1201.1 | 0.5 | 2030.2 | 1330.1 | 1.5 |
| oddEven | 1460.0 | 0.9 | 1949.0 | 1833.6 | 3.1 |
| cycle | 1963.2 | 213.6 | 655.5 | 861.8 | 285.4 |
| pancake | 1906.8 | 385.0 | 273.7 | 1693.9 | 587.1 |
| stooge | 1638.7 | 1464.5 | 1681.6 | 1900.6 | 1656.9 |

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
