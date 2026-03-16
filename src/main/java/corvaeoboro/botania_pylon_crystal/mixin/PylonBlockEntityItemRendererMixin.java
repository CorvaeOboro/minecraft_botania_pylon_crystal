/*
 * # Botania Pylon Crystal 
 * - addon replaces Botania's Pylon visuals with crystal json models.
 * - Overrides via a built-in resourcepack driven by config selected model JSON, dyable color, and client-side rotation.
 * 
 * # PylonBlockEntityItemRendererMixin.java
 * - Overrides Botania's pylon item rendering (TEISR) for mana/natura/gaia pylons when the per-pylon override toggles are enabled.
 * - Renders the selected crystal model variant (tall/short/tallshort/shorttall) plus the corresponding ring model.
 * - Applies dye tinting to the ring by wrapping it with `TintIndexBakedModel` using tint index 1 , if enabled in the config
 */

package corvaeoboro.botania_pylon_crystal.mixin;

import corvaeoboro.botania_pylon_crystal.BotaniaPylonCrystalConfig;
import corvaeoboro.botania_pylon_crystal.TintIndexBakedModel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "vazkii.botania.client.render.block_entity.PylonBlockEntityRenderer$ItemRenderer", remap = false)
public class PylonBlockEntityItemRendererMixin {
	private static final ResourceLocation BOTANIA_MANA_PYLON = new ResourceLocation("botania", "mana_pylon");
	private static final ResourceLocation BOTANIA_NATURA_PYLON = new ResourceLocation("botania", "natura_pylon");
	private static final ResourceLocation BOTANIA_GAIA_PYLON = new ResourceLocation("botania", "gaia_pylon");

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
	private static final ResourceLocation PYLON_RING_MANA_MODEL = new ResourceLocation("botania_pylon_crystal", "block/pylon_ring_mana");
	private static final ResourceLocation PYLON_RING_NATURA_MODEL = new ResourceLocation("botania_pylon_crystal", "block/pylon_ring_natura");
	private static final ResourceLocation PYLON_RING_GAIA_MODEL = new ResourceLocation("botania_pylon_crystal", "block/pylon_ring_gaia");

	private static ResourceLocation crystalModelFor(ResourceLocation id, String variant) {
		String v = String.valueOf(variant).trim().toLowerCase(java.util.Locale.ROOT);
		if (BOTANIA_NATURA_PYLON.equals(id)) {
			return switch (v) {
				case "short" -> PYLON_CRYSTAL_SHORT_NATURA_MODEL;
				case "tallshort" -> PYLON_CRYSTAL_TALLSHORT_NATURA_MODEL;
				case "shorttall" -> PYLON_CRYSTAL_SHORTTALL_NATURA_MODEL;
				default -> PYLON_CRYSTAL_TALL_NATURA_MODEL;
			};
		}
		if (BOTANIA_GAIA_PYLON.equals(id)) {
			return switch (v) {
				case "short" -> PYLON_CRYSTAL_SHORT_GAIA_MODEL;
				case "tallshort" -> PYLON_CRYSTAL_TALLSHORT_GAIA_MODEL;
				case "shorttall" -> PYLON_CRYSTAL_SHORTTALL_GAIA_MODEL;
				default -> PYLON_CRYSTAL_TALL_GAIA_MODEL;
			};
		}
		return switch (v) {
			case "short" -> PYLON_CRYSTAL_SHORT_MODEL;
			case "tallshort" -> PYLON_CRYSTAL_TALLSHORT_MODEL;
			case "shorttall" -> PYLON_CRYSTAL_SHORTTALL_MODEL;
			default -> PYLON_CRYSTAL_TALL_MODEL;
		};
	}

	private static ResourceLocation ringModelFor(ResourceLocation id) {
		String ringChoice = BOTANIA_NATURA_PYLON.equals(id) ? BotaniaPylonCrystalConfig.get().naturaPylonRingModel
				: (BOTANIA_GAIA_PYLON.equals(id) ? BotaniaPylonCrystalConfig.get().gaiaPylonRingModel
				: BotaniaPylonCrystalConfig.get().manaPylonRingModel);
		String v = String.valueOf(ringChoice).trim().toLowerCase(java.util.Locale.ROOT);
		return switch (v) {
			case "natura" -> PYLON_RING_NATURA_MODEL;
			case "gaia" -> PYLON_RING_GAIA_MODEL;
			default -> PYLON_RING_MANA_MODEL;
		};
	}

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void botania_pylon_crystal$overridePylonItemRender(ItemStack stack, ItemDisplayContext type, PoseStack ms, MultiBufferSource buffers, int light, int overlay, CallbackInfo ci) {
		var block = net.minecraft.world.level.block.Block.byItem(stack.getItem());
		if (block == null) {
			return;
		}
		var id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block);
		boolean isMana = BOTANIA_MANA_PYLON.equals(id);
		boolean isNatura = BOTANIA_NATURA_PYLON.equals(id);
		boolean isGaia = BOTANIA_GAIA_PYLON.equals(id);
		if (!isMana && !isNatura && !isGaia) {
			return;
		}

		boolean overrideEnabled = (isMana && Boolean.TRUE.equals(BotaniaPylonCrystalConfig.get().overrideManaPylon))
				|| (isNatura && Boolean.TRUE.equals(BotaniaPylonCrystalConfig.get().overrideNaturaPylon))
				|| (isGaia && Boolean.TRUE.equals(BotaniaPylonCrystalConfig.get().overrideGaiaPylon));
		if (!overrideEnabled) {
			return;
		}

		var modelManager = Minecraft.getInstance().getModelManager();
		String variant = isMana ? BotaniaPylonCrystalConfig.get().manaPylonCrystalVariant
				: (isNatura ? BotaniaPylonCrystalConfig.get().naturaPylonCrystalVariant
				: BotaniaPylonCrystalConfig.get().gaiaPylonCrystalVariant);
		boolean perPylonDisplayOnlyCrystal = isNatura ? BotaniaPylonCrystalConfig.get().naturaPylonDisplayOnlyCrystal
				: (isGaia ? BotaniaPylonCrystalConfig.get().gaiaPylonDisplayOnlyCrystal
				: BotaniaPylonCrystalConfig.get().manaPylonDisplayOnlyCrystal);

		var crystalModel = BakedModelManagerHelper.getModel(modelManager, crystalModelFor(id, variant));
		var ringModel = BakedModelManagerHelper.getModel(modelManager, ringModelFor(id));

		var brd = Minecraft.getInstance().getBlockRenderer();
		var renderer = brd.getModelRenderer();
		VertexConsumer buffer = buffers.getBuffer(Sheets.translucentCullBlockSheet());

		if (crystalModel != null) {
			renderer.renderModel(ms.last(), buffer, null, crystalModel, 1, 1, 1, light, overlay);
		}
		if (!BotaniaPylonCrystalConfig.get().displayOnlyCrystal && !perPylonDisplayOnlyCrystal && ringModel != null) {
			var tintedRing = new TintIndexBakedModel(ringModel, 1);
			renderer.renderModel(ms.last(), buffer, null, tintedRing, 1, 1, 1, light, overlay);
		}

		ci.cancel();
	}
}
