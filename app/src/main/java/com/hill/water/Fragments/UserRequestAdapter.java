package com.hill.water.Fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hill.water.R;

import java.util.ArrayList;
import java.util.List;

public class UserRequestAdapter extends RecyclerView.Adapter<UserRequestAdapter.UserRequestViewHolder> {

    private List<UserRegistrationRequest> userRequests = new ArrayList<>();
    private DatabaseReference userRequestsRef;

    public UserRequestAdapter(DatabaseReference userRequestsRef) {
        this.userRequestsRef = userRequestsRef;
    }

    @NonNull
    @Override
    public UserRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_request, parent, false);
        return new UserRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserRequestViewHolder holder, int position) {
        UserRegistrationRequest request = userRequests.get(position);
        holder.bind(request);
    }

    @Override
    public int getItemCount() {
        return userRequests.size();
    }

    public void setUserRequests(List<UserRegistrationRequest> userRequests) {
        this.userRequests = userRequests;
        notifyDataSetChanged();
    }

    public class UserRequestViewHolder extends RecyclerView.ViewHolder {

        private TextView tvName, tvEmail, tvPhone, tvRole, tvStatus;
        private Button btnApprove, btnReject;

        public UserRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvPhone = itemView.findViewById(R.id.tv_phone);
            tvRole = itemView.findViewById(R.id.tv_role);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnApprove = itemView.findViewById(R.id.btn_approve);
            btnReject = itemView.findViewById(R.id.btn_reject);
        }

        public void bind(UserRegistrationRequest request) {
            tvName.setText(request.getFullName());
            tvEmail.setText(request.getEmail());
            tvPhone.setText(request.getPhoneNumber());
            tvRole.setText(request.getRole());
            tvStatus.setText(request.getStatus());

            btnApprove.setOnClickListener(v -> updateRequestStatus(request, "approved"));
            btnReject.setOnClickListener(v -> updateRequestStatus(request, "rejected"));
        }

        private void updateRequestStatus(UserRegistrationRequest request, String status) {

            userRequestsRef.child(request.getUid()).child("status").setValue(status)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (status.equals("approved")) {

                                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                                usersRef.child(request.getUid()).setValue(request)
                                        .addOnCompleteListener(innerTask -> {
                                            if (innerTask.isSuccessful()) {

                                                usersRef.child(request.getUid()).child("status").setValue("approved")
                                                        .addOnCompleteListener(updateTask -> {
                                                            if (updateTask.isSuccessful()) {

                                                                userRequestsRef.child(request.getUid()).removeValue()
                                                                        .addOnCompleteListener(finalTask -> {
                                                                            if (finalTask.isSuccessful()) {
                                                                                Toast.makeText(itemView.getContext(), "Request approved and user added", Toast.LENGTH_SHORT).show();
                                                                            } else {
                                                                                Toast.makeText(itemView.getContext(), "Failed to remove approved request", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });
                                                            } else {
                                                                Toast.makeText(itemView.getContext(), "Failed to update status to approved", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            } else {
                                                Toast.makeText(itemView.getContext(), "Failed to add user", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {

                                userRequestsRef.child(request.getUid()).removeValue()
                                        .addOnCompleteListener(finalTask -> {
                                            if (finalTask.isSuccessful()) {
                                                Toast.makeText(itemView.getContext(), "Request rejected", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(itemView.getContext(), "Failed to remove rejected request", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(itemView.getContext(), "Failed to update request status", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}

