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

            //Initialize categories spinner and populate.  These values will never change.
            String[] categories = {"Tractor","Implement"};
            ArrayAdapter<String> catsArrayAdapter = new ArrayAdapter<String>(
                    ManageEquipment.this, R.layout.spinnertext, categories);
            catsArrayAdapter.setDropDownViewResource(R.layout.spinnertext);
            Spinner cats_spinner = (Spinner)findViewById(R.id.equipcat_spinner);
            cats_spinner.setAdapter(catsArrayAdapter);
        }// end onPostExecute
    }// end AsyncTask populateSpinners


    public void addMachine(View view) {
        //First I need to retrieve the name of the new crop from the text editor
        EditText edit = (EditText) findViewById(R.id.edit_machine);
        String newmachine =  edit.getText().toString();

        Spinner catSpin = (Spinner) findViewById(R.id.equipcat_spinner);
        String category = catSpin.getSelectedItem().toString();
        String[] catEquip = {newmachine, category};

        if (newmachine.equals("") || newmachine.isEmpty() || newmachine.equals(null)){
            Toast.makeText(getApplicationContext(), "Enter name of equipment to add", Toast.LENGTH_LONG).show();
        }else if (newmachine.contains(":") || newmachine.contains(",")) {
            Toast.makeText(getApplicationContext(), "Include only letters and numbers in equipment name", Toast.LENGTH_LONG).show();
        } else {
            //Add crop to Db
            new doAdd().execute(catEquip);
        }

    } // End add machine

    private class doAdd extends AsyncTask<String[], Integer, String> {
        protected String doInBackground(String[]... param) {
            String[] outputs = param[0];
            String newmachine = outputs[0];
            String category = outputs[1];
            String mechCat = category+": "+newmachine;

            SQLiteHelper db = new SQLiteHelper(ManageEquipment.this);
            db.addMachineList(mechCat);
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
