package com.example.final_project;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            buttonSave.setVisibility(View.GONE); // Hide save button initially
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            // Simulating a delay of 5 seconds
            for (int i = 0; i <= 100; i++) {
                try {
                    Thread.sleep(50); // Sleep for 50 milliseconds to simulate progress
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                publishProgress(i); // Publish the progress
            }

            String date = params[0];
            String apiKey = "DgPLcIlnmN0Cwrzcg3e9NraFaYLIDI68Ysc6Zh3d";
            String apiUrl = "https://api.nasa.gov/planetary/apod?api_key=" +
                    apiKey + "&date=" + date;
            HttpURLConnection connection = null;
            StringBuilder response = new StringBuilder();
            try {
                URL url = new URL(apiUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
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
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(values[0]); // Update the progress bar
        }
        @Override
        protected void onPostExecute(JSONObject jsonResponse) {
            super.onPostExecute(jsonResponse);
            progressBar.setVisibility(View.GONE);
            if (jsonResponse != null) {
                try {
                    String imageUrl = jsonResponse.getString("url");
                    String hdUrl = jsonResponse.getString("hdurl");
                    String date = jsonResponse.getString("date");
                    new LoadImageTask().execute(imageUrl);
                    textViewURL.setText("URL: " + imageUrl);
                    textViewDate.setText("Date: " + date);
                    textViewHDURL.setText("HDURL: " + hdUrl);
                    textViewHDURL.setVisibility(View.VISIBLE); // Show HD URL
                    // Make URLs clickable
                    textViewURL.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openURL(imageUrl);
                        }
                    });
                    textViewHDURL.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openURL(hdUrl);
                        }
                    });
                    // Show save button
                    buttonSave.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(SearchActivity.this, "Failed to parse response", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SearchActivity.this, "Failed to fetch image", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            String imageUrl = strings[0];
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
            } else {
                Toast.makeText(SearchActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void saveToDatabase() {
        // Access the image URL, HD URL, and other relevant information from your UI elements
        String imageUrl = textViewURL.getText().toString().substring(4);
        String hdUrl = textViewHDURL.getText().toString().substring(5);
        String date = textViewDate.getText().toString().substring(6);
        // Call the database insertion method
        DBConnect dbConnect = new DBConnect(this);
        long result = dbConnect.insertData(date, imageUrl, hdUrl);

        // Check the result of the insertion operation
        if (result != -1) {
            // Database insertion successful
            Toast.makeText(this, "Data saved to database successfully", Toast.LENGTH_SHORT).show();
        } else {
            // Database insertion failed
            Toast.makeText(this, "Failed to save data to database", Toast.LENGTH_SHORT).show();
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
    private void openURL(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }
}
