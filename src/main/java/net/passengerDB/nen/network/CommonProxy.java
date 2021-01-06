package net.passengerDB.nen.network;

import net.passengerDB.nen.entityparts.PartsHandler;
import net.passengerDB.nen.registry.Entities;

public abstract class CommonProxy {

	public final void init() {
		
		PartsHandler.init();
		Entities.init();
		
		sideSpecificInitialization();
	}
	
	public abstract void sideSpecificInitialization();
	
}
