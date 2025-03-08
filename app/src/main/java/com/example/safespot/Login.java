package com.example.safespot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Login extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button loginButton, registerButton; // Add buttons for login and register
    private TextView loginFailedText, checkDetailsText, enterFieldsText;
    private boolean isPasswordVisible = false;
    private ImageButton togglePasswordVisibilityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the user is already logged in
        SharedPreferences preferences = getSharedPreferences("SafeSpotPrefs", MODE_PRIVATE);
        int userId = preferences.getInt("userId", -1); // Default value is -1 if not found

        if (userId != -1) {
            // User is already logged in, redirect to Updates activity
            Intent intent = new Intent(Login.this, Updates.class);
            startActivity(intent);
            finish(); // Close the Login activity
            return;
        }

        // If user is not logged in, proceed with login UI setup
        setContentView(R.layout.activity_login);

        // Initialize views
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);
        loginFailedText = findViewById(R.id.loginFailed);
        checkDetailsText = findViewById(R.id.checkdetails);
        enterFieldsText = findViewById(R.id.enterfields);
        togglePasswordVisibilityButton = findViewById(R.id.toggle_password_visibility);

        // Set OnClickListener to toggle password visibility
        togglePasswordVisibilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // Hide password
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    passwordEditText.setSelection(passwordEditText.length()); // Maintain cursor position
                    togglePasswordVisibilityButton.setImageResource(R.drawable.ic_visibility); // Update icon
                } else {
                    // Show password
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    passwordEditText.setSelection(passwordEditText.length()); // Maintain cursor position
                    togglePasswordVisibilityButton.setImageResource(R.drawable.ic_visibility_off); // Update icon
                }
                isPasswordVisible = !isPasswordVisible; // Toggle state
            }
        });

        // Set OnClickListener for Login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    // Case 1: One or both fields are empty
                    showLoginFailedMessage("fields");
                } else {
                    // Case 2: Attempt login with provided credentials
                    loginUser(username, password);
                }
            }
        });

        // Set OnClickListener for Register button (navigate to Register activity)
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser(String username, String password) {
        // Enable logging to capture request and response details
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // Log request and response bodies

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)  // Add the interceptor to the client
                .build();

        // Retrofit setup with OkHttp client
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.40/SafeSpot/")
                .client(client)  // Use the custom OkHttpClient
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create API service
        LoginApi apiService = retrofit.create(LoginApi.class);

        // Create login request data object
        LoginData loginData = new LoginData(username, password);

        // Make the network request
        Call<LoginResponse> call = apiService.loginUser(loginData);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    // Log the parsed response for debugging
                    Log.d("LoginDebug", "Status: " + loginResponse.getStatus());
                    Log.d("LoginDebug", "Message: " + loginResponse.getMessage());
                    Log.d("LoginDebug", "UserId: " + loginResponse.getUserId());
                    Log.d("LoginDebug", "Username: " + username); // Add this log for debugging

                    if ("success".equals(loginResponse.getStatus())) {
                        // Save the user ID and username in SharedPreferences
                        getSharedPreferences("SafeSpotPrefs", MODE_PRIVATE)
                                .edit()
                                .putInt("userId", loginResponse.getUserId())
                                .putString("username", username) // Save the username
                                .apply();

                        // Successful login: Navigate to Updates activity
                        Intent intent = new Intent(Login.this, Updates.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Case 2: Incorrect login details
                        showLoginFailedMessage("details");
                    }
                } else {
                    Log.e("LoginDebug", "Error Response: " + response.message());
                    showLoginFailedMessage("details");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // Log network failure
                Log.e("Login", "Network Error: " + t.getMessage());
                Toast.makeText(Login.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoginFailedMessage(String type) {
        // Make all messages invisible initially
        loginFailedText.setVisibility(View.VISIBLE); // Always show "Login Failed" message
        checkDetailsText.setVisibility(View.INVISIBLE);
        enterFieldsText.setVisibility(View.INVISIBLE);

        if ("details".equals(type)) {
            checkDetailsText.setVisibility(View.VISIBLE); // Show check details message
        } else if ("fields".equals(type)) {
            enterFieldsText.setVisibility(View.VISIBLE); // Show enter fields message
        }
    }
}
