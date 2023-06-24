package com.example.spammer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {
//static Client_Sender clientSender;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ProgressBar progressBar= findViewById(R.id.progressBar);
        Button connection = findViewById(R.id.button);
        connection.setVisibility(View.INVISIBLE);
        progressBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.INVISIBLE);
                connection.setVisibility(View.VISIBLE);
            }
        },2000);
        Context context = this.getApplicationContext();


        connection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(context,MessengerActivity.class);
                    startActivity(intent);
                }
               catch (Exception e)
               {
                   e.printStackTrace();
               }
            }
        });

    }

}