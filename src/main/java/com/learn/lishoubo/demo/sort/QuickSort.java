package com.learn.lishoubo.demo.sort;

/**
 * Created by lishoubo on 17/7/17.
 */
public class QuickSort {

    public void sort(int[] array) {
        doQuickSort(array, 0, array.length - 1);
    }

    private void doQuickSort(int[] array, final int left, final int right) {
        if (right <= left) {
            return;
        }

        int seed = array[left];
        int i = left, j = right;

        while (i != j) {
            for (; j > i && array[j] >= seed; j--) {

            }
            if (j > i) {
                array[i++] = array[j];
            }

            for (; i < j && array[i] < seed; i++) {

            }
            if (i < j) {
                array[j--] = array[i];
            }
        }
        array[i] = seed;

        doQuickSort(array, left, i - 1);
        doQuickSort(array, i + 1, right);
    }

    public static void main(String[] args) {
        int[] array = new int[]{1, 1, 5, 2};

        new QuickSort().sort(array);
        print(array);
    }

    private static void print(int[] array) {
        System.out.println();
        for (int i = 0; i < array.length; i++) {
            System.out.print(" " + String.valueOf(array[i]));
        }
        System.out.println();
    }

}
