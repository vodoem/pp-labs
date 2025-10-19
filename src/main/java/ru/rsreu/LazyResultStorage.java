package ru.rsreu;

import java.util.ArrayList;
import java.util.List;

public class LazyResultStorage {
    private List<Double> partialResults;

    public synchronized List<Double> getStorage() {
        if (partialResults == null) {
            System.out.println("Создано хранилище результатов для задачи");
            partialResults = new ArrayList<>();
        }
        return partialResults;
    }

    public synchronized void addResult(double result) {
        getStorage().add(result);
    }
}
