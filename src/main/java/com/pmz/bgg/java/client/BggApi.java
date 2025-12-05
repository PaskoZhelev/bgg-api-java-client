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
package com.pmz.bgg.java.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.pmz.bgg.java.client.apibuilder.BggApiBuilder;
import com.pmz.bgg.java.client.enums.*;
import com.pmz.bgg.java.client.model.*;
import com.pmz.bgg.java.client.tools.HttpTools;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BggApi {

    private static final Logger LOG = LoggerFactory.getLogger(BggApi.class);
    private final HttpTools httpTools;
    private final XmlMapper mapper;

    private static final String BASE_URL_TEMPLATE = "https://boardgamegeek.com/xmlapi2/{command}";

    public BggApi(String authorizationToken) {
        this.httpTools = new HttpTools(authorizationToken);
        this.mapper = new XmlMapper();
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public List<BoardGameExtended> getBoardGameInfo(int id) throws BggException {
        return getBoardGameInfo(ThingType.BOARDGAME, id,
                List.of(IncludeExclude.STATS, IncludeExclude.VERSIONS, IncludeExclude.VIDEOS),
                null);
    }

    public List<BoardGameExtended> getBoardGameInfo(ThingType type, int id, List<IncludeExclude> includes, List<IncludeExclude> excludes) throws BggException {
        URI uri = new BggApiBuilder(BASE_URL_TEMPLATE, Command.THING)
                .id(id)
                .thingType(type)
                .include(includes)
                .exclude(excludes)
                .page(1)
                .pageSize(25)
                .buildUri();

        return fetchAndMap(uri, BoardGameWrapper.class).getItems();
    }

    public List<Family> getFamilyItems(int id, FamilyType familyType) throws BggException {
        URI uri = new BggApiBuilder(BASE_URL_TEMPLATE, Command.FAMILY)
                .family(familyType)
                .id(id)
                .buildUri();

        return fetchAndMap(uri, FamilyWrapper.class).getItems();
    }

    public UserInfo getUserInfo(String username) throws BggException {
        URI uri = new BggApiBuilder(BASE_URL_TEMPLATE, Command.USER)
                .username(username)
                .include(IncludeExclude.BUDDIES, IncludeExclude.GUILDS, IncludeExclude.HOT, IncludeExclude.TOP)
                .buildUri();

        return fetchAndMap(uri, UserInfo.class);
    }

    public CollectionItemWrapper getCollectionInfo(String username, String id, List<IncludeExclude> include, List<IncludeExclude> exclude, boolean includeExpansions) throws BggException {
        BggApiBuilder builder = new BggApiBuilder(BASE_URL_TEMPLATE, Command.COLLECTION)
                .username(username)
                .id(id)
                .include(include)
                .exclude(exclude);

        if (!includeExpansions) {
            builder.excludeSubType(ThingType.BOARDGAMEEXPANSION);
        }

        return fetchAndMap(builder.buildUri(), CollectionItemWrapper.class);
    }

    public SearchWrapper searchBoardGame(String query, boolean exact, boolean includeExpansions) throws BggException {
        BggApiBuilder builder = new BggApiBuilder(BASE_URL_TEMPLATE, Command.SEARCH)
                .query(query)
                .thingType(includeExpansions
                        ? new ThingType[]{ThingType.BOARDGAME, ThingType.BOARDGAMEEXPANSION}
                        : new ThingType[]{ThingType.BOARDGAME});

        if (exact) {
            builder.include(IncludeExclude.EXACT);
        }

        return fetchAndMap(builder.buildUri(), SearchWrapper.class);
    }

    public List<HotListItem> getHotItems(HotItemType itemType) throws BggException {
        URI uri = new BggApiBuilder(BASE_URL_TEMPLATE, Command.HOT)
                .hotType(itemType == null ? HotItemType.BOARDGAME : itemType)
                .buildUri();

        try {
            String xml = httpTools.retrieveWebpage(uri);
            GenericListWrapper<HotListItem> wrapper = mapper.readValue(xml, new TypeReference<GenericListWrapper<HotListItem>>() {});
            return wrapper.getItems();
        } catch (IOException e) {
            throw new BggException("Failed to map Hot Items", e);
        }
    }

    // Helper method to reduce code duplication
    private <T> T fetchAndMap(URI uri, Class<T> clazz) throws BggException {
        LOG.debug("Fetching URI: {}", uri);
        String xml = httpTools.retrieveWebpage(uri);
        try {
            return mapper.readValue(xml, clazz);
        } catch (IOException e) {
            throw new BggException("Failed to map XML to " + clazz.getSimpleName(), e);
        }
    }
}