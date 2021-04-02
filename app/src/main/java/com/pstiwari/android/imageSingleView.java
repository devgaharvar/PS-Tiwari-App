package com.pstiwari.android;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

public class imageSingleView extends AppCompatActivity {
    PhotoView imagesingle;
    String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_single_view);
        imagesingle = findViewById(R.id.imagesingle);
        imageUrl = getIntent().getStringExtra("url");
        if(!imageUrl.isEmpty()) {
            Picasso.get().load(imageUrl).placeholder(R.drawable.profile_image).into(imagesingle);
        } else {
            imagesingle.setImageResource(R.drawable.profile_image);
        }

    }
}