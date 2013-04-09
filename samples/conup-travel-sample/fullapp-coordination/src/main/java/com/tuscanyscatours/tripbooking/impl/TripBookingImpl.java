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
package com.tuscanyscatours.tripbooking.impl;

import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

import cn.edu.nju.moon.conup.spi.datamodel.ConupTransaction;

import com.tuscanyscatours.car.impl.CarBook;
import com.tuscanyscatours.common.TripItem;
import com.tuscanyscatours.flight.impl.FlightBook;
import com.tuscanyscatours.hotel.HotelBook;
import com.tuscanyscatours.shoppingcart.CartUpdates;
import com.tuscanyscatours.trip.impl.TripBook;
import com.tuscanyscatours.tripbooking.TripBooking;

/**
 * An implementation of the TripBooking service
 */
@Service(TripBooking.class)
public class TripBookingImpl implements TripBooking {
//    @Reference
    protected HotelBook hotelBook;

//    @Reference
    protected FlightBook flightBook;

//    @Reference
    protected CarBook carBook;

//    @Reference
    protected TripBook tripBook;

//    @Reference
    protected CartUpdates cartUpdates;


    public HotelBook getHotelBook() {
		return hotelBook;
	}

    @Reference
	public void setHotelBook(HotelBook hotelBook) {
		this.hotelBook = hotelBook;
	}

	public FlightBook getFlightBook() {
		return flightBook;
	}

	@Reference
	public void setFlightBook(FlightBook flightBook) {
		this.flightBook = flightBook;
	}

	public CarBook getCarBook() {
		return carBook;
	}

	@Reference
	public void setCarBook(CarBook carBook) {
		this.carBook = carBook;
	}

	public TripBook getTripBook() {
		return tripBook;
	}

	@Reference
	public void setTripBook(TripBook tripBook) {
		this.tripBook = tripBook;
	}

	public CartUpdates getCartUpdates() {
		return cartUpdates;
	}

	@Reference
	public void setCartUpdates(CartUpdates cartUpdates) {
		this.cartUpdates = cartUpdates;
	}

	@ConupTransaction
    public TripItem bookTrip(String cartId, TripItem trip) {

        String bookingCode = "";

        // book any nested items
        TripItem[] nestedItems = trip.getTripItems();
        if (nestedItems != null) {
            for(TripItem tripItem : nestedItems){
                if (tripItem.getType().equals(TripItem.CAR)) {
                    tripItem.setBookingCode(carBook.book(tripItem));
                } else if (tripItem.getType().equals(TripItem.FLIGHT)) {
                    tripItem.setBookingCode(flightBook.book(tripItem));
                } else if (tripItem.getType().equals(TripItem.HOTEL)) {
                    tripItem.setBookingCode(hotelBook.book(tripItem));
                } else {
                    tripItem.setBookingCode(tripItem.getType() + " is invalid");
                }
            }
        }

        // book the top level item if it's a packaged trip
        if (trip.getType().equals(TripItem.TRIP)) {
            bookingCode = tripBook.book(trip);
            trip.setBookingCode(bookingCode);
        }

        cartUpdates.addTrip(cartId, trip);
        
        return trip;
    }

}
