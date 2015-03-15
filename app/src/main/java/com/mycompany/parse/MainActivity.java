package com.mycompany.Parse;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {



    private static final String DIALOG_ERROR = "dialog error";
//    private static final int REQUEST_RESOLVE_ERROR = 1001;
//    private boolean mResolvingError = false;
//    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    protected static final String TAG = "basic-location-sample";
    private GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    protected TextView mLatitudeText;
    protected TextView mLongitudeText;
    protected ShareActionProvider mShareActionProvider;
    protected String mShare;


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }



    private Intent getDefaultShareIntent(){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, mShare);
        return intent;
    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLatitudeText = (TextView) findViewById((R.id.latitude_text));
        mLongitudeText = (TextView) findViewById((R.id.longitude_text));

        buildGoogleApiClient();

    }

    public void onStartClick(View view) {
        PostLocation postlocation = new PostLocation();
        postlocation.execute();
        Toast.makeText(this, getString(R.string.submit_label), Toast.LENGTH_SHORT).show();
    }

    public void onLocateClick(View view) {
        //PostLocation postlocation = new PostLocation();
        //postlocation.execute();
        //Toast.makeText(this, getString(R.string.submit_label), Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "this should check my progress", Toast.LENGTH_SHORT).show();
    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    /** @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.share).getActionProvider();
    mShareActionProvider.setShareIntent(getDefaultShareIntent());
    return true;
    } */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
            mShare = "I'm at " + String.valueOf(mLastLocation.getLatitude())
                    + " degrees Latitude and " + String.valueOf(mLastLocation.getLongitude())
                    + " degrees Longitude";
        }   else {
            Toast.makeText(this, R.string.no_location_detected, Toast.LENGTH_LONG).show();
            mShare = "I have no idea where you are";
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onClick(View v) {

    }

    //Begin Test MongoDB -- ksymer is working here
    /**
     * Represents a geographical location.
     */

    private class PostLocation extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {

            try {
                //create connection
                String myUri = "mongodb://findme_service:abcde12345@ds043991.mongolab.com:43991/location";
                String myColl = "FindMe253";
                MongoClientURI uri = new MongoClientURI(myUri);
                MongoClient mongoClient = new MongoClient(uri);
                DB db = mongoClient.getDB(uri.getDatabase());
                DBCollection coll = db.getCollection(myColl);

                SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                String now = time.format(new Date());

                /* Mongo data structure:
                    "GameID": 1,
                    "PlayerID": 0,
                    "TimeInterval: 0,
                    "Latitude": 47.247005,
                    "Longitude": -122.438734,
                    "IsWinner": 0,
                    "Time": "2015-03-14_15:26:00"
                */
                int game_id = 1;
                int player_id = 0;
                int time_interval = 0;
                int is_winner = 0;

                //insert lat/long
                if (mLastLocation != null) {
                    BasicDBObject lastLocation = new BasicDBObject();
                    lastLocation.put("GameID", game_id);
                    lastLocation.put("PlayerID", player_id);
                    lastLocation.put("TimeInterval", time_interval);
                    lastLocation.put("Latitude", String.valueOf(mLastLocation.getLatitude()));
                    lastLocation.put("Longitude", String.valueOf(mLastLocation.getLongitude()));
                    lastLocation.put("IsWinner", is_winner);
                    lastLocation.put("Time", String.valueOf(now));

                    //coll.insert(lastLocation, WriteConcern.SAFE);

                    WriteResult result = coll.insert(lastLocation, WriteConcern.SAFE);
                    //Log.i(result.toString());

                    mongoClient.close();

                    return getString(R.string.submit_label); //"@string/submit_label"
                }
                else {
                    mongoClient.close();
                    return getString(R.string.submit_error); //"@string/submit_error"
                }

            } catch(UnknownHostException e) {
                return getString(R.string.host_error); //"@string/host_error"
            }
        }
    }//END Test MongoDB



    public class QueryActivity extends Activity {

        protected TextView queryLat;
        protected TextView queryLong;
        protected TextView queryTime;

        public String passLat;
        public String passLong;
        public String passTime;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            QueryLocation queryLocation = new QueryLocation();
            queryLocation.execute();

            queryLat = (TextView) findViewById(R.id.latitude_text);
            queryLong = (TextView) findViewById(R.id.longitude_text);
            //queryTime = (TextView) findViewById(R.id.time_query);

        }


        /*@Override
        public boolean onCreateOptionsMenu(Menu menu) {
         Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_query, menu);
        return true;
    }*/

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                return true;
            }

            return super.onOptionsItemSelected(item);
        }


        private class QueryLocation extends AsyncTask<Void, Void, String> {
            @Override
            protected String doInBackground(Void... voids) {

                try {
                    //create connection
                    String myUri = "mongodb://joetest:abcde12345@ds043991.mongolab.com:43991/location";
                    String myColl = "MyLatLng";
                    MongoClientURI uri = new MongoClientURI(myUri);
                    MongoClient mongoClient = new MongoClient(uri);
                    DB db = mongoClient.getDB(uri.getDatabase());
                    DBCollection coll = db.getCollection(myColl);

                /*
                DBObject sort = new BasicDBObject("$natural", "-1");
                DBObject q = new BasicDBObject();*/

                    DBObject cursor = coll.findOne();

                    //or... DBCursor cursor =

                /*final DBCursor cursor = coll.findOne("{}", sort);
                    ().sort(sort).limit(1);*/

                    passLat = String.valueOf(cursor.get("Latitude"));
                    passLong = String.valueOf(cursor.get("Longitude"));
                    passTime = String.valueOf(cursor.get("Time"));
                    mongoClient.close();

                    return "who cares";

                } catch(UnknownHostException e) {
                    return getString(R.string.host_error); //"@string/host_error"
                }
            }

            @Override
            protected void onPostExecute(String result) {
                queryLat.setText(passLat);
                queryLong.setText(passLong);
                queryTime.setText(passTime);
            }

        }
    }
}
