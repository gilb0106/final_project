package com.example.final_project;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
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
        getSupportActionBar().setTitle(R.string.searchimg);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable back arrow

        editTextDate = findViewById(R.id.editTextDate);
        imageView = findViewById(R.id.imageView);
        textViewURL = findViewById(R.id.textViewURL);
        textViewDate = findViewById(R.id.textViewDate);
        textViewHDURL = findViewById(R.id.textViewHDURL);
        buttonFetch = findViewById(R.id.buttonFetch);
        buttonSave = findViewById(R.id.buttonSave);
        progressBar = findViewById(R.id.progressBar);

        buttonFetch.setOnClickListener(v -> {
            if (editTextDate.getText().toString().isEmpty()) {
                showDatePicker(); // If editTextDate is empty, show date picker
            } else {
                String manuallyEnteredDate = editTextDate.getText().toString();
                new FetchDataTask().execute(manuallyEnteredDate); // Fetch image for manually entered date
            }
        });

        buttonSave.setOnClickListener(v -> saveToDatabase());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu); // For Toolbar
        return true;  // inflate menu_toolbar to display images out of overflow on top right of toolbar
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.Choice1) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        } else if (id == R.id.Choice2) {
            Intent intent = new Intent(this, SavedActivity.class);
            startActivity(intent);
        } else if (id == R.id.Choice3) {
            Intent intent = new Intent(this, RandomActivity.class);
            startActivity(intent);
        } else if (id == R.id.Choice4) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.searchhelp))
                    .setPositiveButton("OK", (dialog, id1) -> {
                        // User clicked OK button, dismiss the dialog
                        dialog.dismiss();
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year1);
                    calendar.set(Calendar.MONTH, monthOfYear);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    SimpleDateFormat dateFormat =
                            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    editTextDate.setText(dateFormat.format(calendar.getTime()));
                    new FetchDataTask().execute(dateFormat.format(calendar.getTime()));
                },
                year, month, day);
        datePickerDialog.show();
    }

    private class FetchDataTask extends AsyncTask<String, Integer, JSONObject> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            buttonFetch.setEnabled(false); // Disable button during loading
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            String date = params[0];
            String apiKey = "KXhsciARcQGmkCjJgMcsQrlWKD9IrTdKhRnloLbo";
            String apiUrl = "https://api.nasa.gov/planetary/apod?api_key=" + apiKey + "&date=" + date;

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

                // Introduce a delay for demonstration purposes
                for (int i = 0; i < 3; i++) {
                    Thread.sleep(1000); // Sleep for 1 second
                    publishProgress((i + 1) * 33); // Update progress
                }

                return new JSONObject(response.toString());
            } catch (IOException | JSONException | InterruptedException e) {
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
            // Update progress bar
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(JSONObject jsonResponse) {
            progressBar.setVisibility(View.GONE);
            buttonFetch.setEnabled(true); // Re-enable button
            if (jsonResponse != null) {
                try {
                    String imageUrl = jsonResponse.getString("url");
                    String hdUrl = jsonResponse.optString("hdurl", "");
                    String date = jsonResponse.getString("date");
                    textViewDate.setText(getString(R.string.date) + date);
                    textViewURL.setText("URL: " + imageUrl);
                    textViewHDURL.setText("HDURL: " + hdUrl);

                    // Load image and save to database
                    new AsyncTask<Void, Void, Bitmap>() {
                        @Override
                        protected Bitmap doInBackground(Void... voids) {
                            Bitmap bitmap = null;
                            try {
                                URL url = new URL(imageUrl);
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setDoInput(true);
                                connection.connect();
                                bitmap = BitmapFactory.decodeStream(connection.getInputStream());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return bitmap;
                        }

                        @Override
                        protected void onPostExecute(Bitmap bitmap) {
                            if (bitmap != null) {
                                // Display the bitmap
                                imageView.setImageBitmap(bitmap);
                                imageView.setVisibility(View.VISIBLE);

                                // Save to database
                                saveToDatabase();
                            } else {
                                Toast.makeText(SearchActivity.this,
                                        R.string.fail, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }.execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(SearchActivity.this, R.string.failfetch, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SearchActivity.this, R.string.failfetch, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveToDatabase() {
        // Access the image URL, HD URL
        String imageUrl = textViewURL.getText().toString().substring(5);
        String hdUrl = textViewHDURL.getText().toString().substring(6);
        String date = textViewDate.getText().toString().substring(5);

        // Call the database insertion method
        DBConnect dbConnect = new DBConnect(SearchActivity.this);
        long result = dbConnect.insertData(date, imageUrl, hdUrl);

        // Check the result of the insertion operation
        if (result != -1) .
            // Database insertion successful
            Toast.makeText(SearchActivity.this, R.string.savedsuccess, Toast.LENGTH_SHORT).show();

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
}
