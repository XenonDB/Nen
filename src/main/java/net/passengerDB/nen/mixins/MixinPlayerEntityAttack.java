package net.passengerDB.nen.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.mojang.authlib.GameProfile;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.passengerDB.nen.entityparts.EntityPart;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntityAttack extends PlayerEntity{

	public MixinPlayerEntityAttack(World p_i241920_1_, BlockPos p_i241920_2_, float p_i241920_3_, GameProfile p_i241920_4_) {
		super(p_i241920_1_, p_i241920_2_, p_i241920_3_, p_i241920_4_);
	}

	//讓不死剋星、節肢剋星以及相應的剋制增傷型附魔可以根據宿主生效
	@Inject(locals = LocalCapture.PRINT, at = { @At(args="log=true", value = "INVOKE", target = "net.minecraft.entity.player.PlayerEntity.getAttackStrengthScale(F)F") }, method = { "attack(Lnet/minecraft/entity/Entity;)V" })
	private void attack1(Entity p_71059_1_) {
		
	}
	
	//TODO:監聽事件去修正針對EntityPart的暴擊傷害
	
	//TODO:讓EntityPart繼承PartEntity以使hitEntity生效
	
	//使火焰附加可以生效
	@Inject(at = { @At(args="log=true", value = "INVOKE", target = "net.minecraft.entity.player.PlayerEntity.causeFoodExhaustion(F)V") }, method = { "attack(Lnet/minecraft/entity/Entity;)V" })
	private void attack2(Entity p_71059_1_) {
		if(p_71059_1_ instanceof EntityPart) {
			int j = EnchantmentHelper.getFireAspect(this);
			if(j > 0) p_71059_1_.setSecondsOnFire(j * 4);
		}
	}
}
