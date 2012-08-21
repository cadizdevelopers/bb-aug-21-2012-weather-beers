/*
 * Copyright (c) 2011 47 Degrees, LLC
 * http://47deg.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.fortysevendeg.lab;

import android.content.Context;
import android.location.Address;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

/**
 * This is the main entry point to the Android App.
 */
public class WeatherBeersActivity extends MapActivity {

    /**
     * constant used to multiply and adjust geocoder values in the right format
     */
    private static final double NTH_6 = 1E6;

    /**
     * Edit text view where a user can type a place
     */
    private EditText editPlaceSearch;

    /**
     * Button that submits the place to the server
     */
    private Button buttonPlaceSearch;

    /**
     * Map where the found city is displayed
     */
    private MapView mapView;

    /**
     * The image displaying the matching drink according to the temperature
     */
    private ImageView imageDrinkSelected;

    /**
     * The displayed temperature
     */
    private TextView textTemperatureDegrees;

    /**
     * A descriptive text that goes along the image with some funny remarks
     */
    private TextView textResultsDescription;

    /**
     * The place choosen as result of the search
     */
    private TextView textFoundCity;

    /**
     * helper to keep track internally of the address
     */
    private String currentAddress;

    /**
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        editPlaceSearch = (EditText) findViewById(R.id.search);
        mapView = (MapView) findViewById(R.id.map);
        textFoundCity = (TextView) findViewById(R.id.city);
        textTemperatureDegrees = (TextView) findViewById(R.id.temp);
        textResultsDescription = (TextView) findViewById(R.id.comment);
        imageDrinkSelected = (ImageView) findViewById(R.id.image);
        buttonPlaceSearch = (Button) findViewById(R.id.btnSearch);
        buttonPlaceSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                searchGeoCoder();
            }
        });
    }

    /**
     * private helper that invokes the GeoCoderTask
     */
    private void searchGeoCoder() {
        // hide keyboard first
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editPlaceSearch.getWindowToken(), 0);
        currentAddress = null;
        textFoundCity.setVisibility(View.GONE);
        textTemperatureDegrees.setVisibility(View.GONE);
        textResultsDescription.setVisibility(View.GONE);
        imageDrinkSelected.setVisibility(View.GONE);
        new GeoCoderTask(WeatherBeersActivity.this, geoCoderTaskListener).execute(editPlaceSearch.getText().toString());
    }

    /**
     * private helper that invokes the WeatherTask
     */
    private void searchWeather() {
        textFoundCity.setVisibility(View.VISIBLE);
        textFoundCity.setText(getString(R.string.searching, currentAddress));
        new WeatherTask(this, weatherTaskListener).execute(currentAddress);
    }

    /**
     * Callback listener that gets notified upon results from the Geocoder
     * @see com.fortysevendeg.lab.GeoCoderTask.GeoCoderTaskListener
     */
    private GeoCoderTask.GeoCoderTaskListener geoCoderTaskListener = new GeoCoderTask.GeoCoderTaskListener() {

        /**
         * @see GeoCoderTask.GeoCoderTaskListener#onLocationFound(android.location.Address)
         * When invoked this method displays the results in the UI interface
         */
        public void onLocationFound(Address address) {
            if (address != null) {
                if (address.getLocality() != null) {
                    currentAddress = address.getLocality();
                } else if (address.getAdminArea() != null) {
                    currentAddress = address.getAdminArea();
                } else {
                    currentAddress = editPlaceSearch.getText().toString();
                }
            }
            if (currentAddress != null) {
                GeoPoint geoPoint = new GeoPoint((int) (address.getLatitude() * NTH_6), (int) (address.getLongitude() * NTH_6));
                mapView.getController().animateTo(geoPoint);
                mapView.getController().setZoom(getResources().getInteger(R.integer.map_zoom_level));
                searchWeather();
            } else {
                Toast.makeText(WeatherBeersActivity.this, R.string.locationNotFound, Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * Callback listener that gets notified upon results from the Weather Temperature service
     * @see com.fortysevendeg.lab.WeatherTask.WeatherTaskListener
     */
    private WeatherTask.WeatherTaskListener weatherTaskListener = new WeatherTask.WeatherTaskListener() {

        /**
         * @see WeatherTask.WeatherTaskListener#onTempFound(int)
         * When invoked this method displays the results in the UI interface
         */
        public void onTempFound(int temperatureCelsius) {
            if (temperatureCelsius == WeatherTask.ERROR) {
                textFoundCity.setText(getString(R.string.weatherNotFound, currentAddress));
            } else {
                textFoundCity.setVisibility(View.VISIBLE);
                textTemperatureDegrees.setVisibility(View.VISIBLE);
                textResultsDescription.setVisibility(View.VISIBLE);
                imageDrinkSelected.setVisibility(View.VISIBLE);
                textFoundCity.setText(getString(R.string.temp, currentAddress));
                textTemperatureDegrees.setText(temperatureCelsius + "º");
                if (temperatureCelsius > 30) {
                    textResultsDescription.setText(R.string.comment_ice_cream);
                    imageDrinkSelected.setImageResource(R.drawable.icon_ice_cream);
                } else if (temperatureCelsius > 20) {
                    textResultsDescription.setText(R.string.comment_beer);
                    imageDrinkSelected.setImageResource(R.drawable.icon_beer);
                } else if (temperatureCelsius > 10) {
                    textResultsDescription.setText(R.string.comment_coffee);
                    imageDrinkSelected.setImageResource(R.drawable.icon_coffee);
                } else {
                    textResultsDescription.setText(R.string.comment_vodka);
                    imageDrinkSelected.setImageResource(R.drawable.icon_vodka);
                }
            }
        }
    };

    /**
     * @see com.google.android.maps.MapActivity#isRouteDisplayed()
     */
    @Override
    protected final boolean isRouteDisplayed() {
        return false;
    }
}
