package com.app.interactiveclass.Model;

public class Channel {

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Channel(){}
    public Channel(String channelName, String uid, String token) {
        this.channelName = channelName;
        this.uid = uid;
        this.token = token;
    }

    String channelName;
    String uid;
    String token;
}
