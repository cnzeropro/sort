package org.zero.sort;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * {@link Sort} 门面类正确性测试。
 * <p>
 * 以 JDK 的 {@link Arrays#sort} / {@link Collections#sort} 为基准（oracle）做差分验证，覆盖：
 * <ol>
 *   <li>7 种原始类型 × 8 种数据形态（随机含重复、含负值、已有序、逆序、全等、空、单元素、两元素）；</li>
 *   <li>原始类型算法自动选择的三分支：n &lt; 47 小数组、n ≥ 47 近有序（相邻逆序对 &lt; n/8）、
 *       n ≥ 47 完全随机——均以结果正确性验证，不依赖内部实现；</li>
 *   <li>大数组冒烟（int[100_000]）；</li>
 *   <li>对象路径：{@code Integer[]}、{@code List<Integer>}、{@code List<String>} 与 Comparator 各重载；</li>
 *   <li>区间重载：区间外前缀/后缀不受影响；</li>
 *   <li>浮点特殊值（NaN 全部在末尾、-0.0 在 +0.0 之前）与 char 无符号序；</li>
 *   <li>异常契约：null 参数、非法索引、区间内 null 元素、null Comparator。</li>
 * </ol>
 * 所有随机数据均使用固定种子 {@value #SEED}，保证测试可复现。
 *
 * @author Zero
 */
class SortTest {

    /** 固定随机种子，保证测试可复现 */
    private static final long SEED = 42L;

    // ==================== 1. 原始类型全形态 ====================

    /**
     * byte 数组各形态排序与 Arrays.sort 结果一致。
     */
    @Test
    void sortByteArrayMatchesJdk() {
        Random rnd = newRandom();
        for (int[] form : standardForms(rnd)) {
            check(toBytes(form));
        }
    }

    /**
     * short 数组各形态排序与 Arrays.sort 结果一致。
     */
    @Test
    void sortShortArrayMatchesJdk() {
        Random rnd = newRandom();
        for (int[] form : standardForms(rnd)) {
            check(toShorts(form));
        }
    }

    /**
     * int 数组各形态排序与 Arrays.sort 结果一致。
     */
    @Test
    void sortIntArrayMatchesJdk() {
        Random rnd = newRandom();
        for (int[] form : standardForms(rnd)) {
            check(form);
        }
    }

    /**
     * long 数组各形态排序与 Arrays.sort 结果一致。
     */
    @Test
    void sortLongArrayMatchesJdk() {
        Random rnd = newRandom();
        for (int[] form : standardForms(rnd)) {
            check(toLongs(form));
        }
    }

    /**
     * float 数组各形态排序与 Arrays.sort 结果一致。
     */
    @Test
    void sortFloatArrayMatchesJdk() {
        Random rnd = newRandom();
        for (int[] form : standardForms(rnd)) {
            check(toFloats(form));
        }
    }

    /**
     * double 数组各形态排序与 Arrays.sort 结果一致。
     */
    @Test
    void sortDoubleArrayMatchesJdk() {
        Random rnd = newRandom();
        for (int[] form : standardForms(rnd)) {
            check(toDoubles(form));
        }
    }

    /**
     * char 数组各形态排序与 Arrays.sort 结果一致（char 无"负值"，以全值域随机形态替代）。
     */
    @Test
    void sortCharArrayMatchesJdk() {
        Random rnd = newRandom();
        for (int[] form : charForms(rnd)) {
            check(toChars(form));
        }
    }

    // ==================== 2. 三分支专项 ====================

    /**
     * 小数组分支（n &lt; 47）：扫描 0..46 全部长度，结果均正确。
     */
    @Test
    void smallArrayBranchBelow47() {
        Random rnd = newRandom();
        for (int n = 0; n <= 46; n++) {
            check(randomWithDuplicates(n, 50, rnd));
        }
        check(toBytes(randomWithDuplicates(46, 50, rnd)));
        check(toDoubles(randomWithDuplicates(46, 50, rnd)));
    }

    /**
     * 近有序分支（n ≥ 47 且相邻逆序对 &lt; n/8）：有序数组随机交换少量相邻对后结果正确。
     */
    @Test
    void nearlySortedBranchAtLeast47() {
        Random rnd = newRandom();
        check(nearlySorted(1000, rnd));
        check(toDoubles(nearlySorted(1000, rnd)));
    }

    /**
     * 完全随机分支（n ≥ 47）：大随机数组结果正确。
     */
    @Test
    void fullyRandomBranchAtLeast47() {
        Random rnd = newRandom();
        int[] a = new int[2000];
        for (int i = 0; i < a.length; i++) {
            a[i] = rnd.nextInt();
        }
        check(a);
        float[] f = new float[2000];
        for (int i = 0; i < f.length; i++) {
            f[i] = rnd.nextFloat() * 200 - 100;
        }
        check(f);
    }

    // ==================== 3. 大数组冒烟 ====================

    /**
     * 大数组冒烟：int[100_000] 随机数据排序结果与 Arrays.sort 一致（控制耗时，仅做一次）。
     */
    @Test
    void largeIntArraySmoke() {
        Random rnd = newRandom();
        int[] a = new int[100_000];
        for (int i = 0; i < a.length; i++) {
            a[i] = rnd.nextInt();
        }
        check(a);
    }

    // ==================== 4. 对象路径 ====================

    /**
     * Integer[] 对象数组各形态与 Arrays.sort（Tim 排序路径）结果一致。
     */
    @Test
    void integerArrayMatchesJdk() {
        Random rnd = newRandom();
        Integer[][] forms = {
                box(randomWithDuplicates(150, 12, rnd)),
                box(randomWithNegatives(150, rnd)),
                box(ascending(150)),
                box(descending(150)),
                box(allEqual(150)),
                box(new int[0]),
                new Integer[]{42},
                new Integer[]{9, 3}
        };
        for (Integer[] a : forms) {
            check(a);
        }
    }

    /**
     * List&lt;Integer&gt; 各形态与 Collections.sort 结果一致。
     */
    @Test
    void integerListMatchesCollectionsSort() {
        List<List<Integer>> forms = new ArrayList<>();
        forms.add(toIntegerList(randomWithDuplicates(150, 12, newRandom())));
        forms.add(toIntegerList(randomWithNegatives(150, newRandom())));
        forms.add(toIntegerList(ascending(150)));
        forms.add(toIntegerList(descending(150)));
        forms.add(toIntegerList(allEqual(150)));
        forms.add(new ArrayList<Integer>());
        forms.add(new ArrayList<>(Collections.singletonList(42)));
        forms.add(new ArrayList<>(Arrays.asList(9, 3)));
        for (List<Integer> list : forms) {
            check(list);
        }
    }

    /**
     * List&lt;String&gt;（含重复、空串、大小写混合）与 Collections.sort 结果一致。
     */
    @Test
    void stringListMatchesCollectionsSort() {
        List<String> list = new ArrayList<>(Arrays.asList(
                "banana", "apple", "", "Cherry", "apple", "date",
                "Banana", "banana", " ", "cherry", "Apple", "appl"));
        check(list);
    }

    /**
     * Comparator 各重载（整数组、区间 + Comparator、List + Comparator）与 JDK 带比较器排序一致。
     */
    @Test
    void comparatorOverloadsMatchJdk() {
        Random rnd = newRandom();
        Comparator<Integer> desc = Comparator.reverseOrder();

        Integer[] whole = box(randomWithDuplicates(150, 12, rnd));
        Integer[] expectedWhole = whole.clone();
        Arrays.sort(expectedWhole, desc);
        Sort.sort(whole, desc);
        assertArrayEquals(expectedWhole, whole);

        Integer[] from = box(randomWithDuplicates(150, 12, rnd));
        Integer[] expectedFrom = from.clone();
        Arrays.sort(expectedFrom, 20, expectedFrom.length, desc);
        Sort.sort(from, 20, desc);
        assertArrayEquals(expectedFrom, from);

        Integer[] range = box(randomWithDuplicates(150, 12, rnd));
        Integer[] expectedRange = range.clone();
        Arrays.sort(expectedRange, 20, 100, desc);
        Sort.sort(range, 20, 100, desc);
        assertArrayEquals(expectedRange, range);

        List<String> list = new ArrayList<>(Arrays.asList("bb", "a", "ccc", "", "dd", "a"));
        List<String> expectedList = new ArrayList<>(list);
        Collections.sort(expectedList, Comparator.comparingInt(String::length));
        Sort.sort(list, Comparator.comparingInt(String::length));
        assertEquals(expectedList, list);
    }

    // ==================== 5. 区间重载 ====================

    /**
     * int 区间重载 sort(a, from) / sort(a, from, to)：前缀与后缀元素不受影响，区间内正确有序。
     */
    @Test
    void intRangeSortKeepsOutsideUntouched() {
        Random rnd = newRandom();
        int[] original = new int[60];
        for (int i = 0; i < original.length; i++) {
            original[i] = rnd.nextInt(1000);
        }

        int[] a = original.clone();
        Sort.sort(a, 10, 40);
        assertArrayEquals(Arrays.copyOfRange(original, 0, 10), Arrays.copyOfRange(a, 0, 10), "前缀被改动");
        assertArrayEquals(Arrays.copyOfRange(original, 40, 60), Arrays.copyOfRange(a, 40, 60), "后缀被改动");
        int[] expectedMid = Arrays.copyOfRange(original, 10, 40);
        Arrays.sort(expectedMid);
        assertArrayEquals(expectedMid, Arrays.copyOfRange(a, 10, 40), "区间内未正确排序");

        int[] b = original.clone();
        Sort.sort(b, 30);
        assertArrayEquals(Arrays.copyOfRange(original, 0, 30), Arrays.copyOfRange(b, 0, 30), "前缀被改动");
        int[] expectedTail = Arrays.copyOfRange(original, 30, 60);
        Arrays.sort(expectedTail);
        assertArrayEquals(expectedTail, Arrays.copyOfRange(b, 30, 60), "尾部区间未正确排序");
    }

    /**
     * 对象数组区间重载 sort(a, from, to)：区间外元素不受影响，区间内正确有序。
     */
    @Test
    void objectRangeSortKeepsOutsideUntouched() {
        Random rnd = newRandom();
        Integer[] original = new Integer[50];
        for (int i = 0; i < 20; i++) {
            original[i] = 1000 + rnd.nextInt(100);
        }
        for (int i = 20; i < 40; i++) {
            original[i] = rnd.nextInt(100);
        }
        for (int i = 40; i < 50; i++) {
            original[i] = -1000 - rnd.nextInt(100);
        }

        Integer[] a = original.clone();
        Sort.sort(a, 20, 40);
        assertArrayEquals(Arrays.copyOfRange(original, 0, 20), Arrays.copyOfRange(a, 0, 20), "前缀被改动");
        assertArrayEquals(Arrays.copyOfRange(original, 40, 50), Arrays.copyOfRange(a, 40, 50), "后缀被改动");
        Integer[] expectedMid = Arrays.copyOfRange(original, 20, 40);
        Arrays.sort(expectedMid);
        assertArrayEquals(expectedMid, Arrays.copyOfRange(a, 20, 40), "区间内未正确排序");
    }

    // ==================== 6. 浮点特殊值与 char 无符号序 ====================

    /**
     * float 特殊值全序：NaN 全部在末尾、-0.0 在 +0.0 之前（Float.compare 相邻遍历验证）。
     */
    @Test
    void floatSpecialValueOrder() {
        float[] a = {
                0.0f, -0.0f, Float.NaN, 5.5f, -3.2f, Float.NaN, -0.0f, 0.0f,
                Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, 0.0f, -0.0f, Float.NaN, -0.0f
        };
        Random rnd = newRandom();
        float[] big = new float[300];
        for (int i = 0; i < big.length; i++) {
            float v = rnd.nextFloat() * 200 - 100;
            if (rnd.nextInt(8) == 0) {
                v = Float.NaN;
            } else if (rnd.nextInt(8) == 0) {
                v = rnd.nextBoolean() ? -0.0f : 0.0f;
            }
            big[i] = v;
        }
        check(a);
        check(big);
        assertTotalOrder(a);
        assertTotalOrder(big);
        assertNaNAtTail(a);
        assertNaNAtTail(big);
        assertSignedZeroOrder(a);
        assertSignedZeroOrder(big);
    }

    /**
     * double 特殊值全序：NaN 全部在末尾、-0.0 在 +0.0 之前（Double.compare 相邻遍历验证）。
     */
    @Test
    void doubleSpecialValueOrder() {
        double[] a = {
                0.0, -0.0, Double.NaN, 5.5, -3.2, Double.NaN, -0.0, 0.0,
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 0.0, -0.0, Double.NaN, -0.0
        };
        Random rnd = newRandom();
        double[] big = new double[300];
        for (int i = 0; i < big.length; i++) {
            double v = rnd.nextDouble() * 200 - 100;
            if (rnd.nextInt(8) == 0) {
                v = Double.NaN;
            } else if (rnd.nextInt(8) == 0) {
                v = rnd.nextBoolean() ? -0.0 : 0.0;
            }
            big[i] = v;
        }
        check(a);
        check(big);
        assertTotalOrder(a);
        assertTotalOrder(big);
        assertNaNAtTail(a);
        assertNaNAtTail(big);
        assertSignedZeroOrder(a);
        assertSignedZeroOrder(big);
    }

    /**
     * char 无符号 16 位序：含 '\u0000' 与 '\uFFFF' 边界值，排序后非降序且极值落位正确。
     */
    @Test
    void charUnsignedOrderWithExtremes() {
        Random rnd = newRandom();
        char[] a = new char[500];
        for (int i = 0; i < a.length; i++) {
            a[i] = (char) rnd.nextInt(65536);
        }
        a[0] = '\u0000';
        a[1] = '\uFFFF';
        a[2] = '\u0000';
        a[3] = '\uFFFF';
        a[4] = '\u8000';
        a[5] = '\u7FFF';
        check(a);
        for (int i = 0; i + 1 < a.length; i++) {
            assertTrue(a[i] <= a[i + 1], "位置 " + i + " 违反无符号非降序");
        }
        assertEquals('\u0000', a[0]);
        assertEquals('\uFFFF', a[a.length - 1]);
    }

    // ==================== 7. 异常契约 ====================

    /**
     * null 数组 / null List 一律抛 NullPointerException（覆盖全部 7 种原始类型与对象重载）。
     */
    @Test
    void nullArgumentThrowsNpe() {
        assertThrows(NullPointerException.class, () -> Sort.sort((byte[]) null));
        assertThrows(NullPointerException.class, () -> Sort.sort((short[]) null));
        assertThrows(NullPointerException.class, () -> Sort.sort((int[]) null));
        assertThrows(NullPointerException.class, () -> Sort.sort((long[]) null));
        assertThrows(NullPointerException.class, () -> Sort.sort((float[]) null));
        assertThrows(NullPointerException.class, () -> Sort.sort((double[]) null));
        assertThrows(NullPointerException.class, () -> Sort.sort((char[]) null));
        assertThrows(NullPointerException.class, () -> Sort.sort((int[]) null, 0, 5));
        assertThrows(NullPointerException.class, () -> Sort.sort((Integer[]) null));
        assertThrows(NullPointerException.class, () -> Sort.sort((Integer[]) null, 0));
        assertThrows(NullPointerException.class, () -> Sort.sort((Integer[]) null, 0, 1));
        assertThrows(NullPointerException.class, () -> Sort.sort((Integer[]) null, Comparator.naturalOrder()));
        assertThrows(NullPointerException.class, () -> Sort.sort((List<Integer>) null));
        assertThrows(NullPointerException.class, () -> Sort.sort((List<Integer>) null, Comparator.naturalOrder()));
    }

    /**
     * 非法索引（fromIndex &lt; 0、toIndex &gt; length、fromIndex &gt; toIndex）抛 IndexOutOfBoundsException；
     * 边界 from == to / from == length 为合法空操作。
     */
    @Test
    void illegalIndexThrowsIoobe() {
        int[] a = new int[10];
        assertThrows(IndexOutOfBoundsException.class, () -> Sort.sort(a, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> Sort.sort(a, -1, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> Sort.sort(a, 0, 11));
        assertThrows(IndexOutOfBoundsException.class, () -> Sort.sort(a, 5, 4));
        assertThrows(IndexOutOfBoundsException.class, () -> Sort.sort(a, 11));
        assertThrows(IndexOutOfBoundsException.class, () -> Sort.sort(new Integer[10], 0, 11));
        assertThrows(IndexOutOfBoundsException.class, () -> Sort.sort(new Integer[10], 5, 4));
        assertDoesNotThrow(() -> Sort.sort(a, 5, 5));
        assertDoesNotThrow(() -> Sort.sort(a, 10, 10));
        assertDoesNotThrow(() -> Sort.sort(a, 10));
    }

    /**
     * Comparable 路径 null 元素契约：区间内含 null 抛 NPE，区间外 null 不影响排序。
     */
    @Test
    void comparableNullElementContract() {
        Integer[] a = {5, null, 5};
        assertThrows(NullPointerException.class, () -> Sort.sort(a.clone()));
        assertThrows(NullPointerException.class, () -> Sort.sort(a.clone(), 0));
        assertThrows(NullPointerException.class, () -> Sort.sort(a.clone(), 0, 3));
        assertThrows(NullPointerException.class, () -> Sort.sort(a.clone(), 0, 2));
        assertDoesNotThrow(() -> Sort.sort(a.clone(), 0, 1));
        assertDoesNotThrow(() -> Sort.sort(a.clone(), 2, 3));
        assertDoesNotThrow(() -> Sort.sort(a.clone(), 2));
        Integer[] b = a.clone();
        Sort.sort(b, 0, 1);
        assertArrayEquals(new Integer[]{5, null, 5}, b, "区间外的 null 元素不应被触碰");
    }

    /**
     * Comparator 为 null 时各重载抛 NullPointerException。
     */
    @Test
    void nullComparatorThrowsNpe() {
        Integer[] a = {3, 1, 2};
        assertThrows(NullPointerException.class, () -> Sort.sort(a, (Comparator<Integer>) null));
        assertThrows(NullPointerException.class, () -> Sort.sort(a, 0, (Comparator<Integer>) null));
        assertThrows(NullPointerException.class, () -> Sort.sort(a, 0, a.length, (Comparator<Integer>) null));
        List<Integer> list = new ArrayList<>(Arrays.asList(3, 1, 2));
        assertThrows(NullPointerException.class, () -> Sort.sort(list, null));
    }

    // ==================== 数据构造辅助 ====================

    private static Random newRandom() {
        return new Random(SEED);
    }

    /** 数值型标准 8 形态：随机含重复、含负值、已有序、逆序、全等、空、单元素、两元素 */
    private static int[][] standardForms(Random rnd) {
        return new int[][]{
                randomWithDuplicates(120, 10, rnd),
                randomWithNegatives(120, rnd),
                ascending(120),
                descending(120),
                allEqual(120),
                {},
                {42},
                {9, 3}
        };
    }

    /** char 专用 8 形态：以全值域随机（0..65535）替代"含负值"形态 */
    private static int[][] charForms(Random rnd) {
        return new int[][]{
                randomWithDuplicates(120, 10, rnd),
                fullRange(120, rnd),
                ascending(120),
                descending(120),
                allEqual(120),
                {},
                {42},
                {9, 3}
        };
    }

    private static int[] randomWithDuplicates(int n, int bound, Random rnd) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = rnd.nextInt(bound);
        }
        return a;
    }

    private static int[] randomWithNegatives(int n, Random rnd) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = rnd.nextInt(200) - 100;
        }
        return a;
    }

    private static int[] fullRange(int n, Random rnd) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = rnd.nextInt(65536);
        }
        return a;
    }

    private static int[] ascending(int n) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = i;
        }
        return a;
    }

    private static int[] descending(int n) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = n - i;
        }
        return a;
    }

    private static int[] allEqual(int n) {
        int[] a = new int[n];
        Arrays.fill(a, 42);
        return a;
    }

    /** 有序数组随机交换 5 对相邻元素：最多引入 5 个相邻逆序对，远小于 n/8 */
    private static int[] nearlySorted(int n, Random rnd) {
        int[] a = ascending(n);
        for (int k = 0; k < 5; k++) {
            int i = rnd.nextInt(n - 1);
            int t = a[i];
            a[i] = a[i + 1];
            a[i + 1] = t;
        }
        return a;
    }

    private static byte[] toBytes(int[] src) {
        byte[] a = new byte[src.length];
        for (int i = 0; i < src.length; i++) {
            a[i] = (byte) src[i];
        }
        return a;
    }

    private static short[] toShorts(int[] src) {
        short[] a = new short[src.length];
        for (int i = 0; i < src.length; i++) {
            a[i] = (short) src[i];
        }
        return a;
    }

    private static long[] toLongs(int[] src) {
        long[] a = new long[src.length];
        for (int i = 0; i < src.length; i++) {
            a[i] = src[i];
        }
        return a;
    }

    private static float[] toFloats(int[] src) {
        float[] a = new float[src.length];
        for (int i = 0; i < src.length; i++) {
            a[i] = src[i];
        }
        return a;
    }

    private static double[] toDoubles(int[] src) {
        double[] a = new double[src.length];
        for (int i = 0; i < src.length; i++) {
            a[i] = src[i];
        }
        return a;
    }

    private static char[] toChars(int[] src) {
        char[] a = new char[src.length];
        for (int i = 0; i < src.length; i++) {
            a[i] = (char) src[i];
        }
        return a;
    }

    private static Integer[] box(int[] src) {
        Integer[] a = new Integer[src.length];
        for (int i = 0; i < src.length; i++) {
            a[i] = src[i];
        }
        return a;
    }

    private static List<Integer> toIntegerList(int[] src) {
        List<Integer> list = new ArrayList<>();
        for (int v : src) {
            list.add(v);
        }
        return list;
    }

    // ==================== 差分断言辅助 ====================

    private static void check(byte[] a) {
        byte[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a);
        assertArrayEquals(expected, a);
    }

    private static void check(short[] a) {
        short[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a);
        assertArrayEquals(expected, a);
    }

    private static void check(int[] a) {
        int[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a);
        assertArrayEquals(expected, a);
    }

    private static void check(long[] a) {
        long[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a);
        assertArrayEquals(expected, a);
    }

    private static void check(float[] a) {
        float[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a);
        assertArrayEquals(expected, a);
    }

    private static void check(double[] a) {
        double[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a);
        assertArrayEquals(expected, a);
    }

    private static void check(char[] a) {
        char[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a);
        assertArrayEquals(expected, a);
    }

    private static <T extends Comparable<? super T>> void check(T[] a) {
        T[] expected = a.clone();
        Arrays.sort(expected);
        Sort.sort(a);
        assertArrayEquals(expected, a);
    }

    private static <T extends Comparable<? super T>> void check(List<T> list) {
        List<T> expected = new ArrayList<>(list);
        Collections.sort(expected);
        Sort.sort(list);
        assertEquals(expected, list);
    }

    // ==================== 浮点全序断言辅助 ====================

    /** 相邻遍历：Float.compare 全序非降（保证 NaN 视为最大、-0.0 小于 +0.0） */
    private static void assertTotalOrder(float[] a) {
        for (int i = 0; i + 1 < a.length; i++) {
            assertTrue(Float.compare(a[i], a[i + 1]) <= 0,
                    "位置 " + i + " 违反 Float.compare 全序: " + Arrays.toString(a));
        }
    }

    /** 相邻遍历：Double.compare 全序非降（保证 NaN 视为最大、-0.0 小于 +0.0） */
    private static void assertTotalOrder(double[] a) {
        for (int i = 0; i + 1 < a.length; i++) {
            assertTrue(Double.compare(a[i], a[i + 1]) <= 0,
                    "位置 " + i + " 违反 Double.compare 全序: " + Arrays.toString(a));
        }
    }

    /** NaN 必须全部位于数组末尾：一旦出现 NaN，其后不得再有非 NaN 元素 */
    private static void assertNaNAtTail(float[] a) {
        boolean nanSeen = false;
        for (int i = 0; i < a.length; i++) {
            if (Float.isNaN(a[i])) {
                nanSeen = true;
            } else {
                assertFalse(nanSeen, "位置 " + i + "：NaN 之后仍有非 NaN 元素");
            }
        }
    }

    /** NaN 必须全部位于数组末尾：一旦出现 NaN，其后不得再有非 NaN 元素 */
    private static void assertNaNAtTail(double[] a) {
        boolean nanSeen = false;
        for (int i = 0; i < a.length; i++) {
            if (Double.isNaN(a[i])) {
                nanSeen = true;
            } else {
                assertFalse(nanSeen, "位置 " + i + "：NaN 之后仍有非 NaN 元素");
            }
        }
    }

    /** 若同时存在 -0.0 与 +0.0，则最后一个 -0.0 必须位于第一个 +0.0 之前（按原始位模式区分） */
    private static void assertSignedZeroOrder(float[] a) {
        int lastNegZero = -1;
        int firstPosZero = -1;
        for (int i = 0; i < a.length; i++) {
            if (a[i] == 0.0f) {
                if (Float.floatToRawIntBits(a[i]) < 0) {
                    lastNegZero = i;
                } else if (firstPosZero < 0) {
                    firstPosZero = i;
                }
            }
        }
        if (lastNegZero >= 0 && firstPosZero >= 0) {
            assertTrue(lastNegZero < firstPosZero, "-0.0 必须全部位于 +0.0 之前");
        }
    }

    /** 若同时存在 -0.0 与 +0.0，则最后一个 -0.0 必须位于第一个 +0.0 之前（按原始位模式区分） */
    private static void assertSignedZeroOrder(double[] a) {
        int lastNegZero = -1;
        int firstPosZero = -1;
        for (int i = 0; i < a.length; i++) {
            if (a[i] == 0.0) {
                if (Double.doubleToRawLongBits(a[i]) < 0) {
                    lastNegZero = i;
                } else if (firstPosZero < 0) {
                    firstPosZero = i;
                }
            }
        }
        if (lastNegZero >= 0 && firstPosZero >= 0) {
            assertTrue(lastNegZero < firstPosZero, "-0.0 必须全部位于 +0.0 之前");
        }
    }
}
