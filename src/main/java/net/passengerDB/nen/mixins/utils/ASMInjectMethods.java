package net.passengerDB.nen.mixins.utils;

import java.util.List;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.passengerDB.nen.entityparts.EntityPart;

public class ASMInjectMethods {

	public static List<Entity> handleGetEntities(List<Entity> list, Entity ref){
		
		if(ref instanceof ProjectileEntity) {
			getEntitiesIncludingSelfBodyPartForProjectile(list, (ProjectileEntity)ref);
		}else {
			getEntitiesExcludingSelfBodyPart(list,ref);
		}
		
		return list;
	}
	
	public static List<Entity> getEntitiesExcludingSelfBodyPart(List<Entity> list, Entity ref){
		
		if(ref != null) list.removeIf(new IsHostEqualTo(ref));
		
		return list;
	}
	
	/**
	 * 這個函數是為了讓投射物「剛投出時無視投擲者」的效果也能作用在EntityPart上的一個方案。
	 * 解法是一個猜測的想法：假如呼叫getEntities時，其參考點是投擲物且其狀態為「尚未離開發射者」，
	 * 那麼可以大膽的假設：此時呼叫這個函數的作用是在「確認是否已離開發射者」。因為從原始碼來看，只要投擲物尚未離開發射者，每tick都會呼叫他來檢查是否已離開發射者。
	 * 所以此時可以將list中的EntityPart，宿主與箭矢發射者相同的部分替換成宿主本體，讓checkLeftOwner也能對EntityPart生效。
	 * */
	public static List<Entity> getEntitiesIncludingSelfBodyPartForProjectile(List<Entity> list, ProjectileEntity proj){
		/**
		 * MC 1.16.5: net/minecraft/world/entity/projectile/Projectile.leftOwner
		 * Name: d => field_234611_d_ => leftOwner
		 * Side: BOTH
		 * AT: public net.minecraft.entity.projectile.ProjectileEntity field_234611_d_ # leftOwner
		 * Type: boolean
		 * */
		
		if(!ObfuscationReflectionHelper.<Boolean,ProjectileEntity>getPrivateValue(ProjectileEntity.class, proj, "field_234611_d_")) {
			Entity owner = proj.getOwner();
			if(list.removeIf(new IsHostEqualTo(owner))) {
				list.add(owner);
			}
		}
		
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
