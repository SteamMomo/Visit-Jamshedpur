package com.example.visitjamshedpur;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class Reviews extends AppCompatActivity {

    private final int reqCode = 200;
    private EditText editText;
    private FirebaseUser user;
    private String aID;
    private ImageView reviewImage;
    private ConstraintLayout imagePreviewLayout;
    private ImageView removeImage;
    private Uri imageUri = null;
    private ArrayList<CommentItemModel> reviewList = new ArrayList<>();
    private CommentItemAdapter commentItemAdapter;
    private RecyclerView reviewRecyclerView;
    private ImageView sendReview;
    private ImageView reviewImageBtn;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);
        initializations();
        imageIntent();
        getIntentInfo();
        sendReview.setOnClickListener(view -> addReview());
        removeImage.setOnClickListener(view -> {
            imageUri = null;
            imagePreviewLayout.setVisibility(View.GONE);
        });
        toolbarOptions();
        getReview();
    }

    private void toolbarOptions() {
        toolbar.setNavigationIcon(R.drawable.back_icon_white);
        toolbar.setNavigationOnClickListener(view -> super.onBackPressed());
    }

    private void getReview() {

        FirebaseFirestore
                .getInstance()
                .collection("Attractions")
                .document(aID)
                .collection("reviews")
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
                            String downUri = documentSnapshot.getString("pImageUri");
                            String deleteUri = documentSnapshot.getString("pImageDeleteUri");
                            reviewList.add(new CommentItemModel(authorIdentity, comment, date, email, cID, pID, downUri, deleteUri, "review"));
                            if (commentItemAdapter != null)
                                commentItemAdapter.updateData(reviewList);
                            else if (reviewList.size() > 0) {
                                commentItemAdapter = new CommentItemAdapter(Reviews.this, reviewList);
                                reviewRecyclerView.setAdapter(commentItemAdapter);
                                reviewRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()
                                        , RecyclerView.VERTICAL, false));
                            }
                        }
                    }
                });
    }

    private void imageIntent() {
        reviewImageBtn.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, reqCode);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == reqCode) {
            {
                if (data != null && data.getData() != null) {
                    imagePreviewLayout.setVisibility(View.VISIBLE);
                    imageUri = data.getData();
                    reviewImage.setImageURI(imageUri);
                }
            }
        }
    }

    private void getIntentInfo() {
        Intent intent = getIntent();
        aID = intent.getStringExtra("aID");
    }

    private void initializations() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        editText = findViewById(R.id.reviewEditText);
        reviewImageBtn = findViewById(R.id.reviewImageBtn);
        imagePreviewLayout = findViewById(R.id.reviewImageViewer);
        removeImage = findViewById(R.id.removeReviewImage);
        reviewRecyclerView = findViewById(R.id.reviewRecyclerView);
        sendReview = findViewById(R.id.sendReview);
        reviewImage = findViewById(R.id.reviewImage);
        toolbar = findViewById(R.id.reviewToolbar);

    }

    private void addReview() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading review");
        progressDialog.setCancelable(false);
        progressDialog.show();
        String review = editText.getText().toString().trim();
        if (imageUri == null && review.length() < 1) {
            Toast.makeText(this, "Please write a review or add an image.", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        } else {
            String uID = UUID.randomUUID().toString();
            String randomUri = UUID.randomUUID().toString();
            editText.setText("");
            StorageReference sb = FirebaseStorage
                    .getInstance()
                    .getReference()
                    .child(getString(R.string.visitJamshdepurFolder))
                    .child("reviews/" + randomUri);
            FirebaseFirestore
                    .getInstance()
                    .collection("users")
                    .document(user.getEmail())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot snapshot = task.getResult();
                            Map<String, Object> temp = new HashMap<>();
                            temp.put("cAuthor", snapshot.getString("uName"));
                            temp.put("cIdentity", snapshot.getString("uIdentity"));
                            temp.put("cProfile", snapshot.getString("uProfile"));
                            temp.put("cComment", review);
                            temp.put("cTimestamp", FieldValue.serverTimestamp());
                            temp.put("cID", uID);
                            temp.put("authorID", user.getEmail());
                            temp.put("pID", aID);
                            temp.put("flag", "review");
                            if(imageUri!=null) {
                                sb.putFile(imageUri)
                                        .addOnCompleteListener(task2 -> {
                                            if (task2.isSuccessful()) {
                                                sb.getDownloadUrl()
                                                        .addOnCompleteListener(task3 -> {
                                                            if (task3.isSuccessful()) {
                                                                Uri downUri = task3.getResult();
                                                                String downUriStr = null;
                                                                if (downUri != null)
                                                                    downUriStr = downUri.toString();
                                                                String finalDownUriStr = downUriStr;
                                                                temp.put("pImageUri", finalDownUriStr);
                                                                temp.put("pImageDeleteUri", randomUri);
                                                                FirebaseFirestore
                                                                        .getInstance()
                                                                        .collection("Attractions")
                                                                        .document(aID)
                                                                        .collection("reviews")
                                                                        .document(uID)
                                                                        .set(temp)
                                                                        .addOnCompleteListener(task1 -> {
                                                                            if (task1.isSuccessful()) {
                                                                                Toast.makeText(this, "Review added.", Toast.LENGTH_SHORT).show();
                                                                                progressDialog.dismiss();
                                                                                imagePreviewLayout.setVisibility(View.GONE);
                                                                                Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                                                                                cal.setTimeInMillis(System.currentTimeMillis());
                                                                                String date = DateFormat.format("dd-MM-yyyy", cal).toString();
                                                                                reviewList.add(0, new CommentItemModel(snapshot.getString("uIdentity"), review, date, user.getEmail(), uID, aID, finalDownUriStr, randomUri, "review"));
                                                                                if (commentItemAdapter != null)
                                                                                    commentItemAdapter.updateData(reviewList);
                                                                                else if (reviewList.size() > 0) {
                                                                                    commentItemAdapter = new CommentItemAdapter(Reviews.this, reviewList);
                                                                                    reviewRecyclerView.setAdapter(commentItemAdapter);
                                                                                    reviewRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()
                                                                                            , RecyclerView.VERTICAL, false));
                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                        });

                                            }
                                        });
                            }else{
                                temp.put("pImageUri", null);
                                temp.put("pImageDeleteUri", null);
                                FirebaseFirestore
                                        .getInstance()
                                        .collection("Attractions")
                                        .document(aID)
                                        .collection("reviews")
                                        .document(uID)
                                        .set(temp)
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                Toast.makeText(this, "Review added.", Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();
                                                imagePreviewLayout.setVisibility(View.GONE);
                                                Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                                                cal.setTimeInMillis(System.currentTimeMillis());
                                                String date = DateFormat.format("dd-MM-yyyy", cal).toString();
                                                reviewList.add(0, new CommentItemModel(snapshot.getString("uIdentity"), review, date, user.getEmail(), uID, aID, null, null, "review"));
                                                if (commentItemAdapter != null)
                                                    commentItemAdapter.updateData(reviewList);
                                                else if (reviewList.size() > 0) {
                                                    commentItemAdapter = new CommentItemAdapter(Reviews.this, reviewList);
                                                    reviewRecyclerView.setAdapter(commentItemAdapter);
                                                    reviewRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()
                                                            , RecyclerView.VERTICAL, false));
                                                }
                                            }
                                        });
                            }
                        }
                    });



        }
    }
}