package com.countrygamer.weepingangels.World.Structure;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.util.Direction;
import net.minecraft.util.Facing;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;

import com.countrygamer.weepingangels.WeepingAngelsMod;
import com.countrygamer.weepingangels.Blocks.TileEnt.TileEntityPlinth;
import com.countrygamer.weepingangels.Entity.EntityStatue;
import com.countrygamer.weepingangels.lib.Util;

public class ComponentAngelDungeon extends ComponentVillage {
	
	private int					averageGroundLevel	= -1;
	private static final int	HEIGHT				= 50;
	
	public ComponentAngelDungeon(
			ComponentVillageStartPiece par1ComponentVillageStartPiece,
			int componentType, Random par3Random,
			StructureBoundingBox par4StructureBoundingBox, int par5) {
		super(par1ComponentVillageStartPiece, componentType);
		this.coordBaseMode = par5;
		this.boundingBox = par4StructureBoundingBox;
	}
	
	public static ComponentAngelDungeon buildComponent(
			ComponentVillageStartPiece startPiece, List par1List,
			Random random, int par3, int par4, int par5, int par6, int par7) {
		StructureBoundingBox var8 = StructureBoundingBox
				.getComponentToAddBoundingBox(par3, par4, par5, 0, 0, 0, 12,
						HEIGHT, 12, par6);
		return canVillageGoDeeper(var8)
				&& StructureComponent.findIntersecting(par1List, var8) == null ? new ComponentAngelDungeon(
						startPiece, par7, random, var8, par6) : null;
	}
	
	// this method create house
	public boolean addComponentParts(World world, Random rand,
			StructureBoundingBox boundBox) {
		if (this.averageGroundLevel < 0) {
			this.averageGroundLevel = this.getAverageGroundLevel(world,
					boundBox);
			if (this.averageGroundLevel < 0) {
				return true;
			}
			
			this.boundingBox.offset(0, this.averageGroundLevel
					- this.boundingBox.maxY + HEIGHT - 1, 0);
		}
		
		int xOffset = 5, yOffset = CG_Core.DEBUG ? 20 : 0, zOffset = 0;
		this.placeBlockAtCurrentPosition(world, Block.cobblestone.blockID, 0,
				-1 + xOffset, +0 + yOffset, +1 + zOffset, boundBox);
		/*
		// place generation house code here
		CoreUtil.fillBlocks(world, -1 + xOffset, -10 + yOffset, 0 + zOffset,
				1 + xOffset, 4 + yOffset, 2 + zOffset, 0, 0, this, boundBox);
		// place generation house code here
		CoreUtil.fillBlocks(world, -1 + xOffset, -10 + yOffset, 0 + zOffset,
				1 + xOffset, 4 + yOffset, 2 + zOffset, 0, 0, this, boundBox);
		CoreUtil.placeBlock(world, 0 + xOffset, 1 + yOffset, 1 + zOffset,
				WeepingAngelsMod.plinthBlock.blockID, 1, this, boundBox);
		// if (CG_Core.DEBUG)
		// CoreUtil.placeBlock(world, 0, 4, 0, Block.dirt.blockID, 0, this,
		// boundBox);
		// 0 == left, 1 == right, 2 == back, 3 == front
		CoreUtil.placeBlock(world, -1 + xOffset, +0 + yOffset, +1 + zOffset,
				Block.stairsCobblestone.blockID, 0, this, boundBox);
		CoreUtil.placeBlock(world, +1 + xOffset, +0 + yOffset, +1 + zOffset,
				Block.stairsCobblestone.blockID, 1, this, boundBox);
		CoreUtil.placeBlock(world, +0 + xOffset, +0 + yOffset, +2 + zOffset,
				Block.stairsCobblestone.blockID, 2, this, boundBox);
		CoreUtil.placeBlock(world, +0 + xOffset, +0 + yOffset, +0 + zOffset,
				Block.stairsCobblestone.blockID, 3, this, boundBox);
		
		CoreUtil.placeBlock(world, +1 + xOffset, +0 + yOffset, +0 + zOffset,
				Block.stoneSingleSlab.blockID, 5, this, boundBox);
		CoreUtil.placeBlock(world, -1 + xOffset, +0 + yOffset, +0 + zOffset,
				Block.stoneSingleSlab.blockID, 5, this, boundBox);
		CoreUtil.placeBlock(world, +1 + xOffset, +0 + yOffset, +2 + zOffset,
				Block.stoneSingleSlab.blockID, 5, this, boundBox);
		CoreUtil.placeBlock(world, -1 + xOffset, +0 + yOffset, +2 + zOffset,
				Block.stoneSingleSlab.blockID, 5, this, boundBox);
		
		CoreUtil.fillVariedStoneBlocks(world, -1 + xOffset, -6 + yOffset,
				0 + zOffset, 1 + xOffset, -1 + yOffset, 2 + zOffset, this,
				boundBox);
		CoreUtil.fillVariedStoneBlocks(world, -1 + xOffset, -10 + yOffset,
				0 + zOffset, 1 + xOffset, -7 + yOffset, 4 + zOffset, this,
				boundBox);
		CoreUtil.fillVariedStoneBlocks(world, -5 + xOffset, -10 + yOffset,
				4 + zOffset, 5 + xOffset, -6 + yOffset, 14 + zOffset, this,
				boundBox);
		
		CoreUtil.fillBlocks(world, 0 + xOffset, -7 + yOffset, 1 + zOffset,
				0 + xOffset, -1 + yOffset, 1 + zOffset, 0, 0, this, boundBox);
		CoreUtil.fillBlocks(world, 0 + xOffset, -9 + yOffset, 1 + zOffset,
				0 + xOffset, -8 + yOffset, 5 + zOffset, 0, 0, this, boundBox);
		CoreUtil.fillBlocks(world, -4 + xOffset, -9 + yOffset, 5 + zOffset,
				4 + xOffset, -7 + yOffset, 13 + zOffset, 0, 0, this, boundBox);
		
		int corX = 0 + xOffset, corY = -9 + yOffset, corZ = 9 + zOffset;
		CoreUtil.placeBlock(world, corX - 3, corY - 1, corZ - 3,
				Block.glowStone.blockID, 0, this, boundBox);
		CoreUtil.placeBlock(world, corX - 3, corY - 1, corZ + 3,
				Block.glowStone.blockID, 0, this, boundBox);
		CoreUtil.placeBlock(world, corX + 3, corY - 1, corZ - 3,
				Block.glowStone.blockID, 0, this, boundBox);
		CoreUtil.placeBlock(world, corX + 3, corY - 1, corZ + 3,
				Block.glowStone.blockID, 0, this, boundBox);
		
		CoreUtil.placeBlock(world, corX - 4, corY, corZ + 1,
				WeepingAngelsMod.plinthBlock.blockID, 1, this, boundBox);
		CoreUtil.placeBlock(world, corX - 4, corY, corZ - 1,
				WeepingAngelsMod.plinthBlock.blockID, 1, this, boundBox);
		CoreUtil.placeBlock(world, corX + 4, corY, corZ + 1,
				WeepingAngelsMod.plinthBlock.blockID, 1, this, boundBox);
		CoreUtil.placeBlock(world, corX + 4, corY, corZ - 1,
				WeepingAngelsMod.plinthBlock.blockID, 1, this, boundBox);
		
		CoreUtil.placeBlock(world, corX - 1, corY, corZ + 4,
				WeepingAngelsMod.plinthBlock.blockID, 1, this, boundBox);
		CoreUtil.placeBlock(world, corX - 1, corY, corZ - 4,
				WeepingAngelsMod.plinthBlock.blockID, 1, this, boundBox);
		CoreUtil.placeBlock(world, corX + 1, corY, corZ + 4,
				WeepingAngelsMod.plinthBlock.blockID, 1, this, boundBox);
		CoreUtil.placeBlock(world, corX + 1, corY, corZ - 4,
				WeepingAngelsMod.plinthBlock.blockID, 1, this, boundBox);
		
		CoreUtil.placeBlock(world, corX - 3, corY, corZ + 3,
				WeepingAngelsMod.plinthBlock.blockID, 1, this, boundBox);
		CoreUtil.placeBlock(world, corX - 3, corY, corZ - 3,
				WeepingAngelsMod.plinthBlock.blockID, 1, this, boundBox);
		CoreUtil.placeBlock(world, corX + 3, corY, corZ + 3,
				WeepingAngelsMod.plinthBlock.blockID, 1, this, boundBox);
		CoreUtil.placeBlock(world, corX + 3, corY, corZ - 3,
				WeepingAngelsMod.plinthBlock.blockID, 1, this, boundBox);
		
		CoreUtil.placeBlock(world, corX, corY + 0, corZ - 5,
				Block.fenceIron.blockID, 0, this, boundBox);
		CoreUtil.placeBlock(world, corX, corY + 1, corZ - 5,
				Block.fenceIron.blockID, 0, this, boundBox);
		
		for (int l = -1; l > -9; l--) {
			CoreUtil.placeBlock(world, 0 + xOffset, l + yOffset, 1 + zOffset,
					Block.ladder.blockID, 2, this, boundBox);
		}
		CoreUtil.placeBlock(world, 0 + xOffset, 0 + yOffset, 1 + zOffset,
				Block.trapdoor.blockID, (5 + 4) | 8, this, boundBox);
		 */
		return true;
	}
	
	private int getMeta() {
		int meta = 0;
		int chance = (new Random()).nextInt(100);
		if (chance <= 45) {
			meta = 1;
			if (chance <= 10)
				meta = 2;
		}
		return meta;
	}
	
	private void stoneBlock(World world, int x, int y, int z,
			StructureBoundingBox boundBox) {
		int meta = this.getMeta();
		this.placeBlockAtCurrentPosition(world, Block.stoneBrick.blockID, meta,
				x, y, z, boundBox);
	}
	
	private void stoneStair(World world, int x, int y, int z, int dir,
			StructureBoundingBox boundBox) {
		this.placeBlockAtCurrentPosition(
				world,
				Block.stairsStoneBrick.blockID,
				this.getMetadataWithOffset(Block.stairsCobblestone.blockID, dir),
				x, y, z, boundBox);
	}
	
	private void fillWithStone(World world, StructureBoundingBox boundBox,
			int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		for (int k2 = minY; k2 <= maxY; ++k2) {
			for (int l2 = minX; l2 <= maxX; ++l2) {
				for (int i3 = minZ; i3 <= maxZ; ++i3) {
					if (!false) {
						int meta = this.getMeta();
						if (k2 != minY && k2 != maxY && l2 != minX
								&& l2 != maxX && i3 != minZ && i3 != maxZ) {
							this.placeBlockAtCurrentPosition(world,
									Block.stoneBrick.blockID, meta, l2, k2, i3,
									boundBox);
						} else {
							this.placeBlockAtCurrentPosition(world,
									Block.stoneBrick.blockID, meta, l2, k2, i3,
									boundBox);
						}
					}
				}
			}
		}
		
	}
	
	private void statue(World world, StructureBoundingBox boundBox, int x,
			int y, int z, int rotation) {
		this.placeBlockAtCurrentPosition(world,
				WeepingAngelsMod.plinthBlock.blockID, 1, x, y, z, boundBox);
		/**
		 * 0 through 14 evens only
		 */
		int statueYaw = 6;
		
		TileEntityPlinth statue = new TileEntityPlinth();
		EntityStatue ent = Util.getEntityStatue(world, EntityStatue.class);
		if (ent != null) {
			// statue.setRotation(statueYaw);
			// statue.statueEntity = ent;
		}
		// world.setBlockTileEntity(x, y, z, statue);
		
	}
	
	public void placeBlockAtCurrentPosition(World par1World, int par2,
			int par3, int par4, int par5, int par6,
			StructureBoundingBox par7StructureBoundingBox) {
		int j1 = this.getBiomeSpecificBlock(par2, par3);
		int k1 = this.getBiomeSpecificBlockMetadata(par2, par3);
		super.placeBlockAtCurrentPosition(par1World, j1, k1, par4, par5, par6,
				par7StructureBoundingBox);
	}
	
	public void fillWithAir(World par1World,
			StructureBoundingBox par2StructureBoundingBox, int par3, int par4,
			int par5, int par6, int par7, int par8) {
		for (int k1 = par4; k1 <= par7; ++k1) {
			for (int l1 = par3; l1 <= par6; ++l1) {
				for (int i2 = par5; i2 <= par8; ++i2) {
					this.placeBlockAtCurrentPosition(par1World, 0, 0, l1, k1,
							i2, par2StructureBoundingBox);
				}
			}
		}
	}
	
	public int getMetadataWithOffset(int par1, int par2) {
		if (par1 == Block.rail.blockID) {
			if (this.coordBaseMode == 1 || this.coordBaseMode == 3) {
				if (par2 == 1) {
					return 0;
				}
				
				return 1;
			}
		} else
			if (par1 != Block.doorWood.blockID
			&& par1 != Block.doorIron.blockID) {
				if (par1 != Block.stairsCobblestone.blockID
						&& par1 != Block.stairsWoodOak.blockID
						&& par1 != Block.stairsNetherBrick.blockID
						&& par1 != Block.stairsStoneBrick.blockID
						&& par1 != Block.stairsSandStone.blockID) {
					if (par1 == Block.ladder.blockID) {
						if (this.coordBaseMode == 0) {
							if (par2 == 2) {
								return 3;
							}
							
							if (par2 == 3) {
								return 2;
							}
						} else
							if (this.coordBaseMode == 1) {
								if (par2 == 2) {
									return 4;
								}
								
								if (par2 == 3) {
									return 5;
								}
								
								if (par2 == 4) {
									return 2;
								}
								
								if (par2 == 5) {
									return 3;
								}
							} else
								if (this.coordBaseMode == 3) {
									if (par2 == 2) {
										return 5;
									}
									
									if (par2 == 3) {
										return 4;
									}
									
									if (par2 == 4) {
										return 2;
									}
									
									if (par2 == 5) {
										return 3;
									}
								}
					} else
						if (par1 == Block.stoneButton.blockID) {
							if (this.coordBaseMode == 0) {
								if (par2 == 3) {
									return 4;
								}
								
								if (par2 == 4) {
									return 3;
								}
							} else
								if (this.coordBaseMode == 1) {
									if (par2 == 3) {
										return 1;
									}
									
									if (par2 == 4) {
										return 2;
									}
									
									if (par2 == 2) {
										return 3;
									}
									
									if (par2 == 1) {
										return 4;
									}
								} else
									if (this.coordBaseMode == 3) {
										if (par2 == 3) {
											return 2;
										}
										
										if (par2 == 4) {
											return 1;
										}
										
										if (par2 == 2) {
											return 3;
										}
										
										if (par2 == 1) {
											return 4;
										}
									}
						} else
							if (par1 != Block.tripWireSource.blockID
							&& (Block.blocksList[par1] == null || !(Block.blocksList[par1] instanceof BlockDirectional))) {
								if (par1 == Block.pistonBase.blockID
										|| par1 == Block.pistonStickyBase.blockID
										|| par1 == Block.lever.blockID
										|| par1 == Block.dispenser.blockID) {
									if (this.coordBaseMode == 0) {
										if (par2 == 2 || par2 == 3) {
											return Facing.oppositeSide[par2];
										}
									} else
										if (this.coordBaseMode == 1) {
											if (par2 == 2) {
												return 4;
											}
											
											if (par2 == 3) {
												return 5;
											}
											
											if (par2 == 4) {
												return 2;
											}
											
											if (par2 == 5) {
												return 3;
											}
										} else
											if (this.coordBaseMode == 3) {
												if (par2 == 2) {
													return 5;
												}
												
												if (par2 == 3) {
													return 4;
												}
												
												if (par2 == 4) {
													return 2;
												}
												
												if (par2 == 5) {
													return 3;
												}
											}
								}
							} else
								if (this.coordBaseMode == 0) {
									if (par2 == 0 || par2 == 2) {
										return Direction.rotateOpposite[par2];
									}
								} else
									if (this.coordBaseMode == 1) {
										if (par2 == 2) {
											return 1;
										}
										
										if (par2 == 0) {
											return 3;
										}
										
										if (par2 == 1) {
											return 2;
										}
										
										if (par2 == 3) {
											return 0;
										}
									} else
										if (this.coordBaseMode == 3) {
											if (par2 == 2) {
												return 3;
											}
											
											if (par2 == 0) {
												return 1;
											}
											
											if (par2 == 1) {
												return 2;
											}
											
											if (par2 == 3) {
												return 0;
											}
										}
				} else
					if (this.coordBaseMode == 0) {
						if (par2 == 2) {
							return 3;
						}
						
						if (par2 == 3) {
							return 2;
						}
					} else
						if (this.coordBaseMode == 1) {
							if (par2 == 0) {
								return 2;
							}
							
							if (par2 == 1) {
								return 3;
							}
							
							if (par2 == 2) {
								return 0;
							}
							
							if (par2 == 3) {
								return 1;
							}
						} else
							if (this.coordBaseMode == 3) {
								if (par2 == 0) {
									return 2;
								}
								
								if (par2 == 1) {
									return 3;
								}
								
								if (par2 == 2) {
									return 1;
								}
								
								if (par2 == 3) {
									return 0;
								}
							}
			} else
				if (this.coordBaseMode == 0) {
					if (par2 == 0) {
						return 2;
					}
					
					if (par2 == 2) {
						return 0;
					}
				} else {
					if (this.coordBaseMode == 1) {
						return par2 + 1 & 3;
					}
					
					if (this.coordBaseMode == 3) {
						return par2 + 3 & 3;
					}
				}
		
		return par2;
	}
	
}