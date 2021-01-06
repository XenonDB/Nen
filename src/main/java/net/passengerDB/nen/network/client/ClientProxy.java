package net.passengerDB.nen.network.client;

import net.passengerDB.nen.network.CommonProxy;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.passengerDB.nen.client.renderer.entity.RenderEntityPart;
import net.passengerDB.nen.client.renderer.aura.*;
import net.passengerDB.nen.entityparts.EntityPart;

public class ClientProxy extends CommonProxy {

	@Override
	public void sideSpecificInitialization() {
		RenderingRegistry.registerEntityRenderingHandler(EntityPart.class, RenderEntityPart.renderFactory);
		//TODO: WIP
		//RenderAuraHandler.init();
	}

}
