package com.example.visitjamshedpur;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;
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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class PostItemAdapter extends RecyclerView.Adapter<PostItemAdapter.MyViewHolder> {

    Context context;
    ArrayList<PostItemModel> mFiles;
    FirebaseUser user;

    public PostItemAdapter(Context context, ArrayList<PostItemModel> mFiles) {
        this.context = context;
        this.mFiles = mFiles;
    }

    @NonNull
    @Override
    public PostItemAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.discover_item_layout, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PostItemAdapter.MyViewHolder holder, int i) {
        String duration= "";
        user = FirebaseAuth.getInstance().getCurrentUser();
        ArrayList<String> imageList;
        Glide.with(context).load(mFiles.get(i).getPostProfile())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        holder.postProfile.setImageResource(R.drawable.ic_baseline_image_not_supported_24);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(holder.postProfile);

        holder.postAuthorName.setText(mFiles.get(i).getAuthorName());
        String title = mFiles.get(i).postTitle;
        if (title.length() < 1) holder.postTitle.setVisibility(View.GONE);
        else holder.postTitle.setText(mFiles.get(i).postTitle);
        holder.postAuthorIdentity.setText(mFiles.get(i).getAuthorIdentity());
        imageList = mFiles.get(i).getImageDownloadUrl();
        int size = imageList.size();
        if (size == 1) holder.postImageCounter.setVisibility(View.GONE);
        holder.postImageCounter.setText("1/" + size);
        final int[] cnt = {0};
        if (imageList.size() > 0) {
            holder.postImageViewer.setVisibility(View.VISIBLE);
            Glide.with(context).load(imageList.get(cnt[0]))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            holder.postImage.setImageResource(R.drawable.image);
                            holder.postProgressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.postProgressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(holder.postImage);
        }

        holder.postImageRightBtn.setOnClickListener(view -> {
            if (imageList.size() > 0) {
                if (cnt[0] + 1 < size) {
                    holder.postProgressBar.setVisibility(View.VISIBLE);
                    cnt[0]++;
                    holder.postImageCounter.setText(cnt[0] + 1 + "/" + size);
                    Glide.with(context).load(imageList.get(cnt[0]))
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    holder.postImage.setImageResource(R.drawable.image);
                                    holder.postProgressBar.setVisibility(View.GONE);
                                    return false;
                                }
                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    holder.postImageCounter.setText(cnt[0] + 1 + "/" + size);
                                    holder.postProgressBar.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .into(holder.postImage);
                }
            }
        });

        holder.postImageLeftBtn.setOnClickListener(view -> {
            if (imageList.size() > 0) {
                if (cnt[0] - 1 >= 0) {
                    holder.postProgressBar.setVisibility(View.VISIBLE);
                    cnt[0]--;
                    holder.postImageCounter.setText(cnt[0] + 1 + "/" + size);
                    Glide.with(context).load(imageList.get(cnt[0]))
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    holder.postImage.setImageResource(R.drawable.image);
                                    holder.postProgressBar.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    holder.postImageCounter.setText(cnt[0] + 1 + "/" + size);
                                    holder.postProgressBar.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .into(holder.postImage);
                }
            }
        });
        String desc = mFiles.get(i).getPostDescription();
        if (desc.length() < 1) holder.postDescription.setVisibility(View.GONE);
        else holder.postDescription.setText(desc);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference db = firestore.collection("Posts").document(mFiles.get(i).getPostId())
                .collection("usersLiked")
                .document(Objects.requireNonNull(user.getEmail()));
        db.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.get("isLiked") != null) {
                    if ((boolean) documentSnapshot.get("isLiked")) {
                        Glide.with(context).load(R.drawable.ic_baseline_thumb_up_alt_24_black)
                                .into(holder.postLikeBtn);
                    } else Glide.with(context).load(R.drawable.ic_baseline_thumb_up_alt_24)
                            .into(holder.postLikeBtn);
                } else {
                    FirebaseFirestore
                            .getInstance()
                            .collection("Posts")
                            .document(mFiles.get(i).postId)
                            .get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            DocumentSnapshot documentSnapshot1 = task1.getResult();
                            if (documentSnapshot1.exists()) {
                                Map<String, Boolean> mp = new HashMap<>();
                                mp.put("isLiked", false);
                                db.set(mp);
                            }
                        }
                    });
                }
            }
        });

        holder.postLikeBtn.setOnClickListener(view -> db.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentReference postDocRef = firestore.collection("Posts")
                        .document(mFiles.get(i).getPostId());
                postDocRef.get().addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        DocumentSnapshot documentSnapshot1 = task2.getResult();
                        if (documentSnapshot.get("isLiked") != null) {
                            if ((boolean) documentSnapshot.get("isLiked")) {
                                Glide.with(context).load(R.drawable.ic_baseline_thumb_up_alt_24)
                                        .into(holder.postLikeBtn);
                                Map<String, Boolean> temp = new HashMap<>();
                                temp.put("isLiked", false);
                                db.set(temp);
                                int likeCount = Integer.parseInt(String.valueOf(documentSnapshot1.get("pLikes")));
                                likeCount--;
                                holder.postLikeCount.setText(String.valueOf(likeCount));
                                Map<String, Object> temp1 = new HashMap<>();
                                temp1.put("pLikes", likeCount);
                                postDocRef.update(temp1);
                            } else {
                                Glide.with(context).load(R.drawable.ic_baseline_thumb_up_alt_24_black)
                                        .into(holder.postLikeBtn);
                                Map<String, Boolean> temp = new HashMap<>();
                                temp.put("isLiked", true);
                                db.set(temp);
                                int likeCount = Integer.parseInt(String.valueOf(documentSnapshot1.get("pLikes")));
                                likeCount++;
                                holder.postLikeCount.setText(String.valueOf(likeCount));
                                Map<String, Object> temp1 = new HashMap<>();
                                temp1.put("pLikes", likeCount);
                                postDocRef.update(temp1);
                            }
                        } else {
                            Glide.with(context).load(R.drawable.ic_baseline_thumb_up_alt_24_black)
                                    .into(holder.postLikeBtn);
                            Map<String, Boolean> temp = new HashMap<>();
                            temp.put("isLiked", true);
                            db.set(temp);
                            int likeCount = Integer.parseInt(String.valueOf(documentSnapshot1.get("pLikes")));
                            likeCount++;
                            holder.postLikeCount.setText(String.valueOf(likeCount));
                            Map<String, Object> temp1 = new HashMap<>();
                            temp1.put("pLikes", likeCount);
                            postDocRef.update(temp1);
                        }
                    }
                });
            } else {
                FirebaseFirestore
                        .getInstance()
                        .collection("Posts")
                        .document(mFiles.get(i).postId)
                        .get().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task1.getResult();
                        if (documentSnapshot.exists()) {
                            Map<String, Boolean> mp = new HashMap<>();
                            mp.put("isLiked", false);
                            db.set(mp);
                        }
                    }
                });
            }
        }));

        DocumentReference postDocRef = firestore.collection("Posts")
                .document(mFiles.get(i).getPostId());
        postDocRef.get().addOnCompleteListener(task2 -> {
            if (task2.isSuccessful()) {
                DocumentSnapshot documentSnapshot1 = task2.getResult();
                int likeCount = Integer.parseInt(String.valueOf(documentSnapshot1.get("pLikes")));
                holder.postLikeCount.setText(String.valueOf(likeCount));
            }
        });

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(System.currentTimeMillis());
        String currentTime = DateFormat.format("dd-MM-yyyy HH:mm:ss", cal).toString();
        String lastDate = mFiles.get(i).getTimestamp(); // dd-mm-yyyy HH:mm:ss format
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        try {
            Date date1 = simpleDateFormat.parse(currentTime);
            Date date2 = simpleDateFormat.parse(lastDate);
            if (date1 != null) {
                if (date2 != null) {
                    duration = printDifference(date1, date2, holder.postTimePassed);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String authorEmail = mFiles.get(i).authorId;
        String userEmail = user.getEmail();
        if (authorEmail.equals(userEmail)) holder.postDelete.setVisibility(View.VISIBLE);

        holder.postDelete.setOnClickListener(view -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog.setTitle("Do you want to delete this post?");
            alertDialog.setPositiveButton("Yes", ((dialogInterface, i1) -> {
                DocumentReference doc = FirebaseFirestore.getInstance()
                        .collection("Posts")
                        .document(mFiles.get(i).postId);
                doc.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();
                        int x = 0;
                        while (snapshot.getString("image-" + x + "-deleteUri") != null) {
                            String deleteString = snapshot.getString("image-" + x + "-deleteUri");
                            FirebaseStorage.getInstance()
                                    .getReference()
                                    .child("Visit Jamshedpur")
                                    .child("Posts")
                                    .child(Objects.requireNonNull(deleteString))
                                    .delete();
                            x++;
                        }
                        doc.delete().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Toast.makeText(context, "Post deleted.", Toast.LENGTH_SHORT).show();
                                mFiles.remove(i);
                                notifyDataSetChanged();
                            }
                        });
                    }
                });
            })).setNegativeButton("No", ((dialogInterface, i1) -> {
            }));
            alertDialog.create().show();
        });
        holder.postProfileCard.setOnClickListener(view -> profileIntent(mFiles.get(i).authorId));
        holder.postAuthorDescription.setOnClickListener(view -> profileIntent(mFiles.get(i).authorId));
        String finalDuration = duration;
        holder.postCommentBtn.setOnClickListener(view -> postCommentIntent(mFiles.get(i).getPostProfile(),
                mFiles.get(i).authorName,
                mFiles.get(i).authorIdentity,
                finalDuration,
                mFiles.get(i).postTitle,
                mFiles.get(i).postDescription,
                mFiles.get(i).postId,
                mFiles.get(i).getImageDownloadUrl()));
    }

    private void postCommentIntent(String postProfile, String authorName, String authorIdentity, String finalDuration, String postTitle, String postDescription, String postId, ArrayList<String> imageDownloadUrls) {

        Intent intent = new Intent(context, CommentSection.class);
        intent.putExtra("postProfile", postProfile);
        intent.putExtra("authorName", authorName);
        intent.putExtra("authorIdentity", authorIdentity);
        intent.putExtra("finalDuration", finalDuration);
        intent.putExtra("postTitle", postTitle);
        intent.putExtra("postDescription", postDescription);
        intent.putExtra("postId", postId);
        intent.putExtra("postImageUrls", imageDownloadUrls);
        context.startActivity(intent);
    }


    private void profileIntent(String email) {
        Intent intent = new Intent(context, ProfileThirdParty.class);
        intent.putExtra("email", email);
        context.startActivity(intent);
    }

    public String printDifference(Date startDate, Date endDate, TextView postTimePassed) {
        long different = Math.abs(startDate.getTime() - endDate.getTime());
        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;

        String s = "";
        if (elapsedDays > 0) {
            if (elapsedDays == 1) s = s + elapsedDays + " day ";
            else s = s + elapsedDays + " days ";
        }
        else if (elapsedHours > 0) {
            if (elapsedHours == 1) s = s + elapsedHours + " hour ";
            else s = s + elapsedHours + " hours ";
        }
        else if (elapsedMinutes > 0) {
            if (elapsedMinutes == 1) s = s + elapsedMinutes + " minute ";
            else s = s + elapsedMinutes + " minutes ";
        }
        if (s.equals("")) s = "Just now";
        else
            s = s + "ago";
        postTimePassed.setText(s);
        return s;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(ArrayList<PostItemModel> temp) {
        mFiles = temp;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView postProfile, postImage, postLikeBtn, postCommentBtn, postDelete;
        TextView postAuthorName, postTitle, postDescription, postImageCounter, postLikeCount, postAuthorIdentity, postTimePassed;
        ProgressBar postProgressBar;
        CardView postImageViewer, postProfileCard;
        LinearLayout postAuthorDescription;
        View postImageLeftBtn, postImageRightBtn;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            postAuthorName = itemView.findViewById(R.id.postAuthorName);
            postCommentBtn = itemView.findViewById(R.id.postComment);
            postImage = itemView.findViewById(R.id.postImage);
            postProfile = itemView.findViewById(R.id.postProfile);
            postLikeBtn = itemView.findViewById(R.id.postLike);
            postTitle = itemView.findViewById(R.id.postTitle);
            postDescription = itemView.findViewById(R.id.postDescription);
            postImageCounter = itemView.findViewById(R.id.postImageCounter);
            postProgressBar = itemView.findViewById(R.id.postProgressBar);
            postImageViewer = itemView.findViewById(R.id.postImageViewer);
            postImageLeftBtn = itemView.findViewById(R.id.postImageLeft);
            postImageRightBtn = itemView.findViewById(R.id.postImageRight);
            postLikeCount = itemView.findViewById(R.id.postLikeCount);
            postAuthorIdentity = itemView.findViewById(R.id.postAuthorIdentity);
            postTimePassed = itemView.findViewById(R.id.postTimePassed);
            postDelete = itemView.findViewById(R.id.postDelete);
            postProfileCard = itemView.findViewById(R.id.postProfileCard);
            postAuthorDescription = itemView.findViewById(R.id.postAuthorDescription);
        }
    }
}
