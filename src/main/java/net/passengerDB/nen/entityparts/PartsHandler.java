package net.passengerDB.nen.entityparts;

import net.passengerDB.nen.utils.NenLogger;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

public class PartsHandler {

	public static final PartsHandler partEventHandler = new PartsHandler();
	
	/**
	 * 以事件來給EntityPartsManager tick已當作廢案。
	 * */
	/*
	private static final HashMap<Entity,EntityPartsManager> managerInstances = new HashMap<Entity,EntityPartsManager>();
	public static final HashSet<Entity> managerToRemove = new HashSet<Entity>();
	*/
	public static void init() {}
	
	static {
		MinecraftForge.EVENT_BUS.register(partEventHandler);
		NenLogger.info("Successfully init entity part related event handler.");
	}
	
	private PartsHandler() {}
	
	/**
	 * 根據設定生成對應的EntityPartManager來管理一個實際存在生物的部件。
	 * 須注意EntityPartManager僅在伺服端(除了單人世界)生成。
	 * */
	/*
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void createPartsForEntity(EntityJoinWorldEvent e) {
		
		//NenLogger.info(String.format("createPartsForEntity %s", e.getEntity().toString()));
		
		Entity ent = e.getEntity();
		
		if(ent instanceof EntityPart || ent.level.isClientSide) return;
		
		Class entCls = ent.getClass();
		Class<? extends EntityPartsManager> partCls = EntityPartsManager.getAssignParts(entCls);
		while(partCls == null && entCls != Entity.class) {
			entCls = entCls.getSuperclass();
			partCls = EntityPartsManager.getAssignParts(entCls);
		}
		
		if(partCls == null) return;
		
		try {
			managerInstances.put(ent,partCls.getConstructor(Entity.class).newInstance(ent));
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e1) {
			e1.printStackTrace();
		}
		
	}
	
	*/
	
	/**
	 * 讓爆擊傷害能反映在對EntityPart的攻擊上
	 * */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void handleCricitalHit(CriticalHitEvent event) {
		if(event.getTarget() instanceof EntityPart) {
			PlayerEntity attacker = event.getPlayer();
			if(attacker.getAttackStrengthScale(0.5f) > 0.9f && attacker.fallDistance > 0.0f && !attacker.isOnGround() && !attacker.onClimbable() && !attacker.isInWater() && !attacker.hasEffect(Effects.BLINDNESS) && !attacker.isPassenger() && !attacker.isSprinting()) {
				event.setDamageModifier(event.getDamageModifier() * 1.5f);
				event.setResult(Event.Result.ALLOW);
			}
		}
	}
	/*
	@SubscribeEvent
	public void onManagerTick(ServerTickEvent event) {
		EntityPartsManager manager;
		Iterator<EntityPartsManager> tmpitr = managerInstances.values().iterator();
		if(event.phase == TickEvent.Phase.START) {
			try {
				while(true) {
					manager = tmpitr.next();
					manager.preUpdate();
				}
			}
			catch(NoSuchElementException exc) {}
		}
		else if(event.phase == TickEvent.Phase.END) {
			try {
				while(true) {
					manager = tmpitr.next();
					manager.postUpdate();
				}
			}
			catch(NoSuchElementException exc) {}
			
			if(managerToRemove.isEmpty()) return;
			
			managerInstances.keySet().removeAll(managerToRemove);
			
			managerToRemove.clear();
		}
	}
	*/
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void disablePartbeingMounted(EntityMountEvent event) {
		if(event.getEntityBeingMounted() instanceof EntityPart) event.setCanceled(true);
	}
	
	/**
	 * 更新了Mixin中對getEntities處理的相關方法來解決投射物立刻撞到自己的問題。
	 * */
	/*
	@SubscribeEvent
	public void handleProjectileImpactOnPart(ProjectileImpactEvent event) {
		//箭矢及EntityThrowable避免打到發射者的機制不同:箭矢是發射後5tick內忽略發射者
		//EntityThrowable則是發射後2tick內，指定範圍內碰撞到的其中一個實體並永久忽略。而在離開該實體2tick後，才可再次碰撞該實體。
		//當碰到方塊時，不做任何處理，setCancelled(false)
		//當碰到實體時才處理。
		
		RayTraceResult hit = event.getRayTraceResult();
		
		if(!(hit instanceof EntityRayTraceResult)) return;
		
		Entity enthit = ((EntityRayTraceResult) hit).getEntity();
		if(enthit == null) return;
		
		Entity proj = event.getEntity();
		boolean flag = false;
		if(proj instanceof ThrowableEntity) {
			//注意EntityThrowable的RayTraceResult表示碰到實體時，投擲物也可能同時碰到方塊(會優先使用碰到實體的RayTraceResult)
			flag = proj.ticksExisted < 5 && proj.world.rayTraceBlocks(new Vec3d(proj.posX, proj.posY, proj.posZ), new Vec3d(proj.posX + proj.motionX, proj.posY + proj.motionY, proj.posZ + proj.motionZ)) == null;
		}
		else if(hit instanceof EntityPart) {
			flag = proj.ticksExisted < 15;
			Entity target = ((EntityPart)hit).getHost();
			if(proj instanceof EntityArrow) {
				EntityArrow arrow = (EntityArrow) proj;
				flag = flag && arrow.shootingEntity == target;
				if(!flag && target instanceof EntityLivingBase && target.attackEntityFrom(DamageSource.causeArrowDamage(arrow, arrow), 0.1f)) {
					EntityLivingBase liv = (EntityLivingBase) target;
					if (!liv.world.isRemote) liv.setArrowCountInEntity(liv.getArrowCountInEntity() + 1);
					int kbStrength = ((Integer) ReflectionHelper.getFieldValue(EntityArrow.class, arrow, "field_70256_ap")).intValue();
					if (kbStrength > 0)
                    {
                        double f1 = MathHelper.sqrt(arrow.motionX * arrow.motionX + arrow.motionZ * arrow.motionZ);
                        if (f1 > 0.0F) liv.addVelocity(arrow.motionX * kbStrength * 0.6 / f1, 0.1D, arrow.motionZ * kbStrength * 0.6 / f1);
                    }
					ReflectionHelper.invokeMethodFrom(arrow, "func_184548_a", new Class[] {EntityLivingBase.class}, liv);
					if (arrow.shootingEntity != null && liv != arrow.shootingEntity && liv instanceof EntityPlayer && arrow.shootingEntity instanceof EntityPlayerMP)
                    {
                        ((EntityPlayerMP)arrow.shootingEntity).connection.sendPacket(new SPacketChangeGameState(6, 0.0F));
                    }
				}
			}
			else if(proj instanceof EntityFireball) {
				flag = flag && ((EntityFireball) proj).shootingEntity == target;
			}
		}
		event.setCanceled(flag);
	}
	*/
}
