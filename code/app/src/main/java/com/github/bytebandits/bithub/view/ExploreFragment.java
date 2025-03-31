package com.github.bytebandits.bithub.view;

import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Location;
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
import com.github.bytebandits.bithub.controller.SessionManager;
import com.github.bytebandits.bithub.model.MoodMarker;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.maps.android.clustering.ClusterManager;

import android.Manifest;
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

    private ListenerRegistration postsListener;

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
        
        setupPostsRealTimeListener();
        
        // Listener so that dataList gets updated whenever the database does
        executor.execute(() -> {
            DatabaseManager.getInstance().getAllFollowerPosts(SessionManager.getInstance(requireContext()).getProfile().getUserId(),posts -> {
                if (posts != null) {
                    Log.e("ExploreFragment", "Error: posts is null");
                }
                mainHandler.post(() -> {
                    dataList.clear();
                    dataList.addAll(posts);

                });
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

    private MoodPost findExactPost(MoodMarker moodMarker, List<MoodPost> posts) {
        if (posts == null || posts.isEmpty()) {
            return null;
        }
        for (MoodPost post : posts) {
            if (moodMarker.getPosition().latitude == post.getLatitude() &&
                    moodMarker.getPosition().longitude == post.getLongitude()) {
                return post;
            }
        }
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove Firestore listener to prevent memory leaks
        if (postsListener != null) {
            postsListener.remove();
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        executor.execute(() -> {
            DatabaseManager.getInstance().getAllFollowerPosts(SessionManager.getInstance(requireContext()).getUserId(),posts -> {
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

    private LatLng currentUserLocation;
    private static final double MAX_DISTANCE_KM = 5.0; // 5 km radius

    private void setupPostsRealTimeListener() {
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        CollectionReference postsRef = databaseManager.getPostsCollectionRef();

        postsListener = postsRef.addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                Log.w("ExploreFragment", "Listen failed.", e);
                return;
            }

            if (querySnapshot != null) {
                // Fetch all public posts
                databaseManager.getAllFollowerPosts(SessionManager.getInstance(requireContext()).getUserId(),posts -> {
                    mainHandler.post(() -> {
                        // Clear existing data and markers
                        dataList.clear();
                        if (clusterManager != null) {
                            clusterManager.clearItems();
                        }

                        // Add new posts within 5km
                        if (posts != null && !posts.isEmpty()) {
                            filterPostsByDistance(posts);

                            // Rerender markers if map is ready
                            if (googleMap != null) {
                                renderMapMarkers();
                            }
                        }
                    });
                });
            }
        });
    }

    private void filterPostsByDistance(List<MoodPost> posts) {
        if (currentUserLocation == null) return;

        dataList.clear();
        for (MoodPost post : posts) {
            if (Boolean.TRUE.equals(post.getLocation())) {
                LatLng postLocation = new LatLng(post.getLatitude(), post.getLongitude());
                double distance = calculateDistance(currentUserLocation, postLocation);

                if (distance <= MAX_DISTANCE_KM) {
                    dataList.add(post);
                }
            }
        }
    }

    private double calculateDistance(LatLng point1, LatLng point2) {
        float[] results = new float[1];
        Location.distanceBetween(
                point1.latitude, point1.longitude,
                point2.latitude, point2.longitude,
                results
        );
        // Convert meters to kilometers
        return results[0] / 1000.0;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.setMapColorScheme(MapColorScheme.DARK);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }
        googleMap.setMyLocationEnabled(true);

        // ... existing permission checks ...

        fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), location -> {
            if (location != null && googleMap != null) {
                // Store current user location
                currentUserLocation = new LatLng(location.getLatitude(), location.getLongitude());

                clusterManager = new ClusterManager<>(requireContext(), googleMap);
                myClusterRenderer myClusterRenderer = new myClusterRenderer(requireContext(), googleMap, clusterManager);
                clusterManager.setRenderer(myClusterRenderer);

                // Update cluster click listener to use filtered posts
                clusterManager.setOnClusterClickListener(cluster -> {
                    List<MoodPost> clusterPosts = new ArrayList<>();
                    for (Object moodMarker : cluster.getItems()) {
                        if (moodMarker instanceof MoodMarker) {
                            clusterPosts.add(((MoodMarker) moodMarker).getMoodPost());
                        }
                    }
                    showMoodPostsListDialog(clusterPosts);
                    return true;
                });

                // Update cluster item click listener
                clusterManager.setOnClusterItemClickListener(moodMarker -> {
                    executor.execute(() -> {
                        DatabaseManager.getInstance().getAllFollowerPosts(SessionManager.getInstance(requireContext()).getUserId(), posts -> {
                            mainHandler.post(() -> {
                                // Filter posts by distance
                                List<MoodPost> filteredPosts = new ArrayList<>();
                                for (MoodPost post : posts) {
                                    if (post.getLocation()) {
                                        LatLng postLocation = new LatLng(post.getLatitude(), post.getLongitude());
                                        double distance = calculateDistance(currentUserLocation, postLocation);

                                        if (distance <= MAX_DISTANCE_KM) {
                                            filteredPosts.add(post);
                                        }
                                    }
                                }

                                MoodPost exactPost = findExactPost(moodMarker, filteredPosts);
                                DetailedMoodPostFragment detailedMoodPostFragment =
                                        DetailedMoodPostFragment.newInstance(exactPost);
                                detailedMoodPostFragment.show(
                                        getActivity().getSupportFragmentManager(),
                                        "Detailed Mood Post View"
                                );
                            });
                        });
                    });
                    return false;
                });

                // Render markers within 5km
                filterPostsByDistance(dataList);
                renderMapMarkers();

                // Move camera to current location
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 15));
            }
        });
    }

    private void renderMapMarkers() {
        if (clusterManager == null || googleMap == null) return;

        // Clear existing items
        clusterManager.clearItems();

        // Add only posts within 5km
        for (MoodPost moodPost : dataList) {
            if (Boolean.TRUE.equals(moodPost.getLocation())) {
                LatLng moodPostLatLng = new LatLng(moodPost.getLatitude(), moodPost.getLongitude());

                MoodMarker moodMarker = new MoodMarker(
                        moodPostLatLng.latitude,
                        moodPostLatLng.longitude,
                        moodPost.getEmotion().getState(),
                        "@" + moodPost.getProfile().getUserId(),
                        moodPost
                );

                clusterManager.addItem(moodMarker);
            }
        }

        // Cluster and refresh
        clusterManager.cluster();
    }

    private void showMoodPostsListDialog(final List<MoodPost> moodPosts) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Nearby Mood Posts");

        // Create a new list with display strings
        List<String> displayStrings = new ArrayList<>();
        for (MoodPost post : moodPosts) {
            // Calculate and display distance
            LatLng postLocation = new LatLng(post.getLatitude(), post.getLongitude());
            double distance = calculateDistance(currentUserLocation, postLocation);

            displayStrings.add(String.format("@%s - %s", post.getProfile().getUserId(), post.getEmotion().getState()));
        }

        ListView listView = new ListView(requireContext());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                displayStrings
        );

        listView.setAdapter(adapter);

        // Handle item clicks in the list
        listView.setOnItemClickListener((parent, view, position, id) -> {
            // Get the corresponding MoodPost
            MoodPost selectedPost = moodPosts.get(position);

            // Open a detailed view dialog or Fragment
            DetailedMoodPostFragment detailedMoodPostFragment =
                    DetailedMoodPostFragment.newInstance(selectedPost);
            detailedMoodPostFragment.show(
                    getActivity().getSupportFragmentManager(),
                    "DetailedMoodPost"
            );
        });

        builder.setView(listView);
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
