package net.passengerDB.nen.entityparts.partsenum;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.passengerDB.nen.entityparts.EntityPart;
import net.passengerDB.nen.entityparts.EntityPartsManager;

public class EntityPartsHuman extends EntityPartsManager {
	
	public EntityPartsHuman(@Nonnull Entity host) {
		super(host);
		setRefHostSize(0.6,1.8,0.6);
	}
	
	public EntityPart getHead() {
		return this.parts.get(EnumEntityPartType.HEAD)[0];
	}
	public EntityPart getRightArm() {
		return this.parts.get(EnumEntityPartType.ARM)[0];
	}
	public EntityPart getLeftArm() {
		return this.parts.get(EnumEntityPartType.ARM)[1];
	}
	public EntityPart getRightLeg() {
		return this.parts.get(EnumEntityPartType.LEG)[0];
	}
	public EntityPart getLeftLeg() {
		return this.parts.get(EnumEntityPartType.LEG)[1];
	}
	public EntityPart getBody() {
		return this.parts.get(EnumEntityPartType.BODY)[0];
	}

	//TODO:將宿主的生物屬性(不死、節肢)反映到部件上
	@Override
	public void createParts() {
		Entity h = getHost();
		this.parts.put(EnumEntityPartType.ARM, new EntityPart[] {new EntityPart(this,this.refHostSize), new EntityPart(this,this.refHostSize)});
		this.parts.put(EnumEntityPartType.LEG, new EntityPart[] {new EntityPart(this,this.refHostSize), new EntityPart(this,this.refHostSize)});
		this.parts.put(EnumEntityPartType.HEAD, new EntityPart[]{new EntityPart(this,this.refHostSize,false,true)});
		this.parts.put(EnumEntityPartType.BODY, new EntityPart[]{new EntityPart(this,this.refHostSize,true,false)});
		
		EntityPart part = getHead();
		part.setRelativeLocation(0.0f, 1.375f, 0.0f).setPartSize(0.6875, 0.5, 0.6875).setPartType(EnumEntityPartType.HEAD).setDamageFactor(1.05f);
		h.world.spawnEntity(part);
		
		part = getRightArm();
		part.setRelativeLocation(-0.375f, 0.6875f, 0.0f).setPartSize(0.3125, 0.75, 0.3125).setPartType(EnumEntityPartType.ARM).setDamageFactor(0.15f);
		h.world.spawnEntity(part);
		
		part = getLeftArm();
		part.setRelativeLocation(0.375f, 0.6875f, 0.0f).setPartSize(0.3125, 0.75, 0.3125).setPartType(EnumEntityPartType.ARM).setDamageFactor(0.15f);
		h.world.spawnEntity(part);
		
		part = getRightLeg();
		part.setRelativeLocation(-0.1875f, 0.0f, 0.0f).setPartSize(0.375, 0.6875, 0.6875).setPartType(EnumEntityPartType.LEG).setDamageFactor(0.25f);
		h.world.spawnEntity(part);
		
		part = getLeftLeg();
		part.setRelativeLocation(0.1875f, 0.0f, 0.0f).setPartSize(0.375, 0.6875, 0.6875).setPartType(EnumEntityPartType.LEG).setDamageFactor(0.25f);
		h.world.spawnEntity(part);
		
		//僅用於顯示氣場用
		part = getBody();
		part.canBeCollided = false;
		part.setRelativeLocation(0.0f, 0.6875f, 0.0f).setPartSize(0.6875, 0.6875, 0.3125).setPartType(EnumEntityPartType.BODY).setDamageFactor(1.0f);
		h.world.spawnEntity(part);
		
	}
	
}
