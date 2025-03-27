package com.github.bytebandits.bithub.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.github.bytebandits.bithub.controller.DatabaseManager;
import com.github.bytebandits.bithub.model.Emotion;
import com.github.bytebandits.bithub.MainActivity;
import com.github.bytebandits.bithub.model.MoodPost;
import com.github.bytebandits.bithub.R;
import com.github.bytebandits.bithub.controller.SessionManager;
import com.github.bytebandits.bithub.model.Profile;
import com.github.bytebandits.bithub.model.SocialSituation;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.Blob;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class PostMoodFragment extends Fragment {
    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private Emotion selectedEmotion;
    private SocialSituation selectedSocialSituation;
    private String selectedDescription;
    private boolean selectedLocation;
    private boolean selectedPublic;
    private Uri selectedImageUri;
    private byte[] selectedImageByteArray = null;

    public static PostMoodFragment newInstance(MoodPost moodPost) {
        // Use Bundle to get info between fragments
        Bundle args = new Bundle();
        args.putSerializable("mood post", moodPost);
        PostMoodFragment fragment = new PostMoodFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.post_mood_fragment, container, false);
        // Initialize views
        Spinner editEmotion = view.findViewById(R.id.postMoodEmotion);
        Spinner editSocialSituation = view.findViewById(R.id.postMoodSocialSituation);
        EditText editDescription = view.findViewById(R.id.postMoodDescription);
        Button cancelButton = view.findViewById(R.id.postMoodCancelButton);
        Button confirmButton = view.findViewById(R.id.postMoodConfirmButton);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        findCurrentLocation();
        CheckBox editPublic = view.findViewById(R.id.postMoodPublic);
        CheckBox editLocation = view.findViewById(R.id.postMoodLocation);
        Button uploadButton = view.findViewById(R.id.postMoodUploadImageButton);
        Button deleteButton = view.findViewById(R.id.postMoodDeleteImageButton);
        deleteButton.setVisibility(View.GONE);
        ImageView editImage = view.findViewById(R.id.postMoodImage);
        SessionManager sessionManager = SessionManager.getInstance(requireContext());
        // Get the mood post if we need to edit a mood post
        String tag = getTag();
        Bundle bundle = getArguments();
        MoodPost postToEdit;
        if (tag != null && tag.equals("edit mood post") && bundle != null) {
            postToEdit = (MoodPost) bundle.getSerializable("mood post");
        } else {
            postToEdit = null;
        }

        // Initialize the spinners
        // Create ArrayAdapters from the enumerations and a default spinner layout.
        List<Object> socialSituations = new ArrayList<>();
        socialSituations.add("prefer not to say");
        socialSituations.addAll(Arrays.asList(SocialSituation.values())); // add the rest of the social situations
        Emotion[] emotions = Emotion.values();
        ArrayAdapter<Emotion> emotionAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                emotions);
        ArrayAdapter<Object> socialSituationAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                socialSituations);

        // Specify the layout to use when the list of choices appears and apply the
        // adapters
        socialSituationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        emotionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editEmotion.setAdapter(emotionAdapter);
        editSocialSituation.setAdapter(socialSituationAdapter);

        // Initialize selected emotions and social situations
        selectedEmotion = emotionAdapter.getItem(0);
        selectedSocialSituation = null;
        // Handle user selected input for spinners
        editEmotion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                    int position, long id) {
                selectedEmotion = (Emotion) parentView.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Default to first emotion on none selected
                selectedEmotion = emotionAdapter.getItem(0);
            }
        });
        editSocialSituation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                    int position, long id) {
                // if prefer not to say is selected, set social situation to null
                if (position == 0) {
                    selectedSocialSituation = null;
                }
                // else set it regularly
                else {
                    selectedSocialSituation = (SocialSituation) parentView.getItemAtPosition(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Default to null option on none selected
                selectedSocialSituation = null;
            }
        });

        // Handle user uploading an image
        // Initialize the activity launcher
        ActivityResultLauncher<Intent> imagePickerLauncher;
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData(); // The first getData() returns and intent, and
                                                                       // the second, the URI
                        // Convert image Uri to Byte array for storage and check the file size of image
                        try {
                            selectedImageByteArray = uriToByteArray(selectedImageUri);
                            // Display the image in the imageview if no exception is thrown
                            editImage.setImageURI(selectedImageUri);
                            deleteButton.setVisibility(View.VISIBLE);
                        } catch (IllegalArgumentException e) {
                            // Show an error message
                            new AlertDialog.Builder(getContext())
                                    .setTitle("Error")
                                    .setMessage(
                                            "Image is too large (Must be less than 65536 bytes). Please select a smaller file.")
                                    .setNegativeButton("Ok", (dialog, which) -> {
                                        dialog.cancel();
                                    })
                                    .show();
                        } catch (IOException e) {
                            // Show an error message
                            new AlertDialog.Builder(getContext())
                                    .setTitle("Error")
                                    .setMessage("Image cannot be read")
                                    .setNegativeButton("Ok", (dialog, which) -> {
                                        dialog.cancel();
                                    })
                                    .show();
                        }
                    }
                });
        // Launch the activity on upload button click
        uploadButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        // Only show location checkbox if we have location services enabled
        if (!sessionManager.getProfile().getLocationServices()) {
            editLocation.setVisibility(View.GONE);
        }

        // If editing mood post, set the input views to mood post properties
        if (postToEdit != null) {
            selectSpinnerItemByValue(editEmotion, postToEdit.getEmotion());
            selectSpinnerItemByValue(editSocialSituation, postToEdit.getSocialSituation());
            editLocation.setChecked(postToEdit.getLocation());
            editPublic.setChecked(postToEdit.isPublic());
            if (postToEdit.getDescription() == null) {
                editDescription.setText("");
            } else {
                editDescription.setText(postToEdit.getDescription());
            }
            if (postToEdit.getImage() != null) {
                byte[] postImageByteArray = Base64.decode(postToEdit.getImage(), Base64.DEFAULT);
                selectedImageByteArray = postImageByteArray; // So if the user doesn't upload a new image, it won't
                                                             // update the image to null
                editImage.setImageBitmap(BitmapFactory.decodeByteArray(postImageByteArray,
                        0, postImageByteArray.length));
                deleteButton.setVisibility(View.VISIBLE);
            }
        }

        // Delete image button logic
        deleteButton.setOnClickListener(v -> {
            editImage.setImageBitmap(null);
            selectedImageByteArray = null;
            selectedImageUri = null;
            deleteButton.setVisibility(View.GONE);
        });

        confirmButton.setOnClickListener(v -> {
            // Get inputs
            selectedLocation = editLocation.isChecked();
            selectedPublic = editPublic.isChecked();
            if (editDescription.getText().toString().isEmpty()) {
                selectedDescription = null;
            } else {
                selectedDescription = editDescription.getText().toString();
            }

            // Check for valid description input (max 20 char. or 3 words), if valid, add
            // mood post
            if (selectedDescription != null && selectedDescription.length() > 200) {
                editDescription.setError("Description can be max 200 characters");
            } else {
                DatabaseManager databaseManager = DatabaseManager.getInstance();
                // Add mood post to database
                String selectedImageBase64String;
                if (selectedImageByteArray != null) {
                    selectedImageBase64String = Base64.encodeToString(selectedImageByteArray, Base64.DEFAULT);
                } else {
                    selectedImageBase64String = null;
                }
                if (postToEdit == null) {
                    MoodPost moodPost = new MoodPost(selectedEmotion, sessionManager.getProfile(),
                            selectedLocation, selectedSocialSituation, selectedDescription,
                            selectedImageBase64String, selectedPublic);
                    databaseManager.addPost(moodPost, sessionManager.getProfile().getUserId(), Optional.empty());
                    // for some reason setting the longitude and latitude then adding post doesn't
                    // work so im updating the values after adding the post
                    if (selectedLocation) {
                        HashMap<String, Object> updateFields = new HashMap<>();
                        updateFields.put("longitude", currentLongitude);
                        updateFields.put("latitude", currentLatitude);
                        databaseManager.updatePost(moodPost.getPostID(), updateFields, Optional.empty());
                    }
                } else {
                    HashMap<String, Object> updateFields = new HashMap<>();
                    updateFields.put("socialSituation", selectedSocialSituation);
                    updateFields.put("emotion", selectedEmotion);
                    updateFields.put("description", selectedDescription);
                    updateFields.put("image", selectedImageBase64String);
                    updateFields.put("location", selectedLocation);
                    updateFields.put("public", selectedPublic);
                    // If user wants to set the location, and no previous location is assigned to
                    // the mood post, then assign new location
                    if (selectedLocation && (postToEdit.getLongitude() == null || postToEdit.getLatitude() == null)) {
                        updateFields.put("longitude", currentLongitude);
                        updateFields.put("latitude", currentLatitude);
                    }
                    databaseManager.updatePost(postToEdit.getPostID(), updateFields, Optional.empty());
                }
                // Go back to homepage fragment
                ((MainActivity) requireActivity()).replaceFragment(new HomepageFragment());
            }
        });

        cancelButton.setOnClickListener(v -> {
            // Just go back to homepage fragment
            ((MainActivity) requireActivity()).replaceFragment(new HomepageFragment());
        });

        return view;
    }

    private void findCurrentLocation() {
        boolean b = ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
        if (ActivityCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request permissions if not granted
            requestPermissions(new String[] {
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQUEST_LOCATION_PERMISSION);
            return;
        }
        // Attempt to get last known location
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
            }
        });
    }

    /**
     * Selects an item in a {@link Spinner} by matching its value.
     * This method iterates through the items in the spinner's adapter
     * and selects the item that matches the given value.
     *
     * @param spnr  The {@link Spinner} whose item is to be selected.
     *              Must not be {@code null}.
     * @param value The value to match against the spinner's items.
     *              Must not be {@code null}.
     *
     * @throws NullPointerException If either {@code spnr} or {@code value} is
     *                              {@code null}.
     */
    private void selectSpinnerItemByValue(Spinner spnr, Object value) {
        SpinnerAdapter adapter = spnr.getAdapter();
        for (int position = 0; position < adapter.getCount(); position++) {
            if (adapter.getItem(position).equals(value)) {
                spnr.setSelection(position);
                return;
            }
        }
    }

    /**
     * Converts the data from a file represented by a URI into a byte array.
     * This method reads the file in chunks using a buffer to efficiently handle
     * large files
     * without loading the entire file into memory at once.
     *
     * @param uri The URI of the file to be converted into a byte array.
     * @return A byte array containing the data from the file.
     * @throws IOException              If an I/O error occurs while reading the
     *                                  file, such as if the URI
     *                                  cannot be opened or the file cannot be read.
     * @throws IllegalArgumentException If the file size is greater than or equal to
     *                                  65536 bytes.
     */
    private byte[] uriToByteArray(Uri uri) throws IOException, IllegalArgumentException {
        // Get input output streams
        InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        // Read the image 1024 bytes at a time and throw error if image is too large
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        int totalBytesRead = 0;
        final int MAX_FILE_SIZE = 65536;
        while ((len = inputStream.read(buffer)) != -1) {
            totalBytesRead += len;
            if (totalBytesRead >= MAX_FILE_SIZE) {
                inputStream.close();
                byteBuffer.close();
                throw new IllegalArgumentException();
            }
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}
