/*
 * # Botania Pylon Crystal 
 * - addon replaces Botania's Pylon visuals with crystal json models.
 * - Overrides via a built-in resourcepack driven by config selected model JSON, dyable color, and client-side rotation.
 * 
 * # BotaniaPylonCrystal.java
 * - Common/server mod entrypoint.
 * - Registers the mana pylon dyeing recipe serializer.
 * - Registers the standalone Enchanted Pylon block + item + block entity type.
 * - Handles in-world dye application (right-click mana pylon with a DyeItem) when enabled in config.
 * - Botania is an optional dependency; when Botania is absent, the mod still loads and the Enchanted Pylon is available.
 *
 * # NOTES:
 * - `BotaniaPylonCrystalConfig.allowDyableVariants` gates both crafting and right-click dyeing.
 * - Dye state is stored on the pylon BlockEntity via `ManaPylonDyedAccess` (implemented by a mixin).
 */

package corvaeoboro.botania_pylon_crystal;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BotaniaPylonCrystal implements ModInitializer {
	public static final String MODID = "botania_pylon_crystal";
	public static final ResourceLocation BOTANIA_MANA_PYLON = new ResourceLocation("botania", "mana_pylon");
	public static final ResourceLocation MANA_PYLON_DYEING_RECIPE_ID = new ResourceLocation(MODID, "mana_pylon_dyeing");
	public static final ResourceLocation ENCHANTED_PYLON_ID = new ResourceLocation(MODID, "enchanted_pylon");

	public static Block ENCHANTED_PYLON_BLOCK;
	public static net.minecraft.world.item.Item ENCHANTED_PYLON_ITEM;
	public static BlockEntityType<EnchantedPylonBlockEntity> ENCHANTED_PYLON_BLOCK_ENTITY_TYPE;

	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, MANA_PYLON_DYEING_RECIPE_ID, ManaPylonDyeingRecipe.SERIALIZER);

		ENCHANTED_PYLON_BLOCK = Registry.register(BuiltInRegistries.BLOCK, ENCHANTED_PYLON_ID,
				new EnchantedPylonBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(Blocks.BOOKSHELF).noOcclusion()));
		ENCHANTED_PYLON_ITEM = Registry.register(BuiltInRegistries.ITEM, ENCHANTED_PYLON_ID,
				new BlockItem(ENCHANTED_PYLON_BLOCK, new net.minecraft.world.item.Item.Properties()));
		ENCHANTED_PYLON_BLOCK_ENTITY_TYPE = Registry.register(
				BuiltInRegistries.BLOCK_ENTITY_TYPE,
				ENCHANTED_PYLON_ID,
				FabricBlockEntityTypeBuilder.create(EnchantedPylonBlockEntity::new, ENCHANTED_PYLON_BLOCK).build()
		);

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (!BotaniaPylonCrystalConfig.get().allowDyableVariants) {
				return InteractionResult.PASS;
			}

			var pos = hitResult.getBlockPos();
			var state = world.getBlockState(pos);
			var id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
			if (!BOTANIA_MANA_PYLON.equals(id)) {
				return InteractionResult.PASS;
			}

			var stack = player.getItemInHand(hand);
			if (!(stack.getItem() instanceof DyeItem dyeItem)) {
				return InteractionResult.PASS;
			}

			DyeColor dye = dyeItem.getDyeColor();
			if (world.isClientSide) {
				return InteractionResult.SUCCESS;
			}

			var be = world.getBlockEntity(pos);
			if (be instanceof ManaPylonDyedAccess access) {
				access.botania_pylon_crystal$setDyeColorId(dye.getId());
				be.setChanged();
				world.sendBlockUpdated(pos, state, state, 3);
				if (!player.getAbilities().instabuild) {
					stack.shrink(1);
				}
				return InteractionResult.CONSUME;
			}

			return InteractionResult.PASS;
		});
	}
}
