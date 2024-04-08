package com.example.final_project;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class SavedActivity extends AppCompatActivity {

    private ListView listViewSavedImages;
    private SavedImagesAdapter savedImagesAdapter;
    private List<SavedImage> savedImageList;
    private boolean isListViewClickable = true;

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
            if (isListViewClickable) {
                SavedImage clickedImage = savedImageList.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(SavedActivity.this);
                builder.setTitle("Options")
                        .setMessage("Do you wish to view the image or delete?")
                        .setPositiveButton("View Image", (dialog, which) -> {
                            // Launch fragment to view image
                            // Pass the URL to the fragment
                            String imageUrl = clickedImage.getImageUrl();
                            ImageViewFragment fragment = new ImageViewFragment();
                            Bundle args = new Bundle();
                            args.putString("imageUrl", imageUrl);
                            fragment.setArguments(args);
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit();

                        })
                        .setNegativeButton("Delete", (dialog, which) -> {
                            // Delete record from database
                            String imageUrl = clickedImage.getImageUrl();
                            DBConnect dbConnect = new DBConnect(SavedActivity.this);
                            dbConnect.deleteSavedImage(imageUrl);
                            // Remove item from the list
                            savedImageList.remove(position);
                            savedImagesAdapter.notifyDataSetChanged();

                            Snackbar snackbar = Snackbar.make(listViewSavedImages, "Image deleted", Snackbar.LENGTH_LONG);
                            snackbar.setAction("Undo", v -> {
                                // Undo delete action
                                dbConnect.undoDeleteImage(imageUrl);
                                // Add the item back to the list
                                savedImageList.add(position, clickedImage);
                                savedImagesAdapter.notifyDataSetChanged();
                            });
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorAccent));
                            View snackbarView = snackbar.getView();
                            Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbarView;
                            TextView textView = snackbarLayout.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setMaxLines(1); // Ensure that the message is displayed on a single line
                            snackbar.show();
                        })
                        .setNeutralButton("Cancel", null)
                        .show();
            }
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
                    String date = cursor.getString(cursor.getColumnIndex(DBConnect.COLUMN_IMAGE_DATE));
                    String imageUrl = cursor.getString(cursor.getColumnIndex(DBConnect.COLUMN_NASA_URL));
                    String hdUrl = cursor.getString(cursor.getColumnIndex(DBConnect.COLUMN_HD_URL));

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
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public static class ImageViewFragment extends Fragment {
        private ImageView imageView;

        public ImageViewFragment() {
            // Required empty public constructor
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View rootView = inflater.inflate(R.layout.fragment_image_view, container, false);
            imageView = rootView.findViewById(R.id.imageView);
            // Retrieve the image URL from arguments
            String imageUrl = getArguments().getString("imageUrl");
            // Load the image using AsyncTask or any other method
            new FetchImageTask(imageView).execute(imageUrl);
            return rootView;
        }
    }
    private static class FetchImageTask extends AsyncTask<String, Void, Bitmap> {

        private ImageView imageView;

        public FetchImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String imageUrl = params[0];
            Bitmap bitmap = null;
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap); // Set the downloaded image to the ImageView
                imageView.setVisibility(View.VISIBLE); // Make the ImageView visible
            } else {
                Toast.makeText(imageView.getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
