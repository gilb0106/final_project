package com.example.final_project;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class SavedActivity extends AppCompatActivity {

    private ListView listViewSavedImages;
    private SavedImagesAdapter savedImagesAdapter;
    private List<SavedImage> savedImageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listViewSavedImages = findViewById(R.id.listViewSavedImages);
        savedImageList = new ArrayList<>();
        savedImagesAdapter = new SavedImagesAdapter(this, savedImageList);
        listViewSavedImages.setAdapter(savedImagesAdapter);

        fetchSavedImages();

        listViewSavedImages.setOnItemClickListener((parent, view, position, id) -> {
            SavedImage clickedImage = savedImageList.get(position);
            // Hide the ListView
            listViewSavedImages.setVisibility(View.GONE);
            // Show the ImageViewFragment
            ImageViewFragment fragment = new ImageViewFragment();
            Bundle args = new Bundle();
            args.putString("date", clickedImage.getDate());
            args.putString("imageUrl", clickedImage.getImageUrl());
            args.putString("hdUrl", clickedImage.getHdUrl());
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void fetchSavedImages() {
        // Fetch saved images from the database
        DBConnect dbConnect = new DBConnect(this);
        Cursor cursor = dbConnect.getAllSavedImages();

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    // Extract data from the cursor
                    String date = cursor.getString(
                            cursor.getColumnIndex(DBConnect.COLUMN_IMAGE_DATE));
                    String imageUrl = cursor.getString(
                            cursor.getColumnIndex(DBConnect.COLUMN_NASA_URL));
                    String hdUrl = cursor.getString(
                            cursor.getColumnIndex(DBConnect.COLUMN_HD_URL));
                    // Create a SavedImage object and add it to the list
                    SavedImage savedImage = new SavedImage(date, imageUrl, hdUrl);
                    savedImageList.add(savedImage);
                } while (cursor.moveToNext());
            } else {
                // Handle the case when the cursor is empty
                Log.d("FetchSavedImages", "No saved images found");
            }
            // Close the cursor
            cursor.close();
        } else {
            // Handle the case when the cursor is null
            Log.e("FetchSavedImages", "Cursor is null");
        }
        // Notify the adapter that the data set has changed
        savedImagesAdapter.notifyDataSetChanged();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            listViewSavedImages.setVisibility(View.VISIBLE);
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
