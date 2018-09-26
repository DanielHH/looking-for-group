package com.example.daniel.lookingforgroup;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

public class CommentsAdapter extends Adapter<CommentsAdapter.ViewHolder> {
    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView commentName;
        public TextView commentText;
        public ImageButton commentProfile;

        public ViewHolder(View itemView) {
            super(itemView);

            commentName = (TextView) itemView.findViewById(R.id.commentName);
            commentText = (TextView) itemView.findViewById(R.id.commentText);
            commentProfile = (ImageButton) itemView.findViewById(R.id.commentProfile);
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
