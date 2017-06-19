package com.xiayu.androidservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class SingleTopActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_top);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        Button normal = (Button)findViewById(R.id.normal);
        normal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SingleTopActivity.this,SecondActivity.class);
                startActivity(intent);
            }
        });

        Button singletop = (Button)findViewById(R.id.singletop);
        singletop .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SingleTopActivity.this,SingleTopActivity.class);
                startActivity(intent);
            }
        });

        Button singletask = (Button)findViewById(R.id.singletask);
        singletask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SingleTopActivity.this,SingleTaskActivity.class);
                startActivity(intent);
            }
        });

        Button singleinstance = (Button)findViewById(R.id.singleinstance);
        singleinstance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SingleTopActivity.this,SingleInstanceActivity.class);
                startActivity(intent);
            }
        });
    }

}
