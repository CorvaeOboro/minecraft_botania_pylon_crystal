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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class EnchantedPylonBlockEntity extends BlockEntity {
	public EnchantedPylonBlockEntity(BlockPos pos, BlockState state) {
		super(BotaniaPylonCrystal.ENCHANTED_PYLON_BLOCK_ENTITY_TYPE, pos, state);
	}
}
