/*
 * # Botania Pylon Crystal 
 * - addon replaces Botania's Pylon visuals with crystal json models.
 * - Overrides via a built-in resourcepack driven by config selected model JSON, dyable color, and client-side rotation.
 * 
 * # ManaPylonDyeingRecipe.java
 * - Custom crafting recipe for dyeing the Mana Pylon item.
 * - Produces a mana pylon ItemStack with `BlockEntityTag` containing the dye color id.
 *
 * # NOTES:
 * - Enabled/disabled by `BotaniaPylonCrystalConfig.allowDyableVariants`.
 * - Uses `NoOpRecipeSerializer` so no JSON/network data is needed beyond the recipe id.
 * - The dye color is stored under `DYE_COLOR_NBT_KEY` and later read by client tint providers and render mixins.
 */

package corvaeoboro.botania_pylon_crystal;

import com.google.common.base.Suppliers;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ManaPylonDyeingRecipe extends CustomRecipe {
	public static final String DYE_COLOR_NBT_KEY = "botania_pylon_crystal_dye_color";
	public static final NoOpRecipeSerializer<ManaPylonDyeingRecipe> SERIALIZER = new NoOpRecipeSerializer<>(ManaPylonDyeingRecipe::new);

	private final Supplier<ItemStack> manaPylon = Suppliers.memoize(() -> {
		var item = BuiltInRegistries.ITEM.get(BotaniaPylonCrystal.BOTANIA_MANA_PYLON);
		return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
	});

	public ManaPylonDyeingRecipe(ResourceLocation id) {
		super(id, CraftingBookCategory.BUILDING);
	}

	@NotNull
	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public boolean matches(@NotNull CraftingContainer inv, @NotNull Level world) {
		if (!BotaniaPylonCrystalConfig.get().allowDyableVariants) {
			return false;
		}

		ItemStack pylon = ItemStack.EMPTY;
		ItemStack dye = ItemStack.EMPTY;

		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack stack = inv.getItem(i);
			if (stack.isEmpty()) {
				continue;
			}

			if (isManaPylon(stack)) {
				if (!pylon.isEmpty()) {
					return false;
				}
				pylon = stack;
				continue;
			}

			if (stack.getItem() instanceof DyeItem) {
				if (!dye.isEmpty()) {
					return false;
				}
				dye = stack;
				continue;
			}

			return false;
		}

		return !pylon.isEmpty() && !dye.isEmpty();
	}

	@NotNull
	@Override
	public ItemStack assemble(@NotNull CraftingContainer inv, @NotNull RegistryAccess registries) {
		ItemStack pylon = ItemStack.EMPTY;
		DyeItem dyeItem = null;

		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack stack = inv.getItem(i);
			if (stack.isEmpty()) {
				continue;
			}

			if (isManaPylon(stack) && pylon.isEmpty()) {
				pylon = stack;
				continue;
			}

			if (stack.getItem() instanceof DyeItem di && dyeItem == null) {
				dyeItem = di;
				continue;
			}
		}

		if (pylon.isEmpty() || dyeItem == null) {
			return ItemStack.EMPTY;
		}

		ItemStack out = pylon.copyWithCount(1);
		CompoundTag bet = out.getOrCreateTagElement("BlockEntityTag");
		bet.putInt(DYE_COLOR_NBT_KEY, dyeItem.getDyeColor().getId());

		return out;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}

	private boolean isManaPylon(ItemStack stack) {
		var expected = manaPylon.get();
		return !expected.isEmpty() && stack.is(expected.getItem());
	}
}
