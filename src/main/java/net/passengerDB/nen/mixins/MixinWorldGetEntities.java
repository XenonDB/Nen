package net.passengerDB.nen.mixins;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.passengerDB.nen.mixins.utils.ASMInjectMethods;

@Mixin(World.class)
public class MixinWorldGetEntities {

	//防止EntityPart在以自身為基準(排除自身)取得周圍實體時，被選進去。
	@Inject(at = {@At(args="log=true", value = "RETURN")}, method = {"getEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/function/Predicate;)Ljava/util/List;"}, cancellable = true)
	private void getEntities(@Nullable Entity p_175674_1_, AxisAlignedBB p_175674_2_, @Nullable Predicate<? super Entity> p_175674_3_, CallbackInfoReturnable<List<Entity>> c) {
		c.setReturnValue(ASMInjectMethods.getEntitiesExcludingSelfBodyPart(c.getReturnValue(), p_175674_1_));
	}
	
}
