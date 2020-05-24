package com.example.mycallapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {
    private String recevierUserId,recevierUserImage,recevierUserName;
    private ImageView background_profile_view;
    private TextView name_profile;
    private Button add_friend,desline_friend_request;

    private FirebaseAuth mAuth;
    private String senderUserId;
    private String currentState="new";
    private DatabaseReference FriendRequestRef,ContactsRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();

        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Request");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");


        recevierUserId = getIntent().getExtras().get("visit_user_id").toString();
        recevierUserImage = getIntent().getExtras().get("profile_image").toString();
        recevierUserName = getIntent().getExtras().get("profile_name").toString();

        background_profile_view = findViewById(R.id.background_profile_view);
        name_profile = findViewById(R.id.name_profile);
        add_friend = findViewById(R.id.add_friend);
        desline_friend_request = findViewById(R.id.desline_friend_request);

        Picasso.get().load(recevierUserImage).into(background_profile_view);
        name_profile.setText(recevierUserName);

        manageClickEvents();
    }

    private void manageClickEvents()
    {
        FriendRequestRef.child(senderUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(recevierUserId)) {
                            String RequestType = dataSnapshot.child(recevierUserId).child("request_type").getValue().toString();
                            if(RequestType.equals("sent")){
                                currentState = "request_sent";
                                add_friend.setText("Cancel Friend Request");
                            }
                            else if(RequestType.equals("received")){
                                currentState = "request_received";
                                add_friend.setText("Accept Friend Request");

                                desline_friend_request.setVisibility(View.VISIBLE);
                                desline_friend_request.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancelFriendRequest();
                                    }
                                });
                            }
                            else{
                                ContactsRef.child(senderUserId)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.hasChild(recevierUserId)){
                                                    currentState = "friends";
                                                    add_friend.setText("Delete Contacts");
                                                }
                                                else {
                                                    currentState = "new";
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        if(senderUserId.equals(recevierUserId)){
            add_friend.setVisibility(View.GONE);
        }
        else{
            add_friend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                     if(currentState.equals("new")){
                         SendFriendRequest();
                     }  
                     if(currentState.equals("request_sent")){
                         CancelFriendRequest();
                     }   
                     if(currentState.equals("request_received")){
                         AcceptFriendRequest();
                     }
                     if(currentState.equals("request_sent")){
                         CancelFriendRequest();
                     }  
                }
            });
        }
    }

    private void AcceptFriendRequest() {
        ContactsRef.child(senderUserId).child(recevierUserId).child("Contact").
                setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            ContactsRef.child(recevierUserId).child(senderUserId).child("Contact").
                                    setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                FriendRequestRef.child(senderUserId).child(recevierUserId).child("request_type").
                                                        removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            FriendRequestRef.child(recevierUserId).child(senderUserId).child("request_type").
                                                                    removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        currentState = "friends";
                                                                        add_friend.setText("Delete Contacts");
                                                                        desline_friend_request.setVisibility(View.GONE);
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

    private void CancelFriendRequest() {
        FriendRequestRef.child(senderUserId).child(recevierUserId).child("request_type").
                removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    FriendRequestRef.child(recevierUserId).child(senderUserId).child("request_type").
                            removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                currentState = "new";
                                add_friend.setText("Add Friend");
                            }
                        }
                    });
                }
            }
        });
    }

    private void SendFriendRequest() {
        FriendRequestRef.child(senderUserId).child(recevierUserId).child("request_type").
                setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    FriendRequestRef.child(recevierUserId).child(senderUserId).child("request_type").
                            setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                currentState = "request_sent";
                                add_friend.setText("Cancel Friend Request");
                                Toast.makeText(ProfileActivity.this,"Friend Request Sent.",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
