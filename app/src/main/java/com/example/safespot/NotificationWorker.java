package com.example.safespot;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;
import java.util.Random;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NotificationWorker extends Worker {

    private static final String TAG = "NotificationWorker";

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Check if notification permissions are granted
        if (hasNotificationPermission()) {
            // Fetch notifications in the background
            fetchNotifications();
        } else {
            Log.w(TAG, "Notification permission not granted. Skipping notifications.");
        }

        // Indicate that the work finished successfully
        return Result.success();
    }

    private void fetchNotifications() {
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

        NotificationsApi apiService = retrofit.create(NotificationsApi.class);

        // Retrieve userId from SharedPreferences
        int userId = getApplicationContext()
                .getSharedPreferences("SafeSpotPrefs", Context.MODE_PRIVATE)
                .getInt("userId", -1);

        if (userId == -1) {
            Log.e(TAG, "User is not logged in. Skipping notifications.");
            return;
        }

        Call<List<com.example.safespot.Notification>> call = apiService.getNotifications(userId);
        try {
            Response<List<com.example.safespot.Notification>> response = call.execute();
            if (response.isSuccessful() && response.body() != null) {
                List<com.example.safespot.Notification> notifications = response.body();
                for (com.example.safespot.Notification notification : notifications) {
                    showNotification(notification.getMessage());
                }
            } else {
                Log.e(TAG, "Failed to fetch notifications: " + response.message());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching notifications", e);
        }
    }

    private void showNotification(String message) {
        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "SafeSpotChannel")
                .setSmallIcon(R.mipmap.ic_launcher) // Replace with your app's notification icon
                .setContentTitle("New Update from SafeSpot")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(new Random().nextInt(), builder.build());
    }

    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED;
        }
        return true; // Permission not required on older Android versions
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "SafeSpot Channel";
            String description = "Channel for SafeSpot notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("SafeSpotChannel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
