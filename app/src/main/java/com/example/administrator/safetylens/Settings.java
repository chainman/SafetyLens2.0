package com.example.administrator.safetylens;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

public class Settings extends AppCompatActivity {

    static boolean pictures = true,status = true; //Pictures if want to save photos and roads if want to show roads options and status to allow to save when red
    static long seconds = 5; //Time to reveal photo options
    Switch picture,road,state;
    EditText editText;

    static boolean maps = true;
    Switch map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        road = findViewById(R.id.roads);
        picture = findViewById(R.id.pictures);
        state = findViewById(R.id.status);
        picture.toggle();
        picture.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                pictures=isChecked;
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

        map = findViewById(R.id.switchMaps);
        map.toggle();
        map.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                maps=isChecked;
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
        picture.setChecked(pictures);
    }
}
