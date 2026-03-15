/*
 * # Botania Pylon Crystal 
 * - addon replaces Botania's Pylon visuals with crystal json models.
 * - Overrides via a built-in resourcepack driven by config selected model JSON, dyable color, and client-side rotation.
 * 
 * # NoOpRecipeSerializer.java
 * - Minimal `RecipeSerializer` implementation for CustomRecipe types that don't need any JSON/network payload.
 * - Delegates to a factory function using only the recipe id.
 */

package corvaeoboro.botania_pylon_crystal;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class NoOpRecipeSerializer<T extends Recipe<?>> implements RecipeSerializer<T> {
	private final Function<ResourceLocation, T> factory;

	public NoOpRecipeSerializer(Function<ResourceLocation, T> factory) {
		this.factory = factory;
	}

	@Override
	public @NotNull T fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json) {
		return factory.apply(id);
	}

	@Override
	public @NotNull T fromNetwork(@NotNull ResourceLocation id, @NotNull FriendlyByteBuf buf) {
		return factory.apply(id);
	}

	@Override
	public void toNetwork(@NotNull FriendlyByteBuf buf, @NotNull T recipe) {
	}
}
