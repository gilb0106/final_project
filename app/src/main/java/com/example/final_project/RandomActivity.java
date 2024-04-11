package com.example.final_project;

import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RandomActivity extends AppCompatActivity {

    private TextView textViewRandomDate;
    private ImageView imageViewRandomImage;
    private TextView textViewRandomImageURL;
    private TextView textViewRandomHDImageURL;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.random);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textViewRandomDate = findViewById(R.id.textViewRandomDate);
        textViewRandomDate.setVisibility(View.INVISIBLE);
        imageViewRandomImage = findViewById(R.id.imageViewRandomImage);
        textViewRandomImageURL = findViewById(R.id.textViewRandomImageURL);
        textViewRandomImageURL.setVisibility(View.INVISIBLE);
        textViewRandomHDImageURL = findViewById(R.id.textViewRandomHDImageURL);
        textViewRandomHDImageURL.setVisibility(View.INVISIBLE);
        saveButton = findViewById(R.id.saveButton);
        saveButton.setVisibility(View.INVISIBLE);

        Button generateButton = findViewById(R.id.generateButton);
        generateButton.setOnClickListener(v -> generateRandomImage());
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
            builder.setMessage(getString(R.string.randomhelp))
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

//Note for this i had to limit to June 15 1995 to properly handle random range
    private void generateRandomImage() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(1995, Calendar.JUNE, 15); // Set minimum date to June 15, 1995
        long minDateMillis = calendar.getTimeInMillis(); // Minimum time
        long maxDateMillis = System.currentTimeMillis(); // Current time
        long randomDateMillis = (long) (Math.random()
                * (maxDateMillis - minDateMillis) + minDateMillis);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String randomDate = dateFormat.format(randomDateMillis);

        FetchImageTask task = new FetchImageTask();
        task.execute(randomDate);
    }

    private class FetchImageTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            String date = params[0];
            String apiKey = "KXhsciARcQGmkCjJgMcsQrlWKD9IrTdKhRnloLbo";
            String apiUrl = "https://api.nasa.gov/planetary/apod?api_key=" +
                    apiKey + "&date=" + date;
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                InputStream inputStream = connection.getInputStream();
                java.util.Scanner scanner = new java.util.Scanner(inputStream).useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                return new JSONObject(response);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonResponse) {
            if (jsonResponse != null) {
                try {
                    String imageUrl = jsonResponse.getString("url");
                    String hdUrl = jsonResponse.getString("hdurl");

                    textViewRandomDate.setText(getString(R.string.date) + jsonResponse.getString("date"));
                    textViewRandomDate.setVisibility(View.VISIBLE); // Set date visible after image is loaded
                    textViewRandomImageURL.setText(getString(R.string.url) + imageUrl);
                    textViewRandomImageURL.setVisibility(View.VISIBLE); // Set image URL visible after image is loaded
                    textViewRandomHDImageURL.setText(getString(R.string.hdurl) + hdUrl);
                    textViewRandomHDImageURL.setVisibility(View.VISIBLE); // Set HD image URL visible after image is loaded

                    new AsyncTask<String, Void, Bitmap>() {
                        @Override
                        protected Bitmap doInBackground(String... urls) {
                            String imageUrl = urls[0];
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
                                imageViewRandomImage.setImageBitmap(bitmap);
                                saveButton.setVisibility(View.VISIBLE); // Set save button visible after image is loaded
                            } else {
                                Toast.makeText(RandomActivity.this, R.string.fail,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }.execute(imageUrl);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(RandomActivity.this, R.string.fail,
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(RandomActivity.this, R.string.failfetch,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void saveImage(View view) {
        String date = textViewRandomDate.getText().
                toString().replace("Date: ", "");
        String imageUrl = textViewRandomImageURL.getText().
                toString().replace("Image URL: ", "");
        String hdImageUrl = textViewRandomHDImageURL.getText().
                toString().replace("HD Image URL: ", "");

        DBConnect dbConnect = new DBConnect(this);
        long result = dbConnect.insertData(date, imageUrl, hdImageUrl);
        if (result != -1) {
            Toast.makeText(this, R.string.savedsuccess, Toast.LENGTH_SHORT).show();
            BitmapDrawable drawable = (BitmapDrawable) imageViewRandomImage.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            try {
                ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
                File directory = contextWrapper.getDir("images", Context.MODE_PRIVATE);
                File filePath = new File(directory, date + ".jpg");
                FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.close();
                Toast.makeText(this, R.string.savedsuccess, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.fail, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.fail, Toast.LENGTH_SHORT).show();
        }
    }
}
