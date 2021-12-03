package com.mitrukahitesh.asrik.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.helpers.Constants;

public class LocaleSelection extends AppCompatActivity {

    private RadioGroup languageGroup;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locale_selection);
        languageGroup = findViewById(R.id.language_radio_group);
        SharedPreferences preferences = getSharedPreferences(Constants.USER_DETAILS_SHARED_PREFERENCE, MODE_PRIVATE);
        preferences.edit().putString(Constants.LOCALE, "en").apply();
        languageGroup.check(findViewById(R.id.en).getId());
        languageGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String locale = "en";
                if (checkedId == findViewById(R.id.hi).getId()) {
                    locale = "hi";
                }
                preferences.edit().putString(Constants.LOCALE, locale).apply();
            }
        });
        button = findViewById(R.id.continue_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LocaleSelection.this, Registration.class));
                finish();
            }
        });
    }
}