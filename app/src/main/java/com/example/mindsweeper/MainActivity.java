package com.example.mindsweeper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    private static final int COLUMN_COUNT = 8;
    private int clock = 0;
    private boolean running = false;
    private boolean gameOver = false;
    private boolean flagMode = false;
    private boolean userWon = false;
    private boolean gameStarted = false;
    private int totalCellsRevealed = 0;
    private int totalCells = 80;
    private int bombs = 4;
    private int flagCount = 4;
    private ArrayList<Integer> bombPlacement = new ArrayList<Integer>();
    private String mine = Html.fromHtml("\uD83D\uDCA3").toString();
    private Set<Integer> rightEdge = new HashSet<Integer>() {
        {
            add(7);
            add(15);
            add(23);
            add(31);
            add(39);
            add(47);
            add(55);
            add(63);
            add(71);
            add(79);
        }
    };

    private Set<Integer> leftEdge = new HashSet<Integer>() {
        {
            add(0);
            add(8);
            add(16);
            add(24);
            add(32);
            add(40);
            add(48);
            add(56);
            add(64);
            add(72);
        }
    };

    private Set<Integer> topEdge = new HashSet<Integer>() {
        {
            add(0);
            add(1);
            add(2);
            add(3);
            add(4);
            add(5);
            add(6);
            add(7);
        }
    };

    private Set<Integer> bottomEdge = new HashSet<Integer>() {
        {
            add(72);
            add(73);
            add(74);
            add(75);
            add(76);
            add(77);
            add(78);
            add(79);
        }
    };

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

        // Method (2): add 80 dynamically created cells
        for (int i = 0; i<=9; i++) {
            for (int j = 0; j <= 7; j++) {
                TextView tv = new TextView(this);
                tv.setHeight(dpToPixel(34));
                tv.setWidth(dpToPixel(34));
                tv.setTextSize(25);//dpToPixel(32) );
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
        generateGrid(bombs);
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
        if(gameOver == true) {
            goToResultPage();
            return;
        }
        gameStarted = true;
        TextView tv = (TextView) view;
        ColorDrawable cd = (ColorDrawable) tv.getBackground();
        int colorCode = cd.getColor();
        int gray = Color.GRAY;
        if (flagMode) {
            if(tv.getText().toString().equals("\uD83D\uDEA9"))
            {
                tv.setText("");
                tv.setBackgroundColor(Color.GRAY);
                flagCount++;
                final TextView timeView = (TextView) findViewById(R.id.flagCount);
                timeView.setText(String.valueOf(flagCount));
            }
            //condition if background color light gray don't touch
            else if(colorCode == gray) {
                tv.setText(Html.fromHtml("\uD83D\uDEA9"));
                final TextView timeView = (TextView) findViewById(R.id.flagCount);
                flagCount --;
                timeView.setText(String.valueOf(flagCount));
            }
        }
        else{
            if(tv.getText().toString().equals("b"))
            {
                for (Integer cell: bombPlacement) {
                    cell_tvs.get(cell).setText(mine);
                    cell_tvs.get(cell).setBackgroundColor(Color.LTGRAY);
                }
                tv.setText(mine);
                userWon = false;
                gameOver = true;
//                goToResultPage();
            }
            tv.setBackgroundColor(Color.LTGRAY);
            totalCellsRevealed++;
            //if no mines around cell everything adjacent is revealed
            //as there is no number, shouldn't have to check for bombs next to
            if(tv.getText().toString().equals(""))
            {
                int index = findIndexOfCellTextView(tv);
                revealCells(index);
            }
            if(totalCells - bombs <= totalCellsRevealed)
            {
                for (Integer cell: bombPlacement) {
                    cell_tvs.get(cell).setText(mine);
                    cell_tvs.get(cell).setBackgroundColor(Color.LTGRAY);
                }
                userWon = true;
                gameOver = true;
//                goToResultPage();
            }
        }
    }

    public void revealCells(int index) {
        ArrayList<Integer> adjacentCells = adjacentCells(index);
        for (Integer cell: adjacentCells) {
            ColorDrawable c = (ColorDrawable) cell_tvs.get(cell).getBackground();
            int colorOfCell = c.getColor();
            if(colorOfCell!=Color.LTGRAY && !cell_tvs.get(cell).getText().toString().equals("b")) {
                totalCellsRevealed++;
                cell_tvs.get(cell).setBackgroundColor(Color.LTGRAY);
                if(cell_tvs.get(cell).getText().toString().equals(""))
                {
                    revealCells(cell);
                }
            }
        }
    }


    public void onClickFlag(View view) {
        final Button button = (Button) findViewById(R.id.textView10);
        if(flagMode)
        {
            button.setText(R.string.pick);
            flagMode = false;
        }
        else {
            //set the button icon equal to a flag
            button.setText(R.string.flagSymbol);
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
    //go to result page with the time and result
    public void goToResultPage() {
        Intent intent = new Intent(this, DisplayResultActivity.class);
        intent.putExtra("com.example.mindsweeper.TIME", String.valueOf(clock));
        if(userWon) {
            intent.putExtra("com.example.mindsweeper.RESULT", "You won. Good Job!");
        }
        else {
            intent.putExtra("com.example.mindsweeper.RESULT", "You lost. Try again next time!");
        }
        startActivity(intent);
    }

    public void generateGrid(int numberBombs) {
        int bombsPlaced = 0;
        while (bombsPlaced < numberBombs) {
            int x = new Random().nextInt(7);
            int y = new Random().nextInt(9);

            //need to access the cell at the x,y position and assign
            //as bomb if empty
            int index = new Random().nextInt(78);
            if(!cell_tvs.get(index).getText().toString().equals("b"))
            {
                cell_tvs.get(index).setText("b");
//                cell_tvs.get(index).setTextColor(Color.LTGRAY);
                bombsPlaced++;
                bombPlacement.add(index);
            }
        }
        // initialize the numbers for all cells in the grid
        for (int i=0;i<=79;i++)
        {
            //if not a bomb find out what number should be placed
            if(!cell_tvs.get(i).getText().toString().equals("b"))
            {
                int countBombs = 0;
                ArrayList<Integer> adjacentCells = adjacentCells(i);
                for(int cell: adjacentCells)
                {
                    if(cell_tvs.get(cell).getText().toString().equals("b"))
                    {
                        countBombs++;
                    }
                }
                if(countBombs > 0)
                {
                    cell_tvs.get(i).setText(String.valueOf(countBombs));
                }
                else{
                    cell_tvs.get(i).setText("");
                }
            }
        }
    }

    public ArrayList<Integer> adjacentCells(int index) {
        ArrayList<Integer> adjacent = new ArrayList<Integer>();

        if(!rightEdge.contains(index)) {
            adjacent.add(index+1);
            //add top right if exists
            if(!topEdge.contains(index)) {
                adjacent.add(index-7);
            }
        }
        if(!leftEdge.contains(index)) {
            adjacent.add(index-1);
            //add top left if exists
            if(!topEdge.contains(index)) {
                adjacent.add(index-9);
            }
        }
        if(!topEdge.contains(index)) {
            adjacent.add(index-8);
        }
        if(!bottomEdge.contains(index)) {
            adjacent.add(index+8);
            //if bottom right exists
            if(!rightEdge.contains(index)) {
                adjacent.add(index+9);
            }
            //if bottom left exists
            if(!leftEdge.contains(index)) {
                adjacent.add(index+7);
            }
        }
        return adjacent;
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

                if (running && gameOver == false && gameStarted == true) {
                    clock++;
                }
                handler.postDelayed(this, 1000);
            }
        });
    }
}