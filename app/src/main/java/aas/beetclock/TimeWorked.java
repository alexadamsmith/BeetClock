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
import android.view.Menu;
import android.view.MenuItem;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class TimeWorked extends AppCompatActivity {

    public String selectedDate;
    public String allEquip = "";

    public ImageView background;
    public boolean isDateReturn = false;


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
        setContentView(R.layout.activity_time_worked);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Time Worked");


        //Do on create

        new populateSpinners().execute("");

    }//end on create

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_time_worked, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.crop_bar) {
            // return true;
            Intent cropIntent = new Intent(this, CropList.class);
            startActivity(cropIntent);
            //finish();
        }
        if (id == R.id.job_bar) {
            // return true;
            Intent cropIntent = new Intent(this, ManageJobs.class);
            startActivity(cropIntent);
            //finish();
        }
        if (id == R.id.equip_bar) {
            // return true;
            Intent cropIntent = new Intent(this, ManageEquipment.class);
            startActivity(cropIntent);
            //finish();
        }
        if (id == R.id.record_bar) {
            // return true;
            Intent cropIntent = new Intent(this, ManageRecords.class);
            startActivity(cropIntent);
            //finish();
        }

        if (id == R.id.summary_bar) {
            Intent summaryIntent = new Intent(this, WorkSummary.class);
            startActivity(summaryIntent);
            //finish();
        }

        if (id == R.id.report_bar) {
            Intent reportIntent = new Intent(this, SendReport.class);
            startActivity(reportIntent);
            //finish();
        }

        if (id == R.id.sheet_bar) {
            Intent sheetIntent = new Intent(this, PopulateSheet.class);
            startActivity(sheetIntent);
            //finish();
        }

        if (id == R.id.feedback_bar) {
            Intent feedbackIntent = new Intent(this, SendFeedback.class);
            startActivity(feedbackIntent);
            //finish();
        }

        if (id == R.id.timer_bar) {
            Intent workedIntent = new Intent(this, MainActivity.class);
            startActivity(workedIntent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    public void onResume()
    {  // After a pause OR at startup
        super.onResume();

        //Repopulate spinners UNLESS returning from date selection
if(isDateReturn){
    isDateReturn = false;
} else {
    new populateSpinners().execute("");
}
    }// end on resume




    public void onBackPressed() {
        //Intent workedIntent = new Intent(this, MainActivity.class);
        //startActivity(workedIntent);
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
if(resultCode == RESULT_OK) {
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
}
        //Prevents spinner refresh when returning from date selection
isDateReturn = true;
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

        //Set text view to reflect selected equipment
        TextView equipView = (TextView) findViewById(R.id.equip_title);
        equipView.setTextSize(16);
        if(!allEquip.equals("")){
            String equipText = "Change equipment (now using "+allEquip+")";
            equipView.setText(equipText);
        }

        // Toast.makeText(getApplicationContext(), "Doing job with "+allEquip, Toast.LENGTH_SHORT).show();
    } // end addEquip

    public void remEquip(View view) {
        //Get currently selected equipment as a string
        Spinner equipSpin = (Spinner) findViewById(R.id.equip_spinner);
        String selectEquip = equipSpin.getSelectedItem().toString();
        //parse selectedEquip to an array
        List<String> equips = Arrays.asList(allEquip.split("\\s*,\\s*"));

        for (int i = 0; i < equips.size(); i++){
            if(equips.get(i).contains(selectEquip)) {
                equips.set(i, "");
            }
        }

        StringBuilder liststring = new StringBuilder();
        String sep = ", ";
        for (int i = 0; i < equips.size(); i++) {
            if(i>0 && !equips.get(i).equals("") && !(i==1 && equips.get(0).equals("")) ) {
                liststring.append(sep).append(equips.get(i));
            } else {
                liststring.append(equips.get(i));
            }
        } // end for array length
        allEquip = liststring.toString();

        //Set text view to reflect selected equipment
        TextView equipView = (TextView) findViewById(R.id.equip_title);
        equipView.setTextSize(16);
        if(!allEquip.equals("")){
            String equipText = "Change equipment (now using "+allEquip+")";
            equipView.setText(equipText);
        }else{
            String equipText = "Add equipment (optional):";
            equipView.setText(equipText);
        }

    } // end remEquip





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
            Long preTime = null;

            //Need to introduce small random numbers to prevent saved dates from being identical
            Random rand = new Random();
            int randint = rand.nextInt(50) + 1;
            if(selectedDate != null && !selectedDate.equals("") && !selectedDate.isEmpty()){
                preTime = Long.parseLong(selectedDate)+ (long)randint;
            } else {
                preTime = System.currentTimeMillis();
            }
            String workTime = Long.toString(preTime);

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
