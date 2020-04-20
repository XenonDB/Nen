package net.passengerDB.nen.entityparts;

import java.util.HashMap;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.passengerDB.nen.entityparts.partsenum.EntityPartsHuman;
import net.passengerDB.nen.entityparts.partsenum.EnumEntityPartType;
import net.passengerDB.nen.utils.NenLogger;

public abstract class EntityPartsManager {

	private static HashMap<Class<? extends Entity>,Class<? extends EntityPartsManager>> assignMap = new HashMap<Class<? extends Entity>,Class<? extends EntityPartsManager>>();
	
	public static Class<? extends EntityPartsManager> getAssignParts(Class<? extends Entity> e){
		return assignMap.get(e);
	}
	
	public static boolean registerPartAssignment(Class<? extends Entity> e, Class<? extends EntityPartsManager> m, boolean replace) {
		if(assignMap.containsKey(e) != replace) return false;
		assignMap.put(e, m);
		return true;
	}
	
	public static boolean removePartAssignment(Class<? extends Entity> e) {
		if(!assignMap.containsKey(e)) return false;
		assignMap.remove(e);
		return true;
	}
	
	//指定哪一種實體要使用哪一種身體部件組
	static{
		assignMap.put(EntityPlayer.class, EntityPartsHuman.class);
		assignMap.put(EntityZombie.class, EntityPartsHuman.class);
	}
	
	
	private final Entity host;
	protected HashMap<EnumEntityPartType,EntityPart[]> parts = new HashMap<EnumEntityPartType,EntityPart[]>();//暫時
	private boolean dirtyFlagMotion = false;
	private boolean initialized = false;
	/*
	 * 身體部件的管理者
	 */
	
	public EntityPartsManager(@Nonnull Entity host) {
		if(host == null) throw new IllegalArgumentException("Can't construct an EntityPartsManager with null host!");
		this.host = host;
	}
	
	public final Entity getHost() {
		return host;
	}
	
	public Entity getBody() {
		return host;
	}
	
	public EntityPart[] getParts(EnumEntityPartType t) {
		return this.parts.get(t);
	}
	
	public HashMap<EnumEntityPartType,EntityPart[]> getAllParts(){
		return parts;
	}
	
	public abstract void createParts();
	
	protected void init() {
		if(host.addedToChunk) {
			createParts();
			this.initialized = true;
		} 
	}
	
	private void checkAllPartsInWorld() {
		for(Entry<EnumEntityPartType, EntityPart[]> entry : parts.entrySet()) {
			for(EntityPart e : entry.getValue()) {
				if(!e.isAddedToWorld()) {
					reCreateParts();
					break;
				}
			}
		}
	}
	
	protected void removeAllParts(boolean flushPartsList) {
		for(Entry<EnumEntityPartType, EntityPart[]> entry : parts.entrySet()) {
			for(EntityPart e : entry.getValue()) {
				e.setDead();
			}
		}
		if(flushPartsList) parts = new HashMap<EnumEntityPartType,EntityPart[]>();
	}
	
	public void reCreateParts() {
		removeAllParts(true);
		createParts();
	}
	
	public void preUpdate() {
		updateMotionToHost();
	}
	
	public void postUpdate() {
		
		if(!this.initialized) {
			init();
		}
		if(host.isDead) {
			removeAllParts(false);
			PartsHandler.managerToRemove.add(this);
			return;
		}
		if(this.host.world.getTotalWorldTime() % 100 == 5) checkAllPartsInWorld();
	}
	
	public void markMotionUpdate() {
		this.dirtyFlagMotion = true;
	}
	
	//注：箭的擊退只針對生物，可能還要找個時間讓此效果也能反映到EntityPart上
	public void updateMotionToHost() {
		if(!this.dirtyFlagMotion) return;
		
		int count = 0;
		double[] motion = new double[3];
		for(Entry<EnumEntityPartType, EntityPart[]> entry : parts.entrySet()) {
			for(EntityPart e : entry.getValue()) {
				if(e == host) continue;
				if(e.motionX != 0 || e.motionY != 0 || e.motionZ != 0) {
					count++;
					motion[0] += e.motionX;
					motion[1] += e.motionY;
					motion[2] += e.motionZ;
					e.motionX = 0.0;
					e.motionY = 0.0;
					e.motionZ = 0.0;
				}
			}
		}
		//NenLogger.info(String.format("%f %f %f %d", motion[0], motion[1], motion[2], count));
		if(host.velocityChanged) {
			count++;
			motion[0] += host.motionX;
			motion[1] += host.motionY;
			motion[2] += host.motionZ;
		}
		if(count > 0) {
			host.motionX = motion[0]/count;
			host.motionY = motion[1]/count;
			host.motionZ = motion[2]/count;
			host.isAirBorne = true;
			host.velocityChanged = true;
		}
		
		this.dirtyFlagMotion = false;
	}
	
}
