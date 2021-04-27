package net.passengerDB.nen.mixins.utils;

import java.util.List;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.passengerDB.nen.entityparts.EntityPart;

public class ASMInjectMethods {

	public static List<Entity> getEntitiesExcludingSelfBodyPart(List<Entity> list, Entity ref){
		
		if(ref != null) list.removeIf(new ExcludingTarget(ref));
		
		return list;
	}
	
	public static float applyEnchantmentToPartForPlayer(PlayerEntity p, Entity target, float orig) {
		if(target instanceof EntityPart) {
			Entity h = ((EntityPart) target).getHost();
			if(h instanceof LivingEntity) return EnchantmentHelper.getDamageBonus(p.getMainHandItem(), ((LivingEntity)h).getMobType());
		}
		return orig;
	}
}
