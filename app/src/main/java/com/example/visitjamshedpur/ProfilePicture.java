package com.example.visitjamshedpur;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ProfilePicture extends AppCompatActivity {

    Toolbar toolbar;
    String profileUrl, pu;
    ImageView profilePicBig, editProfilePicBtn;
    private FirebaseUser user;
    ProgressDialog progressDialog;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_picture);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Image uploading");
        user = FirebaseAuth.getInstance().getCurrentUser();
        toolbar = findViewById(R.id.toolbar);
        editProfilePicBtn = findViewById(R.id.editProfilePic);
        profilePicBig = findViewById(R.id.profilePicBig);
        progressBar = findViewById(R.id.progressBarBigDP);

        toolbar.setNavigationOnClickListener(view -> {
            super.onBackPressed();
            finish();
        });

        DocumentReference documentReference = FirebaseFirestore.getInstance()
                .collection("users").document(Objects.requireNonNull(user.getEmail()));
        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.getString("uStorageProfileUrl") != null)
                    pu = documentSnapshot.getString("uStorageProfileUrl");
                if (documentSnapshot.getString("uProfile") != null){
                    progressBar.setVisibility(View.VISIBLE);
                    Glide.with(getApplicationContext())
                            .load(documentSnapshot.getString("uProfile"))
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
                            .into(profilePicBig);
                }
                else progressBar.setVisibility(View.GONE);
            }
        });

        editProfilePicBtn.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, 101);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (data != null) {
                progressDialog.show();
                if (pu != null) {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Visit Jamshedpur").child("UserProfilePics").child(pu);
                    storageReference.delete().addOnSuccessListener(unused -> Toast.makeText(this, "Previous profile picture deleted.", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                Uri imageUri = data.getData();
                DocumentReference documentReference = FirebaseFirestore.getInstance()
                        .collection("users").document(Objects.requireNonNull(user.getEmail()));
                Glide.with(this).load(imageUri).into(profilePicBig);
                final StorageReference ImageFolder = FirebaseStorage.getInstance().getReference().child("Visit Jamshedpur");
                profileUrl = UUID.randomUUID().toString();
                final StorageReference imageName = ImageFolder.child("UserProfilePics/" + profileUrl);
                imageName.putFile(imageUri).addOnSuccessListener(taskSnapshot -> imageName.getDownloadUrl().addOnSuccessListener(uri -> {
                    String url = String.valueOf(uri);
                    Map<String, Object> tempMap = new HashMap<>();
                    tempMap.put("uStorageProfileUrl", profileUrl);
                    tempMap.put("uProfile", url);
                    documentReference.update(tempMap).addOnSuccessListener(unused -> Toast.makeText(ProfilePicture.this, "Image Changed", Toast.LENGTH_SHORT).show());
                    progressDialog.dismiss();
                }).addOnFailureListener(e -> {
                    Toast.makeText(ProfilePicture.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }));
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}