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
package com.tuscanyscatours.car.impl;



import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.oasisopen.sca.annotation.Service;

import cn.edu.nju.moon.conup.spi.datamodel.ConupTransaction;

import com.tuscanyscatours.common.TripItem;
import com.tuscanyscatours.common.TripLeg;

/**
 * An implementation of the Car service
 */
@Service( {CarSearch.class, CarBook.class})
public class CarImpl implements CarSearch, CarBook {
	private Logger LOGGER = Logger.getLogger(CarImpl.class.getName());
	/** it's used to identify component version */
	private String COMP_VER = "Ver_0";
    private List<CarInfo> cars = new ArrayList<CarInfo>();

    public CarImpl(){
        cars.add(new CarInfo("Premier Cars", "BMW 5 Series", "FLR", "06/12/09", "5", 100.00, "EUR",
                "http://localhost:8085/tbd"));
        cars.add(new CarInfo("Premier Cars", "Ford Focus", "FLR", "06/12/09", "4", 60.00, "EUR",
                "http://localhost:8085/tbd"));
    }
    
//    @Init
//    public void init() {
//        cars.add(new CarInfo("Premier Cars", "BMW 5 Series", "FLR", "06/12/09", "5", 100.00, "EUR",
//                             "http://localhost:8085/tbd"));
//        cars.add(new CarInfo("Premier Cars", "Ford Focus", "FLR", "06/12/09", "4", 60.00, "EUR",
//                             "http://localhost:8085/tbd"));
//    }

    @ConupTransaction
    public TripItem[] searchSynch(TripLeg tripLeg) {
        List<TripItem> items = new ArrayList<TripItem>();

        // find available cars
        for (CarInfo car : cars) {
            if (car.getLocation().equals(tripLeg.getToLocation())) {
                TripItem item =
                    new TripItem("", "", TripItem.CAR, car.getName(), car.getDescription(), car.getLocation(), tripLeg
                        .getFromDate(), tripLeg.getToDate(), car.getPricePerDay(), car.getCurrency(), car.getLink());
                items.add(item);
            }
        }
        // add 200ms delay
        try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        
        return items.toArray(new TripItem[items.size()]);
    }

    @ConupTransaction
    public int getPercentComplete() {
        return 100;
    }

    @ConupTransaction
    public String book(TripItem tripItem) {
        return "car1";
    }
}
