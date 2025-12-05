/*
 *
 *      This file is part of the Board Game Geek API Wrapper.
 *
 *      This API wrapper is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      any later version.
 *
 *      The API wrapper is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with the API Wrapper.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.pmz.bgg.java.client;

import java.io.IOException;

public class BggException extends Exception {
    private final int statusCode;
    private final String url;

    public BggException(String message) {
        super(message);
        this.statusCode = 0;
        this.url = null;
    }

    public BggException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
        this.url = null;
    }

    public BggException(String message, int statusCode, String url) {
        super(message + " (Status: " + statusCode + ", URL: " + url + ")");
        this.statusCode = statusCode;
        this.url = url;
    }

    public int getStatusCode() {
        return statusCode;
    }
}