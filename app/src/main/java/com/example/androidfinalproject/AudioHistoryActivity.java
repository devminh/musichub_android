package com.example.androidfinalproject;

import android.content.Intent;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AudioHistoryActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser myuser = mAuth.getCurrentUser();
    String useremail="";


    //Declare firebase db
    FirebaseFirestore db = FirebaseFirestore.getInstance();





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audiohistory);

        //get useremail when already signed in
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser theUser = mAuth.getCurrentUser();
        if (theUser !=null)
        {
            //String _UID = theUser.getEmail().toString();
            useremail = theUser.getEmail().toString();

        }




        db.collection("audiohistory")
                .whereEqualTo("usermail", useremail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<String> songlist = new ArrayList<>();
                        final List<String> songlink = new ArrayList<>();

                        String songname="";
                        String artist="";
                        String link="";

                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("showmejson:", document.getId() + " => " + document.getData().get("songname"));

                                //historylist = historylist + document.get("songname") + "/" + " ";
                                songname = (String) document.getData().get("songname");
                                artist = (String) document.getData().get("artist");
                                link = (String) document.getData().get("songlink");

                                songlist.add(songname + " - " + artist);
                                songlink.add(link);

                            }

                            if (songname != "") {
                                ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.custom_textview, songlist);

                                ListView listView = findViewById(R.id.history_list);
                                listView.setAdapter(adapter);


                                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                                        String url = songlink.get(i);
                                        goToUrl(url);


                                    }
                                });
                            }
                        }

                        else {
                            Log.d("showmejson:", "Error getting documents: ", task.getException());
                        }

                    }

                });








    }



    private void gotoProfileActivity(){
        Intent intent=new Intent(this,ProfileActivity.class);
        startActivity(intent);
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void backToHome(View view) {

        gotoProfileActivity();
    }

    private void goToUrl (String url) {
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }


}