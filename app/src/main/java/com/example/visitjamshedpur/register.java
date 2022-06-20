package com.example.visitjamshedpur;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class register extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1;
    TextInputLayout mUsername, mEmail, mPassword;
    LinearLayout loginText;
    Button continueBtn;
    SignInButton signInButton;
    ProgressBar progressBar;
    String userName, email, password;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    private DocumentReference documentReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        signInButton = findViewById(R.id.googleSignIn);
        mUsername = findViewById(R.id.userName);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        continueBtn = findViewById(R.id.continueBtn);
        progressBar = findViewById(R.id.progressBar);
        loginText = findViewById(R.id.loginText);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();


        loginText.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), login.class));
            finish();
        });
        continueBtn.setOnClickListener(view -> {
            userName = Objects.requireNonNull(mUsername.getEditText()).getText().toString().trim();
            email = Objects.requireNonNull(mEmail.getEditText()).getText().toString().trim();
            password = Objects.requireNonNull(mPassword.getEditText()).getText().toString().trim();

            if (!validateUserName(userName) | !validateEmail(email) | !validatePassword(password)) {

                if (!validateUserName(userName)) {
                    mUsername.setError(null);
                    mUsername.setErrorEnabled(true);
                    mUsername.setCounterEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    mUsername.setError("Username too short");
                } else {
                    mUsername.setError(null);
                    mUsername.setCounterEnabled(false);
                    mUsername.setErrorEnabled(false);
                }
                if (!validateEmail(email)) {
                    mEmail.setError(null);
                    mEmail.setErrorEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    mEmail.setError("Please enter a valid email address");
                } else {
                    mEmail.setError(null);
                    mEmail.setErrorEnabled(false);
                }
                if (!validatePassword(password)) {
                    mPassword.setError(null);
                    mPassword.setErrorEnabled(true);
                    mPassword.setCounterEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    mPassword.setError("Password should be at least 6 characters long");
                } else {
                    mPassword.setError(null);
                    mPassword.setErrorEnabled(false);
                    mPassword.setCounterEnabled(false);
                }
            } else {
                mUsername.setError(null);
                mUsername.setCounterEnabled(false);
                mUsername.setErrorEnabled(false);
                mEmail.setError(null);
                mEmail.setErrorEnabled(false);
                mPassword.setError(null);
                mPassword.setErrorEnabled(false);
                mPassword.setCounterEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                signInButton.setVisibility(View.GONE);
                continueBtn.setVisibility(View.INVISIBLE);
                if (firebaseAuth.getCurrentUser() != null && !firebaseAuth.getCurrentUser().isEmailVerified()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(register.this, "Please verify the email with the link sent to you.", Toast.LENGTH_SHORT).show();
                }
                if (firebaseAuth.getCurrentUser() == null) {
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            firebaseAuth.getCurrentUser().sendEmailVerification().addOnSuccessListener(unused -> {
                                Toast.makeText(register.this, "Email verification link sent to " + email, Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getApplicationContext(), login.class);
                                documentReference = firebaseFirestore.collection("users").document(email);
                                Map<String, Object> user = new HashMap<>();
                                user.put("uName", userName);
                                user.put("uEmail", email);
                                user.put("uTimestamp", FieldValue.serverTimestamp());
                                user.put("uIdentity", null);
                                documentReference.set(user).addOnSuccessListener(unused1 -> Toast.makeText(register.this, "User data added to cloud.", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(register.this, e.getMessage(), Toast.LENGTH_SHORT).show());
                                startActivity(intent);
                                progressBar.setVisibility(View.GONE);
                                continueBtn.setVisibility(View.VISIBLE);
                                finish();
                            }).addOnFailureListener(e -> {
                                continueBtn.setVisibility(View.VISIBLE);
                                Toast.makeText(register.this, "Email verification link not sent. " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });

                        } else {
                            Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            signInButton.setVisibility(View.VISIBLE);
                            continueBtn.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("775833077180-2t63d87dlvjknghjp6en56ftgg3mb4ef.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(view -> signIn());
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
                firebaseAuthWithGoogle(account.getIdToken());

            } catch (ApiException ignored) {
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, "null");
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        Toast.makeText(getApplicationContext(), "Signed in", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(getApplicationContext(), "Sign-in with google failed.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public boolean validateUserName(String s) {
        return s.length() >= 3;
    }

    public boolean validateEmail(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." + "[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(s).matches();
    }

    public boolean validatePassword(String s) {
        return s.length() >= 6;
    }
}