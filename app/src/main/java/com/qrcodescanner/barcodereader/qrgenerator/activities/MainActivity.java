package com.qrcodescanner.barcodereader.qrgenerator.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.mlkit.vision.common.InputImage;
/*import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;*/
import com.qrcodescanner.barcodereader.qrgenerator.R;
import com.qrcodescanner.barcodereader.qrgenerator.adapters.SearchResultsRVAdapter;
import com.qrcodescanner.barcodereader.qrgenerator.models.dataModal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ImageView img;
    private Button snap, searchResultsBtn;
    private Bitmap imageBitmap;
    private RecyclerView resultRV;
    private FusedLocationProviderClient fusedLocationClient;
    private SearchResultsRVAdapter searchResultsRVAdapter;
    private ArrayList<dataModal> dataModalArrayList;
    private String title, link, displayed_link, snippet;
    ActivityResultLauncher<Intent> takeImageLauncher;
    private String currentLocationName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_search);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            checkLocationServicesEnabled();
        }

        // Initializing all our variables for views
        img = findViewById(R.id.image);
        snap = findViewById(R.id.snapbtn);
        searchResultsBtn = findViewById(R.id.idBtnSearchResuts);
        resultRV = findViewById(R.id.idRVSearchResults);

        // Initializing our array list
        dataModalArrayList = new ArrayList<>();

        // Initializing our adapter class
        searchResultsRVAdapter = new SearchResultsRVAdapter(dataModalArrayList, MainActivity.this);

        // Adding onClickListener for snap button
        snap.setOnClickListener(v -> {
            // Calling a method to capture an image
            dispatchTakePictureIntent();
        });

        // Adding onClickListener for search button
        searchResultsBtn.setOnClickListener(v -> {
            // Calling a method to get search results
//            getResults();
        });

        // Initialize takeImageLauncher
        takeImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                o -> {
                    if (o.getResultCode() == Activity.RESULT_OK) {
                        Intent data = o.getData();
                        if (data != null && data.getExtras() != null) {
                            imageBitmap = (Bitmap) data.getExtras().get("data");
                            img.setImageBitmap(imageBitmap);
                        }
                    }
                }
        );
    }
    private void checkLocationServicesEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Check if GPS and network location are enabled
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            // Prompt user to enable location services
            new AlertDialog.Builder(this)
                    .setTitle("Enable Location Services")
                    .setMessage("Location services are required for this feature. Please enable them in settings.")
                    .setPositiveButton("Settings", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, 200);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        } else {
            // Proceed to get location
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                if (addresses != null && !addresses.isEmpty()) {
                                    String locationName = addresses.get(0).getLocality() + "," + addresses.get(0).getCountryName();
                                    currentLocationName = locationName;
                                    Log.d("Location", "Current Location: " + locationName);
                                    // Proceed with your search query
                                    String searchQuery = "example search query"; // Use your search query here
                                    searchData(searchQuery, locationName);  // Pass both searchQuery and location
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            // Location is null, maybe because GPS or network is still off
                            Toast.makeText(MainActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Error while retrieving location", Toast.LENGTH_SHORT).show());
        }
    }

//    private void getResults() {
//        // Clear existing results
//        dataModalArrayList.clear();
//
//        if (imageBitmap == null) {
//            Toast.makeText(MainActivity.this, "Please capture an image first.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Get image labels
//        InputImage image = InputImage.fromBitmap(imageBitmap, 0);
//        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
//
//        labeler.process(image)
//                .addOnSuccessListener(labels -> {
//                    // Task completed successfully
//                    if (labels.isEmpty()) {
//                        Toast.makeText(MainActivity.this, "No labels detected in image.", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
//                    String searchQuery = labels.get(0).getText();
//                    if (!currentLocationName.isEmpty()) {
//                        searchData(searchQuery, currentLocationName);  // Pass both searchQuery and location
//                    } else {
//                        Toast.makeText(MainActivity.this, "Location not available. Please enable location services.", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    // Task failed with an exception
//                    Toast.makeText(MainActivity.this, "Failed to detect image.", Toast.LENGTH_SHORT).show();
//                });
//    }

    private void searchData(String searchQuery, String location) {
        String apiKey = "59e92c90f42f2776fef3b295be0f55705b9da098bd215ad8a27611b3dae929dd";
        String url = "https://serpapi.com/search.json?q=" + searchQuery.trim()
                + "&location=" + Uri.encode(location) + "&hl=en&gl=us&google_domain=google.com&api_key=" + apiKey;

        Log.d("API_URL", "Request URL: " + url);

        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        Log.d("API_RESPONSE", "Full Response: " + response.toString());
                        dataModalArrayList.clear();

                        JSONArray organicResultsArray = response.getJSONArray("organic_results");
                        for (int i = 0; i < organicResultsArray.length(); i++) {
                            JSONObject organicObj = organicResultsArray.getJSONObject(i);
                            String title = organicObj.optString("title", "N/A");
                            String link = organicObj.optString("link", "N/A");
                            String displayed_link = organicObj.optString("displayed_link", "N/A");
                            String snippet = organicObj.optString("snippet", "N/A");

                            Log.d("API_ITEM", "Title: " + title + ", Link: " + link);

                            dataModalArrayList.add(new dataModal(title, link, displayed_link, snippet));
                        }

                        LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
                        resultRV.setLayoutManager(manager);
                        resultRV.setAdapter(searchResultsRVAdapter);
                        searchResultsRVAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        Log.e("API_ERROR", "JSON Parsing error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("API_ERROR", "Volley error: " + error.toString());
                    Toast.makeText(MainActivity.this, "No results found for the search query.", Toast.LENGTH_SHORT).show();
                }
        );

        queue.add(jsonObjectRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkLocationServicesEnabled();
        } else {
            Toast.makeText(this, "Location permission is required for this feature.", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to capture image
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            takeImageLauncher.launch(takePictureIntent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200) {
            checkLocationServicesEnabled();  // Check if location services were enabled
        }
    }
}

