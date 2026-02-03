# 模组简介
本模组旨在精准阻止《我的世界》中特定生物与实体对环境造成方块破坏行为，同时完整保留这些生物与实体在游戏中的其他所有特性。简单来讲，就是在确保特定生物与实体的攻击、移动等行为不受影响的前提下，杜绝它们对方块的破坏。

## 生物限制名单
|生物/实体|防破坏类型|保留的行为特性|
| ---- | ---- | ---- |
|苦力怕|爆炸不破坏方块|保留爆炸伤害、音效、粒子效果、击退以及点燃其他实体的能力|
|恶魂|火球爆炸不破坏方块|保留火球伤害、音效、飞行轨迹以及火焰点燃效果|
|凋灵|所有破坏形式均被阻止|保留攻击伤害、调整后的生命恢复（不依赖破坏方块）、飞行以及音效|
|末影龙|无法破坏任何方块|保留战斗AI、扑杀攻击、水晶召唤以及栖息行为|
|末影人|无法拾取和移除方块|保留传送能力、愤怒状态、攻击行为以及音效|
|末影水晶|爆炸不破坏任何方块|保留爆炸伤害、治疗末影龙能力以及音效|

## 配置文件
本模组支持通过 `config/no_mobs_destroy_block.toml` 文件进行灵活配置（目前尚未实现）。

```toml
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
```
