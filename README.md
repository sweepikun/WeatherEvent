# WeatherEvent

WeatherEvent 是一个 Minecraft Bukkit/Spigot 插件，提供动态天气效果系统，让天气影响游戏玩法。

## 功能特点

- **天气效果系统**：不同的天气会给玩家施加不同的效果
  - **雨天**：移动速度减慢
  - **晴天**：获得速度和跳跃提升
  - **雷暴**：有几率受到伤害并获得虚弱效果
- **天气命令**：提供便捷的天气控制命令
- **权限系统**：可以设置不同玩家的权限
- **配置灵活**：所有效果和消息都可以在配置文件中自定义

## 安装说明

1. 下载最新版本的 WeatherEvent.jar 文件
2. 将 JAR 文件放入服务器的 plugins 文件夹中
3. 重启服务器或使用插件管理器加载插件
4. 插件将自动生成默认配置文件

## 配置说明

配置文件位于 `plugins/WeatherEvent/config.yml`，包含以下主要设置：

### 基本设置

```yaml
main-world-only: true        # 是否只在主世界应用天气效果
main-world-name: "world"     # 主世界名称
notify-weather-changes: true # 是否通知玩家天气变化
send-weather-info-on-join: true # 是否在玩家加入时发送天气信息
send-weather-info-on-world-change: true # 是否在玩家切换世界时发送天气信息
```

### 天气效果设置

```yaml
effects:
  # 雨天效果
  rain:
    enabled: true      # 是否启用雨天效果
    slow-level: 0      # 缓慢效果等级 (0 = 缓慢 I, 1 = 缓慢 II, 等)
  
  # 晴天效果
  sunny:
    enabled: true      # 是否启用晴天效果
    bonus-level: 0     # 效果等级 (0 = 效果 I, 1 = 效果 II, 等)
    speed-boost: true  # 是否提供速度提升
    jump-boost: true   # 是否提供跳跃提升
  
  # 雷暴效果
  thunder:
    enabled: true      # 是否启用雷暴效果
    damage-chance: 0.05 # 造成伤害的几率 (0.0-1.0)
    damage-amount: 2.0 # 伤害数值
    apply-weakness: true # 是否施加虚弱效果
```

### 消息设置

```yaml
messages:
  rain-start: "&9开始下雨了！移动速度将会减慢。"
  rain-stop: "&e雨停了，天气转晴！"
  thunder-start: "&5雷暴开始了，小心闪电！"
  thunder-stop: "&e雷暴结束了！"
  weather-info-clear: "当前天气：&e晴朗"
  weather-info-rain: "当前天气：&9下雨"
  weather-info-thunder: "当前天气：&5雷暴"
  command-clear: "&e天气已设置为晴朗！"
  command-rain: "&9天气已设置为下雨！"
  command-thunder: "&5天气已设置为雷暴！"
```

## 命令

| 命令 | 描述 | 权限 |
|------|------|------|
| `/weather clear` | 设置晴朗天气 | `weatherevent.command.clear` |
| `/weather rain` | 设置雨天 | `weatherevent.command.rain` |
| `/weather thunder` | 设置雷暴天气 | `weatherevent.command.thunder` |
| `/weather info` | 查询当前天气状态 | `weatherevent.command.info` |
| `/weather effects` | 查看当前天气效果 | `weatherevent.command.effects` |
| `/weather reload` | 重新加载配置 | `weatherevent.command.reload` |

## 权限

| 权限 | 描述 | 默认 |
|------|------|------|
| `weatherevent.command.clear` | 允许使用 /weather clear 命令 | OP |
| `weatherevent.command.rain` | 允许使用 /weather rain 命令 | OP |
| `weatherevent.command.thunder` | 允许使用 /weather thunder 命令 | OP |
| `weatherevent.command.info` | 允许使用 /weather info 命令 | 所有玩家 |
| `weatherevent.command.effects` | 允许使用 /weather effects 命令 | 所有玩家 |
| `weatherevent.command.reload` | 允许使用 /weather reload 命令 | OP |
| `weatherevent.bypass` | 免疫天气效果 | OP |
| `weatherevent.*` | 允许使用所有 WeatherEvent 功能 | OP |

## 支持与反馈

如有问题或建议，请在 GitHub 上提交 Issue 或 Pull Request。

## 许可证

本插件采用 MIT 许可证。