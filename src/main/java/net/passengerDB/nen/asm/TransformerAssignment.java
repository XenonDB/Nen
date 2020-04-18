package net.passengerDB.nen.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import net.passengerDB.nen.asm.transformer.itemboat.PreventBoatBlockedByPart;
import net.passengerDB.nen.asm.transformer.raytrace.PlayerRayTraceExclude;

public class TransformerAssignment implements IClassTransformer {
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] clsdata) {
		
		switch(transformedName) {
		case "net.minecraft.client.renderer.EntityRenderer":
			return PlayerRayTraceExclude.transform(name, transformedName, clsdata);
		case "net.minecraft.entity.player.EntityPlayer"://attackTargetEntityWithCurrentItem
			return clsdata;
		case "net.minecraft.entity.monster.EntityMob"://attackEntityAsMob
			return clsdata;
		case "net.minecraft.item.ItemBoat":
			return PreventBoatBlockedByPart.transform(name, transformedName, clsdata);
		default:
			return clsdata;
		}
	}

	
	
}
