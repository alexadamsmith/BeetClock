package aas.beetclock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ManageRecords extends AppCompatActivity {

    File file = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_records);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        new populateSpinners().execute("");

        //Enables Strict Mode testing
        /*
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                        //.penaltyDialog()
                .penaltyFlashScreen()
                .build());
                */
    }

    private class populateSpinners extends AsyncTask<String, Integer, List<String[]>> {
        protected List<String[]> doInBackground(String... param) {

            //Get a list of all files in the Downloads
            List<String> recordsFiles = new ArrayList<>();

            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            for (File f : directory.listFiles()) {
                if (f.isFile()) {
                    String name = f.getName();
                    if (name.contains("BeetClockRecords")){
                        recordsFiles.add(name);
                    }
                }
            }//end for file

            //Convert to array and send to populate spinner on postexecute
            String[] recordsArray = new String[recordsFiles.size()];
            recordsArray = recordsFiles.toArray(recordsArray);

            List<String[]> allSpinners = new ArrayList<>();
            allSpinners.add(recordsArray);

            return allSpinners;
        }//end doInBackground

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(List<String[]> allSpinners) {
            String[] spinmachine = allSpinners.get(0);

            //Populate machine spinner
            ArrayAdapter<String> machineArrayAdapter = new ArrayAdapter<String>(
                    ManageRecords.this, R.layout.spinnertext, spinmachine);
            machineArrayAdapter.setDropDownViewResource(R.layout.spinnertext);

            Spinner machine_spinner = (Spinner)findViewById(R.id.bcfiles_spinner);
            machine_spinner.setAdapter(machineArrayAdapter);
        }// end onPostExecute
    }// end AsyncTask populateSpinners


    public void clearTimes(View view) {
        //When the delete work records button is pressed

        //Get content of edit_delete
        EditText editDelete = (EditText) findViewById(R.id.edit_delete);
        String toDelete = editDelete.getText().toString();

        if(toDelete.equals("delete")) {
            new doClear().execute("");
        }else{
            Toast.makeText(getApplicationContext(), "To delete all records, enter 'delete' above.", Toast.LENGTH_SHORT).show();
        }

    }// end clear times

    private class doClear extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... param) {
            SQLiteHelper db = new SQLiteHelper(ManageRecords.this);
            db.deleteTimes();
            return "";
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), "Records deleted.", Toast.LENGTH_SHORT).show();
        }
    }// end AsyncTask doClear

public void exportDb(View view) {
    new createDump().execute("");
}

    private class createDump extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... param) {

        //Retrieving all records
        SQLiteHelper db = new SQLiteHelper(ManageRecords.this);
        //String nullsearch = null; // Must send function a null string in order to return all results
        List<String> cropslist = db.getCrops();
        List<Long> timeslist = db.getTimes();
        List<Long> elapsedlist = db.getElapsed();
        List<String> jobslist = db.getJobs();
        List<String> equiplist = db.getMachine();

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

//This data dump includes all records
        long startDate = 0;

        //Go through the records and add each to a list<string[]>
        for (int i = 0; i < cropslist.size(); i++) {
            if(timeslist.get(i) > startDate) {

                String[] csvsummary = {cropslist.get(i), jobslist.get(i), equiplist.get(i), Long.toString(elapsedlist.get(i)), Long.toString(timeslist.get(i))};
                csvSummaries.add(csvsummary);
            } //end timeslist if
        } //end all entries for
        //--if worksum > 0, create string[] array with crop, job, worksum

//Create filename containing date
            Long time = System.currentTimeMillis();
            Date date = new Date(time);
            DateFormat formatter = new SimpleDateFormat("yyMMddhhmmss");
String filename = "BeetClockRecords"+formatter.format(date)+".csv";

        //File localDir = new File(this.getFilesDir(), "");
        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
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

        return filename;

        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String filename) {
            Toast.makeText(getApplicationContext(), "Database saved to Downloads as "+filename, Toast.LENGTH_LONG).show();

            //Refresh database spinner
            new populateSpinners().execute("");
        }
    } // end AsyncTask Create Dump

    public void importDb(View view) {

        Spinner spin = (Spinner) findViewById(R.id.bcfiles_spinner);
        String selection = spin.getSelectedItem().toString();

        new doImport().execute(selection);
    }

    private class doImport extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... param) {

            String fileName = param[0];

            String[] row = null;

            File dlDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File dlFile = new File (dlDirectory, fileName);

            List<String[]> records = new ArrayList<>();
try {
    CSVReader csvReader = new CSVReader(new FileReader(dlFile));
    //List content = csvReader.readAll();
    records = csvReader.readAll();

    //for (Object object : content) {
    for (int i = 0; i < records.size();i++) {
        String[] record = records.get(i);

        System.out.println(record[0]
                + " # " + record[1]
                + " #  " + record[2]
                + " #  " + record[3]
                + " #  " + record[4]);
    }
    csvReader.close();
} catch(Exception e){
}//end read records

            //Write records to database
            SQLiteHelper db = new SQLiteHelper(ManageRecords.this);

//Write each line of the records to the db
            for (int i = 0; i < records.size();i++) {
                String[] record = records.get(i);

                List<String> entry = new ArrayList<String>();
                entry.add(record[0]);
                entry.add(record[4]);
                entry.add(record[3]);
                entry.add(record[1]);
                entry.add(record[2]);
//Add all records to times table
                db.addTime(entry);
            }

            return fileName;

        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String filename) {
            Toast.makeText(getApplicationContext(), "Records imported from "+ filename, Toast.LENGTH_LONG).show();
        }
    } // end AsyncTask Create Dump

}
