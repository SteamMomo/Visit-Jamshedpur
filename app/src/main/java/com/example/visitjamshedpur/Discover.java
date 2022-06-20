package com.example.visitjamshedpur;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;


public class Discover extends Fragment {

    RecyclerView recyclerViewPosts;
    PostItemAdapter postItemAdapter;
    RelativeLayout progressBar;
    FirebaseFirestore firestore;
    FirebaseStorage storage;
    FirebaseUser user;
    ArrayList<PostItemModel> posts = new ArrayList<>();
    View rootView;
    private SearchView searchView;
    private Toolbar toolBar;
    private TextView textView;

    public Discover() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_discover, container, false);
        initializations();
        toolbarSearch();
        if (postItemAdapter == null || posts.size() > 0)
            getPosts();
        return rootView;
    }

    private void toolbarSearch() {
        toolBar.inflateMenu(R.menu.search_menu);
        toolBar.setNavigationOnClickListener(view -> {
            toolBar.setNavigationIcon(null);
            textView.setVisibility(View.VISIBLE);
            searchView.setVisibility(View.GONE);
            getPosts();
        });
        toolBar.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.searchOption) {
                toolBar.setNavigationIcon(R.drawable.back_icon_white);
                textView.setVisibility(View.GONE);
                searchView.setVisibility(View.VISIBLE);
                searchView.requestFocus();
            }
            return false;
        });
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return false;
            }
        });
    }
    private void filter(String s) {
        ArrayList<PostItemModel> temp = new ArrayList<>();
        if (posts.size() > 0) {
            for (PostItemModel b : posts) {
                if (b.authorName.toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT))
                ||b.postTitle.toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT))
                ||b.postDescription.toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT)))
                    temp.add(b);
            }
        }
        if (postItemAdapter != null) postItemAdapter.updateData(temp);
    }
    private void initializations() {
        recyclerViewPosts = rootView.findViewById(R.id.discoverRecyclerView);
        progressBar = rootView.findViewById(R.id.progressBarLayoutDiscover);
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        toolBar = rootView.findViewById(R.id.toolbarDiscover);
        textView = rootView.findViewById(R.id.toolbarDTitle);
        searchView = rootView.findViewById(R.id.searchDiscover);
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void getPosts() {
        posts.clear();
        progressBar.setVisibility(View.VISIBLE);
        firestore.collection("Posts")
                .orderBy("pLongTimestamp", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    DocumentReference db = FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(Objects.requireNonNull(document.getString("pAuthor")));
                    db.get().addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task2.getResult();
                            String authorName = documentSnapshot.getString("uName");
                            String authorIdentity = documentSnapshot.getString("uIdentity");
                            String postDescription = document.getString("pDescription");
                            String postTitle = document.getString("pTitle");
                            String postProfile = documentSnapshot.getString("uProfile");
                            String authorId = documentSnapshot.getString("uEmail");
                            String postId = document.getString("pId");
                            Timestamp t = (Timestamp) document.get("pTimeStamp");
                            long timestamp = Objects.requireNonNull(t).getSeconds();
                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(timestamp * 1000L);
                            String date = DateFormat.format("dd-MM-yyyy HH:mm:ss", cal).toString();
                            int likes = Integer.parseInt(String.valueOf(document.get("pLikes")));
                            ArrayList<String> imageDownloadUrl = new ArrayList<>();
                            ArrayList<String> imageDeleteUrl = new ArrayList<>();
                            int cnt = 0;
                            while (document.getString("image-" + cnt) != null) {
                                imageDownloadUrl.add(document.getString("image-" + cnt));
                                imageDeleteUrl.add(document.getString("image-" + cnt + "-deleteUri"));
                                cnt++;
                            }
                            PostItemModel postItemModel = new PostItemModel(imageDownloadUrl,
                                    imageDeleteUrl, authorName, postDescription, postTitle, postProfile, authorIdentity,
                                    authorId, postId, likes, date);
                            posts.add(postItemModel);
                            if (postItemAdapter != null) postItemAdapter.updateData(posts);
                        }
                        recyclerViewPosts.setHasFixedSize(true);
                        if (posts.size() > 0) {
                            postItemAdapter = new PostItemAdapter(getContext(), posts);
                            recyclerViewPosts.setAdapter(postItemAdapter);
                            recyclerViewPosts.setLayoutManager(new LinearLayoutManager(getContext()
                                    , RecyclerView.VERTICAL, false));
                        }
                    }).addOnFailureListener(e -> Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }


    public static class SquareImageView extends androidx.appcompat.widget.AppCompatImageView {
        public SquareImageView(Context context) {
            super(context);
        }

        public SquareImageView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public SquareImageView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        }
    }
}