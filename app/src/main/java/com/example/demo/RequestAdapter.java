package com.example.demo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private List<FriendRequest> requestList = new ArrayList<>();

    public void setData(List<FriendRequest> requests) {
        this.requestList = requests;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        FriendRequest request = requestList.get(position);
        holder.emailTv.setText(request.senderEmail);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // BOUTON ACCEPTER
        holder.btnAccept.setOnClickListener(v -> {
            db.collection("friend_requests").document(request.getDocumentId())
                    .update("status", "accepted")
                    .addOnSuccessListener(aVoid -> Toast.makeText(v.getContext(), "Ami ajouté !", Toast.LENGTH_SHORT).show());
        });

        // BOUTON REFUSER
        holder.btnRefuse.setOnClickListener(v -> {
            db.collection("friend_requests").document(request.getDocumentId())
                    .delete()
                    .addOnSuccessListener(aVoid -> Toast.makeText(v.getContext(), "Demande refusée", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView emailTv;
        ImageButton btnAccept, btnRefuse;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            emailTv = itemView.findViewById(R.id.tv_sender_email);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnRefuse = itemView.findViewById(R.id.btn_refuse);
        }
    }
}