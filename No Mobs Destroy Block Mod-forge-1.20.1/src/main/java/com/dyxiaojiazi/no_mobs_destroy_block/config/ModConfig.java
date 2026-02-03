package com.dyxiaojiazi.no_mobs_destroy_block.config;

import com.dyxiaojiazi.no_mobs_destroy_block.NoMobsDestroyBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = NoMobsDestroyBlock.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModConfig {
    public static final Common COMMON;
    public static final ForgeConfigSpec SPEC;

    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = specPair.getLeft();
        SPEC = specPair.getRight();
    }

    public static class Common {
        public final ConfigValue<List<? extends String>> disabledEntities;
        public final ForgeConfigSpec.BooleanValue enableExplosionEffects;
        public final ForgeConfigSpec.BooleanValue preventEnderManPickup;
        public final ForgeConfigSpec.BooleanValue preventWitherBlockDamage;
        public final ForgeConfigSpec.BooleanValue debugLogging;
        public final ForgeConfigSpec.BooleanValue aggressiveEndCrystalDetection;

        private static final List<String> DEFAULT_ENTITIES = Arrays.asList(
                "minecraft:ender_dragon",
                "minecraft:creeper",
                "minecraft:ghast",
                "minecraft:wither",
                "minecraft:ender_crystal",
                "minecraft:enderman"
        );

        public Common(ForgeConfigSpec.Builder builder) {
            builder.comment("No Mobs Destroy Block Configuration")
                    .push("general");

            disabledEntities = builder
                    .comment("List of entity IDs that should be prevented from destroying blocks",
                            "Format: 'modid:entity_id'",
                            "Default: " + DEFAULT_ENTITIES)
                    .defineList("disabled_entities", DEFAULT_ENTITIES,
                            entry -> entry instanceof String && ((String) entry).contains(":"));

            enableExplosionEffects = builder
                    .comment("Whether to enable explosion effects (damage, particles, sounds) while preventing block damage")
                    .define("enable_explosion_effects", true);

            preventEnderManPickup = builder
                    .comment("Whether to prevent Endermen from picking up blocks")
                    .define("prevent_enderman_pickup", true);

            preventWitherBlockDamage = builder
                    .comment("Whether to prevent Wither from damaging blocks with blue skulls and spawn explosion")
                    .define("prevent_wither_block_damage", true);

            debugLogging = builder
                    .comment("Enable debug logging for troubleshooting")
                    .define("debug_logging", false);

            aggressiveEndCrystalDetection = builder
                    .comment("Enable aggressive detection for End Crystal explosions (fixes special cases)",
                            "This may prevent some legitimate explosions but ensures End Crystals never break blocks")
                    .define("aggressive_end_crystal_detection", true);

            builder.pop();
        }
    }

    // 缓存已禁用的实体集合以提高性能
    private static Set<ResourceLocation> disabledEntitySet = new HashSet<>();

    public static boolean isEntityDisabled(ResourceLocation entityId) {
        if (entityId == null) return false;
        return disabledEntitySet.contains(entityId);
    }

    public static boolean shouldPreventEnderManPickup() {
        return COMMON.preventEnderManPickup.get();
    }

    public static boolean shouldPreventWitherBlockDamage() {
        return COMMON.preventWitherBlockDamage.get();
    }

    public static boolean shouldEnableExplosionEffects() {
        return COMMON.enableExplosionEffects.get();
    }

    public static boolean isDebugLoggingEnabled() {
        return COMMON.debugLogging.get();
    }

    public static boolean shouldUseAggressiveEndCrystalDetection() {
        return COMMON.aggressiveEndCrystalDetection.get();
    }

    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SPEC) {
            updateDisabledEntitySet();
            com.dyxiaojiazi.no_mobs_destroy_block.event.ModEvents.clearCache();
            NoMobsDestroyBlock.LOGGER.info("Config reloaded. Disabled entities: {}", disabledEntitySet);
        }
    }

    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) {
            updateDisabledEntitySet();
            NoMobsDestroyBlock.LOGGER.info("Config loaded. Disabled entities: {}", disabledEntitySet);
        }
    }

    private static void updateDisabledEntitySet() {
        disabledEntitySet.clear();
        for (String entityId : COMMON.disabledEntities.get()) {
            try {
                disabledEntitySet.add(ResourceLocation.parse(entityId));
            } catch (Exception e) {
                NoMobsDestroyBlock.LOGGER.warn("Invalid entity ID in config: {}", entityId);
            }
        }
    }
}