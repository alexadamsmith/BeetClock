package aas.beetclock;

/*BeetClock version 01 (Beta) was created by Alex Smith, Feb 2016.  The code is free to share
under a Creative Commons Attribution-ShareAlike 4.0 International License.*/

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import aas.beetclock.SQLiteHelper;

import java.util.Comparator;
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

   public FrameLayout frameLayout;
    public ImageView background;
    public LayoutInflater inflater;



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


        inflater = this.getLayoutInflater();
        frameLayout = new FrameLayout(this);
        View view = inflater.inflate(R.layout.activity_main, null);
        frameLayout.addView(view);

        //setContentView(R.layout.activity_main);
        setContentView(frameLayout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Default background image is tall
        background = (ImageView) findViewById(R.id.background_image);
        background.setImageResource(R.drawable.bcbg2_tall);

        //set listener for orientation change (image view is declared final)
        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
                                       int oldTop, int oldRight, int oldBottom) {
                int orientation = getResources().getConfiguration().orientation;
//First clear existing bitmap

                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                           background.setImageResource(R.drawable.bcbg2_wide);
                } else {
                    background.setImageResource(R.drawable.bcbg2_tall);
                }
            }
        });

        // Run when the app loads***********************************

        //Populate Spinners
        new populateSpinners().execute("");

        //Get buttons for ongoing processes
        //refreshButtons();
        new refreshButtons().execute("");

        // set initial background image based on configuration

    }//End onCreate


/*
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        frameLayout.removeAllViews();
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_main, null);
        frameLayout.addView(view);

        // refresh the instructions image; Thanks to Seven7s! http://stackoverflow.com/questions/7203384/changing-image-in-imageview-on-orientation-change-does-not-work
        ImageView background = (ImageView) findViewById(R.id.background_image);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            background.setImageResource(R.drawable.bcbg_wide);
        } else {
            background.setImageResource(R.drawable.bcbg_tall);
        }
        background.refreshDrawableState();

    }
*/
    private class populateSpinners extends AsyncTask<String, Integer, List<String[]>> {
        protected List<String[]> doInBackground(String... param) {

            //Get an array of crops
            SQLiteHelper db = new SQLiteHelper(MainActivity.this);
            String nullsearch = null; // Must send function a null string in order to return all results
            List<String> croplist = db.getCropList(nullsearch);
            java.util.Collections.sort(croplist, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareToIgnoreCase(o2);
                }
            }); // Alphebetizes while ignoring case
            String[] cropArray = new String[croplist.size()];
            cropArray = croplist.toArray(cropArray);

            //Get an array of jobs
            List<String> joblist = db.getJobList();
            java.util.Collections.sort(joblist, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareToIgnoreCase(o2);
                }
            }); // Alphebetizes while ignoring case
            String[] jobArray = new String[joblist.size()];
            jobArray = joblist.toArray(jobArray);

            //Get an array of machinery
            List<String> equiplist = db.getMachineList(nullsearch);
            java.util.Collections.sort(equiplist, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareToIgnoreCase(o2);
                }
            }); // Alphebetizes while ignoring case
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