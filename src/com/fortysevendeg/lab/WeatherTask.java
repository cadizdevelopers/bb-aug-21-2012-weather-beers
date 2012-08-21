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
import android.os.AsyncTask;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.xpath.*;
import java.io.*;
import java.net.URLEncoder;

/**
 * Fetches remote weather information given a place
 */
public class WeatherTask extends AsyncTask<String, Void, Integer> {

    /**
     * Default http client used for http requests to the remote weather service
     */
    private static final HttpClient HTTP_CLIENT = new DefaultHttpClient();

    /**
     * Flag utilized to determine if the temperature returned is valid
     */
    public static final int ERROR = Integer.MAX_VALUE;

    /**
     * The context utilized to load vars and config files
     */
    private Context context;

    /**
     * Callback that gets invoked when the weather service returns results
     */
    private WeatherTaskListener weatherTaskListener;

    /**
     * Constructor
     *
     * @param weatherTaskListener Listener Callback that gets invoked when the weather service returns results
     */
    public WeatherTask(Context context, WeatherTaskListener weatherTaskListener) {
        this.context = context;
        this.weatherTaskListener = weatherTaskListener;
    }

    /**
     * Performs work in the background invoking the weather remote service and parses the xml results into temp values
     * @param strings the place params to search form
     * @return the temperature in celsius
     */
    @Override
    protected final Integer doInBackground(String... strings) {
        int temp = ERROR;
        try {
            String xml = fetchWeatherData(strings[0]);
            if (xml != null) {
                XPathFactory factory = XPathFactory.newInstance();
                XPath xpath = factory.newXPath();
                XPathExpression expr = xpath.compile(context.getString(R.string.xpath_weather_service_temperature));
                Object result = expr.evaluate(new InputSource(new StringReader(xml)), XPathConstants.NODESET);
                NodeList nodes = (NodeList) result;
                if (nodes.getLength() > 0) {
                    temp = Integer.valueOf(nodes.item(0).getAttributes().getNamedItem("data").getNodeValue());
                }
            }
        } catch (XPathExpressionException e) {
            Log.wtf(WeatherTask.class.toString(), e);
        } catch (IOException e) {
            Log.wtf(WeatherTask.class.toString(), e);
        }
        return temp;
    }

    /**
     * Invoked in the main UI thread delegating results to the weatherTaskListener
     * @param temperatureCelsius the temperature in celsius
     */
    @Override
    protected final void onPostExecute(final Integer temperatureCelsius) {
        super.onPostExecute(temperatureCelsius);
        weatherTaskListener.onTempFound(temperatureCelsius);
    }

    /**
     * private helper that actually makes the http call and fetches the remote response from Google's weather service
     * @param place the place to look weather info for
     * @return the xml response as a string
     * @throws IOException
     */
    private String fetchWeatherData(String place) throws IOException {
        String url = context.getString(R.string.url_service_weather, URLEncoder.encode(place, "UTF-8"));
        StringBuilder builder = new StringBuilder();
        InputStream content = null;
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = HTTP_CLIENT.execute(httpGet);
        StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        BufferedReader reader = null;
        if (statusCode == HttpStatus.SC_OK) {
            HttpEntity entity = response.getEntity();
            content = entity.getContent();
            reader = new BufferedReader(new InputStreamReader(content));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } else {
            Log.e(WeatherTask.class.toString(), "Failed to download file");
        }
        Utils.close(content, reader);
        return builder.toString();
    }

    /**
     * Callback interface utilized to delegate temperature results to the AsyncTask invokers
     */
    interface WeatherTaskListener {

        /**
         * Receives the results from the weather service
         * @param temperatureCelsius the temperature in celsius or potentially WeatherTask.ERROR if not a valid temperature or there
         *                           was a problem communicating with the remote weather service
         */
        void onTempFound(int temperatureCelsius);
    }

}
