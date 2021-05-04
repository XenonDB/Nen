package net.passengerDB.nen.entityparts;

import java.nio.charset.Charset;
import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.*;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.passengerDB.nen.Nen;
import net.passengerDB.nen.entityparts.partsenum.EnumEntityPartType;
import net.passengerDB.nen.utils.NenLogger;


//TODO: 需要更多測試來確認是否沒有問題
public class EntityPart extends PartEntity<Entity> implements IEntityAdditionalSpawnData {
	
	private static final DataParameter<Float> LEN = EntityDataManager.defineId(EntityPart.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> HEIGHT = EntityDataManager.defineId(EntityPart.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> WIDTH = EntityDataManager.defineId(EntityPart.class, DataSerializers.FLOAT);
	
	private static final DataParameter<Float> RELATIVE_X = EntityDataManager.defineId(EntityPart.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> RELATIVE_Y = EntityDataManager.defineId(EntityPart.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> RELATIVE_Z = EntityDataManager.defineId(EntityPart.class, DataSerializers.FLOAT);
	
	//private static final DataParameter<Boolean> HAS_HOST = EntityDataManager.defineId(EntityPart.class, DataSerializers.BOOLEAN);
	//private static final DataParameter<Integer> HOST_ID = EntityDataManager.defineId(EntityPart.class, DataSerializers.INT);
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
	//於建構式中初始化，由EntityPartsManager傳入宿主的體型參考陣列進來。
	//分別為x,y,z
	private double[] refHostSize;
	private double[] basicSize;
	
	//給客戶端用來確認自己的宿主是誰
	@OnlyIn(Dist.CLIENT)
	private Entity hostClient;
	
	//表示該身體部件相對於宿主的位置
	private float[] relativeCoord = new float[3];
	
	//表示"上一次更新時，宿主的hitbox大小以及宿主的轉向"。用來作為是否更新EntityPart狀態的參考。
	private AxisAlignedBB lastBB;
	private float[] lastRotation;
	
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
	}
	
	public EntityPart(EntityPartsManager p, double[] refSize, boolean powerSrc, boolean controlSrc) {
		this(p.getHost());
		this.manager = p;
		this.isPowerSource = powerSrc;
		this.isControlSource = controlSrc;
		setRefSize(refSize);
		
		Entity h = this.manager.getHost();
		this.moveTo(h.getX(), h.getY(), h.getZ());
	}
	
	public EntityPart(EntityPartsManager p, double[] refSize) {
		this(p,refSize,false,false);
	}
	
	public EntityPartsManager getManager() {
		return manager;
	}
	
	public Entity getHost() {
		if(this.level.isClientSide) {
			/*if(this.hostClient == null) {
				this.hostClient = this.entityData.get(HAS_HOST).booleanValue() ? this.level.getEntity(this.entityData.get(HOST_ID).intValue()) : null;
			}*/
			return this.hostClient;
		}
		else {
			//return this.manager != null ? this.manager.getHost() : null;
			return getParent();
		}
	}
	
	/**
	 * 直接取得EntityPartsManager的參考陣列，因此會與manager持有的同步
	 * */
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
	
	/**
	 * bounding box包含了座標的訊息(其分別記錄box的x y z絕對座標的起點和終點)並且為final。
	 * 因此宿主座標、大小變了後，bounding box肯定不會是原本的那個box。
	 * */
	private boolean needUpdateSyncData() {
		Entity h = getHost();
		AxisAlignedBB box = h.getBoundingBox();
		if(lastRotation == null) {
			lastRotation = new float[2];
		}
		if(lastRotation[0] == h.yRot && lastRotation[1] == h.xRot && lastBB == box) {
			return false;
		}
		lastRotation[0] = h.yRot;
		lastRotation[1] = h.xRot;
		lastBB = box;
		return true;
	}
	
	private void tryUpdateSyncData() {
		
		if(!needUpdateSyncData()) return;
		
		double lr = lastBB.getXsize()/this.refHostSize[0];
		double hr = lastBB.getYsize()/this.refHostSize[1];
		double wr = lastBB.getZsize()/this.refHostSize[2];
		
		double relX = this.relativeCoord[0]*lr;
		double relZ = this.relativeCoord[2]*wr;
		double rad = lastRotation[0] / 180 * Math.PI;
		double cosrad = Math.cos(rad);
		double sinrad = Math.sin(rad);
		double newwidth = this.basicSize[2]*wr;
		double wlDiffer = this.basicSize[0]*lr - this.basicSize[2]*wr;
		
		setSyncBoundingBoxSize(newwidth + wlDiffer*Math.abs(cosrad), this.basicSize[1]*hr, newwidth + wlDiffer*Math.abs(sinrad));
		this.entityData.set(RELATIVE_X, Float.valueOf((float)(cosrad*relX - sinrad*relZ)));
		this.entityData.set(RELATIVE_Y, Float.valueOf((float) (this.relativeCoord[1]*hr)));
		this.entityData.set(RELATIVE_Z, Float.valueOf((float)(sinrad*relX + cosrad*relZ)));
	}
	
	private void updateLocalStatus() {
		
		Entity h = getHost();
		
		if(!this.entityData.isDirty()) return;
		
		double hx = h.getX();
		double hy = h.getY();
		double hz = h.getZ();
		
		this.setPosAndOldPos(
				hx + this.entityData.get(RELATIVE_X).doubleValue(),
				hy + this.entityData.get(RELATIVE_Y).doubleValue(),
				hz + this.entityData.get(RELATIVE_Z).doubleValue());
		this.yRot = h.yRot;
		this.xRot = h.xRot;
		
		double[] boxsize = getSyncBoundingBoxSize();
		
		setBoundingBox(new AxisAlignedBB(hx - boxsize[0]/2, hy, hz - boxsize[2]/2, hx + boxsize[0]/2, hy + boxsize[1], hz + boxsize[2]/2));
		
	}
	
	//TODO: 將EntityPart是否可以在身體之外甚至另一維度保持的判定交由宿主本身決定
	@Override
	public void tick() {
		Entity h = getHost();
		
		if(h == null || !h.isAlive()) {
			this.remove();
		}
		else {
			if(!level.isClientSide) tryUpdateSyncData();
			updateLocalStatus();
		}
		super.tick();
	}
	
	
	//實現第4點。不確定如果本體和部件處於不同區塊，會不會發生本體斷手之類的事情。
	public void load(CompoundNBT compound) {}
	public CompoundNBT saveWithoutId(CompoundNBT compound) {
		return compound;
	}
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
	
	//實現第3點
	@Override
	public void push(Entity e) {}
	
	@Override
	public boolean hurt(DamageSource src, float dmg) {
		if(!this.level.isClientSide) {
			Entity h = getHost();
			boolean flag = h != null ? h.hurt(src, dmg*dmgFactor) : false;
			if(flag) {
				Entity attacker = src.getDirectEntity();
				//假如其他模組的附魔，其中的onEntityDamaged或onUserHurt(尤其是onEntityDamaged)
				//其第二個參數有可能傳入EntityPart做處理(不包含使用instanceof或之類的排除處理)，則EntityPart受攻擊時可能導致該附魔實際功能連續發動2次。
				if(attacker instanceof LivingEntity)this.doEnchantDamageEffects((LivingEntity) attacker, h);
				manager.markMotionUpdate();
			}
			return flag;
		}
		return false;
	}
	
	@Override
	public void setSecondsOnFire(int seconds) {
		Entity h = getHost();
		seconds *= 20;
		if(h instanceof LivingEntity) {
			seconds = ProtectionEnchantment.getFireAfterDampener((LivingEntity)h, seconds);
		}
		if(this.getRemainingFireTicks() < seconds) {
			this.setRemainingFireTicks(seconds);
		}
	}
	
	/**
	 * EntityPart是宿主身上部分資料的實體化。因此設定嘗試穿越維度的時候將直接移除該Part。
	 * 等宿主到達另一維度後再由PartsManager重新建立。
	 * 如果有需要設計肢體錯位或斷手斷腳(?)的狀況，則用EntityPartsManager自行實作。
	 * */
	@Override
	public Entity changeDimension(ServerWorld p_241206_1_, ITeleporter teleporter) {
		return null;
	}
	
	@Override
	public boolean isPushedByFluid() {
		return false;
	}
	
	@Override
	public boolean isNoGravity() {
		return true;
	}
	
	@Override
	protected boolean isMovementNoisy() {
		return false;
	}
	
	@Override
	public boolean startRiding(Entity entityIn, boolean force) {
		return false;
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
	public void writeSpawnData(PacketBuffer buffer) {
		buffer.writeInt(this.manager != null ? this.manager.getHost().getId() : 0);
		buffer.writeInt(this.type.ordinal());		
	}

	@Override
	@OnlyIn(value = Dist.CLIENT)
	public void readSpawnData(PacketBuffer additionalData) {
		this.hostClient = this.level.getEntity(additionalData.readInt());
		this.type = EnumEntityPartType.values()[additionalData.readInt()];		
	}

	@Override
	public ITextComponent getCustomName() {
		String name = I18n.get(String.format("nen.entity.entitypart.name.%s", this.type.name()), getHost().getName());
		return ITextComponent.Serializer.fromJson(String.format("{\"text\":\"%s\"}", name));
	}
	
	@Override
	protected void defineSynchedData() {
		this.entityData.define(LEN,Float.valueOf(0.0f));
		this.entityData.define(HEIGHT,Float.valueOf(0.0f));
		this.entityData.define(WIDTH,Float.valueOf(0.0f));
		//不確定為甚麼客戶端的實體位置有時後不會跟伺服端同步
		this.entityData.define(RELATIVE_X,Float.valueOf(0.0f));
		this.entityData.define(RELATIVE_Y,Float.valueOf(0.0f));
		this.entityData.define(RELATIVE_Z,Float.valueOf(0.0f));
		
		//this.entityData.define(HAS_HOST,Boolean.valueOf(false));
		//this.entityData.define(HOST_ID,Integer.valueOf(0));
		//this.dataManager.register(PART_TYPE,Integer.valueOf(-1));
	}

	@Override
	protected void readAdditionalSaveData(CompoundNBT p_70037_1_) {}

	@Override
	protected void addAdditionalSaveData(CompoundNBT p_213281_1_) {}
	
	/**
	 * 阻擋某些hitbox或是刷新實體大小的事件，因為EntityPart是根據宿主動態計算的
	 * */
	
	@Override
	public void onSyncedDataUpdated(DataParameter<?> p_184206_1_) {}
	
	@Override
	public void refreshDimensions() {}
	
}
