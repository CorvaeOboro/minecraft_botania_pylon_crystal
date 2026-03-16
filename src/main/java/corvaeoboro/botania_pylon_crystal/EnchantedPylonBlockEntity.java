/*
 * # Botania Pylon Crystal 
 * - addon replaces Botania's Pylon visuals with crystal json models.
 * - Overrides via a built-in resourcepack driven by config selected model JSON, dyable color, and client-side rotation.
 * 
 * # EnchantedPylonBlockEntity.java
 * - Minimal block entity used to attach the custom Enchanted Pylon renderer when rotation is enabled.
 */

package corvaeoboro.botania_pylon_crystal;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;


public class EnchantedPylonBlockEntity extends BlockEntity implements ManaPylonDyedAccess {
	private int dyeColorId = -1;

	public EnchantedPylonBlockEntity(BlockPos pos, BlockState state) {
		super(BotaniaPylonCrystal.ENCHANTED_PYLON_BLOCK_ENTITY_TYPE, pos, state);
	}

	@Override
	public int botania_pylon_crystal$getDyeColorId() {
		return dyeColorId;
	}

	@Override
	public void botania_pylon_crystal$setDyeColorId(int id) {
		this.dyeColorId = id;
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		if (tag.contains(ManaPylonDyeingRecipe.DYE_COLOR_NBT_KEY)) {
			this.dyeColorId = tag.getInt(ManaPylonDyeingRecipe.DYE_COLOR_NBT_KEY);
		} else {
			this.dyeColorId = -1;
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		if (this.dyeColorId >= 0) {
			tag.putInt(ManaPylonDyeingRecipe.DYE_COLOR_NBT_KEY, this.dyeColorId);
		}
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = super.getUpdateTag();
		if (this.dyeColorId >= 0) {
			tag.putInt(ManaPylonDyeingRecipe.DYE_COLOR_NBT_KEY, this.dyeColorId);
		}
		return tag;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
}
