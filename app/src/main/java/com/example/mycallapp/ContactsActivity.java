package com.example.mycallapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;


import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ContactsActivity extends AppCompatActivity {
    BottomNavigationView navView;
    RecyclerView mContactsList;
    ImageView findpeoplebtn;
    //it intialization should be inside onCreate
    private FirebaseAuth mAuth ;
    FirebaseUser currentUser;
    private DatabaseReference ContactsRef, UsersRef;
    private String userName="",profileImage="",CalledBy="";

    private String currentUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();

        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        mContactsList = findViewById(R.id.contactlist);
        findpeoplebtn = findViewById(R.id.findpeoplrbtn);

        mContactsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        findpeoplebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent findpeopleIntent = new Intent(ContactsActivity.this,FindPeopleActivity.class);
                startActivity(findpeopleIntent);
            }
        });

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener=
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()){
                        case R.id.navigation_home:
                            Intent mainIntent = new Intent(ContactsActivity.this,ContactsActivity.class);
                            startActivity(mainIntent);
                            break;
                        case R.id.navigation_settings:
                            Intent settingsIntent = new Intent(ContactsActivity.this,SettingsActivity.class);
                            startActivity(settingsIntent);
                            break;
                        case R.id.navigation_notifications:
                            Intent notifiIntent = new Intent(ContactsActivity.this,NotificationActivity.class);
                            startActivity(notifiIntent);
                            break;
                        case R.id.navigation_logout:
                            FirebaseAuth.getInstance().signOut();
                            Intent logoutIntent = new Intent(ContactsActivity.this,RegistrationActivity.class);
                            startActivity(logoutIntent);
                            finish();
                            break;

                    }
                    return true;
                }
            };

    @Override
    protected void onStart() {
        super.onStart();

        checkForReceivingCall();
        validateUser();
        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ContactsRef.child(currentUserId),Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int i, @NonNull Contacts contacts) {
                final String listUserId = getRef(i).getKey();

                UsersRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            userName = dataSnapshot.child("name").getValue().toString();
                            profileImage = dataSnapshot.child("image").getValue().toString();

                            holder.UserNameText.setText(userName);
                            Picasso.get().load(profileImage).into(holder.profileImageView);
                        }
                        holder.callBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent callingintent = new Intent(ContactsActivity.this,CallingActivity.class);
                                callingintent.putExtra("visit_user_id",listUserId);
                                startActivity(callingintent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_design,parent,false);
                ContactsViewHolder viewHolder1 = new ContactsViewHolder(view);
                return viewHolder1;
            }
        };
        mContactsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {
        TextView UserNameText;
        Button callBtn;
        ImageView profileImageView;


        public ContactsViewHolder(@NonNull View itemView){
            super(itemView);

            UserNameText = itemView.findViewById(R.id.name_contact);
            callBtn = itemView.findViewById(R.id.call_btn);
            profileImageView = itemView.findViewById(R.id.image_contact);
        }
    }

    private void validateUser(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        reference.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    Intent settingsintent = new Intent(ContactsActivity.this,SettingsActivity.class);
                    startActivity(settingsintent);
                    finish();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void checkForReceivingCall() {
        UsersRef.child(currentUserId)
                .child("Ringing")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild("ringing")){
                            CalledBy = dataSnapshot.child("ringing").getValue().toString();

                            Intent callingintent = new Intent(ContactsActivity.this,CallingActivity.class);
                            callingintent.putExtra("visit_user_id",CalledBy);
                            startActivity(callingintent);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

}
