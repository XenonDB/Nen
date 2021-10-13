package net.passengerDB.nen.registry;

import net.minecraft.entity.EntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.passengerDB.nen.Nen;

public class DeferredRegistered {

	static final DeferredRegister<EntityType<?>> registeredEntities =  DeferredRegister.create(ForgeRegistries.ENTITIES, Nen.MODID);
	
}
