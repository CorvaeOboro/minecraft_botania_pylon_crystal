/*
 * # Botania Pylon Crystal 
 * - addon replaces Botania's Pylon visuals with crystal json models.
 * - Overrides via a built-in resourcepack driven by config selected model JSON, dyable color, and client-side rotation.
 * 
 * # EnchantedPylonRecipe.java
 * - Custom crafting recipe for the standalone Enchanted Pylon.
 */

package corvaeoboro.botania_pylon_crystal;

import com.google.common.base.Suppliers;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class EnchantedPylonRecipe extends CustomRecipe {
	public static final NoOpRecipeSerializer<EnchantedPylonRecipe> SERIALIZER = new NoOpRecipeSerializer<>(EnchantedPylonRecipe::new);

	private final Supplier<ItemStack> output = Suppliers.memoize(() -> {
		var item = BuiltInRegistries.ITEM.get(BotaniaPylonCrystal.ENCHANTED_PYLON_ID);
		return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
	});

	public EnchantedPylonRecipe(ResourceLocation id) {
		super(id, CraftingBookCategory.BUILDING);
	}

	@NotNull
	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public boolean matches(@NotNull CraftingContainer inv, @NotNull Level world) {
		if (!Boolean.TRUE.equals(BotaniaPylonCrystalConfig.get().enableEnchantedPylon)) {
			return false;
		}
		if (inv.getWidth() < 3 || inv.getHeight() < 3) {
			return false;
		}

		for (int offY = 0; offY <= inv.getHeight() - 3; offY++) {
			for (int offX = 0; offX <= inv.getWidth() - 3; offX++) {
				if (matchesAt(inv, offX, offY) && emptyElsewhere(inv, offX, offY)) {
					return true;
				}
			}
		}

		return false;
	}

	private static boolean matchesAt(CraftingContainer inv, int offX, int offY) {
		ItemStack s00 = inv.getItem((offY + 0) * inv.getWidth() + (offX + 0));
		ItemStack s01 = inv.getItem((offY + 0) * inv.getWidth() + (offX + 1));
		ItemStack s02 = inv.getItem((offY + 0) * inv.getWidth() + (offX + 2));
		ItemStack s10 = inv.getItem((offY + 1) * inv.getWidth() + (offX + 0));
		ItemStack s11 = inv.getItem((offY + 1) * inv.getWidth() + (offX + 1));
		ItemStack s12 = inv.getItem((offY + 1) * inv.getWidth() + (offX + 2));
		ItemStack s20 = inv.getItem((offY + 2) * inv.getWidth() + (offX + 0));
		ItemStack s21 = inv.getItem((offY + 2) * inv.getWidth() + (offX + 1));
		ItemStack s22 = inv.getItem((offY + 2) * inv.getWidth() + (offX + 2));

		return isItem(s00, Items.LAPIS_LAZULI)
				&& isItem(s01, Items.BOOK)
				&& isItem(s02, Items.LAPIS_LAZULI)
				&& isItem(s10, Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
				&& isItem(s11, Items.DIAMOND_BLOCK)
				&& isItem(s12, Items.LIGHT_WEIGHTED_PRESSURE_PLATE)
				&& isItem(s20, Items.GLASS)
				&& isItem(s21, Items.SOUL_LANTERN)
				&& isItem(s22, Items.GLASS);
	}

	private static boolean emptyElsewhere(CraftingContainer inv, int offX, int offY) {
		for (int y = 0; y < inv.getHeight(); y++) {
			for (int x = 0; x < inv.getWidth(); x++) {
				boolean inPattern = x >= offX && x < offX + 3 && y >= offY && y < offY + 3;
				ItemStack stack = inv.getItem(y * inv.getWidth() + x);
				if (!inPattern && !stack.isEmpty()) {
					return false;
				}
			}
		}
		return true;
	}

	private static boolean isItem(ItemStack stack, Item item) {
		return !stack.isEmpty() && stack.is(item);
	}

	@NotNull
	@Override
	public ItemStack assemble(@NotNull CraftingContainer inv, @NotNull RegistryAccess registries) {
		if (!Boolean.TRUE.equals(BotaniaPylonCrystalConfig.get().enableEnchantedPylon)) {
			return ItemStack.EMPTY;
		}
		var out = output.get();
		return out.isEmpty() ? ItemStack.EMPTY : out.copyWithCount(1);
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width >= 3 && height >= 3;
	}
}
