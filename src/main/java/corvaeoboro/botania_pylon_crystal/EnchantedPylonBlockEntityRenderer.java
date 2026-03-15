/*
 * # Botania Pylon Crystal 
 * - addon replaces Botania's Pylon visuals with crystal json models.
 * - Overrides via a built-in resourcepack driven by config selected model JSON, dyable color, and client-side rotation.
 * 
 * # EnchantedPylonBlockEntityRenderer.java
 * - Block entity renderer + builtin item renderer for the standalone Enchanted Pylon.
 * - When rotation is enabled in config, renders the crystal + ring baked models with a Y-axis rotation.
 * - Uses the configured Enchanted Pylon render layer (cutout/translucent/solid) for its rotating render path.
 * - When rotation is disabled:
 *   - placed block uses vanilla model rendering (no block entity rendering)
 *   - item rendering falls back to the static combined block model
 */

package corvaeoboro.botania_pylon_crystal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public class EnchantedPylonBlockEntityRenderer implements BlockEntityRenderer<EnchantedPylonBlockEntity> {
	private static final ResourceLocation ENCHANTED_PYLON_STATIC_MODEL = new ResourceLocation("botania_pylon_crystal", "block/enchanted_pylon");
	private static final ResourceLocation PYLON_CRYSTAL_TALL_MODEL = new ResourceLocation("botania_pylon_crystal", "block/pylon_crystal_bipyramid_tall");
	private static final ResourceLocation PYLON_CRYSTAL_SHORT_MODEL = new ResourceLocation("botania_pylon_crystal", "block/pylon_crystal_bipyramid_short");
	private static final ResourceLocation PYLON_CRYSTAL_TALLSHORT_MODEL = new ResourceLocation("botania_pylon_crystal", "block/pylon_crystal_bipyramid_tallshort");
	private static final ResourceLocation PYLON_CRYSTAL_SHORTTALL_MODEL = new ResourceLocation("botania_pylon_crystal", "block/pylon_crystal_bipyramid_shorttall");
	private static final ResourceLocation PYLON_RING_MANA_MODEL = new ResourceLocation("botania_pylon_crystal", "block/pylon_ring_mana");
	private static final float RING_ROTATION_SPEED_MULT = 0.8F;

	public EnchantedPylonBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	private static ResourceLocation crystalModelFor(String variant) {
		String v = String.valueOf(variant).trim().toLowerCase(java.util.Locale.ROOT);
		return switch (v) {
			case "short" -> PYLON_CRYSTAL_SHORT_MODEL;
			case "tallshort" -> PYLON_CRYSTAL_TALLSHORT_MODEL;
			case "shorttall" -> PYLON_CRYSTAL_SHORTTALL_MODEL;
			default -> PYLON_CRYSTAL_TALL_MODEL;
		};
	}

	@Override
	public void render(EnchantedPylonBlockEntity be, float pticks, PoseStack ms, MultiBufferSource buffers, int light, int overlay) {
		if (!BotaniaPylonCrystalConfig.get().enchantedPylonEnableRotation) {
			return;
		}

		var level = be.getLevel();
		if (level == null) {
			return;
		}

		RenderType type = corvaeoboro.botania_pylon_crystal.BotaniaPylonCrystalClient.getConfiguredEnchantedPylonRenderType();
		var brd = Minecraft.getInstance().getBlockRenderer();
		var modelManager = Minecraft.getInstance().getModelManager();

		String variant = BotaniaPylonCrystalConfig.get().enchantedPylonCrystalVariant;
		var crystalModel = BakedModelManagerHelper.getModel(modelManager, crystalModelFor(variant));
		var ringModel = BakedModelManagerHelper.getModel(modelManager, PYLON_RING_MANA_MODEL);

		double timeTicks = (Util.getMillis() / 50.0) + pticks;
		float deg = (float) (timeTicks * BotaniaPylonCrystalConfig.get().enchantedPylonRotationSpeedDegPerTick);

		VertexConsumer buffer = buffers.getBuffer(type);
		ms.pushPose();
		ms.translate(0.5, 0.0, 0.5);
		ms.mulPose(Axis.YP.rotationDegrees(deg));
		ms.translate(-0.5, 0.0, -0.5);

		if (crystalModel != null) {
			brd.getModelRenderer().tesselateBlock(level, crystalModel, be.getBlockState(), be.getBlockPos(), ms,
					buffer, false, RandomSource.create(), be.getBlockState().getSeed(be.getBlockPos()), overlay);
		}

		if (!BotaniaPylonCrystalConfig.get().enchantedPylonDisplayOnlyCrystal && ringModel != null) {
			var tintedRingModel = new TintIndexBakedModel(ringModel, 1);
			ms.pushPose();
			ms.translate(0.5, 0.0, 0.5);
			ms.mulPose(Axis.YP.rotationDegrees(-(deg * RING_ROTATION_SPEED_MULT)));
			ms.translate(-0.5, 0.0, -0.5);
			brd.getModelRenderer().tesselateBlock(level, tintedRingModel, be.getBlockState(), be.getBlockPos(), ms,
					buffer, false, RandomSource.create(), be.getBlockState().getSeed(be.getBlockPos()), overlay);
			ms.popPose();
		}

		ms.popPose();
	}

	public static void renderItem(PoseStack ms, MultiBufferSource buffers, int light, int overlay) {
		var modelManager = Minecraft.getInstance().getModelManager();
		if (!BotaniaPylonCrystalConfig.get().enchantedPylonEnableRotation) {
			var brd = Minecraft.getInstance().getBlockRenderer();
			var renderer = brd.getModelRenderer();
			var staticModel = BakedModelManagerHelper.getModel(modelManager, ENCHANTED_PYLON_STATIC_MODEL);
			if (staticModel == null) {
				return;
			}
			VertexConsumer buffer = buffers.getBuffer(Sheets.translucentCullBlockSheet());
			renderer.renderModel(ms.last(), buffer, null, staticModel, 1, 1, 1, light, overlay);
			return;
		}

		var crystalModel = BakedModelManagerHelper.getModel(modelManager, crystalModelFor(BotaniaPylonCrystalConfig.get().enchantedPylonCrystalVariant));
		var ringModel = BakedModelManagerHelper.getModel(modelManager, PYLON_RING_MANA_MODEL);
		var brd = Minecraft.getInstance().getBlockRenderer();
		var renderer = brd.getModelRenderer();
		VertexConsumer buffer = buffers.getBuffer(Sheets.translucentCullBlockSheet());

		double timeTicks = (Util.getMillis() / 50.0);
		float deg = (float) (timeTicks * BotaniaPylonCrystalConfig.get().enchantedPylonRotationSpeedDegPerTick);

		ms.pushPose();
		ms.translate(0.5, 0.0, 0.5);
		ms.mulPose(Axis.YP.rotationDegrees(deg));
		ms.translate(-0.5, 0.0, -0.5);

		if (crystalModel != null) {
			renderer.renderModel(ms.last(), buffer, null, crystalModel, 1, 1, 1, light, overlay);
		}
		if (!BotaniaPylonCrystalConfig.get().enchantedPylonDisplayOnlyCrystal && ringModel != null) {
			var tintedRing = new TintIndexBakedModel(ringModel, 1);
			renderer.renderModel(ms.last(), buffer, null, tintedRing, 1, 1, 1, light, overlay);
		}

		ms.popPose();
	}
}
