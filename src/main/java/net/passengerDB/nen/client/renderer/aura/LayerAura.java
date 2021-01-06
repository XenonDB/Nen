package net.passengerDB.nen.client.renderer.aura;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.passengerDB.nen.utils.ReflectionHelper;

import java.util.Stack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

//TODO: WIP
@SideOnly(Side.CLIENT)
public class LayerAura implements LayerRenderer<EntityLivingBase>{

	public static final ResourceLocation AURA_TEXTURE = new ResourceLocation("textures/entity/creeper/creeper_armor.png");
	//遮罩用途。用來組擋模型上特定部位不作渲染。
	
	public LayerAura(RenderLivingBase m) {
		livingRender = m;
	}
	
	private RenderLivingBase livingRender;
	
	@Override
	public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount,
			float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (true)
        {
			GlStateManager.pushMatrix();
			
			//copy from layerCreeperCharge
			boolean flag = entitylivingbaseIn.isInvisible();
            GlStateManager.depthMask(!flag);
            livingRender.bindTexture(AURA_TEXTURE);
            GlStateManager.matrixMode(5890);
            GlStateManager.loadIdentity();
            float f = (float)entitylivingbaseIn.ticksExisted + partialTicks;
            GlStateManager.translate(f * 0.01F, f * 0.01F, 0.0F);
            GlStateManager.matrixMode(5888);
            GlStateManager.enableBlend();
            GlStateManager.color(0.5F, 0.5F, 0.5F, 1.0F);
            GlStateManager.disableLighting();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
            
            ModelBase model = livingRender.getMainModel();
            /*if(model instanceof ModelPlayer) {
            	ModelPlayer modelb = (ModelPlayer)model;
            	
            	modelb.bipedRightLeg.isHidden = true;
            	modelb.bipedLeftLeg.isHidden = true;
            	//modelb.bipedRightArm.isHidden = true;
            	modelb.bipedLeftArm.isHidden = true;
            	modelb.bipedHeadwear.isHidden = true;
            	//modelb.bipedHead.isHidden = true;
            	modelb.bipedBody.isHidden = true;
            	modelb.bipedLeftLegwear.isHidden = true;
            	modelb.bipedRightLegwear.isHidden = true;
            	modelb.bipedLeftArmwear.isHidden = true;
            	modelb.bipedRightArmwear.isHidden = true;
            	modelb.bipedBodyWear.isHidden = true;
            	
            	GlStateManager.scale(1.3f, 1.3f, 1.3f);
            	//ReflectionHelper.invokeMethodFrom(modelb, "func_78088_a", new Class[] {Entity.class,float.class,float.class,float.class,float.class,float.class,float.class}, entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            	modelb.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            	
            	modelb.bipedRightLeg.isHidden = false;
            	modelb.bipedLeftLeg.isHidden = false;
            	//modelb.bipedRightArm.isHidden = false;
            	modelb.bipedLeftArm.isHidden = false;
            	modelb.bipedHeadwear.isHidden = false;
            	//modelb.bipedHead.isHidden = false;
            	modelb.bipedBody.isHidden = false;
            	modelb.bipedLeftLegwear.isHidden = false;
            	modelb.bipedRightLegwear.isHidden = false;
            	modelb.bipedLeftArmwear.isHidden = false;
            	modelb.bipedRightArmwear.isHidden = false;
            	modelb.bipedBodyWear.isHidden = false;
            	
            }*/
            if(model instanceof ModelBiped) {
            	//mo's bend將雙手、頭及頭飾的部位設成了身體的childmodel。這應該是方便其操作上半身的動作，但對於顯示各別部位的氣場有副作用(原始手、頭、頭飾部位高度過高，以及顯示身體的氣場時會連帶也在這些childmodel部位顯示氣場)。
            	//可能暫時不打算解決這問題，或對於有莊這模組時，直接放棄部位分別，僅顯示整體散發的氣場。
            	ModelBiped modelb = (ModelBiped)model;
            	
            	modelb.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entitylivingbaseIn);
            	
            	GlStateManager.pushMatrix();
            	GlStateManager.scale(1.18f, 1.06f, 1.18f);
            	
            	modelb.bipedRightLeg.render(scale);
            	modelb.bipedLeftLeg.render(scale);
            	modelb.bipedRightArm.render(scale);
            	modelb.bipedLeftArm.render(scale);
            	modelb.bipedHeadwear.render(scale);
            	modelb.bipedHead.render(scale);
            	modelb.bipedBody.render(scale);
            	GlStateManager.popMatrix();
            }
            else {
            	GlStateManager.scale(1.18f, 1.06f, 1.18f);
            	model.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            }
            
            Minecraft.getMinecraft().entityRenderer.setupFogColor(false);
            GlStateManager.matrixMode(5890);
            GlStateManager.loadIdentity();
            GlStateManager.matrixMode(5888);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(flag);
            
            GlStateManager.popMatrix();
        }
		
	}

	@Override
	public boolean shouldCombineTextures() {
		// TODO 不確定這個表示什麼意思
		return false;
	}
	
}
