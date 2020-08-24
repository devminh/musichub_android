package com.example.androidfinalproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SongResultFragment extends Fragment {
    String songtitle;
    String artistname;
    String streamnow_link;
    String status;

    public SongResultFragment(String json_status, String title, String artist, String song_link){

        status = json_status;
        songtitle=title;
        artistname=artist;
        streamnow_link = song_link;


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_songresult,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        TextView songname = getView().findViewById(R.id.songname);
        TextView singername = getView().findViewById(R.id.singername);
        Button streamnow_btn = getView().findViewById(R.id.streamnow_btn);

        if(status == "success"){
            songname.setText(songtitle);
            singername.setText(artistname);

        }
        else{
            songname.setText("Can not find the song !");
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
}
