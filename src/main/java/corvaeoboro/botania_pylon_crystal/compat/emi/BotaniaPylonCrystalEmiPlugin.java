/*
 * # Botania Pylon Crystal 
 * - addon replaces Botania's Pylon visuals with crystal json models.
 * - Overrides via a built-in resourcepack driven by config selected model JSON, dyable color, and client-side rotation.
 * 
 * # BotaniaPylonCrystalEmiPlugin.java
 * - EMI integration that exposes the Mana Pylon dyeing recipes as EMI crafting recipes.
 * - Generates one recipe per `DyeColor` when dyeable variants are enabled in config.
 */

package corvaeoboro.botania_pylon_crystal.compat.emi;

import corvaeoboro.botania_pylon_crystal.BotaniaPylonCrystal;
import corvaeoboro.botania_pylon_crystal.BotaniaPylonCrystalConfig;
import corvaeoboro.botania_pylon_crystal.ManaPylonDyeingRecipe;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class BotaniaPylonCrystalEmiPlugin implements EmiPlugin {
	@Override
	public void register(EmiRegistry registry) {
		if (!BotaniaPylonCrystalConfig.get().allowDyableVariants) {
			return;
		}

		Item pylonItem = BuiltInRegistries.ITEM.get(BotaniaPylonCrystal.BOTANIA_MANA_PYLON);
		if (pylonItem == null || pylonItem == Items.AIR) {
			return;
		}

		for (DyeColor dye : DyeColor.values()) {
			Item dyeItem = DyeColorToItem.itemFor(dye);
			if (dyeItem == null || dyeItem == Items.AIR) {
				continue;
			}

			ItemStack output = new ItemStack(pylonItem);
			CompoundTag bet = output.getOrCreateTagElement("BlockEntityTag");
			bet.putInt(ManaPylonDyeingRecipe.DYE_COLOR_NBT_KEY, dye.getId());

			List<EmiIngredient> inputs = List.of(
					EmiStack.of(new ItemStack(pylonItem)),
					EmiStack.of(new ItemStack(dyeItem))
			);

			var id = new ResourceLocation(BotaniaPylonCrystal.MODID, "mana_pylon_dyeing/" + dye.getName());
			registry.addRecipe(new EmiCraftingRecipe(inputs, EmiStack.of(output), id));
		}
	}

	private static final class DyeColorToItem {
		private DyeColorToItem() {
		}

		static Item itemFor(DyeColor dye) {
			return switch (dye) {
				case WHITE -> Items.WHITE_DYE;
				case ORANGE -> Items.ORANGE_DYE;
				case MAGENTA -> Items.MAGENTA_DYE;
				case LIGHT_BLUE -> Items.LIGHT_BLUE_DYE;
				case YELLOW -> Items.YELLOW_DYE;
				case LIME -> Items.LIME_DYE;
				case PINK -> Items.PINK_DYE;
				case GRAY -> Items.GRAY_DYE;
				case LIGHT_GRAY -> Items.LIGHT_GRAY_DYE;
				case CYAN -> Items.CYAN_DYE;
				case PURPLE -> Items.PURPLE_DYE;
				case BLUE -> Items.BLUE_DYE;
				case BROWN -> Items.BROWN_DYE;
				case GREEN -> Items.GREEN_DYE;
				case RED -> Items.RED_DYE;
				case BLACK -> Items.BLACK_DYE;
			};
		}
	}
}
