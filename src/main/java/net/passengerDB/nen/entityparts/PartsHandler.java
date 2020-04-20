package net.passengerDB.nen.entityparts;

import net.passengerDB.nen.utils.NenLogger;
import net.passengerDB.nen.client.renderer.entity.RenderNull;
import net.passengerDB.nen.utils.ReflectionHelper;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityLlamaSpit;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;

public class PartsHandler {

	public static final PartsHandler partEventHandler = new PartsHandler();
	
	private static final HashMap<Entity,EntityPartsManager> managerInstances = new HashMap<Entity,EntityPartsManager>();
	public static final HashSet<EntityPartsManager> managerToRemove = new HashSet<EntityPartsManager>();
	
	public static void init(Side s) {
		MinecraftForge.EVENT_BUS.unregister(partEventHandler);
		MinecraftForge.EVENT_BUS.register(partEventHandler);
		if(s.isClient()) RenderingRegistry.registerEntityRenderingHandler(EntityPart.class, RenderNull.nullRenderFactory);
		NenLogger.info("Successfully init entity part system.");
	}
	
	private PartsHandler() {}
	
	@SubscribeEvent
	public void createPartsForEntity(EntityJoinWorldEvent e) {
		
		//NenLogger.info(String.format("createPartsForEntity %s", e.getEntity().toString()));
		
		Entity ent = e.getEntity();
		
		if(ent instanceof EntityPart) return;
		
		Class entCls = ent.getClass();
		Class<? extends EntityPartsManager> partCls = EntityPartsManager.getAssignParts(entCls);
		while(partCls == null && entCls != Entity.class) {
			entCls = entCls.getSuperclass();
			partCls = EntityPartsManager.getAssignParts(entCls);
		}
		
		if(partCls == null) return;
		
		try {
			if(!e.getWorld().isRemote) {
				managerInstances.put(ent,partCls.getConstructor(Entity.class).newInstance(ent));
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
	}
	/*
	@SubscribeEvent
	public void onInteractEntity(PlayerInteractEvent.EntityInteractSpecific event) {
		NenLogger.info(event.getTarget().toString());
		if(event.getTarget().world.isRemote) NenLogger.info(event.getTarget().serverPosX + " " + event.getTarget().serverPosY + " " + event.getTarget().serverPosZ);
		try {
			throw new Exception("For testing event");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	*/
	@SubscribeEvent
	public void onManagerTick(ServerTickEvent event) {
		EntityPartsManager manager;
		Iterator<EntityPartsManager> tmpitr = managerInstances.values().iterator();
		if(event.phase == Phase.START) {
			try {
				while(true) {
					manager = tmpitr.next();
					manager.preUpdate();
				}
			}
			catch(NoSuchElementException exc) {}
		}
		else if(event.phase == Phase.END) {
			try {
				while(true) {
					manager = tmpitr.next();
					manager.postUpdate();
				}
			}
			catch(NoSuchElementException exc) {}
			
			if(managerToRemove.isEmpty()) return;
			
			tmpitr = managerToRemove.iterator();
			try {
				while(true) {
					manager = tmpitr.next();
					managerInstances.remove(manager.getHost());
				}
			}
			catch(NoSuchElementException exc) {}
			managerToRemove.clear();
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void disablePartbeingMounted(EntityMountEvent event) {
		if(event.getEntityBeingMounted() instanceof EntityPart) event.setCanceled(true);
	}
	
	@SubscribeEvent
	public void handleProjectileImpactOnPart(ProjectileImpactEvent event) {
		//箭矢及EntityThrowable避免打到發射者的機制不同:箭矢是發射後5tick內忽略發射者
		//EntityThrowable則是發射後2tick內，指定範圍內碰撞到的其中一個實體並永久忽略。而在離開該實體2tick後，才可再次碰撞該實體。
		//當碰到方塊時，不做任何處理，setCancelled(false)
		//當碰到實體時才處理。
		Entity hit = event.getRayTraceResult().entityHit;
		if(hit == null) return;
		
		Entity proj = event.getEntity();
		boolean flag = false;
		if(proj instanceof EntityThrowable) {
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
	
}
