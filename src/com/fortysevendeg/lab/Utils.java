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

import android.util.Log;

import java.io.Closeable;
import java.io.IOException;

/**
 * Util class to perform some boiler plate code and operations
 */
public class Utils {

    /**
     * Hides visibility to avoid instantiation
     */
    private Utils() {
    }

    /**
     * Closes any closeables
     * @param closeables a vararg list of closeables
     */
    public static void close(Closeable ... closeables) {
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    Log.wtf(Utils.class.getName(), e);
                }
            }
        }
    }

}
