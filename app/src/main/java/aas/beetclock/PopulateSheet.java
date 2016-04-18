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
    //Equip carries tractors, implem carries tractor implements
    public final static String KEY_EQUIP = "aas.beetclock.sheetEquipment";
    public final static String KEY_IMPLEM = "aas.beetclock.sheetImplements";

    public final static int FILE_CODE = 12345;
    public final static int SHEET_CODE = 54321;
    public final static int DATE_CODE = 61983;
    public final static int POP_CODE = 42113;
    public final static int EQUIP_CODE = 15672;

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
    //public List<String> sheetEquipment;


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
        getSupportActionBar().setTitle("Fill NOFA Workbook");

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
            dateSince = "Write all records since you started using BeetClock, OR ";
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

                    if (fileSpin.getSelectedItem() != null) {
                        position = fileSpin.getSelectedItemPosition();

                        String fileName = fileSpin.getSelectedItem().toString();
                        String fileId = ids[position];
                        //Save name and Id of selected
                        savedName = fileName;
                        savedId = fileId;

                        String[] fileInfo = {fileName, fileId};

                        new doGet().execute(fileInfo);
                    }//end if file not null
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
                String dateSince = "Write records since " + formatter.format(date);
            TextView msgView = (TextView) findViewById(R.id.date_text);
            msgView.setText(dateSince);
                            }
//End date result
    }  else if (requestCode == POP_CODE && resultCode == Activity.RESULT_OK) {
            Toast.makeText(getApplicationContext(), "Values written to workbook", Toast.LENGTH_SHORT).show();
        }//End populate result

        else if (requestCode == EQUIP_CODE && resultCode == Activity.RESULT_OK) {
//First populating list of equipment from NOFA sheet
            String[] equips = data.getStringArrayExtra(DoScriptExecute.KEY_RESPONSE);

            List<String> sheetEquipment = new ArrayList<>();
            for (int i = 0; i < equips.length; i++) {
                //Add only array elements that contain data.
                //Tractors comprise the first four entries; preface them with 'Tractor:', and the rest with 'Implement:'
                if (!equips[i].equals("") && !equips[i].isEmpty() && !equips[i].equals(null) && i < 4) {
                    sheetEquipment.add("Tractor: " + equips[i]);
                } else if (!equips[i].equals("") && !equips[i].isEmpty() &&! equips[i].equals(null)){
                    sheetEquipment.add("Implement: " + equips[i]);
                }
            } // end equips for
            String[] sheetEquip = sheetEquipment.toArray(new String[0]);

//Then running doPopulate
            //Getting the names of the selected sheet and selected crop
            Spinner sheetSpin = (Spinner) findViewById(R.id.sheets_spinner);

            Spinner cropSpin = (Spinner) findViewById(R.id.crops_spinner);

            if(sheetSpin.getSelectedItem() != null && cropSpin.getSelectedItem() != null) {
                String sheetName = sheetSpin.getSelectedItem().toString();
                String cropName = cropSpin.getSelectedItem().toString();
                String[] population = {sheetName, cropName};

                List<String[]> outputs = new ArrayList<>();
                outputs.add(population);
                outputs.add(sheetEquip);

                new doPopulate().execute(outputs);
            }
            //new doWrite().execute(equips);

        }//End get equip result

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
            //Get equipment if a sheet ID is saved / exists
            String[] params = {"getEquip", savedId};
            String[] blank = {};
            Intent intent = new Intent(PopulateSheet.this, DoScriptExecute.class);
            intent.putExtra(KEY_PARAMS, params);
            intent.putExtra(KEY_JOBS, blank);
            intent.putExtra(KEY_EQUIP, blank);
            intent.putExtra(KEY_TIMES, blank);
            startActivityForResult(intent, EQUIP_CODE);

        }
    }


    private class doPopulate extends AsyncTask<List<String[]>, Integer, List<String[]>> {
        protected List<String[]> doInBackground(List<String[]>... params) {

            List<String[]> outputs = params[0];
            String[] parameters = outputs.get(0);

            //Summarizing the selected crop
            List<String[]> summary = makeSum(outputs);
            String[] jobs = summary.get(0);
            String[] equipment = summary.get(1);
            String[] times = summary.get(2);
            String[] implem = summary.get(3);
            String[] errors = summary.get(4);

            List<String[]> toPopulate = new ArrayList<>();
            toPopulate.add(parameters);
            toPopulate.add(jobs);
            toPopulate.add(equipment);
            toPopulate.add(times);
            toPopulate.add(implem);
            toPopulate.add(errors);

            return toPopulate;
           }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(List<String[]> toPopulate) {

            String[] parameters = toPopulate.get(0);
            String[] jobs = toPopulate.get(1);
            String[] tractors = toPopulate.get(2);
            String[] times = toPopulate.get(3);
            String[] implem = toPopulate.get(4);
            String[] errors = toPopulate.get(5);

            if(!errors[0].equals("")){
                Toast.makeText(getApplicationContext(), errors[0], Toast.LENGTH_LONG).show();
            }
            if(!errors[1].equals("")){
                Toast.makeText(getApplicationContext(), errors[1], Toast.LENGTH_LONG).show();
            }


            //I now need to convert the AsyncTask parameters to the params needed for doScriptExecute
            String[] params = {"popSheet", savedId, parameters[0]};

            Intent intent = new Intent(PopulateSheet.this, DoScriptExecute.class);
            intent.putExtra(KEY_PARAMS, params);
            intent.putExtra(KEY_JOBS, jobs);
            intent.putExtra(KEY_EQUIP, tractors);
            intent.putExtra(KEY_TIMES, times);
            intent.putExtra(KEY_IMPLEM, implem);
            startActivityForResult(intent, POP_CODE);
        }
    }


    public void openDatePicker(View view){
        //Do nothing but open the date picker
        Intent intent = new Intent(this, PickDate.class);
        startActivityForResult(intent, DATE_CODE);
    }

    public List<String[]> makeSum (List<String[]> params){

        String crop = params.get(0)[1];
        String[] sheetEquipment = params.get(1);

        List<String[]> outputs = new ArrayList<>();

        //These lists will be populated as we iterate through the results
        List<String> sumJobs = new ArrayList<>();
        List<String> sumTract = new ArrayList<>();
        List<String> sumImplem = new ArrayList<>();
        List<String> sumTimes = new ArrayList<>();


            //Retrieving all records
            SQLiteHelper db = new SQLiteHelper(this);
            //String nullsearch = null; // Must send function a null string in order to return all results
        List<String> cropslist = db.getCrops();
        List<Long> timeslist = db.getTimes();
        List<Long> elapsedlist = db.getElapsed();
        List<String> jobslist = db.getJobs();
        List<String> equiplist = db.getMachine();

//Compare equipment in jobs to the list of equipment from sheet.  Remove others and generate warning popup

        //At the same time, write all matching equipment entries to a hashset
        Set<String> equipSet = new HashSet<>();

        int countMismatch = 0;

        for (int i = 0; i < equiplist.size(); i++) {
            int occurrances = 0;
            String[] equips = equiplist.get(i).split(",");
            for (int j = 0; j < equips.length; j++) {
                for (int k = 0; k < sheetEquipment.length; k++) {
                    if (equips[j].contains(sheetEquipment[k])) {
                        occurrances++;
                    }//
                } //equipSaved for
              }//equips for
            if ((occurrances < equips.length) && !equips[0].equals("") && !equips[0].equals(" ") && !equips[0].equals("no equip") && !equips[0].isEmpty()) {
                countMismatch++;
            } else {
                //If all pieces of equipment match saved pieces, add the entry to the set
                equipSet.add(equiplist.get(i));
            }
        }//equipList for

        //Creates a list of errors that will be passed to postExecute
List<String> errorList = new ArrayList<>();
        if (countMismatch > 0){
            String misString = Integer.toString(countMismatch);
            errorList.add("Some records contain equipment that is not in the workbook.  This equipment has not been written to the crop budget.");
        } else {errorList.add("");}

            //Write equipment hashset to a list for further work
            List<String> allequip = new ArrayList<>();
             allequip.addAll(equipSet);


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

        //Record if any jobs have multiple implements
        int multiImp = 0;
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
                            //If the 'equip' string starts as blank, simply add the entry.  Otherwise add a comma spacer
                            if(!equip.contains(allequip.get(i)) && equiplist.get(k).contains(allequip.get(i)) && equip.equals("")) {
                                equip = equip + equiplist.get(k);
                            } else if (!equip.contains(allequip.get(i)) && equiplist.get(k).contains(allequip.get(i))){
                                equip = equip + ", "+equiplist.get(k);
                            }
                        }// end all equipment for
                    }
                } //end all entries for

                //--if worksum > 0, add job and summary to array
//Add number as string if greater than zero; else add empty string
                String hours = "";
                if(worksum>0) {
                    hours = String.valueOf(worksum);
                }
                //Adding job time and equipment to lists
                String thisJob = ssJobs[j];
                sumTimes.add(hours);
                sumJobs.add(thisJob);

                String tractStr = "";
                String implemStr = "";
                if (!equip.equals("")){
                    // Split each equipment entry into indiv pieces of equipment
                String[] equipEntries = equip.split(",");
                    //Count tractors and implements per job; generate warning if > 1
                    int tractCount = 0;
                    int implemCount = 0;


                    for(int i = 0; i< equipEntries.length; i++){

                        String piece = equipEntries[i].trim();
                        String[] pieces = piece.split(":");
                        if(pieces[0].contains("Tractor")){
                            tractStr = (pieces[1]);
                            tractCount ++;
                        }else if (pieces[0].contains("Implement")){
                            implemStr = (pieces[1]);
                            implemCount ++;
                        }
                    }

                    if(tractCount > 1 || implemCount > 1){
                    multiImp ++;
                    }
//Make a comma separated list of tractors for each job
                    /*
                    StringBuilder tractBuilder = new StringBuilder();
                    String sep = ", ";
                    for(int i = 0; i < tractorsJob.size(); i++){
                        if(i < (tractorsJob.size()-1) && !tractorsJob.get(i).equals("")){
                        tractBuilder.append(tractorsJob.get(i));
                        tractBuilder.append(sep);
                        } else{
                            tractBuilder.append(tractorsJob.get(i));
                        }
                    }
                    */

/*
                    // Make a comma separated list of implements for each job
                    StringBuilder implemBuilder = new StringBuilder();
                    for(int i = 0; i < implementsJob.size(); i++){
                        if(i < (tractorsJob.size()-1) && !tractorsJob.get(i).equals("")){
                            implemBuilder.append(implementsJob.get(i));
                            implemBuilder.append(sep);
                        } else{
                            implemBuilder.append(implementsJob.get(i));
                        }
                    }
                    String implemStr = implemBuilder.toString().trim();
*/

                } //end equip else
                //Add tractor and implement for current job (no leading or trailing spaces
                sumTract.add(tractStr.trim());
                sumImplem.add(implemStr.trim());

                if (thisJob.contains("Soil prep")) {
                    soilSum += worksum;
                } else if (thisJob.contains("Cultivation")) {
                    cultSum += worksum;
                } else if (thisJob.contains("Post-harvest")) {
                    postSum += worksum;
                }
            } // end all jobs for
        if(multiImp>0){
            errorList.add("Only the most recent tractor and implement used for each job have been written to the crop budget.");
        }else{errorList.add("");}


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
        //Add number if greater than zero, else blank
        if(decSoil>0) {
            sumTimes.add(String.valueOf(decSoil));
        }else{
            sumTimes.add("");
        }
        sumJobs.add("Soil prep: Other");
        sumTract.add("");
        sumImplem.add("");
        long decCult = (cultTot - cultSum);
        if(decCult>0) {
            sumTimes.add(String.valueOf(decCult));
        }else{
            sumTimes.add("");
        }
        sumJobs.add("Cultivation: Other");
        sumTract.add("");
        sumImplem.add("");
        long decPost = (postTot - postSum);
        if(decPost>0) {
            sumTimes.add(String.valueOf(decPost));
        }else{
            sumTimes.add("");
        }
        sumJobs.add("Post-harvest: Other");
        sumTract.add("");
        sumImplem.add("");

        String[] timesArray = sumTimes.toArray(new String[0]);
        String[] tractArray = sumTract.toArray(new String[0]);
        String[] implemArray = sumImplem.toArray(new String[0]);
        String[] jobsArray = sumJobs.toArray(new String[0]);
        String[] errors = errorList.toArray(new String[0]);

        outputs.add(jobsArray);
            outputs.add(tractArray);
                outputs.add(timesArray);
        outputs.add(implemArray);
        outputs.add(errors);

            return outputs;
    }//end makeSum


}
