package com.temportalist.weepingangels.common.init

import com.temportalist.origin.library.common.register.EntityRegister
import com.temportalist.weepingangels.common.entity.{EntityAngel, EntityAngelArrow, EntityWeepingAngel}
import com.temportalist.weepingangels.common.{WAOptions, WeepingAngels}
import net.minecraft.entity.EnumCreatureType
import net.minecraft.world.biome.BiomeGenBase
import net.minecraftforge.fml.common.registry.EntityRegistry

/**
 *
 *
 * @author TheTemportalist
 */
object WAEntity extends EntityRegister {

	override def register(): Unit = {

		//EntityRegistry.registerGlobalEntityID(classOf[EntityWeepingAngel], "WeepingAngel",
		//EntityRegistry.findGlobalUniqueEntityId(), 0x808080, 0xD1D1D1)

		this.addEntity(classOf[EntityAngel], "weepingangel", WeepingAngels)
		this.addEgg(classOf[EntityAngel], 0x808080, 0xD1D1D1)

		/*
		this.addEntity(classOf[EntityWeepingAngel], "weepingangel", WeepingAngels)
		//this.addEgg(classOf[EntityWeepingAngel], 0x808080, 0xD1D1D1)

		var angelID: Int = -1
		val iter: util.Iterator[_] = EntityList.idToClassMapping.keySet().iterator()
		while (iter.hasNext) {
			val id: Int = iter.next().asInstanceOf[Int]
			if (EntityList.idToClassMapping.get(id) == classOf[EntityWeepingAngel])
				angelID = id
		}
		EntityList.entityEggs.asInstanceOf[util.LinkedHashMap[Int, EntityEggInfo]].put(
			angelID, new EntityEggInfo(angelID, 0x808080, 0xD1D1D1)
		)
		*/

		this.addEntity(classOf[EntityAngelArrow], "AngelArrow", WeepingAngels,
			100, 10, sendsVelocityUpdates = true)

	}

	override def addEntitySpawns: Unit = {
		for (index <- 0 until BiomeGenBase.getBiomeGenArray.length) {
			val biome: BiomeGenBase = BiomeGenBase.getBiomeGenArray()(index)

			if (biome != null && !(biome == BiomeGenBase.mushroomIsland ||
					biome == BiomeGenBase.mushroomIslandShore)) {
				EntityRegistry.addSpawn(
					classOf[EntityWeepingAngel], WAOptions.spawnProbability, 1, 1,
					EnumCreatureType.MONSTER, biome
				)

			}

		}

	}

}
