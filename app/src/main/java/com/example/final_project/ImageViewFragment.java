package com.example.final_project;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.File;

public class ImageViewFragment extends Fragment {
    private ImageView imageView;
    private SavedImage savedImage;
    private TextView textViewDate;
    private TextView textViewURL;
    private TextView textViewHDURL;
    private ListView listViewSavedImages;

    public ImageViewFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image_details, container, false);

        imageView = rootView.findViewById(R.id.imageView);
        textViewDate = rootView.findViewById(R.id.textViewDate);
        textViewURL = rootView.findViewById(R.id.textViewURL);
        textViewHDURL = rootView.findViewById(R.id.textViewHDURL);
        listViewSavedImages = getActivity().findViewById(R.id.listViewSavedImages);

        Bundle args = getArguments();
        if (args != null && args.containsKey("date") && args.containsKey("imageUrl") && args.containsKey("hdUrl")) {
            String date = args.getString("date");
            String imageUrl = args.getString("imageUrl");
            String hdUrl = args.getString("hdUrl");
            savedImage = new SavedImage(date, imageUrl, hdUrl);

            textViewDate.setText("Date: " + savedImage.getDate());
            textViewURL.setText("URL: " + savedImage.getImageUrl());
            textViewURL.setMovementMethod(LinkMovementMethod.getInstance());
            textViewHDURL.setText("HD URL: " + savedImage.getHdUrl());
            textViewHDURL.setMovementMethod(LinkMovementMethod.getInstance());

            loadImageFromStorage(savedImage.getDate());

            if (listViewSavedImages != null) {
                listViewSavedImages.setVisibility(View.GONE);
            }
        }

        return rootView;
    }

    private void loadImageFromStorage(String date) {
        try {
            ContextWrapper contextWrapper = new ContextWrapper(getActivity().getApplicationContext());
            File directory = contextWrapper.getDir("images", Context.MODE_PRIVATE);
            File filePath = new File(directory, date + ".jpg");
            Bitmap bitmap = BitmapFactory.decodeFile(filePath.getAbsolutePath());
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(getActivity(), "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

}
