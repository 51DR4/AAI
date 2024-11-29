/*
 * Copyright (c) 2024 ABB-03
 * Licensed under the MIT License. See the LICENSE file for details.
 *
 */

package com.application.aai.processing.requests;

import static com.application.aai.views.AnalysisActivity.selectedAspects;

import android.graphics.Bitmap;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.application.aai.api.GeminiNetworkManager;
import com.application.aai.api.NetworkCallback;
import com.application.aai.processing.database.DataBaseHelper;
import com.application.aai.processing.database.DataModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * Manages all requests sent to the LLM and handles responses. Only intermediary between Views and LLM.
 * Implements the Singleton pattern to ensure only one instance exists.
 */
public class RequestManager {
    public static boolean SAVE_TO_DB = false;
    private static RequestManager instance; // Singleton instance
    private AppCompatActivity observer;
    private OutputCallback observerCallback;
    private final int NUMBER_OF_DIAGRAMS = 7;
    private String imagePath;
    private boolean first = false;
    public final static String[] diagramTypes = {
            "SystemArchitectureDiagram",
            "ComponentDiagram",
            "ClassDiagram",
            "ObjectDiagram",
            "StateDiagram",
            "UseCaseDiagram",
            "SequenceDiagram"
    };
    private final String[] nonDiagramPrompts = new String[]{ Prompts.NON_DIAGRAM_PROMPT_1, Prompts.NON_DIAGRAM_PROMPT_2 };

    private final String[] diagramPrompts = new String[]{ Prompts.DIAGRAM_PROMPT_1, Prompts.DIAGRAM_PROMPT_2 };
    private final String[][] outputs  = new String[2][NUMBER_OF_DIAGRAMS + 1];
    private final int[][] scoresArrays = new int[2][selectedAspects.size()];
    private Bitmap imageInput;
    private String audioInput = "";
    private String summary;
    private boolean saved = false;

    // Private constructor to prevent instantiation
    private RequestManager() { }

    /**
     * Provides access to the singleton instance of RequestManager.
     *
     * @return The single instance of RequestManager.
     */
    public static synchronized RequestManager getInstance() {
        if (instance == null) {
            instance = new RequestManager();
        }
        return instance;
    }

    /**
     * Sets the observer activity to receive updates.
     *
     * @param observer The activity to observe changes.
     */
    public void setObserver(AppCompatActivity observer) {
        this.observer = observer;
    }

    /**
     * Sets the callback to handle output results.
     *
     * @param observerCallback The callback for output handling.
     */
    public void setObserverCallback(OutputCallback observerCallback) {
        this.observerCallback = observerCallback;
    }

    /**
     * Sets the input for processing with an image.
     *
     * @param input The image to be processed.
     * @param path  The path to the image file.
     */
    public void setInput(Bitmap input, String path) {
        SAVE_TO_DB = false;
        first = false;
        saved = false;
        imageInput = input;
        imagePath = path;
    }

    /**
     * Sets the input for processing with text.
     *
     * @param input The text to be processed.
     */
    public void setInput(String input) {
        SAVE_TO_DB = false;
        first = false;
        saved = false;
        audioInput = input;
    }

    /**
     * Sends all requests for processing diagrams and non-diagram analysis.
     */
    public void sendAllRequests() {
        sendNonDiagramRequests();
    }

    private void sendDiagramRequests() {
        if (first) return;
        else first = true;
        for (int i = 0; i < 2; i++) {
            int outputType = i;
            String prompt = diagramPrompts[outputType];
            if (outputType == 1) prompt += outputs[1][0];
            if (!Objects.equals(audioInput, "")) prompt += "Here is the software architecture description: " + audioInput;
            String finalPrompt = prompt;

            new Thread(() -> GeminiNetworkManager.sendRequest(observer, finalPrompt, imageInput, new NetworkCallback() {
                @Override
                public void onResponse(String output) {
                    handleDiagramResponse(output, outputType);
                    if (SAVE_TO_DB) saveInDatabase();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(observer, "Network error.", Toast.LENGTH_SHORT).show();
                }
            })).start();
        }
    }

    private void handleDiagramResponse(String output, int outputType) {
        output = removeBeforeAndAfterBrackets(output);
        Gson gson = new Gson();
        JsonObject jsonResponse;
        try {
            jsonResponse = gson.fromJson(output, JsonObject.class);
        } catch (Exception e) {
            // malformed json: try again
            sendDiagramRequests();
            return;
        }

        // Extract PlantUML scripts from JSON response
        for (int i = 0; i < NUMBER_OF_DIAGRAMS; i++) {
            String diagramType = diagramTypes[i];
            if (jsonResponse.has(diagramType)) {
                String plantUmlScript = jsonResponse.get(diagramType).getAsString();
                outputs[outputType][i + 1] = plantUmlScript.replace("```", "");
                observerCallback.outputReady(outputType, i + 1);
                String url = Encoder.generateURL(plantUmlScript);
                observerCallback.urlReady(url, outputType, i + 1);
            }
        }
    }

    public void sendNonDiagramRequests() {
        for (int i = 0; i < 2; i++) {
            int outputType = i;
            String prompt = nonDiagramPrompts[outputType] + selectedAspects.toString();
            if (!Objects.equals(audioInput, "")) prompt += "Here are additional information relevant to the architecture: " + audioInput;
            String finalPrompt = prompt;

            new Thread(() -> GeminiNetworkManager.sendRequest(observer, finalPrompt, imageInput, new NetworkCallback() {
                @Override
                public void onResponse(String output) {
                    handleNonDiagramResponse(output, outputType);
                    if (SAVE_TO_DB) saveInDatabase();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(observer, "Network error.", Toast.LENGTH_SHORT).show();
                }
            })).start();
        }
    }

    private void handleNonDiagramResponse(String output, int outputType) {
        try {
            output = removeBeforeAndAfterBrackets(output);
            Gson gson = new Gson();
            JsonObject jsonResponse = gson.fromJson(output, JsonObject.class);

            String valid = jsonResponse.get("validity").getAsString();
            if ("0".equals(valid)) {
                observerCallback.invalidInput();
                return;
            } else sendDiagramRequests();

            String scoresString = jsonResponse.get("scores").getAsString();
            String description = jsonResponse.get("description").getAsString();

            scoresArrays[outputType] = getIntArrayFromCommaSeparatedString(scoresString);

            synchronized (outputs) {
                outputs[outputType][0] = description;
            }
            observerCallback.outputReady(outputType, 0);
            observerCallback.ratingScoresReady(outputType, scoresArrays[outputType]);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the output for a specific type and diagram type.
     *
     * @param type        The type of output (0 or 1).
     * @param diagramType The index of the diagram type.
     * @return The output string or an error message.
     */
    public String getOutput(int type, int diagramType) {
        if ((0 == type || type == 1) && diagramType >= 0 && diagramType <= NUMBER_OF_DIAGRAMS + 1)
            return outputs[type][diagramType];
        else
            return "Internal error";
    }

    /**
     * Converts the comma-separated string from of scores from the LLM response into an integer array.
     *
     * @param scoresString The comma-separated string of scores.
     * @return An array of integer scores.
     */
    public int[] getIntArrayFromCommaSeparatedString(String scoresString) {
        // Step 1: Split the string by commas
        String[] scoreStrings = scoresString.split(",");

        // Step 2: Create an int array to store the scores
        int[] scores = new int[scoreStrings.length];

        // Step 3: Convert each score string to an integer and store in the int array
        for (int i = 0; i < scoreStrings.length; i++) {
            try {
                scores[i] = Integer.parseInt(scoreStrings[i].trim()); // Trim to handle any extra whitespace
            } catch (NumberFormatException e) {
                // Handle parsing errors if necessary
                e.printStackTrace();
                // Set a default value or handle the error case appropriately
                scores[i] = 0; // Default value
            }
        }
        return scores;
    }

    /**
     * Removes any text before the first '{' and after the last '}' in the input string to ensure correct JSON format
     *
     * @param input The input string.
     * @return The substring between the first '{' and the last '}'.
     */
    public String removeBeforeAndAfterBrackets(String input) {
        int firstOpenIndex = input.indexOf('{');
        int lastCloseIndex = input.lastIndexOf('}');

        // Check if both opening and closing brackets exist
        if (firstOpenIndex == -1 || lastCloseIndex == -1 || firstOpenIndex > lastCloseIndex) {
            return "";
        }

        // Extract the substring between the first '{' and the last '}'
        return input.substring(firstOpenIndex, lastCloseIndex + 1);
    }

    /**
     * Saves all outputs and scores to the database.
     */
    public void saveInDatabase() {
        if(saved || !outputsReady()) return;
        DataModel dataModel;
        try {
            dataModel = new DataModel(-1, imagePath, audioInput, outputs[0][0], outputs[0][1], outputs[0][2], outputs[0][3], outputs[0][4], outputs[0][5], outputs[0][6], outputs[0][7], outputs[1][0], outputs[1][1], outputs[1][2], outputs[1][3], outputs[1][4], outputs[1][5], outputs[1][6], outputs[1][7], scoresArrays[0], scoresArrays[1]);
            try (DataBaseHelper dataBaseHelper = new DataBaseHelper(observer)) {
                dataBaseHelper.saveToDatabase(dataModel);
            }
        } catch (Exception e){
            Toast.makeText(observer, "Internal Error: Results could not be saved to history", Toast.LENGTH_SHORT).show();
        }
        if (!saved) saved = true;
    }

    private boolean outputsReady() {
        for (String[] row:
                outputs) {
            for (String output:
                    row) {
                if (output == null) return false;
            }
        }
        return true;
    }

    public void sendSummarizeRequest(String text, CustomCallback callback) {
        String prompt = Prompts.SUM_PROMPT + text;

        new Thread(() -> GeminiNetworkManager.sendRequest(observer, prompt, null, new NetworkCallback() {
            @Override
            public void onResponse(String output) {
                summary = output;
                callback.handleEvent();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(observer, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        })).start();
    }

    public String getSummary() {
        return summary;
    }
}
