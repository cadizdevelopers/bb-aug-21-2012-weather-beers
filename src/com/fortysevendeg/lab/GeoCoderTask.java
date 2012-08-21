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

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Connects with a geocoder service and fetches geolocation information for an arbitrary string or address
 */
public class GeoCoderTask extends AsyncTask<String, Void, Address> {

    /**
     * The progress dialog that will display progress while the geocoder is being contacted
     */
    private ProgressDialog progressDialog;

    /**
     * The geocoder object
     */
    private Geocoder geocoder;

    /**
     * The context utilized to load vars and config files
     */
    private Context context;

    /**
     * A callback delegate invoked when result are ready
     */
    private GeoCoderTaskListener geoCoderTaskListener;

    /**
     * Constructor
     * @param context The context utilized to load vars and config files
     * @param geoCoderTaskListener callback delegate invoked when result are ready
     */
    public GeoCoderTask(Context context, GeoCoderTaskListener geoCoderTaskListener) {
        this.context = context;
        this.geoCoderTaskListener = geoCoderTaskListener;
        geocoder = new Geocoder(context, Locale.getDefault());
    }

    /**
     * Connects with the geocoder in the background and retrieves a list of possible matches for the search string
     * in the form of {Address}
     * @param strings an arbitrary string that matches a place in the world expressed in the user's locale
     * @return Address the first match returned by the Geocoder
     */
    @Override
    protected final Address doInBackground(String... strings) {
        try {
            List<Address> addresses = geocoder.getFromLocationName(strings[0], 1);
            if (!addresses.isEmpty()) {
                return addresses.get(0);
            }
        } catch (IOException e) {
            Log.e(WeatherTask.class.toString(), "GeoCoder error", e);
        }
        return null;
    }

    /**
     * Runs on the UI thread before doInBackground(Params...)
     */
    @Override
    protected final void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(context.getString(R.string.searchingAddress));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    /**
     * Runs on the UI thread after publishProgress(Progress...) is invoked
     * @param address Address, if isn't found the address would be null
     */
    @Override
    protected final void onPostExecute(Address address) {
        super.onPostExecute(address);
        // Hide dialog and return results using listener
        progressDialog.hide();
        geoCoderTaskListener.onLocationFound(address);
    }

    /**
     * A callback interface to get notified whenever a an address is found for the place search
     */
    public interface GeoCoderTaskListener {

        /**
         * Invoked when an Address is found or the geocoder has completed with no address in which case it will be invoked with a null arg
         * @param address the found address
         */
        void onLocationFound(Address address);
    }
}
