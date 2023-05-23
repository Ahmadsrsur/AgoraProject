package com.app.interactiveclass;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import static com.app.interactiveclass.activities.CreateQuestionaireActivity.millisToTime;

import static io.agora.rtc2.video.VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15;
import static io.agora.rtc2.video.VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30;
import static io.agora.rtc2.video.VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE;
import static io.agora.rtc2.video.VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT;
import static io.agora.rtc2.video.VideoEncoderConfiguration.STANDARD_BITRATE;
import static io.agora.rtc2.video.VideoEncoderConfiguration.VD_640x360;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.app.interactiveclass.Model.Question;
import com.app.interactiveclass.activities.CreateQuestionaireActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

//import io.agora.rtc.Constants;
//import io.agora.rtc.IRtcEngineEventHandler;
//import io.agora.rtc.RtcChannel;
//import io.agora.rtc.RtcEngine;
//import io.agora.rtc.models.ChannelMediaOptions;
//import io.agora.rtc.video.VideoCanvas;
//import io.agora.rtc.video.VideoEncoderConfiguration;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.ScreenCaptureParameters;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.video.VideoEncoderConfiguration;
import io.agora.rtc2.video.WatermarkOptions;


public class ClassRoomActivity extends AppCompatActivity {


    RtcEngine mRtcEngine;
    String TAG="VideoActivity";
    String channelName="";
    String host_participent="";
    ImageView imgDisableCamera,imageCallEnd,imgMuteAudio;
    MaterialCardView cardCreateQuestionaire;
    ImageView imgShareScreen;
    TextView txtTotalUsers;
    String token;
    boolean videoEnabled=true;
    boolean audioEnabled=true;
    int totalUsers=1;
    FirebaseDatabaseManager firebaseDatabaseManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_room);

       // uid= FirebaseAuth.getInstance().getUid();
        channelName=getIntent().getStringExtra("Channel_Name");
        host_participent=getIntent().getStringExtra("host_participent");
        token=getIntent().getStringExtra("token");
        Log.d(TAG,"Token : "+token);
        imgDisableCamera=findViewById(R.id.imgDisableCamera);
        Log.d(TAG,channelName);
        Log.d(TAG,host_participent);
        cardCreateQuestionaire=findViewById(R.id.cardCreateQuestionaire);
        imgShareScreen=findViewById(R.id.imgShareScreen);
        imageCallEnd=findViewById(R.id.imageCallEnd);
        txtTotalUsers=findViewById(R.id.txtTotalUsers);
        imgMuteAudio=findViewById(R.id.imgMuteAudio);
        firebaseDatabaseManager=new FirebaseDatabaseManager();

        imgDisableCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mRtcEngine==null)
                    return;




                if(videoEnabled)
                {
                    if(joined)
                    {
                        mRtcEngine.stopScreenCapture();
                    }



                    mRtcEngine.enableLocalVideo(false);
                    localViewContainer.removeAllViews();
                    localViewContainer.addView(getEmptyImageView());
                }
                else
                {

                    if(joined)
                    {
                       startScreenShareOnly();
                    }
                    else
                    {
                        mRtcEngine.enableLocalVideo(true);
                        localViewContainer.removeAllViews();
                        localViewContainer.addView(mLocalView);
                    }

                }
                videoEnabled=!videoEnabled;
                imgDisableCamera.setImageResource(videoEnabled?R.drawable.baseline_videocam_24:R.drawable.baseline_videocam_off_24);
            }
        });

        imgMuteAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mRtcEngine==null)
                    return;


                if(audioEnabled)
                {
                    mRtcEngine.enableAudio();
                }
                else
                {
                    mRtcEngine.disableAudio();
                }
                audioEnabled=!audioEnabled;
                imgMuteAudio.setImageResource(audioEnabled?R.drawable.baseline_mic_24:R.drawable.baseline_mic_off_24);

            }
        });

        imageCallEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mRtcEngine==null)
                    return;
                onBackPressed();
            }
        });
        initAgora();
        initRtcEngineAndJoin();

       if(host_participent.equals("host"))
       {
           cardCreateQuestionaire.setVisibility(View.VISIBLE);
           cardCreateQuestionaire.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {

                   Intent intent=new Intent(ClassRoomActivity.this, CreateQuestionaireActivity.class);
                   intent.putExtra("Channel_Name",channelName);
                   startActivity(intent);

               }
           });
       }
       else
       {
           cardCreateQuestionaire.setVisibility(View.GONE);
           waitForQuestions();
       }


        imgShareScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareScreen();
            }
        });

    }


    boolean joined=false;
    private void shareScreen() {

        if (!joined) {
            // Check permission
            if (AndPermission.hasPermissions(this, Permission.Group.STORAGE, Permission.Group.MICROPHONE, Permission.Group.CAMERA)) {
                joinChannel(channelName);
                return;
            }
            // Request permission
            AndPermission.with(this).runtime().permission(
                    Permission.Group.STORAGE,
                    Permission.Group.MICROPHONE,
                    Permission.Group.CAMERA
            ).onGranted(permissions ->
            {
                // Permissions Granted
                joinChannel(channelName);
            }).start();
        }
        //else {
//            joined=false;
//            leaveChannel();
//        }
    }

    private void leaveChannel() {
      mRtcEngine.leaveChannel();
        mRtcEngine.stopPreview();

        // Stop sharing screen capture if it is already being shared
        mRtcEngine.stopScreenCapture();
        initVideoProfile();
        initLocalVideo();
        initRtcEngineAndJoin();
    }
    private final ScreenCaptureParameters screenCaptureParameters = new ScreenCaptureParameters();
    private static final int DEFAULT_SHARE_FRAME_RATE = 15;


    private void startScreenShareOnly() {
        // Stop previewing camera video
        mRtcEngine.leaveChannel();
        mRtcEngine.stopPreview();

        // Stop sharing screen capture if it is already being shared
        mRtcEngine.stopScreenCapture();

        // Set up video encoding configs for screen capture
        VideoEncoderConfiguration config = new VideoEncoderConfiguration(
                new VideoEncoderConfiguration.VideoDimensions(1280, 720),
                FRAME_RATE_FPS_30,
                DEFAULT_SHARE_FRAME_RATE,
                ORIENTATION_MODE_FIXED_PORTRAIT
        );
        mRtcEngine.setVideoEncoderConfiguration(config);

        // Start sharing screen capture
        mRtcEngine.startScreenCapture(new ScreenCaptureParameters());

        // Set up local video to render your screen capture preview
        SurfaceView surfaceView = new SurfaceView(this);
        localViewContainer.removeAllViews();
        localViewContainer.addView(surfaceView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, Constants.RENDER_MODE_FIT,
                Constants.VIDEO_MIRROR_MODE_DISABLED,
                Constants.VIDEO_SOURCE_SCREEN_PRIMARY,
                0));
        mRtcEngine.startPreview(Constants.VideoSourceType.VIDEO_SOURCE_SCREEN_PRIMARY);

        ChannelMediaOptions options = new ChannelMediaOptions();
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
        options.autoSubscribeVideo = true;
        options.autoSubscribeAudio = true;
        options.publishCameraTrack = false;
        options.publishMicrophoneTrack = false;
        options.publishScreenCaptureVideo = true;
        options.publishScreenCaptureAudio = true;
        int res = mRtcEngine.joinChannel(token, channelName, 0, options);
    }

    private void joinChannel(String channelId) {

        joined=true;
        startScreenShareOnly();


    }


    DatabaseReference questionnairesRef;
    ValueEventListener questionsListener;
    private void waitForQuestions() {


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        questionnairesRef = database.getReference("questionnaires").child(channelName);
        questionsListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists())
                {
                    showQuestionDialog(snapshot, questionnairesRef);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        questionnairesRef.addValueEventListener(questionsListener);
    }

    private void showQuestionDialog(@NonNull DataSnapshot snapshot, DatabaseReference ref) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference resultsRef = database.getReference("results").child(channelName);
        String uid=FirebaseAuth.getInstance().getUid();
        DataSnapshot firstChild = snapshot.getChildren().iterator().next();
        Question question= firstChild.getValue(Question.class);
        long startTime = question.create_time;
        long duration = question.duration;
        long currentTime=System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;
        long remainingTime = duration - elapsedTime;
        if(remainingTime>0)
        {
            Dialog dialog=new Dialog(ClassRoomActivity.this);
            dialog.setCancelable(false);

            dialog.setContentView(R.layout.lyt_dialog_question);
            dialog.getWindow().setLayout(MATCH_PARENT,WRAP_CONTENT);
            dialog.show();

            TextView txtTimer=dialog.findViewById(R.id.txtTimer);
            TextView txtQuestion=dialog.findViewById(R.id.txtQuestion);
            RadioGroup radioGroup=dialog.findViewById(R.id.answerRadioGroup);
            RadioButton answer1RadioButton=dialog.findViewById(R.id.answer1RadioButton);
            RadioButton answer2RadioButton=dialog.findViewById(R.id.answer2RadioButton);
            RadioButton answer3RadioButton=dialog.findViewById(R.id.answer3RadioButton);

            txtQuestion.setText(question.question);
            answer1RadioButton.setText(question.opt1);
            answer2RadioButton.setText(question.opt2);
            answer3RadioButton.setText(question.opt3);


            MaterialButton btnCancel=dialog.findViewById(R.id.btnCancel);
            MaterialButton btnSave=dialog.findViewById(R.id.btnSave);
            CountDownTimer count=new CountDownTimer(  remainingTime,1000 )
            {
                @Override
                public void onTick(long millisUntilFinished) {
                    String timeString = millisToTime(millisUntilFinished);
                    txtTimer.setText(timeString);
                }

                @Override
                public void onFinish() {
                    question.userAnswer=getSelectedAnswer(radioGroup);
                    resultsRef.child(uid).setValue(question);
                    dialog.dismiss();
                    Toast.makeText(ClassRoomActivity.this, "Question Submitted", Toast.LENGTH_SHORT).show();

                }
            };
            count.start();

            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    question.userAnswer=getSelectedAnswer(radioGroup);
                    resultsRef.child(uid).setValue(question);
                    count.cancel();
                    dialog.dismiss();
                    Toast.makeText(ClassRoomActivity.this, "Question Submitted", Toast.LENGTH_SHORT).show();

                }
            });
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    count.cancel();
                    dialog.dismiss();
                }
            });
        }
    }
    private int getSelectedAnswer(RadioGroup answerRadioGroup) {
        int selectedId = answerRadioGroup.getCheckedRadioButtonId();
        switch (selectedId) {
            case R.id.answer1RadioButton:
                return 1;
            case R.id.answer2RadioButton:
                return 2;
            case R.id.answer3RadioButton:
                return 3;
            default:
                return 0;
        }
    }

    private void initAgora() {
        initRtcAgora();
        initVideoProfile();
        initLocalVideo();
        joinChannel();
    }
    FrameLayout localViewContainer;
    SurfaceView mLocalView;

    private View getEmptyImageView()
    {
        ImageView imageView=new ImageView(this);
        imageView.setImageResource(R.drawable.baseline_videocam_off_24);
        return imageView;
    }
    private void initLocalVideo() {
        if(localViewContainer==null)
            localViewContainer = findViewById(R.id.containerHost);

        mLocalView = RtcEngine.CreateRendererView(getBaseContext());
        mLocalView.setZOrderMediaOverlay(true);
        localViewContainer.removeAllViews();
        localViewContainer.addView(mLocalView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(mLocalView, VideoCanvas.RENDER_MODE_FIT, 0));
        mRtcEngine.startPreview();

    }
    private void initRtcAgora() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_key), mRtcEventHandler);
        } catch (Exception e) {
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }
    private io.agora.rtc2.IRtcEngineEventHandler mRtcEventHandler = new io.agora.rtc2.IRtcEngineEventHandler() {

        @Override
        public void onUserOffline(int uid, int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserLeft(uid);
                    totalUsers--;
                    txtTotalUsers.setText("Total Users : "+totalUsers);
                    Log.d(TAG,"onUserOffline 1");
                }
            });
        }

        @Override
        public void onUserJoined(final int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    {
                        onRemoteUserLeft(uid);
                        setupRemoteVideo(uid);
                        totalUsers++;
                        txtTotalUsers.setText("Total Users : "+totalUsers);
                        Log.d(TAG,"onUserJoined 1");
                    }

                }
            });
        }
    };


    private void setupRemoteVideo(int uid) {

        LinearLayout container = (LinearLayout) findViewById(R.id.containerParticipent);

        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
        int dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 500, getResources().getDisplayMetrics());
        surfaceView.setLayoutParams(new FrameLayout.LayoutParams(dp, ViewGroup.LayoutParams.MATCH_PARENT));
        container.addView(surfaceView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid));

        surfaceView.setTag(uid); // for mark purpose
    }
    private void onRemoteUserLeft(int uid) {
        LinearLayout container = (LinearLayout) findViewById(R.id.containerParticipent);
        int childCount = container.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View child = container.getChildAt(i);
            Object tag = child.getTag();

            if (tag != null && tag.equals(uid)) {
                container.removeView(child);
                break;
            }
        }
    }
    private void initVideoProfile() {
        mRtcEngine.enableVideo();
        mRtcEngine.enableAudio();
        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(VideoEncoderConfiguration.VD_640x360, VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                ORIENTATION_MODE_FIXED_PORTRAIT));

    }
    private void joinChannel() {
        mRtcEngine.joinChannel(token, channelName, "Extra Optional Data", 0);
    }

    private void initRtcEngineAndJoin()
    {


//        RtcChannel rtcChannel = mRtcEngine.createRtcChannel(channelName);



        ChannelMediaOptions options = new ChannelMediaOptions();
        options.autoSubscribeVideo = true;
        options.autoSubscribeAudio = true;
        options.publishEncodedVideoTrack = true;
        options.publishSecondaryCameraTrack = true;
//
//
//
//        rtcChannel.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
//        rtcChannel.joinChannel(token, " ",0 , option);
//        rtcChannel.publish();


        mRtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        mRtcEngine.joinChannel(token, channelName, 0, options);


    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
            mRtcEngine.stopScreenCapture();
            mRtcEngine.stopPreview();
        }
        mRtcEngine = null;

        new Handler().post(RtcEngine::destroy);

        if (questionnairesRef != null && questionsListener != null)
            questionnairesRef.removeEventListener(questionsListener);

        if (host_participent.equals("host")) {
            firebaseDatabaseManager.removeChannel(channelName, null);
        }
    }

}