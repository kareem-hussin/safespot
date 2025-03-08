package com.example.safespot;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Updates extends AppCompatActivity {
    private static final int REQUEST_POST_NOTIFICATIONS = 1;
    private int userId; // To store the logged-in user ID
    private ProgressBar progressBar;
    private TextView connectionGuide; // TextView to display connection guide
    private ListView updatesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_updates);

        // Schedule background notifications
        scheduleNotificationWorker();

        View moreMenu = findViewById(R.id.moreMenu);
        View connectedMenu = findViewById(R.id.connectedMenu);
        progressBar = findViewById(R.id.progressBar); // ProgressBar
        connectionGuide = findViewById(R.id.updates_guide); // Connection guide TextView
        updatesListView = findViewById(R.id.updates_list); // ListView for updates
        ImageView refreshIcon = findViewById(R.id.refreshIcon);

        // Set click listener on the refresh icon
        refreshIcon.setOnClickListener(view -> fetchUpdates());

        moreMenu.setOnClickListener(view -> {
            Intent intent = new Intent(Updates.this, More.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        connectedMenu.setOnClickListener(view -> {
            Intent intent = new Intent(Updates.this, Connected.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        // Retrieve userId from SharedPreferences
        userId = getSharedPreferences("SafeSpotPrefs", MODE_PRIVATE).getInt("userId", -1);

        if (userId == -1) {
            Toast.makeText(this, "User not logged in. Redirecting to login...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        // Check if the permission is required and not already granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            final String POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS"; // Define the permission string
            if (ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Request the permission
                ActivityCompat.requestPermissions(this, new String[]{POST_NOTIFICATIONS}, REQUEST_POST_NOTIFICATIONS);
            }
        }

        // Initial fetch
        fetchUpdates();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Log.d("Permissions", "POST_NOTIFICATIONS permission granted.");
            } else {
                // Permission denied
                Log.d("Permissions", "POST_NOTIFICATIONS permission denied.");
            }
        }
    }

    private void scheduleNotificationWorker() {
        PeriodicWorkRequest notificationWorkRequest =
                new PeriodicWorkRequest.Builder(NotificationWorker.class, 15, TimeUnit.MINUTES) // Minimum interval is 15 minutes
                        .build();

        WorkManager.getInstance(this).enqueue(notificationWorkRequest);
    }

    private void fetchUpdates() {
        // Show the ProgressBar and hide the ListView and connection guide initially
        progressBar.setVisibility(View.VISIBLE);
        updatesListView.setVisibility(View.GONE);
        connectionGuide.setVisibility(View.GONE);

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.40/SafeSpot/") // Replace with your actual base URL
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        UpdatesApi apiService = retrofit.create(UpdatesApi.class);

        apiService.getUpdates(userId).enqueue(new Callback<List<Update>>() {
            @Override
            public void onResponse(Call<List<Update>> call, Response<List<Update>> response) {
                progressBar.setVisibility(View.GONE); // Hide ProgressBar
                if (response.isSuccessful() && response.body() != null) {
                    List<Update> updates = response.body();

                    // Sort updates by timedate in descending order
                    updates.sort((update1, update2) -> update2.getTimedate().compareTo(update1.getTimedate()));

                    if (updates.isEmpty()) {
                        connectionGuide.setVisibility(View.VISIBLE); // Show connection guide if no updates
                    } else {
                        updatesListView.setVisibility(View.VISIBLE); // Show ListView if updates are fetched
                        populateListView(updates);
                    }
                } else {
                    connectionGuide.setVisibility(View.VISIBLE); // Show connection guide if no updates
                    try {
                        if (response.errorBody() != null) {
                            String errorResponse = response.errorBody().string();
                            Log.e("APIResponse", "Error Response Body: " + errorResponse);
                        }
                    } catch (IOException e) {
                        Log.e("APIResponse", "Error reading error body", e);
                    }
                    Toast.makeText(Updates.this, "No updates found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Update>> call, Throwable t) {
                progressBar.setVisibility(View.GONE); // Hide ProgressBar
                connectionGuide.setVisibility(View.VISIBLE); // Show connection guide on failure
                Log.e("FetchUpdates", "Failed to fetch updates: " + t.getMessage(), t);
                Toast.makeText(Updates.this, "Network Error, Check your Connection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateListView(List<Update> updates) {
        UpdatesAdapter adapter = new UpdatesAdapter(this, updates);
        updatesListView.setAdapter(adapter);
    }

    // Helper method to format date into "January 01, 2005"
    public static String formatDate(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return "Unknown Date";
        }
        try {
            // Parse the datetime string into a Date object
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date parsedDate = inputFormat.parse(dateTime);

            // Format the Date object into the desired date format
            SimpleDateFormat outputDateFormat = new SimpleDateFormat("MMMM dd, yyyy");
            return outputDateFormat.format(parsedDate);
        } catch (ParseException e) {
            Log.e("DateFormat", "Error formatting date", e);
            return "Unknown Date";
        }
    }

    // Helper method to format time into "9:25 AM"
    public static String formatTime(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return "Unknown Time";
        }
        try {
            // Parse the datetime string into a Date object
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date parsedDate = inputFormat.parse(dateTime);

            // Format the Date object into the desired time format
            SimpleDateFormat outputTimeFormat = new SimpleDateFormat("h:mm a");
            return outputTimeFormat.format(parsedDate);
        } catch (ParseException e) {
            Log.e("TimeFormat", "Error formatting time", e);
            return "Unknown Time";
        }
    }
}
