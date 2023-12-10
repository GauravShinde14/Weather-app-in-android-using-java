package com.gaurav.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    private RelativeLayout homeRL;
    private ProgressBar lodingPB;
    private TextView cityNameTV,temperatureTV,conditionTV;
    private RecyclerView weatherRV;
    private TextInputEditText cityEdt;
    private ImageView backIV, iconIV, searchIV;
    private ArrayList<WeatherRVModel> weatherRVModelArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE =1;
    private String cityName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_main);

        homeRL = findViewById(R.id.idRLHome);
        lodingPB = findViewById(R.id.idPBLoading);
        cityNameTV = findViewById(R.id.idTVCityName);
        temperatureTV = findViewById(R.id.idTVTemperature);
        conditionTV = findViewById(R.id.idTVCondition);
        weatherRV = findViewById(R.id.idRVWeather);
        cityEdt = findViewById(R.id.idEdtCity);
        backIV = findViewById(R.id.idIVBack);
        iconIV = findViewById(R.id.idIVIcon);
        searchIV = findViewById(R.id.idIvSearch);

        weatherRVModelArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this,weatherRVModelArrayList);
        weatherRV.setAdapter(weatherRVAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this,Manifest.permission.ACCESS_COARSE_LOCATION )!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION , Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);

        }

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityName = cityName(location.getLongitude(),location.getLatitude());


        getWeatherInfo(cityName);

        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = cityEdt.getText().toString();

                if(city.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter City Name", Toast.LENGTH_SHORT).show();

                }else{
                    cityNameTV.setText(cityName);
                    getWeatherInfo(city);
                }
            }
        });



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(MainActivity.this, "Permissioned granted", Toast.LENGTH_SHORT).show();

            }else {
                Toast.makeText(MainActivity.this, "Please Grant the permissions", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String cityName(double longitude , double latitude){
        String cityName = "Not Found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());

        try {

            List<Address> addressList = gcd.getFromLocation(latitude,longitude,10);

            for (Address a:addressList){

                if(a!=null){
                    String city = a.getLocality();
                    if(city !=null & !city.equals("")){
                        cityName = city;
                    }else {
                        Log.d("TAG", "cityName: " +cityName);
                        Toast.makeText(this, "City not found", Toast.LENGTH_SHORT).show();

                    }
                }

            }


        }catch (IOException e){
            e.printStackTrace();
        }

        return cityName;
    }

    private void getWeatherInfo( String cityName){
        String url="https://api.weatherapi.com/v1/forecast.json?key=d65ca51a56ef4e9fafc75700231012&q="+cityName+"&days=1&aqi=no&alerts=no\n";

        cityNameTV.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                lodingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);
                weatherRVModelArrayList.clear();

                try {
                    String temperature = response.getJSONObject("current").getString("temp_c");
                    temperatureTV.setText(temperature +"C");

                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("http:".concat(conditionIcon)).into(iconIV);
                    conditionTV.setText(condition);

                    if(isDay == 1){
                        //morning
                        Picasso.get().load("https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.peakpx.com%2Fen%2Fsearch%3Fq%3Dweather%2Bvillage&psig=AOvVaw1n-lvMbdx9DdMhwFY9lqDZ&ust=1702317836869000&source=images&cd=vfe&opi=89978449&ved=0CBIQjRxqFwoTCIi62qS6hYMDFQAAAAAdAAAAABAD").into(backIV);

                    }else {
                        Picasso.get().load("data:image/jpeg;base64,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").into(backIV);

                    }

                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forecast0 = forecastObj.getJSONArray("forecastday").getJSONObject(0);

                    JSONArray hourArray = forecast0.getJSONArray("hour");

                    for (int i = 0; i < hourArray.length(); i++) {

                        JSONObject hourObj = hourArray.getJSONObject(i);
                        String time = hourObj.getString("time");
                        String temper = hourObj.getString("temp_c");
                        String img = hourObj.getJSONObject("condition").getString("icon");
                        String wind = hourObj.getString("wind_kph");
                        weatherRVModelArrayList.add(new WeatherRVModel(time,temper,img,wind));
                    }

                    weatherRVAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Please provide valid city name", Toast.LENGTH_SHORT).show();
                Log.d("TAG", "onErrorResponse: "+error);
            }
        });

        requestQueue.add(jsonObjectRequest);


    }
}