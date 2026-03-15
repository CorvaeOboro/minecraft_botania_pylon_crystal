<img src="https://github.com/CorvaeOboro/minecraft_botania_pylon_crystal/raw/main/docs/minecraft_botania_pylon_crystal_title_small.png?raw=true"/>

# Botania Pylon Crystal
a Minecraft mod that replaces [Botania](https://github.com/VazkiiMods/Botania) pylon visuals with crystals
- Available for Minecraft 1.20.x with Fabric 

| <a href="https://github.com/CorvaeOboro/minecraft_botania_pylon_crystal/raw/main/docs/minecraft_botania_pylon_crystal_thumb.png?raw=true"><img src="https://github.com/CorvaeOboro/minecraft_botania_pylon_crystal/raw/main/docs/minecraft_botania_pylon_crystal_thumb.png?raw=true" height="50"/></a> | <a href="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/fabric_vector.svg"><img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/fabric_vector.svg" height="50"/></a> | <a href="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg"><img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg" height="50"/></a> | <a href="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/curseforge_vector.svg"><img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/curseforge_vector.svg" height="50"/></a> |
| :---: | :---: | :---: | :---: |

<insert image of the animated webp  image here>

# FEATURES
- dyeable crystals , right click with dye 

<img src="https://github.com/CorvaeOboro/minecraft_botania_pylon_crystal/raw/main/docs/minecraft_botania_pylon_crystal_dyed.png?raw=true"/>

- built-in resourcepack of two part model .json files , with client-side rotation
- editable via resourcepack ( model and textures opensource )
- compatible with shaders  
- model compatible with previous versions of minecraft ( single axis limited rotation )

# CONFIG OPTIONS
<img src="https://github.com/CorvaeOboro/minecraft_botania_pylon_crystal/raw/main/docs/minecraft_botania_pylon_crystal_config_options.png?raw=true"/>

 Botania Overrides
- `override_mana_pylon`: replaces the MANA pylon rendering with crystal model. ( true )
- `override_natura_pylon`: replaces the NATURA pylon rendering with crystal model. ( true )
- `override_gaia_pylon`: replaces the GAIA pylon rendering with crystal model. ( true )

 Crystal Shape Bipyramid Variants
- `mana_pylon_crystal_variant`: tall, short, tallshort, shorttall. ( tall )
- `natura_pylon_crystal_variant`: tall, short, tallshort, shorttall. ( tall )
- `gaia_pylon_crystal_variant`: tall, short, tallshort, shorttall. ( tall )

 Pylon Crystal Rendering
- `enableRotation`: rotate the crystal and ring when placed in world. ( true )
- `rotationSpeedDegPerTick`: rotation speed in degrees per tick. ( 1.0 )
- `renderLayer`: cutout (default), translucent, solid.
- `display_only_crystal`: render only the crystal , no ring. ( false )

 Dyeing + Palette
- `allow_dyable_variants`: allow dyeing by right-click and via the crafting recipe. ( true ) 
- `tint_ring_with_dye`: if true, tint the ring too. ( false )
- `dye_tint_hex_named`: per-dye tint palette (hex #RRGGBB) using named keys.

Enchanted Pylon - decorative 
- `enchanted_pylon_enable_rotation`: rotate the crystal and ring when placed in world. ( true )
- `enchanted_pylon_rotation_speed_deg_per_tick`: rotation speed  ( 1.0 )
- `enchanted_pylon_render_layer`: cutout (default), translucent, solid.
- `enchanted_pylon_display_only_crystal`: render only the crystal , no ring. ( false )
- `enchanted_pylon_crystal_variant`: tall, short, tallshort, shorttall. ( tall )
 

# THANKS
- Major thanks to [Vazkii](https://github.com/Vazkii) for creating [Botania](https://github.com/VazkiiMods/Botania)

# LICENSE
free to all , free to modify ,  [creative commons zero CC0 1.0](https://creativecommons.org/publicdomain/zero/1.0/) , free to re-distribute , attribution not needed . 
feel free to integrate into your own projects , mods , and texture packs 