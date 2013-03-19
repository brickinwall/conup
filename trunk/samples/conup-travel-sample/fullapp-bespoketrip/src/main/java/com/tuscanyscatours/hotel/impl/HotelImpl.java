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
package com.tuscanyscatours.hotel.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Service;

import cn.edu.nju.moon.conup.spi.datamodel.ConupTransaction;

import com.tuscanyscatours.common.TripItem;
import com.tuscanyscatours.common.TripLeg;
import com.tuscanyscatours.hotel.HotelBook;
import com.tuscanyscatours.hotel.HotelInfo;
import com.tuscanyscatours.hotel.HotelManagement;
import com.tuscanyscatours.hotel.HotelSearch;

/**
 * An implementation of the Hotel service
 */
@Service( {HotelSearch.class, HotelBook.class, HotelManagement.class})
public class HotelImpl implements HotelSearch, HotelBook, HotelManagement {
	private static Logger LOGGER = Logger.getLogger(HotelImpl.class.getName());

    private List<HotelInfo> hotels = new ArrayList<HotelInfo>();

    @Init
    public void init() {
        hotels.add(new HotelInfo("Deep Bay Hotel", "Wonderful sea views and a relaxed atmosphere", "FLR", "06/12/09",
                                 "200", 100, "EUR", "http://localhost:8085/tbd"));
        hotels.add(new HotelInfo("Long Bay Hotel", "Friendly staff and an ocean breeze", "FLR", "06/12/09", "200", 100,
                                 "EUR", "http://localhost:8085/tbd"));
        hotels.add(new HotelInfo("City Hotel", "Smart rooms and early breakfasts", "FLR", "06/12/09", "200", 100,
                                 "EUR", "http://localhost:8085/tbd"));
        hotels.add(new HotelInfo("County Hotel", "The smell of the open country", "FLR", "06/12/09", "200", 100, "EUR",
                                 "http://localhost:8085/tbd"));
    }

    @ConupTransaction
    public TripItem[] searchSynch(TripLeg tripLeg) {
        List<TripItem> items = new ArrayList<TripItem>();

        // find available hotels
        for (HotelInfo hotel : hotels) {
            if (hotel.getLocation().equals(tripLeg.getToLocation())) {
                TripItem item =
                    new TripItem("", "", TripItem.HOTEL, hotel.getName(), hotel.getDescription(), hotel.getLocation(),
                                 tripLeg.getFromDate(), tripLeg.getToDate(), hotel.getPricePerBed(), hotel
                                     .getCurrency(), hotel.getLink());
                items.add(item);
            }
        }

        return items.toArray(new TripItem[items.size()]);
    }

    @ConupTransaction
    public int getPercentComplete() {
        return 100;
    }

    @ConupTransaction
    public String book(TripItem tripItem) {
        return "hotel1";
    }
    
    @ConupTransaction
    public void addHotelInfo(HotelInfo hotelInfo) {
        hotels.add(hotelInfo);
        LOGGER.fine("Added hotel info - " + hotelInfo.getName());
    }
}
