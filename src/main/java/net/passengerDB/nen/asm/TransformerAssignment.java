package net.passengerDB.nen.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import net.passengerDB.nen.asm.transformer.raytrace.WorldAABBexcluding;
import net.passengerDB.nen.asm.transformer.*;

public class TransformerAssignment implements IClassTransformer {
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] clsdata) {
		
		switch(transformedName) {
		case "net.minecraft.world.World":
			return WorldAABBexcluding.transform(name, transformedName, clsdata);
		case "net.minecraft.entity.player.EntityPlayer"://attackTargetEntityWithCurrentItem
			return EnchantmentResponse.transformForPlayer(name, transformedName, clsdata);
		case "net.minecraft.entity.monster.EntityMob"://attackEntityAsMob
			return EnchantmentResponse.transformForMob(name, transformedName, clsdata);
		default:
			return clsdata;
		}
	}

	
	
}
