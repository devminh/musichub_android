package com.example.androidfinalproject;

import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class ProfileActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final int RC_SIGN_IN =1 ;
    Button logoutBtn;
    SignInButton signInButton;
    TextView userName,userEmail,userId;
    ImageView profileImage;
    String useremail;


    //Declare Google Sign-in Var
    private static final String bag = "MainActivity";

    private GoogleApiClient googleApiClient;

    String name, email;
    String idToken;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser myuser = mAuth.getCurrentUser();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        logoutBtn = findViewById(R.id.logoutBtn);
        userName = findViewById(R.id.name);
        userEmail = findViewById(R.id.email);

        //Google Sign-in Part


        firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
        //this is where we start the Auth state Listener to listen for whether the user is signed in or not
        authStateListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // Get signedIn user
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //if user is signed in, we call a helper method to save the user details to Firebase
                if (user != null) {
                    // User is signed in
                    // you could place other firebase code
                    //logic to save the user details to Firebase
                    Log.d(bag, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(bag, "onAuthStateChanged:signed_out");
                }
            }
        };

        GoogleSignInOptions gso =  new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))//you can also use R.string.default_web_client_id
                .requestEmail()
                .build();
        googleApiClient=new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();




        //get useremail when already signed in
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser theUser = mAuth.getCurrentUser();
        if (theUser !=null)
        {
            //String _UID = theUser.getEmail().toString();
            useremail = theUser.getEmail().toString();


        }

        userEmail.setText(useremail);

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                if (status.isSuccess()){
                                    gotoMainActivity();
                                }else{
                                    Toast.makeText(getApplicationContext(),"Session not close",Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });


        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent,RC_SIGN_IN);
            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result){
        if(result.isSuccess()){
            GoogleSignInAccount account = result.getSignInAccount();
            idToken = account.getIdToken();
            name = account.getDisplayName();
            email = account.getEmail();


            // you can store user data to SharedPreference
            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
            firebaseAuthWithGoogle(credential);
        }else{
            // Google Sign In failed, update UI appropriately
            Log.e(bag, "Login Unsuccessful. "+result);
            Toast.makeText(this, "Login Unsuccessful", Toast.LENGTH_SHORT).show();
        }
    }


    private void firebaseAuthWithGoogle(AuthCredential credential){

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(bag, "signInWithCredential:onComplete:" + task.isSuccessful());
                        if(task.isSuccessful()){
                            Toast.makeText(ProfileActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                            gotoMainActivity();
                        }else{
                            Log.w(bag, "signInWithCredential" + task.getException().getMessage());
                            task.getException().printStackTrace();
                            Toast.makeText(ProfileActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }








    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(currentUser != null){
            signInButton = findViewById(R.id.sign_in_button);
            signInButton.setVisibility(View.INVISIBLE);


        }
        else{
            Button aud_btn = findViewById(R.id.aud_btn);
            aud_btn.setVisibility(View.INVISIBLE);
            logoutBtn.setVisibility(View.INVISIBLE);


        }


    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }









    private void gotoMainActivity(){
        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
    }
    private void gotoAudioHistoryActivity(){
        Intent intent=new Intent(this,AudioHistoryActivity.class);
        startActivity(intent);
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void backToHome(View view) {

        gotoMainActivity();
    }


    public void goToAudioHistory(View view) {
        gotoAudioHistoryActivity();

    }


}