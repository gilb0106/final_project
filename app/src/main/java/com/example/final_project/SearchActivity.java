package com.example.final_project;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    private EditText editTextDate;
    private ImageView imageView;
    private TextView textViewURL;
    private TextView textViewDate;
    private TextView textViewHDURL;
    private Button buttonFetch;
    private Button buttonSave;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable back arrow

        editTextDate = findViewById(R.id.editTextDate);
        imageView = findViewById(R.id.imageView);
        textViewURL = findViewById(R.id.textViewURL);
        textViewDate = findViewById(R.id.textViewDate);
        textViewHDURL = findViewById(R.id.textViewHDURL);
        buttonFetch = findViewById(R.id.buttonFetch);
        buttonSave = findViewById(R.id.buttonSave);
        progressBar = findViewById(R.id.progressBar);

        buttonFetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToDatabase();
            }
        });
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        editTextDate.setText(dateFormat.format(calendar.getTime()));
                        fetchNASAImage(dateFormat.format(calendar.getTime()));
                    }
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void fetchNASAImage(String date) {
        FetchImageTask task = new FetchImageTask();
        task.execute(date);
    }

    private class FetchImageTask extends AsyncTask<String, Integer, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            String date = params[0];
            String apiKey = "KXhsciARcQGmkCjJgMcsQrlWKD9IrTdKhRnloLbo";
            String apiUrl = "https://api.nasa.gov/planetary/apod?api_key=" +
                    apiKey + "&date=" + date;
            HttpURLConnection connection = null;
            StringBuilder response = new StringBuilder();
            try {
                URL url = new URL(apiUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new
                        InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return new JSONObject(response.toString());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(JSONObject jsonResponse) {
            if (jsonResponse != null) {
                try {
                    String imageUrl = jsonResponse.getString("url");
                    String hdUrl = jsonResponse.has("hdurl") ? jsonResponse.getString("hdurl") : "";
                    String date = jsonResponse.getString("date");
                    textViewDate.setText("Date: " + date);
                    textViewURL.setText("URL: " + imageUrl);
                    textViewHDURL.setText("HDURL: " + hdUrl);
                    // Load image asynchronously
                    new AsyncTask<Void, Void, Bitmap>() {
                        @Override
                        protected Bitmap doInBackground(Void... voids) {
                            try {
                                URL url = new URL(imageUrl);
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setDoInput(true);
                                connection.connect();
                                InputStream input = connection.getInputStream();
                                return BitmapFactory.decodeStream(input);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Bitmap bitmap) {
                            super.onPostExecute(bitmap);
                            if (bitmap != null) {
                                // Display the bitmap
                                imageView.setImageBitmap(bitmap);
                                imageView.setVisibility(View.VISIBLE);

                                // Save the bitmap to the device if needed
                                saveBitmapToStorage(bitmap, date);
                            } else {
                                Toast.makeText(SearchActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }.execute();
                    textViewHDURL.setText("HDURL: " + hdUrl);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(SearchActivity.this, "Failed to parse response", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SearchActivity.this, "Failed to fetch image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveBitmapToStorage(final Bitmap bitmap, final String date) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String fileName = date + ".jpg";
                File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
                try (OutputStream outputStream = new FileOutputStream(file)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    return file.getAbsolutePath();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String filePath) {
                super.onPostExecute(filePath);
                if (filePath != null) {
                    Toast.makeText(SearchActivity.this, "Bitmap saved to " + filePath, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SearchActivity.this, "Failed to save bitmap", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }
    private void saveToDatabase() {
        // Access the image URL, HD URL, and other relevant information from your UI elements
        String imageUrl = textViewURL.getText().toString().substring(5);
        String hdUrl = textViewHDURL.getText().toString().substring(6);
        String date = textViewDate.getText().toString().substring(6);

        // Call the database insertion method
        DBConnect dbConnect = new DBConnect(SearchActivity.this);
        long result = dbConnect.insertData(date, imageUrl, hdUrl);

        // Check the result of the insertion operation
        if (result != -1) {
            // Database insertion successful
            Toast.makeText(SearchActivity.this, "Data saved to database successfully", Toast.LENGTH_SHORT).show();

            // Save the bitmap to the device
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();

            // Get the directory where the image was previously saved
            ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
            File directory = contextWrapper.getDir("images", Context.MODE_PRIVATE);
            File filePath = new File(directory, date + ".jpg");

            try {
                // Save bitmap to internal storage
                FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.close();
                Toast.makeText(this, R.string.savedsuccess,
                        Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.failfetch,
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Database insertion failed
            Toast.makeText(SearchActivity.this,
                    R.string.dbfail, Toast.LENGTH_SHORT).show();
        }
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
}
