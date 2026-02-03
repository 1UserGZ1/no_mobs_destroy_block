package com.dyxiaojiazi.no_mobs_destroy_block.util;

import net.minecraft.resources.ResourceLocation;

public class ModConstants {
    public static final String MOD_ID = "no_mobs_destroy_block";

    // 默认禁用实体列表
    // 使用ResourceLocation.parse替代new ResourceLocation
    public static final ResourceLocation[] DEFAULT_DISABLED_ENTITIES = {
            ResourceLocation.parse("minecraft:ender_dragon"),
            ResourceLocation.parse("minecraft:creeper"),
            ResourceLocation.parse("minecraft:ghast"),
            ResourceLocation.parse("minecraft:wither"),
            ResourceLocation.parse("minecraft:ender_crystal"),
            ResourceLocation.parse("minecraft:enderman")
    };

    // 配置相关常量
    public static final String CONFIG_FILE_NAME = MOD_ID + ".toml";
    public static final String CONFIG_CATEGORY_GENERAL = "general";

    // 事件相关常量
    public static final String EVENT_CHANNEL = MOD_ID + ":events";

    // 本地化键
    public static final class TranslationKeys {
        public static final String CONFIG_TITLE = "config." + MOD_ID + ".title";
        public static final String CONFIG_DISABLED_ENTITIES = "config." + MOD_ID + ".disabled_entities";
        public static final String CONFIG_DISABLED_ENTITIES_TOOLTIP = "config." + MOD_ID + ".disabled_entities.tooltip";
        public static final String CONFIG_EXPLOSION_EFFECTS = "config." + MOD_ID + ".explosion_effects";
        public static final String CONFIG_EXPLOSION_EFFECTS_TOOLTIP = "config." + MOD_ID + ".explosion_effects.tooltip";
        public static final String CONFIG_ENDERMAN_PICKUP = "config." + MOD_ID + ".enderman_pickup";
        public static final String CONFIG_ENDERMAN_PICKUP_TOOLTIP = "config." + MOD_ID + ".enderman_pickup.tooltip";
        public static final String CONFIG_WITHER_DAMAGE = "config." + MOD_ID + ".wither_damage";
        public static final String CONFIG_WITHER_DAMAGE_TOOLTIP = "config." + MOD_ID + ".wither_damage.tooltip";
    }

    // 网络包ID
    public static final int NETWORK_PROTOCOL_VERSION = 1;

    // 调试设置
    public static final boolean DEBUG_MODE = false;

    // 性能优化设置
    public static final int ENTITY_CACHE_SIZE = 100;
    public static final long CACHE_CLEANUP_INTERVAL = 60000; // 毫秒

    private ModConstants() {
        // 防止实例化
    }
}