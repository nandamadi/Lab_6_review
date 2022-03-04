package com.example.lab_6_review;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new CatImages("https://cataas.com/cat?json=true").execute();
    }

    private class CatImages extends AsyncTask<String, Integer, String>{

        private String url;
        private Bitmap image;

        public CatImages(String url){
            this.url = url;
        }

        @Override
        protected String doInBackground(String... strings) {
            while(true){
                try {
                    HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
                    //wait for data:
                    InputStream response = urlConnection.getInputStream();

                    //JSON reading:
                    //Build the entire string response:
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response, "UTF-8"), 8);
                    StringBuilder sb = new StringBuilder();

                    String line = null;
                    while ((line = reader.readLine()) != null)
                    {
                        sb.append(line + "\n");
                    }
                    String result = sb.toString(); //result is the whole string

                    // convert string to JSON:
                    JSONObject catImage = new JSONObject(result);
                    String catURL = catImage.getString("url");
                    String catID = catImage.getString("id");

                    File file = new File(getFilesDir(), catID);
                    if(file.exists()){
                        Log.i("Main", "Cat with ID " + catID + "alredy exists" );
                        image = BitmapFactory.decodeFile(file.getPath());
                    } else {
                        Log.i("Main", "Downloading cat picture with ID " + catID);
                        urlConnection = (HttpURLConnection) new URL("https://cataas.com" + catURL).openConnection();
                        image = BitmapFactory.decodeStream(urlConnection.getInputStream());
                        image.compress(Bitmap.CompressFormat.JPEG, 100, new BufferedOutputStream(new FileOutputStream(file)));
                    }

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }


                for (int i = 0; i < 100; i++) {
                    try {
                        publishProgress(i);
                        Thread.sleep(30);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            ((ProgressBar)findViewById(R.id.progressBar2)).setProgress(values[0]);

            if (values[0] == 0){
                ((ImageView)findViewById(R.id.imageView)).setImageBitmap(image);
            }

        }
    }

}