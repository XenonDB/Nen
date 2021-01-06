package net.passengerDB.nen.client.renderer.aura;

import java.util.HashSet;

import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.passengerDB.nen.client.renderer.entity.RenderEntityPart;
import net.passengerDB.nen.entityparts.EntityPart;
import net.passengerDB.nen.utils.NenLogger;

@SideOnly(Side.CLIENT)
public class RenderAuraHandler {
	
	public static final RenderAuraHandler handler = new RenderAuraHandler();
	private static final HashSet<RenderLivingBase> hasAddedNewLayer = new HashSet();
	
	private RenderAuraHandler() {}
	
	public static void init() {
		
		MinecraftForge.EVENT_BUS.unregister(handler);
		MinecraftForge.EVENT_BUS.register(handler);
		NenLogger.info("Successfully init aura render system.");
		
	}
	
	@SubscribeEvent
	public void addLayerAuraRenderer(RenderLivingEvent.Pre<EntityLivingBase> event) {
		RenderLivingBase<EntityLivingBase> render = event.getRenderer();
		//NenLogger.info(String.format("%s   %s",event.getEntity().toString(),render.toString()));
		
		if(!hasAddedNewLayer.contains(render)) {
			render.addLayer(new LayerAura(render));
			hasAddedNewLayer.add(render);
		}
	}
	
}
