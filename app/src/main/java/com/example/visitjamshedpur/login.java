package com.example.visitjamshedpur;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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

import java.util.Objects;
import java.util.regex.Pattern;

public class login extends AppCompatActivity {

    TextInputLayout mEmail, mPassword;
    SignInButton signInButton;
    ProgressBar progressBar;
    String email, password;
    Button continueBtn;
    FirebaseAuth fAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private final int RC_SIGN_IN=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        (findViewById(R.id.registerText)).setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), register.class)));
        mEmail= findViewById(R.id.emailLogin);
        mPassword= findViewById(R.id.passwordLogin);
        continueBtn= findViewById(R.id.continueBtnLogin);
        progressBar= findViewById(R.id.progressBarLogin);
        signInButton= findViewById(R.id.googleSignInLogin);
        fAuth= FirebaseAuth.getInstance();
        continueBtn.setOnClickListener(view -> {
            email= Objects.requireNonNull(mEmail.getEditText()).getText().toString().trim();
            password= Objects.requireNonNull(mPassword.getEditText()).getText().toString().trim();

            if(!validateEmail(email) | !validatePassword(password)){

                if(!validateEmail(email)) {
                    mEmail.setError(null);
                    mEmail.setErrorEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    mEmail.setError("Please enter a valid email address");
                } else {
                    mEmail.setError(null);
                    mEmail.setErrorEnabled(false);
                }
                if(!validatePassword(password)) {
                    mPassword.setError(null);
                    mPassword.setErrorEnabled(true);
                    mPassword.setCounterEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    mPassword.setError("Enter a password");
                } else{
                    mPassword.setError(null);
                    mPassword.setErrorEnabled(false);
                    mPassword.setCounterEnabled(false);
                }
            }
            else{
                mEmail.setError(null);
                mEmail.setErrorEnabled(false);
                mPassword.setError(null);
                mPassword.setErrorEnabled(false);
                mPassword.setCounterEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                signInButton.setVisibility(View.GONE);
                fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(getApplicationContext(), "User logged in", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        progressBar.setVisibility(View.GONE);
                        finish();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        signInButton.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });

        (findViewById(R.id.forgotPasswordLogin)).setOnClickListener(view -> {
            EditText editText= new EditText(view.getContext());
            AlertDialog.Builder passwordResetDialog= new AlertDialog.Builder(view.getContext());
            passwordResetDialog.setTitle("Password Reset");
            passwordResetDialog.setMessage("Enter your Email-ID to receive password reset link.");
            passwordResetDialog.setView(editText);

            passwordResetDialog.setPositiveButton("Proceed", (dialogInterface, i) -> {
                String mail= editText.getText().toString().trim();
                fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(unused -> Toast.makeText(login.this, "Password reset link sent.", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(login.this, "Password reset link not sent. "+ e.getMessage(), Toast.LENGTH_SHORT).show());
            }).setNegativeButton("Cancel", (dialogInterface, i) -> {

            });
            passwordResetDialog.create().show();
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
            handleSignInResult(task);
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException ignored) {
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, "null");
        fAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        Toast.makeText(getApplicationContext(), "Signed in", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(getApplicationContext(),"Sign-in with google failed.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(),e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    public boolean validateEmail(String s){
        if(s==null || s.isEmpty()){
            return false;
        }
        String emailRegex= "^[a-zA-Z0-9_+&*-]+(?:\\."+"[a-zA-Z0-9_+&*-]+)*@"+"(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern= Pattern.compile(emailRegex);
        return pattern.matcher(s).matches();
    }
    public boolean validatePassword(String s){
        return s.length() > 0;
    }
}