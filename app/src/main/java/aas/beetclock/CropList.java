package aas.beetclock;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import aas.beetclock.SQLiteHelper;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.RelativeLayout;
import android.app.Activity;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

public class CropList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Manage Records");

        //Enables Strict Mode testing
        /*
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                        //.penaltyDialog()
                .penaltyFlashScreen()
                .build());
                */

//Load crop names from Crops table
        new populateSpinners().execute("");
/*
                StringBuilder liststring = new StringBuilder();
                String sep = ", ";
                for (String each : croplist) {
                  liststring.append(sep).append(each);
                }
                String display = liststring.toString();

                //String test = "Print this I beg you!!!";

                TextView textView = new TextView(CropList.this);
                textView.setTextSize(20);
                textView.setText(display);
                RelativeLayout msgLayout = (RelativeLayout) findViewById(R.id.display_crop_text);
                msgLayout.removeAllViews();
                msgLayout.addView(textView);
                */

//As an alternative, try printing all crop values
    }

    private class populateSpinners extends AsyncTask<String, Integer, List<String[]>> {
        protected List<String[]> doInBackground(String... param) {

            //Get an array of crops
            SQLiteHelper db = new SQLiteHelper(CropList.this);
            String nullsearch = null; // Must send function a null string in order to return all results
            List<String> croplist = db.getCropList(nullsearch);
            java.util.Collections.sort(croplist);
            String[] cropArray = new String[croplist.size()];
            cropArray = croplist.toArray(cropArray);

            List<String[]> allSpinners = new ArrayList<>();
            allSpinners.add(cropArray);

            return allSpinners;
        }//end doInBackground

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(List<String[]> allSpinners) {
            String[] cropArray = allSpinners.get(0);

            //Populate crops spinner
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                    CropList.this, R.layout.spinnertext, cropArray); //android.R.layout.simple_spinner_item
            spinnerArrayAdapter.setDropDownViewResource(R.layout.spinnertext); //android.R.layout.simple_spinner_item

            Spinner crop_spinner = (Spinner)findViewById(R.id.crop_update_spinner);
            crop_spinner.setAdapter(spinnerArrayAdapter);
        }// end onPostExecute
    }// end AsyncTask populateSpinners


    public void addCrop(View view) {
        //First I need to retrieve the name of the new crop from the text editor
        EditText edit = (EditText) findViewById(R.id.edit_newcrop);
        String newcrop =  edit.getText().toString();

        if (newcrop.equals("") || newcrop.isEmpty() || newcrop.equals(null)){
            Toast.makeText(getApplicationContext(), "Enter name of crop to add", Toast.LENGTH_LONG).show();
        }else{
            //Add crop to Db
            new doAdd().execute(newcrop);
        }


        /*
        //Then I can add the new crop to the Crops table
        SQLiteHelper db = new SQLiteHelper(this);
        db.addCropList(newcrop);

        //And finally I want to update the list of crops on the spinner
        String nullsearch = null; // Must send function a null string in order to return all results
        List<String> croplist = db.getCropList(nullsearch);
//Initialize spinner and populate with items
        String[] spinarray = new String[croplist.size()];
        spinarray = croplist.toArray(spinarray);
        //This initializes the spinner with values from crop table
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                CropList.this, android.R.layout.simple_spinner_item, spinarray);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//Update spinner values
        Spinner crop_update_spinner = (Spinner)findViewById(R.id.crop_update_spinner);
        crop_update_spinner.setAdapter(spinnerArrayAdapter);

        Toast.makeText(getApplicationContext(), "Crop added", Toast.LENGTH_SHORT).show();
        */

    } // End add newcrop

    private class doAdd extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... param) {
            String crop = param[0];

            SQLiteHelper db = new SQLiteHelper(CropList.this);
            db.addCropList(crop);
            return "";
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String result) {
            new populateSpinners().execute("");
            Toast.makeText(getApplicationContext(), "Crop added", Toast.LENGTH_SHORT).show();
        }
    }//end AsyncTask doAdd


    public void deleteCrop(View view) {

        //First I will need to retrieve the selected value from the crops spinner; that's the crop that will be deleted
        Spinner spin = (Spinner) findViewById(R.id.crop_update_spinner);
        String selection = spin.getSelectedItem().toString();

        new doRemove().execute(selection);
        /*
        //Now I can call the delete function in SQLiteHelper
        SQLiteHelper db = new SQLiteHelper(this);
        db.deleteCropList(selection);

        //And finally I want to update the list of crops on the spinner
        String nullsearch = null; // Must send function a null string in order to return all results
        List<String> croplist = db.getCropList(nullsearch);
//Initialize spinner and populate with items
        String[] spinarray = new String[croplist.size()];
        spinarray = croplist.toArray(spinarray);
        //This initializes the spinner with values from crop table
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                CropList.this, R.layout.spinnertext, spinarray);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinnertext);
//Update spinner values
        Spinner crop_update_spinner = (Spinner)findViewById(R.id.crop_update_spinner);
        crop_update_spinner.setAdapter(spinnerArrayAdapter);

        Toast.makeText(getApplicationContext(), "Crop deleted", Toast.LENGTH_SHORT).show();
        */
    }//End delete crop

    private class doRemove extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... param) {
            String crop = param[0];

            SQLiteHelper db = new SQLiteHelper(CropList.this);
            db.deleteCropList(crop);
            return "";
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String result) {
            new populateSpinners().execute("");
            Toast.makeText(getApplicationContext(), "Crop deleted", Toast.LENGTH_SHORT).show();
        }
    }//end AsyncTask doRemove

    }
