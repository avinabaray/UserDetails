package com.avinabaray.userinfo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    private Activity mActivity;
    private ArrayList<UserModel> users;
    private OnUserDataChangeListener onUserDataChangeListener;

    public UsersAdapter(Activity mActivity, ArrayList<UserModel> users, OnUserDataChangeListener onUserDataChangeListener) {
        this.mActivity = mActivity;
        this.users = users;
        this.onUserDataChangeListener = onUserDataChangeListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.user_grid_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        UserModel currUser = users.get(position);

        holder.nameText.setText(currUser.getFirstName() + " " + currUser.getLastName());
        holder.userImage.setImageDrawable(TextDrawable
                .builder()
                .buildRound(currUser.getFirstName().substring(0, 1),
                        ColorGenerator.DEFAULT.getRandomColor())
        );

        holder.deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUserDataChangeListener.onDelete(position);
            }
        });

        holder.editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUserDataChangeListener.onEdit(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView userImage, deleteImage, editImage;
        TextView nameText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userImage = itemView.findViewById(R.id.userImage);
            deleteImage = itemView.findViewById(R.id.deleteImage);
            editImage = itemView.findViewById(R.id.editImage);
            nameText = itemView.findViewById(R.id.nameText);
        }
    }

    interface OnUserDataChangeListener {
        void onEdit(int pos);

        void onDelete(int pos);
    }
}
