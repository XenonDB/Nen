package net.passengerDB.nen.mixins;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.entity.MobEntity;

@Mixin(MobEntity.class)
public abstract class MixinMobEntityAttack {

	/**
	 * 暫時不給怪物的直接攻擊做兼容，因為自動控制的怪物總是會直接搜尋並瞄準目標本體來攻擊，而不會去瞄準EntityPart(應該吧)。
	 * */
	
}
