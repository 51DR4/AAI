/*
 * Copyright (c) 2024 ABB-03
 * Licensed under the MIT License. See the LICENSE file for details.
 *
 */

package com.application.aai.processing.requests;

/**
 * Interface for the RequestManager class to communicate results of asynchronous actions to the required context (Analysis Activity)
 */
public interface OutputCallback {
    void outputReady(int outputIndex, int diagramIndex);
    void urlReady(String url, int outputIndex, int diagramIndex);
    void ratingScoresReady(int outputType, int[] scores);
    void invalidInput();
}