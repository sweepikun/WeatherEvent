# WeatherEvent 插件

WeatherEvent 是一个 Minecraft 服务器插件，为游戏中的天气系统添加了丰富的效果和互动元素。

## 插件介绍

WeatherEvent 插件通过扩展原版天气系统，为服务器管理员提供了高度可定制的天气效果控制能力。主要特点包括：

- 支持多种天气状态（晴天、雨天、雷暴等）
- 可配置的生物群系特定天气效果
- 支持时间、高度、光照等多种触发条件
- 丰富的效果类型（药水、粒子、音效等）
- 增强的连锁效果系统，支持延迟触发和复杂条件
- 支持为不同生物群系添加独特天气类型（如沙漠中的沙尘暴）
- 基于特殊节日、月相和玩家在线时长的动态效果
- 支持基于权限组、世界和区域的差异化配置
- 效果冷却系统，防止过于频繁触发
- 支持Minecraft 1.21新增的Pale Garden（苍白花园）生物群系

## 安装方法

1. 将插件jar文件放入服务器的`plugins`文件夹
2. 重启服务器
3. 插件会自动生成配置文件`config.yml`和`weatherevent`文件夹

## 基础命令

- `/we reload` - 重载插件配置
- `/we list` - 列出所有已加载的天气效果
- `/we info <效果名>` - 查看特定效果的详细信息

## 功能特点

- **天气效果系统**：不同天气状态下触发不同的游戏效果
- **灵活的配置**：支持多种触发条件和效果组合
- **药水效果**：在特定天气下给予玩家药水效果
- **随机效果**：随机触发各种效果，增加游戏的不确定性
- **命令执行**：在特定条件下执行自定义命令
- **消息和标题**：通过标题、动作栏和聊天消息提示玩家
- **声音效果**：播放与天气相关的音效
- **连锁效果**：一个效果触发后可能引发其他效果，支持延迟触发和复杂条件
- **生物群系天气效果**：为不同生物群系提供独特的天气类型和动态效果
- **时间相关效果**：基于特殊节日、月相和玩家在线时长的动态效果
- **权限组支持**：为不同玩家组提供不同的效果配置
- **冷却系统**：防止效果过于频繁触发
- **世界和区域配置**：支持基于世界或区域的效果配置

## 配置指南

### 基本结构

配置文件分为几个主要部分：
- `effects`：定义各种天气效果
- `shared-effects`：定义可重用的共享效果
- `biome-weather`：定义生物群系特定的天气效果
- `time-effects`：定义时间相关的特殊效果
- `permissions`：定义权限组配置
- `worlds`：定义世界配置
- `regions`：定义区域配置

### 触发条件

可以为每种天气效果设置以下触发条件：

1. **生物群系限制**：
```yaml
biomes:
  - plains
  - forest
  - taiga
```

2. **高度限制**：
```yaml
min-height: 60
max-height: 256
```

3. **光照限制**：
```yaml
min-light: 0
max-light: 10
```

### 效果类型

1. **药水效果**：
```yaml
potion-effects:
  - type: SPEED
    level: 0  # 或 [0, 1] 表示随机范围
    duration: 200  # 或 [100, 200] 表示随机范围
```

2. **随机效果**：
```yaml
random-effects:
  chance: 0.15  # 触发几率
  # 前置条件（可选）
  prerequisites:
    type: has_potion_effect
    effect_type: SLOW
    level: 0
  effects:
    - type: REGENERATION
      level: 0
      duration: 100
```

3. **标题显示**：
```yaml
title:
  enabled: true
  text: "§e阳光明媚"
  subtitle: "§7享受温暖的阳光吧，%player_name%！"
  fadeIn: 10
  stay: 70
  fadeOut: 20
  chance: 0.3  # 显示几率
```

4. **动作栏消息**：
```yaml
action-bar:
  enabled: true
  text: "§b雨水淋湿了你"
  chance: 0.4
```

5. **声音效果**：
```yaml
sound:
  enabled: true
  resource: "entity.experience_orb.pickup"
  volume: 1.0
  pitch: 1.0
  chance: 0.5
```

6. **聊天消息**：
```yaml
message:
  enabled: true
  text: "§8一道闪电划过天空！"
  chance: 0.3
```

7. **命令执行**：
```yaml
commands:
  chance: 0.05
  list:
    - "title %player% title {\"text\":\"雷电交加\",\"color\":\"dark_purple\"}"
```

### 连锁效果

增强的连锁效果系统支持延迟触发和复杂条件：

1. **基本连锁效果**：
```yaml
commands:
  chance: 0.1
  list:
    - "title %player% actionbar {\"text\":\"触发连锁效果\",\"color\":\"green\"}"
  chain-effects:
    # 简单连锁效果
    bonus_effect:
      chance: 0.2
      effect-id: "healing_effect"
```

2. **带延迟的连锁效果**：
```yaml
chain-effects:
  delayed_effect:
    chance: 0.3
    effect-id: "strong_healing_effect"
    delay: 40  # 延迟2秒触发（20 ticks = 1秒）
```

3. **带条件的连锁效果**：
```yaml
chain-effects:
  conditional_effect:
    chance: 0.25
    effect-id: "night_vision_effect"
    conditions:
      type: "light_level"
      min: 0
      max: 5
```

4. **复杂条件示例**：
```yaml
chain-effects:
  # 只在玩家有速度效果时触发
  speed_chain:
    chance: 0.15
    effect-id: "speed_bonus"
    conditions:
      type: "has_potion_effect"
      effect_type: "SPEED"
  
  # 只在雨天触发
  rain_chain:
    chance: 0.2
    effect-id: "slip_effect"
    conditions:
      type: "weather"
      weather: "rain"
  
  # 随机触发
  random_chain:
    chance: 0.1
    effect-id: "lucky_effect"
    conditions:
      type: "random"
      chance: 0.5
```

### 生物群系天气效果

增强的生物群系天气系统支持为不同生物群系定义独特的天气类型和动态效果：

1. **基本结构**：
```yaml
biome-weather:
  enabled: true
  update-interval: 20
  biomes:
    # 为特定生物群系定义天气效果
    desert:
      weather:
        # 定义晴天时的特殊效果
        clear:
          enabled: true
          chance: 0.1  # 10% 几率触发
          # 可以定义特殊效果类型
          effects:
            - type: sandstorm
              duration: [200, 400]  # 持续时间10-20秒
```

2. **特殊效果类型**：
```yaml
# 沙漠中的沙尘暴效果
desert:
  weather:
    clear:
      enabled: true
      chance: 0.1
      effects:
        - type: sandstorm
          duration: [200, 400]

# 雪原中的暴风雪效果
snowy_plains:
  weather:
    rain:
      enabled: true
      chance: 0.15
      effects:
        - type: blizzard
          duration: [300, 600]
          
# 苍白花园中的神秘凋零效果 (Minecraft 1.21新增)
pale_garden:
  weather:
    any:
      enabled: true
      chance: 0.1
      potion-effects:
        - type: WITHER
          level: 0
          duration: 100
      message:
        enabled: true
        text: "§8苍白花园中弥漫着不祥的气息..."
        chance: 0.7
```

3. **时间相关效果**：
```yaml
# 夜晚时的特殊效果
swamp:
  weather:
    night:
      enabled: true
      chance: 0.1
      potion-effects:
        - type: POISON
          level: 0
          duration: 100
      message:
        enabled: true
        text: "§2沼泽中升起毒雾"
        chance: 0.7
```

4. **任意条件效果**：
```yaml
# 适用于任何天气的效果
nether_wastes:
  weather:
    any:
      enabled: true
      chance: 0.05
      damage:
        enabled: true
        chance: 0.3
        amount: [1.0, 3.0]
```

### 时间相关效果

增强的时间效果系统支持基于特殊节日、月相和玩家在线时长的动态效果：

1. **特殊节日效果**：
```yaml
time-effects:
  special-events:
    # 春节效果
    spring_festival:
      enabled: true
      months: [2]  # 2月
      days: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]  # 2月1日到15日
      effect-id: "spring_festival_effect"
      
    # 万圣节效果
    halloween:
      enabled: true
      months: [10]  # 10月
      days: [31]  # 10月31日
      effect-id: "halloween_effect"
```

2. **月相效果**：
```yaml
time-effects:
  moon-phases:
    full_moon:
      enabled: true
      effect-id: "full_moon_effect"
    new_moon:
      enabled: true
      effect-id: "new_moon_effect"
```

3. **玩家在线时长效果**：
```yaml
time-effects:
  playtime:
    veteran:  # 60分钟以上
      enabled: true
      minutes: 60
      effect-id: "veteran_player_effect"
```

### 权限组配置

支持为不同权限组配置不同的效果：

```yaml
permissions:
  groups:
    # 默认组配置
    default:
      # 是否免疫所有效果
      bypass-effects: false
      # 效果强度乘数
      effect-multiplier: 1.0
      # 特定效果的启用/禁用状态
      effects:
        rain_slow: true
        thunder_damage: true
        sunny_bonus: true
    
    # VIP组配置
    vip:
      bypass-effects: false
      effect-multiplier: 1.5
      effects:
        rain_slow: false  # VIP玩家免疫雨天减速效果
        thunder_damage: true
        sunny_bonus: true
    
    # 管理员组配置
    admin:
      bypass-effects: true  # 管理员免疫所有效果
      effect-multiplier: 1.0
```

### 世界配置

支持为不同世界配置不同的效果：

```yaml
worlds:
  # 主世界配置
  world:
    # 是否启用天气效果
    enabled: true
    # 效果强度乘数
    effect-multiplier: 1.0
    # 特定效果的启用/禁用状态
    effects:
      rain_slow: true
      thunder_damage: true
      sunny_bonus: true
  
  # 下界配置
  world_nether:
    enabled: true
    effect-multiplier: 1.5  # 下界效果更强
    effects:
      rain_slow: false  # 下界没有雨天效果
      thunder_damage: false  # 下界没有雷暴效果
      sunny_bonus: true
```

### 区域配置

支持为不同区域配置不同的效果：

```yaml
regions:
  # 安全区配置
  safe_zone:
    # 世界名称（可选）
    world: "world"
    # 区域范围
    area:
      min-x: -100
      min-y: 0
      min-z: -100
      max-x: 100
      max-y: 256
      max-z: 100
    # 效果强度乘数
    effect-multiplier: 0.5  # 安全区效果减半
    # 特定效果的启用/禁用状态
    effects:
      rain_slow: false  # 安全区禁用雨天减速效果
      thunder_damage: false  # 安全区禁用雷暴伤害效果
      sunny_bonus: true
```

### 冷却系统

支持为效果设置冷却时间，防止过于频繁触发：

```yaml
effects:
  clear:
    # 冷却时间（秒），防止过于频繁触发
    cooldown: 30
    # ... 其他配置
```

### 占位符

在消息、标题和命令中可以使用以下占位符：

- `%player%` 或 `%player_name%`：玩家名称
- `%player_x%`：玩家 X 坐标
- `%player_y%`：玩家 Y 坐标
- `%player_z%`：玩家 Z 坐标
- `%player_health%`：玩家生命值
- `%player_food%`：玩家饥饿值
- `%player_world%`：玩家所在世界名称

## 命令

- `/weather reload`：重新加载配置文件
- `/weather enable <effect>`：启用指定效果
- `/weather disable <effect>`：禁用指定效果
- `/weather list`：列出所有可用效果

## 示例配置

查看 `config.yml` 文件获取完整的示例配置。