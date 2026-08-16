package org.zero.sort;

/**
 * 原始类型排序分派
 * <p>
 * 将 {@link Algorithm} 枚举分派到各原始类型的专用实现（{@code XxxSorts}）。
 * 适用性校验（{@link Algorithm.Applicability}）由公共门面 {@link Sort} 完成。
 *
 * @author Zero
 */
final class PrimitiveDispatcher {

    private PrimitiveDispatcher() {
    }

    /**
     * 对 byte[] 的 [from, to) 区间按指定算法排序
     *
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @param from      起始索引（含）
     * @param to        结束索引（不含）
     */
    static void sort(byte[] a, Algorithm algorithm, int from, int to) {
        switch (algorithm) {
            case BUBBLE:
                ByteSorts.bubble(a, from, to);
                break;
            case SELECTION:
                ByteSorts.selection(a, from, to);
                break;
            case INSERTION:
                ByteSorts.insertion(a, from, to);
                break;
            case SHELL:
                ByteSorts.shell(a, from, to);
                break;
            case MERGE:
                ByteSorts.merge(a, from, to);
                break;
            case QUICK:
                ByteSorts.quick(a, from, to);
                break;
            case HEAP:
                ByteSorts.heap(a, from, to);
                break;
            case TIM:
                ByteSorts.tim(a, from, to);
                break;
            case COMB:
                ByteSorts.comb(a, from, to);
                break;
            case GNOME:
                ByteSorts.gnome(a, from, to);
                break;
            case COCKTAIL:
                ByteSorts.cocktail(a, from, to);
                break;
            case CYCLE:
                ByteSorts.cycle(a, from, to);
                break;
            case ODD_EVEN:
                ByteSorts.oddEven(a, from, to);
                break;
            case PANCAKE:
                ByteSorts.pancake(a, from, to);
                break;
            case STOOGE:
                ByteSorts.stooge(a, from, to);
                break;
            case BITONIC:
                ByteSorts.bitonic(a, from, to);
                break;
            case TREE:
                ByteSorts.tree(a, from, to);
                break;
            case COUNTING:
                ByteSorts.counting(a, from, to);
                break;
            case RADIX:
                ByteSorts.radix(a, from, to);
                break;
            case PIGEONHOLE:
                ByteSorts.pigeonhole(a, from, to);
                break;
            default:
                throw new IllegalStateException("Unknown algorithm: " + algorithm);
        }
    }

    /**
     * 对 short[] 的 [from, to) 区间按指定算法排序
     *
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @param from      起始索引（含）
     * @param to        结束索引（不含）
     */
    static void sort(short[] a, Algorithm algorithm, int from, int to) {
        switch (algorithm) {
            case BUBBLE:
                ShortSorts.bubble(a, from, to);
                break;
            case SELECTION:
                ShortSorts.selection(a, from, to);
                break;
            case INSERTION:
                ShortSorts.insertion(a, from, to);
                break;
            case SHELL:
                ShortSorts.shell(a, from, to);
                break;
            case MERGE:
                ShortSorts.merge(a, from, to);
                break;
            case QUICK:
                ShortSorts.quick(a, from, to);
                break;
            case HEAP:
                ShortSorts.heap(a, from, to);
                break;
            case TIM:
                ShortSorts.tim(a, from, to);
                break;
            case COMB:
                ShortSorts.comb(a, from, to);
                break;
            case GNOME:
                ShortSorts.gnome(a, from, to);
                break;
            case COCKTAIL:
                ShortSorts.cocktail(a, from, to);
                break;
            case CYCLE:
                ShortSorts.cycle(a, from, to);
                break;
            case ODD_EVEN:
                ShortSorts.oddEven(a, from, to);
                break;
            case PANCAKE:
                ShortSorts.pancake(a, from, to);
                break;
            case STOOGE:
                ShortSorts.stooge(a, from, to);
                break;
            case BITONIC:
                ShortSorts.bitonic(a, from, to);
                break;
            case TREE:
                ShortSorts.tree(a, from, to);
                break;
            case COUNTING:
                ShortSorts.counting(a, from, to);
                break;
            case RADIX:
                ShortSorts.radix(a, from, to);
                break;
            case PIGEONHOLE:
                ShortSorts.pigeonhole(a, from, to);
                break;
            default:
                throw new IllegalStateException("Unknown algorithm: " + algorithm);
        }
    }

    /**
     * 对 int[] 的 [from, to) 区间按指定算法排序
     *
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @param from      起始索引（含）
     * @param to        结束索引（不含）
     */
    static void sort(int[] a, Algorithm algorithm, int from, int to) {
        switch (algorithm) {
            case BUBBLE:
                IntSorts.bubble(a, from, to);
                break;
            case SELECTION:
                IntSorts.selection(a, from, to);
                break;
            case INSERTION:
                IntSorts.insertion(a, from, to);
                break;
            case SHELL:
                IntSorts.shell(a, from, to);
                break;
            case MERGE:
                IntSorts.merge(a, from, to);
                break;
            case QUICK:
                IntSorts.quick(a, from, to);
                break;
            case HEAP:
                IntSorts.heap(a, from, to);
                break;
            case TIM:
                IntSorts.tim(a, from, to);
                break;
            case COMB:
                IntSorts.comb(a, from, to);
                break;
            case GNOME:
                IntSorts.gnome(a, from, to);
                break;
            case COCKTAIL:
                IntSorts.cocktail(a, from, to);
                break;
            case CYCLE:
                IntSorts.cycle(a, from, to);
                break;
            case ODD_EVEN:
                IntSorts.oddEven(a, from, to);
                break;
            case PANCAKE:
                IntSorts.pancake(a, from, to);
                break;
            case STOOGE:
                IntSorts.stooge(a, from, to);
                break;
            case BITONIC:
                IntSorts.bitonic(a, from, to);
                break;
            case TREE:
                IntSorts.tree(a, from, to);
                break;
            case COUNTING:
                IntSorts.counting(a, from, to);
                break;
            case RADIX:
                IntSorts.radix(a, from, to);
                break;
            case PIGEONHOLE:
                IntSorts.pigeonhole(a, from, to);
                break;
            default:
                throw new IllegalStateException("Unknown algorithm: " + algorithm);
        }
    }

    /**
     * 对 long[] 的 [from, to) 区间按指定算法排序
     *
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @param from      起始索引（含）
     * @param to        结束索引（不含）
     */
    static void sort(long[] a, Algorithm algorithm, int from, int to) {
        switch (algorithm) {
            case BUBBLE:
                LongSorts.bubble(a, from, to);
                break;
            case SELECTION:
                LongSorts.selection(a, from, to);
                break;
            case INSERTION:
                LongSorts.insertion(a, from, to);
                break;
            case SHELL:
                LongSorts.shell(a, from, to);
                break;
            case MERGE:
                LongSorts.merge(a, from, to);
                break;
            case QUICK:
                LongSorts.quick(a, from, to);
                break;
            case HEAP:
                LongSorts.heap(a, from, to);
                break;
            case TIM:
                LongSorts.tim(a, from, to);
                break;
            case COMB:
                LongSorts.comb(a, from, to);
                break;
            case GNOME:
                LongSorts.gnome(a, from, to);
                break;
            case COCKTAIL:
                LongSorts.cocktail(a, from, to);
                break;
            case CYCLE:
                LongSorts.cycle(a, from, to);
                break;
            case ODD_EVEN:
                LongSorts.oddEven(a, from, to);
                break;
            case PANCAKE:
                LongSorts.pancake(a, from, to);
                break;
            case STOOGE:
                LongSorts.stooge(a, from, to);
                break;
            case BITONIC:
                LongSorts.bitonic(a, from, to);
                break;
            case TREE:
                LongSorts.tree(a, from, to);
                break;
            case COUNTING:
                LongSorts.counting(a, from, to);
                break;
            case RADIX:
                LongSorts.radix(a, from, to);
                break;
            case PIGEONHOLE:
                LongSorts.pigeonhole(a, from, to);
                break;
            default:
                throw new IllegalStateException("Unknown algorithm: " + algorithm);
        }
    }

    /**
     * 对 float[] 的 [from, to) 区间按指定算法排序
     *
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @param from      起始索引（含）
     * @param to        结束索引（不含）
     */
    static void sort(float[] a, Algorithm algorithm, int from, int to) {
        switch (algorithm) {
            case BUBBLE:
                FloatSorts.bubble(a, from, to);
                break;
            case SELECTION:
                FloatSorts.selection(a, from, to);
                break;
            case INSERTION:
                FloatSorts.insertion(a, from, to);
                break;
            case SHELL:
                FloatSorts.shell(a, from, to);
                break;
            case MERGE:
                FloatSorts.merge(a, from, to);
                break;
            case QUICK:
                FloatSorts.quick(a, from, to);
                break;
            case HEAP:
                FloatSorts.heap(a, from, to);
                break;
            case TIM:
                FloatSorts.tim(a, from, to);
                break;
            case COMB:
                FloatSorts.comb(a, from, to);
                break;
            case GNOME:
                FloatSorts.gnome(a, from, to);
                break;
            case COCKTAIL:
                FloatSorts.cocktail(a, from, to);
                break;
            case CYCLE:
                FloatSorts.cycle(a, from, to);
                break;
            case ODD_EVEN:
                FloatSorts.oddEven(a, from, to);
                break;
            case PANCAKE:
                FloatSorts.pancake(a, from, to);
                break;
            case STOOGE:
                FloatSorts.stooge(a, from, to);
                break;
            case BITONIC:
                FloatSorts.bitonic(a, from, to);
                break;
            case TREE:
                FloatSorts.tree(a, from, to);
                break;
            case BUCKET:
                FloatSorts.bucket(a, from, to);
                break;
            default:
                throw new IllegalStateException("Unknown algorithm: " + algorithm);
        }
    }

    /**
     * 对 double[] 的 [from, to) 区间按指定算法排序
     *
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @param from      起始索引（含）
     * @param to        结束索引（不含）
     */
    static void sort(double[] a, Algorithm algorithm, int from, int to) {
        switch (algorithm) {
            case BUBBLE:
                DoubleSorts.bubble(a, from, to);
                break;
            case SELECTION:
                DoubleSorts.selection(a, from, to);
                break;
            case INSERTION:
                DoubleSorts.insertion(a, from, to);
                break;
            case SHELL:
                DoubleSorts.shell(a, from, to);
                break;
            case MERGE:
                DoubleSorts.merge(a, from, to);
                break;
            case QUICK:
                DoubleSorts.quick(a, from, to);
                break;
            case HEAP:
                DoubleSorts.heap(a, from, to);
                break;
            case TIM:
                DoubleSorts.tim(a, from, to);
                break;
            case COMB:
                DoubleSorts.comb(a, from, to);
                break;
            case GNOME:
                DoubleSorts.gnome(a, from, to);
                break;
            case COCKTAIL:
                DoubleSorts.cocktail(a, from, to);
                break;
            case CYCLE:
                DoubleSorts.cycle(a, from, to);
                break;
            case ODD_EVEN:
                DoubleSorts.oddEven(a, from, to);
                break;
            case PANCAKE:
                DoubleSorts.pancake(a, from, to);
                break;
            case STOOGE:
                DoubleSorts.stooge(a, from, to);
                break;
            case BITONIC:
                DoubleSorts.bitonic(a, from, to);
                break;
            case TREE:
                DoubleSorts.tree(a, from, to);
                break;
            case BUCKET:
                DoubleSorts.bucket(a, from, to);
                break;
            default:
                throw new IllegalStateException("Unknown algorithm: " + algorithm);
        }
    }

    /**
     * 对 char[] 的 [from, to) 区间按指定算法排序
     *
     * @param a         待排序数组
     * @param algorithm 排序算法
     * @param from      起始索引（含）
     * @param to        结束索引（不含）
     */
    static void sort(char[] a, Algorithm algorithm, int from, int to) {
        switch (algorithm) {
            case BUBBLE:
                CharSorts.bubble(a, from, to);
                break;
            case SELECTION:
                CharSorts.selection(a, from, to);
                break;
            case INSERTION:
                CharSorts.insertion(a, from, to);
                break;
            case SHELL:
                CharSorts.shell(a, from, to);
                break;
            case MERGE:
                CharSorts.merge(a, from, to);
                break;
            case QUICK:
                CharSorts.quick(a, from, to);
                break;
            case HEAP:
                CharSorts.heap(a, from, to);
                break;
            case TIM:
                CharSorts.tim(a, from, to);
                break;
            case COMB:
                CharSorts.comb(a, from, to);
                break;
            case GNOME:
                CharSorts.gnome(a, from, to);
                break;
            case COCKTAIL:
                CharSorts.cocktail(a, from, to);
                break;
            case CYCLE:
                CharSorts.cycle(a, from, to);
                break;
            case ODD_EVEN:
                CharSorts.oddEven(a, from, to);
                break;
            case PANCAKE:
                CharSorts.pancake(a, from, to);
                break;
            case STOOGE:
                CharSorts.stooge(a, from, to);
                break;
            case BITONIC:
                CharSorts.bitonic(a, from, to);
                break;
            case TREE:
                CharSorts.tree(a, from, to);
                break;
            case COUNTING:
                CharSorts.counting(a, from, to);
                break;
            case RADIX:
                CharSorts.radix(a, from, to);
                break;
            case PIGEONHOLE:
                CharSorts.pigeonhole(a, from, to);
                break;
            default:
                throw new IllegalStateException("Unknown algorithm: " + algorithm);
        }
    }

    /** 对 byte[] 的 [from, to) 区间使用自适应默认策略排序 */
    static void sortDefault(byte[] a, int from, int to) {
        ByteSorts.sortDefault(a, from, to);
    }

    /** 对 short[] 的 [from, to) 区间使用自适应默认策略排序 */
    static void sortDefault(short[] a, int from, int to) {
        ShortSorts.sortDefault(a, from, to);
    }

    /** 对 int[] 的 [from, to) 区间使用自适应默认策略排序 */
    static void sortDefault(int[] a, int from, int to) {
        IntSorts.sortDefault(a, from, to);
    }

    /** 对 long[] 的 [from, to) 区间使用自适应默认策略排序 */
    static void sortDefault(long[] a, int from, int to) {
        LongSorts.sortDefault(a, from, to);
    }

    /** 对 float[] 的 [from, to) 区间使用自适应默认策略排序 */
    static void sortDefault(float[] a, int from, int to) {
        FloatSorts.sortDefault(a, from, to);
    }

    /** 对 double[] 的 [from, to) 区间使用自适应默认策略排序 */
    static void sortDefault(double[] a, int from, int to) {
        DoubleSorts.sortDefault(a, from, to);
    }

    /** 对 char[] 的 [from, to) 区间使用自适应默认策略排序 */
    static void sortDefault(char[] a, int from, int to) {
        CharSorts.sortDefault(a, from, to);
    }
}
