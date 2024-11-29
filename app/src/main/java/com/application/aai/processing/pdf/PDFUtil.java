/*
 * Copyright (c) 2024 ABB-03
 * Licensed under the MIT License. See the LICENSE file for details.
 *
 */

package com.application.aai.processing.pdf;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.print.pdf.PrintedPdfDocument;
import android.print.PrintAttributes;
import android.text.Spanned;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.application.aai.processing.requests.HtmlParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utility class for creating and sharing PDFs containing the LLM responses.
 */
public class PDFUtil {

    private static final int PAGE_WIDTH = 595;
    private static final int PAGE_HEIGHT = 842;
    private static final int MARGIN = 30;
    private static final int LINE_HEIGHT = 12;
    private static final int TEXT_SIZE = 11;

    /**
     * Creates and shares a PDF document containing the provided regular and improved results,
     * with their respective ratings as images.
     *
     * @param context       The context in which this operation is executed.
     * @param regularResult The regular architecture result in HTML format.
     * @param improvedResult The improved architecture result in HTML format.
     * @param regularRating  The drawable image representing the rating of the regular architecture.
     * @param improvedRating The drawable image representing the rating of the improved architecture.
     */
    public static void SharePDF(Context context, String regularResult, String improvedResult, Drawable regularRating, Drawable improvedRating) {
        // Define print attributes
        PrintAttributes printAttributes = new PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setResolution(new PrintAttributes.Resolution("res1", "Resolution", PAGE_WIDTH, PAGE_HEIGHT))
                .setMinMargins(new PrintAttributes.Margins(MARGIN, MARGIN, MARGIN, MARGIN))
                .build();

        // Create a new document
        PdfDocument document = new PrintedPdfDocument(context, printAttributes);

        // Create a page description
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();

        // Define Paint objects for titles and regular text
        Paint titlePaint = new Paint();
        titlePaint.setColor(android.graphics.Color.BLACK);
        titlePaint.setTextSize(TEXT_SIZE * 1.5f); // Larger size for titles
        titlePaint.setFakeBoldText(true); // Make the text bold
        titlePaint.setTextAlign(Paint.Align.CENTER);

        Paint regularPaint = new Paint();
        regularPaint.setColor(android.graphics.Color.BLACK);
        regularPaint.setTextSize(TEXT_SIZE);

        int x = MARGIN, y = MARGIN;

        // Start a page
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Draw the regular result text as a title
        y = drawTextWrapped(canvas, titlePaint, "Original Architecture", PAGE_WIDTH / 2, y);
        y += LINE_HEIGHT;
        y = drawTextWrapped(canvas, regularPaint, regularResult, x, y);
        y += LINE_HEIGHT;

        // Draw the regular rating drawable
        if (regularRating != null) {
            Bitmap bitmap = drawableToBitmap(regularRating);
            if (y + bitmap.getHeight() > PAGE_HEIGHT - MARGIN) {
                document.finishPage(page);
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = MARGIN;
            }
            y = drawTextWrapped(canvas, titlePaint, "Estimated rating scores of the original architecture", canvas.getWidth() / 2, y);
            y += LINE_HEIGHT;
            canvas.drawBitmap(bitmap, (float) (canvas.getWidth() - bitmap.getWidth()) / 2, y, null);
        }

        // Finish the current page
        document.finishPage(page);

        // Start a new page for improved result
        page = document.startPage(pageInfo);
        canvas = page.getCanvas();
        y = MARGIN;

        // Draw the improved result text as a title
        y = drawTextWrapped(canvas, titlePaint, "Improved Architecture", PAGE_WIDTH / 2, y);
        y += LINE_HEIGHT;
        y = drawTextWrapped(canvas, regularPaint, improvedResult, x, y);
        y += LINE_HEIGHT;

        // Draw the improved rating drawable
        if (improvedRating != null) {
            Bitmap bitmap = drawableToBitmap(improvedRating);
            if (y + bitmap.getHeight() > PAGE_HEIGHT - MARGIN) {
                document.finishPage(page);
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = MARGIN;
            }
            y = drawTextWrapped(canvas, titlePaint, "Estimated rating scores of the improved architecture", canvas.getWidth() / 2, y);
            y += LINE_HEIGHT;
            canvas.drawBitmap(bitmap, (float) (canvas.getWidth() - bitmap.getWidth()) / 2, y, null);
        }

        // Finish the page
        document.finishPage(page);

        // Write the document content to a file
        String filePath = savePdfToFile(context, document);
        if (filePath != null) {
            sharePdf(context, filePath);
        } else {
            Toast.makeText(context, "Failed to create PDF", Toast.LENGTH_SHORT).show();
        }

        // Close the document
        document.close();
    }

    /**
     * Draws text on the canvas, wrapping it if it exceeds the page width.
     *
     * @param canvas The canvas on which to draw the text.
     * @param paint  The paint used to style the text.
     * @param text   The text to be drawn.
     * @param x      The x-coordinate where the text starts.
     * @param y      The y-coordinate where the text starts.
     * @return The updated y-coordinate after drawing the text.
     */
    private static int drawTextWrapped(Canvas canvas, Paint paint, String text, int x, int y) {
        Spanned spannedText = HtmlParser.fromHtml(text);
        String[] lines = spannedText.toString().split("\n");
        for (String line : lines) {
            String[] words = line.split(" ");
            StringBuilder currentLine = new StringBuilder();
            for (String word : words) {
                String testLine = currentLine + word + " ";
                float textWidth = paint.measureText(testLine);
                if (textWidth > PAGE_WIDTH - 2 * MARGIN) {
                    // Draw the current line
                    canvas.drawText(currentLine.toString(), x, y, paint);
                    currentLine = new StringBuilder(word + " ");
                    y += LINE_HEIGHT;
                } else {
                    currentLine.append(word).append(" ");
                }
            }
            // Draw the last line in the current block
            canvas.drawText(currentLine.toString(), x, y, paint);
            y += LINE_HEIGHT;
        }
        return y;
    }


    private static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth() / 7, drawable.getIntrinsicHeight() / 7, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Saves the generated PDF document to a file on external storage.
     *
     * @param context  The context in which this operation is executed.
     * @param document The PdfDocument to be saved.
     * @return The file path of the saved PDF, or null if saving failed.
     */
    private static String savePdfToFile(Context context, PdfDocument document) {
        String directoryPath = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/pdfs";
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String filePath = directoryPath + "/result.pdf";
        File file = new File(filePath);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            fos.close();
            return filePath;
        } catch (IOException e) {
            Toast.makeText(context, "Error saving PDF. Please try again", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * Shares the PDF document through an Intent, allowing the user to choose the sharing method.
     *
     * @param context  The context in which this operation is executed.
     * @param filePath The file path of the PDF to be shared.
     */
    private static void sharePdf(Context context, String filePath) {
        File file = new File(filePath);
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(Intent.createChooser(intent, "Share PDF"));
    }
}