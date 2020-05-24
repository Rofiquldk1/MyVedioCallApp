package com.example.mycallapp;

import android.Manifest;
import android.content.Intent;
import android.media.MediaCas;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;

import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VedioChatActivity extends AppCompatActivity implements Session.SessionListener
    , PublisherKit.PublisherListener

{
    private static String API_Key="46739052";
    private static String SESSION_ID="2_MX40NjczOTA1Mn5-MTU4OTQ3OTExODU0N35zU1VaQytybElmbXNzRklBZmhUOVpzd3F-fg";
    private static String TOKEN="T1==cGFydG5lcl9pZD00NjczOTA1MiZzaWc9OGNhZTc3MTBhZGFjZTAzYzBhZDBiMDRiMjU2MDhjZWUwOTQxOTM3ZjpzZXNzaW9uX2lkPTJfTVg0ME5qY3pPVEExTW41LU1UVTRPVFEzT1RFeE9EVTBOMzV6VTFWYVF5dHliRWxtYlhOelJrbEJabWhVT1ZwemQzRi1mZyZjcmVhdGVfdGltZT0xNTg5NDc5MTU4Jm5vbmNlPTAuMzIyODgwMjY4MzI3ODk1MyZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNTkyMDcxMTU1JmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";
    private static final String LOG_TAG=VedioChatActivity.class.getSimpleName();
    private static final int RC_VEDIO_APP_FROM = 124;

    private FrameLayout mPublisherViewController;
    private FrameLayout mSubscriberViewController;

    private Session mSession;
    private Publisher mpublisher;
    private Subscriber mSubscriber;

    private ImageView closeVedioChatBtn;
    private DatabaseReference usersRef;
    private String userId="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vedio_chat);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        closeVedioChatBtn = findViewById(R.id.close_vedio_chat_btn);
        closeVedioChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child(userId).hasChild("Ringing")){
                            usersRef.child(userId).child("Ringing").removeValue();
                            if(mpublisher!=null){
                                mpublisher.destroy();
                            }
                            if(mSubscriber!=null){
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VedioChatActivity.this,RegistrationActivity.class));
                            finish();
                        }
                        if(dataSnapshot.child(userId).hasChild("Calling")){
                            usersRef.child(userId).child("Calling").removeValue();
                            if(mpublisher!=null){
                                mpublisher.destroy();
                            }
                            if(mSubscriber!=null){
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VedioChatActivity.this,RegistrationActivity.class));
                            finish();
                        }
                        else {
                            if(mpublisher!=null){
                                mpublisher.destroy();
                            }
                            if(mSubscriber!=null){
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VedioChatActivity.this,RegistrationActivity.class));
                            finish();
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });
        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,VedioChatActivity.this);
    }

    @AfterPermissionGranted(RC_VEDIO_APP_FROM)
    private void requestPermissions(){
        String[] perms = {Manifest.permission.INTERNET,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
        if(EasyPermissions.hasPermissions(this,perms)){
            mPublisherViewController = findViewById(R.id.publisher_container);
            mSubscriberViewController = findViewById(R.id.subscriber_container);

            //1.initialize and connect to the session
            mSession = new Session.Builder(this,API_Key,SESSION_ID).build();
            mSession.setSessionListener(VedioChatActivity.this);
            mSession.connect(TOKEN);
        }
        else{
            EasyPermissions.requestPermissions(this,"Hye this app needs mic and camera,Please allow",RC_VEDIO_APP_FROM,perms);
        }

    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    //2.publishing a stream to the session
    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG,"Session Connected");

        mpublisher = new Publisher.Builder(this).build();
        mpublisher.setPublisherListener(VedioChatActivity.this);

        mPublisherViewController.addView(mpublisher.getView());

        if(mpublisher.getView() instanceof GLSurfaceView){
            ((GLSurfaceView) mpublisher.getView()).setZOrderOnTop(true);
        }
        mSession.publish(mpublisher);

    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG,"Stream Disconnected");
    }

    //3.suvsscribing to the stream
    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG,"Stream Received");
        if(mSubscriber == null){
            mSubscriber = new Subscriber.Builder(this,stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewController.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG,"Stream Dropped");
        if(mSubscriber!=null){
            mSubscriber=null;
            mSubscriberViewController.removeAllViews();
        }

    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.i(LOG_TAG,"Stream Error");

    }
}
