package com.pstiwari.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ImageViewerActivity extends AppCompatActivity {

    private String imageUrl;
    RecyclerView imagesliderrec;
    ArrayList<String> imagelist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        /*imageView = findViewById(R.id.image_viewer);
        imageUrl = getIntent().getStringExtra("url");
        if(!imageUrl.isEmpty()) {
            Picasso.get().load(imageUrl).placeholder(R.drawable.profile_image).into(imageView);
        } else {
            imageView.setImageResource(R.drawable.profile_image);
        }*/
        imagesliderrec=findViewById(R.id.imagesliderre);
        imagelist = (ArrayList<String>) getIntent().getSerializableExtra("mylist");
        imageUrl = getIntent().getStringExtra("url");
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        layoutManager.setStackFromEnd(true);

        SnapHelper snapHelper = new PagerSnapHelper();
        imagesliderrec.setLayoutManager(layoutManager);
        snapHelper.attachToRecyclerView(imagesliderrec);
        imagesliderrec.setAdapter(new ImageSliderAdpater(this,imagelist));
    }
}