package com.pstiwari.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ImageSliderAdpater extends RecyclerView.Adapter<ImageSliderAdpater.ViewHolder> {

    private Context mContext ;
    private ArrayList<String> chatdata ;

    public ImageSliderAdpater(Context mContext, ArrayList<String> chatdata) {
        this.mContext = mContext;
        this.chatdata = chatdata;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.imagesliderdesgn, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

            Picasso.get().load(chatdata.get(position)).placeholder(R.drawable.profile_image).into(holder.photoView);

            }


    @Override
    public int getItemCount() {
        return chatdata.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        PhotoView photoView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.imagelist);
        }
    }
}
