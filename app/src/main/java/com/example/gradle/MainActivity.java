package com.example.gradle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        TextView java = findViewById(R.id.javatext);
        TextView kotlin = findViewById(R.id.kotlintext);

        java.setText("java更改1");

        kotlin.setText(new AAA().gettext());
    }
}