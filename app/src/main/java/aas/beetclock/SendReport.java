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
import android.widget.DatePicker;
import java.util.Calendar;
import android.app.Dialog;
import android.app.DatePickerDialog;
import android.app.Activity;
import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.io.File;
import android.os.Environment;
import android.widget.Toast;


public class SendReport extends AppCompatActivity {

    public final static String DISPLAY_SUMMARY = "aas.beetclock.SUMMARY";
    public final static String SAVED_SENDER = "aas.beetclock.report_sender";
    public final static String SAVED_RECIPIENT = "aas.beetclock.report_recipient";
    public final static String REPORT_DATE = "aas.beetclock.Report_Date";
    //public final static List<String> SHEET_PARAMS = new ArrayList<>(Arrays.asList("aas.beetclock.sheetUrl", "aas.beetclock.sheetData"));
    public final static String SHEET_PARAMS = "aas.beetclock.sheetParams";

    public String SELECTED_DATE = new String();
    public String reportDate = "0";
    public String senderSaved;
    public String recipSaved;

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
        setContentView(R.layout.activity_send_report);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Email Report");

        new onLoad().execute("");

    }

    private class onLoad extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... param) {

            String selectedDate = param[0];

            SharedPreferences sharedPref = SendReport.this.getSharedPreferences(
                    "aas.beetclock", Context.MODE_PRIVATE);

            //If report date has just been selected, store this to Saved Preferences
            if(selectedDate.equals("")) {
            } else {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(REPORT_DATE, selectedDate);
                editor.commit();
            }



//Set saved values to public strings
            reportDate = sharedPref.getString(REPORT_DATE, "0");
            senderSaved = sharedPref.getString(SAVED_SENDER, "");
            recipSaved = sharedPref.getString(SAVED_RECIPIENT, "");

            return "";
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String param) {
//Set text with saved report date
            TextView msgView = (TextView) findViewById(R.id.date_text_report);
            msgView.setTextSize(16);
            String dateSince = new String();
            if(reportDate.equals("")){
                //If there is no date of last report, retrieve all records ever
                dateSince = "Retrieve records since you started using BeetClock, OR ";
            }else {
                //If there is a date of last report, retrieve records since that date
                Date date = new Date(Long.valueOf(reportDate));
                DateFormat formatter = new SimpleDateFormat("dd:MMM:yyyy");
                // Codes for re-writing this format available at http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
                dateSince = "Retrieve records since " + formatter.format(date)+" OR";
            }//end reporDate else
            msgView.setText(dateSince);
//Set text with saved sender
            TextView senderView = (TextView) findViewById(R.id.sender_text);
            senderView.setTextSize(16);
            String senderText = "";
            if(senderSaved.equals("")){
            }else{
                senderText = ("Send report as " + senderSaved + " OR ");
            }
            senderView.setText(senderText);
 //Set text with saved address
            TextView recipView = (TextView) findViewById(R.id.email_text);
            recipView.setTextSize(16);
            String recipText = "";
            if(recipSaved.equals("")){
            }else{
                recipText = "Send report to " + recipSaved + " OR ";
            }
            recipView.setText(recipText);

        }
    } // end AsyncTask onLoad


    public void onBackPressed() {
        finish();

    }

    public void onResume(){
        super.onResume();

        new onLoad().execute("");

    }//end onResume





    //Will also need an onResume method here to refresh the date in the text field
    /*This is run when the user navigates back to the main activity*/
    //@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {  // When result of date picker is returned
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            String selection = data.getStringExtra("selection");
            new onLoad().execute(selection);
        }
    }//end onResult


    public void sendReport(View view){
        //Emails a report to the specified address
        String inputs = "";
        new queryDB().execute(inputs);

    }//end sendReport

    private class queryDB extends AsyncTask<String, Integer, String[]> {
        protected String[] doInBackground(String... params) {
            String summary = createSummary();
            String dump = createDump();
            String[] queryOut = {summary, dump};
            return queryOut;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String[] queryOut) {
            //Gets summary and full record dump from query calls
            String summary = queryOut[0];
            String dump = queryOut[1];
            //Builds report
            StringBuilder reportBuilder = new StringBuilder();
            reportBuilder.append(summary);
            reportBuilder.append(System.getProperty("line.separator"));
            reportBuilder.append(System.getProperty("line.separator"));
            reportBuilder.append(System.getProperty("line.separator"));
            reportBuilder.append(dump);

            String fullReport = reportBuilder.toString();

//Access shared prefs to load saved sender and recip; save new values
            //   SharedPreferences sharedPref = WorkSummary.this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences sharedPref = SendReport.this.getSharedPreferences(
                    "aas.beetclock", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();


            //Gets name of sender from text editor
            EditText editsender = (EditText) findViewById(R.id.edit_sender);
            String newsender = editsender.getText().toString();
            //If no new address entered, use saved value
            String sender = new String();
            if(newsender.equals("")){
                sender = sharedPref.getString(SAVED_SENDER, "");
            }else{
                sender = newsender;
                editor.putString(SAVED_SENDER, sender);
            }

            //Gets address of recipient(s) from saved prefs or text editor
            EditText editaddress = (EditText) findViewById(R.id.edit_address);
            String newaddress = editaddress.getText().toString();
            String address = new String();
            if(newaddress.equals("")){
                address = sharedPref.getString(SAVED_RECIPIENT, "");
            }else{
                address = newaddress;
                editor.putString(SAVED_RECIPIENT, address);
            }
            editor.commit(); // Gotta remember to commit!


            //Now I'll try sending mail with an attached csv as created in CreateSummary
            String reportName = "/data/user/0/aas.beetclock/files/beetclock_report.csv";
            String recordsName = "/data/user/0/aas.beetclock/files/beetclock_records.csv";
            // String filename = "beetclock_report.csv";
            String[] mailin = {sender, address, fullReport, reportName, recordsName};

            new mailReport().execute(mailin);
        }
    }



    private class mailReport extends AsyncTask<String[], Integer, Boolean> {
        protected Boolean doInBackground(String[]... params) {
            String[] arguments = params[0];
            SendMail sendMail = new SendMail();

            String exceptionMsg = "";
            try {
            sendMail.send(arguments);
        }catch (Exception e) {
            Log.e("SendMail", e.getMessage(), e);
            exceptionMsg = e.getMessage();
            //sentSuccess = false;
        }
            Boolean wasSuccess = false;
            if(exceptionMsg.equals("")){
                wasSuccess = true; }

            return wasSuccess;
        }

        protected void onProgressUpdate(Integer... progress) {
         }

        protected void onPostExecute(Boolean success) {
if (success){
    Toast.makeText(getApplicationContext(), "Report sent successfully", Toast.LENGTH_SHORT).show();
}else{
    Toast.makeText(getApplicationContext(), "Error sending report - please check email address.", Toast.LENGTH_LONG).show();
}
        }//end onPostExecute
    }//endAsyncTask mailReport


    public void openDatePickerReport(View view){
        //Do nothing but open the date picker
        Intent intent = new Intent(this, PickDate.class);
        startActivityForResult(intent, 12345);
    }

    // public String createSummary(View view) {
    public String createSummary() {
        //Summarizes time worked by crop and job

        //Retrieving all records
        SQLiteHelper db = new SQLiteHelper(this);
        //String nullsearch = null; // Must send function a null string in order to return all results
        List<String> cropslist = db.getCrops();
        List<Long> timeslist = db.getTimes();
        List<Long> elapsedlist = db.getElapsed();
        List<String> jobslist = db.getJobs();
        List<String> equiplist = db.getMachine();

        //Retrieve list of crops without duplicates based on hashset
        //String nullsearch = null; // Must send function a null string in order to return all results
        //List<String> allcrops = db.getCropList(nullsearch);
        List<String> allcrops = db.getCrops();
        Set<String> hs = new HashSet<>();
        hs.addAll(allcrops);
        allcrops.clear();
        allcrops.addAll(hs);

        //Retrieve list of jobs without duplicates based on hashset
        List<String> alljobs = db.getJobs();
        hs.clear();
        hs.addAll(alljobs);
        alljobs.clear();
        alljobs.addAll(hs);

        //Retrieve list of equipment without duplicates based on hashset
        List<String> allequip = db.getMachine();
        hs.clear();
        hs.addAll(allequip);
        allequip.clear();
        allequip.addAll(hs);
        //allequip.add("no equip"); //This summarizes work for which no equipment was added

        //Create list of string[] arrays
        List<String[]> summaries = new ArrayList<String[]>();
        List<String[]> csvSummaries = new ArrayList<String[]>();

//Retrieve the date of the last report
        //SharedPreferences sharedPref = WorkSummary.this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences sharedPref = this.getSharedPreferences(
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


//Create header for summaries and CSV summaries tables
        DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        // Codes for re-writing this format available at http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
        String startFormat = "BeetClock report beginning "+formatter.format(startDate);
        String[] startLine = {startFormat};
        summaries.add(startLine);
        csvSummaries.add(startLine);
        String[] spacer = {};
        summaries.add(spacer);
        csvSummaries.add(spacer);
        String[] headers = {"Crop","Job","Equipment","Hours", "Minutes"};
        csvSummaries.add(headers);

        //Bugcheck equip:
        for (int i = 0; i < equiplist.size(); i++){
            System.out.println(equiplist.get(i));
        }

            //Loop through all crops
            for (int i = 0; i < allcrops.size(); i++) {
                //Begin sum for each crop
                long cropsum = 0;
                //Loop through all machinery
                for (int h = 0; h < allequip.size(); h++) {
                    System.out.println(allequip.get(h));

                //Loop through all jobs
                for (int j = 0; j < alljobs.size(); j++) {
                    //--For each crop, job combo, loop through all entries

                    //This is a convoluted method, but going straight from worksum did not work
                    long worksum = 0;
                    for (int k = 0; k < cropslist.size(); k++) {
                        //---if crop and job match combo, sum elapsed time as worksum
                        if (cropslist.get(k).equals(allcrops.get(i)) && jobslist.get(k).equals(alljobs.get(j))
                                && equiplist.get(k).contains(allequip.get(h)) && timeslist.get(k) > startDate) {
                            //worksum.add(elapsedlist.get(i));
                            worksum += Long.valueOf(elapsedlist.get(k));
                        }
                    } //end all entries for
                    //Add worksum to cropsum
                    cropsum = cropsum + worksum;
                    //--if worksum > 0, create string[] array with crop, job, worksum
                    if (worksum > 0) {
                        String hrsFormat = String.valueOf(TimeUnit.MILLISECONDS.toHours(worksum));
                        String hrsSep = "hrs";
                        String minFormat = String.valueOf(TimeUnit.MILLISECONDS.toMinutes(worksum) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(worksum)));
                        String minSep = "min";
                        //String secFormat = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(worksum) -
                        //        (TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(worksum))));
                        //String secSep = "sec";
                        String[] csvsummary = {allcrops.get(i), alljobs.get(j), allequip.get(h), hrsFormat, minFormat};
                        String[] summary = {allcrops.get(i), alljobs.get(j), allequip.get(h), hrsFormat, hrsSep, minFormat, minSep}; //String.valueOf(worksum)
                        summaries.add(summary);
                        csvSummaries.add(csvsummary);
                    }
                } // end all jobs for
                }//end all equip for
                //Writing crop summaries
                String hrsFormat = String.valueOf(TimeUnit.MILLISECONDS.toHours(cropsum));
                String hrsSep = " hrs ";
                String minFormat = String.valueOf(TimeUnit.MILLISECONDS.toMinutes(cropsum) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(cropsum)));
                String minSep = " min";

                //Add summary line to text output
                summaries.add(spacer);
                String[] cropTot = {"Total for "+allcrops.get(i)+" "+hrsFormat+hrsSep+minFormat+minSep};
                summaries.add(cropTot);
                summaries.add(spacer);

                //Add summary line to CSV
                String[] csvTot = {allcrops.get(i),"total:","",hrsFormat,minFormat};
                csvSummaries.add(csvTot);
                csvSummaries.add(spacer);
            } // end all crops for



        //I will write the summary to a .csv file using openCSV

        File localDir = new File(this.getFilesDir(), "");
        file = new File(localDir, "beetclock_report.csv");
        System.out.println(file.getAbsolutePath());

        try {
            CSVWriter writer;
            file.createNewFile();
            writer = new CSVWriter(new FileWriter(file));
            //CSVWriter writer = new CSVWriter(new FileWriter(filename));
            writer.writeAll(csvSummaries);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


        //NOW I can create a string to print using the for loop below on the list of string[] arrays
        StringBuilder liststring = new StringBuilder();
        String sep = " ";
        for (int i = 0; i < summaries.size(); i++) {
            for (int j = 0; j < summaries.get(i).length; j++){
                //I can add both the crop and job
                liststring.append(sep).append(summaries.get(i)[j]);

            } // end for array length
            liststring.append(System.getProperty("line.separator"));

        } // end list for

        //Return the summary
        String display = liststring.toString();
        return display;


    }// end generate work summary

    public String createDump() {
        //Returns all saved records

        //Retrieving all records
        SQLiteHelper db = new SQLiteHelper(this);
        //String nullsearch = null; // Must send function a null string in order to return all results
        List<String> cropslist = db.getCrops();
        List<Long> timeslist = db.getTimes();
        List<Long> elapsedlist = db.getElapsed();
        List<String> jobslist = db.getJobs();
        List<String> equiplist = db.getMachine();

        List<Long> timeSort = sortListWithoutModifyingOriginalList(timeslist);

        //Retrieve list of ALL crops
        String nullsearch = null; // Must send function a null string in order to return all results
        List<String> allcrops = db.getCropList(nullsearch);

        //Write list of jobs
        List<String> alljobs = db.getJobList();
        String[] jobsarray = new String[alljobs.size()];

        //List of all machines
        List<String> allequip = db.getMachineList(nullsearch);

        //Create list of string[] arrays for print summary and csv summary
        List<String[]> summaries = new ArrayList<String[]>();
        List<String[]> csvSummaries = new ArrayList<String[]>();

//Retrieve the date of the last report
        //SharedPreferences sharedPref = WorkSummary.this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences sharedPref = this.getSharedPreferences(
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



//Create header for summary and csv summary tables:
        DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        // Codes for re-writing this format available at http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
        String startFormat = "BeetClock records since "+formatter.format(startDate);
        String[] startLine = {startFormat};
        summaries.add(startLine);
        csvSummaries.add(startLine);
        String[] spacer = {};
        summaries.add(spacer);
        csvSummaries.add(spacer);
        String[] header = {"Crop","Job","Equipment","Hours","Minutes","Date"};
        csvSummaries.add(header);



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
                    String[] csvsummary = {cropslist.get(j), jobslist.get(j), equiplist.get(j), hrsFormat, minFormat, dateFormat};
                    String[] summary = {cropslist.get(j), jobslist.get(j), equiplist.get(j), hrsFormat, hrsSep, minFormat, minSep, dateFormat}; //String.valueOf(worksum)
                    summaries.add(summary);
                    csvSummaries.add(csvsummary);
                                    }
            }//end j for
        }//end i for

        //File localDir = new File(this.getFilesDir(), "");
        file = new File(this.getFilesDir(), "beetclock_records.csv");
        // System.out.println(file.getAbsolutePath());

        try {
            CSVWriter writer;
            file.createNewFile();
            writer = new CSVWriter(new FileWriter(file));
            //CSVWriter writer = new CSVWriter(new FileWriter(filename));
            writer.writeAll(csvSummaries);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


        //NOW I can create a string to print using the for loop below on the list of string[] arrays
        StringBuilder liststring = new StringBuilder();
        String sep = " ";
        for (int i = 0; i < summaries.size(); i++) {
            for (int j = 0; j < summaries.get(i).length; j++){
                //I can add both the crop and job
                liststring.append(sep).append(summaries.get(i)[j]);
            } // end for array length
            liststring.append(System.getProperty("line.separator"));

        } // end list for

        //Return the summary
        String display = liststring.toString();
        return display;


    }// end full work dump

    private <E extends Comparable<E>> List<E> sortListWithoutModifyingOriginalList(List<E> list){
        List<E> newList = new ArrayList<E>(list);
        Collections.sort(newList);
        return newList;
    }












}
