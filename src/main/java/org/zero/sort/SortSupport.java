package org.zero.sort;

import java.util.List;

/**
 * 算法类共享的内部工具：List 与数组的互转。
 * <p>
 * 包私有，不对外暴露。每个算法类通过 {@link #listToArray(List)} 把 List 转为数组、
 * 排序后通过 {@link #copyBack(List, Object[])} 回写，从而复用同一套数组排序实现。
 *
 * @author Zero
 */
final class SortSupport {

    private SortSupport() {
    }

    /**
     * 校验 List 非 null 并转成对象数组
     * <p>
     * 返回数组的运行时类型为 {@code Object[]}，仅供元素类型无上界约束的
     * Comparator 路径使用；Comparable 路径须使用 {@link #comparableListToArray(List)}，
     * 否则调用点编译器插入的 {@code checkcast [Comparable} 会抛 ClassCastException。
     *
     * @param <T>  元素类型
     * @param list 待转换的 List
     * @return 与 List 元素等序的对象数组
     * @throws NullPointerException list 为 null
     */
    static <T> T[] listToArray(List<? extends T> list) {
        if (list == null) {
            throw new NullPointerException("list must not be null");
        }
        @SuppressWarnings("unchecked")
        T[] a = (T[]) list.toArray(new Object[list.size()]);
        return a;
    }

    /**
     * 校验 List 非 null 并转成元素为 Comparable 的数组
     * <p>
     * 返回数组的运行时类型为 {@code Comparable[]}，保证 Comparable 路径调用点
     * 编译器插入的数组类型转换可以通过。
     *
     * @param <T>  元素类型
     * @param list 待转换的 List
     * @return 与 List 元素等序的数组
     * @throws NullPointerException    list 为 null
     * @throws ArrayStoreException     元素不是 Comparable
     */
    static <T extends Comparable<? super T>> T[] comparableListToArray(List<? extends T> list) {
        if (list == null) {
            throw new NullPointerException("list must not be null");
        }
        @SuppressWarnings("unchecked")
        T[] a = (T[]) list.toArray(new Comparable[list.size()]);
        return a;
    }

    /**
     * 把排序结果回写到 List（原地排序语义）
     *
     * @param <T>  元素类型
     * @param list 目标 List
     * @param a    已排序的数组
     */
    static <T> void copyBack(List<? super T> list, T[] a) {
        for (int i = 0; i < a.length; i++) {
            list.set(i, a[i]);
        }
    }
}
