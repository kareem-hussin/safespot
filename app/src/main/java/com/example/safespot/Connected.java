package com.example.safespot;

import android.content.Intent;
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

import com.google.gson.GsonBuilder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Connected extends AppCompatActivity {

    private ImageView qrScannerButton;
    private int userId; // ID of the logged-in app user
    TextView connectionGuide;
    ListView connectedUsersList;
    ProgressBar loadingSpinner;
    private static final int SCANNER_REQUEST_CODE = 1001; // You can set any unique integer value



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_connected);

        View menuUpdates = findViewById(R.id.menuUpdates);
        View moreMenu = findViewById(R.id.moreMenu);
        connectionGuide = findViewById(R.id.connection_guide);
        connectedUsersList = findViewById(R.id.connected_users_list);
        loadingSpinner = findViewById(R.id.loading_spinner);


        menuUpdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the Login Activity
                Intent intent = new Intent(Connected.this, Updates.class);  // Assuming Login is the name of your login activity
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });



        moreMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the Login Activity
                Intent intent = new Intent(Connected.this, More.class);  // Assuming Login is the name of your login activity
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });


        // Initialize views
        qrScannerButton = findViewById(R.id.qrScannerButton); // Replace with the correct ID of your ImageView

        // Retrieve user ID from SharedPreferences
        userId = getSharedPreferences("SafeSpotPrefs", MODE_PRIVATE).getInt("userId", -1);

        if (userId == -1) {
            Toast.makeText(this, "User not logged in. Redirecting to login...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }
        // Set up QR scanner button
        qrScannerButton.setOnClickListener(v -> {
            Intent intent = new Intent(Connected.this, CustomScannerActivity.class);
            startActivityForResult(intent, SCANNER_REQUEST_CODE);
        });

        if (userId != -1) {
            fetchConnectedUsers();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SCANNER_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                String scannedId = data.getStringExtra("SCANNED_ID");
                if (scannedId != null) {
                    Log.d("Connected", "Scanned ID: " + scannedId);
                    addScannedIdToServer(scannedId); // Call your API function
                }
            }
        }
    }
    // Retrofit API Call
    private void addScannedIdToServer(String scannedId) {
        Log.d("AddScannedId", "User ID: " + userId + ", Scanned ID: " + scannedId);

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.40/SafeSpot/") // Update with your API URL
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AddQRApi apiService = retrofit.create(AddQRApi.class);

        Call<GenericResponse> call = apiService.addScannedId(userId, scannedId);

        call.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("APIResponse", "Success: " + response.body().getMessage());
                    Toast.makeText(Connected.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    // Refresh the list of connected users
                    fetchConnectedUsers();
                } else {
                    try {
                        if (response.errorBody() != null) {
                            String errorResponse = response.errorBody().string();
                            Log.e("APIResponse", "Error Response Body: " + errorResponse);
                        }
                    } catch (IOException e) {
                        Log.e("APIResponse", "Error reading error body", e);
                    }
                    Toast.makeText(Connected.this, "Failed to add scanned ID", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Log.e("NetworkError", "Error: " + t.getMessage(), t);
                Toast.makeText(Connected.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchConnectedUsers() {
        // Show the loading spinner and hide other views initially
        loadingSpinner.setVisibility(View.VISIBLE);
        connectionGuide.setVisibility(View.GONE);
        connectedUsersList.setVisibility(View.GONE);

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.40/SafeSpot/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(
                        new GsonBuilder().setLenient().create()
                ))
                .build();

        ConnectedApi apiService = retrofit.create(ConnectedApi.class);

        apiService.getConnectedUsers(userId).enqueue(new Callback<List<CardUser>>() {
            @Override
            public void onResponse(Call<List<CardUser>> call, Response<List<CardUser>> response) {
                // Hide the loading spinner
                loadingSpinner.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<CardUser> users = response.body();

                    if (users.isEmpty()) {
                        // No users found, show connection guide
                        connectionGuide.setVisibility(View.VISIBLE);
                    } else {
                        // Users found, show the list and hide the connection guide
                        connectionGuide.setVisibility(View.GONE);
                        connectedUsersList.setVisibility(View.VISIBLE);
                        populateListView(users);
                    }
                } else {
                    Log.e("FetchConnectedUsers", "No connected users found or invalid response.");
                    Toast.makeText(Connected.this, "No connected users found.", Toast.LENGTH_SHORT).show();

                    // Show connection guide in case of invalid response
                    connectionGuide.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<CardUser>> call, Throwable t) {
                // Hide the loading spinner
                loadingSpinner.setVisibility(View.GONE);

                Log.e("FetchConnectedUsers", "Failed to fetch connected users: " + t.getMessage(), t);
                Toast.makeText(Connected.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                // Show connection guide in case of error
                connectionGuide.setVisibility(View.VISIBLE);
            }
        });
    }


    private void populateListView(List<CardUser> users) {
        ConnectedUsersAdapter adapter = new ConnectedUsersAdapter(this, users, userId); // Pass userId as the third parameter
        ListView listView = findViewById(R.id.connected_users_list);
        listView.setAdapter(adapter);
    }
}