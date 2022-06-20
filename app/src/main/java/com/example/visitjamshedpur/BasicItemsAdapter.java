package com.example.visitjamshedpur;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

public class BasicItemsAdapter extends RecyclerView.Adapter<BasicItemsAdapter.MyViewHolder> {
    private final Context mContext;
    private ArrayList<BasicListItem> mFiles;

    BasicItemsAdapter(Context mContext, ArrayList<BasicListItem> mFiles) {
        this.mContext = mContext;
        this.mFiles = mFiles;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.basic_list_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (holder.recyclerView != null)
            holder.recyclerView.removeAllViews();
        holder.title.setText(mFiles.get(position).getTitle());
        holder.address.setText(mFiles.get(position).getAddress());
        Glide.with(mContext).load(mFiles.get(position).getImage1()).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                holder.progressBar.setVisibility(View.GONE);
                Log.wtf("here", e.getMessage());
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                holder.progressBar.setVisibility(View.GONE);
                return false;
            }
        }).into(holder.imageViewOne);
        holder.rightBtn.setOnClickListener(view -> {
            Intent intent;
            intent = new Intent(mContext, MainActivity2.class);
            intent.putExtra("ID", mFiles.get(position).getmId());
            intent.putExtra("aName", mFiles.get(position).getTitle());
            intent.putExtra("aAddress", mFiles.get(position).getAddress());
            view.getContext().startActivity(intent);
        });
        holder.linearLayout.setOnClickListener(view -> {
            Intent intent;
            intent = new Intent(mContext, MainActivity2.class);
            intent.putExtra("ID", mFiles.get(position).getmId());
            intent.putExtra("aName", mFiles.get(position).getTitle());
            intent.putExtra("aAddress", mFiles.get(position).getAddress());
            view.getContext().startActivity(intent);
        });
        holder.toGMap.setOnClickListener(view -> {
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(mFiles.get(position).getTitle()
                    + ", Jamshedpur" + ", Jharkhand"));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            view.getContext().startActivity(mapIntent);
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(ArrayList<BasicListItem> temp) {
        mFiles = temp;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewOne, toGMap;
        LinearLayout linearLayout;
        ImageView rightBtn;
        TextView title, address;
        RecyclerView recyclerView;
        ProgressBar progressBar;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewOne = itemView.findViewById(R.id.imageOne);
            title = itemView.findViewById(R.id.basicTitle);
            address = itemView.findViewById(R.id.basicAddress);
            rightBtn = itemView.findViewById(R.id.rightBtn);
            linearLayout = itemView.findViewById(R.id.basicItemTitleBar);
            toGMap = itemView.findViewById(R.id.toGMap);
            recyclerView = itemView.findViewById(R.id.attractionFrag);
            progressBar = itemView.findViewById(R.id.itemProgressBar);
        }
    }
}
