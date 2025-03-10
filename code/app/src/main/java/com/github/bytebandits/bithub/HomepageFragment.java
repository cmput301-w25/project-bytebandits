package com.github.bytebandits.bithub;

import static java.lang.Thread.sleep;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomepageFragment extends Fragment {
    private ArrayList<MoodPost> dataList;
    private ListView moodPostList;
    private MoodPostArrayAdapter moodPostAdapter;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.homepage_fragment, container, false);

        // Initialize dataList to avoid NullPointerException
        if (dataList == null) {
            dataList = new ArrayList<>();
            Log.d("HomepageFragment", "dataList initialized as empty list");
        } else {
            Log.d("HomepageFragment", "dataList already initialized with size: " + dataList.size());
        }

        executor.execute(() -> {
                    DatabaseManager.getPosts(posts -> {
                        if (posts == null) {
                            Log.e("HomepageFragment", "Error: posts is null");
                        }

                        Log.d("HomepageFragment", "Fetched posts count: " + posts.size());

                        // Switch to UI thread for UI updates
                        mainHandler.post(() -> {
                            dataList.clear();
                            dataList.addAll(posts);

                            if (moodPostAdapter != null) {
                                moodPostAdapter.notifyDataSetChanged();
                            } else {
                                Log.e("HomepageFragment", "moodPostAdapter is null");
                            }

                            // Initialize views and adapters
                            moodPostList = view.findViewById(R.id.homepageMoodPostList);
                            if (dataList.size() > 0) {
                                moodPostAdapter = new MoodPostArrayAdapter(getContext(), dataList);
                                moodPostList.setAdapter(moodPostAdapter);

                                // on item click on list, open detailed view of post
                                moodPostList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view,
                                                            int position, long id) {
                                        DetailedMoodPostFragment detailedMoodPostFragment =
                                                DetailedMoodPostFragment.newInstance(dataList.get(position));
                                        detailedMoodPostFragment.show(getActivity().getSupportFragmentManager(), "Detailed Mood Post View");
                                    }
                                });
                            }
                        });
                        return null;
                    });
                });

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



        return view;
    }
}

