package com.mitrukahitesh.asrik.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.utility.Constants;

public class Registration extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Main.userDefaultLanguage(this, getSharedPreferences(Constants.USER_DETAILS_SHARED_PREFERENCE, MODE_PRIVATE).getString(Constants.LOCALE, "en"));
        setContentView(R.layout.activity_registration);
    }
}