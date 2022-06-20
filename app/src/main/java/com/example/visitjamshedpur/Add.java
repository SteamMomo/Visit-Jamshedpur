package com.example.visitjamshedpur;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class Add extends Fragment {
    private final int reqCode = 1001;
    private final ArrayList<Uri> ImageList = new ArrayList<>();
    View rootView;
    ConstraintLayout imagePreviewLayout;
    Toolbar toolbar;
    ImageView sendBtn, imagePreview, leftBtn, rightBtn;
    EditText titleText, descText;
    Button removeImageBtn;
    FirebaseUser user;
    FirebaseFirestore fireStore;
    FirebaseStorage storage;

    public Add() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_add, container, false);
        initializations();
        toolbarOptions();
        removeImageBtn.setOnClickListener(view -> {
            imagePreviewLayout.setVisibility(View.GONE);
            ImageList.clear();
            removeImageBtn.setVisibility(View.GONE);
        });
        sendBtn.setOnClickListener(v -> sendData());
        return rootView;
    }

    private void sendData() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("Do you want to upload this Post ?");

        alertDialog.setPositiveButton("Yes", ((dialogInterface, it) -> {
            String title = titleText.getText().toString();
            String description = descText.getText().toString();

            if (title.length() > 0 || description.length() > 0) {
                ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage("Uploading Post...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                String postId = UUID.randomUUID().toString();
                DocumentReference db = fireStore.collection("Posts")
                        .document(postId);
                Map<String, Object> postDoc = new HashMap<>();
                postDoc.put("pAuthor", user.getEmail());
                postDoc.put("pTitle", title);
                postDoc.put("pDescription", description);
                postDoc.put("pId", postId);
                postDoc.put("pLikes", 0);
                postDoc.put("pTimeStamp", new Date());
                postDoc.put("pLongTimestamp", System.currentTimeMillis());
                db.set(postDoc);

                int count = ImageList.size();
                if (count > 0) {
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = ImageList.get(i);
                        String randomUri = UUID.randomUUID().toString();
                        StorageReference block = storage.getReference()
                                .child(getString(R.string.visitJamshdepurFolder))
                                .child("Posts/" + randomUri);
                        int finalI = i;
                        block.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {

                            block.getDownloadUrl().addOnSuccessListener(uri -> {
                                postDoc.put("image-" + finalI, uri.toString());
                                postDoc.put("image-" + finalI + "-deleteUri", randomUri);
                                db.set(postDoc);
                            });
                            if (finalI == count - 1) {
                                progressDialog.dismiss();
                                titleText.setText("");
                                descText.setText("");
                                imagePreviewLayout.setVisibility(View.GONE);
                                ImageList.clear();
                                removeImageBtn.setVisibility(View.GONE);
                            }
                        }).addOnFailureListener(e -> {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            if (finalI == count - 1) {
                                progressDialog.dismiss();
                            }
                        });
                    }
                } else progressDialog.dismiss();
            } else {
                Toast.makeText(getActivity(), "Enter a valid Post Title or Description", Toast.LENGTH_SHORT).show();
            }
        })).setNegativeButton("No", ((dialogInterface, it) -> {
        }));
        alertDialog.create().show();

    }

    private void toolbarOptions() {
        toolbar.setNavigationOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(intent, reqCode);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == reqCode) {
            {
                ImageList.clear();
                if (data != null && data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    if (count > 0) {
                        imagePreviewLayout.setVisibility(View.VISIBLE);
                        removeImageBtn.setVisibility(View.VISIBLE);
                    }
                    int CurrentImageSelect = 0;
                    while (CurrentImageSelect < count) {
                        Uri imageUri = data.getClipData().getItemAt(CurrentImageSelect).getUri();
                        ImageList.add(imageUri);
                        CurrentImageSelect = CurrentImageSelect + 1;
                    }
                    setImage();
                } else if (data != null && data.getData() != null) {
                    imagePreviewLayout.setVisibility(View.VISIBLE);
                    removeImageBtn.setVisibility(View.VISIBLE);
                    ImageList.add(data.getData());
                    setImage();
                }
            }
        }
    }

    private void setImage() {
        final int[] i = {0};
        if (ImageList.size() > 0)
            Glide.with(requireActivity())
                    .load(ImageList.get(i[0]))
                    .into(imagePreview);
        leftBtn.setOnClickListener(view -> {
            if ((i[0] -1)>=0) {
                i[0]--;
                if (ImageList.size() > 0)
                    Glide.with(requireActivity())
                            .load(ImageList.get(i[0]))
                            .into(imagePreview);
            }
        });
        rightBtn.setOnClickListener(view -> {
            if ((i[0]+1)<ImageList.size())
            i[0]++;
            if (ImageList.size() > 0)
                Glide.with(requireActivity())
                        .load(ImageList.get(i[0]))
                        .into(imagePreview);
        });
    }

    private void initializations() {
        toolbar = rootView.findViewById(R.id.postToolbar);
        sendBtn = rootView.findViewById(R.id.sendBtn);
        imagePreview = rootView.findViewById(R.id.imagePreview);
        leftBtn = rootView.findViewById(R.id.leftPreBtn);
        rightBtn = rootView.findViewById(R.id.rightPreBtn);
        titleText = rootView.findViewById(R.id.titleEditText);
        descText = rootView.findViewById(R.id.descriptionEditText);
        imagePreviewLayout = rootView.findViewById(R.id.imagePreviewLayout);
        removeImageBtn = rootView.findViewById(R.id.removeImageButton);
        user = FirebaseAuth.getInstance().getCurrentUser();
        fireStore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public void onPause() {
        super.onPause();
        ImageList.clear();
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