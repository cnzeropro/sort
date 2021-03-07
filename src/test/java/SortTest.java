import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class SortTest {
    private Double[] array;
    private long start, end;

    private void initFormFile() {
        String numStr = null;
        try {
            numStr = FileUtils.readFileToString(
                    FileUtils.toFile(
                            this.getClass().getClassLoader().getResource("array.txt")),
                    "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        array = Arrays.stream(numStr.strip().split(","))
                .map(Double::valueOf)
                .toArray(Double[]::new);
    }

    private Integer[] initFormRandom(int count, int bound) {
        Integer[] a = new Integer[count];
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            a[i] = random.nextInt(bound);
        }
        return a;
    }

    @Test
    public void bubble() {
        Integer[] array = initFormRandom(8, 101);
        System.out.println("冒泡排序前:" + Arrays.toString(array));
        start = System.nanoTime();
        Sort.Bubble.sort(array);
        end = System.nanoTime();
        System.out.println("冒泡排序后:" + Arrays.toString(array));
        System.out.println("所用时间:" + (end - start) + "纳秒");
    }

    @Test
    public void selection() {
        initFormFile();
        System.out.println("选择排序前:" + Arrays.toString(array));
        start = System.nanoTime();
        Sort.Selection.sort(array);
        end = System.nanoTime();
        System.out.println("选择排序后:" + Arrays.toString(array));
        System.out.println("所用时间:" + (end - start) + "纳秒");
    }

    @Test
    public void insertion() {
        initFormFile();
        System.out.println("插入排序前:" + Arrays.toString(array));
        start = System.nanoTime();
        Sort.Insertion.sort(array);
        end = System.nanoTime();
        System.out.println("插入排序后:" + Arrays.toString(array));
        System.out.println("所用时间:" + (end - start) + "纳秒");
    }

    @Test
    public void shell() {
        initFormFile();
        System.out.println("普通增量序列的希尔排序前:" + Arrays.toString(array));
        start = System.nanoTime();
        Sort.Shell.generalSort(array, 0, array.length - 1);
        end = System.nanoTime();
        System.out.println("普通增量序列的希尔排序后:" + Arrays.toString(array));
        System.out.println("所用时间:" + (end - start) + "纳秒");

        System.out.println();

        initFormFile();
        System.out.println("严谨增量序列的希尔排序前:" + Arrays.toString(array));
        start = System.nanoTime();
        Sort.Shell.sort(array);
        end = System.nanoTime();
        System.out.println("严谨增量序列的希尔排序后:" + Arrays.toString(array));
        System.out.println("所用时间:" + (end - start) + "纳秒");
    }

    @Test
    public void merge() {
        initFormFile();
        System.out.println("归并排序前:" + Arrays.toString(array));
        start = System.nanoTime();
        Sort.Merge.sort(array);
        end = System.nanoTime();
        System.out.println("归并排序后:" + Arrays.toString(array));
        System.out.println("所用时间:" + (end - start) + "纳秒");
    }

    @Test
    public void quick() {
        initFormFile();
        System.out.println("快速排序前:" + Arrays.toString(array));
        start = System.nanoTime();
        Sort.Quick.sort(array);
        end = System.nanoTime();
        System.out.println("快速排序后:" + Arrays.toString(array));
        System.out.println("所用时间:" + (end - start) + "纳秒");
    }
}