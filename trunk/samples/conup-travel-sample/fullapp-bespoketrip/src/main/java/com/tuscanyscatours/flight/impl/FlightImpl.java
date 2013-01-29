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
package com.tuscanyscatours.flight.impl;


import java.util.ArrayList;
import java.util.List;

import org.oasisopen.sca.annotation.Callback;
import org.oasisopen.sca.annotation.ComponentName;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Scope;
import org.oasisopen.sca.annotation.Service;

import cn.edu.nju.moon.conup.spi.datamodel.ConupTransaction;

import com.tuscanyscatours.common.Book;
import com.tuscanyscatours.common.Search;
import com.tuscanyscatours.common.SearchCallback;
import com.tuscanyscatours.common.TripItem;
import com.tuscanyscatours.common.TripLeg;

/**
 * An implementation of the Flight service
 */
@Service( {Search.class, Book.class})
public class FlightImpl implements Search, Book {

    private List<FlightInfo> flights = new ArrayList<FlightInfo>();

    @ComponentName
    protected String componentName;

    private int percentComplete = 0;

    @Init
    public void init() {
        flights.add(new FlightInfo("EA26", "Europe Airlines Airbus A300", "LGW", "FLR", "06/12/09", "06/12/09", "350",
                                   250, "EUR", "http://localhost:8085/tbd"));
        flights.add(new FlightInfo("EA27", "Europe Airlines Airbus A300", "FLR", "LGW", "13/12/09", "13/12/09", "350",
                                   250, "EUR", "http://localhost:8085/tbd"));

    }

    @ConupTransaction
    public TripItem[] searchSynch(TripLeg tripLeg) {
        List<TripItem> items = new ArrayList<TripItem>();

        // find outbound leg
        for (FlightInfo flight : flights) {
            if ((flight.getFromLocation().equals(tripLeg.getFromLocation())) && (flight.getToLocation().equals(tripLeg
                .getToLocation()))
                && (flight.getFromDate().equals(tripLeg.getFromDate()))) {
                TripItem item =
                    new TripItem("", "", TripItem.FLIGHT, flight.getName(), flight.getDescription(), flight
                        .getFromLocation() + " - "
                        + flight.getToLocation(), flight.getFromDate(), flight.getToDate(), flight.getPricePerSeat(),
                                 flight.getCurrency(), flight.getLink());
                items.add(item);
            }
        }

        // find return leg
        for (FlightInfo flight : flights) {
            if ((flight.getFromLocation().equals(tripLeg.getToLocation())) && (flight.getToLocation().equals(tripLeg
                .getFromLocation()))
                && (flight.getFromDate().equals(tripLeg.getToDate()))) {
                TripItem item =
                    new TripItem("", "", TripItem.FLIGHT, flight.getName(), flight.getDescription(), flight
                        .getFromLocation() + " - "
                        + flight.getToLocation(), flight.getFromDate(), tripLeg.getToDate(), flight.getPricePerSeat(),
                                 flight.getCurrency(), flight.getLink());
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
        return "flight1";
    }
}
