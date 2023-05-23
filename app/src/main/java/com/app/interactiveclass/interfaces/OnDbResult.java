package com.app.interactiveclass.interfaces;

import com.google.firebase.database.DataSnapshot;

public interface OnDbResult {

    void  onSuccess(DataSnapshot dataSnapshot);
    void onFailure(String message);
}
