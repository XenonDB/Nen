package net.passengerDB.nen.entityparts;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.passengerDB.nen.entityparts.partsenum.EntityPartsHuman;
import net.passengerDB.nen.entityparts.partsenum.EnumEntityPartType;

public abstract class EntityPartsManager {

	private static HashMap<Class<? extends Entity>,Class<? extends EntityPartsManager>> assignMap = new HashMap<Class<? extends Entity>,Class<? extends EntityPartsManager>>();
	
	public static Class<? extends EntityPartsManager> getAssignParts(Class<? extends Entity> e){
		return assignMap.get(e);
	}
	
	/**
	 * 註冊哪一類實體需要使用哪種EntityPartsManager。replace = true表示要取代現有的設定，回傳值表示是否註冊成功。
	 * 若replace = true但沒有註冊，或是replace = false且已經註冊，則會回傳false。
	 * **/
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
		registerPartAssignment(PlayerEntity.class, EntityPartsHuman.class, false);
		registerPartAssignment(ZombieEntity.class, EntityPartsHuman.class, false);
	}
	
	
	private final Entity host;
	protected HashMap<EnumEntityPartType,EntityPart[]> parts = new HashMap<EnumEntityPartType,EntityPart[]>();//暫時
	private boolean dirtyFlagMotion = false;
	private boolean initialized = false;
	protected double[] refHostSize = new double[3];
	private boolean markRecreateParts = false;
	
	/**
	 * 身體部件的管理者
	 * !!注意:實際的EntityPartsManager只會存在於伺服端，客戶端的EntityPart是根據伺服端操作entityData來同步行為的。因此要注意依賴於此類的資料或功能必須只能在伺服端操作。
	 */
	public EntityPartsManager(@Nonnull Entity host) {
		if(host == null) throw new IllegalArgumentException("Can't construct an EntityPartsManager with null host!");
		this.host = host;
	}
	
	public final Entity getHost() {
		return host;
	}
	
	/**
	 * createParts指定給各部位的參考大小，直接傳陣列值的方式給予，會同步
	 * */
	protected void setRefHostSize(double len, double height, double width) {
		refHostSize[0] = len;
		refHostSize[1] = height;
		refHostSize[2] = width;
	}
	
	public EntityPart[] getParts(EnumEntityPartType t) {
		return this.parts.get(t);
	}
	
	public HashMap<EnumEntityPartType,EntityPart[]> getAllParts(){
		return parts;
	}
	
	public abstract void createParts();
	
	protected void init() {
		if(host.isAddedToWorld()) {
			createParts();
			this.initialized = true;
		} 
	}
	
	public void markRecreateParts(boolean b) {
		synchronized(this) {
			markRecreateParts = b;
		}
	}
	
	private void checkAllPartsInWorld() {
		parts.values().parallelStream().forEach(e -> {
			Arrays.stream(e).parallel().forEach(ee -> {
				if(!this.markRecreateParts && !ee.isAddedToWorld()) markRecreateParts(true);
			});
		});
		if(this.markRecreateParts) reCreateParts();
	}
	
	protected void removeAllParts(boolean flushPartsList) {
		for(Entry<EnumEntityPartType, EntityPart[]> entry : parts.entrySet()) {
			for(EntityPart e : entry.getValue()) {
				e.remove();
			}
		}
		if(flushPartsList) parts = new HashMap<EnumEntityPartType,EntityPart[]>();
	}
	
	public void reCreateParts() {
		removeAllParts(true);
		createParts();
		markRecreateParts(false);
	}
	
	public void preUpdate() {
		updateMotionToHost();
	}
	
	public void postUpdate() {
		
		if(!this.initialized) {
			init();
		}
		if(host.isAlive()) {
			//先暫時取消好了，一直很懷疑是否真的會發生誤入未載入區塊導致斷肢問題。要是真的有的話到時候再想想該怎麼修。
			//if(this.host.level.getGameTime() % 100 == 5) checkAllPartsInWorld();
		}
		else {
			
		}
		
		if(!host.isAddedToWorld()) PartsHandler.managerToRemove.add(host);
		
	}
	
	public void markMotionUpdate() {
		this.dirtyFlagMotion = true;
	}
	
	//注：箭的擊退只針對生物，可能還要找個時間讓此效果也能反映到EntityPart上
	public void updateMotionToHost() {
		if(!this.dirtyFlagMotion) return;
		
		int count = 0;
		double[] motion = new double[3];
		
		Vector3d tmp = null;
		
		for(Entry<EnumEntityPartType, EntityPart[]> entry : parts.entrySet()) {
			for(EntityPart e : entry.getValue()) {
				if(e == host) continue;
				tmp = e.getDeltaMovement();
				if(tmp.x() != 0 || tmp.y() != 0 || tmp.z() != 0) {
					count++;
					motion[0] += tmp.x;
					motion[1] += tmp.y;
					motion[2] += tmp.z;
					e.setDeltaMovement(Vector3d.ZERO);
				}
			}
		}
		//NenLogger.info(String.format("%f %f %f %d", motion[0], motion[1], motion[2], count));
		tmp = host.getDeltaMovement();
		if(tmp.x() != 0 || tmp.y() != 0 || tmp.z() != 0) {
			count++;
			motion[0] += tmp.x;
			motion[1] += tmp.y;
			motion[2] += tmp.z;
		}
		if(count > 0) {
			host.setDeltaMovement(motion[0]/count,motion[1]/count,motion[2]/count);
		}
		
		this.dirtyFlagMotion = false;
	}
	
}
