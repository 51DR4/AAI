/*
 * Copyright (c) 2024 ABB-03
 * Licensed under the MIT License. See the LICENSE file for details.
 *
 */

package com.application.aai.processing.database;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.application.aai.views.AnalysisActivity;
import com.example.aai.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
//the DataModelAdapter class displays data from a list of DataModel objects in a RecyclerView
public class DataModelAdapter extends RecyclerView.Adapter<DataModelAdapter.ViewHolder> {

    private List<DataModel> dataList; //each entry in the list is of type DataModel and is displayed in the RecyclerView
    private Context context;          //activity in which the adapter is used

    public DataModelAdapter(List<DataModel> dataList, Context context) {
        this.dataList = dataList;
        this.context = context;
    }

    //create a ViewHolder to hold view elements that are displayed in a list.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    //binds the data from the datalist to the corresponding UI elements in the ViewHolder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //read DataModel-entry for current position
        DataModel data = dataList.get(position);

        //determine image
        if (data.getImagePath() != null && !data.getImagePath().isEmpty()) {
            holder.imageView.setImageDrawable(Drawable.createFromPath(data.getImagePath()));
            holder.imageView.setVisibility(View.VISIBLE);
            holder.transcriptionTextView.setVisibility(View.GONE);
            holder.itemSeparatorImage.setVisibility(View.VISIBLE);
            holder.itemSeparatorText.setVisibility(View.GONE);
        } else {
            holder.imageView.setVisibility(View.GONE);
            holder.transcriptionTextView.setText(data.getTranscriptionText());
            holder.transcriptionTextView.setVisibility(View.VISIBLE);
            holder.itemSeparatorImage.setVisibility(View.GONE);
            holder.itemSeparatorText.setVisibility(View.VISIBLE);
        }

        //Date is given the format yyyy-MM-dd
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date(data.getDateAdded()));
        holder.dateTextView.setText(formattedDate);

        //ClickListener to start the AnalysisActivity with the corresponding data from the database of the clicked list element
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AnalysisActivity.class);
            intent.putExtra("FROM_DATABASE", true);
            intent.putExtra("DATABASE_ID", data.getId());
            context.startActivity(intent);
        });
    }

    //returns the number of elements in the dataList so that RecyclerView knows how many entries it should display
    @Override
    public int getItemCount() {
        return dataList.size();
    }

    //contains references to UI elements
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView imageView;
        public final TextView transcriptionTextView;
        public final TextView dateTextView;
        public final View itemSeparatorText;
        public final View itemSeparatorImage;

        public ViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.image_view);
            transcriptionTextView = view.findViewById(R.id.transcription_text_view);
            dateTextView = view.findViewById(R.id.date_text_view);
            itemSeparatorText = view.findViewById(R.id.item_separator_text);
            itemSeparatorImage = view.findViewById(R.id.item_separator_image);
        }
    }
}
