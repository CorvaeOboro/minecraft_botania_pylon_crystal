/*
 * # Botania Pylon Crystal 
 * - addon replaces Botania's Pylon visuals with crystal json models.
 * - Overrides via a built-in resourcepack driven by config selected model JSON, dyable color, and client-side rotation.
 * 
 * # PylonBlockEntityRendererMixin.java
 * - Overrides placed pylon rendering for Botania pylons (mana/natura/gaia) when the per-pylon override toggles are enabled.
 * - Renders the selected crystal baked model (tall/short/tallshort/shorttall) and the correct ring baked model.
 * - Applies optional rotation based on `BotaniaPylonCrystalConfig.enableRotation` / `rotationSpeedDegPerTick`.
 * - Cancels Botania's original renderer to avoid double-rendering.
 *
 * # NOTES:
 * - Uses the render layer selection from `BotaniaPylonCrystalClient.getConfiguredPylonRenderType()`.
 * - Render routing (MODEL vs ENTITYBLOCK_ANIMATED) is handled by `PylonBlockRenderShapeMixin`.
 * - Ring tinting uses `TintIndexBakedModel` (tint index 1).
 */

package corvaeoboro.botania_pylon_crystal.mixin;

import corvaeoboro.botania_pylon_crystal.BotaniaPylonCrystalConfig;
import corvaeoboro.botania_pylon_crystal.TintIndexBakedModel;

import com.mojang.logging.LogUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Coerce;

@Pseudo
@Mixin(targets = "vazkii.botania.client.render.block_entity.PylonBlockEntityRenderer", remap = false)
public class PylonBlockEntityRendererMixin {
	private static final boolean DEBUG_MODE = false;
	private static final Logger LOGGER = LogUtils.getLogger();
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
	private static final float RING_ROTATION_SPEED_MULT = 0.8F;
	private static boolean LOGGED_ONCE = false;

	private static ResourceLocation crystalModelFor(String variant) {
		return crystalModelFor(BOTANIA_MANA_PYLON, variant);
	}

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
		if (BOTANIA_NATURA_PYLON.equals(id)) {
			return PYLON_RING_NATURA_MODEL;
		}
		if (BOTANIA_GAIA_PYLON.equals(id)) {
			return PYLON_RING_GAIA_MODEL;
		}
		return PYLON_RING_MANA_MODEL;
	}

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void botania_pylon_crystal$cancelPlacedManaPylonRenderer(
			@Coerce Object pylon,
			float pticks,
			PoseStack ms,
			MultiBufferSource buffers,
			int light,
			int overlay,
			CallbackInfo ci
	) {
		if (pylon instanceof BlockEntity be) {
			var id = BuiltInRegistries.BLOCK.getKey(be.getBlockState().getBlock());
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

			if (!LOGGED_ONCE) {
				LOGGED_ONCE = true;
				if (DEBUG_MODE) {
					LOGGER.info("PylonBlockEntityRendererMixin active for {} (enableRotation={})", id, BotaniaPylonCrystalConfig.get().enableRotation);
				}
			}

			var state = be.getBlockState();
			RenderType type = corvaeoboro.botania_pylon_crystal.BotaniaPylonCrystalClient.getConfiguredPylonRenderType();
			var level = be.getLevel();
			var pos = be.getBlockPos();
			if (level == null) {
				ci.cancel();
				return;
			}
			var brd = Minecraft.getInstance().getBlockRenderer();
			var modelManager = Minecraft.getInstance().getModelManager();
			String variant = isMana ? BotaniaPylonCrystalConfig.get().manaPylonCrystalVariant
					: (isNatura ? BotaniaPylonCrystalConfig.get().naturaPylonCrystalVariant
					: BotaniaPylonCrystalConfig.get().gaiaPylonCrystalVariant);
			var crystalModel = BakedModelManagerHelper.getModel(modelManager, crystalModelFor(id, variant));
			var model = crystalModel != null ? crystalModel : brd.getBlockModel(state);
			VertexConsumer buffer = buffers.getBuffer(type);

			float deg = 0.0F;
			if (BotaniaPylonCrystalConfig.get().enableRotation) {
				double time = level.getGameTime() + pticks;
				deg = (float) (time * BotaniaPylonCrystalConfig.get().rotationSpeedDegPerTick);
			}

			ms.pushPose();
			ms.translate(0.5, 0.0, 0.5);
			if (BotaniaPylonCrystalConfig.get().enableRotation) {
				ms.mulPose(Axis.YP.rotationDegrees(deg));
			}
			ms.translate(-0.5, 0.0, -0.5);
			brd.getModelRenderer().tesselateBlock(level, model, state, pos, ms,
					buffer, false, RandomSource.create(), state.getSeed(pos), overlay);
			ms.popPose();

			if (!BotaniaPylonCrystalConfig.get().displayOnlyCrystal) {
				ResourceLocation ringId = ringModelFor(id);
				var ringModel = BakedModelManagerHelper.getModel(modelManager, ringId);
				if (ringModel != null) {
					var tintedRingModel = new TintIndexBakedModel(ringModel, 1);
					ms.pushPose();
					ms.translate(0.5, 0.0, 0.5);
					if (BotaniaPylonCrystalConfig.get().enableRotation) {
						float ringDeg = -(deg * RING_ROTATION_SPEED_MULT);
						ms.mulPose(Axis.YP.rotationDegrees(ringDeg));
					}
					ms.translate(-0.5, 0.0, -0.5);
					brd.getModelRenderer().tesselateBlock(level, tintedRingModel, state, pos, ms,
							buffer, false, RandomSource.create(), state.getSeed(pos), overlay);
					ms.popPose();
				} else if (DEBUG_MODE) {
					LOGGER.info("ring model missing/unbaked: {}", ringId);
				}
			}

			ci.cancel();
		}
	}

}
