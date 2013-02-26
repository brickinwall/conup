package com.tuscanyscatours.coordination.impl;

import java.util.logging.Logger;

import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

import cn.edu.nju.moon.conup.spi.datamodel.ConupTransaction;

import com.tuscanyscatours.common.TripItem;
import com.tuscanyscatours.common.TripLeg;
import com.tuscanyscatours.coordination.Coordination;
import com.tuscanyscatours.shoppingcart.CartCheckout;
import com.tuscanyscatours.shoppingcart.CartInitialize;
import com.tuscanyscatours.travelcatalog.TravelCatalogSearch;
import com.tuscanyscatours.tripbooking.TripBooking;

@Service(Coordination.class)
public class CoordinationImpl implements Coordination {
	private static Logger LOGGER = Logger.getLogger(CoordinationImpl.class.getName());
	@Reference
	protected TravelCatalogSearch travelCatalogSearch;
	
	@Reference
	protected TripBooking tripBooking;
	
	@Reference
	protected CartInitialize cartInitialize;
	
	@Reference
	protected CartCheckout cartCheckout;
	
	@Override
	@ConupTransaction
	public void coordinate() {
		LOGGER.fine("\nTry to access TravelCatalog#service-binding(TravelCatalogSearch/TravelCatalogSearch):");
		
		TripLeg tripLeg = new TripLeg("a0001", "LGW", "FLR", "06/12/09", "06/12/09", "2");
		TripItem[] tripSearchResults = travelCatalogSearch.search(tripLeg);
		LOGGER.fine("\t" + "travelCatalogSearch.searchSynch(tripLeg)=" + tripSearchResults);
		
		String cartId = cartInitialize.newCart();
		
		for (TripItem tripItem : tripSearchResults) {
			if(!tripItem.getType().equals(TripItem.TRIP)){
				tripItem.setTripItems(new TripItem[]{});
			}
			tripBooking.bookTrip(cartId, tripItem);
		}

		TripItem[] tripItems = cartInitialize.getTrips(cartId);
		LOGGER.fine("All trips in shoppingcart:");
		for (TripItem tripItem : tripItems) {
			LOGGER.fine(tripItem.toString());
		}
		
		cartCheckout.checkout(cartId, "c-0");
	}

}
