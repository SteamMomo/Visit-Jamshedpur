package com.example.visitjamshedpur;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class CommentItemAdapter extends RecyclerView.Adapter<CommentItemAdapter.MyViewHolder> {

    private final Context context;
    FirebaseUser user;
    private ArrayList<CommentItemModel> mFiles;

    public CommentItemAdapter(Context context, ArrayList<CommentItemModel> mFiles) {
        this.context = context;
        this.mFiles = mFiles;
    }

    @NonNull
    @Override
    public CommentItemAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.comment_item_layout, parent, false);
        return new CommentItemAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentItemAdapter.MyViewHolder holder, int i) {
        holder.commentImageViewer.setVisibility(View.GONE);
        String email = mFiles.get(i).authorID;
        if (email.equals(user.getEmail())) holder.commentDelete.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Glide.with(context)
                                .load(task.getResult().getString("uProfile"))
                                .into(holder.commentProfile);
                        holder.commentAuthor.setText(task.getResult().getString("uName"));
                    }
                });
        holder.commentAuthorIdentity.setText(mFiles.get(i).authorIdentity);
        holder.comment.setText(mFiles.get(i).comment);
        holder.commentTime.setText(mFiles.get(i).time);
        holder.commentDelete.setOnClickListener(view -> deleteComment(mFiles.get(i).cID, mFiles.get(i).pID, i));
        holder.commentAuthorDescription.setOnClickListener(view -> profileIntent(mFiles.get(i).authorID));
        holder.commentProfileCard.setOnClickListener(view -> profileIntent(mFiles.get(i).authorID));
        String imageUri = mFiles.get(i).downUri;
        if(mFiles.get(i).flag.equals("comment")) holder.commentImageViewer.setVisibility(View.GONE);
        if(imageUri!=null && mFiles.get(i).flag.equals("review")) {
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.commentImageViewer.setVisibility(View.VISIBLE);
            Glide.with(context).load(imageUri)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            holder.progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(holder.commentImage);
        }
    }

    private void profileIntent(String email) {
        Intent intent = new Intent(context, ProfileThirdParty.class);
        intent.putExtra("email", email);
        context.startActivity(intent);
    }

    private void deleteComment(String cID, String pID, int it) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        if(mFiles.get(it).flag.equals("review"))
        {
            dialog.setTitle("Do you want to delete this review?");
            dialog.setPositiveButton("Yes", ((dialogInterface, i) -> {
                FirebaseFirestore
                        .getInstance()
                        .collection("Attractions")
                        .document(mFiles.get(it).pID)
                        .collection("reviews")
                        .document(mFiles.get(it).cID)
                        .delete();
                String deleteUri = mFiles.get(it).imageUri;
                if(deleteUri!=null)
                FirebaseStorage
                        .getInstance()
                        .getReference()
                        .child("Visit Jamshedpur")
                        .child("reviews")
                        .child(deleteUri)
                        .delete();
                mFiles.remove(it);
                notifyDataSetChanged();
            })).setNegativeButton("No", ((dialogInterface, i) -> {
            }));
        }
        else {
            dialog.setTitle("Do you want to delete this comment?");
            dialog.setPositiveButton("Yes", ((dialogInterface, i) -> {
                FirebaseFirestore
                        .getInstance()
                        .collection("Posts")
                        .document(pID)
                        .collection("Comments")
                        .document(cID)
                        .delete().addOnSuccessListener(unused -> Toast.makeText(context, "Comment deleted.", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(context, "Comment not deleted." + e.getMessage(), Toast.LENGTH_SHORT).show());
                mFiles.remove(it);
                notifyDataSetChanged();
            })).setNegativeButton("No", ((dialogInterface, i) -> {
            }));
        }
        dialog.create().show();
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public void updateData(ArrayList<CommentItemModel> comments) {
        mFiles = comments;
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private final ProgressBar progressBar;
        ImageView commentProfile, commentDelete, commentImage;
        TextView commentAuthor, commentAuthorIdentity, comment, commentTime;
        LinearLayout commentAuthorDescription;
        CardView commentProfileCard;
        ConstraintLayout commentImageViewer;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            comment = itemView.findViewById(R.id.comment);
            commentAuthor = itemView.findViewById(R.id.commentAuthorName);
            commentAuthorIdentity = itemView.findViewById(R.id.commentAuthorIdentity);
            commentProfile = itemView.findViewById(R.id.commentProfile);
            commentTime = itemView.findViewById(R.id.commentTime);
            commentDelete = itemView.findViewById(R.id.commentDelete);
            commentAuthorDescription = itemView.findViewById(R.id.commentAuthorDescription);
            commentProfileCard= itemView.findViewById(R.id.commentProfileCard);
            commentImage = itemView.findViewById(R.id.commentImage);
            commentImageViewer = itemView.findViewById(R.id.commentImageViewer);
            progressBar = itemView.findViewById(R.id.reviewProgressBar);
            user = FirebaseAuth.getInstance().getCurrentUser();
        }
    }
}
