package aas.beetclock;

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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class TimeWorked extends AppCompatActivity {

    public String selectedDate;
    public String allEquip = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_worked);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Enter time worked");

        //Enables Strict Mode testing
        /*
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                        //.penaltyDialog()
                .penaltyFlashScreen()
                .build());
                */

        //Do on create

        new populateSpinners().execute("");

    }//end on create

    public void onBackPressed() {
        finish();
    }

    private class populateSpinners extends AsyncTask<String, Integer, List<String[]>> {
        protected List<String[]> doInBackground(String... param) {

            //Get an array of crops
            SQLiteHelper db = new SQLiteHelper(TimeWorked.this);
            String nullsearch = null; // Must send function a null string in order to return all results
            List<String> croplist = db.getCropList(nullsearch);
            java.util.Collections.sort(croplist, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareToIgnoreCase(o2);
                }
            }); // Alphebetizes while ignoring case
            String[] cropArray = new String[croplist.size()];
            cropArray = croplist.toArray(cropArray);

            //Get an array of jobs
            List<String> joblist = db.getJobList();
            java.util.Collections.sort(joblist, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareToIgnoreCase(o2);
                }
            }); // Alphebetizes while ignoring case
            String[] jobArray = new String[joblist.size()];
            jobArray = joblist.toArray(jobArray);

            //Get an array of machinery
            List<String> equiplist = db.getMachineList(nullsearch);
            java.util.Collections.sort(equiplist, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareToIgnoreCase(o2);
                }
            }); // Alphebetizes while ignoring case
            String[] equipArray = new String[equiplist.size()];
            equipArray = equiplist.toArray(equipArray);

            List<String[]> allSpinners = new ArrayList<>();
            allSpinners.add(cropArray);
            allSpinners.add(jobArray);
            allSpinners.add(equipArray);

            return allSpinners;
        }//end doInBackground

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(List<String[]> allSpinners) {
            String[] cropArray = allSpinners.get(0);
            String[] jobArray = allSpinners.get(1);
            String[] equipArray = allSpinners.get(2);

            //Populate crops spinner
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                    TimeWorked.this, R.layout.spinnertext, cropArray); //android.R.layout.simple_spinner_item
            spinnerArrayAdapter.setDropDownViewResource(R.layout.spinnertext); //android.R.layout.simple_spinner_item

            Spinner crop_spinner = (Spinner)findViewById(R.id.crop_spinner_worked);
            crop_spinner.setAdapter(spinnerArrayAdapter);

            //Populate jobs spinner
            ArrayAdapter<String> jobsArrayAdapter = new ArrayAdapter<String>(
                    TimeWorked.this, R.layout.spinnertext, jobArray); //android.R.layout.simple_spinner_item
            jobsArrayAdapter.setDropDownViewResource(R.layout.spinnertext);//android.R.layout.simple_spinner_item
            Spinner jobs_spinner = (Spinner)findViewById(R.id.jobs_spinner_worked);
            jobs_spinner.setAdapter(jobsArrayAdapter);

            //Populate machinery spinner
            ArrayAdapter<String> machineArrayAdapter = new ArrayAdapter<String>(
                    TimeWorked.this, R.layout.spinnertext, equipArray);
            machineArrayAdapter.setDropDownViewResource(R.layout.spinnertext);
            Spinner machine_spinner = (Spinner)findViewById(R.id.equip_spinner);
            machine_spinner.setAdapter(machineArrayAdapter);
        }// end onPostExecute
    }// end AsyncTask populateSpinners


    public void openDatePicker(View view){
        //Do nothing but open the date picker
        Intent intent = new Intent(this, PickDate.class);
        startActivityForResult(intent, 12345);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {  // When result of date picker is returned
        super.onActivityResult(requestCode, resultCode, data);

        String selection = data.getStringExtra("selection");
        //Make the selected date public
        selectedDate = selection;

//Set text view to reflect selected date
        TextView msgView = (TextView) findViewById(R.id.current_date);
        msgView.setTextSize(16);
        String dateSince = new String();

        Date date = new Date(Long.valueOf(selection));
        DateFormat formatter = new SimpleDateFormat("dd:MMM:yyyy");
        // Codes for re-writing this format available at http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
        dateSince = "On " + formatter.format(date) + " OR";

        msgView.setText(dateSince);

    }//end onResult

    public void addEquip(View view) {
        //Get currently selected equipment as a string
        Spinner equipSpin = (Spinner) findViewById(R.id.equip_spinner);
        String selectEquip = equipSpin.getSelectedItem().toString();
        //Only add equip if not already contained in string
        if(!allEquip.contains(selectEquip)) {

                if(!allEquip.equals("")){
                    allEquip = allEquip + ", "+ selectEquip ;
                } else {
                    allEquip = selectEquip;
                }
        }
        Toast.makeText(getApplicationContext(), "Doing job with "+allEquip, Toast.LENGTH_SHORT).show();
    }

    public void saveWork (View view){ // When save work button is clicked

    EditText hrsEdit = (EditText) findViewById(R.id.edit_hours);
    String hrsString =  hrsEdit.getText().toString();

        //Ensure that both number entries are valid
        Boolean isvalid = true;

    //If string is convertable to float, convert to float
    double hours = 0;
    try {
        hours = Double.parseDouble(hrsString);
    } catch (NumberFormatException nfe) {
        isvalid=false;
        Toast.makeText(getApplicationContext(), "Enter valid number of hours", Toast.LENGTH_LONG).show();
    }


    EditText workersEdit = (EditText) findViewById(R.id.edit_workers);
    String workersString =  workersEdit.getText().toString();
    //If string is convertable to int, convert to int
    int workers = 1;
    try {
        workers = Integer.parseInt(workersString);
    } catch (NumberFormatException nfe) {
        if (!workersString.equals("") && !workersString.equals(null)) {
            Toast.makeText(getApplicationContext(), "Invalid number of workers", Toast.LENGTH_SHORT).show();
        }
    }
if(hours > 0 && workers > 0){
    //Multiply elapsed (ms) by workers
    long msElapsed = (long)(hours * 3600000);
    long timeWorked = msElapsed * (long)workers;

    //Get values from crop and jobs spinners
    Spinner cropSpin = (Spinner) findViewById(R.id.crop_spinner_worked);
    String crop = cropSpin.getSelectedItem().toString();

    Spinner jobSpin = (Spinner) findViewById(R.id.jobs_spinner_worked);
    String job = jobSpin.getSelectedItem().toString();

    String[] toSave = {crop, String.valueOf(timeWorked), job};

    new workedDb().execute(toSave);

}else{
    Toast.makeText(getApplicationContext(), "Invalid number of workers", Toast.LENGTH_SHORT).show();
}

} // end saveWork

    private class workedDb extends AsyncTask<String[], Integer, String> {
        protected String doInBackground(String[]... params) {

            String[] parameters = params[0];

            String crop = parameters[0];
            String timeWorked = parameters[1];
            String job = parameters[2];

            //Get saved date OR current time
            String workTime = "";
            if(selectedDate != null && !selectedDate.equals("") && !selectedDate.isEmpty()){
                workTime = selectedDate;
            } else {
                workTime = Long.toString(System.currentTimeMillis());
            }

            //Save to Db, return notification in toast
            List<String> entry = new ArrayList<String>();
            entry.add(crop);
            entry.add(String.valueOf(workTime));
            entry.add(timeWorked);
            entry.add(job);
            if (allEquip.equals("")){
                entry.add("no equip");
            }else {
                entry.add(allEquip);
            }

            SQLiteHelper db = new SQLiteHelper(TimeWorked.this);
            db.addTime(entry);


            return timeWorked;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), "Entry saved", Toast.LENGTH_SHORT).show();
             }
    }


}
