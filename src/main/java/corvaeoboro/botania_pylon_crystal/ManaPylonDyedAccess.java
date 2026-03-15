/*
 * # Botania Pylon Crystal 
 * - addon replaces Botania's Pylon visuals with crystal json models.
 * - Overrides via a built-in resourcepack driven by config selected model JSON, dyable color, and client-side rotation.
 * 
 * # ManaPylonDyedAccess.java
 * - Small interface used as an access shim for storing a dye color id on the Mana Pylon BlockEntity.
 * - Implemented by `mixin/PylonBlockEntityDyeMixin` and consumed by:
 *   - `BotaniaPylonCrystal` (right-click dye application)
 *   - `BotaniaPylonCrystalClient` (tint providers)
 */

package corvaeoboro.botania_pylon_crystal;

public interface ManaPylonDyedAccess {
	int botania_pylon_crystal$getDyeColorId();
	void botania_pylon_crystal$setDyeColorId(int id);
}
