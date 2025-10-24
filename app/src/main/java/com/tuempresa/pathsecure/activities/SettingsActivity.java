package com.tuempresa.pathsecure.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import com.tuempresa.pathsecure.R;

public class SettingsActivity extends AppCompatActivity {

    Button btnBackSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnBackSettings = findViewById(R.id.btnBackSettings);
        btnBackSettings.setOnClickListener(v -> finish());
    }
}
