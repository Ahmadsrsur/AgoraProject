package com.app.interactiveclass;

import androidx.annotation.NonNull;

import com.app.interactiveclass.Model.Channel;
import com.app.interactiveclass.interfaces.OnDbResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseDatabaseManager{

    private static final String DB_CHANNELS="Channels";
    private static final String DB_QUESTIONS="Questions";
    private DatabaseReference database;
    FirebaseDatabaseManager()
    {
        database=FirebaseDatabase.getInstance().getReference();
    }

    void createChannel(String channel_name,String uid,String token,OnDbResult onDbResult)
    {

        Channel channel=new Channel(channel_name,uid,token);
        database.child(DB_CHANNELS).child(channel_name).setValue(channel);
    }
    void channelExists(String channel_name,OnDbResult onDbResult)
    {
        database.child(DB_CHANNELS).child(channel_name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                onDbResult.onSuccess(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onDbResult.onFailure("Error");
            }
        });
    }
    void removeChannel(String channel_name,OnDbResult onDbResult)
    {
        database.child(DB_CHANNELS).child(channel_name).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(onDbResult==null)
                    return;


                if(task.isSuccessful())
                {
                    onDbResult.onSuccess(null);
                }
                else
                    onDbResult.onFailure("Failed to delete");
            }
        });

    }
}
