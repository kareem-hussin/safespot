package com.example.safespot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import kotlin.text.UStringsKt;

public class More extends AppCompatActivity {

    private ListView optionsList; // Declare optionsList at the class level
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_more);

        // Retrieve the user ID from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("SafeSpotPrefs", MODE_PRIVATE);
        userId = preferences.getInt("userId", -1);

        if (userId == -1) {
            Toast.makeText(this, "User not logged in. Redirecting to login...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
            finish(); // Close the current activity
            return;
        }


        // Initialize the optionsList
        optionsList = findViewById(R.id.options_list);

        View menuUpdates = findViewById(R.id.menuUpdates);
        View connectedMenu = findViewById(R.id.connectedMenu);

        menuUpdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the Updates Activity
                Intent intent = new Intent(More.this, Updates.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        connectedMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the Connected Activity
                Intent intent = new Intent(More.this, Connected.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        // Set up the options list and handle click events
        String[] options = {"Notification Settings", "About SafeSpot", "Support", "Feedback", "Log Out"};
        int[] icons = {
                R.drawable.notification, // Icon for Notification Settings
                R.drawable.about,        // Icon for About
                R.drawable.support,      // Icon for Support
                R.drawable.feedback,     // Icon for Feedback
                R.drawable.logout        // Icon for Log Out
        };

        // Set the custom adapter
        OptionsAdapter adapter = new OptionsAdapter(options, icons);
        optionsList.setAdapter(adapter);

        optionsList.setOnItemClickListener((parent, view, position, id) -> {
            switch (position) {
                case 0: // Notification Settings
                    showNotificationSettingsDialog();
                    break;
                case 1: // About
                    showAboutDialog();
                    break;
                case 2: // Support
                    showSupportDialog();
                    break;
                case 3: // Feedback
                    showFeedbackDialog();
                    break;
                case 4: // Log Out
                    showLogoutConfirmation();
                    break;
            }
        });
    }

    // Custom Adapter Class
    private class OptionsAdapter extends BaseAdapter {
        private final String[] options;
        private final int[] icons;

        public OptionsAdapter(String[] options, int[] icons) {
            this.options = options;
            this.icons = icons;
        }

        @Override
        public int getCount() {
            return options.length;
        }

        @Override
        public Object getItem(int position) {
            return options[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            }

            // Bind the data to the views
            ImageView icon = convertView.findViewById(R.id.list_item_icon);
            TextView text = convertView.findViewById(R.id.list_item_text);

            icon.setImageResource(icons[position]); // Set the icon
            text.setText(options[position]);        // Set the text

            return convertView;
        }
    }


    private void showNotificationSettingsDialog() {
        // Inflate the custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_notification_settings, null);

        // Initialize the views in the custom layout
        RadioGroup notificationOptionsGroup = dialogView.findViewById(R.id.notificationOptionsGroup);
        RadioButton optionOn = dialogView.findViewById(R.id.notificationOptionOn);
        RadioButton optionSilent = dialogView.findViewById(R.id.notificationOptionSilent);
        RadioButton optionOff = dialogView.findViewById(R.id.notificationOptionOff);
        Button saveButton = dialogView.findViewById(R.id.saveButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);

        // Define the color you want
        int color = getResources().getColor(R.color.black);

        // Apply the color programmatically
        optionOn.setButtonTintList(ColorStateList.valueOf(color));
        optionSilent.setButtonTintList(ColorStateList.valueOf(color));
        optionOff.setButtonTintList(ColorStateList.valueOf(color));

        // Retrieve the saved option from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("SafeSpotPrefs", MODE_PRIVATE);
        int savedOption = preferences.getInt("notificationOption", -1);

        // Pre-select the saved option
        if (savedOption == 0) {
            optionOn.setChecked(true);
        } else if (savedOption == 1) {
            optionSilent.setChecked(true);
        } else if (savedOption == 2) {
            optionOff.setChecked(true);
        }

        // Create the dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // Set listeners for the buttons
        saveButton.setOnClickListener(v -> {
            // Save the selected option
            int selectedOption = -1;
            if (optionOn.isChecked()) {
                selectedOption = 0; // On
            } else if (optionSilent.isChecked()) {
                selectedOption = 1; // Silent
            } else if (optionOff.isChecked()) {
                selectedOption = 2; // Off
            }

            // Store the selected option in SharedPreferences
            preferences.edit().putInt("notificationOption", selectedOption).apply();

            // Dismiss the dialog
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss()); // Close the dialog on cancel

        // Show the dialog
        dialog.show();
    }


    private void showAboutDialog() {
        // Inflate the custom layout for About
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_about, null);

        // Initialize the views in the custom layout
        TextView aboutTitle = dialogView.findViewById(R.id.aboutTitle);
        TextView aboutMessage = dialogView.findViewById(R.id.aboutMessage);
        Button closeButton = dialogView.findViewById(R.id.closeButton);

        // Set the title and message
        aboutTitle.setText("About SafeSpot");
        aboutMessage.setText("SafeSpot aims to address certain social issues by providing parents and guardians with location updates using cheap and accessible technology. SafeSpot was created by a group of passionate IT students with a desire to leave an impact through their creations and technology.");

        // Create the dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // Close button click listener
        closeButton.setOnClickListener(v -> dialog.dismiss());

        // Show the dialog
        dialog.show();
    }

    private void showSupportDialog() {
        // Inflate the custom layout for Support
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_support, null);

        // Initialize the views in the custom layout
        Button closeButton = dialogView.findViewById(R.id.supportCloseButton);


        // Create the dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // Close button click listener
        closeButton.setOnClickListener(v -> dialog.dismiss());

        // Show the dialog
        dialog.show();
    }


    private void showFeedbackDialog() {
        // Inflate the custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_feedback, null);

        // Initialize views from the custom layout
        EditText feedbackInput = dialogView.findViewById(R.id.feedbackInput);
        Button feedbackSubmit = dialogView.findViewById(R.id.submitButton);
        Button feedbackCancel = dialogView.findViewById(R.id.cancelButton);

        // Create the AlertDialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // Set listeners for the buttons
        feedbackSubmit.setOnClickListener(v -> {
            String feedback = feedbackInput.getText().toString();
            if (!feedback.isEmpty()) {
                sendFeedbackEmail(feedback); // Call the method to send feedback
                dialog.dismiss(); // Close the dialog
            } else {
                Toast.makeText(this, "Please enter your feedback before submitting.", Toast.LENGTH_SHORT).show();
            }
        });

        feedbackCancel.setOnClickListener(v -> dialog.dismiss()); // Close the dialog on cancel

        // Show the dialog
        dialog.show();
    }

    private void sendFeedbackEmail(String feedback) {
        // Replace with your recipient email address
        String recipientEmail = "itsafespot@gmail.com";

        // Retrieve the username from SharedPreferences or other storage
        SharedPreferences preferences = getSharedPreferences("SafeSpotPrefs", MODE_PRIVATE);
        String username = preferences.getString("username", "UnknownUser"); // Default to "UnknownUser" if not found

        // Construct the email subject with the username
        String subject = "SafeSpot Feedback - " + username;

        // Send email in a background thread to avoid blocking the UI
        // Show confirmation dialog on the main UI thread
        runOnUiThread(() -> {
            showFeedbackConfirmationDialog(); // Show confirmation popup
        });
        new Thread(() -> {
            try {
                EmailSender.sendEmail(recipientEmail, subject, feedback);
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Failed to send feedback: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }
        }).start();
    }

    private void showFeedbackConfirmationDialog() {
        // Inflate the custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_feedback_confirmation, null);

        // Initialize the close button
        Button closeButton = dialogView.findViewById(R.id.feedbackConfirmationCloseButton);

        // Create the AlertDialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // Set the click listener for the close button
        closeButton.setOnClickListener(v -> dialog.dismiss());

        // Show the dialog
        dialog.show();
    }


    private void showLogoutConfirmation() {
        // Inflate the custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_logout, null);

        // Initialize views from the custom layout
        Button yesButton = dialogView.findViewById(R.id.logoutDialogYesButton);
        Button cancelButton = dialogView.findViewById(R.id.logoutDialogCancelButton);

        // Create the AlertDialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // Set listeners for the buttons
        yesButton.setOnClickListener(v -> {
            // Clear the user ID from SharedPreferences
            getSharedPreferences("SafeSpotPrefs", MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();

            // Redirect to the Login activity
            Intent intent = new Intent(More.this, Login.class);
            startActivity(intent);
            finish(); // Close the current activity

            // Dismiss the dialog
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss()); // Close the dialog on cancel

        // Show the dialog
        dialog.show();
    }

}
