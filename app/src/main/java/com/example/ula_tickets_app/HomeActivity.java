package com.example.ula_tickets_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomeActivity extends AppCompatActivity {

    LinearLayout toMoviePageBtn, toConcertPageBtn;
    ImageView toSettingsPageBtn, toInfoPageBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        toMoviePageBtn = findViewById(R.id.MovieOptionContainer);
        toConcertPageBtn = findViewById(R.id.ConcertOptionContainer);
        toSettingsPageBtn = findViewById(R.id.SettingsButton);
        toInfoPageBtn = findViewById(R.id.InfoButton);

        toMoviePageBtn.setOnClickListener(V-> { // To movie page
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
            Log.d("ClickEvent", "Click on MOVIE option");
        });

        toConcertPageBtn.setOnClickListener(V-> {

        });

        toSettingsPageBtn.setOnClickListener(V-> {
            Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        toInfoPageBtn.setOnClickListener(V-> {

        });




    }
}