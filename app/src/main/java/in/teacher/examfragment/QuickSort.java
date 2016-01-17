package in.teacher.examfragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vinkrish on 17/01/16.
 */
public class QuickSort {
    private float[] numbers;
    private int number;

    public List<Float> sort(List<Float> marksList) {
        this.numbers = convertIntegers(marksList);

        number = numbers.length;
        quicksort(0, number - 1);

        List<Float> intList = new ArrayList<>(numbers.length);
        for (int index = 0; index < numbers.length; index++)
            intList.add(numbers[index]);

        return intList;
    }

    private void quicksort(int low, int high) {
        int i = low, j = high;
        // Get the pivot element from the middle of the list
        float pivot = numbers[low + (high - low) / 2];

        // Divide into two lists
        while (i <= j) {
            // If the current value from the left list is smaller then the pivot
            // element then get the next element from the left list
            while (numbers[i] < pivot) {
                i++;
            }
            // If the current value from the right list is larger then the pivot
            // element then get the next element from the right list
            while (numbers[j] > pivot) {
                j--;
            }

            // If we have found a values in the left list which is larger then
            // the pivot element and if we have found a value in the right list
            // which is smaller then the pivot element then we exchange the
            // values.
            // As we are done we can increase i and j
            if (i <= j) {
                exchange(i, j);
                i++;
                j--;
            }
        }
        // Recursion
        if (low < j)
            quicksort(low, j);
        if (i < high)
            quicksort(i, high);
    }

    private void exchange(int i, int j) {
        float temp = numbers[i];
        numbers[i] = numbers[j];
        numbers[j] = temp;
    }

    public float[] convertIntegers(List<Float> integers) {
        float[] ret = new float[integers.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = integers.get(i).intValue();
        }
        return ret;
    }

    private int[] buildIntArray(List<Integer> integers) {
        int[] ints = new int[integers.size()];
        int i = 0;
        for (Integer n : integers) {
            ints[i++] = n;
        }
        return ints;
    }
}
