package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    TextView lat, longitude, statcoord, statloc, loc, country, current, state, desc, input;
    TextView max, min, hum, statmax, statmin, stathum, statinfo, statfeel, statcloud, feel, cloud;
    Switch aswitch;
    ImageView pic;
    public boolean check;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initial startup, setting IDs for later on
        aswitch = (Switch)findViewById(R.id.FC);
        lat = (TextView)findViewById(R.id.lat);
        longitude = (TextView)findViewById(R.id.longitude);
        statcoord = (TextView)findViewById(R.id.staticCoords);
        statloc = (TextView)findViewById(R.id.staticLocation);
        loc = (TextView)findViewById(R.id.loc);
        pic = (ImageView)findViewById(R.id.imageView2);
        country = (TextView)findViewById(R.id.country);
        current = (TextView)findViewById(R.id.current);
        state = (TextView)findViewById(R.id.state);
        desc = (TextView)findViewById(R.id.desc);
        max = (TextView)findViewById(R.id.max);
        min = (TextView)findViewById(R.id.min);
        hum = (TextView)findViewById(R.id.hum);
        statmax = (TextView)findViewById(R.id.staticmax);
        statmin = (TextView)findViewById(R.id.staticmin);
        stathum = (TextView)findViewById(R.id.statichum);
        statinfo = (TextView)findViewById(R.id.staticinfo);
        statfeel = (TextView)findViewById(R.id.staticfeel);
        statcloud = (TextView)findViewById(R.id.staticcloud);
        feel = (TextView)findViewById(R.id.feel);
        cloud = (TextView)findViewById(R.id.cloud);
        input = (EditText)findViewById(R.id.input);
        input.setText("");
    }

    private static HttpURLConnection connection;
    //method called when button is pressed
    public void start(View v){

        //Hides the soft keyboard after user is done and needs to see results
        hideSoftKeyboard(this);

        //Gather whats typed into search bar and save it in source
        EditText source = (EditText)findViewById(R.id.input);

        //Save whatever was gathered into a string
        String locname = source.getText().toString();

        //Account for spaces for URL. meaning all spaces get replaced with %20
        if(locname.contains(" ") == true){
            locname = locname.replaceAll(" ", "%20");
        }

        //Begin connection to the API
        try {
            getData(locname);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void getData(String cityName) throws MalformedURLException {
        //API Key
        String key = "060824987ab26b5f057b76a478c908ab";

        //Parsing URL which includes inputted location and the key
        Uri uri = Uri.parse("https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&appid=" + key).buildUpon().build();

        //Execute connection
        URL url = new URL(uri.toString());
        new Connection().execute(url);
    }


class Connection extends AsyncTask<URL, Void, String> {
    @Override
    protected String doInBackground(URL... urls) {
        URL url = urls[0];
        String data = null;
        try {
            data = NetworkUtils.makeHTTPRequest(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
        //Begin HTTPRequest with URL from NetworkUtils class
    }

    @Override
    protected void onPostExecute(String s) {
        //After executing, analyze data. If the data is null, the location is invalid and
        //toast will be sent to inform the user. Otherwise, parse the Json data.
        if(s == null){
            Toast toast=Toast.makeText(getApplicationContext(),"Invalid Location",Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        try {
            parseJson(s);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void parseJson(String data) throws JSONException {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Handling all the JSON data and storing them. Afterwards, they get sent to the
        //respective view.

        JSONObject infos = jsonObject.getJSONObject("coord");
        double latval = infos.getDouble("lat");
        lat.setText(String.valueOf(latval));
        double longval = infos.getDouble("lon");
        longitude.setText(String.valueOf(longval));

        JSONObject clouds = jsonObject.getJSONObject("clouds");
        int cloudinfo = clouds.getInt("all");
        cloud.setText(String.valueOf(cloudinfo) + "%");

        JSONObject sys = jsonObject.getJSONObject("sys");
        String countryinfo = sys.getString("country");
        country.setText(countryinfo);

        String city = jsonObject.getString("name");
        loc.setText(city);

        JSONObject main = jsonObject.getJSONObject("main");
        double steptemp = main.getDouble("temp");
        int temp;
        //Detecting if F/C switch is checked
        if(aswitch.isChecked()){
            temp = TempConvertC(steptemp);
        }
        else temp = TempConvertF(steptemp);
        current.setText(String.valueOf(temp) + "°");

        double stepmax = main.getDouble("temp_max");
        int maxtemp;
        //Detecting if F/C switch is checked
        if(aswitch.isChecked()){
            maxtemp = TempConvertC(steptemp);
        }
        else maxtemp = TempConvertF(stepmax);
        max.setText(String.valueOf(maxtemp) + "°");

        double stepmin = main.getDouble("temp_min");
        int mintemp;
        //Detecting if F/C switch is checked
        if(aswitch.isChecked()){
            mintemp = TempConvertC(stepmin);
        }
        else mintemp = TempConvertF(stepmin);
        min.setText(String.valueOf(mintemp) + "°");

        int humid = main.getInt("humidity");
        hum.setText(String.valueOf(humid) + "%");

        double stepfeel = main.getDouble("feels_like");
        int feelinfo;
        //Detecting if F/C switch is checked
        if(aswitch.isChecked()){
            feelinfo = TempConvertC(stepfeel);
        }
        else feelinfo = TempConvertF(stepfeel);
        feel.setText(String.valueOf(feelinfo) + "°");

        JSONArray weatherinfo = jsonObject.getJSONArray("weather");
        for(int i = 0; i < weatherinfo.length(); i++){
            JSONObject arr = weatherinfo.getJSONObject(i);
            String stateinfo = arr.getString("main");
            state.setText(stateinfo);

            String descinfo = arr.getString("description");
            desc.setText(descinfo);

            //The API provides an ID number that symbolizes the ongoing weather. The picture will
            //change to fit the current kind of weather and will be decided with these statements.
            int idnum = arr.getInt("id");
            if(idnum >= 200 && idnum <=232){
                pic.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.thunder));
            }
            else if(idnum >= 300 && idnum <= 321){
                pic.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.drizzle));
            }
            else if(idnum >= 500 && idnum <= 531){
                pic.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rain));
            }
            else if(idnum >= 600 && idnum <= 622){
                pic.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.snow));
            }
            else if(idnum >= 701 && idnum <= 781){
                pic.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.download));
            }
            else if(idnum == 800){
                pic.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.clearsky));
            }
            else if(idnum >= 801 && idnum <= 804){
                pic.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.cloud));
            }
            else{
                pic.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.download));
            }
        }

        //Everything gets set to visible
        statfeel.setVisibility(View.VISIBLE);
        statcloud.setVisibility(View.VISIBLE);
        cloud.setVisibility(View.VISIBLE);
        feel.setVisibility(View.VISIBLE);
        stathum.setVisibility(View.VISIBLE);
        statmin.setVisibility(View.VISIBLE);
        statmax.setVisibility(View.VISIBLE);
        country.setVisibility(View.VISIBLE);
        desc.setVisibility(View.VISIBLE);
        state.setVisibility(View.VISIBLE);
        current.setVisibility(View.VISIBLE);
        max.setVisibility(View.VISIBLE);
        min.setVisibility(View.VISIBLE);
        hum.setVisibility(View.VISIBLE);
        pic.setVisibility(View.VISIBLE);
        loc.setVisibility(View.VISIBLE);
        lat.setVisibility(View.VISIBLE);
        longitude.setVisibility(View.VISIBLE);
        statcoord.setVisibility(View.VISIBLE);
        statloc.setVisibility(View.VISIBLE);
        statinfo.setVisibility(View.VISIBLE);
    }
}

    //This function hides the softkeyboard when called
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if(inputMethodManager.isAcceptingText()){
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(),
                    0
            );
        }
    }

    //This function converted Kelvin into Fahrenheit
    public static int TempConvertF(double kelvin){
        double v = 1.8 * (kelvin - 273) + 32;
        int result = (int)v;
        return result;
    }

    //This function converts Kelvin into Celsius
    public static int TempConvertC(double kelvin){
        double v = kelvin - 273.15;
        int result = (int)v;
        return result;
    }
}



