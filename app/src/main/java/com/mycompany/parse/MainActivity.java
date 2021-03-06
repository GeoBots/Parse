package com.mycompany.parse;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mycompany.parse.R;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.widget.Toast.*;

public class MainActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {



    private static final String DIALOG_ERROR = "dialog error";
//    private static final int REQUEST_RESOLVE_ERROR = 1001;
//    private boolean mResolvingError = false;
//    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    protected static final String TAG = "basic-location-sample";
    private GoogleApiClient mGoogleApiClient;

    protected Location mLastLocation;
    protected Location mPreviousLocation;
    protected double mPreviousDistance = -1.0; //a logical fallacy
    protected double mCurrentDistance = -1.0; //a logical fallacy
    protected TextView mLatitudeText;
    protected TextView mLongitudeText;
    protected ShareActionProvider mShareActionProvider;
    protected String mShare;
    protected double mBufferDistance = 50;

    public String passLat;
    public String passLong;
    public Boolean passWinner;
    public Location targetLocation;



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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLatitudeText = (TextView) findViewById((R.id.latitude_text));
        mLongitudeText = (TextView) findViewById((R.id.longitude_text));

        buildGoogleApiClient();

    }

    public void onGameStartClick(View view) {
        StartGame startgame = new StartGame();
        startgame.execute();
        makeText(this, getString(R.string.button_start_msg), LENGTH_SHORT).show();
        Button startBtn= (Button)findViewById(R.id.button_start);
        startBtn.setEnabled(false);
    }

    public void onLocateClick(View view) {
        CheckLocation checklocation = new CheckLocation();
        checklocation.execute();
        makeText(this, getString(R.string.button_locate_msg), LENGTH_SHORT).show();
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


     @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.share).getActionProvider();
    mShareActionProvider.setShareIntent(getDefaultShareIntent());
    return true;
    }

    private Intent getDefaultShareIntent(){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, mShare);
        return intent;
    }

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


    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
            mShare = "I'm at " + String.valueOf(mLastLocation.getLatitude()) + " degrees latitude and " + String.valueOf(mLastLocation.getLongitude()) + " degrees longitude";
        } else {
            makeText(this, R.string.no_location_detected, LENGTH_LONG).show();
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


    /**
     * Represents a geographical location.
     */

    //hider starts game and stores current location as target
    private class StartGame extends AsyncTask<Void, Void, String> {
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

                /* Mongo collection document structure:
                    "GameID": 1,
                    "PlayerID": 0,
                    "TimeInterval: 0,
                    "Latitude": 47.247005,
                    "Longitude": -122.438734,
                    "IsWinner": 0,
                    "Time": "2015-03-14_15:26:00"
                */
                //game_id must increment when new game is started; query db for max game_id
                int game_id = 1;
                //need to determine player_id dynamically or get username
                int player_id = 0;
                //this should increment from user's last location check
                int time_interval = 0;
                //hider cannot win; game is starting
                boolean is_winner = false;

                //insert lat/long
                if (mLastLocation != null) {
                    BasicDBObject lastLocation = new BasicDBObject();
                    lastLocation.put("GameID", game_id);
                    lastLocation.put("PlayerID", player_id);
                    lastLocation.put("TimeInterval", time_interval);
                    lastLocation.put("Latitude", mLastLocation.getLatitude());
                    lastLocation.put("Longitude", mLastLocation.getLongitude());
                    lastLocation.put("IsWinner", is_winner);
                    lastLocation.put("Time", String.valueOf(now));

                    WriteResult result = coll.insert(lastLocation, WriteConcern.SAFE);
                    //Log.i(result.toString());

                    mongoClient.close();

                    //if push notifications work, notify other players of game start
                    //otherwise, use manual sms ;-)

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
    }

    //seekers get hider's location for match calcs later
    private class GetTargetLocation extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {        //periodically check player location and progress

            //get hider's lat/long position from db

            //pass coords to CheckLocation as needed

            return getString(R.string.button_locate_msg);
        }
    }

    //periodically check player location and progress
    private class CheckLocation extends AsyncTask<Void, Void, String> {
        @Override
        public String doInBackground(Void... voids) {

            try {
                //create connection
                String myUri = "mongodb://findme_service:abcde12345@ds043991.mongolab.com:43991/location";
                String myColl = "FindMe253";
                MongoClientURI uri = new MongoClientURI(myUri);
                MongoClient mongoClient = new MongoClient(uri);
                DB db = mongoClient.getDB(uri.getDatabase());
                DBCollection coll = db.getCollection(myColl);

                /*
                Since two records will be written per game, if record set is greater than one game over
                other way iterate through records to determine iswinner true
                filter database for game ID or wipe database every game*/
                //DBObject sort = new BasicDBObject("$natural", "-1");
                DBObject q = new BasicDBObject();



                DBCursor cursor = coll.find(q);//.sort(new BasicDBObject("$natural", -1));



                /*DBCursor cursor = coll.find(q);
                try {
                    while (cursor.hasNext()) {
                        //System.out.println(cursor.next());
                        passLat = String.valueOf(cursor.one().get("Latitude"));
                        passLong = String.valueOf(cursor.one().get("Longitude"));
                        passWinner = Boolean.valueOf(cursor.one().get("IsWinner"));
                    }
                } finally {
                    cursor.close();
                }*/

                //or... DBCursor cursor =

                /*final DBCursor cursor = coll.findOne("{}", sort);
                    ().sort(sort).limit(1);*/



                /*
                BasicDBObject WinnerQuery = new BasicDBObject("IsWinner", true);
                passWinner = WinnerQuery.get("IsWinner");

                BasicDBObject TargetQuery = new BasicDBObject("Latitude", new BasicDBObject("$exists", true));
                passLat = String.valueOf(TargetQuery.get("Latitude"));
                passLong = String.valueOf(TargetQuery.get("Longitude"));
                */
                //Location targetLocation = Double.parseDouble(passlat), Double.parseDouble(passlong);
                //float distance = mLastLocation.distanceTo(targetLocation);
                //cursor.close();
                //mongoClient.close();

                //return "who cares";

                //test value
                passWinner = true;

                //has game been won? check db for winner: is distance is within buffer distance?
                if (passWinner) {
                    //display winner message
                    makeText(MainActivity.this, getString(R.string.winner), LENGTH_LONG).show();

                    //return getString(R.string.winner);
                }
                //else game has not been won, check progress
                else {
                    //it's not this player's first turn if previous location or distance has been set
                    if (mLastLocation != null) {
                        //save previous location before getting current location
                        mPreviousLocation = mLastLocation;

                        //save previous distance to target before updating
                        mPreviousDistance = mCurrentDistance;

                        //get current lat/long position
                        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                        //calculate new distance to target
                        mCurrentDistance = mLastLocation.distanceTo(targetLocation);

                        //check buffer distance for winner and write to the database if won
                        if (mCurrentDistance <= mBufferDistance) {
                            //write to the database, you won!
                            //try {
                                //create connection
                                /*String myUri = "mongodb://findme_service:abcde12345@ds043991.mongolab.com:43991/location";
                                String myColl = "FindMe253";
                                MongoClientURI uri = new MongoClientURI(myUri);
                                MongoClient mongoClient = new MongoClient(uri);
                                DB db = mongoClient.getDB(uri.getDatabase());
                                DBCollection coll = db.getCollection(myColl);*/

                                SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                                String now = time.format(new Date());

                                    /* Mongo collection document structure:
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
                                boolean is_winner = true;

                                //insert lat/long
                                if (mLastLocation != null) {
                                    BasicDBObject lastLocation = new BasicDBObject();
                                    lastLocation.put("GameID", game_id);
                                    lastLocation.put("PlayerID", player_id);
                                    lastLocation.put("TimeInterval", time_interval);
                                    lastLocation.put("Latitude", mLastLocation.getLatitude());
                                    lastLocation.put("Longitude", mLastLocation.getLongitude());
                                    lastLocation.put("IsWinner", is_winner);
                                    lastLocation.put("Time", String.valueOf(now));

                                    WriteResult result = coll.insert(lastLocation, WriteConcern.SAFE);
                                    //Log.i(result.toString());

                                    mongoClient.close();

                                    //display winner message
                                    makeText(MainActivity.this, getString(R.string.winner), LENGTH_LONG).show();

                                    //if push notifications work, notify other players of game start
                                    //otherwise, use manual sms ;-)

                                    return getString(R.string.submit_label); //"@string/submit_label"
                                } else {
                                    mongoClient.close();
                                    return getString(R.string.submit_error); //"@string/submit_error"
                                }

                            //} catch (UnknownHostException e) {
                            //    return getString(R.string.host_error); //"@string/host_error"
                            //}

                            //display winner message
                            //makeText(MainActivity.this, getString(R.string.winner), LENGTH_LONG).show();
                        }
                        //compare current distance from previous to determine hot or cold
                        else if (mCurrentDistance < mPreviousDistance) {
                            //display warmer message
                            makeText(MainActivity.this, getString(R.string.warmer), LENGTH_LONG).show();
                        } else {
                            //display colder message
                            makeText(MainActivity.this, getString(R.string.colder), LENGTH_LONG).show();
                        }
                    }
                    //this is the player's first time checking location
                    else {

                        //try {
                            //get current lat/long position
                            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                            //calculate distance to target
                            mCurrentDistance = mLastLocation.distanceTo(targetLocation);
                            //check if winner
                            if (mCurrentDistance < mBufferDistance) {
                                //write to the database, you won!
                                //try {
                                    //create connection
                                    /*String myUri = "mongodb://findme_service:abcde12345@ds043991.mongolab.com:43991/location";
                                    String myColl = "FindMe253";
                                    MongoClientURI uri = new MongoClientURI(myUri);
                                    MongoClient mongoClient = new MongoClient(uri);
                                    DB db = mongoClient.getDB(uri.getDatabase());
                                    DBCollection coll = db.getCollection(myColl);*/

                                    SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                                    String now = time.format(new Date());

                                    /* Mongo collection document structure:
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
                                    boolean is_winner = true;

                                    //insert lat/long
                                    if (mLastLocation != null) {
                                        BasicDBObject lastLocation = new BasicDBObject();
                                        lastLocation.put("GameID", game_id);
                                        lastLocation.put("PlayerID", player_id);
                                        lastLocation.put("TimeInterval", time_interval);
                                        lastLocation.put("Latitude", mLastLocation.getLatitude());
                                        lastLocation.put("Longitude", mLastLocation.getLongitude());
                                        lastLocation.put("IsWinner", is_winner);
                                        lastLocation.put("Time", String.valueOf(now));

                                        WriteResult result = coll.insert(lastLocation, WriteConcern.SAFE);
                                        //Log.i(result.toString());

                                        mongoClient.close();

                                        //if push notifications work, notify other players of game start
                                        //otherwise, use manual sms ;-)

                                        return getString(R.string.submit_label); //"@string/submit_label"
                                    } else {
                                        mongoClient.close();
                                        return getString(R.string.submit_error); //"@string/submit_error"
                                    }

                                //} catch (UnknownHostException e) {
                                //    return getString(R.string.host_error); //"@string/host_error"
                                //}
                            }

                            makeText(MainActivity.this, getString(R.string.winner), LENGTH_LONG).show();

                        //} catch (UnknownHostException e) {
                        //    return getString(R.string.host_error); //"@string/host_error"
                        //}
                    }
                }
                cursor.close();
                mongoClient.close();
                Toast.makeText(MainActivity.this, getString(R.string.game_id), LENGTH_LONG).show();
                return "3.1415926";
                //error handling
            } catch (UnknownHostException e) {
                return getString(R.string.host_error); //"@string/host_error"
            }
        }
    }
}