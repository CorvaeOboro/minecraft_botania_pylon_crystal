/*
 * # Botania Pylon Crystal 
 * - addon replaces Botania's Pylon visuals with crystal json models.
 * - Overrides via a built-in resourcepack driven by config selected model JSON, dyable color, and client-side rotation.
 * 
 * # EnchantedPylonDyeingRecipe.java
 * - Custom crafting recipe for dyeing the standalone Enchanted Pylon item.
 * - Produces an enchanted pylon ItemStack with `BlockEntityTag` containing the dye color id.
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

public class EnchantedPylonDyeingRecipe extends CustomRecipe {
	public static final NoOpRecipeSerializer<EnchantedPylonDyeingRecipe> SERIALIZER = new NoOpRecipeSerializer<>(EnchantedPylonDyeingRecipe::new);

	private final Supplier<ItemStack> enchantedPylon = Suppliers.memoize(() -> {
		var item = BuiltInRegistries.ITEM.get(BotaniaPylonCrystal.ENCHANTED_PYLON_ID);
		return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
	});

	public EnchantedPylonDyeingRecipe(ResourceLocation id) {
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
		if (!Boolean.TRUE.equals(BotaniaPylonCrystalConfig.get().enableEnchantedPylon)) {
			return false;
		}

		ItemStack pylon = ItemStack.EMPTY;
		ItemStack dye = ItemStack.EMPTY;

		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack stack = inv.getItem(i);
			if (stack.isEmpty()) {
				continue;
			}

			if (isEnchantedPylon(stack)) {
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

			if (isEnchantedPylon(stack) && pylon.isEmpty()) {
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
		bet.putInt(ManaPylonDyeingRecipe.DYE_COLOR_NBT_KEY, dyeItem.getDyeColor().getId());

		return out;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}

	private boolean isEnchantedPylon(ItemStack stack) {
		var expected = enchantedPylon.get();
		return !expected.isEmpty() && stack.is(expected.getItem());
	}
}
