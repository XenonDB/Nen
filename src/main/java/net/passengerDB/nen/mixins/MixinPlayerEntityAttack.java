package net.passengerDB.nen.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.passengerDB.nen.entityparts.EntityPart;
import net.passengerDB.nen.mixins.utils.ASMInjectMethods;

/**
 * 讓玩家的直接攻擊擊中部位時可以根據宿主做出正確的計算/反應。
 * */
@Mixin(PlayerEntity.class)
public final class MixinPlayerEntityAttack{

	private MixinPlayerEntityAttack() {}
	
	//讓不死剋星、節肢剋星以及相應的剋制增傷型附魔可以根據宿主生效
	@Inject(locals = LocalCapture.CAPTURE_FAILHARD, at = { @At(value = "INVOKE", ordinal = 0, target = "net.minecraft.entity.player.PlayerEntity.getAttackStrengthScale(F)F") }, method = { "attack(Lnet/minecraft/entity/Entity;)V" })
	private void attack1(Entity p_71059_1_, CallbackInfo ci, float f, float f1) {
		//f:角色攻擊力, f1:附魔對目標的攻擊力加成
		f1 = ASMInjectMethods.applyEnchantmentToPartForPlayer((PlayerEntity)(Object)this, p_71059_1_, f1);
	}
	
	//使火焰附加可以生效
	@Inject(at = { @At(value = "INVOKE", target = "net.minecraft.entity.player.PlayerEntity.causeFoodExhaustion(F)V") }, method = { "attack(Lnet/minecraft/entity/Entity;)V" })
	private void attack2(Entity p_71059_1_) {
		if(p_71059_1_ instanceof EntityPart) {
			int j = EnchantmentHelper.getFireAspect((PlayerEntity)(Object)this);
			if(j > 0) p_71059_1_.setSecondsOnFire(j * 4);
		}
	}
}
