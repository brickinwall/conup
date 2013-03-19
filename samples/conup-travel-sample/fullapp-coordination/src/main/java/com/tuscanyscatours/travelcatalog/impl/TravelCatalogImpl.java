/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package com.tuscanyscatours.travelcatalog.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.oasisopen.sca.ComponentContext;
import org.oasisopen.sca.RequestContext;
import org.oasisopen.sca.ServiceReference;
import org.oasisopen.sca.annotation.Callback;
import org.oasisopen.sca.annotation.Context;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Scope;
import org.oasisopen.sca.annotation.Service;

import cn.edu.nju.moon.conup.spi.datamodel.ConupTransaction;

import com.tuscanyscatours.car.impl.CarSearch;
import com.tuscanyscatours.common.SearchCallback;
import com.tuscanyscatours.common.TripItem;
import com.tuscanyscatours.common.TripLeg;
import com.tuscanyscatours.currencyconverter.CurrencyConverter;
import com.tuscanyscatours.flight.impl.FlightSearch;
import com.tuscanyscatours.hotel.HotelSearch;
import com.tuscanyscatours.travelcatalog.TravelCatalogSearch;
import com.tuscanyscatours.trip.impl.TripSearch;

/**
 * An implementation of the travel catalog service
 */
@Service(TravelCatalogSearch.class)
public class TravelCatalogImpl implements TravelCatalogSearch {

    @Reference
    protected HotelSearch hotelSearch;

    @Reference
    protected FlightSearch flightSearch;

    @Reference
    protected CarSearch carSearch;

    @Reference
    protected TripSearch tripSearch;

    @Property
    public String quoteCurrencyCode = "USD";

    @Reference
    protected CurrencyConverter currencyConverter;

    private List<TripItem> searchResults = new ArrayList<TripItem>();


    // TravelSearch methods
    @ConupTransaction
    public TripItem[] search(TripLeg tripLeg) {
        searchResults.clear();

        TripItem[] hotelTrips = hotelSearch.searchSynch(tripLeg);
        TripItem[] flightTrips = flightSearch.searchSynch(tripLeg);
        TripItem[] carTrips = carSearch.searchSynch(tripLeg);
        TripItem[] packagedTrips = tripSearch.searchSynch(tripLeg);
        
        if(flightTrips != null)
        	searchResults.addAll(Arrays.asList(flightTrips));
        if(carTrips != null)
        	searchResults.addAll(Arrays.asList(carTrips));
        if(packagedTrips != null)
        	searchResults.addAll(Arrays.asList(packagedTrips));
        if(hotelTrips != null)
        	searchResults.addAll(Arrays.asList(hotelTrips));
        
        for (TripItem tripItem : searchResults) {
            tripItem.setId(UUID.randomUUID().toString());
            tripItem.setTripId(tripLeg.getId());
            tripItem
                .setPrice(currencyConverter.convert(tripItem.getCurrency(), quoteCurrencyCode, tripItem.getPrice()));
            tripItem.setCurrency(quoteCurrencyCode);
        }

        return searchResults.toArray(new TripItem[searchResults.size()]);
    }

}
