package aas.beetclock;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;



import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.services.script.model.*;
import java.util.Map;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DoScriptExecute extends AppCompatActivity { //previously just an activity
    GoogleAccountCredential mCredential;
    private TextView mOutputText;
    ProgressDialog mProgress;

    public static final String KEY_RESPONSE = "aas.beetclock.DoScriptExecute.response";
    public static final String KEY_IDS = "aas.beetclock.DoScriptExecute.ids";

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    //Can add any necessary permissions here
    private static final String[] SCOPES = { "https://www.googleapis.com/auth/drive", "https://www.googleapis.com/auth/spreadsheets" };


    /**
     * Create the main activity.
     * // @param savedInstanceState previously saved instance data.
     */
    @Override
        protected void onCreate(Bundle savedInstanceState) {

        //Enables Strict Mode testing
        /*
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                        //.penaltyDialog()
                .penaltyFlashScreen()
                .build());
                */


        super.onCreate(savedInstanceState);
        LinearLayout activityLayout = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        activityLayout.setLayoutParams(lp);
        activityLayout.setOrientation(LinearLayout.VERTICAL);
        activityLayout.setPadding(16, 16, 16, 16);

        ViewGroup.LayoutParams tlp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        mOutputText = new TextView(this);
        mOutputText.setLayoutParams(tlp);
        mOutputText.setPadding(16, 16, 16, 16);
        mOutputText.setVerticalScrollBarEnabled(true);
        mOutputText.setMovementMethod(new ScrollingMovementMethod());
        activityLayout.addView(mOutputText);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google Apps Script Execution API ...");

        setContentView(activityLayout);

        // Initialize credentials and service object.
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
    }

    /**
     * Extend the given HttpRequestInitializer (usually a credentials object)
     * with additional initialize() instructions.
     *
     * @param requestInitializer the initializer to copy and adjust; typically
     *         a credential object.
     * @return an initializer with an extended read timeout.
     */
    private static HttpRequestInitializer setHttpTimeout(
            final HttpRequestInitializer requestInitializer) {
        return new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest httpRequest)
                    throws java.io.IOException {
                requestInitializer.initialize(httpRequest);
                // This allows the API to call (and avoid timing out on)
                // functions that take up to 6 minutes to complete (the maximum
                // allowed script run time), plus a little overhead.
                httpRequest.setReadTimeout(380000);
            }
        };
    }
    /**
     * Called whenever this activity is pushed to the foreground, such as after
     * a call to onCreate().
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            refreshResults();
        } else {
            mOutputText.setText("Google Play Services required: " +
                    "after installing, close and relaunch this app.");
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        }


    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mCredential.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                    }
                } else if (resultCode == RESULT_CANCELED) {

                    finish();
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode != RESULT_OK) {
                    chooseAccount();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Attempt to get a set of data from the Google Apps Script Execution API to display. If the
     * email address isn't known yet, then call chooseAccount() method so the
     * user can pick an account.
     */
    private void refreshResults() {
        if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            if (isDeviceOnline()) {

                //*********************************** run after successful login **************************************************

                //Passing an List<String[]> consisting of basic parameters, a list of jobs, and a list of times
                Intent intent = getIntent();
                String[] params = intent.getStringArrayExtra(PopulateSheet.KEY_PARAMS);
                String[] jobs = intent.getStringArrayExtra(PopulateSheet.KEY_JOBS);
                String[] times = intent.getStringArrayExtra(PopulateSheet.KEY_TIMES);
                String[] equip = intent.getStringArrayExtra(PopulateSheet.KEY_EQUIP);
                List<String[]> allParams = new ArrayList<>();
                allParams.add(params);
                allParams.add(jobs);
                allParams.add(equip);
                allParams.add(times);

                new MakeRequestTask(mCredential).execute(allParams);
            } else {
                mOutputText.setText("No network connection available.");
            }
        }
    }

    /**
     * Starts an activity in Google Play Services so the user can pick an
     * account.
     */
    private void chooseAccount() {
        startActivityForResult(
                mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date. Will
     * launch an error dialog for the user to update Google Play Services if
     * possible.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
            return false;
        }
        return true;
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                connectionStatusCode,
                DoScriptExecute.this,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }


//I am calling a new script with the pair URL, WRITE as a List<String>
    /**
     * An asynchronous task that handles the Google Apps Script Execution API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    //private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
    private class MakeRequestTask extends AsyncTask<List<String[]>, Void, List<String[]>> { //Adding input parameters to the app script
         private com.google.api.services.script.Script mService = null;
        private Exception mLastError = null;

        public MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.script.Script.Builder(
                    transport, jsonFactory, setHttpTimeout(credential))
                    //.setApplicationName("Google Apps Script Execution API Android Quickstart")
                    .setApplicationName("BeetClock")
                    .build();
        }

        /**
         * Background task to call Google Apps Script Execution API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String[]> doInBackground(List<String[]>... params) {

            List<String[]> inputs = params[0];
            try {
                //return getDataFromApi();
                return getDataFromApi(inputs);
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Call the API to run an Apps Script function that returns a list
         * of folders within the user's root directory on Drive.
         *
         * @return list of String folder names and their IDs
         * @throws IOException
         */
        private List<String[]> getDataFromApi(List<String[]> inputs)
                throws IOException, GoogleAuthException {
            // ID of the script to call. Acquire this from the Apps Script editor,
            // under Publish > Deploy as API executable.
//I want to capture parameters only, for use in the get file and get sheets functions, and parameters + time values for use in populate
            String[] parameters = inputs.get(0);

            //List<String[]> paramValues = new ArrayList<>();
            //paramValues.add(parameters);
            //paramValues.add(values);
            // ******************************************** call api request *******************************************
            //String scriptId = "MRVrQ4vZSFC9u4g-YerjLXFvJnrnB_frN";
            String scriptId = "MfVLV7-wvOq7fF2Olzx_gDVvJnrnB_frN";

            List<String> outList = new ArrayList<String>();
            List<String> idList = new ArrayList<String>();

            //Parameters must be added as a List<Object>
            //Lets try adding the entire array as one object
            List<Object> requestParams = new ArrayList<Object>();
           // for (int i = 0; i < inputs.length; i++) {
            if (parameters[0].equals("getFiles")) {
                requestParams.add(parameters);
            } else if (parameters[0].equals("getSheets")) {
                requestParams.add(parameters);
            } else if (parameters[0].equals("getEquip")) {
                requestParams.add(parameters);
            } else if (parameters[0].equals("popSheet")) {

                String[] values = inputs.get(3);
                String[] equip = inputs.get(2);
                //Passing an array with parameters followed by input values
                List<String> paramList = new ArrayList<>();
                for (int i = 0; i < parameters.length; i++) {
                    paramList.add(parameters[i]);
                }
                for (int j = 0; j < values.length; j++) {
                    paramList.add(values[j]);
                }
                for (int k = 0; k < equip.length; k++) {
                    paramList.add(equip[k]);
                }
                String[] paramValues = paramList.toArray(new String[0]);
                System.out.println("Params length:");
                System.out.println(paramValues.length);
                for (int i = 0; i < paramValues.length; i++) {
                    System.out.println(paramValues[i]);
                }

                requestParams.add(paramValues);
            }//end if popSheet

                //requestParams.add(inputs[i]);
           // }

//Select function based on first input
            String function = "";
            if (parameters[0].equals("getFiles")) {
                function = "getFilesUnderRoot";
            } else if (parameters[0].equals("getSheets")) {
                function = "sheetNames";
            } else if (parameters[0].equals("popSheet")) {
                function = "popSheet";
            } else if (parameters[0].equals("getEquip")) {
                function = "getEquip";
            }

            // Create an execution request object.
            ExecutionRequest request = new ExecutionRequest()
                    //.setFunction("getFoldersUnderRoot"); //Defualt execution script
             .setFunction(function) //Defualt execution script
             .setParameters(requestParams);


            // Make the request.
            Operation op =
                    mService.scripts().run(scriptId, request).execute();

            // Print results of request.
            if (op.getError() != null) {
                throw new IOException(getScriptError(op));
            }
            if (op.getResponse() != null &&
                    op.getResponse().get("result") != null) {
                // The result provided by the API needs to be cast into
                // the correct type, based upon what types the Apps Script
                // function returns. Here, the function returns an Apps
                // Script Object with String keys and values, so must be
                // cast into a Java Map (folderSet).

                Map<String, String> folderSet =
                        (Map<String, String>)(op.getResponse().get("result"));

                for (String id: folderSet.keySet()) {
                    outList.add(
                            //String.format("%s (%s)", folderSet.get(id), id));
                           folderSet.get(id) );
                    idList.add( id );
                }

                //In the revised function I am returning a List<String> directly

                //folderList = (List<String>)(op.getResponse().get("result"));
            }
        String[] outArray = outList.toArray(new String[0]);
        String[] idArray = idList.toArray(new String[0]);
            List<String[]> outputs = new ArrayList<>();
            outputs.add(outArray);
            outputs.add(idArray);
            return outputs;
        }

        /**
         * Interpret an error response returned by the API and return a String
         * summary.
         *
         * @param op the Operation returning an error response
         * @return summary of error response, or null if Operation returned no
         *     error
         */
        private String getScriptError(Operation op) {
            if (op.getError() == null) {
                return null;
            }

            // Extract the first (and only) set of error details and cast as a Map.
            // The values of this map are the script's 'errorMessage' and
            // 'errorType', and an array of stack trace elements (which also need to
            // be cast as Maps).
            Map<String, Object> detail = op.getError().getDetails().get(0);
            List<Map<String, Object>> stacktrace =
                    (List<Map<String, Object>>)detail.get("scriptStackTraceElements");

            java.lang.StringBuilder sb =
                    new StringBuilder("\nScript error message: ");
            sb.append(detail.get("errorMessage"));

            if (stacktrace != null) {
                // There may not be a stacktrace if the script didn't start
                // executing.
                sb.append("\nScript error stacktrace:");
                for (Map<String, Object> elem : stacktrace) {
                    sb.append("\n  ");
                    sb.append(elem.get("function"));
                    sb.append(":");
                    sb.append(elem.get("lineNumber"));
                }
            }
            sb.append("\n");
            return sb.toString();
        }


        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String[]> output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
               mOutputText.setText("No results returned.");

                //Set successful return with no extras if there is no output (as in popSheet)
               // Intent returnIntent = new Intent();
               // setResult(RESULT_OK, returnIntent);
               // finish();
            } else {
            //***************************************************Return data ***************************************************

                //output.add(0, "Data retrieved using the Google Apps Script Execution API:");
                //mOutputText.setText(TextUtils.join("\n", output));
                //String[] outArray = output.toArray(new String[0]);
                String[] outArray = output.get(0);
                String[] idArray = output.get(1);
                //Rather than printing output, attach it as an extra
                Intent returnIntent = new Intent();
                returnIntent.putExtra(KEY_RESPONSE, outArray);
                returnIntent.putExtra(KEY_IDS, idArray);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            DoScriptExecute.REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }// end AsyncTask

    public void onDestroy(){
        super.onDestroy();


    }
}