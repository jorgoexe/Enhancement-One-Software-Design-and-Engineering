package com.example.weighttracker.ui.main;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weighttracker.R;
import com.example.weighttracker.adapter.WeightAdapter;
import com.example.weighttracker.model.WeightEntry;
import com.example.weighttracker.viewmodel.WeightViewModel;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private MainViewModel weightViewModel;
    private WeightAdapter weightAdapter;
    private long currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentUserId = getIntent().getLongExtra("USER_ID", -1);
        if (currentUserId == -1) {
            Toast.makeText(this, "Error: User ID not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        EditText etDate = findViewById(R.id.et_date);
        EditText etWeight = findViewById(R.id.et_weight);
        EditText etGoal = findViewById(R.id.et_goal);
        Button btnAdd = findViewById(R.id.btn_add);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        weightAdapter = new WeightAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(weightAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize ViewModel
        weightViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Observe weight entries for current user
        weightViewModel.getWeightEntries(currentUserId).observe(this, entries -> {
            weightAdapter.setWeightList(entries);
        });

        btnAdd.setOnClickListener(v -> {
            String date = etDate.getText().toString().trim();
            String weightStr = etWeight.getText().toString().trim();
            String goalStr = etGoal.getText().toString().trim();

            if (date.isEmpty() || weightStr.isEmpty()) {
                Toast.makeText(this, "Date and weight are required.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double weight = Double.parseDouble(weightStr);
                Double goal = goalStr.isEmpty() ? null : Double.parseDouble(goalStr);

                // Add the new weight entry
                WeightEntry newEntry = new WeightEntry(currentUserId, date, weight, goal);
                weightViewModel.addWeight(
                        newEntry.getUserId(),
                        newEntry.getDate(),
                        newEntry.getWeight(),
                        newEntry.getGoal(),
                        null // No callback needed here unless you want one
                );

                // Clear input fields
                etDate.setText("");
                etWeight.setText("");
                etGoal.setText("");

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number format.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
