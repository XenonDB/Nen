package net.passengerDB.nen.entityparts.api;

import net.minecraft.entity.Entity;
import net.passengerDB.nen.entityparts.EntityPartsManager;
import net.passengerDB.nen.entityparts.IHostable;

public final class GeneralEntityPartsAPI {

	private GeneralEntityPartsAPI() {}
	
	public static EntityPartsManager getManager(Entity host) {
		return ((IHostable) host).getManager();
	}
	
	public static void setManager(Entity host, EntityPartsManager m) {
		((IHostable) host).setManager(m);
	}
	
}
