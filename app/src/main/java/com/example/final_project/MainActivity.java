package com.example.final_project;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.final_project.DBConnect;
import com.example.final_project.R;
import com.example.final_project.SearchActivity;
import com.google.android.material.navigation.NavigationView;

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

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {
    private TextView textViewHeading;
    private TextView textViewTodaysDate;
    private ImageView imageViewTodaysImage;
    private TextView textViewImageURL;
    private TextView textViewHDImageURL;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        textViewHeading = findViewById(R.id.textViewHeading);
        textViewTodaysDate = findViewById(R.id.textViewTodaysDate);
        imageViewTodaysImage = findViewById(R.id.imageViewTodaysImage);
        textViewImageURL = findViewById(R.id.textViewImageURL);
        textViewHDImageURL = findViewById(R.id.textViewHDImageURL);

        fetchNASAImage();
    }
    private void fetchNASAImage() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",
                Locale.getDefault());
        String todayDate = dateFormat.format(calendar.getTime());

        FetchImageTask task = new FetchImageTask();
        task.execute(todayDate);
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
                    String hdUrl = jsonResponse.getString("hdurl");
                    String date = jsonResponse.getString("date");

                    textViewTodaysDate.setText(date);
                    textViewImageURL.setText("URL: " + imageUrl);
                    new LoadImageTask().execute(imageUrl);
                    textViewHDImageURL.setText("HDURL: " + hdUrl);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this,
                            "Failed to parse response", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this,
                        "Failed to fetch image", Toast.LENGTH_SHORT).show();
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
                imageViewTodaysImage.setImageBitmap(bitmap);
            } else {
                Toast.makeText(MainActivity.this,
                        "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.Home) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.searchImages) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        } else if (id == R.id.savedImages) {
            Intent intent = new Intent(this, SavedActivity.class);
            startActivity(intent);
        } else if (id == R.id.Exit) {
            // Handle the Exit action
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    public void saveToDatabase(View view) {
        String date = textViewTodaysDate.getText().toString();
        String imageUrl = textViewImageURL.getText().toString().substring(5);
        // Remove "URL: " prefix
        String hdImageUrl = textViewHDImageURL.getText().toString().substring(7);
        // Remove "HDURL: " prefix

        DBConnect dbConnect = new DBConnect(this);
        long result = dbConnect.insertData(date, imageUrl, hdImageUrl);
        if (result != -1) {
            Toast.makeText(this, "Image details saved to database",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to save image details",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
