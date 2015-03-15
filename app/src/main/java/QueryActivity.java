/*
package com.mycompany.parse;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;


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

        queryLat = (TextView) findViewById(R.id.latitude_query);
        queryLong = (TextView) findViewById(R.id.longitude_query);
        queryTime = (TextView) findViewById(R.id.time_query);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_query, menu);
        return true;
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

                */
/*
                DBObject sort = new BasicDBObject("$natural", "-1");
                DBObject q = new BasicDBObject();*//*


                DBObject cursor = coll.findOne();

                //or... DBCursor cursor =

                */
/*final DBCursor cursor = coll.findOne("{}", sort);
                    ().sort(sort).limit(1);*//*


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
*/
