package aas.beetclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.IOException;

public class SendFeedback extends AppCompatActivity {

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
        setContentView(R.layout.activity_send_feedback);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Send Feedback");
    }

    public void onBackPressed() {
        finish();
    }

    public void sendFeedback(View view){

//Access shared prefs to load saved sender and recip; save new values
        //   SharedPreferences sharedPref = WorkSummary.this.getPreferences(Context.MODE_PRIVATE);

        //Gets feedback from text editor
        EditText editFeedback = (EditText) findViewById(R.id.edit_feedback);
        String feedback = editFeedback.getText().toString();

        //Gets email address if sender chooses to include
        EditText editFeedbackEmail = (EditText) findViewById(R.id.edit_feedbackEmail);
        String feedbackEmail = "Feedback: " + editFeedbackEmail.getText().toString();
              // String filename = "beetclock_report.csv";

        //Sends feedback directly to me
        String address = "alex@beetclock.com";

        //Inputs for feedback sender
        String[] mailin = {feedbackEmail, address, feedback, "", ""};

        new emailFeedback().execute(mailin);

    }//end sendFeedback


    private class emailFeedback extends AsyncTask<String[], Integer, Boolean> {
        protected Boolean doInBackground(String[]... params) {

            String[] mailin = params[0];
            SendMail sendMail = new SendMail();

            Boolean issuccess = true;
            try {
                sendMail.send(mailin);
            }catch (Exception e) {
                //Log.e("SendMail", e.getMessage(), e);
                issuccess=false;
            }

            return issuccess;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean success) {
            String sentMsg = new String();
            if(success){
                                sentMsg = "Report sent successfully!";
            }else {
                sentMsg = "Error sending report";
            }//end null string else
            Toast.makeText(getApplicationContext(), sentMsg, Toast.LENGTH_SHORT).show();

        }
    }//end AsyncTask emailFeedback


}
