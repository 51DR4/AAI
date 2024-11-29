/*
 * Copyright (c) 2024 ABB-03
 * Licensed under the MIT License. See the LICENSE file for details.
 *
 */

package com.application.aai.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.application.aai.processing.database.DataBaseHelper;
import com.application.aai.processing.database.DataModel;
import com.application.aai.processing.database.DataModelAdapter;
import com.example.aai.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.history), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        List<DataModel> dataList;
        try (DataBaseHelper dataBaseHelper = new DataBaseHelper(this)) {
            dataList = dataBaseHelper.getEveryone();

        } catch (Exception e) {
            Toast.makeText(this, "Could not access history. Please try again later.", Toast.LENGTH_SHORT).show();
            return;
        }
        DataModelAdapter adapter = new DataModelAdapter(dataList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setSelectedItemId(R.id.navigation_history);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_history) {
                return true;
            } else if (itemId == R.id.navigation_home) {
                Intent intent = new Intent(HistoryActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }
}
