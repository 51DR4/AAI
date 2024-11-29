/*
 * Copyright (c) 2024 ABB-03
 * Licensed under the MIT License. See the LICENSE file for details.
 *
 */

package com.application.aai.processing.requests;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;


/**
 * Helps convert PlantUML syntax into a format that can be used in a URL.
 * It compresses the input text and then encodes it to make it URL-friendly.
 */
public class Encoder {

    /**
     * Generates a URL for a PlantUML diagram.
     *
     * @param text The PlantUML syntax to encode.
     * @return The complete URL to render the PlantUML diagram.
     */
    public static String generateURL(String text) {
        return "http://www.plantuml.com/plantuml/png/~1" + Encoder.encodep(text);
    }

    /**
     * Encodes the provided text by first compressing it and then encoding it into a
     * base64-like format.
     *
     * @param text The text to encode.
     * @return The encoded string.
     */
    public static String encodep(String text) {
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        byte[] compressed = compress(data);
        return encode64(compressed);
    }

    /**
     * Compresses the given byte array using the Deflater algorithm with the best compression level.
     *
     * @param data The byte array to compress.
     * @return The compressed byte array.
     */
    private static byte[] compress(byte[] data) {
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        deflater.setInput(data);
        deflater.finish();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }

    /**
     * Encodes a 6-bit value into a single character using a custom encoding scheme.
     *
     * @param b The 6-bit value to encode.
     * @return The encoded character as a string.
     */
    private static String encode6bit(int b) {
        if (b < 10) {
            return String.valueOf((char) (48 + b));
        }
        b -= 10;
        if (b < 26) {
            return String.valueOf((char) (65 + b));
        }
        b -= 26;
        if (b < 26) {
            return String.valueOf((char) (97 + b));
        }
        b -= 26;
        if (b == 0) {
            return "-";
        }
        if (b == 1) {
            return "_";
        }
        return "?";
    }

    /**
     * Encodes three bytes into four 6-bit characters using the custom encoding scheme.
     *
     * @param b1 The first byte.
     * @param b2 The second byte.
     * @param b3 The third byte.
     * @return A string of four encoded characters.
     */
    private static String append3bytes(int b1, int b2, int b3) {
        int c1 = b1 >> 2;
        int c2 = ((b1 & 0x3) << 4) | (b2 >> 4);
        int c3 = ((b2 & 0xF) << 2) | (b3 >> 6);
        int c4 = b3 & 0x3F;

        return encode6bit(c1 & 0x3F) +
                encode6bit(c2 & 0x3F) +
                encode6bit(c3 & 0x3F) +
                encode6bit(c4 & 0x3F);
    }

    /**
     * Encodes a byte array into a base64-like string using the custom 6-bit encoding.
     *
     * @param c The byte array to encode.
     * @return The encoded string.
     */
    private static String encode64(byte[] c) {
        StringBuilder str = new StringBuilder();
        int len = c.length;
        for (int i = 0; i < len; i += 3) {
            if (i + 2 == len) {
                str.append(append3bytes(c[i] & 0xFF, c[i + 1] & 0xFF, 0));
            } else if (i + 1 == len) {
                str.append(append3bytes(c[i] & 0xFF, 0, 0));
            } else {
                str.append(append3bytes(c[i] & 0xFF, c[i + 1] & 0xFF, c[i + 2] & 0xFF));
            }
        }
        return str.toString();
    }

}
