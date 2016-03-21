package aas.beetclock;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.ScrollView;
import android.content.Intent;

public class DisplaySummary extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_summary);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Summary");

        //Enables Strict Mode testing
        /*
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                        //.penaltyDialog()
                .penaltyFlashScreen()
                .build());
                */

        //Run on create

        Intent intent = getIntent();
        String summary = intent.getStringExtra(WorkSummary.DISPLAY_SUMMARY);

        TextView textView = new TextView(this);
        textView.setTextSize(16);
        textView.setText(summary);
        ScrollView msgView = (ScrollView) findViewById(R.id.display_summary);
        msgView.removeAllViews();
        msgView.addView(textView);


    }
    public void onBackPressed() {
        finish();
    }

}
