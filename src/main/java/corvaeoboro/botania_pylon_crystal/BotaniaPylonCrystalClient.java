/*
 * # Botania Pylon Crystal 
 * - addon replaces Botania's Pylon visuals with crystal json models.
 * - Overrides via a built-in resourcepack driven by config selected model JSON, dyable color, and client-side rotation.
 * 
 * # BotaniaPylonCrystalClient.java
 * - Client mod entrypoint.
 * - Registers the built-in resource pack `botania_pylon_override` as ALWAYS_ENABLED so Botania models are redirected to this addon's models.
 * - Registers model IDs for baking (ring + crystal variants for mana/natura/gaia, plus the Enchanted Pylon static model).
 * - Applies configurable render layer mapping for Botania pylons and the standalone Enchanted Pylon.
 * - Registers block/item color providers for dye tinting.
 * - Exposes `getConfiguredPylonRenderType()` for render mixins.
 * - Registers the Enchanted Pylon block entity renderer and builtin item renderer.
 *
 * # NOTES:
 * - Reads settings from `BotaniaPylonCrystalConfig` (written as JSON5-style `config/botania_pylon_crystal.json5`).
 * - Placed pylon rendering override is handled by `mixin/PylonBlockEntityRendererMixin`.
 * - Item rendering override is handled by `mixin/PylonBlockEntityItemRendererMixin`.
 * - Render shape routing is handled by `mixin/PylonBlockRenderShapeMixin`.
 */

package corvaeoboro.botania_pylon_crystal;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;

import com.mojang.logging.LogUtils;

import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import org.slf4j.Logger;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class BotaniaPylonCrystalClient implements ClientModInitializer {
	public static final boolean DEBUG_MODE = false;
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final ResourceLocation BOTANIA_MANA_PYLON = new ResourceLocation("botania", "mana_pylon");
	private static final ResourceLocation BOTANIA_NATURA_PYLON = new ResourceLocation("botania", "natura_pylon");
	private static final ResourceLocation BOTANIA_GAIA_PYLON = new ResourceLocation("botania", "gaia_pylon");
	private static final ResourceLocation BOTANIA_MANA_PYLON_BLOCK_MODEL = new ResourceLocation("botania", "models/block/mana_pylon.json");
	private static final ResourceLocation BOTANIA_MANA_PYLON_ITEM_MODEL = new ResourceLocation("botania", "models/item/mana_pylon.json");
	
	private static final ResourceLocation CUSTOM_PARENT_BLOCK_MODEL = new ResourceLocation("botania_pylon_crystal", "models/block/mana_pylon_combined.json");
	private static final ResourceLocation ENCHANTED_PYLON_STATIC_MODEL = new ResourceLocation("botania_pylon_crystal", "block/enchanted_pylon");
	private static final ResourceLocation PYLON_COMBINED_MANA_MODEL = new ResourceLocation("botania_pylon_crystal", "block/mana_pylon_combined");
	private static final ResourceLocation PYLON_COMBINED_NATURA_MODEL = new ResourceLocation("botania_pylon_crystal", "block/natura_pylon_combined");
	private static final ResourceLocation PYLON_COMBINED_GAIA_MODEL = new ResourceLocation("botania_pylon_crystal", "block/gaia_pylon_combined");
	private static final ResourceLocation PYLON_RING_MANA_MODEL = new ResourceLocation("botania_pylon_crystal", "block/pylon_ring_mana");
	private static final ResourceLocation PYLON_RING_NATURA_MODEL = new ResourceLocation("botania_pylon_crystal", "block/pylon_ring_natura");
	private static final ResourceLocation PYLON_RING_GAIA_MODEL = new ResourceLocation("botania_pylon_crystal", "block/pylon_ring_gaia");
	private static final ResourceLocation PYLON_CRYSTAL_TALL_MODEL = new ResourceLocation("botania_pylon_crystal", "block/pylon_crystal_bipyramid_tall");
	private static final ResourceLocation PYLON_CRYSTAL_SHORT_MODEL = new ResourceLocation("botania_pylon_crystal", "block/pylon_crystal_bipyramid_short");
	private static final ResourceLocation PYLON_CRYSTAL_TALLSHORT_MODEL = new ResourceLocation("botania_pylon_crystal", "block/pylon_crystal_bipyramid_tallshort");
	private static final ResourceLocation PYLON_CRYSTAL_SHORTTALL_MODEL = new ResourceLocation("botania_pylon_crystal", "block/pylon_crystal_bipyramid_shorttall");
	private static final ResourceLocation PYLON_CRYSTAL_TALL_NATURA_MODEL = new ResourceLocation("botania_pylon_crystal", "block/pylon_crystal_bipyramid_tall_natura");
	private static final ResourceLocation PYLON_CRYSTAL_SHORT_NATURA_MODEL = new ResourceLocation("botania_pylon_crystal", "block/pylon_crystal_bipyramid_short_natura");
	private static final ResourceLocation PYLON_CRYSTAL_TALLSHORT_NATURA_MODEL = new ResourceLocation("botania_pylon_crystal", "block/pylon_crystal_bipyramid_tallshort_natura");
	private static final ResourceLocation PYLON_CRYSTAL_SHORTTALL_NATURA_MODEL = new ResourceLocation("botania_pylon_crystal", "block/pylon_crystal_bipyramid_shorttall_natura");
	private static final ResourceLocation PYLON_CRYSTAL_TALL_GAIA_MODEL = new ResourceLocation("botania_pylon_crystal", "block/pylon_crystal_bipyramid_tall_gaia");
	private static final ResourceLocation PYLON_CRYSTAL_SHORT_GAIA_MODEL = new ResourceLocation("botania_pylon_crystal", "block/pylon_crystal_bipyramid_short_gaia");
	private static final ResourceLocation PYLON_CRYSTAL_TALLSHORT_GAIA_MODEL = new ResourceLocation("botania_pylon_crystal", "block/pylon_crystal_bipyramid_tallshort_gaia");
	private static final ResourceLocation PYLON_CRYSTAL_SHORTTALL_GAIA_MODEL = new ResourceLocation("botania_pylon_crystal", "block/pylon_crystal_bipyramid_shorttall_gaia");

	@Override
	public void onInitializeClient() {
		BotaniaPylonCrystalConfig.get();
		ModelLoadingPlugin.register(ctx -> ctx.addModels(
				ENCHANTED_PYLON_STATIC_MODEL,
				PYLON_COMBINED_MANA_MODEL,
				PYLON_COMBINED_NATURA_MODEL,
				PYLON_COMBINED_GAIA_MODEL,
				PYLON_RING_MANA_MODEL,
				PYLON_RING_NATURA_MODEL,
				PYLON_RING_GAIA_MODEL,
				PYLON_CRYSTAL_TALL_MODEL,
				PYLON_CRYSTAL_SHORT_MODEL,
				PYLON_CRYSTAL_TALLSHORT_MODEL,
				PYLON_CRYSTAL_SHORTTALL_MODEL,
				PYLON_CRYSTAL_TALL_NATURA_MODEL,
				PYLON_CRYSTAL_SHORT_NATURA_MODEL,
				PYLON_CRYSTAL_TALLSHORT_NATURA_MODEL,
				PYLON_CRYSTAL_SHORTTALL_NATURA_MODEL,
				PYLON_CRYSTAL_TALL_GAIA_MODEL,
				PYLON_CRYSTAL_SHORT_GAIA_MODEL,
				PYLON_CRYSTAL_TALLSHORT_GAIA_MODEL,
				PYLON_CRYSTAL_SHORTTALL_GAIA_MODEL
		));
		FabricLoader.getInstance().getModContainer("botania_pylon_crystal").ifPresent(container -> {
			boolean ok = ResourceManagerHelper.registerBuiltinResourcePack(
					new ResourceLocation("botania_pylon_crystal", "botania_pylon_override"),
					container,
					ResourcePackActivationType.ALWAYS_ENABLED
			);
			if (!ok) {
				LOGGER.warn("builtin resource pack NOT registered: botania_pylon_override ALWAYS_ENABLED ok=false");
			} else if (DEBUG_MODE) {
				LOGGER.info("builtin resource pack registered: botania_pylon_override ALWAYS_ENABLED ok=true");
				LOGGER.info("botania_pylon_crystal origin paths: {}", container.getOrigin().getPaths());
			}
		});
		if (DEBUG_MODE) {
			ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
				@Override
				public ResourceLocation getFabricId() {
					return new ResourceLocation("botania_pylon_crystal", "debug_model_sources");
				}

				@Override
				public void onResourceManagerReload(ResourceManager manager) {
					logModelSource(manager, BOTANIA_MANA_PYLON_BLOCK_MODEL);
					logModelSource(manager, BOTANIA_MANA_PYLON_ITEM_MODEL);
					logModelSource(manager, CUSTOM_PARENT_BLOCK_MODEL);
				}
			});
		}
		applyPylonLayerMapping("onInitializeClient");
		applyEnchantedPylonLayerMapping("onInitializeClient");
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> applyPylonLayerMapping("CLIENT_STARTED"));
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> applyEnchantedPylonLayerMapping("CLIENT_STARTED"));

		BlockEntityRenderers.register(BotaniaPylonCrystal.ENCHANTED_PYLON_BLOCK_ENTITY_TYPE, EnchantedPylonBlockEntityRenderer::new);
		BuiltinItemRendererRegistry.INSTANCE.register(BotaniaPylonCrystal.ENCHANTED_PYLON_ITEM,
				(stack, mode, ms, buffers, light, overlay) -> EnchantedPylonBlockEntityRenderer.renderItem(ms, buffers, light, overlay));
		registerPylonColors();
	}

	private static void registerPylonColors() {
		registerPylonColorsFor(BOTANIA_MANA_PYLON, "botania:mana_pylon");
		registerPylonColorsFor(BOTANIA_NATURA_PYLON, "botania:natura_pylon");
		registerPylonColorsFor(BOTANIA_GAIA_PYLON, "botania:gaia_pylon");
	}

	private static void registerPylonColorsFor(ResourceLocation id, String label) {
		Block block = BuiltInRegistries.BLOCK.get(id);
		Item item = BuiltInRegistries.ITEM.get(id);
		if (block == Blocks.AIR || item == net.minecraft.world.item.Items.AIR) {
			LOGGER.warn("{} not present in registry yet; dye color providers not registered", label);
			return;
		}

		ColorProviderRegistry.BLOCK.register((BlockState state, net.minecraft.world.level.BlockAndTintGetter world, BlockPos pos, int tintIndex) -> {
			if (tintIndex != 0 && tintIndex != 1) {
				return 0xFFFFFF;
			}
			if (tintIndex == 1 && !BotaniaPylonCrystalConfig.get().tintRingWithDye) {
				return 0xFFFFFF;
			}
			if (world == null || pos == null) {
				return 0xFFFFFF;
			}
			var be = world.getBlockEntity(pos);
			if (be instanceof ManaPylonDyedAccess access) {
				return dyeColorIdToRgb(access.botania_pylon_crystal$getDyeColorId());
			}
			return 0xFFFFFF;
		}, block);

		ColorProviderRegistry.ITEM.register((ItemStack stack, int tintIndex) -> {
			if (tintIndex != 0 && tintIndex != 1) {
				return 0xFFFFFF;
			}
			if (tintIndex == 1 && !BotaniaPylonCrystalConfig.get().tintRingWithDye) {
				return 0xFFFFFF;
			}
			var bet = stack.getTagElement("BlockEntityTag");
			if (bet != null && bet.contains(ManaPylonDyeingRecipe.DYE_COLOR_NBT_KEY)) {
				return dyeColorIdToRgb(bet.getInt(ManaPylonDyeingRecipe.DYE_COLOR_NBT_KEY));
			}
			return 0xFFFFFF;
		}, item);
	}

	private static int dyeColorIdToRgb(int id) {
		if (id < 0) {
			return 0xFFFFFF;
		}
		try {
			String[] palette = BotaniaPylonCrystalConfig.get().dyeTintHex;
			if (palette != null && id >= 0 && id < palette.length) {
				String hex = String.valueOf(palette[id]).trim();
				if (hex.startsWith("#")) {
					hex = hex.substring(1);
				}
				if (hex.startsWith("0x") || hex.startsWith("0X")) {
					hex = hex.substring(2);
				}
				if (!hex.isEmpty()) {
					return Integer.parseInt(hex, 16) & 0xFFFFFF;
				}
			}
		} catch (Exception ignored) {
		}
		DyeColor dye = DyeColor.byId(id);
		return dye == null ? 0xFFFFFF : dye.getTextColor();
	}

	public static RenderType getConfiguredPylonRenderType() {
		String mode = String.valueOf(BotaniaPylonCrystalConfig.get().renderLayer).trim().toLowerCase();
		if (mode.equals("translucent")) {
			return RenderType.translucent();
		}
		if (mode.equals("solid")) {
			return RenderType.solid();
		}
		if (mode.equals("auto") || mode.equals("vanilla")) {
			try {
				Block block = BuiltInRegistries.BLOCK.get(BOTANIA_MANA_PYLON);
				if (block != Blocks.AIR) {
					return ItemBlockRenderTypes.getChunkRenderType(block.defaultBlockState());
				}
			} catch (Exception ignored) {
			}
		}
		return RenderType.cutout();
	}

	public static RenderType getConfiguredEnchantedPylonRenderType() {
		String mode = String.valueOf(BotaniaPylonCrystalConfig.get().enchantedPylonRenderLayer).trim().toLowerCase();
		if (mode.equals("translucent")) {
			return RenderType.translucent();
		}
		if (mode.equals("solid")) {
			return RenderType.solid();
		}
		if (mode.equals("auto") || mode.equals("vanilla")) {
			try {
				if (BotaniaPylonCrystal.ENCHANTED_PYLON_BLOCK != null) {
					return ItemBlockRenderTypes.getChunkRenderType(BotaniaPylonCrystal.ENCHANTED_PYLON_BLOCK.defaultBlockState());
				}
			} catch (Exception ignored) {
			}
		}
		return RenderType.cutout();
	}

	private static void logModelSource(ResourceManager manager, ResourceLocation modelJson) {
		if (!DEBUG_MODE) {
			return;
		}
		try {
			Resource chosen = manager.getResourceOrThrow(modelJson);
			String chosenPack = chosen.sourcePackId();
			String chosenParent = extractParentLine(chosen);
			LOGGER.info("Model JSON source: {} from pack={} parentLine={}", modelJson, chosenPack, chosenParent);

			List<Resource> candidates = getAllCandidateResources(manager, modelJson);
			if (!candidates.isEmpty()) {
				LOGGER.info("Model JSON candidates: {} count={}", modelJson, candidates.size());
				for (int i = 0; i < candidates.size(); i++) {
					Resource r = candidates.get(i);
					String pack = safeSourcePackId(r);
					String parent = extractParentLine(r);
					String tag = pack.equals(chosenPack) ? " (CHOSEN_PACK)" : "";
					LOGGER.info("  candidate[{}] pack={}{} parentLine={}", i, pack, tag, parent);
				}
			} else {
				LOGGER.info("Model JSON candidates: {} (not available)", modelJson);
			}
		} catch (Exception e) {
			LOGGER.warn("Model JSON missing/unreadable: {} ({})", modelJson, e.toString());
		}
	}

	private static String safeSourcePackId(Resource resource) {
		try {
			return resource.sourcePackId();
		} catch (Exception e) {
			return "<unknown>";
		}
	}

	private static String extractParentLine(Resource resource) throws IOException {
		String parent = "";
		try (BufferedReader reader = resource.openAsReader()) {
			String line;
			while ((line = reader.readLine()) != null) {
				int idx = line.indexOf("\"parent\"");
				if (idx >= 0) {
					parent = line.trim();
					break;
				}
			}
		}
		return parent;
	}

	private static List<Resource> getAllCandidateResources(ResourceManager manager, ResourceLocation id) {
		List<Resource> out = new ArrayList<>();
		for (String methodName : new String[] { "getResources", "getAllResources", "getResourceStack" }) {
			try {
				Method m = manager.getClass().getMethod(methodName, ResourceLocation.class);
				Object result = m.invoke(manager, id);
				if (result instanceof List<?>) {
					for (Object o : (List<?>) result) {
						if (o instanceof Resource) {
							out.add((Resource) o);
						}
					}
					return out;
				}
			} catch (Exception ignored) {
				// Try next method name
			}
		}
		return out;
	}

	private static void applyPylonLayerMapping(String phase) {
		applyPylonLayerMappingOne(phase, BOTANIA_MANA_PYLON, "botania:mana_pylon");
		applyPylonLayerMappingOne(phase, BOTANIA_NATURA_PYLON, "botania:natura_pylon");
		applyPylonLayerMappingOne(phase, BOTANIA_GAIA_PYLON, "botania:gaia_pylon");
	}

	private static void applyEnchantedPylonLayerMapping(String phase) {
		if (BotaniaPylonCrystal.ENCHANTED_PYLON_BLOCK == null) {
			return;
		}
		RenderType type = getConfiguredEnchantedPylonRenderType();
		BlockRenderLayerMap.INSTANCE.putBlock(BotaniaPylonCrystal.ENCHANTED_PYLON_BLOCK, type);
		if (DEBUG_MODE) {
			LOGGER.info("[{}] applied {} to {}", phase, type, BotaniaPylonCrystal.ENCHANTED_PYLON_ID);
		}
	}

	private static void applyPylonLayerMappingOne(String phase, ResourceLocation id, String label) {
		Block block = BuiltInRegistries.BLOCK.get(id);
		if (block == Blocks.AIR) {
			LOGGER.warn("[{}] {} not present in registry yet; render layer not applied", phase, label);
			return;
		}
		RenderType type = getConfiguredPylonRenderType();
		BlockRenderLayerMap.INSTANCE.putBlock(block, type);
		if (DEBUG_MODE) {
			LOGGER.info("[{}] applied {} to {}", phase, type, id);
		}
	}
}
