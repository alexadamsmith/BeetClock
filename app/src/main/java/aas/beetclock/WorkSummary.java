package aas.beetclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Arrays;
import android.util.Log;
import android.os.StrictMode;
import android.content.Intent;
import java.io.File;
import android.os.Environment;

import com.google.common.collect.Lists;


public class WorkSummary extends AppCompatActivity {

    public final static String DISPLAY_SUMMARY = "aas.beetclock.SUMMARY";
    public final static String REPORT_DATE = "aas.beetclock.Report_Date";

    public String reportDate;

    File file = null;

    public ImageView background;


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
        setContentView(R.layout.activity_work_summary);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("View records");

       new onLoad().execute("");

   }

    private class onLoad extends AsyncTask<String, Integer, List<String>> {
        protected List<String> doInBackground(String... param) {

            String selectedDate = param[0];

            SharedPreferences sharedPref = WorkSummary.this.getSharedPreferences(
                    "aas.beetclock", Context.MODE_PRIVATE);

            //If report date has just been selected, store this to Saved Preferences
            if(selectedDate.equals("")) {
            } else {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(REPORT_DATE, selectedDate);
            editor.commit();
            }

//Set saved date as default reportDate
            reportDate = sharedPref.getString(REPORT_DATE, "0");
//Get non-duplicated list of crops from timer entries
            SQLiteHelper db = new SQLiteHelper(WorkSummary.this);
            List<String> croplist = db.getCrops();
             Set<String> hs = new HashSet<>();
            hs.addAll(croplist);
            croplist.clear();
            croplist.addAll(hs);



            java.util.Collections.sort(croplist, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareToIgnoreCase(o2);
                }
            }); // Alphebetizes while ignoring case

            return croplist;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(List<String> croplist) {

            //Initialize crops spinner and populate with items
            String[] spinarray = new String[croplist.size()];
            spinarray = croplist.toArray(spinarray);

            //This initializes the spinner with crop names
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                    WorkSummary.this, R.layout.spinnertext, spinarray);
            spinnerArrayAdapter.setDropDownViewResource(R.layout.spinnertext);

            Spinner crop_report_spinner = (Spinner)findViewById(R.id.crop_report_spinner);
            crop_report_spinner.setAdapter(spinnerArrayAdapter);

//Set default date in textview
            TextView msgView = (TextView) findViewById(R.id.date_text);
            msgView.setTextSize(16);
            String dateSince = new String();
            if(reportDate.equals("0")){
                //If there is no date of last report, retrieve all records ever
                Date date = new Date(Long.valueOf(reportDate));
                DateFormat formatter = new SimpleDateFormat("dd:MMM:yyyy");

                dateSince = "Retrieve records since you started using BeetClock, OR";
            }else {
                //If there is a date of last report, retrieve records since that date
                Date date = new Date(Long.valueOf(reportDate));
                DateFormat formatter = new SimpleDateFormat("dd:MMM:yyyy");
                // Codes for re-writing this format available at http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html

                dateSince = "Retrieve records since " + formatter.format(date)+" OR";
            }//end reporDate else
            msgView.setText(dateSince);
        }
    } // end AsyncTask onLoad


    public void onBackPressed() {
        finish();
            }

    public void onResume(){
        super.onResume();

    }//end onResume




    public void onActivityResult(int requestCode, int resultCode, Intent data) {  // When result of date picker is returned
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            String selection = data.getStringExtra("selection");
            //Make the selected date public

            new onLoad().execute(selection);
        }
    }//end onResult


    public void displayReport(View view){

        //Get selected crop from spinner
        Spinner spin = (Spinner) findViewById(R.id.crop_report_spinner);

if(spin.getSelectedItem() != null) {
    String cropSelect = spin.getSelectedItem().toString();
    new createSummary().execute(cropSelect);
}
    } // end displayReport


    public void displayRecords(View view){
        new createDump().execute("");

    }


    public void openDatePicker(View view){
        //Do nothing but open the date picker
        Intent intent = new Intent(this, PickDate.class);
        startActivityForResult(intent, 12345);
    }





    // creates a summary for the selected crop
       private class createSummary extends AsyncTask<String, Integer, String> {
           protected String doInBackground(String... param) {

               //When the generate work summary button is pressed - Yikes!
        String cropSelect = param[0];
       List<String[]> summary = makeSum(cropSelect);
       //Retrieve jobs list, crop list, total
       String[] jobsArray = summary.get(0);
       String[] equipArray = summary.get(1);
       String[] timesArray = summary.get(2);
       String[] timeTotal = summary.get(3);
               Long totTime = Long.parseLong(timeTotal[0]);
               String hrsFormat = String.valueOf(TimeUnit.MILLISECONDS.toHours(totTime));
               String hrsSep = " hrs ";
               String minFormat = String.valueOf(TimeUnit.MILLISECONDS.toMinutes(totTime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(totTime)));
               String minSep = " min";
               String totalTime = hrsFormat+hrsSep+minFormat+minSep;
       //float totalDec = Float.valueOf(timeTotal[0]);
       //String totalHours = String.format("%.2f", totalDec / 3600000);
        //NOW I can create a string to print using the for loop below on the list of string[] arrays
        StringBuilder liststring = new StringBuilder();
        String sep = ": ";
       //Starting with the total
       liststring.append("Total time for "+cropSelect+": "+totalTime);
       liststring.append(System.getProperty("line.separator"));
               liststring.append(System.getProperty("line.separator"));

        for (int i = 0; i < jobsArray.length; i++) {
//Adding both crop and job
            liststring.append(jobsArray[i]);
            liststring.append(sep).append("with ").append(equipArray[i]);
            liststring.append(sep).append(timesArray[i]);
            liststring.append(System.getProperty("line.separator"));
            liststring.append(System.getProperty("line.separator"));
        } // end jobsArray for

       //Return the summary
       String display = liststring.toString();
       return display;
           }

           protected void onProgressUpdate(Integer... progress) {
           }

           protected void onPostExecute(String summary) {
               Intent intent = new Intent(WorkSummary.this, DisplaySummary.class);
               intent.putExtra(DISPLAY_SUMMARY, summary);
               startActivity(intent);
           }
       }//end AsyncTask createSummary




    public List<String[]> makeSum (String crop){

        List<String[]> outputs = new ArrayList<>();

        //These lists will be populated as we iterate through the results
        List<String> sumJobs = new ArrayList<>();
        List<String> sumTimes = new ArrayList<>();
        List<String> sumEquip = new ArrayList<>();
        long totalTime = 0;


        //Retrieving all records
        SQLiteHelper db = new SQLiteHelper(this);
        String nullsearch = null; // Must send function a null string in order to return all results
        List<String> cropslist = db.getCrops();
        List<Long> timeslist = db.getTimes();
        List<Long> elapsedlist = db.getElapsed();
        List<String> jobslist = db.getJobs();
        List<String> equiplist = db.getMachine();

//Removing duplicates from jobs and equipment lists using a hashset
        List<String> alljobs = db.getJobs();
        Set<String> hs = new HashSet<>();
        hs.addAll(alljobs);
        alljobs.clear();
        alljobs.addAll(hs);
        //Sort all jobs list
        java.util.Collections.sort(alljobs, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        }); // Alphebetizes while ignoring case

        List<String> allequip = db.getMachine();
        hs.clear();
        hs.addAll(allequip);
        allequip.clear();
        allequip.addAll(hs);

//Retrieve the date of the last report
        //SharedPreferences sharedPref = WorkSummary.this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences sharedPref = this.getSharedPreferences(
                "aas.beetclock", Context.MODE_PRIVATE);
        //SharedPreferences.Editor editor = sharedPref.edit();
        String reportDate = sharedPref.getString(REPORT_DATE, "");

//Set a start date for the summary
        long startDate = 0;

        if (reportDate.equals("")) {
            //If no date saved, stay at zero
        } else {
            //If there is a date saved, start there
            startDate = Long.parseLong(reportDate);
        }//end selected date else

//Loop through all machinery
            for (int j = 0; j < alljobs.size(); j++) {
                //--For each crop,loop through all entries
                for (int i = 0; i < allequip.size(); i++) {
                    //Loop through all jobs
                long worksum = 0;
                for (int k = 0; k < cropslist.size(); k++) {
                    //---if crop and job match combo, sum elapsed time as worksum
                    if (cropslist.get(k).equals(crop) && jobslist.get(k).contains(alljobs.get(j)) &&
                            equiplist.get(k).equals(allequip.get(i)) && timeslist.get(k) > startDate) {
                        //worksum.add(elapsedlist.get(i));
                        worksum += elapsedlist.get(k);
                        totalTime += elapsedlist.get(k);
                    }
                } //end all entries for
                //--if worksum > 0, add job and summary to array
//We want zeros for all jobs w/o an entry
                if (worksum > 0) {
                    String hrsFormat = String.valueOf(TimeUnit.MILLISECONDS.toHours(worksum));
                    String hrsSep = " hrs ";
                    String minFormat = String.valueOf(TimeUnit.MILLISECONDS.toMinutes(worksum) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(worksum)));
                    String minSep = " min";
                    String elapsed = hrsFormat+hrsSep+minFormat+minSep;
                    //String hours = String.format("%.2f", (float) worksum / 3600000); // Should produce hours to 2 decimal places
                    //Adding job and time to lists
                    String thisJob = alljobs.get(j);
                    String thisEquip = allequip.get(i);
                    sumTimes.add(elapsed);
                    sumJobs.add(thisJob);
                    sumEquip.add(thisEquip);
                }

            } // end all equip for
        } // end all jobs for

            String[] jobsArray = sumJobs.toArray(new String[0]);
            String[] equipArray = sumEquip.toArray(new String[0]);
        String[] timesArray = sumTimes.toArray(new String[0]);
        String[] totalArray = {String.valueOf(totalTime)};

        outputs.add(jobsArray);
            outputs.add(equipArray);
        outputs.add(timesArray);
        outputs.add(totalArray);

        return outputs;
    }//end makeSum


    private class createDump extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... param) {


        //Retrieving all records
        SQLiteHelper db = new SQLiteHelper(WorkSummary.this);
        //String nullsearch = null; // Must send function a null string in order to return all results
        List<String> cropslist = db.getCrops();
        List<Long> timeslist = db.getTimes();
        List<Long> elapsedlist = db.getElapsed();
        List<String> jobslist = db.getJobs();
        List<String> equiplist = db.getMachine();


            List<Long> timeSort = sortListWithoutModifyingOriginalList(timeslist);

        //Create list of string[] arrays
        List<String[]> summaries = new ArrayList<String[]>();

//Retrieve the date of the last report
        //SharedPreferences sharedPref = WorkSummary.this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences sharedPref = WorkSummary.this.getSharedPreferences(
                "aas.beetclock", Context.MODE_PRIVATE);
        //SharedPreferences.Editor editor = sharedPref.edit();
        String reportDate = sharedPref.getString(REPORT_DATE, "0");

//Set a start date for the summary
        long startDate = 0;

            if (reportDate.equals("")) {
                //If there is no date of last report, stay at zero
            } else {
                //If there is a date of last report, start there
                startDate = Long.parseLong(reportDate);
            }

//Enter start date as first line of summary
        DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        // Codes for re-writing this format available at http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
        String startFormat = "BeetClock records since "+formatter.format(startDate);
        String[] startLine = {startFormat};
        summaries.add(startLine);

//Sort entries by date saved and add to dump!
            for (int i = 0; i < timeSort.size(); i++) {
                for (int j = 0; j < timeslist.size(); j++) {
                    if (timeSort.get(i).equals(timeslist.get(j)) && timeslist.get(j) > startDate) {
                        //Format work times
                        String hrsFormat = String.valueOf( TimeUnit.MILLISECONDS.toHours(elapsedlist.get(j)) );
                        String hrsSep = " hrs";
                        String minFormat = String.valueOf( TimeUnit.MILLISECONDS.toMinutes(elapsedlist.get(j)) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsedlist.get(j))) );
                        String minSep = " min";

//Add a column with the record date
                        String dateFormat = formatter.format(timeslist.get(j));

                        String[] summary = {cropslist.get(j), jobslist.get(j), equiplist.get(j), hrsFormat, hrsSep, minFormat, minSep, dateFormat}; //String.valueOf(worksum)
                        summaries.add(summary);
                    }
                }//end j for
            }//end i for

        //NOW I can create a string to print using the for loop below on the list of string[] arrays
        StringBuilder liststring = new StringBuilder();
        String sep = "; ";
        for (int i = 0; i < summaries.size(); i++) {
            for (int j = 0; j < summaries.get(i).length; j++){
                //I can add both the crop and job
                if(j>0) {
                    liststring.append(sep).append(summaries.get(i)[j]);
                } else {
                    liststring.append(summaries.get(i)[j]);
                }
            } // end for array length
            liststring.append(System.getProperty("line.separator"));
            liststring.append(System.getProperty("line.separator"));

        } // end list for

        //Return the summary
        String display = liststring.toString();
        return display;


        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(String summary) {
            Intent intent = new Intent(WorkSummary.this, DisplaySummary.class);
            intent.putExtra(DISPLAY_SUMMARY, summary);
            startActivity(intent);
        }
    } // end AsyncTask Create Dump


    private <E extends Comparable<E>> List<E> sortListWithoutModifyingOriginalList(List<E> list){
        List<E> newList = new ArrayList<E>(list);
        Collections.sort(newList);
        return newList;
    }










}
