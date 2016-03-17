package aas.beetclock;

/*BeetClock version 01 (Beta) was created by Alex Smith, Feb 2016.  The code is free to share
under a Creative Commons Attribution-ShareAlike 4.0 International License.*/

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.content.SharedPreferences;
import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;
import aas.beetclock.SQLiteHelper;
import java.util.List;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import android.widget.DigitalClock;
import java.util.concurrent.TimeUnit;
import android.widget.ScrollView;
import android.widget.Toast;
import android.view.View.OnClickListener;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

//Declare public variables

    //Here I will want to create more public strings for each value I would like to save or pass to a new view
    public final static String SAVED_CROP = "aas.beetclock.Saved_Crop";
    public final static String SAVED_TIME = "aas.beetclock.Saved_Time";
    public final static String SAVED_JOB = "aas.beetclock.Saved_Job";
    public final static String SAVED_EQUIP = "aas.beetclock.Saved_Equip";

    public String allEquip = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Enables Strict Mode testing
        /*
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                        //.penaltyDialog()
                .penaltyFlashScreen()
                .build());
                */






        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Run when the app loads***********************************

/* moved to populateSpinners


        //Load values from the crop table

        SQLiteHelper db = new SQLiteHelper(this);
        String nullsearch = null; // Must send function a null string in order to return all results
        List<String> croplist = db.getCropList(nullsearch);

//This populates the crop spinner with the search results

        //First it is necessary to convert the list of search results to an array
        String[] spinarray = new String[croplist.size()];
        spinarray = croplist.toArray(spinarray);

        //This initializes the spinner with values from crop table
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                this, R.layout.spinnertext, spinarray); //android.R.layout.simple_spinner_item
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinnertext); //android.R.layout.simple_spinner_item

        Spinner crop_spinner = (Spinner)findViewById(R.id.crop_spinner);
        crop_spinner.setAdapter(spinnerArrayAdapter);


//Initialize machinery spinner and populate with items
        List<String> machinelist = db.getMachineList(nullsearch);
        String[] spinmachine = new String[machinelist.size()];
        spinmachine = machinelist.toArray(spinmachine);
        ArrayAdapter<String> machineArrayAdapter = new ArrayAdapter<String>(
                this, R.layout.spinnertext, spinmachine);
        machineArrayAdapter.setDropDownViewResource(R.layout.spinnertext);

        Spinner machine_spinner = (Spinner)findViewById(R.id.equip_spinner);
        machine_spinner.setAdapter(machineArrayAdapter);

        //Placing a digital clock in the upper lefthand corner
        DigitalClock digital = (DigitalClock) findViewById(R.id.digital_clock);

        //I also want to populate the jobs spinner with jobs from the jobs table
        List<String> joblist = db.getJobList();
        String[] jobsarray = new String[joblist.size()];
        jobsarray = joblist.toArray(jobsarray);
        //This initializes the spinner with values from the job table
        ArrayAdapter<String> jobsArrayAdapter = new ArrayAdapter<String>(
                this, R.layout.spinnertext, jobsarray); //android.R.layout.simple_spinner_item
        jobsArrayAdapter.setDropDownViewResource(R.layout.spinnertext);//android.R.layout.simple_spinner_item
        Spinner jobs_spinner = (Spinner)findViewById(R.id.jobs_spinner);
        jobs_spinner.setAdapter(jobsArrayAdapter);

        */

        //Populate Spinners
        new populateSpinners().execute("");

        //Get buttons for ongoing processes
        //refreshButtons();
        new refreshButtons().execute("");

        //Finally remove old sharedPreference values

/*
        //Checks whether there is a current crop saved to SharedPreferences.  If so, displays crop and time started
        SharedPreferences sharedPref = this.getSharedPreferences(
                "aas.beetclock", Context.MODE_PRIVATE);
       // SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
      //  SharedPreferences.Editor editor = sharedPref.edit();
        String savedcrop = sharedPref.getString(SAVED_CROP, "");
        if(savedcrop.equals("")) {}else{
//If there is already a crop saved to SharedPreferences, I want to display crop and time started
            String savedtime = sharedPref.getString(SAVED_TIME, "");
            long longtime = Long.parseLong(savedtime);
            //Put the date into a readable format
            Date date = new Date(longtime);
            DateFormat formatter = new SimpleDateFormat("hh:mm:aa");

            //Retrieve the saved job
            String savedjob = sharedPref.getString(SAVED_JOB, "");

            //Parse together string reporting open job
            String timeStarted = "Started " + savedjob + "ing " + savedcrop + " at " + formatter.format(date);

            //Insert the date and crop into a text view
            TextView textView = new TextView(this);
            textView.setTextSize(20);
            textView.setText(timeStarted);

            //Update the text layout
            ScrollView msgView = (ScrollView) findViewById(R.id.display_text);
            msgView.removeAllViews();
            msgView.addView(textView);

        } //end if current crop in SharedPreferences
        */
    }

    private class populateSpinners extends AsyncTask<String, Integer, List<String[]>> {
        protected List<String[]> doInBackground(String... param) {

            //Get an array of crops
            SQLiteHelper db = new SQLiteHelper(MainActivity.this);
            String nullsearch = null; // Must send function a null string in order to return all results
            List<String> croplist = db.getCropList(nullsearch);
            java.util.Collections.sort(croplist);
            String[] cropArray = new String[croplist.size()];
            cropArray = croplist.toArray(cropArray);

            //Get an array of jobs
            List<String> joblist = db.getJobList();
            java.util.Collections.sort(joblist); // Put jobs in alphebetical order
            String[] jobArray = new String[joblist.size()];
            jobArray = joblist.toArray(jobArray);

            //Get an array of machinery
            List<String> equiplist = db.getMachineList(nullsearch);
            java.util.Collections.sort(equiplist);
            String[] equipArray = new String[equiplist.size()];
            equipArray = equiplist.toArray(equipArray);

            List<String[]> allSpinners = new ArrayList<>();
            allSpinners.add(cropArray);
            allSpinners.add(jobArray);
            allSpinners.add(equipArray);

            return allSpinners;
        }//end doInBackground

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(List<String[]> allSpinners) {
            String[] cropArray = allSpinners.get(0);
            String[] jobArray = allSpinners.get(1);
            String[] equipArray = allSpinners.get(2);

            //Populate crops spinner
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                    MainActivity.this, R.layout.spinnertext, cropArray); //android.R.layout.simple_spinner_item
            spinnerArrayAdapter.setDropDownViewResource(R.layout.spinnertext); //android.R.layout.simple_spinner_item

            Spinner crop_spinner = (Spinner)findViewById(R.id.crop_spinner);
            crop_spinner.setAdapter(spinnerArrayAdapter);

            //Populate jobs spinner
            ArrayAdapter<String> jobsArrayAdapter = new ArrayAdapter<String>(
                    MainActivity.this, R.layout.spinnertext, jobArray); //android.R.layout.simple_spinner_item
            jobsArrayAdapter.setDropDownViewResource(R.layout.spinnertext);//android.R.layout.simple_spinner_item
            Spinner jobs_spinner = (Spinner)findViewById(R.id.jobs_spinner);
            jobs_spinner.setAdapter(jobsArrayAdapter);

            //Populate machinery spinner
            ArrayAdapter<String> machineArrayAdapter = new ArrayAdapter<String>(
                    MainActivity.this, R.layout.spinnertext, equipArray);
            machineArrayAdapter.setDropDownViewResource(R.layout.spinnertext);
            Spinner machine_spinner = (Spinner)findViewById(R.id.equip_spinner);
            machine_spinner.setAdapter(machineArrayAdapter);
        }// end onPostExecute
    }// end AsyncTask populateSpinners


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.crop_bar) {
            // return true;
            Intent cropIntent = new Intent(this, CropList.class);
            startActivity(cropIntent);
        }
        if (id == R.id.job_bar) {
            // return true;
            Intent cropIntent = new Intent(this, ManageJobs.class);
            startActivity(cropIntent);
        }
        if (id == R.id.equip_bar) {
            // return true;
            Intent cropIntent = new Intent(this, ManageEquipment.class);
            startActivity(cropIntent);
        }
        if (id == R.id.record_bar) {
            // return true;
            Intent cropIntent = new Intent(this, ManageRecords.class);
            startActivity(cropIntent);
        }

        if (id == R.id.summary_bar) {
            Intent summaryIntent = new Intent(this, WorkSummary.class);
            startActivity(summaryIntent);
        }

        if (id == R.id.report_bar) {
            Intent reportIntent = new Intent(this, SendReport.class);
            startActivity(reportIntent);
        }

        if (id == R.id.sheet_bar) {
            Intent sheetIntent = new Intent(this, PopulateSheet.class);
            startActivity(sheetIntent);
        }

        if (id == R.id.feedback_bar) {
            Intent feedbackIntent = new Intent(this, SendFeedback.class);
            startActivity(feedbackIntent);
        }

        if (id == R.id.timeworked_bar) {
            Intent workedIntent = new Intent(this, TimeWorked.class);
            startActivity(workedIntent);
        }

        return super.onOptionsItemSelected(item);
    }


    /*This is run when the user navigates back to the main activity*/
    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();
        String blankParam = "";

        //Populate Spinners
        new populateSpinners().execute(blankParam);

        //Get buttons for ongoing processes
        //refreshButtons();
        new refreshButtons().execute("");

    }// end on resume


    public void onBackPressed() {
        //Close the app when back is pressed
        //android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }


    /*OnClick actions*/

    public void addEquip(View view) {
        //Get currently selected equipment as a string
        Spinner equipSpin = (Spinner) findViewById(R.id.equip_spinner);
        String selectEquip = equipSpin.getSelectedItem().toString();
        //Only add equip if not already contained in string
if(!allEquip.contains(selectEquip)) {
    if(!allEquip.equals("")){
        allEquip = allEquip + ", "+ selectEquip ;
    } else {
        allEquip = selectEquip;
    }
}
        Toast.makeText(getApplicationContext(), "Doing job with "+allEquip, Toast.LENGTH_SHORT).show();
    } // end addEquip


    private class refreshButtons extends AsyncTask<String, Integer, List<String[]>> {
        protected List<String[]> doInBackground(String... param) {

            SQLiteHelper db = new SQLiteHelper(MainActivity.this);
            //Get crops for current timers
            List<String> currentCrops = db.getCurrentCrops();
            String[] cropArray = new String[currentCrops.size()];
            cropArray = currentCrops.toArray(cropArray);

            //Get start times for current timers
            List<Long> currentTimes = db.getCurrentTimes();
            List<String> currentTimeStr = new ArrayList<>();
            for (int i = 0; i < currentTimes.size(); i++){
                String timeStr = Long.toString(currentTimes.get(i));
                currentTimeStr.add(timeStr);
            }
            String[] timeArray = new String[currentTimes.size()];
            timeArray = currentTimeStr.toArray(timeArray);

            //Get jobs for current timers
            List<String> currentJobs = db.getCurrentJobs();
            String[] jobArray = new String[currentJobs.size()];
            jobArray = currentJobs.toArray(jobArray);

            List<String[]> allCurrent = new ArrayList<>();
            allCurrent.add(cropArray);
            allCurrent.add(timeArray);
            allCurrent.add(jobArray);

            return allCurrent;
        }//end doInBackground

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(List<String[]> allCurrent) {

            //First I will connect to the layout and set params including space between buttons
            LinearLayout linLayout = (LinearLayout)findViewById(R.id.clock_controls);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 0, 0, 15);

            //Clear existing entries in layout
            linLayout.removeAllViews();

            //I will also inflate the spacer.xml view for insertion between buttons
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            //Setting up a listener for the button clicks
            OnClickListener listener = new OnClickListener(){
                @Override
                public void onClick(View view){
                    int buttonTag = Integer.parseInt(view.getTag().toString().trim());
                    stopTime(buttonTag);
                    //refreshButtons();
                    new refreshButtons().execute("");
                }
            };

            String[] cropArray = allCurrent.get(0);
            String[] timeArray = allCurrent.get(1);
            String[] jobArray = allCurrent.get(2);

            for (int i = 0; i < cropArray.length; i++) {

                //Put the date into a readable format
                Date date = new Date(Long.parseLong(timeArray[i]));
                DateFormat formatter = new SimpleDateFormat("hh:mm:aa");

                //Parse together string reporting open job
                String stopProcess = jobArray[i] + " on " + cropArray[i] + " (started " + formatter.format(date)+"): Press to Stop";

                Button stopButton = new Button(MainActivity.this);
                stopButton.setTag(i);
                stopButton.setText(stopProcess);

                stopButton.setOnClickListener(listener); //end onclicklistener

                linLayout.addView(stopButton, layoutParams);

                View spacerView = inflater.inflate(R.layout.spacer, null, false);
                linLayout.addView(spacerView);

            } // end current for

        }//end postExecute
    }//end AsyncTask refreshButtons


/*
    public void refreshButtons () {

        //First I will connect to the layout and set params including space between buttons
        LinearLayout linLayout = (LinearLayout)findViewById(R.id.clock_controls);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, 15);

        //Clear existing entries in layout
        linLayout.removeAllViews();

        //I will also inflate the spacer.xml view for insertion between buttons
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        SQLiteHelper db = new SQLiteHelper(this);

        List<String> currentCrops = db.getCurrentCrops();

            List<Long> currentTimes = db.getCurrentTimes();
            List<String> currentJobs = db.getCurrentJobs();

        //Setting up a listener for the button clicks
        OnClickListener listener = new OnClickListener(){
            @Override
            public void onClick(View view){
                int buttonTag = Integer.parseInt(view.getTag().toString().trim());
                stopTime(buttonTag);
                refreshButtons();
            }
        };

        for (int i = 0; i < currentCrops.size(); i++) {

            //Put the date into a readable format
            Date date = new Date(currentTimes.get(i));
            DateFormat formatter = new SimpleDateFormat("hh:mm:aa");

            //Parse together string reporting open job
            String stopProcess = currentJobs.get(i) + " on " + currentCrops.get(i) + " (started " + formatter.format(date)+"): Press to Stop";

            Button stopButton = new Button(this);
            stopButton.setTag(i);
            stopButton.setText(stopProcess);

            stopButton.setOnClickListener(listener); //end onclicklistener

            linLayout.addView(stopButton, layoutParams);

            View spacerView = inflater.inflate(R.layout.spacer, null, false);
            linLayout.addView(spacerView);

        } // end current for


/*

        Date date = new Date(time);
        DateFormat formatter = new SimpleDateFormat("hh:mm:aa");
        // Codes for re-writing this format available at http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
        //String timeStarted = "Started " + activejob + "ing " + activecrop + " at " + formatter.format(date);
        String timeStarted = "Started " + selectedjob + " on " + selectedcrop + " at " + formatter.format(date);

// text ******************************************************************************

        //Add onclick

        TextView textView = new TextView(this);
        textView.setTextSize(20);
        textView.setText(timeStarted);

//This clears and updates the display text view
        ScrollView msgView = (ScrollView) findViewById(R.id.display_text);
        msgView.removeAllViews();
        msgView.addView(textView);

    }
*/


    /* Called when the user clicks the Start button */
    public void startTime(View view) {

        //Get values from spinners and call toCurrent
        Spinner spin = (Spinner) findViewById(R.id.crop_spinner);
        String selectedcrop = spin.getSelectedItem().toString();

        Spinner jobspin = (Spinner) findViewById(R.id.jobs_spinner);
        String selectedjob = jobspin.getSelectedItem().toString();


        EditText workersEdit = (EditText) findViewById(R.id.edit_number_workers);
        String workers = "1";
        int workerEntry = 0;
        String workerText = workersEdit.getText().toString();
        try {
            workerEntry = Integer.parseInt(workerText);
        } catch (NumberFormatException nfe) {
            if (!workerText.equals("") && !workerText.equals(null)) {
                Toast.makeText(getApplicationContext(), "Invalid number of workers", Toast.LENGTH_SHORT).show();
            }
        }
//
        if(workerEntry > 1){
            workers = workersEdit.getText().toString();
        }


        String[] params = {selectedcrop, selectedjob, workers};

        new toCurrent().execute(params);
        //first I need to check if there is already an active crop saved to shared preferences
        //SharedPreferences sharedPref = this.getSharedPreferences(
        //        "aas.beetclock", Context.MODE_PRIVATE);
        //SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
        //SharedPreferences.Editor editor = sharedPref.edit();
       /*
        SQLiteHelper db = new SQLiteHelper(this);

            //Get system time in millaseconds and convert to string
            Long time = System.currentTimeMillis();

            //Get currently selected crop as string
            Spinner spin = (Spinner) findViewById(R.id.crop_spinner);
            String selectedcrop = spin.getSelectedItem().toString();

            //Get currently selected job as a string
            Spinner jobspin = (Spinner) findViewById(R.id.jobs_spinner);
            String selectedjob = jobspin.getSelectedItem().toString();


            List<String> entry = new ArrayList<String>();
            entry.add(selectedcrop);
            entry.add(String.valueOf(time));
            entry.add(selectedjob);
        //add allEquip only if equipment has been entered
        if (allEquip.equals("")){
            entry.add("no equip");
        }else {
            entry.add(allEquip);
        }
            db.addCurrent(entry);

            //Now i need to refresh the stop buttons displayed on the main page:
            //refreshButtons();
        new refreshButtons().execute("");
*/
            //Get time, put the time into a human-readable format


            /*
            //This saves the crop and start time values
            //first clears the old values from the memory
            editor.remove(SAVED_CROP);
            editor.remove(SAVED_TIME);
            editor.remove(SAVED_JOB);
            editor.remove(SAVED_EQUIP);
            //Then saves the new values
            //editor.putString(SAVED_CROP, activecrop);
            editor.putString(SAVED_CROP, selectedcrop);
            editor.putString(SAVED_TIME, time_str);
            editor.putString(SAVED_JOB, selectedjob);
            editor.putString(SAVED_EQUIP, allEquip);
            editor.commit();


            //Return message with crop and time started

          /*
            //First, retrieve saved preference values
            savedcrop = sharedPref.getString(SAVED_CROP, "");
            String savedtime = sharedPref.getString(SAVED_TIME, "");

            //In order to work with time I need it as a long
            long longtime = Long.parseLong(savedtime);


            //Then, put the time into a human-readable format
            TextView textView = new TextView(this);
            textView.setTextSize(20);
            Date date = new Date(time);
            DateFormat formatter = new SimpleDateFormat("hh:mm:aa");
            // Codes for re-writing this format available at http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
            //String timeStarted = "Started " + activejob + "ing " + activecrop + " at " + formatter.format(date);
            String timeStarted = "Started " + selectedjob + " on " + selectedcrop + " at " + formatter.format(date);
            textView.setText(timeStarted);

//This clears and updates the display text view
            ScrollView msgView = (ScrollView) findViewById(R.id.display_text);
            msgView.removeAllViews();
            msgView.addView(textView);
            */
        //Lets try to create a database!
        //MySQLiteHelper db = new MySQLiteHelper(this);

        //db.addBook(entry1);
        //db.addBook(entry2);

    } // End Start

    private class toCurrent extends AsyncTask<String[], Integer, String> {
        protected String doInBackground(String[]... params) {

            String[] selected = params[0];
            SQLiteHelper db = new SQLiteHelper(MainActivity.this);

            //Get system time in millaseconds and convert to string
            Long time = System.currentTimeMillis();
            String selectedcrop = selected[0];
            String selectedjob = selected[1];
            String workersString = selected[2];


            List<String> entry = new ArrayList<String>();
            entry.add(selectedcrop);
            entry.add(String.valueOf(time));
            entry.add(selectedjob);
                        //add allEquip only if equipment has been entered
            if (allEquip.equals("")){
                entry.add("no equip");
            }else {
                entry.add(allEquip);
            }
            entry.add(workersString);
            db.addCurrent(entry);

            String noReturn = "";
            return noReturn;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String result) {
            //When db entry is written, refresh buttons
            new refreshButtons().execute("");
        }
    }



    public void stopTime(int process) {

        new saveFinished().execute(process);
        /*
        //first I need to make sure there is already an active crop saved to shared preferences; otherwise the button should do nothing
        //SharedPreferences sharedPref = this.getSharedPreferences(
        //        "aas.beetclock", Context.MODE_PRIVATE);
        //SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
        //SharedPreferences.Editor editor = sharedPref.edit();
        //String savedcrop = sharedPref.getString(SAVED_CROP, "");
        SQLiteHelper db = new SQLiteHelper(this);
       // List<String> savedcrops = db.getCurrentCrops();
       // if(savedcrops.size() == 0) {} else {
        //if(savedcrop.equals("")) {}else {

            //First I will need to get the current time from the system clock
            Long stoptime = System.currentTimeMillis();
            //And also retrieve the start time and crop from the database
            List<String> currentCrops = db.getCurrentCrops();
                List<Long> currentTimes = db.getCurrentTimes();
                List<String> currentJobs = db.getCurrentJobs();
            List<String> currentEquip = db.getCurrentMachinery();
        String procString = Integer.toString(process);
        //Remove the stopped process from the database
        db.deleteCurrent(procString);

            //Now I can calculate time elapsed
            long elapsed = stoptime - currentTimes.get(process);

            //If more than 1 worker is working, I will multiply by # of workers

            //If string is convertable to int, convert to int
            int workers = 1;
            try {
                workers = Integer.parseInt(workersString);
            } catch (NumberFormatException nfe) {
            }
            long elapsedWork = 0;
            if(elapsed > 0 && workers > 1){
                elapsedWork = elapsed * (long)workers;
            } else {
                elapsedWork = elapsed;
            }
//add entry to times database
            List<String> entry = new ArrayList<String>();
            entry.add(currentCrops.get(process));
            entry.add(String.valueOf(stoptime));
            entry.add(String.valueOf(elapsedWork));
            entry.add(currentJobs.get(process));
            entry.add(currentEquip.get(process));

            db.addTime(entry);

            //I also want to display the elapsed time on screen
            //The date formatter does not work for elapsed time, so I will need to use a different approach.  Thanks to Yiwei
            String timeFormat = String.format("%d hrs, %d min, %d sec",
                    TimeUnit.MILLISECONDS.toHours(elapsed),
                    TimeUnit.MILLISECONDS.toMinutes(elapsed) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsed)),
                    TimeUnit.MILLISECONDS.toSeconds(elapsed) -
                            (TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsed)))
            );


            String timeElapsed = "Worked on " + currentCrops.get(process) + " for " + timeFormat;

        Toast.makeText(getApplicationContext(), timeElapsed, Toast.LENGTH_LONG).show();

/*
            //Update the display layout with a text view showing time elapsed
            TextView textView = new TextView(this);
            textView.setTextSize(20);
            textView.setText(timeElapsed);
            ScrollView msgView = (ScrollView) findViewById(R.id.display_text);
            msgView.removeAllViews();
            msgView.addView(textView);


            String savedtime = sharedPref.getString(SAVED_TIME, "");
            //In order to work with time I need it as a long
            long starttime = Long.parseLong(savedtime);

            //Now I can calculate time elapsed
            long elapsed = stoptime - starttime;

            //If more than 1 worker is working, I will multiply by # of workers
            EditText workersEdit = (EditText) findViewById(R.id.edit_number_workers);
            String workersString =  workersEdit.getText().toString();
            //If string is convertable to int, convert to int
            int workers = 1;
            try {
                workers = Integer.parseInt(workersString);
            } catch (NumberFormatException nfe) {
            }
            long elapsedWork = 0;
            if(elapsed > 0 && workers > 1){
                elapsedWork = elapsed * (long)workers;
            } else {
                elapsedWork = elapsed;
            }

            //I will also need to get the job from the job spinner
            String savedjob = sharedPref.getString(SAVED_JOB, "");
            String savedEquip = sharedPref.getString(SAVED_EQUIP, "no equip");

            //I can now save the crop, system time and elapsed time to my Times table

            //Start by creating a string list with crop, system time, elapsed, job
            List<String> entry = new ArrayList<String>();
            entry.add(savedcrop);
            entry.add(String.valueOf(stoptime));
            entry.add(String.valueOf(elapsedWork));
            entry.add(savedjob);
            entry.add(savedEquip);

            SQLiteHelper db = new SQLiteHelper(this);
            db.addTime(entry);


            //I also want to display the elapsed time on screen
            //The date formatter does not work for elapsed time, so I will need to use a different approach.  Thanks to Yiwei
            String timeFormat = String.format("%d hrs, %d min, %d sec",
                    TimeUnit.MILLISECONDS.toHours(elapsed),
                    TimeUnit.MILLISECONDS.toMinutes(elapsed) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsed)),
                    TimeUnit.MILLISECONDS.toSeconds(elapsed) -
                            (TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsed)))
            );


            String timeElapsed = "Worked on " + savedcrop + " for " + timeFormat;


            //Update the display layout with a text view showing time elapsed
            TextView textView = new TextView(this);
            textView.setTextSize(20);
            textView.setText(timeElapsed);
            ScrollView msgView = (ScrollView) findViewById(R.id.display_text);
            msgView.removeAllViews();
            msgView.addView(textView);

            //Clear the saved crop and start time from memory
            editor.remove(SAVED_CROP);
            editor.remove(SAVED_TIME);
            editor.remove(SAVED_JOB);
            editor.remove(SAVED_EQUIP);
            editor.commit();
            */
        //} // end else no crop saved to preferences
    } // End Stop


    private class saveFinished extends AsyncTask<Integer, Integer, String> {
        protected String doInBackground(Integer... params) {

            //Load process
            int process = params[0];

            SQLiteHelper db = new SQLiteHelper(MainActivity.this);
            // List<String> savedcrops = db.getCurrentCrops();
            // if(savedcrops.size() == 0) {} else {
            //if(savedcrop.equals("")) {}else {

            //First I will need to get the current time from the system clock
            Long stoptime = System.currentTimeMillis();
            //And also retrieve the start time and crop from the database
            List<String> currentCrops = db.getCurrentCrops();
            List<Long> currentTimes = db.getCurrentTimes();
            List<String> currentJobs = db.getCurrentJobs();
            List<String> currentEquip = db.getCurrentMachinery();
            List<Long> currentWorkers = db.getCurrentWorkers();
            String procString = Integer.toString(process);
            //Remove the stopped process from the database
            db.deleteCurrent(procString);

            //Now I can calculate time elapsed
            long elapsed = stoptime - currentTimes.get(process);

            //If more than 1 worker is working, I will multiply by # of workers

            //If string is convertable to int, convert to int

            long elapsedWork = 0;
            if(elapsed > 0 && currentWorkers.get(process) > 1){
                elapsedWork = elapsed * currentWorkers.get(process);
            } else {
                elapsedWork = elapsed;
            }
//add entry to times database
            List<String> entry = new ArrayList<String>();
            entry.add(currentCrops.get(process));
            entry.add(String.valueOf(stoptime));
            entry.add(String.valueOf(elapsedWork));
            entry.add(currentJobs.get(process));
            entry.add(currentEquip.get(process));

            db.addTime(entry);

            //I also want to display the elapsed time on screen
            //The date formatter does not work for elapsed time, so I will need to use a different approach.  Thanks to Yiwei
            String timeFormat = String.format("%d hrs, %d min, %d sec",
                    TimeUnit.MILLISECONDS.toHours(elapsed),
                    TimeUnit.MILLISECONDS.toMinutes(elapsed) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsed)),
                    TimeUnit.MILLISECONDS.toSeconds(elapsed) -
                            (TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsed)))
            );

            String workString = Long.toString(currentWorkers.get(process));
            String timeElapsed = workString+" workers on " + currentCrops.get(process) + " for " + timeFormat;

            return timeElapsed;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String timeElapsed) {
            Toast.makeText(getApplicationContext(), timeElapsed, Toast.LENGTH_LONG).show();
        }
    } // end AsyncTaks saveFinished


}