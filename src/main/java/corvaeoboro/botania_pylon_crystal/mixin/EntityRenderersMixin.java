/*
 * # Botania Pylon Crystal 
 * - addon replaces Botania's Pylon visuals with crystal json models.
 * - Overrides via a built-in resourcepack driven by config selected model JSON, dyable color, and client-side rotation.
 * 
 * # EntityRenderersMixin.java
 * - Optional compatibility mixin for Botania's client renderer registry (`vazkii.botania.client.render.entity.EntityRenderers`).
 * - Can remove the block-entity item renderer factory (TEISR-style) for `botania:mana_pylon` to avoid conflicts with
 *   custom pylon item rendering.
 *
 * Notes:
 * - This mixin is a no-op by default; it only runs when the system property
 *   `botania_pylon_crystal.disable_teisr_mixin` is set to true.
 */

package corvaeoboro.botania_pylon_crystal.mixin;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Pseudo
@Mixin(targets = "vazkii.botania.client.render.entity.EntityRenderers", remap = false)
public class EntityRenderersMixin {
	private static final boolean DEBUG_MODE = false;
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final ResourceLocation BOTANIA_MANA_PYLON = new ResourceLocation("botania", "mana_pylon");
	private static boolean LOGGED_ONCE = false;

	@Shadow
	@Final
	@Mutable
	public static Map BE_ITEM_RENDERER_FACTORIES;

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void botania_pylon_crystal$disableManaPylonTeisr(CallbackInfo ci) {
		if (Boolean.getBoolean("botania_pylon_crystal.disable_teisr_mixin")) {
			// Allows temporarily restoring the old behavior for debugging.
		} else {
			return;
		}
		var map = new HashMap<>(BE_ITEM_RENDERER_FACTORIES);
		Object toRemove = null;
		for (Object k : map.keySet()) {
			if (k instanceof net.minecraft.world.level.block.Block b) {
				var id = BuiltInRegistries.BLOCK.getKey(b);
				if (BOTANIA_MANA_PYLON.equals(id)) {
					toRemove = k;
					break;
				}
			}
		}
		if (toRemove != null) {
			map.remove(toRemove);
			BE_ITEM_RENDERER_FACTORIES = Map.copyOf(map);
			if (!LOGGED_ONCE) {
				LOGGED_ONCE = true;
				if (DEBUG_MODE) {
					LOGGER.info("EntityRenderersMixin successfully removed BLOCK ENTITTY item renderer factory for {}", BOTANIA_MANA_PYLON);
				}
			}
		} else {
			if (!LOGGED_ONCE) {
				LOGGED_ONCE = true;
				LOGGER.warn("EntityRenderersMixin did not find BE item renderer factory for {}", BOTANIA_MANA_PYLON);
			}
		}
	}
}
