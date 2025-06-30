package com.example.weighttracker.data;

import android.content.Context;
import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.weighttracker.model.WeightEntry;

import java.util.ArrayList;
import java.util.List;

public class WeightRepository {

    private final DatabaseHelper dbHelper;
    private final MutableLiveData<List<WeightEntry>> weightEntriesLiveData = new MutableLiveData<>();

    public WeightRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
        weightEntriesLiveData.setValue(new ArrayList<>());
    }

    // Load weights from DB and expose as LiveData
    public LiveData<List<WeightEntry>> getWeightsForUser(long userId) {
        loadWeightsFromDb(userId);
        return weightEntriesLiveData;
    }

    // Load weights from the database into LiveData
    private void loadWeightsFromDb(long userId) {
        List<WeightEntry> weights = new ArrayList<>();
        Cursor cursor = dbHelper.getWeightsByUser(userId);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_WEIGHT_ID));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE));
                double weight = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_WEIGHT));

                int goalIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_GOAL);
                Double goal = null;
                if (goalIndex != -1 && !cursor.isNull(goalIndex)) {
                    goal = cursor.getDouble(goalIndex);
                }

                WeightEntry entry = new WeightEntry(userId, date, weight, goal);
                weights.add(entry);
            } while (cursor.moveToNext());
            cursor.close();
        }

        weightEntriesLiveData.postValue(weights);
    }

    // Insert a new weight entry, then reload weights for the user
    public void insertWeight(WeightEntry entry) {
        dbHelper.addWeight(entry.getUserId(), entry.getDate(), entry.getWeight(), entry.getGoal());
        loadWeightsFromDb(entry.getUserId());
    }

    // Remove a weight entry by ID, then reload weights for the user
    public void removeWeight(long weightId, long userId) {
        dbHelper.deleteWeight(weightId);
        loadWeightsFromDb(userId);
    }

    // Delete a user and all their weights
    public boolean deleteUserAndWeights(long userId) {
        return dbHelper.deleteUserAndWeights(userId);
    }

    public void close() {
        dbHelper.close();
    }
}
