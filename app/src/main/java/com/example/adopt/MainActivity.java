package com.example.adopt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.ListView;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.net.Uri;

public class MainActivity extends AppCompatActivity {

    private static final int LIST_PETS = 1;
    private PetArrayAdapter adapter = null;
    private String stat = "";
    private ProgressDialog progressDialog;
    private boolean isFin = false;
    private Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LIST_PETS: {
                    List<Pet> pets = (List<Pet>)msg.obj;
                    refreshPetList(pets);
                    break;
                }
            }
        }
    };

    private void refreshPetList(List<Pet> pets) {
        adapter.clear();
        adapter.addAll(pets);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseCrash.log("Here comes the exception!!!");
        FirebaseCrash.report(new Exception("Wrong"));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView lvPets = (ListView) findViewById(R.id.listview_pet);

        adapter = new PetArrayAdapter(this, new ArrayList<Pet>());
        lvPets.setAdapter(adapter);
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        getStatFromFirebase();
        getPetsFromFirebase();
        //while(isFin == false);


        FloatingActionButton fab = findViewById(R.id.hotzone);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, stat, Toast.LENGTH_SHORT).show();
                //openhotzone();
            }
        });

        Uri lineLink = getIntent().getData();

        if (lineLink != null) {
            String path = lineLink.toString();
            Toast.makeText(MainActivity.this, "path=" + path, Toast.LENGTH_LONG).show();

            WebView webView = (WebView) findViewById(R.id.webView);
            webView.loadUrl(path);
        }
        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
    }

    //open new activity
    public void openhotzone()
    {
        Intent intent = new Intent(this, MainActivity2.class);
        startActivity(intent);
    }

    //1.讀取firebase資料
    private void getPetsFromFirebase()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //DatabaseReference myRef = database.getReference("");
        DatabaseReference myRef = database.getReference("/data");        //
        //DatabaseReference myRef = database.getReference("/statistic");
        myRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                new FirebaseThread(dataSnapshot).start();

                /*for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    new FirebaseThread(dataSnapshot).start();
                }*/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.v("adoptpet", databaseError.getMessage());
            }
        });
    }

    private void getStatFromFirebase(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("/statistic");
        myRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    Log.v("stat", ds.getKey().toString() + ": " + ds.getValue().toString());
                    stat = stat + ds.getKey().toString() + ": " + ds.getValue().toString() + "\n";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.v("statistic", databaseError.getMessage());
            }
        });

    }

    //2.讀取firebase資料(子項目)
    class FirebaseThread extends Thread
    {

        private DataSnapshot dataSnapshot;

        public FirebaseThread(DataSnapshot dataSnapshot) {
            this.dataSnapshot = dataSnapshot;
        }

        @Override
        public void run()
        {
            /*String[] pic = new String[2];
            for(int i=0;i<2;i++)
                pic[i] = "";*/
            List<Pet> lsPets = new ArrayList<>();
            for (DataSnapshot ds : dataSnapshot.getChildren())
            {
                Bitmap petImg;

                /*DataSnapshot dsSName = ds.child("shelter_name");
                DataSnapshot dsAKind = ds.child("animal_kind");
                DataSnapshot dsImg = ds.child("album_file");
                DataSnapshot dsTel = ds.child("shelter_tel");*/    //


                DataSnapshot dsSName = ds.child("contents");
                DataSnapshot dsAKind = ds.child("location");
                DataSnapshot dsImg = ds.child("imgs_url");
                DataSnapshot dsTel = ds.child("timeStamps");

                String shelterName = (String)dsSName.getValue();            //shelter
                String kind = (String)dsAKind.getValue();                   //kind
                String tel = (String)dsTel.getValue(); //add
                String imgUrl = (String) dsImg.getValue();                  //pic

                if(imgUrl.contains(","))
                {
                    String[] pic = imgUrl.split(",");
                    imgUrl = pic[0];
                }
                /*
                Log.d("shelterName", shelterName);
                Log.d("kind", kind);
                Log.d("tel", tel);
                Log.d("imgUrl", imgUrl);
                */

                /*String[] pic = imgUrl.split(",");
                imgUrl = pic[0];*/

                /*if(imgUrl.contains(","))
                {
                    pic = imgUrl.split(",");
                    imgUrl = pic[0];
                    //petImg = getImgBitmap(pic[0]);
                }*/

                petImg = getImgBitmap(imgUrl);

                //Bitmap petImg = getImgBitmap(pic[1]);

                Pet aPet = new Pet();                                       //存入list, send data
                aPet.setShelter(shelterName);
                aPet.setKind(kind);
                aPet.setImgUrl(petImg);
                aPet.setTel(tel);   //add

                lsPets.add(aPet);

                Log.v("adoptpet", shelterName + ";" + kind + ";" + imgUrl);
            }
            progressDialog.dismiss();
            Message msg = new Message();
            msg.what = LIST_PETS;
            msg.obj = lsPets;
            handler.sendMessage(msg);
        }
    }

    //private Bitmap getImgBitmap
    private Bitmap getImgBitmap(String imgUrl)
    {

        //String[] pic = imgUrl.split(",");
        //imgUrl = pic[0];

        try{
            URL url = new URL(imgUrl);
            Bitmap bm = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            return bm;
        }catch(MalformedURLException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    //show data
    class PetArrayAdapter extends ArrayAdapter<Pet>
    {
        Context context;

        public PetArrayAdapter(Context context, List<Pet> items)
        {
            super(context, 0, items);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            LayoutInflater inflater = LayoutInflater.from(context);
            LinearLayout itemlayout = null;
            if (convertView == null) {
                itemlayout = (LinearLayout) inflater.inflate(R.layout.pet_item, null);
            } else {
                itemlayout = (LinearLayout) convertView;
            }
            Pet item = (Pet) getItem(position);

            TextView tvShelter = (TextView) itemlayout.findViewById(R.id.tv_shelter);
            tvShelter.setText(item.getShelter());

            TextView tvKind = (TextView) itemlayout.findViewById(R.id.tv_kind);
            tvKind.setText(item.getKind());

            TextView tvTel = (TextView) itemlayout.findViewById(R.id.tv_tel);       //add
            tvTel.setText(item.getTel());

            ImageView ivPet = (ImageView) itemlayout.findViewById(R.id.iv_pet);
            ivPet.setImageBitmap(item.getImgUrl());

            return itemlayout;
        }
    }

}