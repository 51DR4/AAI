/*
 * Copyright (c) 2024 ABB-03
 * Licensed under the MIT License. See the LICENSE file for details.
 *
 */

package com.application.aai.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.aai.BuildConfig;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Handles sending requests to the Gemini API using text prompts and optionally an image. It processes the API response and communicates back to the caller through a callback interface.
 */
public abstract class GeminiNetworkManager {
    private static final String API_KEY = BuildConfig.apiKey;

    /**
     Sends a request to the Gemini API with the provided text prompt and optionally an image. The response from the API is handled asynchronously and the result is returned back through the provided NetworkCallback.
     *
     * @param context  The Android context from which the request is made, used to get the main executor.
     * @param prompt   The text prompt to be sent to the Gemini API.
     * @param image    An optional image to be sent along with the prompt. Can be null if only text is used.
     * @param callback The callback interface to handle the API response or any errors.
     */
    public static void sendRequest(Context context, String prompt, Bitmap image, NetworkCallback callback) {

        // The Gemini 1.5 models are versatile and work with both text-only and multimodal prompts
        GenerativeModel gm = new GenerativeModel(/* modelName */ "gemini-1.5-flash",
                // Access your API key as a Build Configuration variable (see "Set up your API key" above)
                /* apiKey */ API_KEY);

        // Use the GenerativeModelFutures Java compatibility layer which offers
        // support for ListenableFuture and Publisher APIs
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content;
        if (image == null) {
            content = new Content.Builder()
                    .addText(prompt)
                    .build();
        } else {
            content = new Content.Builder()
                    .addText(prompt)
                    .addImage(image)
                    .build();
        }

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                callback.onResponse(resultText);
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                Toast.makeText(context, "Response could not be generated. Please try again.", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(context));

    }

}
