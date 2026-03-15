/*
 * # Botania Pylon Crystal 
 * - addon replaces Botania's Pylon visuals with crystal json models.
 * - Overrides via a built-in resourcepack driven by config selected model JSON, dyable color, and client-side rotation.
 * 
 * # EnchantedPylonBlock.java
 * - Standalone pylon-like block (no Botania dependency).
 * - Provides enchantment power like a bookshelf via the vanilla `enchantment_power_provider` block tag.
 * - Switches render shape based on config:
 *   - rotation enabled: `ENTITYBLOCK_ANIMATED` so the block entity renderer draws the rotating crystal/ring.
 *   - rotation disabled: `MODEL` so the static combined model JSON is used.
 */

package corvaeoboro.botania_pylon_crystal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

public class EnchantedPylonBlock extends BaseEntityBlock {
	public EnchantedPylonBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		if (BotaniaPylonCrystalConfig.get().enchantedPylonEnableRotation) {
			return RenderShape.ENTITYBLOCK_ANIMATED;
		}
		return RenderShape.MODEL;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new EnchantedPylonBlockEntity(pos, state);
	}

	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level level, BlockState state,
			BlockEntityType<T> type) {
		return null;
	}
}
