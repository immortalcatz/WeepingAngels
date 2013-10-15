package WeepingAngels.Entity;

import java.util.List;

import WeepingAngels.WeepingAngelsMod;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class EntityWeepingAngel extends EntityMob {

	private int attackStrength;
	private final int health = 15;
	
	private int[] transparentBlocks = { 20, 8, 9 , 10, 11, 18, 27, 
			28, 30, 31, 32, 37, 38, 39, 
			40, 44, 50, 51, 52, 59, 64, 
			65, 66, 67, 69, 70, 71, 72, 75, 
			76, 77, 78, 83, 85, 90, 92, 96, 
			101, 102, 106, 107, 108, 109, 
			111, 113, 114, 114, 117};
	private int spawntimer;
	private int randomSoundDelay;
	

	private int torchTimer;
	private int torchNextBreak;
	private boolean breakOnePerTick;
	private boolean canSeeSkyAndDay;
	private float moveSpeed;
	private float maxSpeed = 7F, minSpeed = 0.3F;
	
	private int timeTillNextTeleport;
	private double distanceToSeen = 5D;
	private int slowPeriod;
	
	public EntityWeepingAngel(World world) {
		super(world);
		//this.ai();
		
		this.attackStrength = WeepingAngelsMod.attackStrength;
		this.setHealth(this.health);
		this.spawntimer = 5;
		
	}
	
	private void ai() {
		this.isImmuneToFire = true;
		this.stepHeight = 1.0F;
		// avoid water
		this.getNavigator().setAvoidsWater(true);
		
		this.tasks.addTask(0,
				new EntityAIAttackOnCollide(
						this, EntityPlayer.class, 1.0D, false));
		this.tasks.addTask(1,
				new EntityAIAttackOnCollide(
						this, EntityVillager.class, 1.0D, true));
		
		// look closest
		this.tasks.addTask(2,
				new EntityAIWatchClosest(
						this, EntityPlayer.class, 6.0F));
		// look idle
		this.tasks.addTask(3,
				new EntityAILookIdle(this));
		
		this.targetTasks.addTask(0,
				new EntityAIHurtByTarget(this, true));
		// go after player
		this.targetTasks.addTask(1,
				new EntityAINearestAttackableTarget(
						this, EntityPlayer.class, 0, true));
		// go after statues
		
		// go after villagers
		this.targetTasks.addTask(3,
				new EntityAINearestAttackableTarget(
						this, EntityVillager.class, 0, false));
		
        
	}
	
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(this.health);
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.23000000417232513D);
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(this.attackStrength);
	}
	public boolean canBreatheUnderwater() { return true; }
	public int getAttackStrength(Entity par1Entity) {
		return attackStrength;
	}
	
	@Override
	public int getMaxSpawnedInChunk() { return 10; }
	
	public String getMovementSound() {
		if(entityToAttack != null &&
				(entityToAttack instanceof EntityPlayer) &&
				!isInFieldOfVision(
						this, (EntityPlayer)entityToAttack, 70, 65)
				) {
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
		}else{
			return "";
		}
	}
	@Override
	protected String getLivingSound() {
		if(rand.nextInt(10) > 8) {
			return getMovementSound();
		}
		return "";
	}
	@Override
	protected String getHurtSound() {
		return "weepingangels:stone";
	}
	@Override
	protected String getDeathSound() {
		return "weepingangels:crumble";
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(16, Byte.valueOf((byte)0)); //Angry
		this.dataWatcher.addObject(17, Byte.valueOf((byte)0)); //ArmMovement
	}

	@Override
	protected int getDropItemId() { return Block.cobblestone.blockID; }
	@Override
	protected void dropFewItems(boolean par1, int par2) {
		int i = rand.nextInt(2 + par2);
		for (int k = 0; k < i; k++) {
			this.dropItem(4, 1);
		}
	}
	@Override
	protected void dropRareDrop(int par1) {
		this.dropItem(WeepingAngelsMod.statue.itemID, 1);
	}
	
	// ~~~~~~~~~~~ Finding Players ~~~~~~~~~~~~~~~~
	@Override
	protected Entity findPlayerToAttack() {
		if(spawntimer < 0){
			EntityPlayer entityplayer = worldObj.getClosestPlayerToEntity(this, 64D);
			if(entityplayer != null && canAngelBeSeenMultiplayer()) {
				return entityplayer;
			}else{
				return null;
			}
		} 
		return null;
	}
	private boolean canAngelBeSeen(EntityPlayer entity1) {
		if(worldObj.getFullBlockLightValue(
				MathHelper.floor_double(posX),
				MathHelper.floor_double(posY),
				MathHelper.floor_double(posZ)) < 1) {
			this.randomSoundDelay = rand.nextInt(40);
			return false;
		}
		if(entity1.canEntityBeSeen(this) || LineOfSightCheck(entity1)) {
			return isInFieldOfVision(this, entity1, 70, 65);
		}else{
			return false;
		}
	}
	private boolean canAngelBeSeenMultiplayer() {
		if(worldObj.getFullBlockLightValue(
				MathHelper.floor_double(posX),
				MathHelper.floor_double(posY),
				MathHelper.floor_double(posZ)) < 1) {
			this.randomSoundDelay = rand.nextInt(40);
			return false;
		}
		int i = 0;
		List list = worldObj.getEntitiesWithinAABB(
				EntityPlayer.class, boundingBox.expand(64D, 20D, 64D));
		for(int j = 0; j < list.size(); j++) {
			EntityPlayer entity1 = (EntityPlayer)list.get(j);
			if(entity1 instanceof EntityPlayer) {
				if(canAngelBeSeen(entity1)) {
					i++;
				}
			}
		}
		if(i > 0) {
			return true;
		}else{
			return false;
		}
	}
	
	//~~~~~~~~~~ Movemnt Sound Calculations ~~~~~~~~~~
	private boolean isInFieldOfVision(Entity entityweepingangel, EntityPlayer entityToAttack, float f4i, float f5i) {
		float f = entityToAttack.rotationYaw;
		float f1 = entityToAttack.rotationPitch;
		entityToAttack.attackEntityAsMob(entityToAttack);
		float f2 = entityToAttack.rotationYaw;
		float f3 = entityToAttack.rotationPitch;
		entityToAttack.rotationYaw = f;
		entityToAttack.rotationPitch = f1;
		f = f2;
		f1 = f3;
		float f4 = f4i; // 70f
		float f5 = f5i; // 65f
		float f6 = entityToAttack.rotationYaw - f4;
		float f7 = entityToAttack.rotationYaw + f4;
		float f8 = entityToAttack.rotationPitch - f5;
		float f9 = entityToAttack.rotationPitch + f5;
		boolean flag = GetFlag(f6, f7, f, 0.0F, 360F);
		boolean flag1 = GetFlag(f8, f9, f1, -180F, 180F);
		return flag && flag1 && (entityToAttack.canEntityBeSeen(
				entityweepingangel) || LineOfSightCheck(entityToAttack));
	}
	public boolean GetFlag(float f, float f1, float f2, float f3, float f4) {
		if(f < f3)
		{
			if(f2 >= f + f4)
			{
				return true;
			}
			if(f2 <= f1)
			{
				return true;
			}
		}
		if(f1 >= f4)
		{
			if(f2 <= f1 - f4)
			{
				return true;
			}
			if(f2 >= f)
			{
				return true;
			}
		}
		if(f1 < f4 && f >= f3)
		{
			return f2 <= f1 && f2 > f;
		} else
		{
			return false;
		}
	}
	private boolean LineOfSightCheck(EntityPlayer entity1) {
		return (rayTraceBlocks(Vec3.createVectorHelper(posX, posY + (double)getEyeHeight(), posZ), Vec3.createVectorHelper(entity1.posX, entity1.posY + (double)entity1.getEyeHeight(), entity1.posZ)) == null)
				|| (rayTraceBlocks(Vec3.createVectorHelper(posX, posY + height, posZ), Vec3.createVectorHelper(entity1.posX, entity1.posY + (double)entity1.getEyeHeight(), entity1.posZ)) == null)
				|| (rayTraceBlocks(Vec3.createVectorHelper(posX, posY + (height * 0.1), posZ), Vec3.createVectorHelper(entity1.posX, entity1.posY + (double)entity1.getEyeHeight(), entity1.posZ)) == null)
				|| (rayTraceBlocks(Vec3.createVectorHelper(posX + 0.7, posY + (double)getEyeHeight(), posZ), Vec3.createVectorHelper(entity1.posX, entity1.posY + (double)entity1.getEyeHeight(), entity1.posZ)) == null)
				|| (rayTraceBlocks(Vec3.createVectorHelper(posX - 0.7, posY + (double)getEyeHeight(), posZ), Vec3.createVectorHelper(entity1.posX, entity1.posY + (double)entity1.getEyeHeight(), entity1.posZ)) == null)
				|| (rayTraceBlocks(Vec3.createVectorHelper(posX, posY + (double)getEyeHeight(), posZ + 0.7), Vec3.createVectorHelper(entity1.posX, entity1.posY + (double)entity1.getEyeHeight(), entity1.posZ)) == null)
				|| (rayTraceBlocks(Vec3.createVectorHelper(posX, posY + (double)getEyeHeight(), posZ - 0.7), Vec3.createVectorHelper(entity1.posX, entity1.posY + (double)entity1.getEyeHeight(), entity1.posZ)) == null)
				|| (rayTraceBlocks(Vec3.createVectorHelper(posX, posY + (height * 1.2), posZ - 0.7), Vec3.createVectorHelper(entity1.posX, entity1.posY + (double)entity1.getEyeHeight(), entity1.posZ)) == null)
				|| (rayTraceBlocks(Vec3.createVectorHelper(posX, posY + (height * 1.2) + 1, posZ), Vec3.createVectorHelper(entity1.posX, entity1.posY + (double)entity1.getEyeHeight(), entity1.posZ)) == null);
	}
	
	//~~~~~~~~~ RayTracing ~~~~~~~~~~~~~~~~~~~~~~~~~~~
	/**C+P from world class. Modified for transparent blocks*/
	private MovingObjectPosition rayTraceBlocks(Vec3 par1Vec3D, Vec3 par2Vec3D) {
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

			if ((!par4 || block1 == null || block1.getCollisionBoundingBoxFromPool(worldObj, l, i1, j1) != null) && j2 > 0 && block1.canCollideCheck(k2, par3) && !isBlockTransparent(j2))
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
	private boolean isBlockTransparent(int id) {
		for(int i = 0; i < transparentBlocks.length; i++) {
			if(id == transparentBlocks[i]) {
				return true;
			}
		}
		return false;
	}
	
	//~~~~~~~~~~~ Attacking ~~~~~~
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
	@Override
	protected void attackEntity(Entity entity, float f) {
		if(entityToAttack != null &&
				(entityToAttack instanceof EntityPlayer) &&
				!canAngelBeSeenMultiplayer()) {
			EntityPlayer entityPlayer = (EntityPlayer)entityToAttack;
			//Always attack, but teleport sometimes as specified in the config
			super.attackEntity(entity, f);
			if(rand.nextInt(100) < WeepingAngelsMod.teleportChance) {
				if(!entityPlayer.capabilities.isCreativeMode) {
					if(this.getDistancetoEntityToAttack() <= 2) {
						worldObj.playSoundEffect(
								entityToAttack.posX,
								entityToAttack.posY,
								entityToAttack.posZ,
								"mob.ghast.scream",
								getSoundVolume(),
								((rand.nextFloat() - rand.nextFloat()) *
										0.2F + 1.0F) * 1.8F);
						worldObj.playSoundAtEntity(
								entityToAttack,
								"weepingangels:teleport_activate",
								getSoundVolume(),
								((rand.nextFloat() - rand.nextFloat()) *
										0.2F + 1.0F) * 1.8F);
						for(int k = 0; k < 5; k++) {
							worldObj.spawnParticle(
									"portal",
									entityToAttack.posX +
											(rand.nextDouble() - 0.5D) *
											(double)width,
									(entityToAttack.posY + rand.nextDouble() *
											(double)height) - 0.25D,
									entityToAttack.posZ +
											(rand.nextDouble() - 0.5D) *
											(double)width,
									(rand.nextDouble() - 0.5D) * 2D,
									-rand.nextDouble(),
									(rand.nextDouble() - 0.5D) * 2D);
						}					
						
						this.teleportPlayer(entityToAttack);
						for(int k = 0; k < 5; k++) {
							worldObj.spawnParticle(
									"portal",
									entityToAttack.posX +
											(rand.nextDouble() - 0.5D) *
											(double)width,
									(entityToAttack.posY + rand.nextDouble() *
											(double)height) - 0.25D,
									entityToAttack.posZ +
											(rand.nextDouble() - 0.5D) *
											(double)width,
									(rand.nextDouble() - 0.5D) * 2D,
									-rand.nextDouble(),
									(rand.nextDouble() - 0.5D) * 2D);
						}
						worldObj.playSoundAtEntity(
								entityToAttack,
								"weepingangels:teleport_activate",
								getSoundVolume(),
								((rand.nextFloat() - rand.nextFloat()) *
										0.2F + 1.0F) * 1.8F);
						entityToAttack = null;
					}

				}
			}
		}
	}
	public double getDistancetoEntityToAttack() {
		if(entityToAttack instanceof EntityPlayer) {
			double d = entityToAttack.posX - posX;
			double d2 = entityToAttack.posY - posY;
			double d4 = entityToAttack.posZ - posZ;
			return (double)MathHelper.sqrt_double(d * d + d2 * d2 + d4 * d4);
		}
		EntityPlayer entityplayer = worldObj.getClosestPlayerToEntity(
				this, 64D);
		if(entityplayer != null) {
			double d1 = entityplayer.posX - posX;
			double d3 = entityplayer.posY - posY;
			double d5 = entityplayer.posZ - posZ;
			return (double)MathHelper.sqrt_double(
					d1 * d1 + d3 * d3 + d5 * d5);
		}else{
			return 40000D;
		}
	}
	private void teleportPlayer(Entity entity) {
		if(entity instanceof EntityPlayer) {
			int rangeDifference = 2 * (WeepingAngelsMod.teleportRangeMax -
					WeepingAngelsMod.teleportRangeMin);
			int offsetX = rand.nextInt(rangeDifference) -
					rangeDifference/2 + WeepingAngelsMod.teleportRangeMin;
			int offsetZ = rand.nextInt(rangeDifference) -
					rangeDifference/2 + WeepingAngelsMod.teleportRangeMin;
			
			//Center the values on a block, to make the boundingbox calculations match less.
			double newX =
					MathHelper.floor_double(entity.posX) + offsetX + 0.5;
			double newY = rand.nextInt(128);
			double newZ =
					MathHelper.floor_double(entity.posZ) + offsetZ + 0.5;
			
			double bbMinX = newX - entity.width / 2.0;
			double bbMinY = newY - entity.yOffset + entity.ySize;
			double bbMinZ = newZ - entity.width / 2.0;
			double bbMaxX = newX + entity.width / 2.0;
			double bbMaxY = newY - entity.yOffset +
					entity.ySize + entity.height;
			double bbMaxZ = newZ + entity.width / 2.0;
			
			//FMLLog.info("Teleporting from: "+(int)entity.posX+" "+(int)entity.posY+" "+(int)entity.posZ);
			//FMLLog.info("Teleporting with offsets: "+offsetX+" "+newY+" "+offsetZ);
			//FMLLog.info("Starting BB Bounds: "+bbMinX+" "+bbMinY+" "+bbMinZ+" "+bbMaxX+" "+bbMaxY+" "+bbMaxZ);
			
			//Use a testing boundingBox, so we don't have to move the player around to test if it is a valid location
			AxisAlignedBB boundingBox = AxisAlignedBB.getBoundingBox(
					bbMinX, bbMinY, bbMinZ, bbMaxX, bbMaxY, bbMaxZ);
			
			// Make sure you are trying to teleport to a loaded chunk.
			Chunk teleportChunk = worldObj.getChunkFromBlockCoords(
					(int)newX, (int)newZ);
			if(!teleportChunk.isChunkLoaded) {
				worldObj.getChunkProvider().loadChunk(
						teleportChunk.xPosition, teleportChunk.zPosition);
			}
			
			// Move up, until nothing intersects the entity anymore
			while (newY > 0 && newY < 128 &&
					!this.worldObj.getCollidingBoundingBoxes(
							entity, boundingBox).isEmpty()) {
				++newY;
				
				bbMinY = newY - entity.yOffset + entity.ySize;
				bbMaxY = newY - entity.yOffset +
						entity.ySize + entity.height;
				
				boundingBox.setBounds(
						bbMinX, bbMinY, bbMinZ, bbMaxX, bbMaxY, bbMaxZ);
				
				//FMLLog.info("Failed to teleport, retrying at height: "+(int)newY);
			}
			//If we could place it, could we have placed it lower? To prevent teleports really high up.
			do {
				--newY;
				
				bbMinY = newY - entity.yOffset + entity.ySize;
				bbMaxY = newY - entity.yOffset +
						entity.ySize + entity.height;
				
				boundingBox.setBounds(
						bbMinX, bbMinY, bbMinZ, bbMaxX, bbMaxY, bbMaxZ);
				
				//FMLLog.info("Trying a lower teleport at height: "+(int)newY);
			}
			while (newY > 0 && newY < 128 &&
					this.worldObj.getCollidingBoundingBoxes(
							entity, boundingBox).isEmpty());
			//Set Y one higher, as the last lower placing test failed.
			++newY;
					
			//Check for placement in lava
			//NOTE: This can potentially hang the game indefinitely, due to random recursion
			//However this situation is highly unlikelely
			//My advice: Dont encounter Weeping Angels in seas of lava
			//NOTE: This can theoretically still teleport you to a block of lava with air underneath, but gladly lava spreads ;)
			int blockId = worldObj.getBlockId(MathHelper.floor_double(newX), MathHelper.floor_double(newY), MathHelper.floor_double(newZ));
			if (blockId == 10 || blockId == 11) {
				this.teleportPlayer(entity);
				return;
			}
			
			//Set the location of the player, on the final position.
			entity.setLocationAndAngles(
					newX, newY, newZ, entity.rotationYaw,
					entity.rotationPitch);
			//FMLLog.info("Succesfully teleported to: "+(int)entity.posX+" "+(int)entity.posY+" "+(int)entity.posZ);
		}
	}
	
	
	//~~~~ On Update ~~~~~~
	@Override
	public void onUpdate() {
		//if(WeepingAngelsMod.DEBUG)System.out.println("EWP Location x: " + this.posX + " y: " + this.posY+ " z: " + this.posZ);
		if(this.spawntimer >= 0)
			--this.spawntimer;
		
		this.breakOnePerTick = false;
		this.moveSpeed = entityToAttack != null ?
				this.maxSpeed : this.minSpeed;
		
		this.isJumping = false;
		if(worldObj.isDaytime()) {
			float f = getBrightness(1.0F);
			if(f > 0.5F && worldObj.canBlockSeeTheSky(
					MathHelper.floor_double(posX),
					MathHelper.floor_double(posY),
					MathHelper.floor_double(posZ)) &&
					rand.nextFloat() * 30F < (f - 0.4F) * 2.0F) {
				this.canSeeSkyAndDay = true;
			}else{
				this.canSeeSkyAndDay = false;
			}
		}
		
		// Angry and Arms
		if(this.entityToAttack != null &&
				(this.entityToAttack instanceof EntityPlayer)) {
			if(WeepingAngelsMod.DEBUG)System.out.println("Checking Seen");
			if(this.canAngelBeSeenMultiplayer()) {
				if(WeepingAngelsMod.DEBUG)System.out.println("Not Seen");
				if((this.getDistancetoEntityToAttack() > 15D &&
						this.timeTillNextTeleport-- < 0)) {
					this.func_35182_c(entityToAttack);
					worldObj.playSoundAtEntity(this, getMovementSound(), getSoundVolume() * 1.1f, ((rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F) * 1.8F);
					this.timeTillNextTeleport = rand.nextInt(60) + 20;
				}
				if(WeepingAngelsMod.DEBUG)
					System.out.println("Time till port: " + timeTillNextTeleport);
				
				if(WeepingAngelsMod.DEBUG)
					System.out.println("Checking near players");
				//if((entityToAttack instanceof EntityPlayer) && getDistancetoEntityToAttack() <= this.distanceToSeen)
				if(getDistancetoEntityToAttack() <= this.distanceToSeen) {					
					//this.aggressiveArmMovement = true;
					this.dataWatcher.updateObject(16, Byte.valueOf((byte)1)); // sets angry
					if(WeepingAngelsMod.DEBUG)System.out.println("Angry"); // print angry
					//entityToAttack.applyEntityCollision(entityToAttack);

				}else{
					//this.aggressiveArmMovement = false;
					this.dataWatcher.updateObject(16, Byte.valueOf((byte)0)); // sets not angry
					if(WeepingAngelsMod.DEBUG)System.out.println("Not Angry"); // print not angry
					//entityToAttack.applyEntityCollision(entityToAttack);
				}
				/* Arm movemnent things
				if((entityToAttack instanceof EntityPlayer) &&
						getDistancetoEntityToAttack() >
								this.distanceToSeen &&
						rand.nextInt(100) > 80) {
					//armMovement = !armMovement;
					if(armMovement) {
						if(WeepingAngelsMod.DEBUG)System.out.println("Arm Movement: 1");
						//this.dataWatcher.updateObject(17, Byte.valueOf((byte)1));
					}else{
						if(WeepingAngelsMod.DEBUG)System.out.println("Arm Movement: 0");
						this.dataWatcher.updateObject(17, Byte.valueOf((byte)0));
					}
				}
				*/
			}
			if(worldObj.getFullBlockLightValue(
					MathHelper.floor_double(posX),
					MathHelper.floor_double(posY),
					MathHelper.floor_double(posZ)) < 1 &&
					worldObj.getFullBlockLightValue(
							MathHelper.floor_double(entityToAttack.posX),
							MathHelper.floor_double(entityToAttack.posY),
							MathHelper.floor_double(entityToAttack.posZ)) < 1 &&
							randomSoundDelay > 0 && --randomSoundDelay == 0) {
				worldObj.playSoundAtEntity(
						this, "mob.ghast.scream",
						getSoundVolume(),
						((rand.nextFloat() - rand.nextFloat()) *
								0.2F + 1.0F) * 1.8F);
			}
			
			if(this.slowPeriod > 0) {
				this.slowPeriod--;
				entityToAttack.motionX *= 0.01D;
				entityToAttack.motionZ *= 0.01D;          	
			}
			
			
			
			if((entityToAttack instanceof EntityPlayer) &&
					(canAngelBeSeenMultiplayer())) {
				this.angelDirectLook((EntityPlayer)entityToAttack);
				this.moveStrafing = this.moveForward = 0.0F;
				moveSpeed = 0.0F;
				this.torchTimer++;
				if(this.torchTimer >= this.torchNextBreak &&
						!this.canSeeSkyAndDay) {
					this.torchTimer = 0;
					this.torchNextBreak = rand.nextInt(1000) + 1000;
					this.findNearestTorch();
				}
			}else{
				this.faceEntity(entityToAttack, 100F, 100F);
			}
		}
		byte var1 = this.dataWatcher.getWatchableObjectByte(16);
		super.onUpdate();
	}
	private boolean angelDirectLook(EntityPlayer entityplayer) {
		if(worldObj.getFullBlockLightValue(
				MathHelper.floor_double(posX),
				MathHelper.floor_double(posY),
				MathHelper.floor_double(posZ)) < 1) {
			return false;
		}
		Vec3 vec3d = entityplayer.getLook(1.0F).normalize();
		Vec3 vec3d1 = Vec3.createVectorHelper(posX - entityplayer.posX, ((boundingBox.minY + (double)height) - entityplayer.posY) + (double)entityplayer.getEyeHeight(), posZ - entityplayer.posZ);
		double d = vec3d1.lengthVector();
		vec3d1 = vec3d1.normalize();
		double d1 = vec3d.dotProduct(vec3d1);
		if(d1 > 1.0D - 0.025000000000000001D / d) {
//			if(aggressiveArmMovement || armMovement)
//				slowPeriod = rand.nextInt(100);
			return entityplayer.canEntityBeSeen(this);
		}else{
			return false;
		}
	}
	private void findNearestTorch() {
		int i = (int)posX;
		int j = (int)posY;
		int k = (int)posZ;
		int l = i + 10;
		int i1 = j + 10;
		int j1 = k + 10;
		int k1 = i - 10;
		int l1 = j - 10;
		int i2 = k - 10;
		int j2 = 100;
		for(int k2 = k1; k2 < l; k2++) {
			for(int l2 = l1; l2 < i1; l2++) {
				for(int i3 = i2; i3 < j1; i3++) {
					if(this.getDistance(i, j, k, k2, l2, i3) > (double)j2) {
						continue;
					}
					int j3 = worldObj.getBlockId(k2, l2, i3);
					Block block = j3 > 0 ? Block.blocksList[j3] : null;
					if(block == null || block != Block.torchWood &&
							block != Block.torchRedstoneActive &&
							block != Block.redstoneLampActive &&
							block != Block.redstoneRepeaterActive &&
							block != Block.glowStone ||
							worldObj.clip(Vec3.createVectorHelper(
									posX,
									posY + (double)getEyeHeight(),
									posZ),
							Vec3.createVectorHelper(k2, l2, i3)) != null ||
							worldObj.clip(Vec3.createVectorHelper(
									entityToAttack.posX,
									entityToAttack.posY +
											(double)entityToAttack.getEyeHeight(),
									entityToAttack.posZ),
							Vec3.createVectorHelper(k2, l2, i3)) != null) {
						continue;
					}
					if(!this.breakOnePerTick) {
						block.dropBlockAsItem(worldObj, k2, l2, i3, 1, 1);
						this.worldObj.setBlockToAir(k2, l2, i3);
						this.worldObj.playSoundAtEntity(
								this,
								"weepingangels:light",
								getSoundVolume(),
								((rand.nextFloat() - rand.nextFloat()) *
										0.2F + 1.0F) * 1.8F);
						this.breakOnePerTick = true;
					}
					break;
				}

			}

		}

	}
	public double getDistance(int i, int j, int k, int l, int i1, int j1) {
		int k1 = l - i;
		int l1 = i1 - j;
		int i2 = j1 - k;
		return Math.sqrt(k1 * k1 + l1 * l1 + i2 * i2);
	}


	public boolean getAngry() {
		return this.dataWatcher.getWatchableObjectByte(16) == 1; 
	}
	public boolean getArmMovement() {
		return this.dataWatcher.getWatchableObjectByte(17) == 1; 
	}
	
	
	
	//~~~~~~~~~ Strange Functions ~~~~~~~~~~~~
	protected boolean func_35182_c(Entity entity)
	{
		Vec3 vec3d = Vec3.createVectorHelper(posX - entity.posX, ((boundingBox.minY + (double)(height / 2.0F)) - entity.posY) + (double)entity.getEyeHeight(), posZ - entity.posZ);
		vec3d = vec3d.normalize();
		double d = 6D;
		double d1 = (posX + (rand.nextDouble() - 0.5D) * 8D) - vec3d.xCoord * d;
		double d2 = (posY + (double)(rand.nextInt(16) - 8)) - vec3d.yCoord * d;
		double d3 = (posZ + (rand.nextDouble() - 0.5D) * 8D) - vec3d.zCoord * d;
		return func_35179_a_(d1, d2, d3);
	}
	protected boolean func_35179_a_(double d, double d1, double d2)
	{
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
		if(worldObj.blockExists(i, j, k))
		{
			boolean flag1;
			for(flag1 = false; !flag1 && j > 0;)
			{
				int i1 = worldObj.getBlockId(i, j - 1, k);
				if(i1 == 0 || !Block.blocksList[i1].blockMaterial.isSolid())
				{
					posY--;
					j--;
				} else
				{
					flag1 = true;
				}
			}

			if(flag1)
			{
				setPosition(posX, posY, posZ);
				if(worldObj.getCollidingBoundingBoxes(this, boundingBox).size() == 0 && !worldObj.isAnyLiquid(boundingBox))
				{
					flag = true;
				}
			}
		}
		if(!flag)
		{
			setPosition(d3, d4, d5);
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
	
	
}
