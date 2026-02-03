package com.dyxiaojiazi.no_mobs_destroy_block.event;

import com.dyxiaojiazi.no_mobs_destroy_block.NoMobsDestroyBlock;
import com.dyxiaojiazi.no_mobs_destroy_block.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.EnderManAngerEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = NoMobsDestroyBlock.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {
    private static final Logger LOGGER = LogManager.getLogger(NoMobsDestroyBlock.MODID + "-Events");

    // 缓存实体类型检查结果以提高性能
    private static final Map<Class<?>, Boolean> ENTITY_CHECK_CACHE = new HashMap<>();

    // 记录最近被破坏的末影水晶位置（用于修复末影水晶bug）
    private static BlockPos lastDestroyedEndCrystalPos = null;
    private static long lastDestroyedEndCrystalTime = 0;

    /**
     * 处理爆炸开始事件
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onExplosionStart(ExplosionEvent.Start event) {
        if (ModConfig.isDebugLoggingEnabled()) {
            Explosion explosion = event.getExplosion();
            Entity source = explosion.getDirectSourceEntity();
            Entity indirectSource = explosion.getIndirectSourceEntity();

            if (source != null) {
                LOGGER.debug("[ExplosionStart] Direct source: {} at {}",
                        ForgeRegistries.ENTITY_TYPES.getKey(source.getType()),
                        BlockPos.containing(source.getX(), source.getY(), source.getZ()));
            }
            if (indirectSource != null) {
                LOGGER.debug("[ExplosionStart] Indirect source: {} at {}",
                        ForgeRegistries.ENTITY_TYPES.getKey(indirectSource.getType()),
                        BlockPos.containing(indirectSource.getX(), indirectSource.getY(), indirectSource.getZ()));
            }
        }
    }

    /**
     * 处理爆炸破坏方块事件 - 核心处理逻辑
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        Explosion explosion = event.getExplosion();
        Entity source = explosion.getDirectSourceEntity();
        Entity indirectSource = explosion.getIndirectSourceEntity();
        Level level = (Level) event.getLevel();
        BlockPos explosionPos = BlockPos.containing(explosion.getPosition());

        boolean prevented = false;
        String preventionReason = "";

        // 方法1：检查直接源实体
        if (source != null && isEntityDisabled(source)) {
            event.getAffectedBlocks().clear();
            prevented = true;
            preventionReason = "Direct source: " + ForgeRegistries.ENTITY_TYPES.getKey(source.getType());
        }

        // 方法2：检查间接源实体
        if (!prevented && indirectSource != null && isEntityDisabled(indirectSource)) {
            event.getAffectedBlocks().clear();
            prevented = true;
            preventionReason = "Indirect source: " + ForgeRegistries.ENTITY_TYPES.getKey(indirectSource.getType());
        }

        // 方法3：特殊处理 - 恶魂火球
        if (!prevented && source instanceof LargeFireball) {
            Entity owner = ((LargeFireball) source).getOwner();
            if (owner instanceof Ghast && isEntityDisabled(owner)) {
                event.getAffectedBlocks().clear();
                prevented = true;
                preventionReason = "Ghast fireball";
            }
        }

        // 方法4：特殊处理 - 凋灵骷髅头
        if (!prevented && source instanceof WitherSkull) {
            Entity owner = ((WitherSkull) source).getOwner();
            if (owner instanceof WitherBoss && isEntityDisabled(owner)) {
                event.getAffectedBlocks().clear();
                prevented = true;
                preventionReason = "Wither skull";
            }
        }

        // 方法5：终极修复 - 检测任何可能是末影水晶的爆炸
        if (!prevented && isEndCrystalExplosion(level, explosionPos, source, indirectSource)) {
            event.getAffectedBlocks().clear();
            prevented = true;
            preventionReason = "End Crystal explosion (special detection)";
        }

        // 记录日志
        if (prevented && ModConfig.isDebugLoggingEnabled()) {
            LOGGER.info("Prevented block destruction: {} at {}", preventionReason, explosionPos);
        }
    }

    /**
     * 检查是否是末影水晶爆炸（终极修复方法）
     */
    private static boolean isEndCrystalExplosion(Level level, BlockPos explosionPos, Entity directSource, Entity indirectSource) {
        // 1. 检查是否是末影水晶实体
        if (directSource instanceof EndCrystal || indirectSource instanceof EndCrystal) {
            return true;
        }

        // 2. 检查爆炸位置附近是否有末影水晶实体
        AABB searchArea = new AABB(explosionPos).inflate(3.0);
        for (Entity entity : level.getEntities(null, searchArea)) {
            if (entity instanceof EndCrystal) {
                return true;
            }
        }

        // 3. 检查是否是刚刚被破坏的末影水晶位置
        if (lastDestroyedEndCrystalPos != null) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastDestroyedEndCrystalTime < 1000) { // 1秒内
                double distance = explosionPos.distSqr(lastDestroyedEndCrystalPos);
                if (distance < 9.0) { // 3格范围内
                    LOGGER.debug("Detected explosion at recently destroyed End Crystal position: {}", lastDestroyedEndCrystalPos);
                    return true;
                }
            }
        }

        // 4. 检查爆炸位置是否是末地传送门框架或基岩位置（末影水晶常见位置）
        BlockState centerBlock = level.getBlockState(explosionPos);
        BlockState belowBlock = level.getBlockState(explosionPos.below());

        // 末影水晶通常放置在基岩或末地传送门框架上
        if (belowBlock.is(Blocks.BEDROCK) || belowBlock.is(Blocks.END_PORTAL_FRAME)) {
            // 注意：这里我们不再检查END_CRYSTAL方块，因为末影水晶是实体
            // 如果是在末地传送门框架或基岩上爆炸，高度怀疑是末影水晶
            LOGGER.debug("Suspected End Crystal explosion at bedrock/end_portal location: {}", explosionPos);
            return true;
        }

        // 5. 检查爆炸特征 - 末影水晶爆炸有特定大小
        if (explosionPos.getY() > 50) { // 末影水晶通常在高处
            // 检查周围是否有末影水晶的特征方块
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos checkPos = explosionPos.offset(x, 0, z);
                    if (level.getBlockState(checkPos).is(Blocks.OBSIDIAN) ||
                            level.getBlockState(checkPos).is(Blocks.BEDROCK)) {
                        LOGGER.debug("Suspected End Crystal explosion near obsidian/bedrock at: {}", explosionPos);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 处理实体破坏方块事件 - 用于处理末影龙、末影人、凋灵冲撞
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDestroyBlock(net.minecraftforge.event.entity.living.LivingDestroyBlockEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof EnderMan && ModConfig.shouldPreventEnderManPickup()) {
            event.setCanceled(true);
            if (ModConfig.isDebugLoggingEnabled()) {
                LOGGER.debug("Prevented Enderman from taking block at: {}", event.getPos());
            }
            return;
        }

        if (entity instanceof EnderDragon && isEntityDisabled(entity)) {
            event.setCanceled(true);
            if (ModConfig.isDebugLoggingEnabled()) {
                LOGGER.debug("Prevented Ender Dragon from destroying block at: {}", event.getPos());
            }
            return;
        }

        if (entity instanceof WitherBoss && ModConfig.shouldPreventWitherBlockDamage()) {
            event.setCanceled(true);
            if (ModConfig.isDebugLoggingEnabled()) {
                LOGGER.debug("Prevented Wither from destroying block at: {}", event.getPos());
            }
            return;
        }
    }

    /**
     * 处理末影水晶被破坏事件 - 记录位置以修复bug
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEndCrystalDestroyed(net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof EndCrystal) {
            lastDestroyedEndCrystalPos = BlockPos.containing(event.getTarget().getX(),
                    event.getTarget().getY(),
                    event.getTarget().getZ());
            lastDestroyedEndCrystalTime = System.currentTimeMillis();

            if (ModConfig.isDebugLoggingEnabled()) {
                LOGGER.debug("End Crystal destroyed at position: {}", lastDestroyedEndCrystalPos);
            }
        }
    }

    /**
     * 处理方块被破坏事件 - 额外检测可能放置末影水晶的位置
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockBreak(net.minecraftforge.event.level.BlockEvent.BreakEvent event) {
        LevelAccessor level = event.getLevel();
        BlockPos pos = event.getPos();

        // 如果破坏的是基岩或末地传送门框架，记录位置（因为末影水晶通常放在这些方块上）
        if (level instanceof Level) {
            BlockState blockState = ((Level) level).getBlockState(pos);
            if (blockState.is(Blocks.BEDROCK) || blockState.is(Blocks.END_PORTAL_FRAME)) {
                lastDestroyedEndCrystalPos = pos;
                lastDestroyedEndCrystalTime = System.currentTimeMillis();

                if (ModConfig.isDebugLoggingEnabled()) {
                    LOGGER.debug("Block destroyed at possible End Crystal location: {}", pos);
                }
            }
        }
    }

    /**
     * 处理凋灵生成事件
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onWitherSpawn(net.minecraftforge.event.entity.living.MobSpawnEvent.FinalizeSpawn event) {
        Entity entity = event.getEntity();

        if (entity instanceof WitherBoss && isEntityDisabled(entity)) {
            if (ModConfig.isDebugLoggingEnabled()) {
                LOGGER.info("Wither spawned at {}, will prevent its block destruction",
                        BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()));
            }
        }
    }

    /**
     * 处理凋灵破坏方块的特殊事件
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onWitherBreakBlocks(net.minecraftforge.event.level.BlockEvent.BreakEvent event) {
        if (!ModConfig.shouldPreventWitherBlockDamage()) return;

        // 检查是否是凋灵破坏的方块
        if (event.getPlayer() == null && event.getLevel() instanceof Level) {
            Level level = (Level) event.getLevel();
            BlockPos pos = event.getPos();

            // 查找附近的凋灵
            for (Entity entity : level.getEntities(null, new AABB(pos).inflate(10.0))) {
                if (entity instanceof WitherBoss && isEntityDisabled(entity)) {
                    event.setCanceled(true);

                    if (ModConfig.isDebugLoggingEnabled()) {
                        LOGGER.debug("Prevented Wither from breaking block at {} via BreakEvent", pos);
                    }
                    break;
                }
            }
        }
    }

    /**
     * 终极修复：拦截所有爆炸前的处理
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onExplosionPre(net.minecraftforge.event.level.ExplosionEvent event) {
        // 这个事件在爆炸开始前触发，我们可以在这里修改爆炸参数
        if (event instanceof ExplosionEvent.Start) {
            Explosion explosion = ((ExplosionEvent.Start) event).getExplosion();
            Entity source = explosion.getDirectSourceEntity();
            Entity indirectSource = explosion.getIndirectSourceEntity();

            // 如果是末影水晶爆炸，直接修改爆炸参数
            if (source instanceof EndCrystal || indirectSource instanceof EndCrystal) {
                if (ModConfig.isDebugLoggingEnabled()) {
                    LOGGER.debug("Intercepted End Crystal explosion before it starts");
                }
            }
        }
    }

    /**
     * 检查实体是否在禁用列表中
     */
    public static boolean isEntityDisabled(Entity entity) {
        if (entity == null) return false;

        // 检查缓存
        Class<?> entityClass = entity.getClass();
        if (ENTITY_CHECK_CACHE.containsKey(entityClass)) {
            return ENTITY_CHECK_CACHE.get(entityClass);
        }

        // 获取实体注册名并检查配置
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());

        boolean isDisabled = entityId != null && ModConfig.isEntityDisabled(entityId);
        ENTITY_CHECK_CACHE.put(entityClass, isDisabled);

        if (ModConfig.isDebugLoggingEnabled() && isDisabled) {
            LOGGER.debug("Entity {} is in disabled list", entityId);
        }

        return isDisabled;
    }

    /**
     * 清理缓存（用于热重载）
     */
    public static void clearCache() {
        ENTITY_CHECK_CACHE.clear();
        LOGGER.debug("Entity check cache cleared");
    }

    /**
     * 手动触发末影水晶位置记录（用于测试）
     */
    public static void recordEndCrystalPosition(BlockPos pos) {
        lastDestroyedEndCrystalPos = pos;
        lastDestroyedEndCrystalTime = System.currentTimeMillis();
        LOGGER.debug("Manually recorded End Crystal position: {}", pos);
    }
}