package aas.beetclock;

/*
Adapted with modifications from code by HMKCode
http://hmkcode.com/android-simple-sqlite-database-tutorial/
*/


import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;

public class SQLiteHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 12;
    // Database Name
    private static final String DATABASE_NAME = "BeetDB";

    // Table names
    private static final String TABLE_CROPS = "crops";
    private static final String TABLE_JOBS = "jobs";
    private static final String TABLE_TIMES = "timestamps";
    private static final String TABLE_MACHINE = "machinery";
    private static final String TABLE_CURRENT = "current";

    //Crops table
    private static final String KEY_ID = "id";
    private static final String KEY_CROP = "crop";
    //Jobs table
    //Times table
    private static final String KEY_TIME = "time";
    private static final String KEY_ELAPSED = "elapsed";
    private static final String KEY_JOB = "job";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_WORKERS = "workers";
    //Machinery table
    private static final String KEY_MACHINE = "machine";




    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creating a table for storing crops
        String CREATE_CROP_TABLE = "CREATE TABLE crops ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "crop TEXT )";//+

        // Creating a table for storing crops
        String CREATE_JOB_TABLE = "CREATE TABLE jobs ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "category TEXT, " +
                "job TEXT )";//

        String CREATE_MACHINE_TABLE = "CREATE TABLE machinery ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "machine TEXT)";

        String CREATE_TIME_TABLE = "CREATE TABLE timestamps ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "crop TEXT, "+
                "time INTEGER, "+
                "elapsed INTEGER, " +
                "category TEXT, " +
                "job TEXT, " +
                "machine TEXT)";

        String CREATE_CURRENT_TABLE = "CREATE TABLE current ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "crop TEXT, "+
                "time INTEGER, "+
                "category TEXT, " +
                "job TEXT, " +
                "machine TEXT, " +
                "workers TEXT)";

        // create crops table and time table
        db.execSQL(CREATE_CROP_TABLE);
        db.execSQL(CREATE_JOB_TABLE);
        db.execSQL(CREATE_TIME_TABLE);
        db.execSQL(CREATE_MACHINE_TABLE);
        db.execSQL(CREATE_CURRENT_TABLE);

        //Adding default jobs to the job table at database creation
        String[] defaultCats = {"Soil prep","Soil prep","Soil prep","Soil prep","Soil prep","Soil prep","Soil prep",
                "Seeding/Transplant","Seeding/Transplant",
                "Cultivation","Cultivation","Cultivation","Cultivation","Cultivation","Cultivation","Cultivation","Cultivation","Cultivation",
                "Harvest","Harvest",
                "Post-harvest","Post-harvest","Post-harvest","Post-harvest"
        };
        String[] defaultjobs = {"Disk","Chisel","Rototill","Bedform","Spread Fertilizer","Spread manure/compost","Lay plastic/drip",
                "Seed in field","Transplant",
                "Cover/uncover","Hoe","Handweed","Straw mulch","Irrigate","Tractor cultivate","Sidedress","Spray","Flame weed",
                "Harvest", "Wash/pack",
                "Mow crop","Remove mulch","Disk","Sow cover crop"
        };

        for (int i = 0; i < defaultjobs.length; i++) {
            String cat = defaultCats[i];
            String job = defaultjobs[i];
// Unsure if I need to create a new ContentValues instance for each new entry, or if I can do batch entry.  Get this working then try batch
            // each crop as content value
            ContentValues values = new ContentValues();
            values.put(KEY_CATEGORY, cat); // get title
            values.put(KEY_JOB, job);
            // Insert into jobs table
            db.insert(TABLE_JOBS,  null, values);
        }// end for

        //Adding default crops to the crop table at database creation
        String[] defaultcrops = {"Beans","Beets","Broccoli","Brussels","Carrots","Cauliflower","Celery","Cabbage","Corn",
                "Cucumbers","Eggplant","Kale","Leeks","Lettuce","Melons","Okra","Onions","Peas","Peppers","Potatoes","Pumpkins", "Radishes",
                "Spinach","Summer Squash","Sweet Potatoes","Swiss Chard","Tomatoes","Turnips","Winter Squash"};

        for (int i = 0; i < defaultcrops.length; i++) {
            String crop = defaultcrops[i];
            ContentValues values = new ContentValues();
            values.put(KEY_CROP, crop);
            db.insert(TABLE_CROPS,  null, values);
        }// end for

        //Adding default machinery at db creation

        String[] defaultmachines = {"Tractor 1","Tractor 2","Tiller 1","Tiller 2"};
        for (int i = 0; i < defaultmachines.length; i++) {
            String machine = defaultmachines[i];
            ContentValues mech = new ContentValues();
            mech.put(KEY_MACHINE, machine);
            db.insert(TABLE_MACHINE,  null, mech);
        }

    }//end oncreate


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if they exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CROPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIMES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_JOBS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MACHINE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CURRENT);
        // Create tables again
        onCreate(db);
    }


    /**
     * Create Read and Delete operations; don't currently need an Update option*********************
     */


    //**********************************TABLE_TIMES*************************************************

    public void addTime(List<String> timestamp) {
        // Reference the database
        SQLiteDatabase db = this.getWritableDatabase();

        //Parse timestamp into five strings: Crop, Time, Elapsed, Job (list elements 0 - 3)
        String crop = timestamp.get(0);
        //This retrieves values from the list as strings but immediately parses into longs
        long time = Long.parseLong(timestamp.get(1));
        long elapsed = Long.parseLong(timestamp.get(2));
        String jobCat = timestamp.get(3);

        //Split category and job into separate strings
        String[] parts = jobCat.split(": ");
        String category = parts[0];
        String job = parts[1];

        String machinery = timestamp.get(4);

        //Content values get sent to the table in a package
        ContentValues values = new ContentValues();
        values.put(KEY_CROP, crop);
        values.put(KEY_TIME, time);
        values.put(KEY_ELAPSED, elapsed);
        values.put(KEY_CATEGORY, category);
        values.put(KEY_JOB, job);
        values.put(KEY_MACHINE, machinery);

        // insert into db
        db.insert(TABLE_TIMES, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values
        // close
        db.close();
    } //End add current

    // Get crops from all entries in Times table
    public List<String> getCrops() {

        List<String> entries = new ArrayList<String>();

        String query = "SELECT  " + KEY_CROP + " FROM "+TABLE_TIMES;

        //Reference the database
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        //Go through each row adding data to the crops arraylist
        String crop = null;
        if (cursor.moveToFirst()) {
            do {
                //Retrieves query result as a string
                crop = cursor.getString(0);
                entries.add(crop);

            } while (cursor.moveToNext());
        }

        // close
        db.close();
        cursor.close();

        return entries;

    } // End get crops


    // Get times from all entries in Times table
    public List<Long> getTimes() {

        List<Long> entries = new ArrayList<Long>();

        String query = "SELECT  " + KEY_TIME + " FROM "+TABLE_TIMES;

        //Reference the database
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        Long time = null;
        if (cursor.moveToFirst()) {
            do {
                time = cursor.getLong(0);
                entries.add(time);
            } while (cursor.moveToNext());
        }

        // close
        db.close();
        cursor.close();

        return entries;
    } // End get times


    // Get elapsed times from all entries in Times table
    public List<Long> getElapsed() {

        List<Long> entries = new ArrayList<Long>();

        String query = "SELECT  " + KEY_ELAPSED + " FROM "+TABLE_TIMES;

        //Reference the database
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        Long elapsed = null;

        if (cursor.moveToFirst()) {
            do {
                elapsed = cursor.getLong(0);
                entries.add(elapsed);
            } while (cursor.moveToNext());
        }

        // close
        db.close();
        cursor.close();

        return entries;
    } // End get elapsed


    // Get machinery from all entries in Times table
    public List<String> getMachine() {

        List<String> entries = new ArrayList<String>();

        String query = "SELECT  " + KEY_MACHINE + " FROM "+TABLE_TIMES;

        //Reference the database
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        String elapsed = null;

        if (cursor.moveToFirst()) {
            do {
                elapsed = cursor.getString(0);
                entries.add(elapsed);
            } while (cursor.moveToNext());
        }

        // close
        db.close();
        cursor.close();

        return entries;
    } // End get machinery

    // Get jobs from all entries in Times table
    public List<String> getJobs() {

        List<String> jobs = new LinkedList<String>();
        String query = "";
        query = "SELECT  * FROM " + TABLE_TIMES;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        String job = null;
        if (cursor.moveToFirst()) {
            do {
                job = cursor.getString(4) + ": " + cursor.getString(5);
                jobs.add(job);
            } while (cursor.moveToNext());
        }

        // close
        db.close();
        cursor.close();

        return jobs;
    } // End get jobs

    // Delete all time records
    public void deleteTimes() {

        SQLiteDatabase db = this.getWritableDatabase();
        //Delete all rows in the table
        db.delete(TABLE_TIMES, null, null);
        db.close();

    } // end deleteTimes


    //********************************************TABLE_CURRENT************************************

    public void addCurrent(List<String> timestamp) {
        // Reference the database
        SQLiteDatabase db = this.getWritableDatabase();

        //Parse timestamp into five strings: Crop, Time, Elapsed, Job (list elements 0 - 3)
        String crop = timestamp.get(0);
        //This retrieves values from the list as strings but immediately parses into longs
        long time = Long.parseLong(timestamp.get(1));
        String jobCat = timestamp.get(2);

        //Split category and job into separate strings
        String[] parts = jobCat.split(": ");
        String category = parts[0];
        String job = parts[1];

        String machinery = timestamp.get(3);
        long workers = Long.parseLong(timestamp.get(4));

        //Content values get sent to the table in a package
        ContentValues values = new ContentValues();
        values.put(KEY_CROP, crop);
        values.put(KEY_TIME, time);
        values.put(KEY_CATEGORY, category);
        values.put(KEY_JOB, job);
        values.put(KEY_MACHINE, machinery);
        values.put(KEY_WORKERS, workers);

        // insert into db
        db.insert(TABLE_CURRENT, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values
        // close
        db.close();
    } //End add current

    public List<Long> getCurrentTimes() {
        List<Long> entries = new ArrayList<Long>();
        String query = "SELECT  " + KEY_TIME + " FROM "+TABLE_CURRENT;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Long time = null;
        if (cursor.moveToFirst()) {
            do {
                time = cursor.getLong(0);
                entries.add(time);
            } while (cursor.moveToNext());
        }
        // close
        db.close();
        cursor.close();

        return entries;
    } // End get current time

    public List<String> getCurrentJobs() {
        List<String> jobs = new LinkedList<String>();
        String query = "";
        query = "SELECT  * FROM " + TABLE_CURRENT;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        String job = null;
        if (cursor.moveToFirst()) {
            do {
                job = cursor.getString(3) + ": " + cursor.getString(4);
                jobs.add(job);
            } while (cursor.moveToNext());
        }
        // close
        db.close();
        cursor.close();

        return jobs;
    } // End get jobs

    public List<String> getCurrentCrops() {
        List<String> entries = new ArrayList<String>();
        String query = "SELECT  " + KEY_CROP + " FROM "+TABLE_CURRENT;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        String crop = null;
        if (cursor.moveToFirst()) {
            do {
                crop = cursor.getString(0);
                entries.add(crop);
            } while (cursor.moveToNext());
        }
        // close
        db.close();
        cursor.close();

        return entries;
    } // End get current time

    public List<String> getCurrentMachinery() {
        List<String> entries = new ArrayList<String>();
        String query = "SELECT  " + KEY_MACHINE + " FROM "+TABLE_CURRENT;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        String mech = null;
        if (cursor.moveToFirst()) {
            do {
                mech = cursor.getString(0);
                entries.add(mech);
            } while (cursor.moveToNext());
        }
        // close
        db.close();
        cursor.close();

        return entries;
    } // End get current time

    public List<Long> getCurrentWorkers() {
        List<Long> entries = new ArrayList<>();
        String query = "SELECT  " + KEY_WORKERS + " FROM "+TABLE_CURRENT;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Long workers = null;
        if (cursor.moveToFirst()) {
            do {
                workers = cursor.getLong(0);
                entries.add(workers);
            } while (cursor.moveToNext());
        }
        // close
        db.close();
        cursor.close();

        return entries;
    } // End get current time


    // Delete current entries matching searchstring, else delete ALL entries
    public void deleteCurrent(String searchStr) {
        SQLiteDatabase db = this.getWritableDatabase();

        //First I need to get the IDs associated with each ordered process
        List<Long> entries = new ArrayList<Long>();
        String query = "SELECT  " + KEY_ID + " FROM "+TABLE_CURRENT;
        Cursor cursor = db.rawQuery(query, null);
        Long id = null;
        if (cursor.moveToFirst()) {
            do {
                id = cursor.getLong(0);
                entries.add(id);
            } while (cursor.moveToNext());
        }
//Now delete record with unique ID associated with process number (i.e. index # in sheet)
        if (searchStr != null && !searchStr.isEmpty() && !searchStr.equals("null")) {
            int searchInt = Integer.parseInt(searchStr);
            long idLong = entries.get(searchInt);
            String idString = Long.toString(idLong);
            db.delete(TABLE_CURRENT, KEY_ID + "='" + idString + "'", null);
        } else {
             db.delete(TABLE_CURRENT, null, null);
        }
        //close
        db.close();
        cursor.close();
    } // end delete joblist


    //********************************************LIST TABLES************************************

    //Add a new job to Table_Jobs
    public void addJobList(String[] entry) {
        String category = entry[0];
        String job = entry[1];
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_CATEGORY, category);
        values.put(KEY_JOB, job);
        db.insert(TABLE_JOBS,
                null,
                values);
        db.close();
    } // End add croplist


    // Get all jobs from Table_Jobs
    public List<String> getJobList() {
        List<String> jobs = new LinkedList<String>();
        String query = "";
            query = "SELECT  * FROM " + TABLE_JOBS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        String job = null;
        if (cursor.moveToFirst()) {
            do {
                job = cursor.getString(1) + ": " + cursor.getString(2);
                jobs.add(job);
            } while (cursor.moveToNext());
        }
        // close
        db.close();
        cursor.close();

        return jobs;
    }// end get joblist




    // Delete jobs matching search string (otherwise delete all)
    public void deleteJobList(String searchStr) {
        SQLiteDatabase db = this.getWritableDatabase();

        //The searchStr will contain a Category: job pair; I need to trim that to just the job
        String[] parts = searchStr.split(": ");
        String trimJob = parts[1];

        if (trimJob != null && !trimJob.isEmpty() && !trimJob.equals("null")) {
            db.delete(TABLE_JOBS, KEY_JOB + "='" + trimJob + "'", null);
        } else {
           // db.delete(TABLE_JOBS, null, null);
        }
        db.close();
    } // end delete joblist






    //Add a new crop to Table_Crops
    public void addCropList(String crop) {

        // Reference the database
        SQLiteDatabase db = this.getWritableDatabase();

        // Package new crop as content value
        ContentValues values = new ContentValues();
        values.put(KEY_CROP, crop); // get title
        //   values.put(KEY_AUTHOR, book.getAuthor()); // get author

        // Insert into crops table
        db.insert(TABLE_CROPS, // table
                null, //nullColumnHack
                values); //

        db.close();
    } // End add croplist


    // Get all or selected crops from Table_Crops
    public List<String> getCropList(String searchStr) {

        List<String> crops = new LinkedList<String>();

        //If a search string is available, searching all items containing that string
        String query = "";
        if (searchStr != null && !searchStr.isEmpty() && !searchStr.equals("null")) {
            query = "SELECT  * FROM " + TABLE_CROPS + " WHERE "+KEY_CROP+"=\""+ searchStr + "\"";
        } else {
            query = "SELECT  * FROM " + TABLE_CROPS;
        }

        // Reference database
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // Go over each row and add it to list
        String crop = null;
        if (cursor.moveToFirst()) {
            do {
                //Retrieves query result as a string
                crop = cursor.getString(1);
                //Adds string to list
                crops.add(crop);
            } while (cursor.moveToNext());
        }
        // close
        db.close();
        cursor.close();

        return crops;
    }// end get croplist

    // Delete crops matching search string (otherwise delete all)
    public void deleteCropList(String searchStr) {

        SQLiteDatabase db = this.getWritableDatabase();

        //Delete entire table or delete only rows matching search string if search string is not null
        if (searchStr != null && !searchStr.isEmpty() && !searchStr.equals("null")) {
            db.delete(TABLE_CROPS, KEY_CROP + "='" + searchStr + "'", null);
        } else {
            // db.delete(TABLE_CROPS, null, null);
        }

        db.close();
    } // end delete croplist


    //Add a new crop to Table_Crops
    public void addMachineList(String machine) {

        // Reference the database
        SQLiteDatabase db = this.getWritableDatabase();

        // Package new crop as content value
        ContentValues values = new ContentValues();
        values.put(KEY_MACHINE, machine); // get title
        //   values.put(KEY_AUTHOR, book.getAuthor()); // get author

        // Insert into crops table
        db.insert(TABLE_MACHINE, // table
                null, //nullColumnHack
                values); //

        db.close();
    } // End add croplist


    // Get all or selected machines from Table_Crops
    public List<String> getMachineList(String searchStr) {

        List<String> mechs = new LinkedList<String>();

        //If a search string is available, searching all items containing that string
        String query = "";
        if (searchStr != null && !searchStr.isEmpty() && !searchStr.equals("null")) {
            query = "SELECT  * FROM " + TABLE_MACHINE + " WHERE "+KEY_MACHINE+"=\""+ searchStr + "\"";
        } else {
            query = "SELECT  * FROM " + TABLE_MACHINE;
        }

        // Reference database
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // Go over each row and add it to list
        String mech = null;
        if (cursor.moveToFirst()) {
            do {
                //Retrieves query result as a string
                mech = cursor.getString(1);
                //Adds string to list
                mechs.add(mech);
            } while (cursor.moveToNext());
        }
        // close
        db.close();
        cursor.close();

        return mechs;
    }// end get croplist

    // Delete crops matching search string (otherwise delete all)
    public void deleteMachineList(String searchStr) {

        SQLiteDatabase db = this.getWritableDatabase();

        //Delete entire table or delete only rows matching search string if search string is not null
        if (searchStr != null && !searchStr.isEmpty() && !searchStr.equals("null")) {
            db.delete(TABLE_MACHINE, KEY_MACHINE + "='" + searchStr + "'", null);
        } else {
            // db.delete(TABLE_CROPS, null, null);
        }
        db.close();
    } // end delete croplist

}