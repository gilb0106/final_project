package com.example.final_project;

import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.List;

public class SavedImagesAdapter extends ArrayAdapter<SavedImage> {

    private Context context;
    private List<SavedImage> savedImageList;

    public SavedImagesAdapter(Context context, List<SavedImage> savedImageList) {
        super(context, 0, savedImageList);
        this.context = context;
        this.savedImageList = savedImageList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_saved_image, parent, false);
        }

        SavedImage savedImage = savedImageList.get(position);

        TextView textViewDate = convertView.findViewById(R.id.textViewDate);

        // Display only the date in the list item
        textViewDate.setText("Date: " + savedImage.getDate());

        // Handle item click event
        convertView.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Options")
                    .setMessage("Do you wish to view the image or delete?")
                    .setPositiveButton("View Image", (dialog, which) -> {
                        // Launch fragment to view image
                        ImageViewFragment fragment = new ImageViewFragment();
                        Bundle args = new Bundle();
                        args.putString("date", savedImage.getDate());
                        args.putString("imageUrl", savedImage.getImageUrl());
                        args.putString("hdUrl", savedImage.getHdUrl());
                        fragment.setArguments(args);
                        ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit();
                    })
                    .setNegativeButton("Delete", (dialog, which) -> {
                        // Delete record from database
                        String imageUrl = savedImage.getImageUrl();
                        DBConnect dbConnect = new DBConnect(context);
                        long result = dbConnect.deleteSavedImage(imageUrl);
                        if (result != -1) {
                            // Delete local copy of bitmap
                            try {
                                ContextWrapper contextWrapper = new ContextWrapper(context.getApplicationContext());
                                File directory = contextWrapper.getDir("images", Context.MODE_PRIVATE);
                                File filePath = new File(directory, savedImage.getDate() + ".jpg");
                                if (filePath.exists()) {
                                    filePath.delete();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            // Remove item from the list
                            savedImageList.remove(position);
                            notifyDataSetChanged();
                            Toast.makeText(context, "Image deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to delete image", Toast.LENGTH_SHORT).show();
                        }
                    });

            builder.show();
        });

        return convertView;
    }
}
