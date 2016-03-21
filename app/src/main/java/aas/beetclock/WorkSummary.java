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
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Arrays;
import android.util.Log;
import android.os.StrictMode;
import android.content.Intent;
import java.io.File;
import android.os.Environment;



public class WorkSummary extends AppCompatActivity {

    public final static String DISPLAY_SUMMARY = "aas.beetclock.SUMMARY";
    public final static String REPORT_DATE = "aas.beetclock.Report_Date";

    public String SELECTED_DATE = new String();
    public String reportDate;

    File file = null;


   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_summary);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("View summaries");

       //Enables Strict Mode testing
       /*
       StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
               .detectAll()
               .penaltyLog()
                       //.penaltyDialog()
               .penaltyFlashScreen()
               .build());
               */

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
//Get crop list from DB
            SQLiteHelper db = new SQLiteHelper(WorkSummary.this);
            String nullsearch = null; // Must send function a null string in order to return all results
            List<String> croplist = db.getCropList(nullsearch);
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

            //This initializes the spinner with values from crop table
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
                System.out.println("Saved report date:"+formatter.format(date));

                dateSince = "Retrieve records since you started using BeetClock, OR";
            }else {
                //If there is a date of last report, retrieve records since that date
                Date date = new Date(Long.valueOf(reportDate));
                DateFormat formatter = new SimpleDateFormat("dd:MMM:yyyy");
                // Codes for re-writing this format available at http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
                System.out.println("Saved report date:"+formatter.format(date));
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

        SharedPreferences sharedPref = this.getSharedPreferences(
                "aas.beetclock", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        String selection = data.getStringExtra("selection");
        //Make the selected date public

        new onLoad().execute(selection);

    }//end onResult


    public void displayReport(View view){

        //Get selected crop from spinner
        Spinner spin = (Spinner) findViewById(R.id.crop_report_spinner);
        String cropSelect = spin.getSelectedItem().toString();
        System.out.println(cropSelect);

        new createSummary().execute(cropSelect);

    };


    public void displayRecords(View view){
        new createDump().execute("");

    };


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
       float totalDec = Float.valueOf(timeTotal[0]);
       String totalHours = String.format("%.2f", totalDec / 3600000);
        //NOW I can create a string to print using the for loop below on the list of string[] arrays
        StringBuilder liststring = new StringBuilder();
        String sep = ": ";
       //Starting with the total
       liststring.append("Total time for "+cropSelect+": "+totalHours).append(" hours");
       liststring.append(System.getProperty("line.separator"));

        for (int i = 0; i < jobsArray.length; i++) {
//Adding both crop and job
            liststring.append(jobsArray[i]);
            liststring.append(sep).append("with ").append(equipArray[i]);
            liststring.append(sep).append(timesArray[i]);
            liststring.append(" hours");
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
//Getting a list of ALL jobs
        List<String> alljobs = db.getJobList();
        List<String> allequip = db.getMachineList(nullsearch);
        allequip.add("no equip"); //This summarizes work for which no equipment was added

        System.out.println("Cropslist");
        System.out.println(cropslist.size());
        System.out.println("Jobslist");
        System.out.println(jobslist.size());
        System.out.println("Timeslist");
        System.out.println(timeslist.size());
        System.out.println("Elapsedlist");
        System.out.println(elapsedlist.size());
        System.out.println("Alljobs");
        System.out.println(alljobs.size());
        System.out.println("Allequipment");
        System.out.println(equiplist.size());


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
        for (int i = 0; i < allequip.size(); i++) {
            //Loop through all jobs
            for (int j = 0; j < alljobs.size(); j++) {
                //--For each crop,loop through all entries
                long worksum = 0;
                for (int k = 0; k < cropslist.size(); k++) {
                    //---if crop and job match combo, sum elapsed time as worksum
                    if (cropslist.get(k).equals(crop) && jobslist.get(k).contains(alljobs.get(j)) &&
                            equiplist.get(k).contains(allequip.get(i)) && timeslist.get(k) > startDate) {
                        //worksum.add(elapsedlist.get(i));
                        worksum += elapsedlist.get(k);
                        totalTime += elapsedlist.get(k);
                    }
                } //end all entries for
                //--if worksum > 0, add job and summary to array
//We want zeros for all jobs w/o an entry
                if (worksum > 0) {
                    String hours = String.format("%.2f", (float) worksum / 3600000); // Should produce hours to 2 decimal places
                    //Adding job and time to lists
                    String thisJob = alljobs.get(j);
                    String thisEquip = allequip.get(i);
                    sumTimes.add(hours);
                    sumJobs.add(thisJob);
                    sumEquip.add(thisEquip);
                }

            } // end all jobs for
        } // end all equip for

            String[] jobsArray = sumJobs.toArray(new String[0]);
            String[] equipArray = sumEquip.toArray(new String[0]);
        String[] timesArray = sumTimes.toArray(new String[0]);
        String[] totalArray = {String.valueOf(totalTime)};

        //Test this summary: print all job time pairs
        for (int i = 0; i < jobsArray.length; i++) {
            System.out.println(jobsArray[i]);
            System.out.println(equipArray[i]);
            System.out.println(timesArray[i]);
        }
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

        if (SELECTED_DATE.equals("")) {
            //If no date selected, stay at zero OR start at last report date
            if (reportDate.equals("")) {
                //If there is no date of last report, stay at zero
            } else {
                //If there is a date of last report, start there
                startDate = Long.parseLong(reportDate);
            }
        } else {  //If there is a selected date, set as start date
            startDate = Long.parseLong(SELECTED_DATE);
        }//end selected date else

//Enter start date as first line of summary
        DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        // Codes for re-writing this format available at http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
        String startFormat = "BeetClock records beginning "+formatter.format(startDate);
        String[] startLine = {startFormat};
        summaries.add(startLine);

                //Go through the records and add each to a list<string[]>
                for (int i = 0; i < cropslist.size(); i++) {
                    if(timeslist.get(i) > startDate) {
                        //Format work times
                        String hrsFormat = String.valueOf( TimeUnit.MILLISECONDS.toHours(elapsedlist.get(i)) );
                        String hrsSep = " hrs";
                        String minFormat = String.valueOf( TimeUnit.MILLISECONDS.toMinutes(elapsedlist.get(i)) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsedlist.get(i))) );
                        String minSep = " min";
                        String secFormat = String.valueOf( TimeUnit.MILLISECONDS.toSeconds(elapsedlist.get(i)) -
                                (TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedlist.get(i))) ));
                        String secSep = " sec";
//Add a column with the record date
                        String dateFormat = formatter.format(timeslist.get(i));
                        String timeFormat = hrsFormat+hrsSep+" "+minFormat+minSep+" "+secFormat+secSep;

                        String[] summary = {cropslist.get(i), jobslist.get(i), equiplist.get(i), timeFormat, dateFormat}; //String.valueOf(worksum)
                        summaries.add(summary);
                    } //end timeslist if
                } //end all entries for
                //--if worksum > 0, create string[] array with crop, job, worksum


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













}
