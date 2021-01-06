package net.passengerDB.nen.client.renderer.entity;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.passengerDB.nen.entityparts.EntityPart;

@SideOnly(Side.CLIENT)
public class RenderEntityPart extends Render<EntityPart> {

	public static final IRenderFactory<Entity> renderFactory = new IRenderFactory<Entity>() {

		@Override
		public Render createRenderFor(RenderManager manager) {
			return new RenderEntityPart(manager);
		}
		
	};
	
	protected RenderEntityPart(RenderManager renderManager) {
		super(renderManager);
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityPart entity) {
		return null;
	}

	
}
