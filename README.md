# WeatherEvent 插件

WeatherEvent 是一个 Minecraft 服务器插件，为游戏中的天气系统添加了丰富的效果和互动元素。

## 插件介绍

WeatherEvent 插件通过扩展原版天气系统，为服务器管理员提供了高度可定制的天气效果控制能力。主要特点包括：

- 支持多种天气状态（晴天、雨天、雷暴等）
- 可配置的生物群系特定天气效果
- 支持时间、高度、光照等多种触发条件
- 丰富的效果类型（药水、粒子、音效等）

## 安装方法

1. 将插件jar文件放入服务器的`plugins`文件夹
2. 重启服务器
3. 插件会自动生成配置文件`config.yml`和`effects`文件夹

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
- **连锁效果**：一个效果触发后可能引发其他效果

## 配置指南

### 基本结构

配置文件分为两个主要部分：
- `effects`：定义各种天气效果
- `shared-effects`：定义可重用的共享效果

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
  # 连锁效果（可选）
  chain-effects:
    lightning_strike:
      chance: 0.2
      effect-id: "small_lightning_strike"
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

查看 `config-enhanced.yml` 文件获取完整的示例配置。