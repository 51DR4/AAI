/*
 * Copyright (c) 2024 ABB-03
 * Licensed under the MIT License. See the LICENSE file for details.
 *
 */

package com.application.aai.processing.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class DataBaseHelper extends SQLiteOpenHelper {
    //Database table name
    public static final String DB_TABLE = "DB_TABLE";
    //Database columns
    public static final String COLUMN_ID = "ID";                      //column id number
    public static final String COLUMN_IMAGEPATH = "IMAGEPATH";        //path of saved images
    public static final String COLUMN_TRANSCRIPTIONTEXT = "TRANSCRIPTIONTEXT";
    public static final String COLUMN_RESULTTEXT00 = "RESULTTEXT00";  // responses from Gemini
    public static final String COLUMN_RESULTTEXT01 = "RESULTTEXT01";
    public static final String COLUMN_RESULTTEXT02 = "RESULTTEXT02";
    public static final String COLUMN_RESULTTEXT03 = "RESULTTEXT03";
    public static final String COLUMN_RESULTTEXT04 = "RESULTTEXT04";
    public static final String COLUMN_RESULTTEXT05 = "RESULTTEXT05";
    public static final String COLUMN_RESULTTEXT06 = "RESULTTEXT06";
    public static final String COLUMN_RESULTTEXT07 = "RESULTTEXT07";
    public static final String COLUMN_RESULTTEXT10 = "RESULTTEXT10";
    public static final String COLUMN_RESULTTEXT11 = "RESULTTEXT11";
    public static final String COLUMN_RESULTTEXT12 = "RESULTTEXT12";
    public static final String COLUMN_RESULTTEXT13 = "RESULTTEXT13";
    public static final String COLUMN_RESULTTEXT14 = "RESULTTEXT14";
    public static final String COLUMN_RESULTTEXT15 = "RESULTTEXT15";
    public static final String COLUMN_RESULTTEXT16 = "RESULTTEXT16";
    public static final String COLUMN_RESULTTEXT17 = "RESULTTEXT17";
    public static final String COLUMN_DATE_ADDED = "DATE_ADDED"; // Added dateAdded column
    public static final String COLUMN_INT_ARRAY1 = "INT_ARRAY1"; // First scores array
    public static final String COLUMN_INT_ARRAY2 = "INT_ARRAY2"; // Second scores array

    public DataBaseHelper(Context context) {
        super(context, "abb5.db", null, 1);
    }

    //function onCreate() is called the first time a database is accessed.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement = "CREATE TABLE " + DB_TABLE + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_IMAGEPATH + " TEXT, " +
                COLUMN_TRANSCRIPTIONTEXT + " TEXT, " +
                COLUMN_RESULTTEXT00 + " TEXT, " +
                COLUMN_RESULTTEXT01 + " TEXT, " +
                COLUMN_RESULTTEXT02 + " TEXT, " +
                COLUMN_RESULTTEXT03 + " TEXT, " +
                COLUMN_RESULTTEXT04 + " TEXT, " +
                COLUMN_RESULTTEXT05 + " TEXT, " +
                COLUMN_RESULTTEXT06 + " TEXT, " +
                COLUMN_RESULTTEXT07 + " TEXT, " +
                COLUMN_RESULTTEXT10 + " TEXT, " +
                COLUMN_RESULTTEXT11 + " TEXT, " +
                COLUMN_RESULTTEXT12 + " TEXT, " +
                COLUMN_RESULTTEXT13 + " TEXT, " +
                COLUMN_RESULTTEXT14 + " TEXT, " +
                COLUMN_RESULTTEXT15 + " TEXT, " +
                COLUMN_RESULTTEXT16 + " TEXT, " +
                COLUMN_RESULTTEXT17 + " TEXT, " +
                COLUMN_DATE_ADDED + " INTEGER, " + // Added dateAdded column as INTEGER
                COLUMN_INT_ARRAY1 + " TEXT, " + // First integer array column
                COLUMN_INT_ARRAY2 + " TEXT)"; // Second integer array column

        db.execSQL(createTableStatement);  //execSQL() creates the table by passing a SQL statement
    }

    //onUpgrade() is automatically called if the database version number changes when the database design is changed
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrade if necessary
    }

    //An array with integer values is converted into a string to be stored in the database
    private String intArrayToString(int[] intArray) {
        return Arrays.toString(intArray).replaceAll("\\[|\\]|\\s", "");
    }

    //transform a stored string value from the database to an integer array
    private int[] stringToIntArray(String str) {
        if (str == null || str.isEmpty()) {
            return new int[0];
        }
        String[] strArray = str.split(",");
        int[] intArray = new int[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            intArray[i] = Integer.parseInt(strArray[i]);
        }
        return intArray;
    }

    //Append a new entry to the database table by adding another row.
    public void saveToDatabase(DataModel dataModel) {
        //Database is ready for storing data
        SQLiteDatabase db = this.getWritableDatabase();
        //Content value is a hashmap optimized for databases that assigns values to the columns of the table.
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_IMAGEPATH, dataModel.getImagePath());
        cv.put(COLUMN_TRANSCRIPTIONTEXT, dataModel.getTranscriptionText());
        cv.put(COLUMN_RESULTTEXT00, dataModel.getResultText00());
        cv.put(COLUMN_RESULTTEXT01, dataModel.getResultText01());
        cv.put(COLUMN_RESULTTEXT02, dataModel.getResultText02());
        cv.put(COLUMN_RESULTTEXT03, dataModel.getResultText03());
        cv.put(COLUMN_RESULTTEXT04, dataModel.getResultText04());
        cv.put(COLUMN_RESULTTEXT05, dataModel.getResultText05());
        cv.put(COLUMN_RESULTTEXT06, dataModel.getResultText06());
        cv.put(COLUMN_RESULTTEXT07, dataModel.getResultText07());
        cv.put(COLUMN_RESULTTEXT10, dataModel.getResultText10());
        cv.put(COLUMN_RESULTTEXT11, dataModel.getResultText11());
        cv.put(COLUMN_RESULTTEXT12, dataModel.getResultText12());
        cv.put(COLUMN_RESULTTEXT13, dataModel.getResultText13());
        cv.put(COLUMN_RESULTTEXT14, dataModel.getResultText14());
        cv.put(COLUMN_RESULTTEXT15, dataModel.getResultText15());
        cv.put(COLUMN_RESULTTEXT16, dataModel.getResultText16());
        cv.put(COLUMN_RESULTTEXT17, dataModel.getResultText17());
        cv.put(COLUMN_DATE_ADDED, dataModel.getDateAdded()); // Insert dateAdded
        cv.put(COLUMN_INT_ARRAY1, intArrayToString(dataModel.getIntArray1())); // Insert first intArray
        cv.put(COLUMN_INT_ARRAY2, intArrayToString(dataModel.getIntArray2())); // Insert second intArray

        db.insert(DB_TABLE, null, cv); //save the new entry to the database
    }

    //delete a row with a specific id number
    public boolean deleteOne(DataModel dataModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = COLUMN_ID + "=?";
        String[] whereArgs = { String.valueOf(dataModel.getId()) };

        int deletedRows = db.delete(DB_TABLE, whereClause, whereArgs);
        return deletedRows > 0;
    }

    //read out all database entries
    public List<DataModel> getEveryone() {
        List<DataModel> returnList = new ArrayList<>();
        //SQL statement to select all table entries
        String queryString = "SELECT * FROM " + DB_TABLE;
        //database is ready for reading out the entries
        SQLiteDatabase db = this.getReadableDatabase();
        //use rawQuery() to get the entries as a set named cursor
        Cursor cursor = db.rawQuery(queryString, null);
        //Read out all entries in cursor and add them to the returnList.
        if (cursor.moveToFirst()) {
            do {
                int dataID = cursor.getInt(0);
                String dataPath = cursor.getString(1);
                String transcriptionText = cursor.getString(2);
                String dataResultText00 = cursor.getString(3);
                String dataResultText01 = cursor.getString(4);
                String dataResultText02 = cursor.getString(5);
                String dataResultText03 = cursor.getString(6);
                String dataResultText04 = cursor.getString(7);
                String dataResultText05 = cursor.getString(8);
                String dataResultText06 = cursor.getString(9);
                String dataResultText07 = cursor.getString(10);
                String dataResultText10 = cursor.getString(11);
                String dataResultText11 = cursor.getString(12);
                String dataResultText12 = cursor.getString(13);
                String dataResultText13 = cursor.getString(14);
                String dataResultText14 = cursor.getString(15);
                String dataResultText15 = cursor.getString(16);
                String dataResultText16 = cursor.getString(17);
                String dataResultText17 = cursor.getString(18);
                long dateAdded = cursor.getLong(19); // Get dateAdded
                String intArrayString1 = cursor.getString(20); // Get first intArray string
                String intArrayString2 = cursor.getString(21); // Get second intArray string

                DataModel newData = new DataModel(dataID, dataPath, transcriptionText,
                        dataResultText00, dataResultText01, dataResultText02, dataResultText03,
                        dataResultText04, dataResultText05, dataResultText06, dataResultText07,
                        dataResultText10, dataResultText11, dataResultText12, dataResultText13,
                        dataResultText14, dataResultText15, dataResultText16, dataResultText17,
                        stringToIntArray(intArrayString1), stringToIntArray(intArrayString2)); // Set intArrays
                newData.setDateAdded(dateAdded); // Set dateAdded
                returnList.add(newData);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return returnList;
    }
    //return transcriptiontext for a given table entry
    public List<String> getResponseText(DataModel dataModel) {
        List<String> returnList = new ArrayList<>();
        String queryString = "SELECT TRANSCRIPTIONTEXT FROM " + DB_TABLE + " WHERE " + COLUMN_ID + " = " + dataModel.getId();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            do {
                String transcriptionText = cursor.getString(0);
                returnList.add(transcriptionText);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return returnList;
    }
}
