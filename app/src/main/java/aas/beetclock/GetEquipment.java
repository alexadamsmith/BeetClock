package aas.beetclock;

import android.app.Activity;
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
import android.widget.AdapterView;
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

public class GetEquipment extends AppCompatActivity {

    public final static String KEY_PARAMS = "aas.beetclock.sheetParams";
    public final static String KEY_JOBS = "aas.beetclock.sheetJobs";
    public final static String KEY_TIMES = "aas.beetclock.sheetTimes";
    public final static String KEY_EQUIP = "aas.beetclock.sheetEquipment";

    public final static int FILE_CODE = 12345;
    public final static int GET_CODE = 42113;

    public final static String FILE_ID = "aas.beetclock.file_id";
    public final static String FILE_NAME = "aas.beetclock.file_name";

    public String[] files;
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
        setContentView(R.layout.activity_get_equipment);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Get Equipment");

        new onLoad().execute("");

    }

    public void onBackPressed() {
        finish();
    }


    private class onLoad extends AsyncTask<String, Integer, List<String[]>> {
        protected List<String[]> doInBackground(String... param) {

            SharedPreferences sharedPref = GetEquipment.this.getSharedPreferences(
                    "aas.beetclock", Context.MODE_PRIVATE);

            savedName = sharedPref.getString(FILE_NAME, "");
            savedId = sharedPref.getString(FILE_ID, "");

            List<String[]> emptyList = new ArrayList<>();
            return emptyList;
        }//end doInBackground

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(List<String[]> allSpinners) {

            //Get all files on drive if no file is preselected

            if(savedId.equals("")) {
                Intent intent = new Intent(GetEquipment.this, DoScriptExecute.class);
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
                Intent intent = new Intent(GetEquipment.this, DoScriptExecute.class);
                //I will need to use two blank arrays to fill in for the jobs and values
                String[] blank = {};
                String[] params = {"getFiles"};
                intent.putExtra(KEY_PARAMS, params);
                intent.putExtra(KEY_JOBS, blank);
                intent.putExtra(KEY_EQUIP, blank);
                intent.putExtra(KEY_TIMES, blank);
                startActivityForResult(intent, FILE_CODE);

                //Set text
                TextView textView = (TextView) findViewById(R.id.file_text);
                textView.setTextSize(16);
                textView.setText("Get equipment from " + savedName + " OR select a new file");
            }// end saveid if



        }// end onPostExecute
    }// end AsyncTask populateSpinners





    public void onActivityResult(int requestCode, int resultCode, Intent data) {  // When result of doScriptExecute is returned
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) { //Do this if returning a filenames response
            files = data.getStringArrayExtra(DoScriptExecute.KEY_RESPONSE);
            ids = data.getStringArrayExtra(DoScriptExecute.KEY_IDS);

            //Now I will go ahead and populate the filenames spinner
            ArrayAdapter<String> filesArrayAdapter = new ArrayAdapter<String>(
                    this, R.layout.spinnertext, files);
            filesArrayAdapter.setDropDownViewResource(R.layout.spinnertext);
            Spinner files_spinner = (Spinner) findViewById(R.id.files_spinner);
            files_spinner.setAdapter(filesArrayAdapter);
//End file result
            files_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

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

                    //Set text
                    TextView textView = (TextView) findViewById(R.id.file_text);
                    textView.setTextSize(16);
                    textView.setText("Get equipment from " + savedName + " OR select a new file");
                }
                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });




        }  else if (requestCode == GET_CODE && resultCode == Activity.RESULT_OK) {

            String[] equips = data.getStringArrayExtra(DoScriptExecute.KEY_RESPONSE);

            for (int i = 0; i < equips.length; i++) {
                System.out.println(equips[i]);
            }
            new doWrite().execute(equips);

        }//End populate result

    }//end onActivityResult


    private class doWrite extends AsyncTask<String[], Integer, String> {
        protected String doInBackground(String[]... param) {
//Before retrieving new machines, remove all old machines from list

            String[] equipment = param[0];
            SQLiteHelper db = new SQLiteHelper(GetEquipment.this);

            for (int i = 0; i < equipment.length; i++) {
                if (!equipment[i].equals("") && !equipment[i].isEmpty() &&! equipment[i].equals(null)) {
                    db.addMachineList(equipment[i]);
                }
            }
            return "";
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), "Equipment retrieved from NOFA spreadsheet", Toast.LENGTH_LONG).show();
        }
    }//end AsyncTask doGet



    /*


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

    */



    public void getEquip(View view) {

    new doGet().execute("");
    }//End delete machine


    private class doGet extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... param) {
//Before retrieving new machines, remove all old machines from list
            SQLiteHelper db = new SQLiteHelper(GetEquipment.this);
            db.deleteMachineList("");
                        return "";
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String result) {
            String[] params = {"getEquip", savedId};
            String[] blank = {};
            Intent intent = new Intent(GetEquipment.this, DoScriptExecute.class);
            intent.putExtra(KEY_PARAMS, params);
            intent.putExtra(KEY_JOBS, blank);
            intent.putExtra(KEY_EQUIP, blank);
            intent.putExtra(KEY_TIMES, blank);
            startActivityForResult(intent, GET_CODE);

            System.out.println("Saved ID: " + savedId);
        }
    }//end AsyncTask doGet

}
