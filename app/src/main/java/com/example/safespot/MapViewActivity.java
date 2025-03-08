package com.example.safespot;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class MapViewActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        // Retrieve data from the Intent
        String name = getIntent().getStringExtra("name");
        String location = getIntent().getStringExtra("location");
        String timedate = getIntent().getStringExtra("timedate");
        String compressedPhotoBlob = getIntent().getStringExtra("profile_image"); // Base64 encoded image

        // Set details at the top of the activity
        View moreMenu = findViewById(R.id.moreMenu);
        View connectedMenu = findViewById(R.id.connectedMenu);
        TextView nameTextView = findViewById(R.id.name_text_view);
        TextView dateTextView = findViewById(R.id.date_text_view);
        TextView timeTextView = findViewById(R.id.time_text_view);
        ImageView backButton = findViewById(R.id.back_button);
        ImageView profileImageView = findViewById(R.id.profile_image_view);

        moreMenu.setOnClickListener(view -> {
            Intent intent = new Intent(MapViewActivity.this, More.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        connectedMenu.setOnClickListener(view -> {
            Intent intent = new Intent(MapViewActivity.this, Connected.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        nameTextView.setText(name);

        // Format and set the date and time
        try {
            // Parse the input date and time (assumes format "yyyy-MM-dd HH:mm:ss")
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US);
            java.util.Date parsedDate = inputFormat.parse(timedate);

            // Format the date to "October 21, 2025"
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.forLanguageTag("eng-PH"));
            dateTextView.setText(dateFormat.format(parsedDate));

            // Format the time to "8:18 AM"
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", java.util.Locale.forLanguageTag("eng-PH"));
            timeTextView.setText(timeFormat.format(parsedDate));
        } catch (Exception e) {
            e.printStackTrace();
            dateTextView.setText("Unknown Date");
            timeTextView.setText("Unknown Time");
        }

        backButton.setOnClickListener(v -> finish());

        // Decode and display the compressed image
        if (compressedPhotoBlob != null && !compressedPhotoBlob.isEmpty()) {
            byte[] decodedBytes = Base64.decode(compressedPhotoBlob, Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            if (decodedBitmap != null) {
                Bitmap circularBitmap = getCircularBitmap(decodedBitmap); // Crop to a circle
                profileImageView.setImageBitmap(circularBitmap);
            } else {
                profileImageView.setImageResource(R.drawable.default_pfp); // Fallback to default image
            }
        } else {
            profileImageView.setImageResource(R.drawable.default_pfp); // Fallback to default image
        }

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Example coordinates (should be replaced with real coordinates based on location)
        LatLng locationCoordinates = getCoordinatesFromAddress(getIntent().getStringExtra("location"));

        if (locationCoordinates != null) {
            mMap.addMarker(new MarkerOptions().position(locationCoordinates).title("Update Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationCoordinates, 15));
        }
    }

    private LatLng getCoordinatesFromAddress(String address) {
        if (address == null || address.isEmpty()) {
            return null; // Return null if the address is invalid
        }

        try {
            Geocoder geocoder = new Geocoder(this);
            List<Address> addressList = geocoder.getFromLocationName(address, 1);

            if (addressList != null && !addressList.isEmpty()) {
                Address location = addressList.get(0);
                return new LatLng(location.getLatitude(), location.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null; // Return null if no location is found
    }

    // Helper method to crop a bitmap into a circle
    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final RectF rect = new RectF(0, 0, size, size);

        paint.setAntiAlias(true);

        // Draw a circle
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(0xFFBAB399);
        canvas.drawCircle(size / 2, size / 2, size / 2, paint);

        // Draw the bitmap inside the circle
        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, null, rect, paint);

        return output;
    }
}
