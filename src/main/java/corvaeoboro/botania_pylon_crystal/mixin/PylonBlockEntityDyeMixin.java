/*
 * # Botania Pylon Crystal 
 * - addon replaces Botania's Pylon visuals with crystal json models.
 * - Overrides via a built-in resourcepack driven by config selected model JSON, dyable color, and client-side rotation.
 * 
 * # PylonBlockEntityDyeMixin.java
 * - Adds persistent dye color storage to the Mana Pylon BlockEntity.
 * - Implements `ManaPylonDyedAccess` and serializes the dye color id to/from NBT.
 * - Ensures dye color is included in client sync via update tag/packet.
 */

package corvaeoboro.botania_pylon_crystal.mixin;

import corvaeoboro.botania_pylon_crystal.ManaPylonDyedAccess;
import corvaeoboro.botania_pylon_crystal.ManaPylonDyeingRecipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(BlockEntity.class)
public class PylonBlockEntityDyeMixin implements ManaPylonDyedAccess {
	@Unique
	private int botania_pylon_crystal$dyeColorId = -1;
	@Unique
	private static final ResourceLocation BOTANIA_MANA_PYLON = new ResourceLocation("botania", "mana_pylon");

	@Unique
	private boolean botania_pylon_crystal$isManaPylon() {
		BlockEntity self = (BlockEntity) (Object) this;
		var id = BuiltInRegistries.BLOCK.getKey(self.getBlockState().getBlock());
		return BOTANIA_MANA_PYLON.equals(id);
	}

	@Override
	public int botania_pylon_crystal$getDyeColorId() {
		return botania_pylon_crystal$dyeColorId;
	}

	@Override
	public void botania_pylon_crystal$setDyeColorId(int id) {
		this.botania_pylon_crystal$dyeColorId = id;
	}

	@Inject(method = "load(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
	private void botania_pylon_crystal$loadDyeColor(CompoundTag tag, CallbackInfo ci) {
		if (!botania_pylon_crystal$isManaPylon()) {
			return;
		}
		if (tag.contains(ManaPylonDyeingRecipe.DYE_COLOR_NBT_KEY)) {
			this.botania_pylon_crystal$dyeColorId = tag.getInt(ManaPylonDyeingRecipe.DYE_COLOR_NBT_KEY);
		} else {
			this.botania_pylon_crystal$dyeColorId = -1;
		}
	}

	@Inject(method = "saveAdditional(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
	private void botania_pylon_crystal$saveDyeColor(CompoundTag tag, CallbackInfo ci) {
		if (!botania_pylon_crystal$isManaPylon()) {
			return;
		}
		if (this.botania_pylon_crystal$dyeColorId >= 0) {
			tag.putInt(ManaPylonDyeingRecipe.DYE_COLOR_NBT_KEY, this.botania_pylon_crystal$dyeColorId);
		}
	}

	@Inject(method = "getUpdateTag()Lnet/minecraft/nbt/CompoundTag;", at = @At("RETURN"), cancellable = true)
	private void botania_pylon_crystal$writeUpdateTag(CallbackInfoReturnable<CompoundTag> cir) {
		if (!botania_pylon_crystal$isManaPylon()) {
			return;
		}
		CompoundTag tag = cir.getReturnValue();
		if (tag != null && this.botania_pylon_crystal$dyeColorId >= 0) {
			tag.putInt(ManaPylonDyeingRecipe.DYE_COLOR_NBT_KEY, this.botania_pylon_crystal$dyeColorId);
		}
	}

	@Inject(method = "getUpdatePacket()Lnet/minecraft/network/protocol/Packet;", at = @At("HEAD"), cancellable = true)
	private void botania_pylon_crystal$forceUpdatePacket(CallbackInfoReturnable<Packet<ClientGamePacketListener>> cir) {
		if (!botania_pylon_crystal$isManaPylon()) {
			return;
		}
		BlockEntity self = (BlockEntity) (Object) this;
		cir.setReturnValue(ClientboundBlockEntityDataPacket.create(self));
	}
}
