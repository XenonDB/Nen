package net.passengerDB.nen.entityparts;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.*;
import net.passengerDB.nen.Nen;
import net.passengerDB.nen.entityparts.partsenum.EnumEntityPartType;
import net.passengerDB.nen.utils.NenConfig;
import net.passengerDB.nen.utils.NenLogger;


//TODO: 需要更多測試來確認是否沒有問題
public class EntityPart extends PartEntity<Entity> implements IEntityAdditionalSpawnData {
	
	private static final DataParameter<Float> LEN = EntityDataManager.defineId(EntityPart.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> HEIGHT = EntityDataManager.defineId(EntityPart.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> WIDTH = EntityDataManager.defineId(EntityPart.class, DataSerializers.FLOAT);
	
	private static final DataParameter<Float> RELATIVE_X = EntityDataManager.defineId(EntityPart.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> RELATIVE_Y = EntityDataManager.defineId(EntityPart.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> RELATIVE_Z = EntityDataManager.defineId(EntityPart.class, DataSerializers.FLOAT);
	private static final DataParameter<Boolean> HAS_HOST = EntityDataManager.defineId(EntityPart.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> HOST_ID = EntityDataManager.defineId(EntityPart.class, DataSerializers.INT);
	//private static final DataParameter<Integer> PART_TYPE = EntityDataManager.createKey(EntityPart.class, DataSerializers.VARINT);
	
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
	@OnlyIn(Dist.CLIENT)
	private Entity hostClient;
	
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
	
	public EntityPart(Entity e) {
		super(e);
		this.entityData.define(LEN,Float.valueOf(0.0f));
		this.entityData.define(HEIGHT,Float.valueOf(0.0f));
		this.entityData.define(WIDTH,Float.valueOf(0.0f));
		//不確定為甚麼客戶端的實體位置有時後不會跟伺服端同步
		this.entityData.define(RELATIVE_X,Float.valueOf(0.0f));
		this.entityData.define(RELATIVE_Y,Float.valueOf(0.0f));
		this.entityData.define(RELATIVE_Z,Float.valueOf(0.0f));
		this.entityData.define(HAS_HOST,Boolean.valueOf(false));
		this.entityData.define(HOST_ID,Integer.valueOf(0));
		//this.dataManager.register(PART_TYPE,Integer.valueOf(-1));
	}
	
	public EntityPart(EntityPartsManager p, double[] refSize, boolean powerSrc, boolean controlSrc) {
		this(p.getHost());
		this.manager = p;
		this.isPowerSource = powerSrc;
		this.isControlSource = controlSrc;
		setRefSize(refSize);
		
		Entity h = this.manager.getHost();
		this.setPosition(h.getX(), h.getY(), h.getZ());
	}
	
	public EntityPart(EntityPartsManager p, double[] refSize) {
		this(p,refSize,false,false);
	}
	
	public EntityPartsManager getManager() {
		return manager;
	}
	
	public Entity getHost() {
		if(this.level.isClientSide) {
			if(this.hostClient == null) {
				this.hostClient = this.entityData.get(HAS_HOST).booleanValue() ? this.level.getEntity(this.entityData.get(HOST_ID).intValue()) : null;
			}
			return this.hostClient;
		}
		else {
			//return this.manager != null ? this.manager.getHost() : null;
			return getParent();
		}
	}
	
	protected void entityInit() {
		
	}
	
	//直接取得EntityPartsManager的參考陣列，因此會與manager持有的同步
	protected EntityPart setRefSize(double[] refArr) {
		refHostSize = refArr;
		return this;
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
		this.entityData.set(LEN, Float.valueOf((float) (len)));
		this.entityData.set(HEIGHT, Float.valueOf((float) (height)));
		this.entityData.set(WIDTH, Float.valueOf((float) (width)));
	}
	
	public double[] getSyncBoundingBoxSize() {
		try {
			return new double[]{this.entityData.get(LEN).doubleValue(), this.entityData.get(HEIGHT).doubleValue(), this.entityData.get(WIDTH).doubleValue()};
		}
		catch(NullPointerException exc) {
			exc.printStackTrace();
			return new double[] {0.0,0.0,0.0};
		}
	}
	
	@Override
	public void tick() {
		Entity h = getHost();
		
		if(h == null || !h.isAlive()) {
			this.remove();
		}
		else {
			if(!level.isClientSide) {
				
				this.entityData.set(HAS_HOST, Boolean.valueOf(true));
				this.entityData.set(HOST_ID, Integer.valueOf(h.getId()));
				//this.dataManager.set(PART_TYPE, Integer.valueOf(this.type.ordinal()));
				this.setRot(h.yRot + rotation[0], h.xRot + rotation[1]);
				
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
		super.tick();
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
		return I18n.translateToLocalFormatted(String.format("nen.entity.entitypart.name.%s", this.type.name()), getHost().getName());
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

	@Override
	public ITextComponent getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeSpawnData(PacketBuffer buffer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readSpawnData(PacketBuffer additionalData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void defineSynchedData() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void readAdditionalSaveData(CompoundNBT p_70037_1_) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void addAdditionalSaveData(CompoundNBT p_213281_1_) {
		// TODO Auto-generated method stub
		
	}

	
}
