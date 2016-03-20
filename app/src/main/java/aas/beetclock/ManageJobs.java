package aas.beetclock;

import android.os.AsyncTask;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ManageJobs extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_jobs);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Manage jobs");

        //Enables Strict Mode testing
        /*
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                        //.penaltyDialog()
                .penaltyFlashScreen()
                .build());
*/
        new populateSpinners().execute("");

        /*
//Initialize jobs spinner and populate
        SQLiteHelper db = new SQLiteHelper(ManageJobs.this);
        String nullsearch = null; // Must send function a null string in order to return all results
        //I also want to populate the jobs spinner with jobs from the jobs table
        List<String> joblist = db.getJobList();
        String[] jobsarray = new String[joblist.size()];
        jobsarray = joblist.toArray(jobsarray);
        //This initializes the spinner with values from the job table
        ArrayAdapter<String> jobsArrayAdapter = new ArrayAdapter<String>(
                this, R.layout.spinnertext, jobsarray);
        jobsArrayAdapter.setDropDownViewResource(R.layout.spinnertext);
        Spinner jobs_spinner = (Spinner)findViewById(R.id.jobs_spinner);
        jobs_spinner.setAdapter(jobsArrayAdapter);

        //Initialize categories spinner and populate.  These values will never change.
        String[] categories = {"Soil prep","Cultivation","Post-harvest"};
        ArrayAdapter<String> catsArrayAdapter = new ArrayAdapter<String>(
                this, R.layout.spinnertext, categories);
        catsArrayAdapter.setDropDownViewResource(R.layout.spinnertext);
        Spinner cats_spinner = (Spinner)findViewById(R.id.categories_spinner);
        cats_spinner.setAdapter(catsArrayAdapter);
*/
    }

    public void onBackPressed() {
        finish();
    }

    private class populateSpinners extends AsyncTask<String, Integer, List<String[]>> {
        protected List<String[]> doInBackground(String... param) {

            //Get an array of crops
            SQLiteHelper db = new SQLiteHelper(ManageJobs.this);
                       List<String> joblist = db.getJobList();
            java.util.Collections.sort(joblist, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareToIgnoreCase(o2);
                }
            }); // Alphebetizes while ignoring case
            String[] jobArray = new String[joblist.size()];
            jobArray = joblist.toArray(jobArray);

            List<String[]> allSpinners = new ArrayList<>();
            allSpinners.add(jobArray);

            return allSpinners;
        }//end doInBackground

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(List<String[]> allSpinners) {
            String[] cropArray = allSpinners.get(0);

            //Populate crops spinner
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                    ManageJobs.this, R.layout.spinnertext, cropArray); //android.R.layout.simple_spinner_item
            spinnerArrayAdapter.setDropDownViewResource(R.layout.spinnertext); //android.R.layout.simple_spinner_item

            Spinner job_spinner = (Spinner)findViewById(R.id.jobs_spinner);
            job_spinner.setAdapter(spinnerArrayAdapter);

            //Initialize categories spinner and populate.  These values will never change.
            String[] categories = {"Soil prep","Cultivation","Post-harvest"};
            ArrayAdapter<String> catsArrayAdapter = new ArrayAdapter<String>(
                    ManageJobs.this, R.layout.spinnertext, categories);
            catsArrayAdapter.setDropDownViewResource(R.layout.spinnertext);
            Spinner cats_spinner = (Spinner)findViewById(R.id.categories_spinner);
            cats_spinner.setAdapter(catsArrayAdapter);
        }// end onPostExecute
    }// end AsyncTask populateSpinners


    public void addJob(View view){
        //As above, but with categories
        //First I need to retrieve the name of the new job from the text editor
        EditText edit = (EditText) findViewById(R.id.edit_newjob);
        String newjob =  edit.getText().toString();

        if (newjob.equals("") || newjob.isEmpty() || newjob.equals(null)){
            Toast.makeText(getApplicationContext(), "Enter name of job to add", Toast.LENGTH_LONG).show();
        }else{
            //I also need to retrieve the cateory
            Spinner catSpin = (Spinner) findViewById(R.id.categories_spinner);
            String category = catSpin.getSelectedItem().toString();
            //Join category and job in an array
            String[] catJob = {category, newjob};

            new doAdd().execute(catJob);
        }


        /*
        //Then I can add the category job array to the Jobs table
        SQLiteHelper db = new SQLiteHelper(this);
        db.addJobList(catJob);

        //And finally I want to update the list of crops on the spinner
        List<String> joblist = db.getJobList();
//Initialize spinner and populate with items
        String[] spinarray = new String[joblist.size()];
        spinarray = joblist.toArray(spinarray);
        //This initializes the spinner with values from crop table
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                ManageJobs.this, android.R.layout.simple_spinner_item, spinarray);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//Update spinner values
        Spinner job_update_spinner = (Spinner)findViewById(R.id.jobs_spinner);
        job_update_spinner.setAdapter(spinnerArrayAdapter);

        Toast.makeText(getApplicationContext(), "Job added", Toast.LENGTH_SHORT).show();
        */
    }//End add job

    private class doAdd extends AsyncTask<String[], Integer, String> {
        protected String doInBackground(String[]... params) {
            String[] catJob = params[0];

            SQLiteHelper db = new SQLiteHelper(ManageJobs.this);
            db.addJobList(catJob);
            return "";
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String result) {
            new populateSpinners().execute("");
            Toast.makeText(getApplicationContext(), "Job added", Toast.LENGTH_SHORT).show();
        }
    }//end AsyncTask doAdd

    public void deleteJob(View view) {

        //First I will need to retrieve the selected value from the crops spinner; that's the crop that will be deleted
        Spinner spin = (Spinner) findViewById(R.id.jobs_spinner);
        String selection = spin.getSelectedItem().toString();

        new doRemove().execute(selection);
        /*
        //Now I can call the delete function in SQLiteHelper
        SQLiteHelper db = new SQLiteHelper(this);
        db.deleteJobList(selection);

        //And finally I want to update the list of crops on the spinner
        List<String> joblist = db.getJobList();
//Initialize spinner and populate with items
        String[] spinarray = new String[joblist.size()];
        spinarray = joblist.toArray(spinarray);
        //This initializes the spinner with values from crop table
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                ManageJobs.this, R.layout.spinnertext, spinarray);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinnertext);
//Update spinner values
        // Spinner jobs_spinner = (Spinner)findViewById(R.id.jobs_spinner);
        spin.setAdapter(spinnerArrayAdapter);

        Toast.makeText(getApplicationContext(), "Job deleted", Toast.LENGTH_SHORT).show();
        */
    }//end delete job

    private class doRemove extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... param) {
            String job = param[0];

            SQLiteHelper db = new SQLiteHelper(ManageJobs.this);
            db.deleteJobList(job);
            return "";
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String result) {
            new populateSpinners().execute("");
            Toast.makeText(getApplicationContext(), "Job deleted", Toast.LENGTH_SHORT).show();
        }
    }//end AsyncTask doRemove

}
