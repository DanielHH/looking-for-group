package com.example.daniel.lookingforgroup.matches.Comment;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.daniel.lookingforgroup.AsyncImageResponse;
import com.example.daniel.lookingforgroup.AsyncResponse;
import com.example.daniel.lookingforgroup.GetImageData;
import com.example.daniel.lookingforgroup.R;

import java.util.List;

public class CommentsAdapter extends Adapter<CommentsAdapter.ViewHolder>{
    public class ViewHolder extends RecyclerView.ViewHolder
            implements AsyncImageResponse {

        public TextView commentName;
        public TextView commentText;
        public ImageButton commentProfile;

        public ViewHolder(View itemView) {
            super(itemView);

            commentName = (TextView) itemView.findViewById(R.id.commentName);
            commentText = (TextView) itemView.findViewById(R.id.commentText);
            commentProfile = (ImageButton) itemView.findViewById(R.id.commentProfile);
        }

        public void setImage(Integer userId) {
            getImageData(Integer.toString(userId));
        }

        public void getImageData(String userId) {
            GetImageData getImageData = new GetImageData();
            getImageData.delegate = this;

            String url = "http://looking-for-group-looking-for-group" +
                    ".193b.starter-ca-central-1.openshiftapps.com/user/" + userId + "/image";
            try {//execute the async task
                getImageData.execute(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void processFinish(Bitmap response) {
            try {
                if(response != null) {
                    this.commentProfile.setImageBitmap(response);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private List<Comment> comments;

    public CommentsAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    @Override
    public void onBindViewHolder(CommentsAdapter.ViewHolder holder, int position) {
        final Comment comment = comments.get(position);

        TextView commentName = holder.commentName;
        commentName.setText(comment.getPosterName());

        TextView commentText = holder.commentText;
        commentText.setText(comment.getText());

        ImageButton commentProfile = holder.commentProfile;

        holder.setImage(comment.getPosterId());

        // TODO: link to profile from the comment profile image button
    }

    @NonNull
    @Override
    public CommentsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View commentView = inflater.inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(commentView);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }
}
