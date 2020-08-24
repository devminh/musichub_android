package com.example.androidfinalproject;

import android.content.Context;
import android.content.Intent;

import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class SearchActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser myuser = mAuth.getCurrentUser();
    String useremail="";


    //Declare firebase db
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seach);

        //Nav configuration
        bottomNavigationView = findViewById(R.id.bottom_navi);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);


        //get useremail when already signed in
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser theUser = mAuth.getCurrentUser();
        if (theUser !=null)
        {
            //String _UID = theUser.getEmail().toString();
            useremail = theUser.getEmail().toString();

        }




        db.collection("searchhistory")
                .whereEqualTo("usermail", useremail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        final List<String> songlist = new ArrayList<>();


                        String keyword="";


                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                keyword = (String) document.getData().get("keyword");


                                songlist.add(keyword);


                            }

                            if (keyword != "") {
                                ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.custom_textview, songlist);

                                ListView listView = findViewById(R.id.searchhistory_list);
                                listView.setAdapter(adapter);


                                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                            String kw = songlist.get(i);
                                            EditText searchbox = findViewById(R.id.searchbox);
                                        searchbox.setText(kw);
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

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                    if(menuItem.getItemId() == R.id.action_profile){
                        gotoProfile();
                    }

                    else if(menuItem.getItemId() == R.id.action_audio_recog){
                        gotoMain();
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

    private void gotoMain(){
        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }


    public void search(View view) throws ExecutionException, InterruptedException {
        TextView noteaccess = findViewById(R.id.noteaccess);
        noteaccess.setVisibility(View.GONE);

        EditText searchbox = findViewById(R.id.searchbox);
        String keyword = searchbox.getText().toString();

        ListView listView =  findViewById(R.id.searchhistory_list);
        listView.setVisibility(View.GONE);

        if (useremail != "") {

            Map<String, Object> user = new HashMap<>();
            user.put("keyword", keyword);

            user.put("usermail", useremail); //get from google sign in api


// Add a new document with a generated ID
            db.collection("searchhistory")
                    .add(user)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("TAG", "DocumentSnapshot added with ID: " + documentReference.getId());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("TAG", "Erroradding document", e);
                        }
                    });
        }

        GetJSON a =  new GetJSON("https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&q="+keyword+"&key=AIzaSyD_Je9CM7Js22DQfHZMNdfpwFM2E8W57PQ");
        String rawJSON = a.execute().get();

        //3 BIEN NAY DE SET 1 LIST VIEW
        String songlink="";
        String title="";
        String imgurl="";

        ListView you = findViewById(R.id.youtubelist);
        ArrayList<YoutuveObject> arrayList = new ArrayList<YoutuveObject>();
        CustomAdapter customAdapter = new CustomAdapter(this, arrayList);

        try {

            JSONObject obj = new JSONObject(rawJSON);

            Log.d("youtubeapi:", obj.toString());

            JSONArray items = obj.getJSONArray("items");


            for(int i =0;i<3;i++)
            {
                JSONObject i1 = items.getJSONObject(i);

                JSONObject id1 = i1.getJSONObject("id");
                String videoid1= id1.getString("videoId");

                songlink = "https://www.youtube.com/watch?v="+videoid1;

                Log.d("songlink",songlink);


                JSONObject snippet1 = i1.getJSONObject("snippet");

                title = snippet1.getString("title");
                Log.d("songtitle",title);


                JSONObject thumnail = snippet1.getJSONObject("thumbnails");

                imgurl = thumnail.getJSONObject("medium").getString("url");
                Log.d("imgurl",imgurl);
                arrayList.add(new YoutuveObject(songlink,title,imgurl));
            }
            you.setAdapter(customAdapter);


        } catch (Throwable t) {
            Log.e("My App", "Could not parse malformed JSON: \"" + rawJSON + "\"");

        }

        //Get AccessToken from https://developer.spotify.com/console/get-search-item/?q=abba&type=track&market=US&limit=&offset=
        //Select Scope : user-follow-read


        String accesstoken="";


        GetJSON b =  new GetJSON("https://api.spotify.com/v1/search?type=track&limit=3&q="+keyword+"&access_token="+accesstoken);
        String spotifyrawJSON;

        spotifyrawJSON = b.execute().get();

        ListView spo = findViewById(R.id.Spotifylist);
        ArrayList<YoutuveObject> spoarrayList = new ArrayList<YoutuveObject>();
        CustomAdapter spocustomAdapter = new CustomAdapter(this, spoarrayList);

        if(spotifyrawJSON != null) {
            Log.d("spotifyrawJSON",spotifyrawJSON.toString());


            try {

                JSONObject spotifyobj = new JSONObject(spotifyrawJSON);

                //Log.d("spotifyapi:", spotifyobj.toString());

                JSONArray item = spotifyobj.getJSONObject("tracks").getJSONArray("items");
                Log.d("itemarr", item.toString());

                for (int i = 0; i < 3; i++) {
                    //Get item1 :
                    JSONObject it1 = item.getJSONObject(i);
                    Log.d("it1:", it1.toString());

                    String sptf_songname1 = it1.getString("name");
                    String sptf_artist = it1.getJSONObject("album").getJSONArray("artists").getJSONObject(0).getString("name");
                    sptf_songname1 = sptf_songname1 + " - "+sptf_artist;

                    String spotifylink1 = it1.getJSONObject("external_urls").getString("spotify");
                    String spotify_imglink1 = it1.getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url");


                    Log.d("spotifyname", sptf_songname1);
                    Log.d("spotifylink", spotifylink1);
                    Log.d("spotify_imglink1", spotify_imglink1);
                    spoarrayList.add(new YoutuveObject(spotifylink1, sptf_songname1, spotify_imglink1));
                }

            } catch (Throwable t) {
                //Log.e("My App", "Could not parse malformed JSON: \"" + spotifyrawJSON + "\"");
            }
            spo.setAdapter(spocustomAdapter);
            RelativeLayout relativeLayout = findViewById(R.id.Songlist);
            relativeLayout.setVisibility(View.VISIBLE);
        }
        else{
            spo.setVisibility(View.GONE);

        }


    }



    private void goToUrl (String url) {
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }


}

class YoutuveObject{
    String songlink;
    String title;
    String imgurl;
    public YoutuveObject(String songlink, String title, String imgurl) {
        this.songlink = songlink;
        this.title = title;
        this.imgurl = imgurl;
    }

    public String getSonglink() {
        return songlink;
    }
}


class CustomAdapter implements ListAdapter {
    ArrayList<YoutuveObject> arrayList;
    Context context;
    public CustomAdapter(Context context, ArrayList<YoutuveObject> arrayList) {
        this.arrayList=arrayList;
        this.context=context;
    }
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }
    @Override
    public boolean isEnabled(int position) {
        return true;
    }
    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
    }
    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
    }
    @Override
    public int getCount() {
        return arrayList.size();
    }
    @Override
    public Object getItem(int position) {
        return position;
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public boolean hasStableIds() {
        return false;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        YoutuveObject subjectData = arrayList.get(position);
        if(convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.single_item_list, null);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            TextView tittle = convertView.findViewById(R.id.Songtitle);
            ImageView imag = convertView.findViewById(R.id.Songimage);
//            TextView link = convertView.findViewById(R.id.Songlink);
            tittle.setText(subjectData.title);
            tittle.setText(Html.fromHtml("<a href=\""+ subjectData.songlink + "\">" + subjectData.title +"</a>"));
            tittle.setClickable(true);
            tittle.setMovementMethod (LinkMovementMethod.getInstance());




//            link.setText(subjectData.songlink);
            Picasso.with(context)
                    .load(subjectData.imgurl)
                    .into(imag);
        }
        return convertView;
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }
    @Override
    public int getViewTypeCount() {
        return arrayList.size();
    }
    @Override
    public boolean isEmpty() {
        return false;
    }


    public String getLink(int position) {
        YoutuveObject subjectData = arrayList.get(position);
        return subjectData.getSonglink();
    }


}