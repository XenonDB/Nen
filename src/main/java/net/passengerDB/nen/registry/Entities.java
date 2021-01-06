package net.passengerDB.nen.registry;

import net.passengerDB.nen.Nen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraft.entity.Entity;
import net.passengerDB.nen.entityparts.EntityPart;
import net.passengerDB.nen.utils.NenLogger;
import net.passengerDB.nen.utils.RegisteryName;

public enum Entities {
	
	//EntityPart可能會給與多種不同的生物甚至是實體來使用，所以其所需的追蹤頻率和範圍(發給客戶端的)可能也不盡相同?這樣是否會有什麼問題?初步先指定為與追蹤玩家相同的數值。
	ENTITYPART(EntityPart.class,"EntityPart",512,2,false);
	
	public static void init() {}
	
	private Entities(Class<? extends net.minecraft.entity.Entity> entityClass, String entityName, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates) {
		entityName = entityName.toLowerCase();
		NenLogger.info(String.format("Register entity: %s, id: %d", entityName, this.ordinal()));
		EntityRegistry.registerModEntity(RegisteryName.getModResourceLocation(entityName), entityClass, RegisteryName.getModRegisteryName(entityName), this.ordinal(), Nen.getInstence(), trackingRange, updateFrequency, sendsVelocityUpdates);
	}
	
}
