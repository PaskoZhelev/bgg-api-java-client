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
package com.pmz.bgg.java.client.apibuilder;


import com.pmz.bgg.java.client.enums.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BggApiBuilder {

    private final String baseUrl;
    private final List<String> params = new ArrayList<>();

    public BggApiBuilder(String baseUrl, Command command) {
        // e.g. https://boardgamegeek.com/xmlapi2/thing
        this.baseUrl = baseUrl.replace("{command}", command.toString());
    }

    public BggApiBuilder id(int id) {
        return addParam("id", String.valueOf(id));
    }

    public BggApiBuilder id(String id) {
        return addParam("id", id);
    }

    public BggApiBuilder thingType(ThingType... values) {
        if (values != null && values.length > 0) {
            String joined = List.of(values).stream()
                    .map(ThingType::toString)
                    .collect(Collectors.joining(","));
            addParam("type", joined);
        }
        return this;
    }

    public BggApiBuilder page(int page) {
        return addParam("page", String.valueOf(Math.max(1, page)));
    }

    public BggApiBuilder pageSize(int size) {
        int clamped = Math.max(10, Math.min(size, 100));
        return addParam("pagesize", String.valueOf(clamped));
    }

    public BggApiBuilder query(String query) {
        return addParam("query", query);
    }

    public BggApiBuilder username(String username) {
        return addParam("username", username);
    }

    public BggApiBuilder family(FamilyType type) {
        return addParam("type", type.toString());
    }

    public BggApiBuilder hotType(HotItemType type) {
        return addParam("type", type.toString());
    }

    public BggApiBuilder include(List<IncludeExclude> includes) {
        if (includes != null) {
            includes.forEach(ie -> addParam(ie.toString(), "1"));
        }
        return this;
    }

    public BggApiBuilder include(IncludeExclude... includes) {
        if (includes != null) {
            for (IncludeExclude ie : includes) addParam(ie.toString(), "1");
        }
        return this;
    }

    public BggApiBuilder exclude(List<IncludeExclude> excludes) {
        if (excludes != null) {
            excludes.forEach(ie -> addParam(ie.toString(), "0"));
        }
        return this;
    }

    public BggApiBuilder excludeSubType(ThingType subType) {
        return addParam("excludesubtype", subType.toString());
    }

    private BggApiBuilder addParam(String key, String value) {
        if (value != null && !value.isEmpty()) {
            String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8);
            params.add(key + "=" + encodedValue);
        }
        return this;
    }

    public URI buildUri() {
        String queryString = String.join("&", params);
        if (queryString.isEmpty()) {
            return URI.create(baseUrl);
        }
        return URI.create(baseUrl + "?" + queryString);
    }
}
