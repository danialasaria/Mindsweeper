package com.example.mindsweeper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final int COLUMN_COUNT = 8;
    private int clock = 0;
    private boolean running = false;
    private boolean flagMode = false;

    // save the TextViews of all cells in an array, so later on,
    // when a TextView is clicked, we know which cell it is
    private ArrayList<TextView> cell_tvs;

    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cell_tvs = new ArrayList<TextView>();
        GridLayout grid = (GridLayout) findViewById(R.id.gridLayout01);

        // Method (2): add four dynamically created cells
        for (int i = 0; i<=9; i++) {
            for (int j = 0; j <= 7; j++) {
                TextView tv = new TextView(this);
                tv.setHeight(dpToPixel(34));
                tv.setWidth(dpToPixel(34));
                tv.setTextSize(32);//dpToPixel(32) );
                tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                tv.setTextColor(Color.GRAY);
                tv.setBackgroundColor(Color.GRAY);
                tv.setOnClickListener(this::onClickTV);

                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.setMargins(dpToPixel(2), dpToPixel(2), dpToPixel(2), dpToPixel(2));
                lp.rowSpec = GridLayout.spec(i);
                lp.columnSpec = GridLayout.spec(j);

                grid.addView(tv, lp);
                cell_tvs.add(tv);
            }
        }
        if (savedInstanceState != null) {
            clock = savedInstanceState.getInt("clock");
            running = savedInstanceState.getBoolean("running");
        }
        running = true;
        runTimer();

    }

    private int findIndexOfCellTextView(TextView tv) {
        for (int n=0; n<cell_tvs.size(); n++) {
            if (cell_tvs.get(n) == tv)
                return n;
        }
        return -1;
    }

    @SuppressLint("SetTextI18n")
    public void onClickTV(View view){
        TextView tv = (TextView) view;
        int n = findIndexOfCellTextView(tv);
        int i = n/COLUMN_COUNT;
        int j = n%COLUMN_COUNT;
        if (flagMode) {
            if(tv.getText().toString().equals("\uD83D\uDEA9"))
            {
                tv.setText("");
                tv.setBackgroundColor(Color.GRAY);
            }
            else if(!isNumeric(tv.getText().toString())) {
                tv.setText(Html.fromHtml("\uD83D\uDEA9"));
                tv.setBackgroundColor(Color.GREEN);
            }
        }
        //tv.getText().toString().equals("\uD83D\uDEA9") ||
        else if(!isNumeric(tv.getText().toString())){
            tv.setText(String.valueOf(i)+String.valueOf(j));
//            if (tv.getCurrentTextColor() == Color.GRAY) {
//                tv.setTextColor(Color.GREEN);
//                tv.setBackgroundColor(Color.parseColor("lime"));
//            }else {
                tv.setTextColor(Color.GRAY);
                tv.setBackgroundColor(Color.LTGRAY);
//            }
        }
    }

    public void onClickFlag(View view) {
        if(flagMode)
        {
            flagMode = false;
        }
        else {
            flagMode = true;
        }
    }

    public boolean isNumeric(String strNum) {
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }

    public void onClickStart(View view) {
        running = true;
    }

    public void onClickStop(View view) {
        running = false;
    }
    public void onClickClear(View view) {
        running = false;
        clock = 0;
    }

    private void runTimer() {
        final TextView timeView = (TextView) findViewById(R.id.textView);
        final Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours =clock/3600;
                int minutes = (clock%3600) / 60;
                int seconds = clock%60;
                String time = String.format("%02d", seconds);
                timeView.setText(time);

                if (running) {
                    clock++;
                }
                handler.postDelayed(this, 1000);
            }
        });
    }
}