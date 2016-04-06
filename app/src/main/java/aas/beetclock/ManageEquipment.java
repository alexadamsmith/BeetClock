package aas.beetclock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ManageEquipment extends AppCompatActivity {

    public final static String KEY_PARAMS = "aas.beetclock.sheetParams";
    public final static String KEY_JOBS = "aas.beetclock.sheetJobs";
    public final static String KEY_TIMES = "aas.beetclock.sheetTimes";
    public final static String KEY_EQUIP = "aas.beetclock.sheetEquipment";

    public final static int FILE_CODE = 12345;
    public final static int SHEET_CODE = 54321;
    public final static int GET_CODE = 42113;

    public final static String FILE_ID = "aas.beetclock.file_id";
    public final static String FILE_NAME = "aas.beetclock.file_name";

    public String[] files;
    public String[] sheets;
    public String[] ids;
    public String savedName;
    public String savedId;

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
        setContentView(R.layout.activity_manage_equipment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Manage Equipment");

        new populateSpinners().execute("");

    }

    /*This is run when the user navigates back to the main activity*/
    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();

        new populateSpinners().execute("");

    }// end on resume

    public void onBackPressed() {
        finish();
    }

/*
    private class onLoad extends AsyncTask<String, Integer, List<String[]>> {
        protected List<String[]> doInBackground(String... param) {



            SharedPreferences sharedPref = ManageEquipment.this.getSharedPreferences(
                    "aas.beetclock", Context.MODE_PRIVATE);


            //Get an array of equipment
            SQLiteHelper db = new SQLiteHelper(ManageEquipment.this);
            String nullsearch = null; // Must send function a null string in order to return all results
            List<String> machinelist = db.getMachineList(nullsearch);
            java.util.Collections.sort(machinelist, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareToIgnoreCase(o2);
                }
            }); // Alphebetizes while ignoring case
            String[] machineArray = new String[machinelist.size()];
            machineArray = machinelist.toArray(machineArray);

            List<String[]> allSpinners = new ArrayList<>();
            allSpinners.add(machineArray);

            //Also getting reportDate from shared prefs

            savedName = sharedPref.getString(FILE_NAME, "");
            savedId = sharedPref.getString(FILE_ID, "");

            return allSpinners;
        }//end doInBackground

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(List<String[]> allSpinners) {
            String[] machineArray = allSpinners.get(0);

            //Populate crops spinner
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                    ManageEquipment.this, R.layout.spinnertext, machineArray); //android.R.layout.simple_spinner_item
            spinnerArrayAdapter.setDropDownViewResource(R.layout.spinnertext); //android.R.layout.simple_spinner_item

            Spinner machine_spinner = (Spinner)findViewById(R.id.machine_spinner);
            machine_spinner.setAdapter(spinnerArrayAdapter);


            //Get all files on drive if no file is preselected

            if(savedId.equals("")) {
                Intent intent = new Intent(ManageEquipment.this, DoScriptExecute.class);
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
                Intent intent = new Intent(ManageEquipment.this, DoScriptExecute.class);
                //I will need to use two blank arrays to fill in for the jobs and values
                String[] blank = {};
                String[] params = {"getFiles"};
                intent.putExtra(KEY_PARAMS, params);
                intent.putExtra(KEY_JOBS, blank);
                intent.putExtra(KEY_EQUIP, blank);
                intent.putExtra(KEY_TIMES, blank);
                startActivityForResult(intent, FILE_CODE);
                //Get spreadsheets
                Intent intent2 = new Intent(ManageEquipment.this, DoScriptExecute.class);
                String[] params2 = {"getSheets", savedId};
                intent2.putExtra(KEY_PARAMS, params2);
                intent2.putExtra(KEY_JOBS, blank);
                intent2.putExtra(KEY_EQUIP, blank);
                intent2.putExtra(KEY_TIMES, blank);
                startActivityForResult(intent2, SHEET_CODE);

                //Set text
                TextView textView = (TextView) findViewById(R.id.file_text);
                textView.setTextSize(16);
                textView.setText("Write to " + savedName + " OR select a new file");
            }// end saveid if



        }// end onPostExecute
    }// end AsyncTask populateSpinners


*/





    private class populateSpinners extends AsyncTask<String, Integer, List<String[]>> {
        protected List<String[]> doInBackground(String... param) {

            SQLiteHelper db = new SQLiteHelper(ManageEquipment.this);
            String nullsearch = null;

            //Initialize machinery spinner and populate with items
            //Load machinery names from Machine table
            List<String> machinelist = db.getMachineList(nullsearch);
            java.util.Collections.sort(machinelist, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareToIgnoreCase(o2);
                }
            }); // Alphebetizes while ignoring case
            String[] spinmachine = new String[machinelist.size()];
            spinmachine = machinelist.toArray(spinmachine);

            List<String[]> allSpinners = new ArrayList<>();
            allSpinners.add(spinmachine);

            return allSpinners;
        }//end doInBackground

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(List<String[]> allSpinners) {
            String[] spinmachine = allSpinners.get(0);

            //Populate machine spinner
            ArrayAdapter<String> machineArrayAdapter = new ArrayAdapter<String>(
                    ManageEquipment.this, R.layout.spinnertext, spinmachine);
            machineArrayAdapter.setDropDownViewResource(R.layout.spinnertext);

            Spinner machine_spinner = (Spinner)findViewById(R.id.machine_spinner);
            machine_spinner.setAdapter(machineArrayAdapter);
        }// end onPostExecute
    }// end AsyncTask populateSpinners


    public void addMachine(View view) {
        //First I need to retrieve the name of the new crop from the text editor
        EditText edit = (EditText) findViewById(R.id.edit_machine);
        String newmachine =  edit.getText().toString();

        if (newmachine.equals("") || newmachine.isEmpty() || newmachine.equals(null)){
            Toast.makeText(getApplicationContext(), "Enter name of equipment to add", Toast.LENGTH_LONG).show();
        }else{
            //Add crop to Db
            new doAdd().execute(newmachine);
        }

    } // End add machine

    private class doAdd extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... param) {
            String newmachine = param[0];

            SQLiteHelper db = new SQLiteHelper(ManageEquipment.this);
            db.addMachineList(newmachine);
            return "";
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String result) {
            new populateSpinners().execute("");
            Toast.makeText(getApplicationContext(), "Equipment added", Toast.LENGTH_SHORT).show();
        }
    }//end AsyncTask doAdd

    public void deleteMachine(View view) {

        //First I will need to retrieve the selected value from the crops spinner; that's the crop that will be deleted
        Spinner spin = (Spinner) findViewById(R.id.machine_spinner);
        String selection = spin.getSelectedItem().toString();

        new doRemove().execute(selection);

    }//End delete machine

    private class doRemove extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... param) {
            String machine = param[0];

            SQLiteHelper db = new SQLiteHelper(ManageEquipment.this);
            db.deleteMachineList(machine);
            return "";
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String result) {
            new populateSpinners().execute("");
            Toast.makeText(getApplicationContext(), "Eqipment deleted", Toast.LENGTH_SHORT).show();
        }
    }//end AsyncTask doRemove

    public void getEquip(View view) {

        Intent intent = new Intent(this, GetEquipment.class);
        startActivity(intent);

    }//End get equipment

}
