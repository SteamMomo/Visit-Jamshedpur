package com.example.visitjamshedpur;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    GoogleSignInAccount account;
    BottomNavigationView bottomNavigation;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private DocumentReference documentReference;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        googleClient();
        initializations();
        DatabaseUpdateEntry();
        bottomNavigationListeners();

        //signOutButton.setOnClickListener(view -> signOut());
        //findViewById(R.id.adminBtn).setOnClickListener(V -> startActivity(new Intent(getApplicationContext(), EmailNotVerified.class)));
    }


    private void bottomNavigationListeners() {
        loadFragment(new Attractions());

        bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.tour) {
                loadFragment(new Attractions());
            } else if (item.getItemId() == R.id.explore) {
                loadFragment(new Discover());
            } else if (item.getItemId() == R.id.add) {
                loadFragment(new Add());
            } else if (item.getItemId() == R.id.profile) {
                loadFragment(new Profile());
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.viewPager, fragment)
                .commit();
    }

    private void initializations() {
        firebaseAuth = FirebaseAuth.getInstance();
        account = GoogleSignIn.getLastSignedInAccount(this);
        bottomNavigation = findViewById(R.id.bottomNavigationView);
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(getApplicationContext(), login.class));
            finish();
        }
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseAuth.getInstance().getCurrentUser().reload();
            firebaseAuth = FirebaseAuth.getInstance();
            if (!Objects.requireNonNull(firebaseAuth.getCurrentUser()).isEmailVerified()) {
                startActivity(new Intent(getApplicationContext(), login.class));
                EmailVerificationFnc();
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(getApplicationContext(), "Check your inbox for email verification link if you haven't verified.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    void googleClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("775833077180-2t63d87dlvjknghjp6en56ftgg3mb4ef.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(getApplicationContext(), "Logged out", Toast.LENGTH_SHORT).show();
        mGoogleSignInClient.signOut();
        startActivity(new Intent(getApplicationContext(), login.class));
        finish();
    }

    void EmailVerificationFnc() {
        if (firebaseAuth.getCurrentUser() != null && !firebaseAuth.getCurrentUser().isEmailVerified()) {
            firebaseAuth.getCurrentUser().sendEmailVerification().addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Email verification link not sent. " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

    void DatabaseUpdateEntry() {
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser != null) {
            documentReference = firebaseFirestore.collection("users").document(Objects.requireNonNull(mUser.getEmail()));
            Map<String, Object> user = new HashMap<>();
            user.put("uEmail", mUser.getEmail());
            documentReference.update(user).addOnFailureListener(e -> {
                Map<String, Object> user1 = new HashMap<>();
                user1.put("uName", mUser.getDisplayName());
                user1.put("uEmail", mUser.getEmail());
                user1.put("uIdentity", null);
                documentReference.set(user1).addOnSuccessListener(unused -> Toast.makeText(MainActivity.this, "User data added to the cloud.", Toast.LENGTH_SHORT).show()).addOnFailureListener(e1 -> Toast.makeText(MainActivity.this, e1.getMessage(), Toast.LENGTH_SHORT).show());
            });
        }

    }
}