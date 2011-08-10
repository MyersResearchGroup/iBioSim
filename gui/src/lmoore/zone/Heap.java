/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lmoore.zone;

import platu.stategraph.UpdateTimer;

/**
 *
 * @author ldmtwo
 */
public class Heap {

    private Comparable[] data;
    private int heapSize;

    public Heap(int size) {
        data = new Comparable[size];
        heapSize = 0;
    }

    public Comparable getMinimum() {
        if (isEmpty()) {
            throw new HeapException("Heap is empty");
        } else {
            return data[0];
        }
    }

    public boolean isEmpty() {
        return (heapSize == 0);
    }

    private int getLeftChildIndex(int nodeIndex) {
        return 2 * nodeIndex + 1;
    }

    private int getRightChildIndex(int nodeIndex) {
        return 2 * nodeIndex + 2;
    }

    private int getParentIndex(int nodeIndex) {
        return (nodeIndex - 1) / 2;
    }

    public class HeapException extends RuntimeException {

        public HeapException(String message) {
            super(message);
        }
    }

    public void insert(int value) {
        if (heapSize == data.length) {
            throw new HeapException("Heap's underlying storage is overflow");
        } else {
            heapSize++;
            data[heapSize - 1] = value;
            siftUp(heapSize - 1);
        }
    }

    public int size() {
        return heapSize;
    }

    private void siftUp(int nodeIndex) {
        int parentIndex;
        Comparable tmp;
        if (nodeIndex != 0) {
            parentIndex = getParentIndex(nodeIndex);
            if (data[parentIndex].compareTo(data[nodeIndex]) > 0) {
                tmp = data[parentIndex];
                data[parentIndex] = data[nodeIndex];
                data[nodeIndex] = tmp;
                siftUp(parentIndex);
            }
        }
    }

    public static void main(String[] arg) {
        UpdateTimer timer = new UpdateTimer(1000) {

            @Override
            public int getNumStates() {
                return 0;
            }

            @Override
            public int getStackHeight() {
                return 0;
            }

            @Override
            public String getLabel() {
                return "";
            }
        };
        timer.start();
        Heap cache = new Heap((int) Math.pow(2, 16));
        int[] data = {1, 2, 2, 2, 4, 4, 6, 7, 8, 9, 1, 6, 7, 8, 2, 3, 4, 5, 6};
        for (int o : data) {
            cache.insert(o);
            System.out.printf("%s\t%s\t%s\n", o, "", cache.size());
        }
        timer.print();

    }
}
