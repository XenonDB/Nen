package net.passengerDB.nen.entityparts;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.network.datasync.*;
import net.passengerDB.nen.Nen;
import net.passengerDB.nen.entityparts.partsenum.EnumEntityPartType;
import net.passengerDB.nen.utils.NenConfig;
import net.passengerDB.nen.utils.NenLogger;

public class EntityPart extends Entity implements IEntityAdditionalSpawnData {
	
	private static final DataParameter<Float> LEN = EntityDataManager.createKey(EntityPart.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> HEIGHT = EntityDataManager.createKey(EntityPart.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> WIDTH = EntityDataManager.createKey(EntityPart.class, DataSerializers.FLOAT);
	
	private static final DataParameter<Float> RELATIVE_X = EntityDataManager.createKey(EntityPart.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> RELATIVE_Y = EntityDataManager.createKey(EntityPart.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> RELATIVE_Z = EntityDataManager.createKey(EntityPart.class, DataSerializers.FLOAT);
	private static final DataParameter<Boolean> HAS_HOST = EntityDataManager.createKey(EntityPart.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> HOST_ID = EntityDataManager.createKey(EntityPart.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> PART_TYPE = EntityDataManager.createKey(EntityPart.class, DataSerializers.VARINT);
	
	private EntityPartsManager manager;
	//標示該部件是否為本體的動力來源(ex:心臟所在)
	private boolean isPowerSource;
	//標示為是否為控制本體的部件(ex:腦、頭)
	private boolean isControlSource;
	public float dmgFactor = 1.0f;
	private EnumEntityPartType type;
	public boolean canBeCollided = true;
	
	//表示宿主"通常"狀況下hitbox的寬與高，用來做為調整自身hitbox的參考值。
	//分別為x,y,z
	private double[] refHostSize;
	private double[] basicSize;
	
	//給客戶端用來確認自己的宿主是誰
	@SideOnly(Side.CLIENT)
	private Entity host;
	
	private float[] relativeCoord = new float[3];
	private float[] rotation = new float[2];
	/*
	 * 身體部件實體化的部分
	 * 必須滿足:
	 * 1.部件不可被本人(玩家)的準心瞄準
	 * 2.部件可以被投射物射中、瞄準、被其他手段攻擊(ex:爆炸)，如同其他實體般。
	 * 3.部件不執行實體推擠判定。
	 * 4.部件為實體本體部分資訊的實體化，因此實體本體或區塊保存時，不儲存部件。
	 */
	
	public EntityPart(World w) {
		super(w);
		this.forceSpawn = true;
		this.dataManager.register(LEN,Float.valueOf(0.0f));
		this.dataManager.register(HEIGHT,Float.valueOf(0.0f));
		this.dataManager.register(WIDTH,Float.valueOf(0.0f));
		//不確定為甚麼客戶端的實體未制有時後不會跟伺服端同步
		this.dataManager.register(RELATIVE_X,Float.valueOf(0.0f));
		this.dataManager.register(RELATIVE_Y,Float.valueOf(0.0f));
		this.dataManager.register(RELATIVE_Z,Float.valueOf(0.0f));
		this.dataManager.register(HAS_HOST,Boolean.valueOf(false));
		this.dataManager.register(HOST_ID,Integer.valueOf(0));
		this.dataManager.register(PART_TYPE,Integer.valueOf(0));
	}
	
	public EntityPart(EntityPartsManager p, boolean powerSrc, boolean controlSrc) {
		this(p.getHost().world);
		this.manager = p;
		this.isPowerSource = powerSrc;
		this.isControlSource = controlSrc;
		
		Entity h = this.manager.getHost();
		AxisAlignedBB box = h.getEntityBoundingBox();
		refHostSize = new double[]{box.maxX-box.minX, box.maxY-box.minY, box.maxZ-box.minZ};
		this.setPosition(h.posX, h.posY, h.posZ);
	}
	
	public EntityPart(EntityPartsManager p) {
		this(p,false,false);
	}
	
	public EntityPartsManager getManager() {
		return manager;
	}
	
	public Entity getHost() {
		if(this.world.isRemote) {
			if(this.host == null) {
				this.host = this.dataManager.get(HAS_HOST).booleanValue() ? this.world.getEntityByID(this.dataManager.get(HOST_ID).intValue()) : null;
			}
			return this.host;
		}
		else {
			return this.manager != null ? this.manager.getHost() : null;
		}
	}
	
	protected void entityInit() {
		
	}
	
	public EntityPart setDamageFactor(float f) {
		this.dmgFactor = f;
		return this;
	}
	
	public EntityPart setRelativeLocation(float x, float y, float z) {
		this.relativeCoord[0] = x;
		this.relativeCoord[1] = y;
		this.relativeCoord[2] = z;
		return this;
	}
	
	public EntityPart setRelativeAngles(float yaw, float pitch) {
		this.rotation[0] = yaw;
		this.rotation[1] = pitch;
		return this;
	}
	
	public EntityPart setPartSize(double len, double height, double width) {
		basicSize = new double[] {len, height, width};
		return this;
	}
	
	public EntityPart setPartType(EnumEntityPartType t) {
		this.type = t;
		return this;
	}
	
	public EntityPart setCanBeCollidedWith(boolean b) {
		this.canBeCollided = b;
		return this;
	}
	
	public EnumEntityPartType getPartType() {
		return this.type;
	}
	
	private void setSyncBoundingBoxSize(double len, double height, double width) {
		this.dataManager.set(LEN, Float.valueOf((float) (len)));
		this.dataManager.set(HEIGHT, Float.valueOf((float) (height)));
		this.dataManager.set(WIDTH, Float.valueOf((float) (width)));
	}
	
	public double[] getSyncBoundingBoxSize() {
		try {
			return new double[]{this.dataManager.get(LEN).doubleValue(), this.dataManager.get(HEIGHT).doubleValue(), this.dataManager.get(WIDTH).doubleValue()};
		}
		catch(NullPointerException exc) {
			return new double[] {0.0,0.0,0.0};
		}
	}
	
	@Override
	public void onUpdate() {
		Entity h = getHost();
		
		if(h == null || h.isDead) {
			this.setDead();
		}
		else {
			if(!world.isRemote) {
				
				this.dataManager.set(HAS_HOST, Boolean.valueOf(true));
				this.dataManager.set(HOST_ID, Integer.valueOf(h.getEntityId()));
				this.dataManager.set(PART_TYPE, Integer.valueOf(this.type.ordinal()));
				this.setRotation(h.rotationYaw + rotation[0], h.rotationPitch + rotation[1]);
				
				double lr = h.width/this.refHostSize[0];
				double hr = h.height/this.refHostSize[1];
				double wr = h.width/this.refHostSize[2];
				
				double relX = this.relativeCoord[0]*lr;
				double relZ = this.relativeCoord[2]*wr;
				double rad = this.rotationYaw / 180 * Math.PI;
				double cosrad = Math.cos(rad);
				double sinrad = Math.sin(rad);
				double newwidth = this.basicSize[2]*wr;
				double wlDiffer = this.basicSize[0]*lr - this.basicSize[2]*wr;
				
				setSyncBoundingBoxSize(newwidth + wlDiffer*Math.abs(cosrad), this.basicSize[1]*hr, newwidth + wlDiffer*Math.abs(sinrad));
				this.dataManager.set(RELATIVE_X, Float.valueOf((float)(cosrad*relX - sinrad*relZ)));
				this.dataManager.set(RELATIVE_Y, Float.valueOf((float) (this.relativeCoord[1]*hr)));
				this.dataManager.set(RELATIVE_Z, Float.valueOf((float)(sinrad*relX + cosrad*relZ)));
			}
			else {
				if(this.dataManager.isDirty()) {
					EntityTracker.updateServerPosition(this,
							h.posX + this.dataManager.get(RELATIVE_X).doubleValue(),
							h.posY + this.dataManager.get(RELATIVE_Y).doubleValue(),
							h.posZ + this.dataManager.get(RELATIVE_Z).doubleValue());
				}
			}
			this.setPositionAndUpdate(
				h.posX + this.dataManager.get(RELATIVE_X).doubleValue(),
				h.posY + this.dataManager.get(RELATIVE_Y).doubleValue(),
				h.posZ + this.dataManager.get(RELATIVE_Z).doubleValue());
		}
		super.onUpdate();
	}
	
	
	//實現第4點。不確定如果本體和部件處於不同區塊，會不會發生本體斷手之類的事情。
	public void writeEntityToNBT(NBTTagCompound compound) {}
	public void readEntityFromNBT(NBTTagCompound compound) {}
	/*
	@Override
	public boolean writeToNBTAtomically(NBTTagCompound c) {
		return false;
	}
	@Override
	public boolean writeToNBTOptional(NBTTagCompound c) {
		return false;
	}
	*/
	
	@Override
	public void setPosition(double x, double y, double z)
    {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        if (this.isAddedToWorld() && !this.world.isRemote) this.world.updateEntityWithOptionalForce(this, false);
        double[] boxsize = this.getSyncBoundingBoxSize();
        boxsize[0] = boxsize[0]/2;
        boxsize[2] = boxsize[2]/2;
        this.setEntityBoundingBox(new AxisAlignedBB(x - boxsize[0], y, z - boxsize[2], x + boxsize[0], y + boxsize[1], z + boxsize[2]));
    }
	
	//實現第3點
	@Override
	public void applyEntityCollision(Entity e) {}
	
	@Override
	public boolean attackEntityFrom(DamageSource src, float dmg) {
		if(!this.world.isRemote) {
			Entity h = getHost();
			boolean flag = h != null ? h.attackEntityFrom(src, dmg*dmgFactor) : false;
			if(flag) {
				Entity attacker = src.getTrueSource();
				//假如其他模組的附魔，其中的onEntityDamaged或onUserHurt(尤其是onEntityDamaged)
				//其第二個參數有可能傳入EntityPart做處理(不包含使用instanceof或之類的排除處理)，則EntityPart受攻擊時可能導致該附魔實際功能連續發動2次。
				if(attacker instanceof EntityLivingBase)this.applyEnchantments((EntityLivingBase) attacker, h);
				manager.markMotionUpdate();
			}
			return flag;
		}
		return false;
	}
	
	@Override
	public void setFire(int seconds) {
		Entity h = getHost();
		if(h instanceof EntityLivingBase) {
			seconds = EnchantmentProtection.getFireTimeForEntity((EntityLivingBase)h, seconds*20);
			super.setFire((int)Math.ceil(seconds/20.0));
			return;
		}
		super.setFire(seconds);
	}
	
	@Override
	public Entity changeDimension(int dimensionIn, net.minecraftforge.common.util.ITeleporter teleporter) {
		return null;
	}
	
	@Override
	public boolean isPushedByWater() {
		return false;
	}
	
	@Override
	public boolean hasNoGravity() {
		return true;
	}
	
	@Override
	protected boolean canTriggerWalking() {
		return false;
	}
	
	@Override
	public boolean startRiding(Entity entityIn, boolean force) {
		return false;
	}
	
	@Override
	public boolean canBeRidden(Entity entityIn)
    {
        return false;
    }
	
	@Override
	public String getName() {
		return I18n.translateToLocalFormatted(String.format("entity.entitypart.name.%s", this.type.name()), getHost().getName());
	}
	
	@Override
    public boolean canBeCollidedWith() {
        return canBeCollided;
    }
	
	@Override
    public boolean isInvisible(){
        /*if(world.isRemote) {
        	EntityPlayerSP p = Minecraft.getMinecraft().player;
        	flag = (!p.isCreative() && !p.isSpectator() && p != this.host);
        }*/
        return getHost().isInvisible() || super.isInvisible();
    }

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		buffer.writeInt(this.manager != null ? this.manager.getHost().getEntityId() : 0);
		buffer.writeInt(this.type.ordinal());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void readSpawnData(ByteBuf additionalData) {
		this.host = this.world.getEntityByID(additionalData.readInt());
		this.type = EnumEntityPartType.values()[additionalData.readInt()];
	}

	
}
