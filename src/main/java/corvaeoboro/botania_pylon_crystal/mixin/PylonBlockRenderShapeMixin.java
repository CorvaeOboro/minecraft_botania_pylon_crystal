/*
 * # Botania Pylon Crystal 
 * - addon replaces Botania's Pylon visuals with crystal json models.
 * - Overrides via a built-in resourcepack driven by editable model JSON and textures, and renders a client-side rotation.
 * 
 * # PylonBlockRenderShapeMixin.java
 * - Routes Botania pylon blocks (mana/natura/gaia) into the block-entity render path when the addon's per-pylon override is enabled.
 * - Forces `RenderShape.ENTITYBLOCK_ANIMATED` so the vanilla/static MODEL render path does not run.
 *
 * # NOTES:
 * - Uses `BotaniaPylonCrystalConfig.override*` toggles to decide when to override.
 * - Works in tandem with `PylonBlockEntityRendererMixin`, which performs the actual baked-model rendering (and optional rotation).
 */

package corvaeoboro.botania_pylon_crystal.mixin;

import corvaeoboro.botania_pylon_crystal.BotaniaPylonCrystalConfig;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class PylonBlockRenderShapeMixin {
	private static final boolean DEBUG_MODE = false;
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final ResourceLocation BOTANIA_MANA_PYLON = new ResourceLocation("botania", "mana_pylon");
	private static final ResourceLocation BOTANIA_NATURA_PYLON = new ResourceLocation("botania", "natura_pylon");
	private static final ResourceLocation BOTANIA_GAIA_PYLON = new ResourceLocation("botania", "gaia_pylon");
	private static boolean LOGGED_ONCE = false;

	@Inject(method = "getRenderShape", at = @At("HEAD"), cancellable = true)
	private void botania_pylon_crystal$forceModelRender(CallbackInfoReturnable<RenderShape> cir) {
		BlockState state = (BlockState) (Object) this;
		var id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
		boolean isMana = BOTANIA_MANA_PYLON.equals(id);
		boolean isNatura = BOTANIA_NATURA_PYLON.equals(id);
		boolean isGaia = BOTANIA_GAIA_PYLON.equals(id);
		if (isMana || isNatura || isGaia) {
			boolean overrideEnabled = (isMana && Boolean.TRUE.equals(BotaniaPylonCrystalConfig.get().overrideManaPylon))
					|| (isNatura && Boolean.TRUE.equals(BotaniaPylonCrystalConfig.get().overrideNaturaPylon))
					|| (isGaia && Boolean.TRUE.equals(BotaniaPylonCrystalConfig.get().overrideGaiaPylon));
			if (!overrideEnabled) {
				return;
			}

			if (!LOGGED_ONCE) {
				LOGGED_ONCE = true;
				if (DEBUG_MODE) {
					LOGGER.info("PylonBlockRenderShapeMixin active for {} (enableRotation={})", id, BotaniaPylonCrystalConfig.get().enableRotation);
				}
			}
			cir.setReturnValue(RenderShape.ENTITYBLOCK_ANIMATED);
		}
	}
}
