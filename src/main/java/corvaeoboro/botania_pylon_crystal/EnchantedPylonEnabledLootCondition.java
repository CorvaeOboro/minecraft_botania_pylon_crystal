/*
 * # Botania Pylon Crystal 
 * - addon replaces Botania's Pylon visuals with crystal json models.
 * - Overrides via a built-in resourcepack driven by config selected model JSON, dyable color, and client-side rotation.
 * 
 * # EnchantedPylonEnabledLootCondition.java
 * - Loot condition used to gate enchanted pylon drops based on config.
 */

package corvaeoboro.botania_pylon_crystal;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import org.jetbrains.annotations.NotNull;

public class EnchantedPylonEnabledLootCondition implements LootItemCondition {
	public static final ResourceLocation ID = new ResourceLocation(BotaniaPylonCrystal.MODID, "enchanted_pylon_enabled");
	public static final LootItemConditionType TYPE = new LootItemConditionType(new Serializer());
	public static final EnchantedPylonEnabledLootCondition INSTANCE = new EnchantedPylonEnabledLootCondition();

	private EnchantedPylonEnabledLootCondition() {
	}

	@Override
	public boolean test(LootContext lootContext) {
		return Boolean.TRUE.equals(BotaniaPylonCrystalConfig.get().enableEnchantedPylon);
	}

	@NotNull
	@Override
	public LootItemConditionType getType() {
		return TYPE;
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<EnchantedPylonEnabledLootCondition> {
		@Override
		public void serialize(JsonObject json, EnchantedPylonEnabledLootCondition value, JsonSerializationContext serializationContext) {
		}

		@Override
		public EnchantedPylonEnabledLootCondition deserialize(JsonObject json, JsonDeserializationContext deserializationContext) {
			return INSTANCE;
		}
	}
}
