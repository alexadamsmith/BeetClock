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

public class ManageEquipment extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_equipment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Manage equipment");

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
        SQLiteHelper db = new SQLiteHelper(ManageEquipment.this);
        String nullsearch = null;

        //Initialize machinery spinner and populate with items
        //Load machinery names from Machine table
        List<String> machinelist = db.getMachineList(nullsearch);
        String[] spinmachine = new String[machinelist.size()];
        spinmachine = machinelist.toArray(spinmachine);
        ArrayAdapter<String> machineArrayAdapter = new ArrayAdapter<String>(
                ManageEquipment.this, R.layout.spinnertext, spinmachine);
        machineArrayAdapter.setDropDownViewResource(R.layout.spinnertext);

        Spinner machine_spinner = (Spinner)findViewById(R.id.machine_spinner);
        machine_spinner.setAdapter(machineArrayAdapter);
        */

    }

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

/*
        //Then I can add the new crop to the Crops table
        SQLiteHelper db = new SQLiteHelper(this);
        db.addMachineList(newmachine);

        //And finally I want to update the list of machinery on the spinner
        String nullsearch = null; // Must send function a null string in order to return all results
//Initialize machinery spinner and populate with items
        //Load machinery names from Machine table
        List<String> machinelist = db.getMachineList(nullsearch);
        String[] spinmachine = new String[machinelist.size()];
        spinmachine = machinelist.toArray(spinmachine);
        ArrayAdapter<String> machineArrayAdapter = new ArrayAdapter<String>(
                ManageEquipment.this, R.layout.spinnertext, spinmachine);
        machineArrayAdapter.setDropDownViewResource(R.layout.spinnertext);

        Spinner machine_spinner = (Spinner)findViewById(R.id.machine_spinner);
        machine_spinner.setAdapter(machineArrayAdapter);

        Toast.makeText(getApplicationContext(), "Equipment added", Toast.LENGTH_SHORT).show();
        */
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
/*
        //Now I can call the delete function in SQLiteHelper
        SQLiteHelper db = new SQLiteHelper(this);
        db.deleteMachineList(selection);

        //And finally I want to update the list of crops on the spinner
        String nullsearch = null; // Must send function a null string in order to return all results
//Initialize machinery spinner and populate with items
        //Load machinery names from Machine table
        List<String> machinelist = db.getMachineList(nullsearch);
        String[] spinmachine = new String[machinelist.size()];
        spinmachine = machinelist.toArray(spinmachine);
        ArrayAdapter<String> machineArrayAdapter = new ArrayAdapter<String>(
                ManageEquipment.this, R.layout.spinnertext, spinmachine);
        machineArrayAdapter.setDropDownViewResource(R.layout.spinnertext);

        Spinner machine_spinner = (Spinner)findViewById(R.id.machine_spinner);
        machine_spinner.setAdapter(machineArrayAdapter);

        Toast.makeText(getApplicationContext(), "Equipment deleted", Toast.LENGTH_SHORT).show();
        */
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

}
