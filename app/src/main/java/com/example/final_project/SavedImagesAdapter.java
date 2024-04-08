package com.example.final_project;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
        TextView textViewURL = convertView.findViewById(R.id.textViewURL);
        TextView textViewHDURL = convertView.findViewById(R.id.textViewHDURL);

        textViewDate.setText("Date: " + savedImage.getDate());
        textViewURL.setText("URL: " + savedImage.getImageUrl());
        textViewHDURL.setText("HD URL: " + savedImage.getHdUrl());

        // Handle item click event
        convertView.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Options")
                    .setMessage("Do you wish to view the image or delete?")
                    .setPositiveButton("View Image", (dialog, which) -> {
                        // Launch fragment to view image
                        String imageUrl = savedImageList.get(position).getImageUrl();
                        ImageViewFragment fragment = new ImageViewFragment();
                        Bundle args = new Bundle();
                        args.putString("imageUrl", imageUrl);
                        fragment.setArguments(args);
                        ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit();
                    })
                    .setNegativeButton("Delete", (dialog, which) -> {
                        // Delete record from database
                        String imageUrl = savedImageList.get(position).getImageUrl();
                        DBConnect dbConnect = new DBConnect(context);
                        dbConnect.deleteSavedImage(imageUrl);
                        // Remove item from the list
                        savedImageList.remove(position);
                        notifyDataSetChanged();
                        Toast.makeText(context, "Image deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNeutralButton("Cancel", null)
                    .show();
        });

        return convertView;
    }
}
