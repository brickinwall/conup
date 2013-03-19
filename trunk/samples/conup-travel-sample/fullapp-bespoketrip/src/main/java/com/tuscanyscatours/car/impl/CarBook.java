package com.tuscanyscatours.car.impl;

import org.oasisopen.sca.annotation.Remotable;

import com.tuscanyscatours.common.TripItem;

@Remotable
public interface CarBook {
	  String book(TripItem tripItem);
}
