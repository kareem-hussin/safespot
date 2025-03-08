package com.example.safespot;

import android.content.Intent;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Register extends AppCompatActivity {

    private EditText usernameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private ImageButton togglePasswordVisibilityButton, toggleConfirmPasswordVisibilityButton;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirm_password);
        Button loginButton = findViewById(R.id.login_button);
        togglePasswordVisibilityButton = findViewById(R.id.toggle_password_visibility);
        toggleConfirmPasswordVisibilityButton = findViewById(R.id.toggle_confirm_password_visibility);

        findViewById(R.id.register_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateAndRegisterUser();

            }
        });

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

        // Set OnClickListener to toggle confirm password visibility
        toggleConfirmPasswordVisibilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConfirmPasswordVisible) {
                    // Hide confirm password
                    confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    confirmPasswordEditText.setSelection(confirmPasswordEditText.length()); // Maintain cursor position
                    toggleConfirmPasswordVisibilityButton.setImageResource(R.drawable.ic_visibility); // Update icon
                } else {
                    // Show confirm password
                    confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    confirmPasswordEditText.setSelection(confirmPasswordEditText.length()); // Maintain cursor position
                    toggleConfirmPasswordVisibilityButton.setImageResource(R.drawable.ic_visibility_off); // Update icon
                }
                isConfirmPasswordVisible = !isConfirmPasswordVisible; // Toggle state
            }
        });

        // Set OnClickListener for Login Button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the Login Activity
                Intent intent = new Intent(Register.this, Login.class);  // Assuming Login is the name of your login activity
                startActivity(intent);
            }
        });
    }

    private void registerUser(String username, String email, String password) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.40/SafeSpot/")  // Make sure the URL is correct
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegisterApi apiService = retrofit.create(RegisterApi.class);

        RegisterData registerData = new RegisterData(username, email, password);

        Call<RegisterResponse> call = apiService.registerUser(registerData);
        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful()) {
                    RegisterResponse registerResponse = response.body();
                    if (registerResponse != null && registerResponse.getStatus().equals("success")) {
                        Toast.makeText(Register.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Register.this, Login.class);
                        startActivity(intent);
                    } else if (registerResponse != null) {
                        handleErrors(registerResponse.getMessage());
                    } else {
                        Toast.makeText(Register.this, "Unexpected error occurred.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Register.this, "Registration failed. Try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Log.e("Register", "Network Failure", t);
                Toast.makeText(Register.this, "Network failure. Please check your connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleErrors(String errorMessage) {
        // Reset visibility of all error messages
        resetErrorMessages();

        // Handle username taken error
        if (errorMessage.contains("username_taken")) {
            ((TextView) findViewById(R.id.username_taken)).setText(R.string.username_taken);
            findViewById(R.id.username_taken).setVisibility(View.VISIBLE);
            findViewById(R.id.choose_name).setVisibility(View.VISIBLE);
        }

        // Handle email taken error
        if (errorMessage.contains("email_taken")) {
            ((TextView) findViewById(R.id.email_taken)).setText(R.string.email_taken);
            findViewById(R.id.email_taken).setVisibility(View.VISIBLE);
            findViewById(R.id.use_email).setVisibility(View.VISIBLE);
        }

        // Handle password mismatch error
        if (errorMessage.contains("password_mismatch")) {
            ((TextView) findViewById(R.id.password_match)).setText(R.string.password_match);
            findViewById(R.id.password_match).setVisibility(View.VISIBLE);
            findViewById(R.id.check_pass).setVisibility(View.VISIBLE);
        }

        // Fallback for unexpected errors
        if (!errorMessage.contains("username_taken") && !errorMessage.contains("email_taken") && !errorMessage.contains("password_mismatch")) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to reset all error messages
    private void resetErrorMessages() {
        findViewById(R.id.username_taken).setVisibility(View.INVISIBLE);
        findViewById(R.id.choose_name).setVisibility(View.INVISIBLE);
        findViewById(R.id.email_taken).setVisibility(View.INVISIBLE);
        findViewById(R.id.use_email).setVisibility(View.INVISIBLE);
        findViewById(R.id.password_match).setVisibility(View.INVISIBLE);
        findViewById(R.id.check_pass).setVisibility(View.INVISIBLE);
    }

    private void validateAndRegisterUser() {
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        // Reset visibility of all error messages
        findViewById(R.id.username_taken).setVisibility(View.INVISIBLE);
        findViewById(R.id.choose_name).setVisibility(View.INVISIBLE);
        findViewById(R.id.email_taken).setVisibility(View.INVISIBLE);
        findViewById(R.id.use_email).setVisibility(View.INVISIBLE);
        findViewById(R.id.password_match).setVisibility(View.INVISIBLE);
        findViewById(R.id.check_pass).setVisibility(View.INVISIBLE);

        // Check for empty fields
        if (username.isEmpty()) {
            ((TextView) findViewById(R.id.username_taken)).setText(R.string.empty_username);
            ((TextView) findViewById(R.id.choose_name)).setText(R.string.fill_fields);
            findViewById(R.id.username_taken).setVisibility(View.VISIBLE);
            findViewById(R.id.choose_name).setVisibility(View.VISIBLE);
            return;
        }
        if (email.isEmpty()) {
            ((TextView) findViewById(R.id.email_taken)).setText(R.string.empty_email);
            ((TextView) findViewById(R.id.use_email)).setText(R.string.fill_fields);
            findViewById(R.id.email_taken).setVisibility(View.VISIBLE);
            findViewById(R.id.use_email).setVisibility(View.VISIBLE);
            return;
        }
        if (password.isEmpty()) {
            ((TextView) findViewById(R.id.password_match)).setText(R.string.empty_password);
            ((TextView) findViewById(R.id.check_pass)).setText(R.string.fill_fields);
            findViewById(R.id.password_match).setVisibility(View.VISIBLE);
            findViewById(R.id.check_pass).setVisibility(View.VISIBLE);
            return;
        }
        if (confirmPassword.isEmpty()) {
            ((TextView) findViewById(R.id.password_match)).setText(R.string.empty_confirm_password);
            ((TextView) findViewById(R.id.check_pass)).setText(R.string.fill_fields);
            findViewById(R.id.password_match).setVisibility(View.VISIBLE);
            findViewById(R.id.check_pass).setVisibility(View.VISIBLE);
            return;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            findViewById(R.id.password_match).setVisibility(View.VISIBLE);
            findViewById(R.id.check_pass).setVisibility(View.VISIBLE);
            return;
        }

        // All fields are valid, proceed with registration
        registerUser(username, email, password);
    }


}