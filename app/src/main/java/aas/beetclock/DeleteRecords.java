package aas.beetclock;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DeleteRecords extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_records);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

  new refreshButtons().execute("");
    }


    private class refreshButtons extends AsyncTask<String, Integer, List<String[]>> {
        protected List<String[]> doInBackground(String... param) {

            SQLiteHelper db = new SQLiteHelper(DeleteRecords.this);
            //Get crops for all records
            List<String> crops = db.getCrops();
            String[] cropArray = new String[crops.size()];
            cropArray = crops.toArray(cropArray);

            //Get times for all records and create a sorted list of times
            List<Long> times = db.getTimes();
            List<Long> timeSort = sortListWithoutModifyingOriginalList(times);

            List<String> timeStr = new ArrayList<>();
            List<String> sortStr = new ArrayList<>();
            //Index numbers are used to delete a DB entry corresponding to the selected button
            List<Integer> indexList = new ArrayList<>();
            for (int i = 0; i < times.size(); i++){
                String time = Long.toString(times.get(i));
                String sort = Long.toString(timeSort.get(i));
                timeStr.add(time);
                sortStr.add(sort);
                indexList.add(i);
            }
            String[] timeArray = new String[times.size()];
            timeArray = timeStr.toArray(timeArray);
            String[] sortArray = new String[times.size()];
            sortArray = sortStr.toArray(sortArray);
            Integer[] indexArray = new Integer[times.size()];
            indexArray = indexList.toArray(indexArray);

            for (int i = 0; i < sortArray.length; i++) {
                System.out.println("Sort:"+sortArray[i]);
            }
            for (int i = 0; i < timeArray.length; i++) {
                System.out.println("Time:"+timeArray);
            }

            //Get jobs for all records
            List<String> jobs = db.getJobs();
            String[] jobArray = new String[jobs.size()];
            jobArray = jobs.toArray(jobArray);

            String[] sortCrop = new String[cropArray.length];
            String[] sortJob = new String[jobArray.length];
            String[] indexSort = new String[jobArray.length];
            List<String[]> allCurrent = new ArrayList<>();
            //Sort entries by date saved and add to dump!
            for (int i = 0; i < sortArray.length; i++) {
                for (int j = 0; j < timeArray.length; j++) {
                    if (sortArray[i].equals(timeArray[j])) {
                        sortCrop[i] = cropArray[j];
                        sortJob[i] = jobArray[j];
                        indexSort[i] = Integer.toString(indexArray[j]);
                    }
                }
            } //end sort for

            allCurrent.add(sortCrop);
            allCurrent.add(sortArray);
            allCurrent.add(sortJob);
            allCurrent.add(indexSort);
            return allCurrent;
        }//end doInBackground

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(List<String[]> allCurrent) {

            //First I will connect to the layout and set params including space between buttons
            LinearLayout linLayout = (LinearLayout)findViewById(R.id.deleteRecords);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 0, 0, 8);

            //Clear existing entries in layout
            linLayout.removeAllViews();

            //I will also inflate the spacer.xml view for insertion between buttons
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            //Setting up a listener for the button clicks
            View.OnClickListener listener = new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    int buttonTag = Integer.parseInt(view.getTag().toString().trim());
                    deleteRecord(buttonTag);
                    //refreshButtons();
                    new refreshButtons().execute("");
                }
            };

            String[] cropArray = allCurrent.get(0);
            String[] timeArray = allCurrent.get(1);
            String[] jobArray = allCurrent.get(2);
            String[] index = allCurrent.get(3);

            for (int i = 0; i < cropArray.length; i++) {


                //Put the date into a readable format
                Date date = new Date(Long.parseLong(timeArray[i]));
                DateFormat formatter = new SimpleDateFormat("dd:MMM:yyyy");

                //Parse together string reporting open job
                String stopProcess = jobArray[i] +" "+ cropArray[i] + " on " + formatter.format(date)+": Press to Delete";

                Button stopButton = new Button(DeleteRecords.this);
                stopButton.setTag(Integer.parseInt(index[i]));//This assigns a button index based on the original position in the retrieved records
                stopButton.setText(stopProcess);

                stopButton.setOnClickListener(listener); //end onclicklistener

                linLayout.addView(stopButton, layoutParams);

                View spacerView = inflater.inflate(R.layout.spacer, null, false);
                linLayout.addView(spacerView);

/* //In case I want to create tickign clocks on stop buttons
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        try {
                            while (!isInterrupted()) {
                                Thread.sleep(1000);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // update TextView here!
                                    }
                                });
                            }
                        } catch (InterruptedException e) {
                        }
                    }
                };

                t.start();
*/
            } // end current for

        }//end postExecute
    }//end AsyncTask refreshButtons


    public void deleteRecord(int process) {

        new doDel().execute(process);

    } // End Stop


    private class doDel extends AsyncTask<Integer, Integer, String> {
        protected String doInBackground(Integer... params) {

            //Load process
            int process = params[0];

            SQLiteHelper db = new SQLiteHelper(DeleteRecords.this);

            String procString = Integer.toString(process);
            //Remove the stopped process from the database
            db.deleteRecord(procString);

            return "";
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String param) {
            new refreshButtons().execute("");

            Toast.makeText(getApplicationContext(), "Record deleted", Toast.LENGTH_LONG).show();
        }
    } // end AsyncTaks saveFinished

    private <E extends Comparable<E>> List<E> sortListWithoutModifyingOriginalList(List<E> list){
        List<E> newList = new ArrayList<E>(list);
        Collections.sort(newList);
        return newList;
    }




}
