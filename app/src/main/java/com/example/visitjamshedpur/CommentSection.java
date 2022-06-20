package com.example.visitjamshedpur;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class CommentSection extends AppCompatActivity {

    ImageView postProfile, postImage, sendBtn;
    EditText editText;
    TextView postAuthorName, postTitle, postDescription, postImageCounter, postAuthorIdentity, postTimePassed;
    ProgressBar postProgressBar;
    CardView postImageViewer, postProfileCard;
    LinearLayout postAuthorDescription;
    View postImageLeftBtn, postImageRightBtn;
    ArrayList<String> imageUrls = new ArrayList<>();
    ArrayList<CommentItemModel> comments = new ArrayList<>();
    FirebaseUser user;
    String postProfileStr, authorNameStr, authorIdentityStr, finalDurationStr, postTitleStr, postDescriptionStr, postIdStr;
    Toolbar toolbar;
    RecyclerView commentRecyclerView;
    CommentItemAdapter commentItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_section);
        initializations();
        getStringIntent();
        setPostData();
        toolbarNavigation();
        sendBtn.setOnClickListener(view -> addComment());
        if (commentItemAdapter == null || comments.size() > 0)
            getComments();
    }

    private void addComment() {
        String comment = editText.getText().toString().trim();
        editText.setText("");
        FirebaseFirestore
                .getInstance()
                .collection("users")
                .document(Objects.requireNonNull(user.getEmail()))
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String uID = UUID.randomUUID().toString();
                String profileUrl, authorName, authorIdentity;
                DocumentSnapshot snapshot = task.getResult();
                profileUrl = snapshot.getString("uProfile");
                authorName = snapshot.getString("uName");
                authorIdentity = snapshot.getString("uIdentity");
                if (authorIdentity == null) authorIdentity = "";
                if (profileUrl == null) profileUrl = "";
                Map<String, Object> temp = new HashMap<>();
                temp.put("cAuthor", authorName);
                temp.put("cIdentity", authorIdentity);
                temp.put("cProfile", profileUrl);
                temp.put("cComment", comment);
                temp.put("cTimestamp", FieldValue.serverTimestamp());
                temp.put("cID", uID);
                temp.put("authorID", user.getEmail());
                temp.put("pID", postIdStr);
                temp.put("flag", "comment");
                String finalAuthorIdentity = authorIdentity;
                FirebaseFirestore
                        .getInstance()
                        .collection("Posts")
                        .document(postIdStr)
                        .collection("Comments")
                        .document(uID)
                        .set(temp)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Comment added.", Toast.LENGTH_SHORT).show();
                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(System.currentTimeMillis());
                            String date = DateFormat.format("dd-MM-yyyy", cal).toString();
                            comments.add(0, new CommentItemModel(finalAuthorIdentity, comment, date, user.getEmail(), uID, postIdStr, null, null, "comment"));
                            if (commentItemAdapter != null) commentItemAdapter.updateData(comments);
                            else if (comments.size() > 0) {
                                commentItemAdapter = new CommentItemAdapter(CommentSection.this, comments);
                                commentRecyclerView.setAdapter(commentItemAdapter);
                                commentRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()
                                        , RecyclerView.VERTICAL, false));
                            }

                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Comment not added. " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void getComments() {
        FirebaseFirestore
                .getInstance()
                .collection("Posts")
                .document(postIdStr)
                .collection("Comments")
                .orderBy("cTimestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            String authorIdentity, comment;
                            authorIdentity = documentSnapshot.getString("cIdentity");
                            comment = documentSnapshot.getString("cComment");
                            Timestamp t = (Timestamp) documentSnapshot.get("cTimestamp");
                            long time = t != null ? t.getSeconds() : 0;
                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(time * 1000);
                            String date = DateFormat.format("dd-MM-yyyy", cal).toString();
                            String email = documentSnapshot.getString("authorID");
                            String cID = documentSnapshot.getString("cID");
                            String pID = documentSnapshot.getString("pID");
                            CommentItemModel commentItemModel = new CommentItemModel(authorIdentity, comment, date, email, cID, pID, null, null, "comment");
                            comments.add(commentItemModel);
                            if (commentItemAdapter != null) commentItemAdapter.updateData(comments);
                            else if (comments.size() > 0) {
                                commentItemAdapter = new CommentItemAdapter(CommentSection.this, comments);
                                commentRecyclerView.setAdapter(commentItemAdapter);
                                commentRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()
                                        , RecyclerView.VERTICAL, false));
                            }
                        }

                    }
                });
    }

    private void toolbarNavigation() {
        toolbar.setNavigationIcon(R.drawable.back_icon_white);
        toolbar.setNavigationOnClickListener(view -> super.onBackPressed());
    }

    private void setPostData() {
        Glide.with(getApplicationContext())
                .load(postProfileStr)
                .into(postProfile);
        postAuthorName.setText(authorNameStr);
        postAuthorIdentity.setText(authorIdentityStr);
        postTimePassed.setText(finalDurationStr);
        if (postTitleStr.length() < 1) postTitle.setVisibility(View.GONE);
        else postTitle.setText(postTitleStr);
        if (postDescriptionStr.length() < 1) postDescription.setVisibility(View.GONE);
        else postDescription.setText(postDescriptionStr);
        setImageLeftRight();
    }

    @SuppressLint("SetTextI18n")
    private void setImageLeftRight() {
        int size = imageUrls.size();
        final int[] cnt = {0};
        if (size > 0) {
            postImageViewer.setVisibility(View.VISIBLE);
            String temp = imageUrls.get(0);
            if (temp != null) {
                Glide.with(getApplicationContext())
                        .load(temp)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                if (e != null) {
                                    Toast.makeText(CommentSection.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    System.out.println(e.getMessage());
                                }
                                postProgressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                postProgressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(postImage);
                postImageCounter.setText((cnt[0] + 1) + "/" + size);
            }
        }
        if (size > 1) postImageCounter.setVisibility(View.VISIBLE);
        postImageLeftBtn.setOnClickListener(view -> {
            if ((cnt[0] - 1) >= 0) {
                cnt[0]--;
                String temp = imageUrls.get(cnt[0]);
                if (temp != null) {
                    Glide.with(getApplicationContext())
                            .load(temp)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    if (e != null) {
                                        Toast.makeText(CommentSection.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        System.out.println(e.getMessage());
                                    }
                                    postProgressBar.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    postProgressBar.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .into(postImage);
                    postImageCounter.setText((cnt[0] + 1) + "/" + size);
                }
            }
        });
        postImageRightBtn.setOnClickListener(view -> {
            if ((cnt[0] + 1) < size) {
                cnt[0]++;
                String temp = imageUrls.get(cnt[0]);
                if (temp != null) {
                    Glide.with(getApplicationContext())
                            .load(temp)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    if (e != null) {
                                        Toast.makeText(CommentSection.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        System.out.println(e.getMessage());
                                    }
                                    postProgressBar.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    postProgressBar.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .into(postImage);
                    postImageCounter.setText((cnt[0] + 1) + "/" + size);
                }
            }
        });
    }

    private void getStringIntent() {
        Intent intent = getIntent();
        postProfileStr = intent.getStringExtra("postProfile");
        authorNameStr = intent.getStringExtra("authorName");
        authorIdentityStr = intent.getStringExtra("authorIdentity");
        finalDurationStr = intent.getStringExtra("finalDuration");
        postTitleStr = intent.getStringExtra("postTitle");
        postDescriptionStr = intent.getStringExtra("postDescription");
        postIdStr = intent.getStringExtra("postId");
        imageUrls = intent.getStringArrayListExtra("postImageUrls");
    }

    private void initializations() {
        postAuthorName = findViewById(R.id.postAuthorName);
        postImage = findViewById(R.id.postImage);
        postProfile = findViewById(R.id.postProfile);
        postTitle = findViewById(R.id.postTitle);
        postDescription = findViewById(R.id.postDescription);
        postImageCounter = findViewById(R.id.postImageCounter);
        postProgressBar = findViewById(R.id.postProgressBar);
        postImageViewer = findViewById(R.id.postImageViewer);
        postImageLeftBtn = findViewById(R.id.postImageLeft);
        postImageRightBtn = findViewById(R.id.postImageRight);
        postAuthorIdentity = findViewById(R.id.postAuthorIdentity);
        postTimePassed = findViewById(R.id.postTimePassed);
        postProfileCard = findViewById(R.id.postProfileCard);
        postAuthorDescription = findViewById(R.id.postAuthorDescription);
        toolbar = findViewById(R.id.commentToolbar);
        sendBtn = findViewById(R.id.sendComment);
        editText = findViewById(R.id.commentEditText);
        commentRecyclerView = findViewById(R.id.commentRecyclerView);
        user = FirebaseAuth.getInstance().getCurrentUser();
    }
}