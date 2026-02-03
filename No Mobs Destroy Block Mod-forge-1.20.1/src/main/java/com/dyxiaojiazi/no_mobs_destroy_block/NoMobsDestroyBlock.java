package com.dyxiaojiazi.no_mobs_destroy_block;

import com.dyxiaojiazi.no_mobs_destroy_block.config.ModConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(NoMobsDestroyBlock.MODID)
public class NoMobsDestroyBlock {
    public static final String MODID = "no_mobs_destroy_block";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public NoMobsDestroyBlock() {
        LOGGER.info("================================");
        LOGGER.info("Initializing No Mobs Destroy Block Mod");
        LOGGER.info("================================");

        // 注册配置
        ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.SPEC, MODID + ".toml");

        // 获取事件总线
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

        // 注意：我们不再手动注册ModEvents，因为使用了@EventBusSubscriber注解
        // 但我们可以注册其他需要手动注册的事件处理器

        LOGGER.info("No Mobs Destroy Block Mod initialized successfully");
        LOGGER.info("Disabled entities from config: {}", ModConfig.COMMON.disabledEntities.get());
        LOGGER.info("================================");
    }
}