package com.example.mycallapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class NotificationActivity extends AppCompatActivity {
    private RecyclerView notificationList;
    private DatabaseReference FriendRequestRef,ContactsRef,UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        notificationList = findViewById(R.id.notificationlist);
        notificationList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Request");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(FriendRequestRef.child(currentUserId),Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,NotificationsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Contacts, NotificationsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final NotificationsViewHolder holder, int i, @NonNull Contacts contacts) {
                holder.acceptbtn.setVisibility(View.VISIBLE);
                holder.cancelbtn.setVisibility(View.VISIBLE);

                final String listUserId = getRef(i).getKey();
                DatabaseReference requestTypeRef = getRef(i).child("request_type").getRef();
                requestTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String type = dataSnapshot.getValue().toString();
                            if(type.equals("received")){
                                holder.cardview.setVisibility(View.VISIBLE);
                                UsersRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild("image")){
                                            final String imageStr = dataSnapshot.child("image").getValue().toString();
                                            Picasso.get().load(imageStr).into(holder.profileImageView);
                                        }
                                            final String nameStr = dataSnapshot.child("name").getValue().toString();
                                            holder.UserNameText.setText(nameStr);
                                        holder.acceptbtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) { ContactsRef.child(currentUserId).child(listUserId).child("Contact").
                                                    setValue("Saved")
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                ContactsRef.child(listUserId).child(currentUserId).child("Contact").
                                                                        setValue("Saved")
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()){
                                                                                    FriendRequestRef.child(currentUserId).child(listUserId).child("request_type").
                                                                                            removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if(task.isSuccessful()){
                                                                                                FriendRequestRef.child(listUserId).child(currentUserId).child("request_type").
                                                                                                        removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if(task.isSuccessful()){
                                                                                                            Toast.makeText(NotificationActivity.this,"New Contact Saved.",Toast.LENGTH_LONG).show();
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                            }
                                                                                        }
                                                                                    });
                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    });

                                            }
                                        });
                                        holder.cancelbtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                FriendRequestRef.child(currentUserId).child(listUserId).child("request_type").
                                                        removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            FriendRequestRef.child(listUserId).child(currentUserId).child("request_type").
                                                                    removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        Toast.makeText(NotificationActivity.this,"Friend Request Canceled.",Toast.LENGTH_LONG).show();

                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });

                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }
                            else{
                                holder.cardview.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friend_design,parent,false);
                NotificationsViewHolder viewHolder = new NotificationsViewHolder(view);
                return viewHolder;
            }
        };
        notificationList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class NotificationsViewHolder extends RecyclerView.ViewHolder {
        TextView UserNameText;
        Button acceptbtn,cancelbtn;
        ImageView profileImageView;
        RelativeLayout cardview;

        public NotificationsViewHolder(@NonNull View itemView){
            super(itemView);

            UserNameText = itemView.findViewById(R.id.name_notification);
            acceptbtn = itemView.findViewById(R.id.request_accept_btn);
            cancelbtn = itemView.findViewById(R.id.request_desline_btn);
            profileImageView = itemView.findViewById(R.id.image_notification);
            cardview = itemView.findViewById(R.id.Card_view);
        }
    }

}
