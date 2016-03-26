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
        getSupportActionBar().setTitle("Pick Date");

        //Enables Strict Mode testing
        /*
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                        //.penaltyDialog()
                .penaltyFlashScreen()
                .build());
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
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }


    }



