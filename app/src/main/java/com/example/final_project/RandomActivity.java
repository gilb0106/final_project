package com.example.final_project;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
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
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateRandomImage();
            }
        });
    }

    private void generateRandomImage() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(1995, Calendar.JUNE, 15); // Set minimum date to June 15, 1995
        long minDateMillis = calendar.getTimeInMillis(); // Minimum time
        long maxDateMillis = System.currentTimeMillis(); // Current time
        long randomDateMillis = (long) (Math.random() * (maxDateMillis - minDateMillis) + minDateMillis);

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

                    textViewRandomDate.setText("Date: " + jsonResponse.getString("date"));
                    textViewRandomDate.setVisibility(View.VISIBLE); // Set date visible after image is loaded
                    textViewRandomImageURL.setText("Image URL: " + imageUrl);
                    textViewRandomImageURL.setVisibility(View.VISIBLE); // Set image URL visible after image is loaded
                    textViewRandomHDImageURL.setText("HD Image URL: " + hdUrl);
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
                                Toast.makeText(RandomActivity.this, "Failed to load image",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }.execute(imageUrl);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(RandomActivity.this, "Failed to parse response",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(RandomActivity.this, "Failed to fetch image",
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
            Toast.makeText(this, "Image details saved to database", Toast.LENGTH_SHORT).show();
            BitmapDrawable drawable = (BitmapDrawable) imageViewRandomImage.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            try {
                ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
                File directory = contextWrapper.getDir("images", Context.MODE_PRIVATE);
                File filePath = new File(directory, date + ".jpg");
                FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.close();
                Toast.makeText(this, "Image saved to device", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to save image details", Toast.LENGTH_SHORT).show();
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
