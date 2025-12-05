/*
 *      Copyright (c) 2017 Stuart Boston
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
package com.pmz.bgg.java.client.tools;

import com.pmz.bgg.java.client.BggException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpTools {

    private static final Logger LOG = LoggerFactory.getLogger(HttpTools.class);
    private final HttpClient httpClient;
    private final String authToken; // Store the token here

    private static final int RETRY_MAX = 5;
    private static final int STATUS_TOO_MANY_REQUESTS = 429;
    private static final int STATUS_OK = 200;

    // Default constructor (no token)
    public HttpTools() {
        this(null);
    }

    // Constructor accepting a token
    public HttpTools(String authToken) {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(20))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.authToken = authToken;
    }

    public HttpTools(HttpClient httpClient, String authToken) {
        this.httpClient = httpClient;
        this.authToken = authToken;
    }

    /**
     * Executes a GET request with built-in retry logic and Auth headers.
     */
    public String retrieveWebpage(URI uri) throws BggException {
        // Start building the request
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .header("Accept", "application/xml");

        // Inject the Authorization header if the token exists
        if (authToken != null && !authToken.isBlank()) {
            requestBuilder.header("Authorization", "Bearer " + authToken);
        }

        HttpRequest request = requestBuilder.build();
        int retryCount = 0;

        while (retryCount <= RETRY_MAX) {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                int status = response.statusCode();

                if (status == STATUS_OK) {
                    return response.body();
                } else if (status == STATUS_TOO_MANY_REQUESTS) {
                    LOG.warn("Rate limit hit (429). Retrying... Attempt {}/{}", retryCount + 1, RETRY_MAX);
                    delay(retryCount);
                    retryCount++;
                } else if (status >= 500) {
                    LOG.warn("Server Error ({}). Retrying...", status);
                    delay(retryCount);
                    retryCount++;
                } else if (status == 401 || status == 403) {
                    // Fail fast on Auth errors
                    throw new BggException("Authorization Failed: " + status, status, uri.toString());
                } else {
                    throw new BggException("API Request Failed", status, uri.toString());
                }

            } catch (IOException | InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new BggException("Network error while reaching BGG", ex);
            }
        }

        throw new BggException("Exceeded retry count for URL", 429, uri.toString());
    }

    private void delay(int attempt) {
        try {
            long sleepTime = 2L * (attempt + 1);
            TimeUnit.SECONDS.sleep(sleepTime);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}