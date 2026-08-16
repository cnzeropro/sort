package org.zero.sort.benchmark;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
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
 * Integer[] 排序基准：全部适用算法（17 种比较类）× 5 种输入形态，
 * 另含默认算法（Tim）与 {@link Arrays#sort(Object[])}（JDK TimSort）基线对照。
 * <p>
 * 运行：mvn -Pbenchmarks compile exec:java -Dexec.args="SortObjectBenchmark"
 *
 * @author Zero
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
public class SortObjectBenchmark {

    /** O(n log n) 类算法规模 */
    private static final int N = 10_000;

    /** O(n^2) 类算法规模 */
    private static final int SMALL_N = 1_000;

    /** STOOGE（O(n^2.71)）规模 */
    private static final int TINY_N = 200;

    @Param({"RANDOM", "SORTED", "REVERSE", "DUPLICATES", "NEAR_SORTED"})
    public BenchShape shape;

    private Integer[] pristine;
    private Integer[] pristineSmall;
    private Integer[] pristineTiny;
    private Integer[] scratch;
    private Integer[] scratchSmall;
    private Integer[] scratchTiny;

    @Setup(Level.Trial)
    public void setup() {
        pristine = new Integer[N];
        pristineSmall = new Integer[SMALL_N];
        pristineTiny = new Integer[TINY_N];
        fill(pristine, shape);
        fill(pristineSmall, shape);
        fill(pristineTiny, shape);
        scratch = new Integer[N];
        scratchSmall = new Integer[SMALL_N];
        scratchTiny = new Integer[TINY_N];
    }

    private static void fill(Integer[] a, BenchShape shape) {
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
                    Integer t = a[x];
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
    public Integer[] shell() {
        System.arraycopy(pristine, 0, scratch, 0, N);
        Sort.sort(scratch, Algorithm.SHELL);
        return scratch;
    }

    @Benchmark
    public Integer[] merge() {
        System.arraycopy(pristine, 0, scratch, 0, N);
        Sort.sort(scratch, Algorithm.MERGE);
        return scratch;
    }

    @Benchmark
    public Integer[] quick() {
        System.arraycopy(pristine, 0, scratch, 0, N);
        Sort.sort(scratch, Algorithm.QUICK);
        return scratch;
    }

    @Benchmark
    public Integer[] heap() {
        System.arraycopy(pristine, 0, scratch, 0, N);
        Sort.sort(scratch, Algorithm.HEAP);
        return scratch;
    }

    @Benchmark
    public Integer[] tim() {
        System.arraycopy(pristine, 0, scratch, 0, N);
        Sort.sort(scratch, Algorithm.TIM);
        return scratch;
    }

    @Benchmark
    public Integer[] comb() {
        System.arraycopy(pristine, 0, scratch, 0, N);
        Sort.sort(scratch, Algorithm.COMB);
        return scratch;
    }

    @Benchmark
    public Integer[] bitonic() {
        System.arraycopy(pristine, 0, scratch, 0, N);
        Sort.sort(scratch, Algorithm.BITONIC);
        return scratch;
    }

    @Benchmark
    public Integer[] tree() {
        System.arraycopy(pristine, 0, scratch, 0, N);
        Sort.sort(scratch, Algorithm.TREE);
        return scratch;
    }

    @Benchmark
    public Integer[] defaultSort() {
        System.arraycopy(pristine, 0, scratch, 0, N);
        Sort.sort(scratch);
        return scratch;
    }

    /** 基线：JDK TimSort */
    @Benchmark
    public Integer[] jdkArraysSort() {
        System.arraycopy(pristine, 0, scratch, 0, N);
        Arrays.sort(scratch);
        return scratch;
    }

    // ==================== O(n^2) 类（n = 1_000）====================

    @Benchmark
    public Integer[] bubble() {
        System.arraycopy(pristineSmall, 0, scratchSmall, 0, SMALL_N);
        Sort.sort(scratchSmall, Algorithm.BUBBLE);
        return scratchSmall;
    }

    @Benchmark
    public Integer[] selection() {
        System.arraycopy(pristineSmall, 0, scratchSmall, 0, SMALL_N);
        Sort.sort(scratchSmall, Algorithm.SELECTION);
        return scratchSmall;
    }

    @Benchmark
    public Integer[] insertion() {
        System.arraycopy(pristineSmall, 0, scratchSmall, 0, SMALL_N);
        Sort.sort(scratchSmall, Algorithm.INSERTION);
        return scratchSmall;
    }

    @Benchmark
    public Integer[] gnome() {
        System.arraycopy(pristineSmall, 0, scratchSmall, 0, SMALL_N);
        Sort.sort(scratchSmall, Algorithm.GNOME);
        return scratchSmall;
    }

    @Benchmark
    public Integer[] cocktail() {
        System.arraycopy(pristineSmall, 0, scratchSmall, 0, SMALL_N);
        Sort.sort(scratchSmall, Algorithm.COCKTAIL);
        return scratchSmall;
    }

    @Benchmark
    public Integer[] oddEven() {
        System.arraycopy(pristineSmall, 0, scratchSmall, 0, SMALL_N);
        Sort.sort(scratchSmall, Algorithm.ODD_EVEN);
        return scratchSmall;
    }

    @Benchmark
    public Integer[] cycle() {
        System.arraycopy(pristineSmall, 0, scratchSmall, 0, SMALL_N);
        Sort.sort(scratchSmall, Algorithm.CYCLE);
        return scratchSmall;
    }

    @Benchmark
    public Integer[] pancake() {
        System.arraycopy(pristineSmall, 0, scratchSmall, 0, SMALL_N);
        Sort.sort(scratchSmall, Algorithm.PANCAKE);
        return scratchSmall;
    }

    // ==================== STOOGE（n = 200）====================

    @Benchmark
    public Integer[] stooge() {
        System.arraycopy(pristineTiny, 0, scratchTiny, 0, TINY_N);
        Sort.sort(scratchTiny, Algorithm.STOOGE);
        return scratchTiny;
    }
}
