package net.passengerDB.nen.mixins;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.entity.Entity;
import net.passengerDB.nen.entityparts.EntityPartsManager;
import net.passengerDB.nen.entityparts.IHostable;
import net.passengerDB.nen.utils.asm.ASMInjectMethods;
import net.passengerDB.nen.entityparts.api.*;

@Mixin(Entity.class)
public final class MixinTickingEntityExtra implements IHostable {

	private MixinTickingEntityExtra() {}
	
	private EntityPartsManager partsManager;
	
	@Inject(locals = LocalCapture.CAPTURE_FAILHARD, at = { @At(value = "FIELD", ordinal = 0, target = "net.minecraft.entity.Entity.firstTick:Z", opcode = Opcodes.GETFIELD) }, method = { "baseTick(V)V" })
	private void tickingEntityPartsManager() {
		ASMInjectMethods.handleTickingEntityPartsManager((Entity)((Object)this));
	}

	@Override
	public EntityPartsManager getManager() {
		return partsManager;
	}

	@Override
	public void setManager(EntityPartsManager manager) {
		
		Entity thisEnt = (Entity)((Object) this);
		
		if(!PartsRegisterAPI.canSetManagerFor(thisEnt.getClass())) return;
		
		if(manager != null && !PartsRegisterAPI.canAssignFor(manager.getClass(), thisEnt.getClass())) {
			throw new IllegalArgumentException("Someone try to set a mismatch EntityPartsManager for an entity!!");
		}else {
			this.partsManager = manager;
		}
		
	}
	
}
