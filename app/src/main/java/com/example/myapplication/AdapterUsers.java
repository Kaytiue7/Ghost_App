package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.UserViewHolder> {

    private List<PostUser> userList;
    private Context context;

    // Constructor'da context'i alıyoruz
    public AdapterUsers(List<PostUser> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        PostUser user = userList.get(position);
        holder.usernameTextView.setText(user.getUsername());

        // Profil fotoğrafını Picasso ile yükle
        Picasso.get().load(user.getProfilePhoto()).into(holder.profileImageView);

        // Profil ve kullanıcı adına tıklanınca profil sayfasına git
        View.OnClickListener profileClickListener = v -> openProfilePage(user.getUsername());
        holder.profileImageView.setOnClickListener(profileClickListener);
        holder.usernameTextView.setOnClickListener(profileClickListener);
    }

    // Profil sayfasına geçiş metodu
    private void openProfilePage(String username) {
        Bundle bundle = new Bundle();
        bundle.putString("username", username);

        FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        HesapSayfa hesapSayfaFragment = new HesapSayfa();
        hesapSayfaFragment.setArguments(bundle);

        fragmentTransaction.replace(R.id.frame_layout, hesapSayfaFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        ImageView profileImageView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
        }
    }
}
