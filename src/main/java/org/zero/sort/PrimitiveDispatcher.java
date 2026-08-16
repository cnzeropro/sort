package org.zero.sort;



/**
 * 原始类型排序分派
 * <p>
 * ★ 本文件由 tools/gen-primitives.py 自动生成，请勿手改；如需修改请编辑脚本后重新生成。
 * <p>
 * 将 {@link Algorithm} 枚举分派到各原始类型的专用实现（{@code XxxSorts}）。
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
            default:
                throw new IllegalStateException("Unknown algorithm: " + algorithm);
        }
    }
}
