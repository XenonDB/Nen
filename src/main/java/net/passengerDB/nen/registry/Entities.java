package net.passengerDB.nen.registry;

import net.minecraft.world.World;
import net.minecraftforge.fml.RegistryObject;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityType.Builder;
import net.passengerDB.nen.entityparts.EntityPart;
import net.passengerDB.nen.utils.NenLogger;
import net.passengerDB.nen.registry.DeferredRegistered;

public enum Entities {
	
	//EntityPart可能會給與多種不同的生物甚至是實體來使用，所以其所需的追蹤頻率和範圍(發給客戶端的)可能也不盡相同?這樣是否會有什麼問題?初步先指定為與追蹤玩家相同的數值。
	ENTITYPART(EntityPart.class,EntityClassification.CREATURE,"EntityPart",512,2,false);
	
	public static void init() {}
	
	private final RegistryObject<EntityType<Entity>> registeredObj;
	
	private Entities(Class<? extends Entity> entityClass, EntityClassification classification, String entityName, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates) {
		this(entityClass, classification, entityName, builder -> builder.setTrackingRange(trackingRange).setUpdateInterval(updateFrequency).setShouldReceiveVelocityUpdates(sendsVelocityUpdates));
	}
	
	private Entities(Class<? extends Entity> entityClass, EntityClassification classification, String entityName, Consumer<EntityType.Builder<?>> extraAttribute) {
		final String finalEntityName = entityName.toLowerCase();
		NenLogger.info(String.format("Register entity: %s, id: %d", entityName, this.ordinal()));
		
		Builder<Entity> builder;
		
		builder = EntityType.Builder.of((type,world) -> {
			try {
				return entityClass.getConstructor(World.class).newInstance(world);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				//不知道會不會遇到例外(理論上不會...吧?)所以先包成非受檢例外。
				throw new RuntimeException(e);
			}
		}, classification);
		
		if(extraAttribute != null) extraAttribute.accept(builder);
		
		this.registeredObj = DeferredRegistered.registeredEntities.register(finalEntityName, () -> builder.build(finalEntityName));
		
	}
	
	public RegistryObject<EntityType<Entity>> getRegisteredObj() {
		return registeredObj;
	}
	
}
