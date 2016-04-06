package aas.beetclock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

import com.opencsv.CSVWriter;

public class PopulateSheet extends AppCompatActivity {

    public final static String KEY_PARAMS = "aas.beetclock.sheetParams";
    public final static String KEY_JOBS = "aas.beetclock.sheetJobs";
    public final static String KEY_TIMES = "aas.beetclock.sheetTimes";
    public final static String KEY_EQUIP = "aas.beetclock.sheetEquipment";

    public final static int FILE_CODE = 12345;
    public final static int SHEET_CODE = 54321;
    public final static int DATE_CODE = 61983;
    public final static int POP_CODE = 42113;

    public final static String REPORT_DATE = "aas.beetclock.Report_Date";
    public final static String FILE_ID = "aas.beetclock.file_id";
    public final static String FILE_NAME = "aas.beetclock.file_name";

    public String[] files;
    public String[] sheets;
    public String[] ids;
    public String reportDate = "0";
    public String savedName = "";
    public String savedId = "";
    public int listPos;


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
        setContentView(R.layout.activity_populate_sheet);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Fill NOFA Spreadsheet");

        //Do on execute:
        //Populate Spinners
        new onLoad().execute("");


            }// end oncreate

    public void onBackPressed() {
        finish();
    }

    private class onLoad extends AsyncTask<String, Integer, List<String[]>> {
        protected List<String[]> doInBackground(String... param) {



            SharedPreferences sharedPref = PopulateSheet.this.getSharedPreferences(
                    "aas.beetclock", Context.MODE_PRIVATE);


            //Get a non-duplicated array of crops from timer entries
            SQLiteHelper db = new SQLiteHelper(PopulateSheet.this);

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
            String[] cropArray = new String[croplist.size()];
            cropArray = croplist.toArray(cropArray);

            List<String[]> allSpinners = new ArrayList<>();
            allSpinners.add(cropArray);

            //Also getting reportDate from shared prefs

            //reportDate = sharedPref.getString(REPORT_DATE, "0");
            //savedName = sharedPref.getString(FILE_NAME, "");
            //savedId = sharedPref.getString(FILE_ID, "");

            return allSpinners;
        }//end doInBackground

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(List<String[]> allSpinners) {
            String[] cropArray = allSpinners.get(0);

            //Populate crops spinner
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                    PopulateSheet.this, R.layout.spinnertext, cropArray); //android.R.layout.simple_spinner_item
            spinnerArrayAdapter.setDropDownViewResource(R.layout.spinnertext); //android.R.layout.simple_spinner_item

            Spinner crop_spinner = (Spinner)findViewById(R.id.crops_spinner);
            crop_spinner.setAdapter(spinnerArrayAdapter);

            //Set a start date for the summary
            String dateSince = new String();

            /* No longer retrieves based on previously saved report dates
            if (reportDate.equals("")) {
                //If no date saved, stay at zero
                dateSince = "Retrieve all records since you started using BeetClock, OR ";
            } else {
                //If there is a date saved, start there
                Date date = new Date(Long.valueOf(reportDate));
                DateFormat formatter = new SimpleDateFormat("dd:MMM:yyyy");
                dateSince = "Retrieve records since " + formatter.format(date) + " OR";
            }//end selected date else
            */
            //Set text
            TextView msgView = (TextView) findViewById(R.id.date_text);
            msgView.setTextSize(16);
            dateSince = "Retrieve all records since you started using BeetClock, OR ";
            msgView.setText(dateSince);

            //Get all files on drive if no file is preselected

            if(savedId.equals("")) {
                Intent intent = new Intent(PopulateSheet.this, DoScriptExecute.class);
                //I will need to use two blank arrays to fill in for the jobs and values
                String[] blank = {};
                String[] params = {"getFiles"};
                intent.putExtra(KEY_PARAMS, params);
                intent.putExtra(KEY_JOBS, blank);
                intent.putExtra(KEY_EQUIP, blank);
                intent.putExtra(KEY_TIMES, blank);
                startActivityForResult(intent, FILE_CODE);

                //Set file text
                TextView textView = (TextView) findViewById(R.id.file_text);
                textView.setTextSize(16);
                textView.setText("Choose file in Google Drive:");
            } else {
                //Get file names
                Intent intent = new Intent(PopulateSheet.this, DoScriptExecute.class);
                //I will need to use two blank arrays to fill in for the jobs and values
                String[] blank = {};
                String[] params = {"getFiles"};
                intent.putExtra(KEY_PARAMS, params);
                intent.putExtra(KEY_JOBS, blank);
                intent.putExtra(KEY_EQUIP, blank);
                intent.putExtra(KEY_TIMES, blank);
                startActivityForResult(intent, FILE_CODE);
                //Get spreadsheets
                Intent intent2 = new Intent(PopulateSheet.this, DoScriptExecute.class);
                String[] params2 = {"getSheets", savedId};
                intent2.putExtra(KEY_PARAMS, params2);
                intent2.putExtra(KEY_JOBS, blank);
                intent2.putExtra(KEY_EQUIP, blank);
                intent2.putExtra(KEY_TIMES, blank);
                startActivityForResult(intent2, SHEET_CODE);

                //Set text
                TextView textView = (TextView) findViewById(R.id.file_text);
                textView.setTextSize(16);
                textView.setText("Choose file in Google Drive:");
            }// end saveid if



        }// end onPostExecute
    }// end AsyncTask populateSpinners



    public void onActivityResult(int requestCode, int resultCode, Intent data) {  // When result of doScriptExecute is returned
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) { //Do this if returning a filenames response
            files = data.getStringArrayExtra(DoScriptExecute.KEY_RESPONSE);
            ids = data.getStringArrayExtra(DoScriptExecute.KEY_IDS);
/*
            //Alphabetize files in spinner
            List<String> fileList = Arrays.asList(files);
            java.util.Collections.sort(fileList, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareToIgnoreCase(o2);
                }
            }); // Alphebetizes while ignoring case
            String[] fileAlpha = fileList.toArray(new String[0]);
*/
            //Now I will go ahead and populate the filenames spinner
            ArrayAdapter<String> filesArrayAdapter = new ArrayAdapter<String>(
                    this, R.layout.spinnertext, files);
            filesArrayAdapter.setDropDownViewResource(R.layout.spinnertext);
            Spinner files_spinner = (Spinner) findViewById(R.id.files_spinner);
            files_spinner.setAdapter(filesArrayAdapter);
//End file result

            files_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    //Get spreadsheets
                    //!!! Must get the ID of the selected file.  Should also RETURN the IDs of the sheets
                    Spinner fileSpin = (Spinner) findViewById(R.id.files_spinner);
                    position = fileSpin.getSelectedItemPosition();
                    String fileName = fileSpin.getSelectedItem().toString();
                    String fileId = ids[position];
                    //Save name and Id of selected
                    savedName = fileName;
                    savedId = fileId;

                    String[] fileInfo = {fileName, fileId};

                    new doGet().execute(fileInfo);
                }
                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });



        } else if (requestCode == SHEET_CODE && resultCode == Activity.RESULT_OK) {
            sheets = data.getStringArrayExtra(DoScriptExecute.KEY_RESPONSE);
            //Now I will go ahead and populate the filenames spinner

            /*//Alphabetize sheets in spinner
            List<String> sheetList = Arrays.asList(sheets);
            java.util.Collections.sort(sheetList, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareToIgnoreCase(o2);
                }
            }); // Alphebetizes while ignoring case
            String[] sheetAlpha = sheetList.toArray(new String[0]);
*/
            ArrayAdapter<String> sheetsArrayAdapter = new ArrayAdapter<String>(
                    this, R.layout.spinnertext, sheets);
            sheetsArrayAdapter.setDropDownViewResource(R.layout.spinnertext);
            Spinner sheets_spinner = (Spinner) findViewById(R.id.sheets_spinner);
            sheets_spinner.setAdapter(sheetsArrayAdapter);

//End sheet result
        } else if (requestCode == DATE_CODE && resultCode == Activity.RESULT_OK) {
//get data from date selector
                reportDate = data.getStringExtra("selection");
                //Make the selected date public

            SharedPreferences sharedPref = PopulateSheet.this.getSharedPreferences(
                    "aas.beetclock", Context.MODE_PRIVATE);

            //If report date has just been selected, store this to Saved Preferences
            if(!reportDate.equals("") && !reportDate.isEmpty() && !reportDate.equals(null)) {
                //SharedPreferences.Editor editor = sharedPref.edit();
                //editor.putString(REPORT_DATE, reportDate);
                //editor.commit();

                Date date = new Date(Long.valueOf(reportDate));
                DateFormat formatter = new SimpleDateFormat("dd:MMM:yyyy");
                String dateSince = "Retrieve records since " + formatter.format(date);
            TextView msgView = (TextView) findViewById(R.id.date_text);
            msgView.setTextSize(16);
            msgView.setText(dateSince);
                            }
//End date result
    }  else if (requestCode == POP_CODE && resultCode == Activity.RESULT_OK) {
            Toast.makeText(getApplicationContext(), "Values written to sheet", Toast.LENGTH_SHORT).show();
        }//End populate result

        }//end onActivityResult
/*
    public void getSheets (View view) {

//!!! Must get the ID of the selected file.  Should also RETURN the IDs of the sheets
        Spinner fileSpin = (Spinner) findViewById(R.id.files_spinner);
        int position = fileSpin.getSelectedItemPosition();
        String fileName = fileSpin.getSelectedItem().toString();
        String fileId = ids[position];
        //Save name and Id of selected
        savedName = fileName;
        savedId = fileId;

        String[] fileInfo = {fileName, fileId};

        new doGet().execute(fileInfo);

    }
*/
    private class doGet extends AsyncTask<String[], Integer, String> {
        protected String doInBackground(String[]... params) {
//Save file ID and file name to shared preferences
            String[] parameters = params[0];
            String fileName = parameters[0];
            String fileId = parameters[1];

            SharedPreferences sharedPref = PopulateSheet.this.getSharedPreferences(
                    "aas.beetclock", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(FILE_ID, fileId);
            editor.putString(FILE_NAME, fileName);
            editor.commit();

            return fileId;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String fileId) {
            //Get sheets wiht params array and two blank arrays
            Intent intent = new Intent(PopulateSheet.this, DoScriptExecute.class);
            //The script reqires a List<String> with two entries: URL, Value
            //List<String> params = new ArrayList<String>();
            //params.add("https://docs.google.com/spreadsheets/d/1o1kjZSo8iioMVpRffHKt1wFcUl31k_mILrGTdEUO7Ko/");
            //params.add("Edit!");
            String[] params = {"getSheets", fileId};
            String[] blank = {};
            intent.putExtra(KEY_PARAMS, params);
            intent.putExtra(KEY_JOBS, blank);
            intent.putExtra(KEY_EQUIP, blank);
            intent.putExtra(KEY_TIMES, blank);
            startActivityForResult(intent, SHEET_CODE);
        }
    }


    public void popSheet (View view) {

        //If no file Id is chosen or non availabe on spinners, notify! Else populate sheet
        if(savedId.equals("") ) { //If no file is saved, get ID from the spinner
            Toast.makeText(getApplicationContext(), "Select a file and sheet to populate", Toast.LENGTH_LONG).show();
        } else {

            //Getting the names of the selected sheet and selected crop
            Spinner sheetSpin = (Spinner) findViewById(R.id.sheets_spinner);
            String sheetName = sheetSpin.getSelectedItem().toString();

            Spinner cropSpin = (Spinner) findViewById(R.id.crops_spinner);
            String cropName = cropSpin.getSelectedItem().toString();

            String[] population = {sheetName, cropName};

            new doPopulate().execute(population);
        }

    }

    private class doPopulate extends AsyncTask<String[], Integer, List<String[]>> {
        protected List<String[]> doInBackground(String[]... params) {

            String[] parameters = params[0];
            String sheetName = parameters[0];
            String cropName = parameters[1];

            //Summarizing the selected crop
            List<String[]> summary = makeSum(cropName);
            String[] jobs = summary.get(0);
            String[] equipment = summary.get(1);
            String[] times = summary.get(2);

            List<String[]> toPopulate = new ArrayList<>();
            toPopulate.add(parameters);
            toPopulate.add(jobs);
            toPopulate.add(equipment);
            toPopulate.add(times);

            return toPopulate;
           }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(List<String[]> toPopulate) {

            String[] parameters = toPopulate.get(0);
            String[] jobs = toPopulate.get(1);
            String[] equipment = toPopulate.get(2);
            String[] times = toPopulate.get(3);

            //I now need to convert the AsyncTask parameters to the params needed for doScriptExecute
            String[] params = {"popSheet", savedId, parameters[0]};

            Intent intent = new Intent(PopulateSheet.this, DoScriptExecute.class);
            intent.putExtra(KEY_PARAMS, params);
            intent.putExtra(KEY_JOBS, jobs);
            intent.putExtra(KEY_EQUIP, equipment);
            intent.putExtra(KEY_TIMES, times);
            startActivityForResult(intent, POP_CODE);
        }
    }


    public void openDatePicker(View view){
        //Do nothing but open the date picker
        Intent intent = new Intent(this, PickDate.class);
        startActivityForResult(intent, DATE_CODE);
    }

    public List<String[]> makeSum (String crop){

        List<String[]> outputs = new ArrayList<>();

        //These lists will be populated as we iterate through the results
        List<String> sumJobs = new ArrayList<>();
        List<String> sumEquip = new ArrayList<>();
        List<String> sumTimes = new ArrayList<>();


            //Retrieving all records
            SQLiteHelper db = new SQLiteHelper(this);
            //String nullsearch = null; // Must send function a null string in order to return all results
            List<String> cropslist = db.getCrops();
            List<Long> timeslist = db.getTimes();
            List<Long> elapsedlist = db.getElapsed();
            List<String> jobslist = db.getJobs();
            List<String> equiplist = db.getMachine();

            //Write non-duplicated list of equipment using hashset
            List<String> allequip = db.getMachine();
            Set<String> hs = new HashSet<>();
        hs.addAll(allequip);
        allequip.clear();
        allequip.addAll(hs);
        //allequip.add("no equip"); //add no equip option

        //Jobs that are included in spreadsheet
        String[] ssJobs = {"Soil prep: Disk","Soil prep: Chisel","Soil prep: Rototill","Soil prep: Bedform","Soil prep: Spread Fertilizer","Soil prep: Spread manure/compost","Soil prep: Lay plastic/drip",
                "Seeding/Transplant: Seed in field","Seeding/Transplant: Transplant",
                "Cultivation: Cover/uncover","Cultivation: Hoe","Cultivation: Handweed","Cultivation: Straw mulch","Cultivation: Irrigate","Cultivation: Tractor cultivate","Cultivation: Sidedress","Cultivation: Spray","Cultivation: Flame weed",
                "Harvest: Harvest", "Harvest: Wash/pack",
                "Post-harvest: Mow crop","Post-harvest: Remove mulch","Post-harvest: Disk","Post-harvest: Sow cover crop"
        };

//Retrieve the date of the last report
            //SharedPreferences sharedPref = WorkSummary.this.getPreferences(Context.MODE_PRIVATE);
            //SharedPreferences sharedPref = this.getSharedPreferences(
            //        "aas.beetclock", Context.MODE_PRIVATE);
            //SharedPreferences.Editor editor = sharedPref.edit();
            //String reportDate = sharedPref.getString(REPORT_DATE, "");

//Set a start date for the summary
            long startDate = 0;

            if (reportDate.equals("") || reportDate.equals(null) || reportDate.isEmpty()) {
                //If no date saved, stay at zero
                } else {
                    //If there is a date saved, start there
                    startDate = Long.parseLong(reportDate);
            }//end selected date else


        //For the purposes of this summary, I will want to know how many jobs on the list fall into each category.
        //I will then subtract this from the total in each category to get other in each category
        long soilSum = 0;
        long cultSum = 0;
        long postSum = 0;
        //Here are the totals
        long soilTot = 0;
        long cultTot = 0;
        long postTot = 0;

            //Loop through all jobs
            for (int j = 0; j < ssJobs.length; j++) {
                //--For each crop, job combo, loop through all entries
                //concatinate all equipment used
                String equip = "";
                //summarize all worktime
                long worksum = 0;

                //Loop through all equipment

                for (int k = 0; k < cropslist.size(); k++) {
                    //---if crop and job match combo, sum elapsed time as worksum
                    if (cropslist.get(k).equals(crop) && jobslist.get(k).contains(ssJobs[j])
                            && timeslist.get(k) > startDate) {
                        //worksum.add(elapsedlist.get(i));
                        worksum += Long.valueOf(elapsedlist.get(k));
                        for (int i = 0; i < allequip.size(); i++) {
                            if(!equip.contains(allequip.get(i)) && equiplist.get(k).contains(allequip.get(i))) {
                                equip = equip + equiplist.get(k);
                            }
                        }// end all equipment for
                    }
                } //end all entries for

                //--if worksum > 0, add job and summary to array
//We want zeros for all jobs w/o an entry
                String hours = String.valueOf(worksum);
                //Adding job and time to lists
                String thisJob = ssJobs[j];
                sumTimes.add(hours);
                sumJobs.add(thisJob);
                if (equip.equals("")){
                 sumEquip.add("no equip");
                }else {
                    sumEquip.add(equip);
                }
                if (thisJob.contains("Soil prep")) {
                    soilSum += worksum;
                } else if (thisJob.contains("Cultivation")) {
                    cultSum += worksum;
                } else if (thisJob.contains("Post-harvest")) {
                    postSum += worksum;
                }
            } // end all jobs for


        //Now I will get the TOTAL worktime in each category
        for (int m = 0; m < cropslist.size(); m++) {
            if (cropslist.get(m).equals(crop) && timeslist.get(m) > startDate) {
                String thisJob = jobslist.get(m);
                if (thisJob.contains("Soil prep")){
                    soilTot += Long.valueOf(elapsedlist.get(m));
                }  else if(thisJob.contains("Cultivation")){
                    cultTot += Long.valueOf(elapsedlist.get(m));
                }  else if(thisJob.contains("Post-harvest")){
                    postTot += Long.valueOf(elapsedlist.get(m));
                }
            }
        } //end total worktime for

        //Finally I will add the other entries for each category
        //double decSoil = (soilTot - soilSum)/ 360000;
        long decSoil = (soilTot - soilSum);
       // if(decSoil > 0){
        //sumTimes.add(String.format("%.5g%n", decSoil));
        sumTimes.add(String.valueOf(decSoil));
        sumJobs.add("Soil prep: Other");
        sumEquip.add("no equip");//}
        long decCult = (cultTot - cultSum);
        sumTimes.add(String.valueOf(decCult));
        sumJobs.add("Cultivation: Other");
        sumEquip.add("no equip");
        long decPost = (postTot - postSum);
        sumTimes.add(String.valueOf(decPost));
        sumJobs.add("Post-harvest: Other");
        sumEquip.add("no equip");

        String[] timesArray = sumTimes.toArray(new String[0]);
            String[] equipArray = sumEquip.toArray(new String[0]);
        String[] jobsArray = sumJobs.toArray(new String[0]);

        //Test this summary: print all job equip time couples
            for (int i = 0; i < jobsArray.length; i++) {
                System.out.println(jobsArray[i]);
                System.out.println(equipArray[i]);
                System.out.println(timesArray[i]);
            }
        outputs.add(jobsArray);
            outputs.add(equipArray);
        outputs.add(timesArray);

            return outputs;
    }//end makeSum


}
