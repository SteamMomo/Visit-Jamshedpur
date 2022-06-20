package com.example.visitjamshedpur;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;


public class Profile extends Fragment {

    private final ArrayList<PostItemModel> profilePosts = new ArrayList<>();
    private ImageView profilePic;
    private View rootView;
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private FirebaseUser user;
    private FirebaseFirestore fireStore;
    private TextView profileName, profileIdentity, profileContributions, profileLikes, profileEmail;
    private RecyclerView profileRecycler;
    private PostItemAdapter postItemAdapter;
    private GoogleSignInClient mGoogleSignInClient;

    public Profile() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        initializations();
        googleClient();
        toolbarOptions();
        setProfileData();
        changeProfileData();
        if (postItemAdapter == null || profilePosts.size() > 0)
            getPosts();
        return rootView;
    }

    private void getPosts() {
        profilePosts.clear();
        progressBar.setVisibility(View.VISIBLE);
        fireStore.collection("Posts")
                .whereEqualTo("pAuthor", user.getEmail())
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
                        profileRecycler.setHasFixedSize(true);
                        if (profilePosts.size() > 0) {
                            postItemAdapter = new PostItemAdapter(getContext(), profilePosts);
                            profileRecycler.setAdapter(postItemAdapter);
                            profileRecycler.setLayoutManager(new LinearLayoutManager(getContext()
                                    , RecyclerView.VERTICAL, false));
                        }
                    }).addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
                }
                progressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void toolbarOptions() {
        toolbar.inflateMenu(R.menu.profile_menu);
        toolbar.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.logout) {
                signOut();
            }
            if (menuItem.getItemId() == R.id.delete) {
                deleteAccount();
            }
            if (menuItem.getItemId() == R.id.resetPassword) {
                forgotPassword();
            }
            return false;
        });
    }

    private void forgotPassword() {
        EditText editText = new EditText(requireContext());
        AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(requireContext());
        passwordResetDialog.setTitle("Password Reset");
        passwordResetDialog.setMessage("Enter your Email-ID to receive password reset link.");
        passwordResetDialog.setView(editText);
        passwordResetDialog.setPositiveButton("Proceed", (dialogInterface, i) -> {
            String mail = editText.getText().toString().trim();
            FirebaseAuth.getInstance().sendPasswordResetEmail(mail).addOnSuccessListener(unused -> Toast.makeText(requireContext(), "Password reset link sent.", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Password reset link not sent. " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }).setNegativeButton("Cancel", (dialogInterface, i) -> {
        });
        passwordResetDialog.create().show();
    }

    private void deleteAccount() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(requireActivity());
        alertDialog.setTitle("Doing this will delete all your posts and account info!!!");
        alertDialog.setPositiveButton("Delete", ((dialogInterface, i) -> {
            DocumentReference doc = FirebaseFirestore
                    .getInstance()
                    .collection("users")
                    .document(Objects.requireNonNull(user.getEmail()));
            doc.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Map<String, Object> temp = new HashMap<>();
                    temp.put("uIdentity", null);
                    temp.put("uName", null);
                    temp.put("uProfile", null);
                    temp.put("uStorageProfileUrl", null);
                    temp.put("uLastNameChangeDate", null);
                    doc.update(temp).addOnSuccessListener(unused -> Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
            FirebaseFirestore
                    .getInstance()
                    .collection("Posts")
                    .whereEqualTo("pAuthor", user.getEmail())
                    .get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        String pId = documentSnapshot.getString("pId");
                        DocumentReference reference = FirebaseFirestore.getInstance().collection("Posts")
                                .document(pId);
                        reference.get().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                DocumentSnapshot documentSnapshot1 = task1.getResult();
                                int x = 0;
                                while (documentSnapshot1.getString("image-" + x + "-deleteUri") != null) {
                                    String deleteString = documentSnapshot1.getString("image-" + x + "-deleteUri");
                                    FirebaseStorage.getInstance()
                                            .getReference()
                                            .child("Visit Jamshedpur")
                                            .child("Posts")
                                            .child(Objects.requireNonNull(deleteString))
                                            .delete();
                                    x++;
                                }
                                reference.delete();
                            }
                        });
                    }
                } else {
                    Toast.makeText(requireContext(), "Could not delete all posts.", Toast.LENGTH_SHORT).show();
                }
            });
        })).setNegativeButton("Cancel", ((dialogInterface, i) -> {
        }));
        alertDialog.create().show();
    }

    private void changeProfileData() {
        profilePic.setOnClickListener(view -> startActivity(new Intent(getContext(), ProfilePicture.class)));
        profileName.setOnClickListener(view -> {
            DocumentReference db = fireStore
                    .collection("users")
                    .document(Objects.requireNonNull(user.getEmail()));
            db.get().addOnSuccessListener(documentSnapshot -> {
                boolean valid = false;
                if (documentSnapshot.get("uLastNameChangeDate") != null) {
                    Timestamp t = (Timestamp) (documentSnapshot.get("uLastNameChangeDate"));
                    long timestamp = t != null ? t.getSeconds() : 0;
                    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                    cal.setTimeInMillis(timestamp * 1000L);
                    String lastDate = DateFormat.format("dd-MM-yyyy HH:mm:ss", cal).toString();
                    cal.setTimeInMillis(System.currentTimeMillis());
                    String currentTime = DateFormat.format("dd-MM-yyyy HH:mm:ss", cal).toString();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                    try {
                        Date date1 = simpleDateFormat.parse(currentTime);
                        Date date2 = simpleDateFormat.parse(lastDate);

                        if (date1 != null) {
                            if (date2 != null) {
                                valid = printDifferenceProfile(date1, date2);
                            }
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else valid = true;
                if (valid) {
                    EditText editText = new EditText(view.getContext());
                    androidx.appcompat.app.AlertDialog.Builder nameChangeDialog = new AlertDialog.Builder(view.getContext());
                    nameChangeDialog.setTitle("Enter new Username.");
                    nameChangeDialog.setMessage("Attention!! Upon changing your username now, you won't be able to change it again within 15 days.");
                    nameChangeDialog.setView(editText);

                    nameChangeDialog.setPositiveButton("Change", (dialogInterface, i) -> {
                        String name = editText.getText().toString().trim();
                        if (name.length() >= 2) {
                            Map<String, Object> temp = new HashMap<>();
                            temp.put("uName", name);
                            temp.put("uLastNameChangeDate", FieldValue.serverTimestamp());
                            db.update(temp).addOnSuccessListener(unused -> Toast.makeText(getContext(), "Username changed.", Toast.LENGTH_SHORT).show()).addOnFailureListener(e ->
                                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
                            setProfileData();
                        } else {
                            Toast.makeText(getContext(), "Given username is invalid.", Toast.LENGTH_SHORT).show();
                        }
                    }).setNegativeButton("Cancel", (dialogInterface, i) -> {
                    });
                    nameChangeDialog.create().show();
                }
            });
        });
        profileIdentity.setOnClickListener(view -> {
            DocumentReference db = fireStore
                    .collection("users")
                    .document(Objects.requireNonNull(user.getEmail()));
            db.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.getString("uIdentity") == null) {
                    androidx.appcompat.app.AlertDialog.Builder nameChangeDialog = new AlertDialog.Builder(view.getContext());
                    nameChangeDialog.setTitle("Choose your identity.");
                    nameChangeDialog.setMessage("Attention!! You can only choose once.");
                    nameChangeDialog.setPositiveButton("Tourist", (dialogInterface, i) -> {
                        Map<String, Object> temp = new HashMap<>();
                        temp.put("uIdentity", "Tourist");
                        db.update(temp).addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
                        setProfileData();
                    }).setNeutralButton("Resident of Jamshedpur", ((dialogInterface, i) -> {
                        Map<String, Object> temp = new HashMap<>();
                        temp.put("uIdentity", "Resident");
                        db.update(temp).addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
                        setProfileData();
                    })).setNegativeButton("Local Guide", ((dialogInterface, i) -> {
                        Map<String, Object> temp = new HashMap<>();
                        temp.put("uIdentity", "Local Guide");
                        db.update(temp).addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
                        setProfileData();
                    }));
                    nameChangeDialog.create().show();
                }
            });
        });
    }

    private boolean printDifferenceProfile(Date startDate, Date endDate) {
        long different = startDate.getTime() - endDate.getTime();
        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        if (elapsedDays >= 15)
            return true;
        else {
            Toast.makeText(getContext(), "Cannot change username within 15 days. " + (15 - elapsedDays) + " days left.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @SuppressLint("SetTextI18n")
    private void setProfileData() {
        DocumentReference db = fireStore.collection("users").document(Objects.requireNonNull(user.getEmail()));
        db.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();

                final long[] contributions = {0};
                final long[] likes = {0};

                FirebaseFirestore
                        .getInstance()
                        .collection("Posts")
                        .whereEqualTo("pAuthor", user.getEmail())
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
                    db.update(user).addOnFailureListener(e -> Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show());
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    Glide.with(Profile.this).load(profileUrl)
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
                String name = documentSnapshot.getString("uName");
                if (name != null)
                    profileName.setText(name);
                String identity = documentSnapshot.getString("uIdentity");
                if (identity == null) profileIdentity.setText("Click here to set identity");
                else profileIdentity.setText(identity);
            }
        });
        profileEmail.setText(user.getEmail());
    }

    private void initializations() {
        profilePic = rootView.findViewById(R.id.profilePic);
        profileIdentity = rootView.findViewById(R.id.profileIdentity);
        profileName = rootView.findViewById(R.id.profileName);
        profileLikes = rootView.findViewById(R.id.likes);
        profileContributions = rootView.findViewById(R.id.contributions);
        progressBar = rootView.findViewById(R.id.progressBar);
        toolbar = rootView.findViewById(R.id.profileToolbar);
        profileEmail = rootView.findViewById(R.id.profileEmail);
        profileRecycler = rootView.findViewById(R.id.profileRecycler);
        user = FirebaseAuth.getInstance().getCurrentUser();
        fireStore = FirebaseFirestore.getInstance();
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(getContext(), "Logged out", Toast.LENGTH_SHORT).show();
        mGoogleSignInClient.signOut();
        startActivity(new Intent(getContext(), login.class));
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        trans.remove(Profile.this);
        trans.commit();
        manager.popBackStack();
    }

    void googleClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("775833077180-2t63d87dlvjknghjp6en56ftgg3mb4ef.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
    }

    @Override
    public void onResume() {
        super.onResume();
        setProfileData();
    }
}