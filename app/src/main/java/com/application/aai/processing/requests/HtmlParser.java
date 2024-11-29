/*
 * Copyright (c) 2024 ABB-03
 * Licensed under the MIT License. See the LICENSE file for details.
 *
 */

package com.application.aai.processing.requests;

import android.text.Html;
import android.text.Spanned;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Extracts Text from a HTML script (the LLM response formatted in HTML) for adding to PDF
 */
public class HtmlParser {

    public static String extractTextFromHtml(String html) {
        // Parse HTML string into a Jsoup Document
        Document doc = Jsoup.parse(html);

        // Use Jsoup's methods to extract text
        return doc.text();
    }

    public static Spanned fromHtml(String html) {
        return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
    }
}