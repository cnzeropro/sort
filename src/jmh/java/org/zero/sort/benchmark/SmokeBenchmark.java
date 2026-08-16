package org.zero.sort.benchmark;

import java.util.Random;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.zero.sort.Algorithm;
import org.zero.sort.Sort;

import java.util.concurrent.TimeUnit;

/**
 * CI 冒烟基准：规模极小，用于验证基准代码可编译可运行（防腐化）
 *
 * @author Zero
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 1, time = 1)
public class SmokeBenchmark {

    private static final int N = 1000;

    private int[] data;
    private int[] scratch;

    @Setup
    public void setup() {
        Random random = new Random(42L);
        data = new int[N];
        for (int i = 0; i < N; i++) {
            data[i] = random.nextInt();
        }
        scratch = new int[N];
    }

    @Benchmark
    public int[] quickSort() {
        System.arraycopy(data, 0, scratch, 0, N);
        Sort.sort(scratch, Algorithm.QUICK);
        return scratch;
    }
}
