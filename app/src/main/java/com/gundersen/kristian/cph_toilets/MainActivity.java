package com.gundersen.kristian.cph_toilets;



import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.android.gms.maps.GoogleMap;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    /*
    private TextView jsonOutput;
    private Button btnFetchJson;
    */
    private JSONObject geoJson;
    private GoogleMap globalGoogleMap;
    private SupportMapFragment supportMapFragment;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //getSupportActionBar().setDisplayShowTitleEnabled(false);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);



        supportMapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap));
        supportMapFragment.getMapAsync(this);


        /*
        jsonOutput = (TextView) findViewById(R.id.txtJsonOut);
        btnFetchJson = (Button) findViewById(R.id.btnFetchJson);
        btnFetchJson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new fetchJsonAsync().execute();
            }
        });
        */

        initJsonAsync();
    }

    //SETTING UP THE MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.info:
                runInfo();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void runInfo() {
        Logger.getAnonymousLogger().info("--> runinfo()");
        Intent intent = new Intent(this,AppInfo.class);
        startActivity(intent);
    }


    public void initJsonAsync(){

        //INITIALIZE THE ASYNC INTO AN INDEPENDENT FUNCTION
        new fetchJsonAsync().execute();

    }


    public void onMapReady (GoogleMap googleMap) {
        globalGoogleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getMinZoomLevel();
        ////// NEXT LINE IS A PLACEHOLDER. It should be generated from the user´s position. the last arg is zoom level.
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(55.6815890,12.5290920), 12.0f));

    }


    public class fetchJsonAsync extends AsyncTask<Void, String, JSONObject> {

        HttpURLConnection urlConnection;
        /* private ProgressDialog spinner;

        public fetchJsonAsync(json_display activity) {
            spinner = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            spinner.setMessage("Please wait...");
            spinner.setCancelable(true);
            spinner.show();

        }
        */

        @Override
        protected JSONObject doInBackground(Void... args) {

            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL("http://wfs-kbhkort.kk.dk/k101/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=k101:toilet&outputFormat=json&SRSNAME=EPSG:4326");
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                geoJson =  new JSONObject(result.toString());

            }catch( Exception e) {
                e.printStackTrace();
            }
            finally {
                urlConnection.disconnect();
            }


            return geoJson;
        }

        @Override
        protected void onPostExecute(JSONObject geoJson) {

            //jsonOutput.setText(geoJson.toString());
            if (globalGoogleMap != null) {

                GeoJsonLayer layer = new GeoJsonLayer(globalGoogleMap, geoJson);
                layer.addLayerToMap();
            }
            else {
                Toast toast =new Toast(getApplicationContext());
                toast.setText("Map not ready yet");
                toast.show();
            }

        }

    }
}
