package net.passengerDB.nen.asm.transformer.raytrace;

import java.util.function.Predicate;

import net.minecraft.entity.Entity;
import net.passengerDB.nen.entityparts.EntityPart;

public class RayTraceExcludeTarget implements Predicate<Entity> {

public static RayTraceExcludeTarget instance = new RayTraceExcludeTarget();
	
	private Entity compared;
	
	private RayTraceExcludeTarget() {
		
	}
	
	public void setComparedEntity(Entity e) {
		compared = e;
	}
	
	@Override
	public boolean test(Entity e) {
		if(e instanceof EntityPart) {
			Entity h = ((EntityPart)e).getHost();
			if(h != null && h.equals(compared)) return true;
		}
		return false;
	}

}
