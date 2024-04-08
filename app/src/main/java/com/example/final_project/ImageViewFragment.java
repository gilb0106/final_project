package com.example.final_project;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageViewFragment extends Fragment {

    private ImageView imageView;
    private ListView listView;

    public ImageViewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image_view, container, false);
        imageView = rootView.findViewById(R.id.imageView);

        // Retrieve the image URL from arguments
        String imageUrl = getArguments().getString("imageUrl");

        // Remove the "URL: " prefix from imageUrl
        imageUrl = imageUrl.replace("URL: ", "");

        // Load the image using AsyncTask
        new FetchImageTask(imageView).execute(imageUrl);
        setHasOptionsMenu(true);


        return rootView;
    }

    // Handle back arrow press
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Check if the back arrow is pressed
        if (item.getItemId() == android.R.id.home) {
            // Navigate back to SavedActivity
            requireActivity().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Enable ListView item click listener when fragment is destroyed
        listView.setEnabled(true);
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
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(imageView.getContext(), "Failed to load image",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
