/*
 * Copyright (c) 2024 ABB-03
 * Licensed under the MIT License. See the LICENSE file for details.
 *
 */

package com.application.aai.api;
/*
This interface is used for communicating responses and errors between the LLM and the request sender (ReuquestManager class)
 */
public interface NetworkCallback {
    void onResponse(String output);
    void onFailure(String error);
}
