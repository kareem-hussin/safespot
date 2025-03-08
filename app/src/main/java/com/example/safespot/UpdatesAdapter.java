package com.example.safespot;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class UpdatesAdapter extends BaseAdapter {
    private Context context;
    private List<Update> updates;

    public UpdatesAdapter(Context context, List<Update> updates) {
        this.context = context;
        this.updates = updates;
    }

    @Override
    public int getCount() {
        return updates.size();
    }

    @Override
    public Object getItem(int position) {
        return updates.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_update, parent, false);
        }

        TextView name = convertView.findViewById(R.id.name);
        TextView location = convertView.findViewById(R.id.location);
        TextView dateView = convertView.findViewById(R.id.date);
        TextView timeView = convertView.findViewById(R.id.time);
        ImageView profilePicture = convertView.findViewById(R.id.profile_picture);
        Button viewButton = convertView.findViewById(R.id.view_button); // Add a View button in your item layout

        Update update = updates.get(position);

        name.setText(update.getName());
        location.setText(update.getLocation());

        String timedate = update.getTimedate();
        if (timedate != null && !timedate.isEmpty()) {
            dateView.setText(formatDate(timedate));
            timeView.setText(convertToLocalTime(timedate));
        } else {
            dateView.setText("Unknown Date");
            timeView.setText("Unknown Time");
        }

        // Decode and set the photo
        String photoBlob = update.getPhotoBlob();
        if (photoBlob != null && !photoBlob.isEmpty()) {
            byte[] decodedString = Base64.decode(photoBlob, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            if (decodedByte != null) {
                profilePicture.setImageBitmap(decodedByte);
            } else {
                profilePicture.setImageResource(R.drawable.default_pfp);
            }
        } else {
            profilePicture.setImageResource(R.drawable.default_pfp); // Fallback to default image
        }

        // Handle View button click
        viewButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, MapViewActivity.class);
            intent.putExtra("name", update.getName());
            intent.putExtra("location", update.getLocation());
            intent.putExtra("timedate", update.getTimedate());

            String photoBlobForIntent = update.getPhotoBlob();
            if (photoBlobForIntent != null && !photoBlobForIntent.isEmpty()) {
                // Decode the Base64 string into a Bitmap
                byte[] decodedString = Base64.decode(photoBlobForIntent, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                if (decodedBitmap != null) {
                    // Compress and encode the image back to Base64
                    String compressedPhotoBlob = compressAndEncodeImage(decodedBitmap);
                    if (compressedPhotoBlob != null) {
                        intent.putExtra("profile_image", compressedPhotoBlob); // Pass the compressed image
                    }
                }
            }

            context.startActivity(intent);
        });

        return convertView;
    }

    private String compressAndEncodeImage(Bitmap bitmap) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Compress the image to 50% quality (you can adjust the value)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] byteArray = baos.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e("ImageCompression", "Error compressing image: " + e.getMessage());
            return null;
        }
    }

    // Helper method to format date into "January 01, 2005"
    private String formatDate(String dateTime) {
        try {
            // Parse the datetime string into a Date object
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date parsedDate = inputFormat.parse(dateTime);

            // Format the Date object into the desired date format
            SimpleDateFormat outputDateFormat = new SimpleDateFormat("MMMM dd, yyyy");
            return outputDateFormat.format(parsedDate);
        } catch (ParseException e) {
            Log.e("UpdatesAdapter", "Error formatting date", e);
            return "Unknown Date";
        }
    }

    public static String convertToLocalTime(String utcDateTime) {
        try {
            // Parse the UTC time
            SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = utcFormat.parse(utcDateTime);

            // Format to local time
            SimpleDateFormat localFormat = new SimpleDateFormat("h:mm a");
            localFormat.setTimeZone(TimeZone.getDefault());
            return localFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown Time";
        }
    }
}
