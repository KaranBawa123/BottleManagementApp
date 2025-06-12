package com.hill.water.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.hill.water.R;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class AgentLocationMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    public GoogleMap googleMap;
    private List<LocationData> locations;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_location_map);

        locations = getIntent().getParcelableArrayListExtra("locations");

        if (locations != null) {
            Log.d("AgentLocationMapActivity", "Locations received: " + locations.size());
            for (LocationData location : locations) {
                Log.d("AgentLocationMapActivity", "Location: " + location.getPlaceName() +
                        " at LatLng: (" + location.getLatitude() + ", " + location.getLongitude() + ")");
            }
        } else {
            Log.d("AgentLocationMapActivity", "No locations received.");
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            initializeMap();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e("AgentLocationMapActivity", "Map fragment is null");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        Log.d("AgentLocationMapActivity", "Google Map is ready");

        if (locations != null && !locations.isEmpty()) {
            Log.d("AgentLocationMapActivity", "Adding markers for " + locations.size() + " locations");

            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            for (int i = 0; i < locations.size(); i++) {
                LocationData location = locations.get(i);
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String placeName = location.getPlaceName();

                if (latitude != 0 && longitude != 0 && !placeName.isEmpty()) {
                    LatLng latLng = new LatLng(latitude, longitude);
                    builder.include(latLng);

                    if (i == 0) {
                        addTextLabel(latLng, "START", googleMap);
                    } else if (i == locations.size() - 1) {
                        addTextLabel(latLng, "END", googleMap);
                    } else {
                        googleMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(placeName)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    }

                    if (i < locations.size() - 1) {
                        LatLng nextLatLng = new LatLng(locations.get(i + 1).getLatitude(), locations.get(i + 1).getLongitude());
                        getRouteFromAPI(latLng, nextLatLng);
                    }
                }
            }

            LatLngBounds bounds = builder.build();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));

        } else {
            Log.d("AgentLocationMapActivity", "No locations to display.");
            Toast.makeText(this, "No locations available to display", Toast.LENGTH_SHORT).show();
        }
    }

    private void addTextLabel(LatLng position, String text, GoogleMap map) {

        Bitmap textBitmap = createTextBitmap(text);

        LatLng labelPosition = new LatLng(position.latitude + 0.0005, position.longitude);

        GroundOverlayOptions overlayOptions = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromBitmap(textBitmap))
                .position(labelPosition, 150f)
                .transparency(0f);

        map.addGroundOverlay(overlayOptions);
    }

    private Bitmap createTextBitmap(String text) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setTextSize(20);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.LEFT);

        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        Bitmap bitmap = Bitmap.createBitmap(bounds.width() + 20, bounds.height() + 20, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(text, 10, bounds.height(), paint);

        return bitmap;
    }

    private void getRouteFromAPI(LatLng origin, LatLng destination) {
        String apiKey = "AIzaSyA1dQ3nLf0HDkiolHRz8wkOwKdEC5kD1XY";
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + origin.latitude + "," + origin.longitude +
                "&destination=" + destination.latitude + "," + destination.longitude +
                "&mode=driving&key=" + apiKey;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray routes = response.getJSONArray("routes");
                        if (routes.length() > 0) {
                            JSONObject route = routes.getJSONObject(0);
                            JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                            String encodedPolyline = overviewPolyline.getString("points");

                            List<LatLng> decodedPath = PolyUtil.decode(encodedPolyline);
                            googleMap.addPolyline(new PolylineOptions()
                                    .addAll(decodedPath)
                                    .color(Color.parseColor("#2196F3"))
                                    .width(10)
                                    .pattern(Arrays.asList(new Dot(), new Gap(10)))
                            );
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("AgentLocationMapActivity", "Error parsing route JSON: " + e.getMessage());
                    }
                },
                error -> Log.e("AgentLocationMapActivity", "Error fetching route: " + error.getMessage())
        );

        queue.add(request);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeMap();
            } else {
                Toast.makeText(this, "Location permission is required to view the map.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}