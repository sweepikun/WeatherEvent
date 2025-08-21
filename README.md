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

## 配置指南

### 基本结构

配置文件分为两个主要部分：
- `effects`：定义各种天气效果
- `shared-effects`：定义可重用的共享效果
- `biome-weather`：定义生物群系特定的天气效果

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