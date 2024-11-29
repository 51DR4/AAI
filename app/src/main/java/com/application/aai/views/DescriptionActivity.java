/*
 * Copyright (c) 2024 ABB-03
 * Licensed under the MIT License. See the LICENSE file for details.
 *
 */

package com.application.aai.views;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.application.aai.processing.requests.RequestManager;
import com.example.aai.R;

public class DescriptionActivity extends AppCompatActivity {
    private EditText descriptionTextview;
    private RequestManager requestManager;
    private String lastText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_text);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.text), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        descriptionTextview = findViewById(R.id.transcriptionTextView);
        requestManager = RequestManager.getInstance();
    }

    public void onAnalyseClicked(View view) {
        String text = String.valueOf(descriptionTextview.getText()).strip();
        if (text.isEmpty()) Toast.makeText(this, "Nothing to analyse", Toast.LENGTH_SHORT).show();
        else {
            Intent analysisIntent = new Intent(this, AnalysisActivity.class);
            requestManager.setInput(text);
            requestManager.setInput(null, null);
            startActivity(analysisIntent);
        }
    }

    public void clearClick(View v) {
        descriptionTextview.setText("");
    }

    public void sumClick(View view) {
        lastText = String.valueOf(descriptionTextview.getText()).strip();
        if (lastText.isEmpty()) Toast.makeText(this, "No text to summarize", Toast.LENGTH_SHORT).show();
        else {
            requestManager.setObserver(this);
            requestManager.sendSummarizeRequest(lastText, () -> {
                descriptionTextview.setText(requestManager.getSummary());
            });
        }
    }

    public void copyText(View view) {
        String textToCopy = descriptionTextview.getText().toString();

        if (!textToCopy.isEmpty()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("", textToCopy);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No text to copy", Toast.LENGTH_SHORT).show();
        }
    }
}