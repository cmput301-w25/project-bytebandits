package com.github.bytebandits.bithub.view;

import android.content.pm.PackageManager;
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
import androidx.fragment.app.Fragment;
import com.github.bytebandits.bithub.controller.DatabaseManager;
import com.github.bytebandits.bithub.R;
import com.github.bytebandits.bithub.model.MoodPost;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapColorScheme;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.Manifest;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExploreFragment extends Fragment implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback {
    private final ArrayList<MoodPost> moodPosts = new ArrayList<>();
    private MoodPost latestMoodPost;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Marker currentLocationMarker                          ;
    private ArrayList<MoodPost> dataList;
    private ListView moodPostList;
    private MoodPostArrayAdapter moodPostAdapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final int REQUEST_LOCATION_PERMISSION = 1001;
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
        CollectionReference moodPostRef = DatabaseManager.getPostsCollectionRef();
        moodPostRef.addSnapshotListener((value, error) -> {
            if (error != null){
                Log.e("Firestore", error.toString());
            }
            if (value != null){
                Log.d("Firestore", "SnapshotListener triggered, updating dataList");
                dataList.clear();
                if (!value.isEmpty()) {
                    for (QueryDocumentSnapshot snapshot : value) {
                        snapshot.toObject(MoodPost.class);
                        dataList.add(snapshot.toObject(MoodPost.class));
                    }
                }
                if (moodPostAdapter != null) {
                    moodPostAdapter.notifyDataSetChanged();
                }
            }
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
                // Convert current location to a LatLng
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                for (MoodPost moodPost : dataList) {
                    LatLng moodPostLatLng = new LatLng(moodPost.getLatitude(), moodPost.getLongitude());
                    googleMap.addMarker(new MarkerOptions().position(moodPostLatLng).title(moodPost.getProfile().getUserID()));
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
            DatabaseManager.getInstance().getAllPosts(posts -> {
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

                    if (moodPostAdapter != null) {
                        moodPostAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("ExploreFragment", "moodPostAdapter is null");
                    }
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
}