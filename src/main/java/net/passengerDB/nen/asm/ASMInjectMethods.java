package net.passengerDB.nen.asm;

import java.util.List;
import java.util.Random;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.passengerDB.nen.asm.transformer.raytrace.ExcludingTarget;
import net.passengerDB.nen.entityparts.EntityPart;
import net.passengerDB.nen.utils.ReflectionHelper;

public class ASMInjectMethods {

	public static List<Entity> getEntitiesExcludingSelfBodyPart(List<Entity> list, Entity ref){
		
		if(ref != null) list.removeIf(new ExcludingTarget(ref));
		
		return list;
	}
	
	public static float applyEnchantmentToPartForPlayer(EntityPlayer p, Entity target, float orig) {
		if(target instanceof EntityPart) {
			Entity h = ((EntityPart) target).getHost();
			if(h instanceof EntityLivingBase) return EnchantmentHelper.getModifierForCreature(p.getHeldItemMainhand(), ((EntityLivingBase)h).getCreatureAttribute());
		}
		return orig;
	}
	
	//部分程序不處理:該次攻擊造成的傷害相關的統計(DAMAGE_DEALT)及處理，因為技術上原因沒有事先記錄host受攻擊前的血量
	//單純只是不想再多插入一段insnlist而已
	public static void attackHandlerForPlayer(EntityPlayer attacker, Entity target) {
		if(target instanceof EntityPart) {
			Entity h = ((EntityPart) target).getHost();
			ItemStack helditem = attacker.getHeldItemMainhand();
			
			target.setFire(EnchantmentHelper.getFireAspectModifier(attacker) * 4);
			
			if (!helditem.isEmpty() && h instanceof EntityLivingBase)
            {
                ItemStack beforeHitCopy = helditem.copy();
                helditem.hitEntity((EntityLivingBase)h, attacker);
                if (helditem.isEmpty())
                {
                    net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(attacker, beforeHitCopy, EnumHand.MAIN_HAND);
                    attacker.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
                }
            }
		}
	}
	
	public static float applyEnchantmentToPartForMob(EntityMob attacker, Entity target, int dmg) {
		if(target instanceof EntityPart) {
			Entity h = ((EntityPart) target).getHost();
			if(h instanceof EntityLivingBase) return (float)(attacker.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue() + EnchantmentHelper.getModifierForCreature(attacker.getHeldItemMainhand(), ((EntityLivingBase)h).getCreatureAttribute()));
		}
		return dmg;
	}
	
	public static void attackHandlerForMob(EntityMob attacker, Entity target) {
		if(target instanceof EntityPart) {
			int kb = EnchantmentHelper.getKnockbackModifier(attacker);
			Entity h = ((EntityPart) target).getHost();
			if(kb > 0 && h instanceof EntityLivingBase) {
				((EntityLivingBase)h).knockBack(attacker, (float)kb * 0.5F, (double)MathHelper.sin(attacker.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(attacker.rotationYaw * 0.017453292F)));
                attacker.motionX *= 0.6D;
                attacker.motionZ *= 0.6D;
			}
			if (h instanceof EntityPlayer)
            {
                EntityPlayer entityplayer = (EntityPlayer)h;
                ItemStack itemstack = attacker.getHeldItemMainhand();
                ItemStack itemstack1 = entityplayer.isHandActive() ? entityplayer.getActiveItemStack() : ItemStack.EMPTY;

                if (!itemstack.isEmpty() && !itemstack1.isEmpty() && itemstack.getItem().canDisableShield(itemstack, itemstack1, entityplayer, attacker) && itemstack1.getItem().isShield(itemstack1, entityplayer))
                {
                    float f1 = 0.25F + (float)EnchantmentHelper.getEfficiencyModifier(attacker) * 0.05F;

                    if (((Random)ReflectionHelper.getFieldValue(Entity.class, attacker, "rand")).nextFloat() < f1)
                    {
                        entityplayer.getCooldownTracker().setCooldown(itemstack1.getItem(), 100);
                        attacker.world.setEntityState(entityplayer, (byte)30);
                    }
                }
            }
		}
	}
	
}
