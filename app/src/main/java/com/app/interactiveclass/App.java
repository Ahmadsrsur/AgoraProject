package com.app.interactiveclass;

import android.app.Application;
import android.content.Context;

import com.app.interactiveclass.io.agora.media.RtcTokenBuilder2;

public class App extends Application {



   static GlobalSettings globalSettings;
    @Override
    public void onCreate() {
        super.onCreate();
    }
    static String getToken(Context context,String channelName)
    {
        int expirationTimeInSeconds=10000;
        RtcTokenBuilder2 tokenBuilder = new RtcTokenBuilder2();
        // Calculate the time expiry timestamp
        int timestamp = (int)(System.currentTimeMillis() / 1000 + expirationTimeInSeconds);

        System.out.println("UID token");

        return tokenBuilder.buildTokenWithUid(context.getString(R.string.agora_key), context.getString(R.string.certificate_key),
                channelName, 0, RtcTokenBuilder2.Role.ROLE_PUBLISHER, timestamp, timestamp);
    }

    public static GlobalSettings getGlobalSettings() {
        if(globalSettings == null){
            globalSettings = new GlobalSettings();
        }
        return globalSettings;
    }
}
