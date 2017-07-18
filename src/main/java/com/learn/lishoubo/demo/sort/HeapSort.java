package com.learn.lishoubo.demo.sort;

/**
 * Created by lishoubo on 17/7/17.
 */
public class HeapSort {

    public void sort(int[] array) {
        doHeapSort(array);
    }

    private void doHeapSort(int[] array) {
        buildHeap(array);
        printHeap(array);

        for (int index = array.length - 1; index > 0; index--) {
            swap(array, 0, index);
            adjustHeap(array, 0, index);
        }

    }

    private void buildHeap(int[] array) {
        final int heapIndex = parentIndex(array.length - 1);
        for (int i = heapIndex; i >= 0; i--) {
            adjustHeap(array, i, array.length);
        }
    }

    private void adjustHeap(int[] array, int index, int length) {
        final int left = leftChild(index), right = rightChild(index);
        int max = array[index];
        int maxIndex = index;

        if (left >= length) {
            return;
        }

        if (array[left] > max) {
            max = array[left];
            maxIndex = left;
        }

        if (right < length && array[right] > max) {
            maxIndex = right;
        }

        if (maxIndex != index) {
            swap(array, index, maxIndex);
            adjustHeap(array, maxIndex, length);
        }
    }

    private void swap(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    private int parentIndex(int index) {
        return (index + 1) / 2 - 1;
    }

    private int leftChild(int index) {
        return 2 * index + 1;
    }

    private int rightChild(int index) {
        return 2 * index + 2;
    }


    private static void printHeap(int[] array) {
        final int heapDepth = heapDepth(array);
        for (int depth = 0; depth <= heapDepth; depth++) {
            printHeapLevel(array, depth, heapDepth);
        }
    }

    private static void printHeapLevel(int[] array, int depth, int heapDepth) {
        System.out.println();
        for (int blank = 0; blank < (heapDepth - depth); blank++) {
            System.out.print(" ");
        }
        for (int start = (int) (Math.pow(2, depth) - 1), size = 0; size < Math.pow(2, depth) && start < array.length; start++, size++) {
            System.out.print(array[start] + " ");
        }
        System.out.println();

    }

    private static int heapDepth(int[] array) {
        if (array.length == 0) {
            return -1;
        }

        int depth = 0, index = 0;
        while (true) {
            if ((2 * index + 1) < array.length) {
                depth++;
                index = 2 * index + 1;
                continue;
            }
            break;
        }
        return depth;
    }

    public static void main(String[] args) {
        int[] array = {4, 1, 6, 7, 9, 8, 2, 8};
        new HeapSort().sort(array);
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
