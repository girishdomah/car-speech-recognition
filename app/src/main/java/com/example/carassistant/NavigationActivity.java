package com.example.carassistant;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NavigationActivity extends AppCompatActivity {
    private EditText editTextLocation;
    private Button buttonDirections;
    private Button buttonSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        editTextLocation = findViewById(R.id.edit_text_location);
        buttonDirections = findViewById(R.id.button_directions);
        buttonSearch = findViewById(R.id.button_search);

        buttonDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = editTextLocation.getText().toString().trim();

                if (location.isEmpty()) {
                    Toast.makeText(NavigationActivity.this, "Enter a location", Toast.LENGTH_SHORT).show();
                    return;
                }

                showDirections(location);
            }
        });

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = editTextLocation.getText().toString().trim();

                if (location.isEmpty()) {
                    Toast.makeText(NavigationActivity.this, "Enter a location", Toast.LENGTH_SHORT).show();
                    return;
                }

                searchLocation(0, 0, location);
            }
        });
    }

    private void showDirections(String location) {
        try {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + location);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        } catch (ActivityNotFoundException e) {
            // Google Maps app is not installed
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    private void searchLocation(double lat, double lon, String location) {
        try {
            Uri gmmIntentUri = Uri.parse("geo:" + lat + "," + lon + "?q=" + location);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        } catch (ActivityNotFoundException e) {
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }
}