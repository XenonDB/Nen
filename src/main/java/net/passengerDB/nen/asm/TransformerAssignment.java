package net.passengerDB.nen.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import net.passengerDB.nen.asm.transformer.PreventBoatBlockedByPart;
import net.passengerDB.nen.asm.transformer.raytrace.PlayerRayTraceExclude;
import net.passengerDB.nen.asm.transformer.*;

public class TransformerAssignment implements IClassTransformer {
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] clsdata) {
		
		switch(transformedName) {
		case "net.minecraft.client.renderer.EntityRenderer":
			return PlayerRayTraceExclude.transform(name, transformedName, clsdata);
		case "net.minecraft.entity.player.EntityPlayer"://attackTargetEntityWithCurrentItem
			return EnchantmentResponse.transformForPlayer(name, transformedName, clsdata);
		case "net.minecraft.entity.monster.EntityMob"://attackEntityAsMob
			return EnchantmentResponse.transformForMob(name, transformedName, clsdata);
		case "net.minecraft.item.ItemBoat":
			return PreventBoatBlockedByPart.transform(name, transformedName, clsdata);
		default:
			return clsdata;
		}
	}

	
	
}
