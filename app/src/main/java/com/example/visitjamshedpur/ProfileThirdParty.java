package com.example.visitjamshedpur;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.visitjamshedpur.R.drawable;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ProfileThirdParty extends AppCompatActivity {

    private final ArrayList<PostItemModel> profilePosts = new ArrayList<>();
    private ImageView profilePic;
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private FirebaseFirestore fireStore;
    private TextView profileName;
    private TextView profileIdentity;
    private TextView profileContributions;
    private TextView profileLikes;
    private String email;
    private RecyclerView profile3rdRecycler;
    private PostItemAdapter postItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_third_party);
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        initializations();
        toolbarOptions();
        getPosts();
        setProfileData();
    }

    private void getPosts() {
        profilePosts.clear();
        progressBar.setVisibility(View.VISIBLE);
        fireStore.collection("Posts")
                .whereEqualTo("pAuthor", email)
                .orderBy("pTimeStamp", Query.Direction.DESCENDING)
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
                            profilePosts.add(postItemModel);
                            if (postItemAdapter != null) postItemAdapter.updateData(profilePosts);
                        }
                        profile3rdRecycler.setHasFixedSize(true);
                        if (profilePosts.size() > 0) {
                            postItemAdapter = new PostItemAdapter(this, profilePosts);
                            profile3rdRecycler.setAdapter(postItemAdapter);
                            profile3rdRecycler.setLayoutManager(new LinearLayoutManager(ProfileThirdParty.this
                                    , RecyclerView.VERTICAL, false));
                        }
                    }).addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
                }
                progressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void toolbarOptions() {
        toolbar.setNavigationIcon(R.drawable.back_icon_white);
        toolbar.setNavigationOnClickListener(view -> super.onBackPressed());
        toolbar.inflateMenu(R.menu.profile_menu);
        toolbar.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.logout) {
                //signOut();
            }
            return false;
        });
    }

    private void setProfileData() {

        DocumentReference db = fireStore
                .collection("users")
                .document(Objects.requireNonNull(email));
        db.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();

                final long[] contributions = {0};
                final long[] likes = {0};


                FirebaseFirestore
                        .getInstance()
                        .collection("Posts")
                        .whereEqualTo("pAuthor", email)
                        .get()
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                contributions[0] = task1.getResult().size();
                                profileContributions.setText(String.valueOf(contributions[0]));
                                for (QueryDocumentSnapshot documentSnapshot1 : task1.getResult())
                                    likes[0] = likes[0] + (long) documentSnapshot1.get("pLikes");
                                profileLikes.setText(String.valueOf(likes[0]));
                            }
                        });

                String profileUrl = documentSnapshot.getString("uProfile");
                if (profileUrl == null) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("uProfile", null);
                    db.update(user).addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    Glide.with(ProfileThirdParty.this).load(profileUrl)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    progressBar.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    progressBar.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .into(profilePic);
                }
                profileName.setText(documentSnapshot.getString("uName"));
                String identity = documentSnapshot.getString("uIdentity");
                if (identity == null) profileIdentity.setText("Identity not set.");
                else profileIdentity.setText(identity);
            }
        });
    }

    private void initializations() {
        profilePic = findViewById(R.id.profilePic);
        profileIdentity = findViewById(R.id.profileIdentity);
        profileName = findViewById(R.id.profileName);
        profileLikes = findViewById(R.id.likes);
        profileContributions = findViewById(R.id.contributions);
        progressBar = findViewById(R.id.progressBar);
        toolbar = findViewById(R.id.profileToolbar);
        profile3rdRecycler = findViewById(R.id.profile3rdRecycler);
        fireStore = FirebaseFirestore.getInstance();
    }
}