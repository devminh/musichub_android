package com.example.androidfinalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;


import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    //Declare audio recorder
    private static final String TAG = "@@@@";
    private static String fileName = null;
    MediaRecorder mediaRecorder = new MediaRecorder();

    String fileonserver;


    //Declare firebase db
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    //Declare songname,title funciton

    String getArtist="";
    String getSongName="";
    String streamnow_link="";
    String useremail="";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Nav configuration
        bottomNavigationView = findViewById(R.id.bottom_navi);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);



        GifImageView gmv = findViewById(R.id.soundwavegif);
        gmv.setVisibility(View.INVISIBLE);

        Button record_btn = findViewById(R.id.record_btn);

        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);

        // Use bounce interpolator with amplitude 0.2 and frequency 20
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.8, 400);

        myAnim.setInterpolator(interpolator);


        record_btn.startAnimation(myAnim);

        LinearLayout songresult = findViewById(R.id.songresult);
        songresult.setVisibility(View.INVISIBLE);

        Button stop_btn = findViewById(R.id.stop_btn);
        stop_btn.setVisibility(View.INVISIBLE);

        TextView stopsuggest = findViewById(R.id.stopsuggestion);
        stopsuggest.setVisibility(View.INVISIBLE);


        //get useremail when already signed in
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser theUser = mAuth.getCurrentUser();
        if (theUser !=null)
        {
            //String _UID = theUser.getEmail().toString();
            useremail = theUser.getEmail().toString();

        }


    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                    if(menuItem.getItemId() == R.id.action_profile){
                        gotoProfile();
                    }

                    else if(menuItem.getItemId() == R.id.action_search){
                        gotoSearch();
                    }

                    return true;

                }


                };





    private void gotoProfile(){
        Intent intent=new Intent(this,ProfileActivity.class);
        startActivity(intent);
    }

    private void gotoSearch(){
        Intent intent=new Intent(this,SearchActivity.class);
        startActivity(intent);
    }


    //Music recognition part

    public void recording(View view)  {

        TextView identifysongtxt = findViewById(R.id.identifysongtext);
        identifysongtxt.setText("Listening...");

        GifImageView gmv = findViewById(R.id.soundwavegif);
        gmv.setVisibility(View.VISIBLE);


        Button record_btn = findViewById(R.id.record_btn);
        record_btn.clearAnimation();
        record_btn.setVisibility(View.GONE);



        Button stop_btn = findViewById(R.id.stop_btn);
        stop_btn.setVisibility(View.VISIBLE);



        TextView stopsuggest = findViewById(R.id.stopsuggestion);
        stopsuggest.setVisibility(View.VISIBLE);

        // Record to the external cache directory for visibility
        fileName = getExternalCacheDir().getAbsolutePath();

        //Log.d("directory",fileName);
        String random_str = getAlphaNumericString(7);

        fileonserver = random_str +".mp3";

        fileName += "/" + fileonserver;

        Log.d("specific",fileName);

        mediaRecorder.setOutputFile(fileName);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);


        mediaRecorder.setOutputFile(fileName);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);


        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            Log.e("asdasdas", "prepare() failed");
        }

        mediaRecorder.start();




    }

    public void stoprecord(View view) throws ExecutionException, InterruptedException {
        mediaRecorder.stop();
        mediaRecorder.release();



        new UploadFileAsync(fileName).execute("");
        GifImageView gmv = findViewById(R.id.soundwavegif);
        gmv.setVisibility(View.INVISIBLE);

        TextView songname = findViewById(R.id.songname);
        TextView singername = findViewById(R.id.singername);
        Button streamnow_btn = findViewById(R.id.streamnow_btn);

        LinearLayout firstlayout = findViewById(R.id.firstlayout);
        firstlayout.setVisibility(View.INVISIBLE);

        LinearLayout songresult = findViewById(R.id.songresult);
        songresult.setVisibility(View.VISIBLE);
        Animation aniSlide = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_up);

        songresult.startAnimation(aniSlide);



        GetJSON a =  new GetJSON("https://api.audd.io/?api_token=f54327070d6e835059db20b7dd006e29&url=http://ngocchaugarden.com/finaltesting/finaltesting"+fileonserver);


        //GetJSON a =  new GetJSON("https://api.audd.io/?api_token=516b5c98c0aa3738c7e4a5a8b1cb6291&url=http://ngocchaugarden.com/finaltesting/tf.mp3");

        String rawJSON = a.execute().get();

        try {

            JSONObject obj = new JSONObject(rawJSON);

            //Log.d("My App", obj.toString());

            String status = obj.getString("status").trim();

            Log.d("letmeseethestatus",status);

            JSONObject obj_result = obj.getJSONObject("result");

            String artist = obj_result.getString("artist");
            String title = obj_result.getString("title");
            String song_link = obj_result.getString("song_link");

            Log.d("showmetheartist",artist);

            getArtist=artist;
            getSongName=title;
            streamnow_link = song_link;

        } catch (Throwable t) {
            Log.e("My App", "Could not parse malformed JSON: \"" + rawJSON + "\"");

        }

        if(getSongName != "") {
            songname.setText(getSongName);
            singername.setText(getArtist);


            if (useremail != "") {

                Map<String, Object> user = new HashMap<>();
                user.put("songname", getSongName);
                user.put("artist", getArtist);
                user.put("songlink", streamnow_link);
                user.put("usermail", useremail); //get from google sign in api


// Add a new document with a generated ID
                db.collection("audiohistory")
                        .add(user)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Erroradding document", e);
                            }
                        });
            }

        }


        else{
            TextView notfind = findViewById(R.id.notfind);
            notfind.setText("Can not find a song !");
            streamnow_btn.setVisibility(View.INVISIBLE);

        }


    }


    public void goToStream (View view) {
        goToUrl (streamnow_link);
    }

    private void goToUrl (String url) {
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }

    // function to generate a random string of length n
    static String getAlphaNumericString(int n)
    {

        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }

    public void restartoncreate(View view) {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    class MyBounceInterpolator implements android.view.animation.Interpolator {
        private double mAmplitude = 1;
        private double mFrequency = 10;

        MyBounceInterpolator(double amplitude, double frequency) {
            mAmplitude = amplitude;
            mFrequency = frequency;
        }

        public float getInterpolation(float time) {
            return (float) (-1 * Math.pow(Math.E, -time/ mAmplitude) *
                    Math.cos(mFrequency * time) + 1);
        }
    }


    public class User {

        public String username;
        public String email;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String username, String email) {
            this.username = username;
            this.email = email;
        }

    }





}