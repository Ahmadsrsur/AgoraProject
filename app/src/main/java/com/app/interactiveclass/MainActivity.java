package com.app.interactiveclass;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.app.interactiveclass.Model.Channel;
import com.app.interactiveclass.activities.LoginActivity;
import com.app.interactiveclass.interfaces.OnDbResult;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;

public class MainActivity extends AppCompatActivity {



    private TextView txtUser;
    private MaterialCardView cardCreateRoom;
    private MaterialCardView cardJoinRoom;
    private MaterialCardView cardLogOut;
    private FirebaseDatabaseManager firebaseDatabaseManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtUser=findViewById(R.id.txtUser);
        cardCreateRoom=findViewById(R.id.cardCreateRoom);
        cardJoinRoom=findViewById(R.id.cardJoinRoom);
        cardLogOut=findViewById(R.id.cardLogOut);
        firebaseDatabaseManager=new FirebaseDatabaseManager();
        checkPermissions();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser==null)
        {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
        else
        {
            txtUser.setText(currentUser.getEmail());
        }

        cardCreateRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showDialogName("host");
            }
        });
        cardJoinRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showDialogName("participent");
            }
        });
        cardLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
            }
        });
    }
    private void showDialogName(String  host_participent)
    {
        Dialog dialog=new Dialog(this);
        dialog.setContentView(R.layout.dialog_name);
        dialog.getWindow().setLayout(MATCH_PARENT,WRAP_CONTENT);
        dialog.show();
        EditText editTextName=dialog.findViewById(R.id.editTextName);
        MaterialButton btnSubmit=dialog.findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String channelName=editTextName.getText().toString();
                if(!channelName.isEmpty())
                {
                    dialog.dismiss();
                    checkIfChannelExists(host_participent,channelName);
                }
            }
        });
    }

    private void checkIfChannelExists(String host_participent,String channelName) {



        firebaseDatabaseManager.channelExists(channelName, new OnDbResult() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {


                boolean roomExists=dataSnapshot.exists();
                String token="";
                if(host_participent.equals("host"))
                {
                    if(roomExists)
                    {
                        Channel channel=dataSnapshot.getValue(Channel.class);
                        if(!channel.getUid().equals(FirebaseAuth.getInstance().getUid()))
                        {
                            showMessageDialog("This room is already being used by someone else, Please try another name for room");
                            return;
                        }
                        token=channel.getToken();
                    }
                    else
                    {
                        token=App.getToken(MainActivity.this,channelName);
                        firebaseDatabaseManager.createChannel(channelName,FirebaseAuth.getInstance().getUid(),token,null);
                    }
                }
                if(host_participent.equals("participent") && !roomExists)
                {
                    showMessageDialog("This room doesn't exist , try other room name or create one");
                    return;
                }

                if(roomExists && token.isEmpty())
                {
                    Channel channel=dataSnapshot.getValue(Channel.class);
                    token=channel.getToken();
                }

                openVideoActivity(host_participent, channelName,token);

            }

            @Override
            public void onFailure(String message) {
                if(host_participent.equals("host"))
                {
                    showMessageDialog("Error");
                }
                else
                {
                    showMessageDialog("This room does not exist , try other room name or create one");
                }
            }
        });

    }

    private void openVideoActivity(String host_participent,String channelName,String token){
        Intent intent=new Intent(MainActivity.this, ClassRoomActivity.class);
        intent.putExtra("host_participent",host_participent);
        intent.putExtra("Channel_Name",channelName.toLowerCase().trim());
        intent.putExtra("token",token);
        startActivity(intent);
    }

    private void showMessageDialog(String message) {
        new AlertDialog.Builder(this).setMessage(message)
                .setPositiveButton("OK",null)
                .show();
    }

    private void checkPermissions() {
        int MY_PERMISSIONS_REQUEST_CAMERA=0;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,  android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{ android.Manifest.permission.CAMERA,  android.Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_CAMERA );
        }
    }

}