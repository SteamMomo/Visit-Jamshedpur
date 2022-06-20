package com.example.visitjamshedpur;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EmailNotVerified extends AppCompatActivity {


    private final int reqCode = 100;
    private final ArrayList<Uri> ImageList = new ArrayList<>();
    ProgressDialog progressDialog;
    EditText aName, aAddress, aOpTime, aCloseTime, aID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_not_verified);
        ImageView imageView = findViewById(R.id.imageEmail);
        aName = findViewById(R.id.editTextName);
        aAddress = findViewById(R.id.editTextAddress);
        aOpTime = findViewById(R.id.editTextOpTime);
        aCloseTime = findViewById(R.id.editTextCloseTime);
        aID = findViewById(R.id.editTextID);
        imageView.setOnClickListener(V -> selectImageFnc());
        findViewById(R.id.selectImage).setOnClickListener(V -> uploadFile());
    }

    private void uploadFile() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Image uploading");
        progressDialog.show();
        final StorageReference ImageFolder = FirebaseStorage.getInstance().getReference().child("Visit Jamshedpur");
        String uidKey = aID.getText().toString();
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Attractions").document(uidKey);
        Map<String, Object> attr = new HashMap<>();
        attr.put("aName", aName.getText().toString().trim());
        attr.put("aAddress", aAddress.getText().toString().trim());
        attr.put("aOpTime", aOpTime.getText().toString().trim());
        attr.put("aCloseTime", aCloseTime.getText().toString().trim());
        documentReference.update(attr).addOnFailureListener(e -> documentReference.set(attr).addOnSuccessListener(unused -> Toast.makeText(EmailNotVerified.this, "Place data added to cloud", Toast.LENGTH_SHORT).show()));

        for (int uploads = 0; uploads < ImageList.size(); uploads++) {
            Uri Image = ImageList.get(uploads);
            final StorageReference imagename = ImageFolder.child("Attractions/" + Image.getLastPathSegment());

            int finalUploads = uploads;
            imagename.putFile(ImageList.get(uploads)).addOnSuccessListener(taskSnapshot -> imagename.getDownloadUrl().addOnSuccessListener(uri -> {
                String url = String.valueOf(uri);
                attr.put("image-" + finalUploads, url);
                documentReference.update(attr).addOnFailureListener(e -> documentReference.set(attr).addOnSuccessListener(unused -> Toast.makeText(EmailNotVerified.this, "Images data added to cloud", Toast.LENGTH_SHORT).show()));
                if (progressDialog.isShowing()) progressDialog.dismiss();
            }).addOnFailureListener(e -> {
                Toast.makeText(EmailNotVerified.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                if (progressDialog.isShowing()) progressDialog.dismiss();
            }));
        }

        ImageList.clear();
    }

    private void selectImageFnc() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, reqCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == reqCode) {
            if (resultCode == RESULT_OK) {
                assert data != null;
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();

                    int CurrentImageSelect = 0;

                    while (CurrentImageSelect < count) {
                        Uri imageuri = data.getClipData().getItemAt(CurrentImageSelect).getUri();
                        ImageList.add(imageuri);
                        CurrentImageSelect = CurrentImageSelect + 1;
                    }
                }

            }

        }
    }

}