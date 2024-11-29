/*
 * Copyright (c) 2024 ABB-03
 * Licensed under the MIT License. See the LICENSE file for details.
 *
 */

package com.application.aai.processing.database;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class DataModel {
    private int id;
    private String imagePath;
    private String transcriptionText;
    private String resultText00;
    private String resultText01;
    private String resultText02;
    private String resultText03;
    private String resultText04;
    private String resultText05;
    private String resultText06;
    private String resultText07;
    private String resultText10;
    private String resultText11;
    private String resultText12;
    private String resultText13;
    private String resultText14;
    private String resultText15;
    private String resultText16;
    private String resultText17;
    private long dateAdded;
    private int[] intArray1; // First scores array
    private int[] intArray2; // Second scores array

    // Default constructor
    public DataModel() {
        // Initialize dateAdded to the current time
        this.dateAdded = System.currentTimeMillis();
        this.intArray1 = new int[0];
        this.intArray2 = new int[0];
    }

    // Parameterized constructor (excluding dateAdded)
    public DataModel(int id, String imagePath, String transcriptionText, String resultText00, String resultText01, String resultText02, String resultText03, String resultText04, String resultText05, String resultText06, String resultText07, String resultText10, String resultText11, String resultText12, String resultText13, String resultText14, String resultText15, String resultText16, String resultText17, int[] intArray1, int[] intArray2) {
        this.id = id;
        this.imagePath = imagePath;
        this.transcriptionText = transcriptionText;
        this.resultText00 = resultText00;
        this.resultText01 = resultText01;
        this.resultText02 = resultText02;
        this.resultText03 = resultText03;
        this.resultText04 = resultText04;
        this.resultText05 = resultText05;
        this.resultText06 = resultText06;
        this.resultText07 = resultText07;
        this.resultText10 = resultText10;
        this.resultText11 = resultText11;
        this.resultText12 = resultText12;
        this.resultText13 = resultText13;
        this.resultText14 = resultText14;
        this.resultText15 = resultText15;
        this.resultText16 = resultText16;
        this.resultText17 = resultText17;
        this.intArray1 = intArray1;
        this.intArray2 = intArray2;
        // Default constructor will handle setting the dateAdded
        this.dateAdded = System.currentTimeMillis();
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getResultText00() {
        return resultText00;
    }

    public void setResultText00(String resultText00) {
        this.resultText00 = resultText00;
    }

    public String getResultText01() {
        return resultText01;
    }

    public void setResultText01(String resultText01) {
        this.resultText01 = resultText01;
    }

    public String getResultText02() {
        return resultText02;
    }

    public void setResultText02(String resultText02) {
        this.resultText02 = resultText02;
    }

    public String getResultText03() {
        return resultText03;
    }

    public void setResultText03(String resultText03) {
        this.resultText03 = resultText03;
    }

    public String getResultText04() {
        return resultText04;
    }

    public void setResultText04(String resultText04) {
        this.resultText04 = resultText04;
    }

    public String getResultText05() {
        return resultText05;
    }

    public void setResultText05(String resultText05) {
        this.resultText05 = resultText05;
    }

    public String getResultText06() {
        return resultText06;
    }

    public void setResultText06(String resultText06) {
        this.resultText06 = resultText06;
    }

    public String getResultText07() {
        return resultText07;
    }

    public void setResultText07(String resultText07) {
        this.resultText07 = resultText07;
    }

    public String getResultText10() {
        return resultText10;
    }

    public void setResultText10(String resultText10) {
        this.resultText10 = resultText10;
    }

    public String getResultText11() {
        return resultText11;
    }

    public void setResultText11(String resultText11) {
        this.resultText11 = resultText11;
    }

    public String getResultText12() {
        return resultText12;
    }

    public void setResultText12(String resultText12) {
        this.resultText12 = resultText12;
    }

    public String getResultText13() {
        return resultText13;
    }

    public void setResultText13(String resultText13) {
        this.resultText13 = resultText13;
    }

    public String getResultText14() {
        return resultText14;
    }

    public void setResultText14(String resultText14) {
        this.resultText14 = resultText14;
    }

    public String getResultText15() {
        return resultText15;
    }

    public void setResultText15(String resultText15) {
        this.resultText15 = resultText15;
    }

    public String getResultText16() {
        return resultText16;
    }

    public void setResultText16(String resultText16) {
        this.resultText16 = resultText16;
    }

    public String getResultText17() {
        return resultText17;
    }

    public void setResultText17(String resultText17) {
        this.resultText17 = resultText17;
    }

    public String getTranscriptionText() {
        return transcriptionText;
    }

    public void setTranscriptionText(String transcriptionText) {
        this.transcriptionText = transcriptionText;
    }

    public int[] getIntArray1() {
        return intArray1;
    }

    public void setIntArray1(int[] intArray1) {
        this.intArray1 = intArray1;
    }

    public int[] getIntArray2() {
        return intArray2;
    }

    public void setIntArray2(int[] intArray2) {
        this.intArray2 = intArray2;
    }

    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date(dateAdded));

        return "DataModel{" +
                "id=" + id +
                ", imagePath='" + imagePath + '\'' +
                ", transcriptionText='" + transcriptionText + '\'' +
                ", resultText00='" + resultText00 + '\'' +
                ", resultText01='" + resultText01 + '\'' +
                ", resultText02='" + resultText02 + '\'' +
                ", resultText03='" + resultText03 + '\'' +
                ", resultText04='" + resultText04 + '\'' +
                ", resultText05='" + resultText05 + '\'' +
                ", resultText06='" + resultText06 + '\'' +
                ", resultText07='" + resultText07 + '\'' +
                ", resultText10='" + resultText10 + '\'' +
                ", resultText11='" + resultText11 + '\'' +
                ", resultText12='" + resultText12 + '\'' +
                ", resultText13='" + resultText13 + '\'' +
                ", resultText14='" + resultText14 + '\'' +
                ", resultText15='" + resultText15 + '\'' +
                ", resultText16='" + resultText16 + '\'' +
                ", resultText17='" + resultText17 + '\'' +
                ", dateAdded=" + formattedDate +
                ", intArray1=" + Arrays.toString(intArray1) +
                ", intArray2=" + Arrays.toString(intArray2) +
                '}';
    }
}
