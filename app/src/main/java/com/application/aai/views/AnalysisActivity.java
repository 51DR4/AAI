/*
 * Copyright (c) 2024 ABB-03
 * Licensed under the MIT License. See the LICENSE file for details.
 *
 */

package com.application.aai.views;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.application.aai.processing.requests.Encoder;
import com.application.aai.api.PlantUMLNetworkManager;
import com.application.aai.processing.database.DataBaseHelper;
import com.application.aai.processing.database.DataModel;
import com.application.aai.processing.pdf.PDFUtil;
import com.application.aai.processing.requests.HtmlParser;
import com.application.aai.processing.requests.OutputCallback;
import com.application.aai.processing.requests.RequestManager;
import com.example.aai.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AnalysisActivity extends AppCompatActivity {

    private final static int OUTPUT_TYPES = 2;
    private final static int DIAGRAM_TYPES = 8;

    private Drawable mImage;
    private final ExecutorService myExecutor = Executors.newSingleThreadExecutor();
    private final Handler myHandler = new Handler(Looper.getMainLooper());

    private AppCompatActivity context;
    private final Drawable[][] images = new Drawable[OUTPUT_TYPES][DIAGRAM_TYPES];
    private final boolean[] loadingRatings = new boolean[OUTPUT_TYPES];

    private Spinner diagramTypeSpinner;
    int outputType = 0;

    private int lastSelectedType = 0;
    private int lastSelectedDiagram = 0;

    private RequestManager requestManager;
    private SwitchCompat switchBtn;
    private ImageView imageView;

    public static List<String> selectedAspects = new ArrayList<>();

    private DrawerLayout drawerLayout;
    private Button applyButton;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;
    private ListView menuListView;
    private ArrayList<String> menuItems;
    private TextView countdown;
    private ConstraintLayout textScroll, imageScroll;
    private boolean returnedHome = false;
    private Drawable thumb_code;
    private Drawable track_image;
    private Drawable thumb_desc;
    private Drawable track_chart;
    private Drawable thumb_image;
    private Drawable track_code;
    private Drawable thumb_chart;
    private Drawable track_desc;
    private TextView leftTextView;
    private TextView rightTextView;
    private TextView descTextView;
    private View rootLayout;
    private Boolean savedInDB;
    private int dbID;
    private Drawable errorDrawable;

    String[][] outputs = new String[OUTPUT_TYPES][DIAGRAM_TYPES];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_analysis);
        rootLayout = findViewById(R.id.analysis);
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        context = this;

        findViews();

        setUpToolbar();

        setupSpinnerAndScrollview();

        emptyTextView();

        setUpSwitchBtn();

        Intent intent = getIntent();
        savedInDB = intent.getBooleanExtra("FROM_DATABASE", false);
        dbID = intent.getIntExtra("DATABASE_ID", -2);

        requestManager = RequestManager.getInstance();
        if (savedInDB) getResultsFromDB();
        else setupRequestManager();

        fillImagesWithLoading();

        errorDrawable = ContextCompat.getDrawable(context, R.drawable.diagram_error);
    }

    public void fillImagesWithLoading() {
        for (int i = 0; i < OUTPUT_TYPES; i++) {
            for (int j = 0; j < DIAGRAM_TYPES; j++) {
                images[i][j] = AppCompatResources.getDrawable(this, R.drawable.loading_image);
            }
        }
        loadingRatings[0] = true;
        loadingRatings[1] = true;
    }

    private void getResultsFromDB() {
        List<DataModel> dataModels;
        try (DataBaseHelper dataBaseHelper = new DataBaseHelper(this)) {
            dataModels = dataBaseHelper.getEveryone();
        }

        for (DataModel dataModel : dataModels) {
            if (dataModel.getId() != dbID) continue;
            String imagePath = dataModel.getImagePath();
            if (imagePath == null) {
                requestManager.setInput(dataModel.getTranscriptionText());
            }
            else {
                Bitmap input = BitmapFactory.decodeFile(imagePath);
                requestManager.setInput(input, imagePath);
            }
            for (int i = 0; i < OUTPUT_TYPES; i++) {
                for (int j = 0; j < DIAGRAM_TYPES; j++) {
                    outputs[i][j] = getResultText(dataModel, i, j);
                    // Update the UI
                    updateTextOutput(i, j);
                    if (i == 0 && j == 0) {
                        displayRating(dataModel.getIntArray1(), 0);
                        displayRating(dataModel.getIntArray2(), 1);
                        continue;
                    } else if (i == 1 && j == 0) continue;
                    String url = Encoder.generateURL(outputs[i][j]);
                    int finalI = i;
                    int finalJ = j;
                    myExecutor.execute(() -> {
                        mImage = PlantUMLNetworkManager.mLoad(context, url, () -> {
                            images[finalI][finalJ] = errorDrawable;
                        }) ;
                        myHandler.post(() -> {
                            if (mImage == null) images[finalI][finalJ] = errorDrawable;
                            else images[finalI][finalJ] = mImage;
                            updateImageOutput(finalI, finalJ);
                        });
                    });
                }
            }
            return;
        }
    }

    private String getResultText(DataModel dataModel, int i, int j) {
        switch (i) {
            case 0:
                switch (j) {
                    case 0:
                        return dataModel.getResultText00();
                    case 1:
                        return dataModel.getResultText01();
                    case 2:
                        return dataModel.getResultText02();
                    case 3:
                        return dataModel.getResultText03();
                    case 4:
                        return dataModel.getResultText04();
                    case 5:
                        return dataModel.getResultText05();
                    case 6:
                        return dataModel.getResultText06();
                    case 7:
                        return dataModel.getResultText07();
                }
            case 1:
                switch (j) {
                    case 0:
                        return dataModel.getResultText10();
                    case 1:
                        return dataModel.getResultText11();
                    case 2:
                        return dataModel.getResultText12();
                    case 3:
                        return dataModel.getResultText13();
                    case 4:
                        return dataModel.getResultText14();
                    case 5:
                        return dataModel.getResultText15();
                    case 6:
                        return dataModel.getResultText16();
                    case 7:
                        return dataModel.getResultText17();
                }
        }
        return null;
    }

    private void findViews() {
        textScroll = findViewById(R.id.left_layout);
        imageScroll = findViewById(R.id.right_layout);
        drawerLayout = findViewById(R.id.drawer_layout);
        applyButton = findViewById(R.id.apply_button);
        toolbar = findViewById(R.id.toolbar);
        countdown = findViewById(R.id.countDown);
        leftTextView = findViewById(R.id.left_textview);
        rightTextView = findViewById(R.id.right_textview);
        descTextView = findViewById(R.id.resultTxt);
        diagramTypeSpinner = findViewById(R.id.output_format_popup);
        switchBtn = findViewById(R.id.switchBtn);
        imageView = findViewById(R.id.diagram_image);
        menuListView = findViewById(R.id.menu_listview);
    }

    private void setUpToolbar() {
        // Initialize menu items list
        menuItems = new ArrayList<>();
        menuItems.add("Energy Efficiency");
        menuItems.add("Sustainability");
        menuItems.add("Modularity");
        menuItems.add("Scalability");
        menuItems.add("Security");
        menuItems.add("Maintainability");
        menuItems.add("Performance");
        menuItems.add("Portability");
        menuItems.add("Usability");
        menuItems.add("Cost Efficiency");
        menuItems.add("Flexibility");
        menuItems.add("Reliability");

        ArrayAdapter<String> menuAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, menuItems);
        menuListView.setAdapter(menuAdapter);

        // Set items as checkable
        menuListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        for (int i = 0; i < 6; i++) {
            menuListView.setItemChecked(i, true);
        }

        setSupportActionBar(toolbar);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        // Set the color of the drawer indicator
        toggle.getDrawerArrowDrawable().setColor(ContextCompat.getColor(this, R.color.color_primary_light));

        applySelectedAspects();

        applyButton.setOnClickListener(v -> {
            applySelectedAspects();
            drawerLayout.closeDrawers();
        });
    }

    private void setUpSwitchBtn() {
        switchBtn.setOnCheckedChangeListener((buttonView, isChecked) -> changeIcons(isChecked));

        // Ensure the default view is showing the analysis text
        switchBtn.setChecked(false);
        textScroll.setVisibility(View.VISIBLE);
        imageScroll.setVisibility(View.GONE);

        thumb_code = ContextCompat.getDrawable(context, R.drawable.thumb_code);
        track_image = ContextCompat.getDrawable(context, R.drawable.track_image);
        thumb_desc = ContextCompat.getDrawable(context, R.drawable.thumb_desc);
        track_chart = ContextCompat.getDrawable(context, R.drawable.track_chart);
        thumb_image = ContextCompat.getDrawable(context, R.drawable.thumb_image);
        track_code = ContextCompat.getDrawable(context, R.drawable.track_code);
        thumb_chart = ContextCompat.getDrawable(context, R.drawable.thumb_chart);
        track_desc = ContextCompat.getDrawable(context, R.drawable.track_desc);
    }

    @SuppressLint("SetTextI18n")
    private void changeIcons(boolean isChecked) {
        Drawable newThumbDrawable;
        Drawable newTrackDrawable;
        if (!isChecked) {
            imageScroll.setVisibility(View.INVISIBLE);
            textScroll.setVisibility(View.VISIBLE);
            if (lastSelectedDiagram > 0) {
                newThumbDrawable = thumb_code;
                newTrackDrawable = track_image;
                leftTextView.setText("C4/PlantUML Script");
            }
            else {
                newThumbDrawable = thumb_desc;
                newTrackDrawable = track_chart;
                leftTextView.setText("Architecture Description");
            }
            updateTextOutput(lastSelectedType, lastSelectedDiagram);
        } else {
            imageScroll.setVisibility(View.VISIBLE);
            textScroll.setVisibility(View.INVISIBLE);
            if (lastSelectedDiagram > 0) {
                newThumbDrawable = thumb_image;
                newTrackDrawable = track_code;
                rightTextView.setText("C4/PlantUML Diagram");
            }
            else {
                newThumbDrawable = thumb_chart;
                newTrackDrawable = track_desc;
                rightTextView.setText("Architecture Rating");
            }
            updateImageOutput(lastSelectedType, lastSelectedDiagram);
        }
        runOnUiThread(() -> {
            switchBtn.setThumbDrawable(newThumbDrawable);
            switchBtn.setTrackDrawable(newTrackDrawable);
        });
    }


    private void emptyTextView() {
        runOnUiThread(() -> descTextView.setText(HtmlParser.fromHtml("<h3 style='text-align:center'>Loading analysis results...</h3>")));
    }


    private void applySelectedAspects() {
        selectedAspects.clear();

        // Iterate over all items in the menuListView
        for (int i = 0; i < menuListView.getCount(); i++) {
            // Check if the item at the current position is checked
            if (menuListView.isItemChecked(i)) {
                // Add the corresponding item to selectedAspects
                selectedAspects.add(menuItems.get(i));
            }
        }

        // Close the navigation drawer
        drawerLayout.closeDrawers();

        // If requestManager is not null, proceed with the requests
        if (requestManager != null) {
            emptyTextView();
            imageView.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.loading_image));
            requestManager.sendNonDiagramRequests();
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateTextOutput(int i, int j) {
        if (!switchBtn.isChecked()) {
            if (outputType != i || lastSelectedDiagram != j) return;
            String output;
            if (outputs[i][j] == null) output = requestManager.getOutput(outputType, lastSelectedDiagram);
            else output = outputs[i][j];
            if (output == null) return;
            runOnUiThread(() -> descTextView.setText(HtmlParser.fromHtml(output)));
        }
    }

    private void updateImageOutput(int i, int j) {
        if (lastSelectedType != i || lastSelectedDiagram != j) return;
        if (switchBtn.isChecked()) {
            // Get the ScrollView and image for the given indices
            Drawable image = images[i][j];

            // Update the ImageView on the UI thread
            runOnUiThread(() -> imageView.setImageDrawable(image));
        }
    }

    private void setupSpinnerAndScrollview() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.output_popup, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        diagramTypeSpinner.setAdapter(adapter);
        diagramTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (lastSelectedDiagram != position || lastSelectedType != outputType) {
                    // If true then settings changed, so update output
                    lastSelectedType = outputType;
                    lastSelectedDiagram = position;
                    switchBtn.setChecked(false); // Reset switch to show text by default
                    changeIcons(false);
                    updateTextOutput(outputType, position);
                    updateImageOutput(outputType, position);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing.
            }
        });
    }



private void setupRequestManager() {
        requestManager.setObserver(this);
        requestManager.setObserverCallback(new OutputCallback() {
            @Override
            public void outputReady(int outputIndex, int diagramIndex) {
                updateTextOutput(outputIndex, diagramIndex);
            }

            @Override
            public void urlReady(String url, int outputIndex, int diagramIndex) {
                if (url != null) {
                    errorDrawable = ContextCompat.getDrawable(context, R.drawable.diagram_error);
                    myExecutor.execute(() -> {
                        mImage = PlantUMLNetworkManager.mLoad(context, url, () -> {

                            images[outputIndex][diagramIndex] = errorDrawable;
                        }) ;
                        myHandler.post(() -> {
                            if (mImage == null) images[outputIndex][diagramIndex] = errorDrawable;
                            else images[outputIndex][diagramIndex] = mImage;
                            updateImageOutput(outputIndex, diagramIndex);
                        });
                    });
                }
            }

            @Override
            public void ratingScoresReady(int outputType, int[] scores) {
                displayRating(scores, outputType);
            }

            @Override
            public void invalidInput() {
                showPopUpWindow();
            }

        });
        // getImageOrText();
        requestManager.sendAllRequests();
    }

    private void displayRating(int[] scores, int outputType) {
        if (scores == null || scores.length != selectedAspects.size()) return;

        int[] colors = {getColor(R.color.color_primary_light), getColor(R.color.color_primary_dark)};
        int numberOfCharts = scores.length;

        PieChart[] pieCharts = new PieChart[numberOfCharts];
        for (int i = 0; i < numberOfCharts; i++) {
            @SuppressLint("DiscouragedApi") int pieChartId = getResources().getIdentifier("pieChart" + (i + 1), "id", getPackageName());
            pieCharts[i] = findViewById(pieChartId);
        }

        GridLayout chartContainer = findViewById(R.id.chart_container);
        chartContainer.setVisibility(View.INVISIBLE);

        chartContainer.post(() -> {
            int chartWidth = pieCharts[0].getWidth();
            int chartHeight = pieCharts[0].getHeight();
            int verticalSpacing = 190;

            int columns = 2;
            int rows = (numberOfCharts + 1) / columns;

            Bitmap combinedBitmap = Bitmap.createBitmap(
                    chartWidth * columns,
                    chartHeight * rows + verticalSpacing * (rows),
                    Bitmap.Config.ARGB_8888
            );
            Canvas combinedCanvas = new Canvas(combinedBitmap);
            // Fill the canvas with a white background
            combinedCanvas.drawColor(Color.WHITE);

            Paint textPaint = new Paint();
            textPaint.setColor(Color.BLACK);
            textPaint.setTextSize(66);
            textPaint.setTextAlign(Paint.Align.CENTER);

            for (int i = 0; i < numberOfCharts; i++) {
                PieChart pieChart = pieCharts[i];
                pieChart.setVisibility(View.INVISIBLE);
                int score = scores[i];

                ArrayList<PieEntry> entries = new ArrayList<>();
                entries.add(new PieEntry(score, selectedAspects.get(i)));
                entries.add(new PieEntry(100 - score));

                PieDataSet dataSet = new PieDataSet(entries, "");
                dataSet.setColors(colors);
                dataSet.setValueTextSize(24);
                dataSet.setValueTextColor(getColor(R.color.background_light));

                PieData data = new PieData(dataSet);
                pieChart.setData(data);
                pieChart.setDrawEntryLabels(false);
                pieChart.getDescription().setEnabled(false);
                pieChart.getLegend().setEnabled(false);

                pieChart.invalidate();

                pieChart.setDrawingCacheEnabled(true);
                pieChart.buildDrawingCache();
                Bitmap chartBitmap = pieChart.getDrawingCache();

                int left = (i % columns) * chartWidth;
                int top = (i / columns) * (chartHeight + verticalSpacing) + 40;

                combinedCanvas.drawText(selectedAspects.get(i).toUpperCase(), left + (float) chartWidth / 2, top + 80, textPaint);
                combinedCanvas.drawBitmap(chartBitmap, left, top + 60 * 2, null);
                pieChart.setDrawingCacheEnabled(false);

                pieChart.setVisibility(View.GONE);
            }

            Drawable drawable = new BitmapDrawable(getResources(), combinedBitmap);
            images[outputType][0] = drawable;
            updateImageOutput(outputType, 0);
            loadingRatings[outputType] = false;
            chartContainer.setVisibility(View.GONE);
        });

    }

    public void shareClick(View view) {
        String regular, improved;
        Drawable regularRating = images[0][0];
        Drawable improvedRating = images[1][0];
        if (savedInDB) {
            regular = outputs[0][0];
            improved = outputs[1][0];

        } else {
            regular = requestManager.getOutput(0, 0);
            improved = requestManager.getOutput(1, 0);
        }
        if(regular != null && improved != null && !loadingRatings[0] && !loadingRatings[1]) PDFUtil.SharePDF(this, regular, improved, regularRating, improvedRating);
        else Toast.makeText(this, "Results are not ready to share yet", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void improvementClick(View v) {
        lastSelectedType = outputType;
        outputType = (outputType + 1) % 2;
        ImageButton btn = findViewById(R.id.improvementButton);
        View imprSymbolUnclicked = findViewById(R.id.impr_symbol);
        View imprSymbolClicked = findViewById(R.id.impr_smybol_clicked);
        TextView imprBtnText = findViewById(R.id.impr_btn_text);
        int yellow = ContextCompat.getColor(context, R.color.accent_color);
        int transparent = ContextCompat.getColor(context, R.color.background_light);
        if (outputType == 1) {
            imprBtnText.setText("Original\nArchitecture");
            btn.setBackgroundResource(R.drawable.border_light);
            btn.setImageResource(0);
            imprBtnText.setTextColor(yellow);
            imprSymbolClicked.setVisibility(View.VISIBLE);
            imprSymbolUnclicked.setVisibility(View.GONE);
        } else {
            imprBtnText.setText("Improved\nArchitecture");
            btn.setImageResource(R.drawable.rounded_impr_button);
            btn.setBackgroundResource(0);
            imprBtnText.setTextColor(transparent);
            imprSymbolClicked.setVisibility(View.GONE);
            imprSymbolUnclicked.setVisibility(View.VISIBLE);
        }

        int selectedPosition = diagramTypeSpinner.getSelectedItemPosition();
        Objects.requireNonNull(diagramTypeSpinner.getOnItemSelectedListener()).onItemSelected(
                diagramTypeSpinner,
                diagramTypeSpinner.getSelectedView(),
                selectedPosition,
                diagramTypeSpinner.getSelectedItemId()
        );
    }

    public void showPopUpWindow() {
        returnedHome = false;

        // Inflate the popup layout
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View popUpView = inflater.inflate(R.layout.check_input_popup, null);

        // Specify the width and height of the popup window
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.MATCH_PARENT; //ViewGroup.LayoutParams.WRAP_CONTENT;
        // create the popup window
        final PopupWindow popupWindow = new PopupWindow(popUpView, width, height, true);

        // Show the popup window at the center of the activity's main layout
        rootLayout.setForeground(AppCompatResources.getDrawable(this, R.color.divider_color));
        rootLayout.post(() -> {
            if (!isFinishing() && !isDestroyed()) popupWindow.showAtLocation(rootLayout, Gravity.CENTER, 0, 0);
        });

        // Find the countdown TextView in the pop-up layout
        countdown = popUpView.findViewById(R.id.countDown);

        // Create a countdown timer for 10 seconds with 1-second intervals
        CountDownTimer timer = getCountDownTimer(popupWindow);
        Button returnBtn = popUpView.findViewById(R.id.return_home);
        // Set a click listener for the return button
        returnBtn.setOnClickListener(v -> {
            returnedHome = true;

            // Dismiss the popup window
            popupWindow.dismiss();

            // Jump to MainActivity
            Intent homeIntent = new Intent(context, MainActivity.class);
            startActivity(homeIntent);
            finish(); // Close AnalysisActivity
            // Cancel the countdown timer
            timer.cancel();
        });
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (popupWindow != null && popupWindow.isShowing()) {
//            popupWindow.dismiss();
//        }
//    }

    private @NonNull CountDownTimer getCountDownTimer(PopupWindow popupWindow) {
        CountDownTimer timer = new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                // Update the countdown TextView with the remaining time
                countdown.setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {
                // Dismiss the popup window and jump back to MainActivity when the countdown is finished
                if (returnedHome) return;
                popupWindow.dismiss();
                Intent homeIntent = new Intent(context, MainActivity.class);
                startActivity(homeIntent);
                finish(); // close AnalysisActivity
            }
        };
        timer.start();
        return timer;
    }

    public void copyText(View view) {
        String textToCopy = descTextView.getText().toString();

        if (!textToCopy.isEmpty()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("", textToCopy);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No text to copy", Toast.LENGTH_SHORT).show();
        }
    }

    public void saveImage(View view) {
        ImageView imageView = findViewById(R.id.diagram_image);
        saveImageFromImageView(this, imageView);
    }

    public static void saveImageFromImageView(Context context, ImageView imageView) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        if (bitmapDrawable == null) {
            Toast.makeText(context, "No image to save", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap bitmap = bitmapDrawable.getBitmap();

        // Generate a random filename using UUID
        String filename = "image_" + UUID.randomUUID().toString() + ".jpg";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveImageToMediaStore(context, bitmap, filename);
        } else {
            saveImageToExternalStorageLegacy(context, bitmap, filename);
        }
    }

    private static void saveImageToMediaStore(Context context, Bitmap bitmap, String filename) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        Uri uri;
        try {
            uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            if (uri != null) {
                try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
                    assert outputStream != null;
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    Toast.makeText(context, "Image saved successfully", Toast.LENGTH_SHORT).show();}
            } else {
                Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Failed to save image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static void saveImageToExternalStorageLegacy(Context context, Bitmap bitmap, String filename) {
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(storageDir, filename);

        try {
            try (FileOutputStream outputStream = new FileOutputStream(imageFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();
            }
            Toast.makeText(context, "Image saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Failed to save image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void clearClick(View view) {
        selectedAspects.clear();
        for (int i = 0; i < menuItems.size(); i++) {
            MenuItem menuItem = (MenuItem) menuListView.getItemAtPosition(i);
            menuItem.setChecked(false);
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        if (savedInDB) super.onBackPressed();
        // Prevent showing the dialog multiple times if back is pressed repeatedly
        else if (!isFinishing()) {
            showSaveDialog();
        }
    }

    private void showSaveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save Results");
        builder.setMessage("Do you want to save the results in history?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                saveResult();
                finish(); // Close the activity
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // Handle back press as normal
                AnalysisActivity.super.onBackPressed();
            }
        });

        // Make the dialog not cancellable by clicking outside
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveResult() {
        RequestManager.SAVE_TO_DB = true;
        requestManager.saveInDatabase();
    }

    public void sumClick(View view) {

    }
}