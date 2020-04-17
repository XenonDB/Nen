package net.passengerDB.nen.client.renderer.entity;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderNull<T extends Entity> extends Render {
	
	public static final IRenderFactory<Entity> nullRenderFactory = new IRenderFactory<Entity>() {

		@Override
		public Render<Entity> createRenderFor(RenderManager manager) {
			return new RenderNull<Entity>(manager);
		}
		
	};
	
	public RenderNull(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return null;
	}

}
