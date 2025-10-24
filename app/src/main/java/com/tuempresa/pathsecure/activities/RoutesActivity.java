package com.tuempresa.pathsecure.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import com.tuempresa.pathsecure.R;

public class RoutesActivity extends AppCompatActivity {

    Button btnBackRoutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);

        btnBackRoutes = findViewById(R.id.btnBackRoutes);
        btnBackRoutes.setOnClickListener(v -> finish());
    }
}
