package com.example.weighttracker.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.weighttracker.model.WeightEntry;
import com.example.weighttracker.data.WeightRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeightViewModel extends AndroidViewModel {

    private final WeightRepository repository;
    private final ExecutorService executorService;

    public WeightViewModel(@NonNull Application application) {
        super(application);
        repository = new WeightRepository(application);
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<WeightEntry>> getWeightEntries(long userId) {
        return repository.getWeightsForUser(userId);
    }

    public void addWeight(WeightEntry entry) {
        executorService.execute(() -> {
            repository.insertWeight(entry);
        });
    }

    public void deleteWeight(long weightId, long userId) {
        executorService.execute(() -> {
            repository.removeWeight(weightId, userId);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
