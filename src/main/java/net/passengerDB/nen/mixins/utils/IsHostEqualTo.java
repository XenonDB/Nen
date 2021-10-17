package net.passengerDB.nen.mixins.utils;

import java.util.function.Predicate;

import net.minecraft.entity.Entity;
import net.passengerDB.nen.entityparts.EntityPart;

public class IsHostEqualTo implements Predicate<Entity> {

	private Entity compared;
	
	public IsHostEqualTo(Entity e) {
		compared = e;
	}
	
	@Override
	public boolean test(Entity e) {
		if(compared != null && e instanceof EntityPart) {
			Entity h = ((EntityPart)e).getHost();
			return (h != null && h.equals(compared));
		}
		return false;
	}
	
}
