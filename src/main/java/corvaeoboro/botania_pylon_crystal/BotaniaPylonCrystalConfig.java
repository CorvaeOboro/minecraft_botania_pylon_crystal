/*
 * # Botania Pylon Crystal 
 * - addon replaces Botania's Pylon visuals with crystal json models.
 * - Overrides via a built-in resourcepack driven by config selected model JSON, dyable color, and client-side rotation.
 *
 * # BotaniaPylonCrystalConfig.java
 * - Loads/saves `config/botania_pylon_crystal.json5`.
 * - Writes a JSON5-style config with `//` comments and section spacing for readability.
 * - Uses Gson lenient parsing so `//` comments are accepted.
 * - Holds the set of runtime toggles ( rotation enable and speed, render layer ).
 *
 * # CONFIG:
 * - `schemaVersion`: internal schema version.
 *
 * ## Botania Overrides
 * - `override_mana_pylon`: override botania:mana_pylon rendering with this addon's pylon crystal model.
 * - `override_natura_pylon`: override botania:natura_pylon rendering with this addon's pylon crystal model.
 * - `override_gaia_pylon`: override botania:gaia_pylon rendering with this addon's pylon crystal model.
 *
 * ## Crystal Variants
 * - `mana_pylon_crystal_variant`: tall, short, tallshort, shorttall.
 * - `natura_pylon_crystal_variant`: tall, short, tallshort, shorttall.
 * - `gaia_pylon_crystal_variant`: tall, short, tallshort, shorttall.
 *
 * ## Pylon Crystal Rendering
 * - `enableRotation`: rotate the crystal and ring via the custom renderer when placed in world.
 * - `rotationSpeedDegPerTick`: rotation speed in degrees per tick.
 * - `renderLayer`: cutout (default), translucent, solid.
 * - `display_only_crystal`: render only the crystal (no ring).
 *
 * ## Dyeing + Palette
 * - `allow_dyable_variants`: allow dyeing by right-click and via the crafting recipe.
 * - `tint_ring_with_dye`: if true, tint the ring too.
 * - `dye_tint_hex_named`: per-dye tint palette (hex #RRGGBB) using named keys.
 * - `dye_tint_hex`: derived 16-entry array (DyeColor id -> hex). Not written to disk.
 *
 * ## Enchanted Pylon
 * - `enchanted_pylon_enable_rotation`: rotate the crystal and ring via the custom renderer when placed in world.
 * - `enchanted_pylon_rotation_speed_deg_per_tick`: rotation speed in degrees per tick.
 * - `enchanted_pylon_render_layer`: cutout (default), translucent, solid.
 * - `enchanted_pylon_display_only_crystal`: render only the crystal (no ring).
 * - `enchanted_pylon_crystal_variant`: tall, short, tallshort, shorttall.
 *
 * # NOTES:
 * - Read by `BotaniaPylonCrystalClient` during client init and for render layer selection.
 * - Read by `mixin/PylonBlockEntityRendererMixin` (override toggles, rotation enable/speed, model variant selection).
 * - Read by `mixin/PylonBlockEntityItemRendererMixin` (override toggles, model variant selection).
 * - Read by `mixin/PylonBlockRenderShapeMixin` (switch render shape routing for overridden pylons).
 */

package corvaeoboro.botania_pylon_crystal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;

import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.world.item.DyeColor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Locale;

public final class BotaniaPylonCrystalConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME_JSON5 = "botania_pylon_crystal.json5";

	private static final DyeColor[] DYE_ORDER = new DyeColor[] {
			DyeColor.WHITE,
			DyeColor.ORANGE,
			DyeColor.MAGENTA,
			DyeColor.LIGHT_BLUE,
			DyeColor.YELLOW,
			DyeColor.LIME,
			DyeColor.PINK,
			DyeColor.GRAY,
			DyeColor.LIGHT_GRAY,
			DyeColor.CYAN,
			DyeColor.PURPLE,
			DyeColor.BLUE,
			DyeColor.BROWN,
			DyeColor.GREEN,
			DyeColor.RED,
			DyeColor.BLACK,
	};

	private static final String[] DYE_KEYS = new String[] {
			"white",
			"orange",
			"magenta",
			"light_blue",
			"yellow",
			"lime",
			"pink",
			"gray",
			"light_gray",
			"cyan",
			"purple",
			"blue",
			"brown",
			"green",
			"red",
			"black",
	};

	private static final String[] DEFAULT_TINTS = new String[] {
			"#FFFFFF",
			"#f87736",
			"#FF00FF",
			"#9AC0CD",
			"#FFFF00",
			"#BFFF00",
			"#FF69B4",
			"#808080",
			"#D3D3D3",
			"#00FFFF",
			"#A020F0",
			"#4c77d4",
			"#8B4513",
			"#00FF00",
			"#FF0000",
			"#252525",
	};

	private static volatile Data INSTANCE;
	private static final Object LOCK = new Object();

	public static Data get() {
		ensureLoaded();
		return INSTANCE;
	}

	public static void reload() {
		synchronized (LOCK) {
			INSTANCE = null;
		}
		ensureLoaded();
	}

	private static void ensureLoaded() {
		if (INSTANCE != null) {
			return;
		}
		synchronized (LOCK) {
			if (INSTANCE != null) {
				return;
			}

			Path configDir = FabricLoader.getInstance().getConfigDir();
			Path path = configDir.resolve(FILE_NAME_JSON5);
			Data data = new Data();
			boolean shouldWrite = !Files.exists(path);
			final boolean allowWriteJson5 = !Files.exists(path);

			if (Files.exists(path)) {
				try (Reader reader = Files.newBufferedReader(path)) {
					JsonReader jsonReader = new JsonReader(reader);
					jsonReader.setLenient(true);
					Data parsed = GSON.fromJson(jsonReader, Data.class);
					if (parsed != null) {
						data = parsed;
					}
				} catch (Exception e) {
					data = new Data();
					shouldWrite = true;
					System.err.println("[BotaniaPylonCrystalConfig] Failed to parse config: " + path);
					e.printStackTrace();
				}
			}

			if (data.overrideManaPylon == null) {
				data.overrideManaPylon = true;
				shouldWrite = true;
			}
			if (data.overrideNaturaPylon == null) {
				data.overrideNaturaPylon = true;
				shouldWrite = true;
			}
			if (data.overrideGaiaPylon == null) {
				data.overrideGaiaPylon = true;
				shouldWrite = true;
			}
			if (data.enableEnchantedPylon == null) {
				data.enableEnchantedPylon = true;
				shouldWrite = true;
			}

			String defaultVariant = "tall";
			String manaVariant = normalizeCrystalVariant(data.manaPylonCrystalVariant, defaultVariant);
			if (!manaVariant.equals(data.manaPylonCrystalVariant)) {
				data.manaPylonCrystalVariant = manaVariant;
				shouldWrite = true;
			}
			String naturaVariant = normalizeCrystalVariant(data.naturaPylonCrystalVariant, defaultVariant);
			if (!naturaVariant.equals(data.naturaPylonCrystalVariant)) {
				data.naturaPylonCrystalVariant = naturaVariant;
				shouldWrite = true;
			}
			String gaiaVariant = normalizeCrystalVariant(data.gaiaPylonCrystalVariant, defaultVariant);
			if (!gaiaVariant.equals(data.gaiaPylonCrystalVariant)) {
				data.gaiaPylonCrystalVariant = gaiaVariant;
				shouldWrite = true;
			}

			String manaRing = normalizeRingModel(data.manaPylonRingModel, "mana");
			if (!manaRing.equals(data.manaPylonRingModel)) {
				data.manaPylonRingModel = manaRing;
				shouldWrite = true;
			}
			String naturaRing = normalizeRingModel(data.naturaPylonRingModel, "natura");
			if (!naturaRing.equals(data.naturaPylonRingModel)) {
				data.naturaPylonRingModel = naturaRing;
				shouldWrite = true;
			}
			String gaiaRing = normalizeRingModel(data.gaiaPylonRingModel, "gaia");
			if (!gaiaRing.equals(data.gaiaPylonRingModel)) {
				data.gaiaPylonRingModel = gaiaRing;
				shouldWrite = true;
			}

			String enchantedVariant = normalizeCrystalVariant(data.enchantedPylonCrystalVariant, defaultVariant);
			if (!enchantedVariant.equals(data.enchantedPylonCrystalVariant)) {
				data.enchantedPylonCrystalVariant = enchantedVariant;
				shouldWrite = true;
			}

			if (data.renderLayer == null || data.renderLayer.trim().isEmpty()) {
				data.renderLayer = "cutout";
				shouldWrite = true;
			}


			if (data.schemaVersion < 19) {
				data.schemaVersion = 19;
				shouldWrite = true;
			}

			String[] defaults = defaultDyeTintHex();
			if (data.dyeTintHex == null || data.dyeTintHex.length != defaults.length) {
				data.dyeTintHex = defaults;
				shouldWrite = true;
			} else {
				for (int i = 0; i < data.dyeTintHex.length; i++) {
					String v = data.dyeTintHex[i];
					if (v == null || v.trim().isEmpty()) {
						data.dyeTintHex[i] = defaults[i];
						shouldWrite = true;
					}
				}
			}

			if (data.dyeTintHexNamed == null) {
				data.dyeTintHexNamed = NamedDyeTintHex.fromArray(data.dyeTintHex);
				shouldWrite = true;
			} else {
				boolean changed = data.dyeTintHexNamed.fillMissingFromArray(data.dyeTintHex);
				data.dyeTintHex = data.dyeTintHexNamed.toArray();
				if (changed) {
					shouldWrite = true;
				}
			}

			INSTANCE = data;
			if (shouldWrite && allowWriteJson5) {
				writeIfMissingOrInvalid(path, data);
			}
		}
	}

	private static void writeIfMissingOrInvalid(Path path, Data data) {
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path)) {
				writer.write(toJson5String(data));
			}
		} catch (IOException ignored) {
		}
	}

	private static String toJson5String(Data data) {
		StringBuilder sb = new StringBuilder(4096);

		sb.append("// Botania Pylon Crystal config \n");
		sb.append("{\n");

		appendKeyValue(sb, "schemaVersion", data.schemaVersion, true);

		appendSectionHeader(sb, "Botania Overrides");
		sb.append("  // If true, override botania:mana_pylon rendering with this addon's pylon crystal model \n");
		appendKeyValue(sb, "override_mana_pylon", data.overrideManaPylon != null && data.overrideManaPylon, true);
		appendKeyValue(sb, "override_natura_pylon", data.overrideNaturaPylon != null && data.overrideNaturaPylon, true);
		appendKeyValue(sb, "override_gaia_pylon", data.overrideGaiaPylon != null && data.overrideGaiaPylon, true);

		appendSectionHeader(sb, "Crystal Variants");
		sb.append("  //Crystal model variants :  tall, short, tallshort, shorttall.\n");
		appendKeyValue(sb, "mana_pylon_crystal_variant", data.manaPylonCrystalVariant, true);
		appendKeyValue(sb, "natura_pylon_crystal_variant", data.naturaPylonCrystalVariant, true);
		appendKeyValue(sb, "gaia_pylon_crystal_variant", data.gaiaPylonCrystalVariant, true);
		appendKeyValue(sb, "mana_pylon_ring_model", data.manaPylonRingModel, true);
		appendKeyValue(sb, "natura_pylon_ring_model", data.naturaPylonRingModel, true);
		appendKeyValue(sb, "gaia_pylon_ring_model", data.gaiaPylonRingModel, true);
		appendKeyValue(sb, "mana_pylon_display_only_crystal", data.manaPylonDisplayOnlyCrystal, true);
		appendKeyValue(sb, "natura_pylon_display_only_crystal", data.naturaPylonDisplayOnlyCrystal, true);
		appendKeyValue(sb, "gaia_pylon_display_only_crystal", data.gaiaPylonDisplayOnlyCrystal, true);
		sb.append("\n");
		
		appendSectionHeader(sb, "Pylon Crystal Rendering");
		appendKeyValueWithComment(sb, "enableRotation", data.enableRotation,
				"If true, the Mana Pylon crystal and ring will rotate via the custom renderer when placed in world");
		appendKeyValueWithComment(sb, "rotationSpeedDegPerTick", data.rotationSpeedDegPerTick,
				"Rotation speed in degrees per tick . Example: 1.0 = 20 degree per sec.");
		appendKeyValueWithComment(sb, "renderLayer", data.renderLayer,
				"Material Render Type : cutout ( default ) , translucent ( has sorting problems ) , solid ( debug ) .");
		appendKeyValueWithComment(sb, "display_only_crystal", data.displayOnlyCrystal,
				"Display Only Crystal : If true, only the crystal is rendered ( no ring ). ");
		sb.append("\n");
		
		appendSectionHeader(sb, "Dyeing + Palette");
		appendKeyValueWithComment(sb, "allow_dyable_variants", data.allowDyableVariants,
				"Include Dyable Pylons :  If true, Mana Pylons can be dyed by right-clicking with a dye and via the crafting recipe.");
		appendKeyValueWithComment(sb, "tint_ring_with_dye", data.tintRingWithDye,
				"Tint Ring : If true, the ring is tinted along with the crystal. If false, only the crystal is tinted (default).");
		sb.append("  // Color Per-dye RGB tint (hex) Format: #RRGGBB using named keys. \n");
		sb.append("  \"dye_tint_hex_named\": {\n");
		NamedDyeTintHex named = data.dyeTintHexNamed != null ? data.dyeTintHexNamed : NamedDyeTintHex.fromArray(data.dyeTintHex);
		for (int i = 0; i < DYE_ORDER.length; i++) {
			appendNamedPaletteEntry(sb, DYE_KEYS[i], named.getByIndex(i), i < (DYE_ORDER.length - 1));
		}
		sb.append("  },\n\n");

		appendSectionHeader(sb, "Enchanted Pylon");
		appendKeyValue(sb, "enchanted_pylon_enable", data.enableEnchantedPylon != null && data.enableEnchantedPylon, true);
		appendKeyValueWithComment(sb, "enchanted_pylon_enable_rotation", data.enchantedPylonEnableRotation,
				"If true, the Enchanted Pylon crystal and ring will rotate via the custom renderer when placed in world");
		appendKeyValueWithComment(sb, "enchanted_pylon_rotation_speed_deg_per_tick", data.enchantedPylonRotationSpeedDegPerTick,
				"Enchanted Pylon rotation speed in degrees per tick. Example:1.0 = 20 degree per sec.");
		appendKeyValueWithComment(sb, "enchanted_pylon_render_layer", data.enchantedPylonRenderLayer,
				"Enchanted Pylon Material Render Type : cutout ( default ) , translucent ( has sorting problems ) , solid ( debug )");
		appendKeyValueWithComment(sb, "enchanted_pylon_display_only_crystal", data.enchantedPylonDisplayOnlyCrystal,
				"Enchanted Pylon Display Only Crystal : If true, only the crystal is rendered for the Enchanted Pylon (no ring).");
		sb.append("  // Crystal model variant for the Enchanted Pylon renderer. :  tall, short, tallshort, shorttall.\n");
		appendKeyValue(sb, "enchanted_pylon_crystal_variant", data.enchantedPylonCrystalVariant, false);

		sb.append("}\n");
		return sb.toString();
	}

	private static void appendSectionHeader(StringBuilder sb, String name) {
		sb.append("  // ").append(name).append("    ------------------------------\n");
	}

	private static void appendKeyValueWithComment(StringBuilder sb, String key, boolean value, String comment) {
		sb.append("  // ").append(comment).append("\n");
		appendKeyValue(sb, key, value, true);
	}

	private static void appendKeyValueWithComment(StringBuilder sb, String key, float value, String comment) {
		sb.append("  // ").append(comment).append("\n");
		appendKeyValue(sb, key, value, true);
	}

	private static void appendKeyValueWithComment(StringBuilder sb, String key, String value, String comment) {
		sb.append("  // ").append(comment).append("\n");
		appendKeyValue(sb, key, value, true);
	}

	private static void appendKeyValueWithComment(StringBuilder sb, String key, Boolean value, String comment) {
		sb.append("  // ").append(comment).append("\n");
		appendKeyValue(sb, key, value != null && value, true);
	}

	private static void appendKeyValue(StringBuilder sb, String key, boolean value, boolean trailingComma) {
		sb.append("  ").append(quote(key)).append(": ").append(value);
		sb.append(trailingComma ? ",\n" : "\n");
	}

	private static void appendKeyValue(StringBuilder sb, String key, int value, boolean trailingComma) {
		sb.append("  ").append(quote(key)).append(": ").append(value);
		sb.append(trailingComma ? ",\n" : "\n");
	}

	private static void appendKeyValue(StringBuilder sb, String key, float value, boolean trailingComma) {
		sb.append("  ").append(quote(key)).append(": ").append(String.format(Locale.ROOT, "%.4f", value).replaceAll("0+$", "").replaceAll("\\.$", ""));
		sb.append(trailingComma ? ",\n" : "\n");
	}

	private static void appendKeyValue(StringBuilder sb, String key, String value, boolean trailingComma) {
		sb.append("  ").append(quote(key)).append(": ").append(quote(value == null ? "" : value));
		sb.append(trailingComma ? ",\n" : "\n");
	}

	private static void appendNamedPaletteEntry(StringBuilder sb, String key, String value, boolean trailingComma) {
		sb.append("    ").append(quote(key)).append(": ").append(quote(value == null ? "" : value));
		sb.append(trailingComma ? ",\n" : "\n");
	}

	private static String quote(String s) {
		if (s == null) {
			return "\"\"";
		}
		StringBuilder out = new StringBuilder(s.length() + 2);
		out.append('"');
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
				case '\\':
					out.append("\\\\");
					break;
				case '"':
					out.append("\\\"");
					break;
				case '\n':
					out.append("\\n");
					break;
				case '\r':
					out.append("\\r");
					break;
				case '\t':
					out.append("\\t");
					break;
				default:
					out.append(c);
					break;
			}
		}
		out.append('"');
		return out.toString();
	}

	public static final class Data {
		public int schemaVersion = 19;
		@SerializedName(value = "enableRotation", alternate = { "rotating", "ROTATING" })
		public boolean enableRotation = true;
		public float rotationSpeedDegPerTick = 1.0F;
		public String renderLayer = "cutout";
		@SerializedName(value = "display_only_crystal", alternate = { "displayOnlyCrystal" })
		public boolean displayOnlyCrystal = false;
		@SerializedName(value = "allow_dyable_variants", alternate = { "allow_dyable_varaints", "allowDyableVariants" })
		public boolean allowDyableVariants = true;
		@SerializedName(value = "tint_ring_with_dye", alternate = { "tintRingWithDye" })
		public boolean tintRingWithDye = false;
		@SerializedName(value = "dye_tint_hex", alternate = { "dyeTintHex" })
		public String[] dyeTintHex = defaultDyeTintHex();
		@SerializedName(value = "dye_tint_hex_named", alternate = { "dyeTintHexNamed" })
		public NamedDyeTintHex dyeTintHexNamed;
		@SerializedName(value = "override_mana_pylon", alternate = { "overrideManaPylon" })
		public Boolean overrideManaPylon = true;
		@SerializedName(value = "override_natura_pylon", alternate = { "overrideNaturaPylon" })
		public Boolean overrideNaturaPylon = true;
		@SerializedName(value = "override_gaia_pylon", alternate = { "overrideGaiaPylon" })
		public Boolean overrideGaiaPylon = true;
		@SerializedName(value = "mana_pylon_crystal_variant", alternate = { "manaPylonCrystalVariant" })
		public String manaPylonCrystalVariant = "tall";
		@SerializedName(value = "natura_pylon_crystal_variant", alternate = { "naturaPylonCrystalVariant" })
		public String naturaPylonCrystalVariant = "tall";
		@SerializedName(value = "gaia_pylon_crystal_variant", alternate = { "gaiaPylonCrystalVariant" })
		public String gaiaPylonCrystalVariant = "tall";
		@SerializedName(value = "mana_pylon_ring_model", alternate = { "manaPylonRingModel" })
		public String manaPylonRingModel = "mana";
		@SerializedName(value = "natura_pylon_ring_model", alternate = { "naturaPylonRingModel" })
		public String naturaPylonRingModel = "natura";
		@SerializedName(value = "gaia_pylon_ring_model", alternate = { "gaiaPylonRingModel" })
		public String gaiaPylonRingModel = "gaia";
		@SerializedName(value = "mana_pylon_display_only_crystal", alternate = { "manaPylonDisplayOnlyCrystal" })
		public boolean manaPylonDisplayOnlyCrystal = false;
		@SerializedName(value = "natura_pylon_display_only_crystal", alternate = { "naturaPylonDisplayOnlyCrystal" })
		public boolean naturaPylonDisplayOnlyCrystal = false;
		@SerializedName(value = "gaia_pylon_display_only_crystal", alternate = { "gaiaPylonDisplayOnlyCrystal" })
		public boolean gaiaPylonDisplayOnlyCrystal = false;
		@SerializedName(value = "enchanted_pylon_enable_rotation", alternate = { "enchantedPylonEnableRotation" })
		public boolean enchantedPylonEnableRotation = true;
		@SerializedName(value = "enchanted_pylon_enable", alternate = { "enableEnchantedPylon" })
		public Boolean enableEnchantedPylon = true;
		@SerializedName(value = "enchanted_pylon_rotation_speed_deg_per_tick", alternate = { "enchantedPylonRotationSpeedDegPerTick" })
		public float enchantedPylonRotationSpeedDegPerTick = 1.0F;
		@SerializedName(value = "enchanted_pylon_render_layer", alternate = { "enchantedPylonRenderLayer" })
		public String enchantedPylonRenderLayer = "cutout";
		@SerializedName(value = "enchanted_pylon_display_only_crystal", alternate = { "enchantedPylonDisplayOnlyCrystal" })
		public boolean enchantedPylonDisplayOnlyCrystal = false;
		@SerializedName(value = "enchanted_pylon_crystal_variant", alternate = { "enchantedPylonCrystalVariant" })
		public String enchantedPylonCrystalVariant = "tall";
	}

	private static String normalizeCrystalVariant(String value, String defaultValue) {
		String v = String.valueOf(value).trim().toLowerCase(Locale.ROOT);
		if (v.equals("tall") || v.equals("short") || v.equals("tallshort") || v.equals("shorttall")) {
			return v;
		}
		return defaultValue;
	}

	private static String normalizeRingModel(String value, String defaultValue) {
		String v = String.valueOf(value).trim().toLowerCase(Locale.ROOT);
		if (v.equals("mana") || v.equals("natura") || v.equals("gaia")) {
			return v;
		}
		return defaultValue;
	}

	public static final class NamedDyeTintHex {
		public String white;
		public String orange;
		public String magenta;
		@SerializedName("light_blue")
		public String lightBlue;
		public String yellow;
		public String lime;
		public String pink;
		public String gray;
		@SerializedName("light_gray")
		public String lightGray;
		public String cyan;
		public String purple;
		public String blue;
		public String brown;
		public String green;
		public String red;
		public String black;

		private static final Field[] INDEX_FIELDS = createIndexFields();

		private static Field[] createIndexFields() {
			try {
				Field[] fields = new Field[] {
						NamedDyeTintHex.class.getDeclaredField("white"),
						NamedDyeTintHex.class.getDeclaredField("orange"),
						NamedDyeTintHex.class.getDeclaredField("magenta"),
						NamedDyeTintHex.class.getDeclaredField("lightBlue"),
						NamedDyeTintHex.class.getDeclaredField("yellow"),
						NamedDyeTintHex.class.getDeclaredField("lime"),
						NamedDyeTintHex.class.getDeclaredField("pink"),
						NamedDyeTintHex.class.getDeclaredField("gray"),
						NamedDyeTintHex.class.getDeclaredField("lightGray"),
						NamedDyeTintHex.class.getDeclaredField("cyan"),
						NamedDyeTintHex.class.getDeclaredField("purple"),
						NamedDyeTintHex.class.getDeclaredField("blue"),
						NamedDyeTintHex.class.getDeclaredField("brown"),
						NamedDyeTintHex.class.getDeclaredField("green"),
						NamedDyeTintHex.class.getDeclaredField("red"),
						NamedDyeTintHex.class.getDeclaredField("black"),
				};
				for (Field f : fields) {
					f.setAccessible(true);
				}
				return fields;
			} catch (Exception e) {
				throw new RuntimeException("Failed to init NamedDyeTintHex palette field mapping", e);
			}
		}

		private String getByIndex(int index) {
			if (index < 0 || index >= INDEX_FIELDS.length) {
				return null;
			}
			try {
				return (String) INDEX_FIELDS[index].get(this);
			} catch (IllegalAccessException e) {
				return null;
			}
		}

		private void setByIndex(int index, String value) {
			if (index < 0 || index >= INDEX_FIELDS.length) {
				return;
			}
			try {
				INDEX_FIELDS[index].set(this, value);
			} catch (IllegalAccessException ignored) {
			}
		}

		public static NamedDyeTintHex fromArray(String[] dyeTintHex) {
			NamedDyeTintHex out = new NamedDyeTintHex();
			out.fillMissingFromArray(dyeTintHex);
			return out;
		}

		public boolean fillMissingFromArray(String[] dyeTintHex) {
			boolean changed = false;
			for (int i = 0; i < DYE_ORDER.length; i++) {
				String v = getByIndex(i);
				if (v == null || v.trim().isEmpty()) {
					DyeColor dyeColor = DYE_ORDER[i];
					int id = dyeColor.getId();
					String fromArray = (dyeTintHex != null && id >= 0 && id < dyeTintHex.length) ? dyeTintHex[id] : null;
					setByIndex(i, (fromArray == null || fromArray.trim().isEmpty()) ? DEFAULT_TINTS[i] : fromArray);
					changed = true;
				}
			}
			return changed;
		}

		public String[] toArray() {
			String[] out = defaultDyeTintHex();
			for (int i = 0; i < DYE_ORDER.length; i++) {
				String v = getByIndex(i);
				if (v != null && !v.trim().isEmpty()) {
					out[DYE_ORDER[i].getId()] = v;
				}
			}
			return out;
		}
	}

	private static String[] defaultDyeTintHex() {
		String[] out = new String[DyeColor.values().length];
		for (int i = 0; i < DYE_ORDER.length; i++) {
			out[DYE_ORDER[i].getId()] = DEFAULT_TINTS[i];
		}
		return out;
	}

	private BotaniaPylonCrystalConfig() {}
}
