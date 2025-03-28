package com.github.bytebandits.bithub.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.github.bytebandits.bithub.controller.DatabaseManager;
import com.github.bytebandits.bithub.R;
import com.github.bytebandits.bithub.model.MoodMarker;
import com.github.bytebandits.bithub.model.MoodPost;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapColorScheme;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.ClusterRenderer;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import android.Manifest;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExploreFragment extends Fragment implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback {
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private ArrayList<MoodPost> dataList;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final int REQUEST_LOCATION_PERMISSION = 1001;

    private ClusterManager<MoodMarker> clusterManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        // Initialize dataList to avoid NullPointerException
        if (dataList == null) {
            dataList = new ArrayList<>();
            Log.d("ExploreFragment", "dataList initialized as empty list");
        } else {
            Log.d("ExploreFragment", "dataList already initialized with size: " + dataList.size());
        }
        // Listener so that dataList gets updated whenever the database does
        executor.execute(() -> {
                    DatabaseManager.getInstance().getAllPosts(posts -> {
                        if (posts != null) {
                            Log.e("ExploreFragment", "Error: posts is null");
                        }
                        mainHandler.post(() -> {
                            dataList.clear();
                            dataList.addAll(posts);

                        });
                        return null;
                    });
        });
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;
    }
    /**
     * Finds the exact MoodPost matching the marker's location
     * @param marker The marker that was clicked
     * @param posts List of posts to search through
     * @return The exact MoodPost matching the marker's location
     */
    private MoodPost findExactPost(Marker marker, List<MoodPost> posts) {
        if (posts == null || posts.isEmpty()) {
            return null;
        }

        for (MoodPost post : posts) {
            // Check if the marker's location exactly matches the post's location
            if (marker.getPosition().latitude == post.getLatitude() &&
                    marker.getPosition().longitude == post.getLongitude()) {
                return post;
            }
        }

        return null;
    }
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.setMapColorScheme(MapColorScheme.DARK);
        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }
        googleMap.setMyLocationEnabled(true);

        // Get and update to the current location
        fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), location -> {
            if (location != null && googleMap != null) {
                clusterManager = new ClusterManager<>(requireContext(), googleMap);
                myClusterRenderer myClusterRenderer = new myClusterRenderer(requireContext(), googleMap, clusterManager);
                clusterManager.setRenderer(myClusterRenderer);
                clusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener() {
                    @Override
                    public boolean onClusterClick(Cluster cluster) {
                        List moodPosts = new ArrayList<MoodMarker>();
                        for (Object moodMarker : cluster.getItems()) {
                            if (moodMarker instanceof MoodMarker){
                                moodPosts.add(((MoodMarker) moodMarker).getMoodPost());
                            }
                        }
                        showMoodPostsListDialog(moodPosts);

                        return true;
                    }

                });

                // Convert current location to a LatLng
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                for (MoodPost moodPost : dataList) {
                    if (moodPost.getLocation() == Boolean.TRUE) {

                        LatLng moodPostLatLng = new LatLng(moodPost.getLatitude(), moodPost.getLongitude());
                        Drawable drawable = ContextCompat.getDrawable(requireContext(), moodPost.getEmotion().getLogoID());
                        int desiredWidth = 240; // Adjust this value as needed
                        int desiredHeight = 240; // Adjust this value as needed

                        // Create a bitmap with the desired dimensions
                        Bitmap bitmap = Bitmap.createBitmap(desiredWidth, desiredHeight, Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);

                        // Draw the off-white rounded rectangle with shadow
                        Paint rectPaint = new Paint();
                        rectPaint.setColor(Color.parseColor("#D3D3D3")); // Light gray color
                        rectPaint.setAntiAlias(true);
                        rectPaint.setShadowLayer(10f, 0f, 5f, Color.BLACK); // Shadow effect
                        float cornerRadius = 40f; // Adjust corner radius as needed
                        float padding = 20f; // Padding around the icon
                        canvas.drawRoundRect(padding, padding, desiredWidth - padding, desiredHeight - padding, cornerRadius, cornerRadius, rectPaint);

                        float drawablePadding = 30f; // Padding specifically for the drawable
                        drawable.setBounds((int) (padding + drawablePadding), (int) (padding + drawablePadding), (int) (desiredWidth - padding - drawablePadding), (int) (desiredHeight - padding - drawablePadding - 40f)); // Leave space for text background
                        drawable.draw(canvas); // Draw the drawable on top of the rectangle

                        // Add a rounded black background for the text
                        Paint textBackgroundPaint = new Paint();
                        textBackgroundPaint.setColor(Color.WHITE); // White background for text
                        textBackgroundPaint.setAntiAlias(true);
                        float textBackgroundHeight = 50f; // Height of the text background
                        float textBackgroundTop = desiredHeight - textBackgroundHeight - padding;
                        canvas.drawRoundRect(padding + 10f, textBackgroundTop, desiredWidth - padding - 10f, textBackgroundTop + textBackgroundHeight, cornerRadius / 2, cornerRadius / 2, textBackgroundPaint);

                        // Add text to the bitmap
                        Paint textPaint = new Paint();
                        textPaint.setColor(Color.BLACK); // Set text color to black
                        textPaint.setTextSize(50f); // Set text size
                        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)); // Bold and italic
                        textPaint.setAntiAlias(true);
                        textPaint.setTextAlign(Paint.Align.CENTER);

                        // Calculate the position for the text with padding
                        float xPos = canvas.getWidth() / 2f;
                        float yPos = textBackgroundTop + textBackgroundHeight / 2f - ((textPaint.descent() + textPaint.ascent()) / 2f);

                        // Draw the text on the canvas after the drawable
                        String userId = "@" + moodPost.getProfile().getUserId();
                        canvas.drawText(userId, xPos, yPos, textPaint);
                        MoodMarker moodMarker = new MoodMarker(moodPostLatLng.latitude, moodPostLatLng.longitude, moodPost.getEmotion().getState(), userId, moodPost);
                        clusterManager.addItem(moodMarker);
                        googleMap.setOnCameraIdleListener(clusterManager);
                        googleMap.setOnMarkerClickListener(clusterManager);
                        clusterManager.cluster();
                        // googleMap.addMarker(new MarkerOptions().position(moodPostLatLng).title(moodPost.getProfile().getUserID()).icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                    }
                }
                // Move and zoom the camera to the user's location
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
            }
        });
        googleMap.setOnMarkerClickListener(this);

    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        executor.execute(() -> {
            DatabaseManager.getInstance().getAllPublicPosts(posts -> {
                if (posts == null) {
                    Log.e("ExploreFragment", "Error: posts is null");
                }

                Log.d("ExploreFragment", "Fetched posts count: " + posts.size());

                // Switch to UI thread for UI updates
                mainHandler.post(() -> {
                    dataList.clear();
                    dataList.addAll(posts);

                    MoodPost exactPost = findExactPost(marker, posts);
                    DetailedMoodPostFragment detailedMoodPostFragment =
                            DetailedMoodPostFragment.newInstance(exactPost);
                    detailedMoodPostFragment.show(getActivity().getSupportFragmentManager(), "Detailed Mood Post View");

                });
                return null;
            });
        });
        return false;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {

                    try {
                        googleMap.setMyLocationEnabled(true);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                    fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14));
                        }
                    });
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    private List getDisplayStrings(List moodPosts) {
        List displayStrings = new ArrayList<MoodPost>();
        for (Object post : moodPosts) {
// For example, display the userID and perhaps a snippet of content.
            if (post instanceof MoodPost) {
                displayStrings.add("@" + ((MoodPost) post).getProfile().getUserId());
            }
        }
        return displayStrings;
    }


    private void showMoodPostsListDialog(final List<MoodPost> moodPosts){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Mood Posts");
        ListView listView = new ListView(requireContext());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1,
                getDisplayStrings(moodPosts));

        listView.setAdapter(adapter);

// Handle item clicks in the list
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the corresponding MoodPost
                MoodPost selectedPost = moodPosts.get(position);

                // Open a detailed view dialog or Fragment.
                DetailedMoodPostFragment detailedMoodPostFragment =
                        DetailedMoodPostFragment.newInstance(selectedPost);
                detailedMoodPostFragment.show(getActivity().getSupportFragmentManager(), "DetailedMoodPost");
            }
        });

// Set the ListView as the dialog view
        builder.setView(listView);

// Optionally add a Cancel button.
        builder.setNegativeButton("Cancel", null);
        builder.show();

    }
}

