package WeepingAngels.Entity;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import WeepingAngels.WeepingAngelsMod;

public class EntityWeepingAngel extends EntityCreature {

	private int spawntimer;
	//private int randomSoundDelay;
	private boolean canSeeSkyAndDay;
	private int torchTimer;
	private int torchNextBreak;
	private boolean breakOnePerTick;
	private boolean didBreak;
	private boolean canTeleport;
	public boolean armMovement;
	public boolean aggressiveArmMovement;

	private float moveSpeed;
	private float maxSpeed = 10.0F, minSpeed = 0.3F;
	private final double closestPlayerRadius = 64D;
	private double distanceToSeen = 5D;

	public EntityWeepingAngel(World world) {
		super(world);
		this.experienceValue = 5;
		this.spawntimer = 5;
		this.isImmuneToFire = true;

		this.targetTasks.addTask(0, new EntityAIHurtByTarget(this, true));

	}

	// ~~~~~~~~~~~~~~~ Entity Mob Properties ~~~~~~~~~~~~~~~~~~~~~~~~~
	@Override
	public void onLivingUpdate() {
		this.updateArmSwingProgress();
		float f = this.getBrightness(1.0F);

		if (f > 0.5F) {
			this.entityAge += 2;
		}

		super.onLivingUpdate();
	}

	@Override
	public void onUpdate() {
		this.moveStrafing = (this.moveForward = 0.0F);
		this.moveSpeed = this.minSpeed;
		//this.setJumping(false);

		if (!this.worldObj.isRemote &&
				this.worldObj.difficultySetting == 0) {
			this.setDead();
		}

		if(this.spawntimer >= 0)
			--this.spawntimer;

		if(this.worldObj.isDaytime()) {
			float f = getBrightness(1.0F);
			if(f > 0.5F && this.worldObj.canBlockSeeTheSky(
					MathHelper.floor_double(this.posX),
					MathHelper.floor_double(this.posY),
					MathHelper.floor_double(this.posZ)) &&
					this.rand.nextFloat() * 30F < (f - 0.4F) * 2.0F)
				this.canSeeSkyAndDay = true;
			else
				this.canSeeSkyAndDay = false;
		}
		
		boolean torchFound = false;
		if(this.getLightValue() > 1.0D && !this.canSeeSkyAndDay) {
			//torchFound = this.findNearestTorch();
			if(!torchFound) {
				if(WeepingAngelsMod.DEBUG)
					WeepingAngelsMod.log.info("No Torches Found");
			}
		}
		
		EntityPlayer player = this.getClosestPlayer();
		if(player != null) {
			this.entityToAttack = player;
		}
		if(this.entityToAttack != null)
			this.moveSpeed = this.maxSpeed;
		else
			this.moveSpeed = this.minSpeed;

		if(this.canBeSeenMulti()) {
			//if(WeepingAngelsMod.DEBUG)
			//	WeepingAngelsMod.log.info("Angel can be seen");
			if(!(this.worldObj.getFullBlockLightValue(
					MathHelper.floor_double(this.posX),
					MathHelper.floor_double(this.posY),
					MathHelper.floor_double(this.posZ)) < 1))
				this.moveSpeed = 0.0F;
		}

		if(this.entityToAttack != null &&
				this.entityToAttack instanceof EntityPlayer &&
				(this.canBeSeenMulti())) {
			if(!this.canSeeSkyAndDay) {
				//this.findNearestTorch();
			}
			if((this.getDistancetoEntityToAttack() > 2D &&
					this.canTeleport)) {
				this.func_35182_c(entityToAttack);
				worldObj.playSoundAtEntity(
						this,
						this.getMovementSound(),
						getSoundVolume() * 1.1f,
						((rand.nextFloat() - rand.nextFloat())
								* 0.2F + 1.0F) * 1.8F);
				this.canTeleport =
						this.entityToAttack != null &&
						this.getDistancetoEntityToAttack() <= (4 * this.distanceToSeen);
			}
		}
		if(this.entityToAttack != null) {
			double d1 = this.entityToAttack.posX-this.posX;
			double d2 = this.entityToAttack.posY-this.posY;
			double d3 = this.entityToAttack.posZ-this.posZ;
			double distance = MathHelper.sqrt_double(d1*d1+d2*d2+d3*d3);
			double closeDis = 13.856D;
			double farDis = 20.785D;
			//if(!this.worldObj.isRemote)
			//if(WeepingAngelsMod.DEBUG) WeepingAngelsMod.log.info(
			//	"Distance to: " + distance);
			if(distance >= farDis) {
				//if(!this.worldObj.isRemote)
				//if(WeepingAngelsMod.DEBUG) WeepingAngelsMod.log.info(
				//	"Calm + Closed");
				this.dataWatcher.updateObject(16, Byte.valueOf((byte)0));
				this.dataWatcher.updateObject(17, Byte.valueOf((byte)0));
			}else{
				if(distance < farDis) {
					//if(!this.worldObj.isRemote)
					//if(WeepingAngelsMod.DEBUG) WeepingAngelsMod.log.info(
					//	"Calm + Open");
					this.dataWatcher.updateObject(16, Byte.valueOf((byte)0));
					this.dataWatcher.updateObject(17, Byte.valueOf((byte)1));
					if(distance < closeDis) {
						//if(!this.worldObj.isRemote)
						//if(WeepingAngelsMod.DEBUG) WeepingAngelsMod.log.info(
						//	"Angry + Open");
						this.dataWatcher.updateObject(16, Byte.valueOf((byte)1));
						this.dataWatcher.updateObject(17, Byte.valueOf((byte)1));
					}
				}
			}
		}



		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed)
		.setAttribute(this.moveSpeed);


		int r = 10;
		double x = this.posX, y = this.posY, z = this.posZ;
		for(int i = (int)x-r; i < x+r; i++) {
			for(int k = (int)z-r; k < z+r; k++) {
				for(int j = (int)y-r; j < y+r; j++) {
					if(this.worldObj.getBlockId(i, j, k) ==
							Block.lavaMoving.blockID ||
							this.worldObj.getBlockId(i, j, k) ==
							Block.lavaStill.blockID) {
						this.heal(0.05F);
					}
				}
			}
		}

		super.onUpdate();
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed)
		.setAttribute(this.minSpeed);
		this.getAttributeMap().func_111150_b(SharedMonsterAttributes.attackDamage);
		//this.getEntityAttribute(SharedMonsterAttributes.attackDamage)
		//		.setAttribute(5D);

	}

	public float getBlockPathWeight(int par1, int par2, int par3) {
		return 0.5F - this.worldObj.getLightBrightness(par1, par2, par3);
	}
	protected boolean isValidLightLevel() {
		int i = MathHelper.floor_double(this.posX);
		int j = MathHelper.floor_double(this.boundingBox.minY);
		int k = MathHelper.floor_double(this.posZ);

		if(this.worldObj.getSavedLightValue(EnumSkyBlock.Sky, i, j, k) >
		this.rand.nextInt(32)) {
			return false;
		}else{
			int l = this.worldObj.getBlockLightValue(i, j, k);

			if (this.worldObj.isThundering()) {
				int i1 = this.worldObj.skylightSubtracted;
				this.worldObj.skylightSubtracted = 10;
				l = this.worldObj.getBlockLightValue(i, j, k);
				this.worldObj.skylightSubtracted = i1;
			}

			return l <= this.rand.nextInt(8);
		}
	}
	public boolean getCanSpawnHere() {
		if(WeepingAngelsMod.spawnRate == 0) return false;
		if(this.worldObj.difficultySetting > 0 &&
				this.isValidLightLevel() &&
				super.getCanSpawnHere()) {
			int i = MathHelper.floor_double(this.posX);
			int j2 = MathHelper.floor_double(this.boundingBox.minY);
			int j1 = MathHelper.floor_double(this.posY + j2);
			int k = MathHelper.floor_double(this.posZ);

			if(j1 <= WeepingAngelsMod.maxSpawnHeight)
				return WeepingAngelsMod.worldSpawnAngels;
		}
		return false;
	}
	private double getLightValue() {
		int i = MathHelper.floor_double(this.posX);
		int j = MathHelper.floor_double(this.boundingBox.minY);
		int k = MathHelper.floor_double(this.posZ);
		return this.worldObj.getBlockLightValue(i, j, k);
	}
	
	// ~~~~~~~~~~~~~~~ Weeping Angel Attributes ~~~~~~~~~~~~~~~~~~~~~~
	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(16, Byte.valueOf((byte)0)); //Angry
		this.dataWatcher.addObject(17, Byte.valueOf((byte)0)); //ArmMovement
	}

	public boolean getAngry() {
		return this.dataWatcher.getWatchableObjectByte(16) == 1; 
	}
	public boolean getArmMovement() {
		return this.dataWatcher.getWatchableObjectByte(17) == 1; 
	}


	// ~~~~~ Freezing the Angel ~~~~~
	private boolean isInFieldOfVision(EntityLivingBase player) {
		if(player == null)
			return false;
		Vec3 vec3 = player.getLookVec();
		Vec3 vec31 = this.worldObj.getWorldVec3Pool().getVecFromPool(
				this.posX - player.posX,
				this.boundingBox.minY +
				(double)(this.height) -
				(player.posY +
						(double)player.getEyeHeight()),
						this.posZ - player.posZ);
		double d0 = vec31.lengthVector();
		vec31 = vec31.normalize();
		double d1 = vec3.dotProduct(vec31);
		return d1 > ((1.0D - 0.025D) / d0) ? player.canEntityBeSeen(this) : false;
	}
	private EntityPlayer getClosestPlayer() {
		List list = worldObj.getEntitiesWithinAABB(EntityPlayer.class,
				boundingBox.expand(
						this.closestPlayerRadius, 20D, this.closestPlayerRadius));
		if(!list.isEmpty())
			return (EntityPlayer)list.get(0);
		return null;
	}
	private boolean canBeSeenMulti() {
		List list = worldObj.getEntitiesWithinAABB(EntityPlayer.class,
				boundingBox.expand(
						this.closestPlayerRadius, 20D, this.closestPlayerRadius));
		int playersWatching = 0;
		for(int j = 0; j < list.size(); j++) {
			EntityPlayer player = (EntityPlayer) list.get(j);
			if(this.isInFieldOfVision(player)) {
				playersWatching++;
			}
		}
		if(playersWatching > 0)
			return true;
		return false;
	}

	// ~~~~~ Freezing the Angel 2~~~~~
	private int[] transparentBlocks = { 20, 8, 9 , 10, 11, 18, 27, 
			28, 30, 31, 32, 37, 38, 39, 
			40, 44, 50, 51, 52, 59, 64, 
			65, 66, 67, 69, 70, 71, 72, 75, 
			76, 77, 78, 83, 85, 90, 92, 96, 
			101, 102, 106, 107, 108, 109, 
			111, 113, 114, 114, 117};
	private boolean isBlockTransparent(int id)
	{
		for(int i = 0; i < this.transparentBlocks.length; i++)
		{
			if(id == this.transparentBlocks[i])
			{
				return true;
			}
		}
		return false;
	}
	private MovingObjectPosition rayTraceBlocks(Vec3 par1Vec3D, Vec3 par2Vec3D)
	{
		boolean par3 = false;
		boolean par4 = false;

		if (Double.isNaN(par1Vec3D.xCoord) || Double.isNaN(par1Vec3D.yCoord) || Double.isNaN(par1Vec3D.zCoord))
		{
			return null;
		}

		if (Double.isNaN(par2Vec3D.xCoord) || Double.isNaN(par2Vec3D.yCoord) || Double.isNaN(par2Vec3D.zCoord))
		{
			return null;
		}

		int i = MathHelper.floor_double(par2Vec3D.xCoord);
		int j = MathHelper.floor_double(par2Vec3D.yCoord);
		int k = MathHelper.floor_double(par2Vec3D.zCoord);
		int l = MathHelper.floor_double(par1Vec3D.xCoord);
		int i1 = MathHelper.floor_double(par1Vec3D.yCoord);
		int j1 = MathHelper.floor_double(par1Vec3D.zCoord);
		int k1 = worldObj.getBlockId(l, i1, j1);
		int i2 = worldObj.getBlockMetadata(l, i1, j1);
		Block block = Block.blocksList[k1];

		if ((!par4 || block == null || block.getCollisionBoundingBoxFromPool(worldObj, l, i1, j1) != null) && k1 > 0 && block.canCollideCheck(i2, par3))
		{
			MovingObjectPosition movingobjectposition = block.collisionRayTrace(worldObj, l, i1, j1, par1Vec3D, par2Vec3D);

			if (movingobjectposition != null)
			{
				return movingobjectposition;
			}
		}

		for (int l1 = 200; l1-- >= 0;)
		{
			if (Double.isNaN(par1Vec3D.xCoord) || Double.isNaN(par1Vec3D.yCoord) || Double.isNaN(par1Vec3D.zCoord))
			{
				return null;
			}

			if (l == i && i1 == j && j1 == k)
			{
				return null;
			}

			boolean flag = true;
			boolean flag1 = true;
			boolean flag2 = true;
			double d = 999D;
			double d1 = 999D;
			double d2 = 999D;

			if (i > l)
			{
				d = (double)l + 1.0D;
			}
			else if (i < l)
			{
				d = (double)l + 0.0D;
			}
			else
			{
				flag = false;
			}

			if (j > i1)
			{
				d1 = (double)i1 + 1.0D;
			}
			else if (j < i1)
			{
				d1 = (double)i1 + 0.0D;
			}
			else
			{
				flag1 = false;
			}

			if (k > j1)
			{
				d2 = (double)j1 + 1.0D;
			}
			else if (k < j1)
			{
				d2 = (double)j1 + 0.0D;
			}
			else
			{
				flag2 = false;
			}

			double d3 = 999D;
			double d4 = 999D;
			double d5 = 999D;
			double d6 = par2Vec3D.xCoord - par1Vec3D.xCoord;
			double d7 = par2Vec3D.yCoord - par1Vec3D.yCoord;
			double d8 = par2Vec3D.zCoord - par1Vec3D.zCoord;

			if (flag)
			{
				d3 = (d - par1Vec3D.xCoord) / d6;
			}

			if (flag1)
			{
				d4 = (d1 - par1Vec3D.yCoord) / d7;
			}

			if (flag2)
			{
				d5 = (d2 - par1Vec3D.zCoord) / d8;
			}

			byte byte0 = 0;

			if (d3 < d4 && d3 < d5)
			{
				if (i > l)
				{
					byte0 = 4;
				}
				else
				{
					byte0 = 5;
				}

				par1Vec3D.xCoord = d;
				par1Vec3D.yCoord += d7 * d3;
				par1Vec3D.zCoord += d8 * d3;
			}
			else if (d4 < d5)
			{
				if (j > i1)
				{
					byte0 = 0;
				}
				else
				{
					byte0 = 1;
				}

				par1Vec3D.xCoord += d6 * d4;
				par1Vec3D.yCoord = d1;
				par1Vec3D.zCoord += d8 * d4;
			}
			else
			{
				if (k > j1)
				{
					byte0 = 2;
				}
				else
				{
					byte0 = 3;
				}

				par1Vec3D.xCoord += d6 * d5;
				par1Vec3D.yCoord += d7 * d5;
				par1Vec3D.zCoord = d2;
			}

			Vec3 vec3d = Vec3.createVectorHelper(par1Vec3D.xCoord, par1Vec3D.yCoord, par1Vec3D.zCoord);
			l = (int)(vec3d.xCoord = MathHelper.floor_double(par1Vec3D.xCoord));

			if (byte0 == 5)
			{
				l--;
				vec3d.xCoord++;
			}

			i1 = (int)(vec3d.yCoord = MathHelper.floor_double(par1Vec3D.yCoord));

			if (byte0 == 1)
			{
				i1--;
				vec3d.yCoord++;
			}

			j1 = (int)(vec3d.zCoord = MathHelper.floor_double(par1Vec3D.zCoord));

			if (byte0 == 3)
			{
				j1--;
				vec3d.zCoord++;
			}

			int j2 = worldObj.getBlockId(l, i1, j1);
			int k2 = worldObj.getBlockMetadata(l, i1, j1);
			Block block1 = Block.blocksList[j2];

			if ((!par4 || block1 == null || block1.getCollisionBoundingBoxFromPool(worldObj, l, i1, j1) != null) && j2 > 0 && block1.canCollideCheck(k2, par3) && !this.isBlockTransparent(j2))
			{
				MovingObjectPosition movingobjectposition1 = block1.collisionRayTrace(worldObj, l, i1, j1, par1Vec3D, par2Vec3D);

				if (movingobjectposition1 != null)
				{
					return movingobjectposition1;
				}
			}
		}

		return null;
	}

	// ~~~~~ Torches ~~~~~
	private boolean findNearestTorch() {
		int i = (int)this.posX;
		int j = (int)this.posY;
		int k = (int)this.posZ;
		int radius = 10;
		int maxRadius = 100;
		for(int i1 = i-radius; i1 < i+radius; i1++) {
			for(int k1 = k-radius; k1 < k+radius; k1++) {
				for(int j1 = j-(radius*2); j1 < j+(radius/2); j1++) {
					if(this.getDistance(i, j, k, i1, j1, k1) < (double)maxRadius) {
						int j3 = worldObj.getBlockId(i1, j1, k1);
						Block block = j3 > 0 ? Block.blocksList[j3] : null;
						if(block != null || block == Block.torchWood) {
							block.dropBlockAsItem(worldObj, i1, j1, k1, 1, 1);
							worldObj.setBlockToAir(i1, j1, k1);
							//worldObj.playSoundAtEntity(
							//		this,
							//		"weepingangels:light",
							//		getSoundVolume(),
							//		((rand.nextFloat() - rand.nextFloat())
							//				* 0.2F + 1.0F) * 1.8F);
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	public double getDistance(int i, int j, int k, int l, int i1, int j1) {
		int k1 = l - i;
		int l1 = i1 - j;
		int i2 = j1 - k;
		return Math.sqrt(k1 * k1 + l1 * l1 + i2 * i2);
	}

	// ~~~~~ Teleporting Angel ~~~~~
	public double getDistancetoEntityToAttack()
	{
		if(entityToAttack instanceof EntityPlayer)
		{
			double d = entityToAttack.posX - posX;
			double d2 = entityToAttack.posY - posY;
			double d4 = entityToAttack.posZ - posZ;
			return (double)MathHelper.sqrt_double(d * d + d2 * d2 + d4 * d4);
		}
		EntityPlayer entityplayer = worldObj.getClosestPlayerToEntity(this, this.closestPlayerRadius);
		if(entityplayer != null)
		{
			double d1 = entityplayer.posX - posX;
			double d3 = entityplayer.posY - posY;
			double d5 = entityplayer.posZ - posZ;
			return (double)MathHelper.sqrt_double(d1 * d1 + d3 * d3 + d5 * d5);
		} else
		{
			return 40000D;
		}
	}
	public String getMovementSound()
	{
		if(entityToAttack != null && (entityToAttack instanceof EntityPlayer) && !isInFieldOfVision((EntityPlayer)this.entityToAttack))
		{
			/*
	            String s = "step.stone";
	            int i = rand.nextInt(4);
	            switch(i)
	            {
	            case 0: // '\0'
	                s = "mob.angel.stoneone";
	                break;

	            case 1: // '\001'
	                s = "mob.angel.stonetwo";
	                break;

	            case 2: // '\002'
	                s = "mob.angel.stonethree";
	                break;

	            case 3: // '\003'
	                s = "mob.angel.stonefour";
	                break;
	            }
	            return s;
			 */
			return "weepingangels:stone";
		} else
		{
			return "";
		}
	}
	/**
	 * Checks area around target I think -Country_Gamer
	 * @param entity
	 * @return
	 */
	protected boolean func_35182_c(Entity entity)
	{
		Vec3 vec3d = Vec3.createVectorHelper(
				posX - entity.posX,
				((boundingBox.minY + (double)(height / 2.0F)) -
						entity.posY) + (double)entity.getEyeHeight(),
						posZ - entity.posZ);
		vec3d = vec3d.normalize();
		double d = 6D;
		double d1 = (posX + (rand.nextDouble() - 0.5D) * 8D) -
				vec3d.xCoord * d;
		double d2 = (posY + (double)(rand.nextInt(16) - 8)) -
				vec3d.yCoord * d;
		double d3 = (posZ + (rand.nextDouble() - 0.5D) * 8D) -
				vec3d.zCoord * d;
		return this.teleportAngel(d1, d2, d3);
	}

	protected boolean teleportAngel(double d, double d1, double d2) {
		double d3 = posX;
		double d4 = posY;
		double d5 = posZ;
		posX = d;
		posY = d1;
		posZ = d2;
		boolean flag = false;
		int i = MathHelper.floor_double(posX);
		int j = MathHelper.floor_double(posY);
		int k = MathHelper.floor_double(posZ);
		if(worldObj.blockExists(i, j, k)) {
			boolean flag1;
			for(flag1 = false; !flag1 && j > 0;) {
				int i1 = worldObj.getBlockId(i, j - 1, k);
				if(i1 == 0 || !Block.blocksList[i1].blockMaterial.isSolid()) {
					posY--;
					j--;
				}else{
					flag1 = true;
				}
			}

			if(flag1) {
				setPosition(posX, posY, posZ);
				if(worldObj.getCollidingBoundingBoxes(
						this, boundingBox).size() == 0 &&
						!worldObj.isAnyLiquid(boundingBox)) {
					flag = true;
				}
			}
		}
		if(!flag) {
			this.setPosition(d3, d4, d5);
			return false;
		}
		int l = 128;
		/* for(int j1 = 0; j1 < l; j1++)
        {
            double d6 = (double)j1 / ((double)l - 1.0D);
            float f = (rand.nextFloat() - 0.5F) * 0.2F;
            float f1 = (rand.nextFloat() - 0.5F) * 0.2F;
            float f2 = (rand.nextFloat() - 0.5F) * 0.2F;
            double d7 = d3 + (posX - d3) * d6 + (rand.nextDouble() - 0.5D) * (double)width * 2D;
            double d8 = d4 + (posY - d4) * d6 + rand.nextDouble() * (double)height;
            double d9 = d5 + (posZ - d5) * d6 + (rand.nextDouble() - 0.5D) * (double)width * 2D;
            worldObj.spawnParticle("portal", d7, d8, d9, f, f1, f2);
        }*/

		return true;
	}

	// ~~~~~ Attacking ~~~~~
	@Override
	protected void attackEntity(Entity entity, float f) {

		if(entityToAttack != null &&
				(entityToAttack instanceof EntityPlayer) &&
				!this.canBeSeenMulti()) {
			EntityPlayer entityPlayer = (EntityPlayer)entityToAttack;

			//Always attack, but teleport sometimes as specified in the config
			super.attackEntity(entity, f);

			if(!entityPlayer.capabilities.isCreativeMode) {
				if(rand.nextInt(100) < WeepingAngelsMod.poisonChance) {
					if(entity instanceof EntityLivingBase) {
						((EntityLivingBase)entity).addPotionEffect(
								new PotionEffect(
										WeepingAngelsMod.angelConvert.id,
										20*60*5, 1));
					}
				}
				if(rand.nextInt(100) < WeepingAngelsMod.teleportChance) {
					if(getDistancetoEntityToAttack() <= 2) {
						worldObj.playSoundEffect(entityToAttack.posX, entityToAttack.posY, entityToAttack.posZ, "mob.ghast.scream", getSoundVolume(), ((rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F) * 1.8F);
						worldObj.playSoundAtEntity(entityToAttack, "weepingangels:teleport_activate", getSoundVolume(), ((rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F) * 1.8F);
						for(int k = 0; k < 5; k++) {
							worldObj.spawnParticle("portal", entityToAttack.posX + (rand.nextDouble() - 0.5D) * (double)width, (entityToAttack.posY + rand.nextDouble() * (double)height) - 0.25D, entityToAttack.posZ + (rand.nextDouble() - 0.5D) * (double)width, (rand.nextDouble() - 0.5D) * 2D, -rand.nextDouble(), (rand.nextDouble() - 0.5D) * 2D);
						}                   

						this.teleportPlayer(entityToAttack);
						for(int k = 0; k < 5; k++) {
							worldObj.spawnParticle("portal", entityToAttack.posX + (rand.nextDouble() - 0.5D) * (double)width, (entityToAttack.posY + rand.nextDouble() * (double)height) - 0.25D, entityToAttack.posZ + (rand.nextDouble() - 0.5D) * (double)width, (rand.nextDouble() - 0.5D) * 2D, -rand.nextDouble(), (rand.nextDouble() - 0.5D) * 2D);
						}
						worldObj.playSoundAtEntity(entityToAttack, "weepingangels:teleport_activate", getSoundVolume(), ((rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F) * 1.8F);
						entityToAttack = null;
					}
				}
			}
		}
	}

	public boolean attackEntityFrom(DamageSource source, float damage) {
		if(source == null) {
			return false;
		}
		if(source.getSourceOfDamage() instanceof EntityPlayer) {
			if(!WeepingAngelsMod.pickOnly)
				super.attackEntityFrom(source, damage);
			else{
				EntityPlayer entityplayer = (EntityPlayer)source.getSourceOfDamage();
				ItemStack itemstack = entityplayer.inventory.getCurrentItem();
				if(worldObj.difficultySetting > 2) {
					if(itemstack != null &&
							(itemstack.itemID == Item.pickaxeDiamond.itemID ||
							itemstack.canHarvestBlock(Block.obsidian))) {
						super.attackEntityFrom(source, damage);
					}
				}else
					if(itemstack != null &&
					(itemstack.itemID == Item.pickaxeDiamond.itemID ||
					itemstack.itemID == Item.pickaxeIron.itemID ||
					(itemstack.canHarvestBlock(Block.oreDiamond) &&
							(itemstack.itemID != Item.pickaxeGold.itemID)))) {
						super.attackEntityFrom(source, damage);
					}
			}
		}
		return false;
	}


	private void teleportPlayer(Entity entity)
	{
		if(entity instanceof EntityPlayer)
		{
			int rangeDifference = 2 * (WeepingAngelsMod.teleportRangeMax - WeepingAngelsMod.teleportRangeMin);
			int offsetX = rand.nextInt(rangeDifference) - rangeDifference/2 + WeepingAngelsMod.teleportRangeMin;
			int offsetZ = rand.nextInt(rangeDifference) - rangeDifference/2 + WeepingAngelsMod.teleportRangeMin;

			//Center the values on a block, to make the boundingbox calculations match less.
			double newX = MathHelper.floor_double(entity.posX) + offsetX + 0.5;
			double newY = rand.nextInt(128);
			double newZ = MathHelper.floor_double(entity.posZ) + offsetZ + 0.5;

			double bbMinX = newX - entity.width / 2.0;
			double bbMinY = newY - entity.yOffset + entity.ySize;
			double bbMinZ = newZ - entity.width / 2.0;
			double bbMaxX = newX + entity.width / 2.0;
			double bbMaxY = newY - entity.yOffset + entity.ySize + entity.height;
			double bbMaxZ = newZ + entity.width / 2.0;

			//FMLLog.info("Teleporting from: "+(int)entity.posX+" "+(int)entity.posY+" "+(int)entity.posZ);
			//FMLLog.info("Teleporting with offsets: "+offsetX+" "+newY+" "+offsetZ);
			//FMLLog.info("Starting BB Bounds: "+bbMinX+" "+bbMinY+" "+bbMinZ+" "+bbMaxX+" "+bbMaxY+" "+bbMaxZ);

			//Use a testing boundingBox, so we don't have to move the player around to test if it is a valid location
			AxisAlignedBB boundingBox = AxisAlignedBB.getBoundingBox(bbMinX, bbMinY, bbMinZ, bbMaxX, bbMaxY, bbMaxZ);

			// Make sure you are trying to teleport to a loaded chunk.
			Chunk teleportChunk = worldObj.getChunkFromBlockCoords((int)newX, (int)newZ);
			if (!teleportChunk.isChunkLoaded)
			{
				worldObj.getChunkProvider().loadChunk(teleportChunk.xPosition, teleportChunk.zPosition);
			}

			// Move up, until nothing intersects the entity anymore
			while (newY > 0 && newY < 128 && !this.worldObj.getCollidingBoundingBoxes(entity, boundingBox).isEmpty())
			{
				++newY;

				bbMinY = newY - entity.yOffset + entity.ySize;
				bbMaxY = newY - entity.yOffset + entity.ySize + entity.height;

				boundingBox.setBounds(bbMinX, bbMinY, bbMinZ, bbMaxX, bbMaxY, bbMaxZ);

				//FMLLog.info("Failed to teleport, retrying at height: "+(int)newY);
			}

			//If we could place it, could we have placed it lower? To prevent teleports really high up.
			do 
			{
				--newY;

				bbMinY = newY - entity.yOffset + entity.ySize;
				bbMaxY = newY - entity.yOffset + entity.ySize + entity.height;

				boundingBox.setBounds(bbMinX, bbMinY, bbMinZ, bbMaxX, bbMaxY, bbMaxZ);

				//FMLLog.info("Trying a lower teleport at height: "+(int)newY);
			}
			while (newY > 0 && newY < 128 && this.worldObj.getCollidingBoundingBoxes(entity, boundingBox).isEmpty());
			//Set Y one higher, as the last lower placing test failed.
			++newY;

			//Check for placement in lava
			//NOTE: This can potentially hang the game indefinitely, due to random recursion
			//However this situation is highly unlikelely
			//My advice: Dont encounter Weeping Angels in seas of lava
			//NOTE: This can theoretically still teleport you to a block of lava with air underneath, but gladly lava spreads ;)
			int blockId = worldObj.getBlockId(MathHelper.floor_double(newX), MathHelper.floor_double(newY), MathHelper.floor_double(newZ));
			if (blockId == 10 || blockId == 11)
			{
				teleportPlayer(entity);
				return;
			}

			//Set the location of the player, on the final position.
			entity.setLocationAndAngles(newX, newY, newZ, entity.rotationYaw, entity.rotationPitch);
			//FMLLog.info("Succesfully teleported to: "+(int)entity.posX+" "+(int)entity.posY+" "+(int)entity.posZ);
		}
	}




}