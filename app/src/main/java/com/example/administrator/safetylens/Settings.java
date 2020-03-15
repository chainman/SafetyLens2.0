package com.example.administrator.safetylens;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

public class Settings extends AppCompatActivity {

    static boolean pictures = true,roads = true,status = false; //Pictures if want to save photos and roads if want to show roads options and status to allow to save when red
    static long seconds = 8; //Time to reveal photo options
    Switch picture,road,state;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        picture = findViewById(R.id.pictures);
        road = findViewById(R.id.roads);
        state = findViewById(R.id.status);
        picture.toggle();
        road.toggle();
        picture.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                pictures=isChecked;
            }
        });
        road.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                roads=isChecked;
            }
        });
        state.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                status=isChecked;
            }
        });
        editText = findViewById(R.id.timeText);
        findViewById(R.id.savesettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              try{ seconds = Long.parseLong(editText.getText().toString()); goToMain(); }
              catch (NumberFormatException n){n.printStackTrace();}
            }
        });
    }

    private void goToMain() {startActivity(new Intent(this,MainActivity.class));}

    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume() {
        super.onResume();
        editText.setText(seconds+"");
        state.setChecked(status);
        road.setChecked(roads);
        picture.setChecked(pictures);
    }
}
