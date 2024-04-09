package com.example.final_project;

import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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
import java.io.File;
import java.io.FileOutputStream;
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
    private String userName;

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

        // Check if user's name is saved in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userName = sharedPreferences.getString("userName", "");

        if (userName.isEmpty()) {
            // If user's name is not saved, prompt the user to enter their name
            showNameDialog();
        } else {
            // If user's name is saved, ask the user if they want to keep it or enter a new one
            askUserToKeepOrEnterNew();
        }

        fetchNASAImage();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu); // For Toolbar
        return true;  // inflate menu_toolbar to display images out of overflow on top right of toolbar
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String message = null;
        int id = item.getItemId();
        if (id == R.id.Choice1) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        } else if (id == R.id.Choice2) {
            Intent intent = new Intent(this, SavedActivity.class);
            startActivity(intent);
        } //  if item from menu_toolbar selected, display applicable toast message
        return true;
    }
    private void askUserToKeepOrEnterNew() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Welcome back");
        builder.setMessage("Do you want to keep using the name '" + userName + "'?");

        // Set up the buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Personalize the greeting with the saved user's name
                textViewHeading.setText("Hello " + userName + " Welcome To NASA Image App");
            }
        });
        builder.setNegativeButton("No, enter a new name", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Prompt the user to enter a new name
                showNameDialog();
            }
        });
        builder.setCancelable(false); // Prevent dismissing the dialog by clicking outside
        builder.show();
    }

    private void showNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Welcome");
        builder.setMessage("Please enter your name to personalize the experience:");

        // Set up the input
        final EditText input = new EditText(this);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                userName = input.getText().toString();
                // Save user's name to SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("userName", userName);
                editor.apply();
                // Personalize the greeting
                textViewHeading.setText("Hello " + userName + " Welcome To NASA Image App");
            }
        });
        builder.setCancelable(false); // Prevent dismissing the dialog by clicking outside
        builder.show();
    }

    private void fetchNASAImage() { // Fetch todays date to send to async task
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
                                imageViewTodaysImage.setImageBitmap(bitmap);
                                imageViewTodaysImage.setVisibility(View.VISIBLE);

                                // Save the bitmap to the device if needed
                                // Code for saving image to device can be added here
                            } else {
                                Toast.makeText(MainActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }.execute();
                    textViewHDImageURL.setText("HDURL: " + hdUrl);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Failed to parse response", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Failed to fetch image", Toast.LENGTH_SHORT).show();
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
            finishAffinity(); // close app
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
            Toast.makeText(this, "Image details saved to database", Toast.LENGTH_SHORT).show();
            // Save the image to device
            BitmapDrawable drawable = (BitmapDrawable) imageViewTodaysImage.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            try {
                // Save bitmap to internal storage
                ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
                File directory = contextWrapper.getDir("images", Context.MODE_PRIVATE);
                File filePath = new File(directory, date + ".jpg");
                FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.close();
                // Provide feedback to the user
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
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}