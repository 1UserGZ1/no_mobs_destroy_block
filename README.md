模组简介：

阻止游戏中特定生物和实体对环境的破坏行为，同时完全保留这些生物的其他所有特性。简单来说就是精准阻止游戏中的特定生物和实体破坏方块，同时完整保留它们的攻击、移动等其他所有行为。

生物限制名单：


生物/实体	防破坏类型	保留的行为特性
苦力怕	爆炸不破坏方块	保留爆炸伤害、音效、粒子效果、击退、点燃其他实体能力
恶魂	火球爆炸不破坏方块	保留火球伤害、音效、飞行轨迹、火焰点燃效果
凋灵	所有破坏形式均被阻止	保留攻击伤害、生命恢复（调整为不依赖破坏方块）、飞行、音效
末影龙	无法破坏任何方块	保留战斗AI、扑杀攻击、水晶召唤、栖息行为
末影人
无法拾取和移除方块	保留传送能力、愤怒状态、攻击行为、音效
末影水晶	爆炸不破坏任何方块	保留爆炸伤害、治疗末影龙能力、音效

配置文件：可通过 config/no_mobs_destroy_block.toml 文件调整（未实现）

[general]
# 禁用实体列表（可自定义添加/移除）
disabled_entities = [
    "minecraft:ender_dragon",
    "minecraft:creeper",
    "minecraft:ghast", 
    "minecraft:wither",
    "minecraft:ender_crystal",
    "minecraft:enderman"
]

# 是否启用爆炸视觉效果
enable_explosion_effects = true

# 是否阻止末影人拾取方块
prevent_enderman_pickup = true

# 是否阻止凋灵破坏方块
prevent_wither_block_damage = true

# 增强型末影水晶检测（修复特殊bug）
aggressive_end_crystal_detection = true

# 调试日志（故障排查时启用）
debug_logging = false







