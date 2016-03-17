package aas.beetclock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.TextView;

import java.util.Calendar;

public class PickDate extends AppCompatActivity {

    private DatePicker pickDate;

    public final static String SELECTED_DATE = "Selected_Date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_date);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Enables Strict Mode testing
        /*
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                        //.penaltyDialog()
                .penaltyFlashScreen()
                .build());
                */


/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/



        //Get calendar with current time and use it to initialize datePicker
        Calendar now = Calendar.getInstance();
        pickDate = (DatePicker) findViewById(R.id.datePicker);
        pickDate.init(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), null);
    }//End onCreate



    public void donePicking(View view){

        //Do nothing but open the date picker
        Calendar newdate = Calendar.getInstance();
        newdate.set(pickDate.getYear(), pickDate.getMonth(), pickDate.getDayOfMonth());
        long pickTime = newdate.getTimeInMillis();



        Intent returnIntent = new Intent();
        returnIntent.putExtra("selection", String.valueOf(pickTime));
        setResult(RESULT_OK, returnIntent);
        finish();

    } //End done picking

    @Override
    public void onBackPressed() {
        //For some reason I need to explicitly tell it what to do when back is pressed; crashes otherwise
           Intent intent = new Intent(this, MainActivity.class);
           startActivity(intent);
    }


    }



