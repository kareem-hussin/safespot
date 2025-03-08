package com.example.safespot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ConnectedUsersAdapter extends BaseAdapter {
    private Context context;
    private List<CardUser> users;
    private int appUserId; // Store appUserId

    public ConnectedUsersAdapter(Context context, List<CardUser> users, int appUserId) {
        this.context = context;
        this.users = users;
        this.appUserId = appUserId; // Initialize appUserId
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private String formatDate(String date) {
        if (date == null || date.isEmpty()) {
            return "Unknown Birthdate";
        }
        try {
            // Parse the date string into a Date object
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date parsedDate = inputFormat.parse(date);

            // Format the Date object into the desired format
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy");
            return outputFormat.format(parsedDate);
        } catch (ParseException e) {
            Log.e("DateFormat", "Error formatting date", e);
            return "Unknown Birthdate";
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_connected_user, parent, false);
        }

        ImageView profilePicture = convertView.findViewById(R.id.profile_picture);
        TextView nameView = convertView.findViewById(R.id.user_name);
        TextView userBirthdate = convertView.findViewById(R.id.user_birthdate);
        TextView userAge = convertView.findViewById(R.id.user_age);
        Button disconnectButton = convertView.findViewById(R.id.disconnect_button);

        CardUser user = users.get(position);

        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String middleName = user.getMiddleName() != null && !user.getMiddleName().isEmpty()
                ? user.getMiddleName().substring(0, 1).toUpperCase() + "."
                : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";
        String fullName = String.format("%s %s %s", firstName, middleName, lastName).trim();
        nameView.setText(fullName.isEmpty() ? "Unknown Name" : fullName);

        String birthdate = user.getBirthdate() != null ? formatDate(user.getBirthdate()) : "Unknown Birthdate";

        // Create a SpannableString for "Birthdate: " with bold style
        SpannableString birthdateLabel = new SpannableString("Birthdate: ");
        birthdateLabel.setSpan(new StyleSpan(Typeface.BOLD), 0, birthdateLabel.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        userBirthdate.setText(birthdateLabel);
        userBirthdate.append(birthdate); // Append the formatted birthdate

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String age = calculateAge(user.getBirthdate());

            // Create a SpannableString for "Age: " with bold style
            SpannableString ageLabel = new SpannableString("Age: ");
            ageLabel.setSpan(new StyleSpan(Typeface.BOLD), 0, ageLabel.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            userAge.setText(ageLabel);
            userAge.append(age); // Append the value of the age
        }

        byte[] photoBlob = user.getPhotoBlob();
        if (photoBlob != null && photoBlob.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(photoBlob, 0, photoBlob.length);
            profilePicture.setImageBitmap(bitmap);
        } else {
            profilePicture.setImageResource(R.drawable.default_pfp);
        }

        disconnectButton.setOnClickListener(view -> {
            // Inflate the custom dialog layout
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_disconnect_user, null);

            // Initialize dialog views
            TextView titleView = dialogView.findViewById(R.id.disconnectDialogTitle);
            TextView messageView = dialogView.findViewById(R.id.disconnectDialogMessage);
            Button cancelButton = dialogView.findViewById(R.id.disconnectCancelButton);
            Button confirmButton = dialogView.findViewById(R.id.disconnectConfirmButton);

            // Create the dialog
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setView(dialogView)
                    .create();

            // Handle Cancel Button
            cancelButton.setOnClickListener(v -> dialog.dismiss());

            // Handle Confirm Button
            confirmButton.setOnClickListener(v -> {
                dialog.dismiss(); // Close dialog
                deleteLink(appUserId, user.getCardUserId(), position); // Disconnect user
            });

            // Show the dialog
            dialog.show();
        });

        return convertView;
    }

    private void deleteLink(int appUserId, String cardUserId, int position) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.40/SafeSpot/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DisconnectApi apiService = retrofit.create(DisconnectApi.class);

        Log.d("API Request", "app_user_id: " + appUserId + ", card_user_id: " + cardUserId);
        Call<GenericResponse> call = apiService.deleteUserCardLink(appUserId, cardUserId);

        call.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("APIResponse", "Success: " + response.body().getMessage());
                    Toast.makeText(context, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    users.remove(position);
                    notifyDataSetChanged();
                } else {
                    try {
                        if (response.errorBody() != null) {
                            String errorResponse = response.errorBody().string();
                            Log.e("APIResponse", "Error Response Body: " + errorResponse);
                        }
                    } catch (IOException e) {
                        Log.e("APIResponse", "Error reading error body", e);
                    }
                    Log.e("APIResponse", "Response Code: " + response.code());
                    Toast.makeText(context, "Failed to disconnect user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Log.e("NetworkError", "Error: " + t.getMessage(), t);
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String calculateAge(String birthdate) {
        if (birthdate == null || birthdate.isEmpty()) {
            return "Unknown";
        }
        try {
            LocalDate birthDate = LocalDate.parse(birthdate);
            LocalDate currentDate = LocalDate.now();
            return String.valueOf(Period.between(birthDate, currentDate).getYears());
        } catch (DateTimeParseException e) {
            Log.e("AgeCalculation", "Invalid birthdate format", e);
            return "Unknown";
        }
    }
}
