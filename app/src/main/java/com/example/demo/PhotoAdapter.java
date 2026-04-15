package com.example.demo;

import android.net.Uri;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {
    private List<String> photos;

    public PhotoAdapter(List<String> photos) {
        this.photos = photos;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // On crée dynamiquement une ImageView pour chaque photo
        ImageView imageView = new ImageView(parent.getContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 600)); // Hauteur de 600px
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setPadding(4, 4, 4, 4);
        return new PhotoViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        // On affiche l'image grâce à son URI
        ((ImageView) holder.itemView).setImageURI(Uri.parse(photos.get(position)));
    }

    @Override
    public int getItemCount() {
        return photos != null ? photos.size() : 0;
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        public PhotoViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
        }
    }
}