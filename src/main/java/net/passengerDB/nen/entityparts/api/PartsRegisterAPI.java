package net.passengerDB.nen.entityparts.api;

import java.util.HashMap;
import java.util.Optional;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.entity.PartEntity;
import net.passengerDB.nen.entityparts.EntityPartsManager;
import net.passengerDB.nen.entityparts.partsenum.EntityPartsHuman;

public final class PartsRegisterAPI {
	
	private PartsRegisterAPI() {}
	
	//指定哪一種實體要使用哪一種身體部件組
	static{
		registerPartAssignment(PlayerEntity.class, EntityPartsHuman.class, false);
		registerPartAssignment(ZombieEntity.class, EntityPartsHuman.class, false);
	}
	
	private static HashMap<Class<? extends Entity>,Class<? extends EntityPartsManager>> assignMap = new HashMap<Class<? extends Entity>,Class<? extends EntityPartsManager>>();
	private static HashMap<Class<? extends Entity>,Boolean> canSetManagerCache = new HashMap<>();
	
	/**
	 * 由指定的一種Entity類(Entity的子類別)依序往繼承類尋找是否有可用的EntityPartsManager，先找到先用。若沒找到則會返回空的Optional。
	 * */
	@SuppressWarnings("unchecked")
	public static Optional<Class<? extends EntityPartsManager>> getAssignParts(@Nonnull Class<? extends Entity> e){
		
		Class<? extends EntityPartsManager> result = assignMap.get(e);
		
		while(result == null && e != Entity.class) {
			e = (Class<? extends Entity>) e.getSuperclass();
			result = assignMap.get(e);
		}
		
		return Optional.ofNullable(result);
	}
	
	public static Class<? extends EntityPartsManager> getMappingAssignment(Class<? extends Entity> e){
		return assignMap.get(e);
	}
	
	/**
	 * 判斷是否可以將特定的manager類指定給特定的entity類
	 * */
	@SuppressWarnings("unchecked")
	public static boolean canAssignFor(@Nonnull Class<? extends EntityPartsManager> manager ,@Nonnull Class<? extends Entity> e) {
		
		while(e != Entity.class) {
			if(manager.equals(assignMap.get(e))) return true;
			e = (Class<? extends Entity>) e.getSuperclass();
		}
		
		return false;
	}
	
	public static boolean canSetManagerFor(Class<? extends Entity> e) {
		
		if(e == null) return false;
		
		if(!canSetManagerCache.containsKey(e)) {
			canSetManagerCache.put(e, getAssignParts(e).isPresent());
		}
		
		return canSetManagerCache.get(e);
	}
	
	/**
	 * 註冊哪一類實體需要使用哪種EntityPartsManager。replace = true表示要取代現有的設定，回傳值表示是否註冊成功。
	 * 若replace = true但沒有註冊，或是replace = false且已經註冊，則會回傳false。
	 * **/
	public static boolean registerPartAssignment(@Nonnull Class<? extends Entity> e, @Nonnull Class<? extends EntityPartsManager> m, boolean update) {
		
		if(e == null || m == null) throw new IllegalArgumentException("Don't try register part assignment with null!");
		
		if(PartEntity.class.isAssignableFrom(e)) throw new IllegalArgumentException("Can't register assignment for an entity which is a PartEntity.");
		
		if(assignMap.containsKey(e) != update) return false;
		assignMap.put(e, m);
		
		return true;
	}
	
	/**
	 * 為了方便維護canSetManagerFor的cache，而且想想應該也沒必要
	 * 所以就把移除註冊的方法取消。
	 * */
	/*
	public static boolean removePartAssignment(@Nonnull Class<? extends Entity> e) {
		if(!assignMap.containsKey(e)) return false;
		
		assignMap.remove(e);
		
		return true;
	}
	*/
}
