package org.zero.sort.benchmark;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.zero.sort.Algorithm;
import org.zero.sort.Sort;

/**
 * int[] 排序基准：全部适用算法 × 5 种输入形态，另含默认自适应策略与
 * {@link Arrays#sort(int[])}（双轴快排）基线对照。
 * <p>
 * 运行：mvn -Pbenchmarks compile exec:java -Dexec.args="SortIntBenchmark"
 *
 * @author Zero
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
public class SortIntBenchmark {

    /** O(n log n) 类算法规模 */
    private static final int N = 10_000;

    /** O(n^2) 类算法规模 */
    private static final int SMALL_N = 1_000;

    /** STOOGE（O(n^2.71)）规模 */
    private static final int TINY_N = 200;

    @Param({"RANDOM", "SORTED", "REVERSE", "DUPLICATES", "NEAR_SORTED"})
    public BenchShape shape;

    private int[] pristine;
    private int[] pristineSmall;
    private int[] pristineTiny;
    private int[] scratch;
    private int[] scratchSmall;
    private int[] scratchTiny;

    /** 计数/鸽巢排序需要值域有界（超 1<<24 抛 IAE），使用 0..65535 的有界数据集 */
    private int[] pristineBounded;
    private int[] scratchBounded;

    @Setup(org.openjdk.jmh.annotations.Level.Trial)
    public void setup() {
        pristine = new int[N];
        pristineSmall = new int[SMALL_N];
        pristineTiny = new int[TINY_N];
        fill(pristine, shape);
        fill(pristineSmall, shape);
        fill(pristineTiny, shape);
        pristineBounded = new int[N];
        fillBounded(pristineBounded, shape);
        scratch = new int[N];
        scratchSmall = new int[SMALL_N];
        scratchTiny = new int[TINY_N];
        scratchBounded = new int[N];
    }

    /** 值域 0..65535 的形态填充（计数/鸽巢专用） */
    private static void fillBounded(int[] a, BenchShape shape) {
        Random random = new Random(42L);
        switch (shape) {
            case SORTED:
                for (int i = 0; i < a.length; i++) {
                    a[i] = i % 65_536;
                }
                break;
            case REVERSE:
                for (int i = 0; i < a.length; i++) {
                    a[i] = 65_535 - (i % 65_536);
                }
                break;
            case DUPLICATES:
                for (int i = 0; i < a.length; i++) {
                    a[i] = random.nextInt(16);
                }
                break;
            case NEAR_SORTED:
                for (int i = 0; i < a.length; i++) {
                    a[i] = i % 65_536;
                }
                for (int i = 0; i < a.length / 200; i++) {
                    int x = random.nextInt(a.length - 1);
                    int t = a[x];
                    a[x] = a[x + 1];
                    a[x + 1] = t;
                }
                break;
            default:
                for (int i = 0; i < a.length; i++) {
                    a[i] = random.nextInt(65_536);
                }
                break;
        }
    }

    private static void fill(int[] a, BenchShape shape) {
        Random random = new Random(42L);
        switch (shape) {
            case SORTED:
                for (int i = 0; i < a.length; i++) {
                    a[i] = i;
                }
                break;
            case REVERSE:
                for (int i = 0; i < a.length; i++) {
                    a[i] = a.length - i;
                }
                break;
            case DUPLICATES:
                for (int i = 0; i < a.length; i++) {
                    a[i] = random.nextInt(16);
                }
                break;
            case NEAR_SORTED:
                for (int i = 0; i < a.length; i++) {
                    a[i] = i;
                }
                for (int i = 0; i < a.length / 200; i++) {
                    int x = random.nextInt(a.length - 1);
                    int t = a[x];
                    a[x] = a[x + 1];
                    a[x + 1] = t;
                }
                break;
            default:
                for (int i = 0; i < a.length; i++) {
                    a[i] = random.nextInt();
                }
                break;
        }
    }

    // ==================== O(n log n) 类（n = 10_000）====================

    @Benchmark
    public int[] shell() {
        System.arraycopy(pristine, 0, scratch, 0, N);
        Sort.sort(scratch, Algorithm.SHELL);
        return scratch;
    }

    @Benchmark
    public int[] merge() {
        System.arraycopy(pristine, 0, scratch, 0, N);
        Sort.sort(scratch, Algorithm.MERGE);
        return scratch;
    }

    @Benchmark
    public int[] quick() {
        System.arraycopy(pristine, 0, scratch, 0, N);
        Sort.sort(scratch, Algorithm.QUICK);
        return scratch;
    }

    @Benchmark
    public int[] heap() {
        System.arraycopy(pristine, 0, scratch, 0, N);
        Sort.sort(scratch, Algorithm.HEAP);
        return scratch;
    }

    @Benchmark
    public int[] tim() {
        System.arraycopy(pristine, 0, scratch, 0, N);
        Sort.sort(scratch, Algorithm.TIM);
        return scratch;
    }

    @Benchmark
    public int[] comb() {
        System.arraycopy(pristine, 0, scratch, 0, N);
        Sort.sort(scratch, Algorithm.COMB);
        return scratch;
    }

    @Benchmark
    public int[] bitonic() {
        System.arraycopy(pristine, 0, scratch, 0, N);
        Sort.sort(scratch, Algorithm.BITONIC);
        return scratch;
    }

    @Benchmark
    public int[] tree() {
        System.arraycopy(pristine, 0, scratch, 0, N);
        Sort.sort(scratch, Algorithm.TREE);
        return scratch;
    }

    @Benchmark
    public int[] counting() {
        System.arraycopy(pristineBounded, 0, scratchBounded, 0, N);
        Sort.sort(scratchBounded, Algorithm.COUNTING);
        return scratchBounded;
    }

    @Benchmark
    public int[] radix() {
        System.arraycopy(pristine, 0, scratch, 0, N);
        Sort.sort(scratch, Algorithm.RADIX);
        return scratch;
    }

    @Benchmark
    public int[] pigeonhole() {
        System.arraycopy(pristineBounded, 0, scratchBounded, 0, N);
        Sort.sort(scratchBounded, Algorithm.PIGEONHOLE);
        return scratchBounded;
    }

    @Benchmark
    public int[] defaultSort() {
        System.arraycopy(pristine, 0, scratch, 0, N);
        Sort.sort(scratch);
        return scratch;
    }

    /** 基线：JDK 双轴快排 */
    @Benchmark
    public int[] jdkArraysSort() {
        System.arraycopy(pristine, 0, scratch, 0, N);
        Arrays.sort(scratch);
        return scratch;
    }

    // ==================== O(n^2) 类（n = 1_000）====================

    @Benchmark
    public int[] bubble() {
        System.arraycopy(pristineSmall, 0, scratchSmall, 0, SMALL_N);
        Sort.sort(scratchSmall, Algorithm.BUBBLE);
        return scratchSmall;
    }

    @Benchmark
    public int[] selection() {
        System.arraycopy(pristineSmall, 0, scratchSmall, 0, SMALL_N);
        Sort.sort(scratchSmall, Algorithm.SELECTION);
        return scratchSmall;
    }

    @Benchmark
    public int[] insertion() {
        System.arraycopy(pristineSmall, 0, scratchSmall, 0, SMALL_N);
        Sort.sort(scratchSmall, Algorithm.INSERTION);
        return scratchSmall;
    }

    @Benchmark
    public int[] gnome() {
        System.arraycopy(pristineSmall, 0, scratchSmall, 0, SMALL_N);
        Sort.sort(scratchSmall, Algorithm.GNOME);
        return scratchSmall;
    }

    @Benchmark
    public int[] cocktail() {
        System.arraycopy(pristineSmall, 0, scratchSmall, 0, SMALL_N);
        Sort.sort(scratchSmall, Algorithm.COCKTAIL);
        return scratchSmall;
    }

    @Benchmark
    public int[] oddEven() {
        System.arraycopy(pristineSmall, 0, scratchSmall, 0, SMALL_N);
        Sort.sort(scratchSmall, Algorithm.ODD_EVEN);
        return scratchSmall;
    }

    @Benchmark
    public int[] cycle() {
        System.arraycopy(pristineSmall, 0, scratchSmall, 0, SMALL_N);
        Sort.sort(scratchSmall, Algorithm.CYCLE);
        return scratchSmall;
    }

    @Benchmark
    public int[] pancake() {
        System.arraycopy(pristineSmall, 0, scratchSmall, 0, SMALL_N);
        Sort.sort(scratchSmall, Algorithm.PANCAKE);
        return scratchSmall;
    }

    // ==================== STOOGE（n = 200）====================

    @Benchmark
    public int[] stooge() {
        System.arraycopy(pristineTiny, 0, scratchTiny, 0, TINY_N);
        Sort.sort(scratchTiny, Algorithm.STOOGE);
        return scratchTiny;
    }
}
