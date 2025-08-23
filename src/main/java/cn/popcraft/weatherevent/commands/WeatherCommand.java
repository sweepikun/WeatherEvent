package cn.popcraft.weatherevent.commands;

import cn.popcraft.weatherevent.WeatherEvent;
import cn.popcraft.weatherevent.effects.BaseWeatherEffect;
import cn.popcraft.weatherevent.manager.StatisticsManager;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 天气命令处理类
 * 处理天气相关的命令
 */
public class WeatherCommand implements CommandExecutor, TabCompleter {
    
    private final WeatherEvent plugin;
    private final List<String> subCommands = Arrays.asList(
        "clear", "rain", "thunder", "info", "effects", "reload", "debug", "stats"
    );
    
    public WeatherCommand(WeatherEvent plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "clear":
                return handleClearCommand(sender);
            case "rain":
                return handleRainCommand(sender);
            case "thunder":
                return handleThunderCommand(sender);
            case "info":
                return handleInfoCommand(sender);
            case "effects":
                return handleEffectsCommand(sender);
            case "reload":
                return handleReloadCommand(sender);
            case "debug":
                return handleDebugCommand(sender);
            case "stats":
                return handleStatsCommand(sender);
            default:
                sendUsage(sender);
                return true;
        }
    }
    
    private boolean handleClearCommand(CommandSender sender) {
        if (!sender.hasPermission("weatherevent.command.clear")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令！");
            return true;
        }
        
        World world = getTargetWorld(sender);
        if (world == null) {
            return true;
        }
        
        // 设置晴朗天气
        world.setStorm(false);
        world.setThundering(false);
        
        // 发送成功消息
        String message = ChatColor.translateAlternateColorCodes('&', 
            plugin.getConfig().getString("messages.command-clear", "&e天气已设置为晴朗！"));
        sender.sendMessage(message);
        
        return true;
    }
    
    private boolean handleRainCommand(CommandSender sender) {
        if (!sender.hasPermission("weatherevent.command.rain")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令！");
            return true;
        }
        
        World world = getTargetWorld(sender);
        if (world == null) {
            return true;
        }
        
        // 设置雨天
        world.setStorm(true);
        world.setThundering(false);
        
        // 发送成功消息
        String message = ChatColor.translateAlternateColorCodes('&', 
            plugin.getConfig().getString("messages.command-rain", "&9天气已设置为下雨！"));
        sender.sendMessage(message);
        
        return true;
    }
    
    private boolean handleThunderCommand(CommandSender sender) {
        if (!sender.hasPermission("weatherevent.command.thunder")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令！");
            return true;
        }
        
        World world = getTargetWorld(sender);
        if (world == null) {
            return true;
        }
        
        // 设置雷暴天气
        world.setStorm(true);
        world.setThundering(true);
        
        // 发送成功消息
        String message = ChatColor.translateAlternateColorCodes('&', 
            plugin.getConfig().getString("messages.command-thunder", "&5天气已设置为雷暴！"));
        sender.sendMessage(message);
        
        return true;
    }
    
    private boolean handleInfoCommand(CommandSender sender) {
        if (!sender.hasPermission("weatherevent.command.info")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令！");
            return true;
        }
        
        World world = getTargetWorld(sender);
        if (world == null) {
            return true;
        }
        
        // 获取天气信息
        String weatherInfo;
        if (world.isThundering()) {
            weatherInfo = ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfig().getString("messages.weather-info-thunder", "&5当前天气：雷暴"));
        } else if (world.hasStorm()) {
            weatherInfo = ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfig().getString("messages.weather-info-rain", "&9当前天气：下雨"));
        } else {
            weatherInfo = ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfig().getString("messages.weather-info-clear", "&e当前天气：晴朗"));
        }
        
        sender.sendMessage(weatherInfo);
        
        // 如果启用了时间信息，也发送时间信息
        if (plugin.getConfig().getBoolean("send-time-info", true)) {
            long time = world.getTime();
            String timeInfo;
            
            if (time >= 0 && time < 1000) {
                timeInfo = ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfig().getString("messages.time-info-sunrise", "&6当前时间：日出"));
            } else if (time >= 1000 && time < 12000) {
                timeInfo = ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfig().getString("messages.time-info-day", "&e当前时间：白天"));
            } else if (time >= 12000 && time < 13000) {
                timeInfo = ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfig().getString("messages.time-info-sunset", "&6当前时间：日落"));
            } else {
                timeInfo = ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfig().getString("messages.time-info-night", "&1当前时间：夜晚"));
            }
            
            sender.sendMessage(timeInfo);
        }
        
        return true;
    }
    
    private boolean handleEffectsCommand(CommandSender sender) {
        if (!sender.hasPermission("weatherevent.command.effects")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令！");
            return true;
        }
        
        // 显示当前加载的效果
        sender.sendMessage(ChatColor.GOLD + "=== 当前加载的天气效果 ===");
        for (BaseWeatherEffect effect : plugin.getEffectManager().getEffects().values()) {
            sender.sendMessage(ChatColor.GREEN + "- " + effect.getId() + 
                (effect.isEnabled() ? " (已启用)" : " (已禁用)"));
        }
        
        return true;
    }
    
    private boolean handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("weatherevent.command.reload")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令！");
            return true;
        }
        
        // 重新加载配置和效果
        plugin.reloadEffects();
        
        // 发送成功消息
        sender.sendMessage(ChatColor.GREEN + "WeatherEvent 配置和效果已重新加载！");
        
        return true;
    }
    
    private boolean handleDebugCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令！");
            return true;
        }
        
        if (!sender.hasPermission("weatherevent.command.debug")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令！");
            return true;
        }
        
        Player player = (Player) sender;
        sender.sendMessage(ChatColor.GOLD + "=== WeatherEvent 调试信息 ===");
        
        // 显示当前世界信息
        World world = player.getWorld();
        sender.sendMessage(ChatColor.YELLOW + "世界: " + world.getName());
        sender.sendMessage(ChatColor.YELLOW + "天气: " + 
            (world.isThundering() ? "雷暴" : world.hasStorm() ? "下雨" : "晴天"));
        sender.sendMessage(ChatColor.YELLOW + "时间: " + world.getTime());
        
        // 显示玩家所在生物群系（使用缓存）
        org.bukkit.block.Biome biome = plugin.getEffectManager().getBiomeCacheManager().getPlayerBiome(player);
        sender.sendMessage(ChatColor.YELLOW + "生物群系: " + biome.name());
        
        // 显示适用的效果
        sender.sendMessage(ChatColor.GOLD + "适用的天气效果:");
        for (BaseWeatherEffect effect : plugin.getEffectManager().getEffects().values()) {
            if (effect.isApplicable(world)) {
                sender.sendMessage(ChatColor.GREEN + "- " + effect.getId() + 
                    (effect.isEnabled() ? " (已启用)" : " (已禁用)"));
            }
        }
        
        // 显示冷却信息
        sender.sendMessage(ChatColor.GOLD + "冷却中的效果:");
        boolean hasCooldowns = false;
        for (BaseWeatherEffect effect : plugin.getEffectManager().getEffects().values()) {
            if (plugin.getEffectManager().getCooldownManager().isOnCooldown(player, effect.getId())) {
                long remaining = plugin.getEffectManager().getCooldownManager().getRemainingCooldown(player, effect.getId());
                sender.sendMessage(ChatColor.RED + "- " + effect.getId() + " (" + (remaining/1000) + "秒后过期)");
                hasCooldowns = true;
            }
        }
        
        if (!hasCooldowns) {
            sender.sendMessage(ChatColor.GREEN + "没有正在冷却的效果");
        }
        
        return true;
    }
    
    private boolean handleStatsCommand(CommandSender sender) {
        if (!sender.hasPermission("weatherevent.command.stats")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令！");
            return true;
        }
        
        sender.sendMessage(ChatColor.GOLD + "=== WeatherEvent 统计信息 ===");
        
        // 显示效果统计
        Map<String, StatisticsManager.EffectStatistics> effectStats = 
            plugin.getEffectManager().getStatisticsManager().getAllEffectStatistics();
        
        if (effectStats.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "暂无效果触发统计");
        } else {
            sender.sendMessage(ChatColor.GOLD + "效果触发统计:");
            for (StatisticsManager.EffectStatistics stats : effectStats.values()) {
                sender.sendMessage(ChatColor.GREEN + "- " + stats.getEffectId() + 
                    ": 触发" + stats.getTriggerCount() + "次, 成功" + 
                    stats.getSuccessCount() + "次 (成功率: " + 
                    String.format("%.2f", stats.getSuccessRate() * 100) + "%)");
            }
        }
        
        // 显示缓存信息
        int cacheSize = plugin.getEffectManager().getBiomeCacheManager().getCacheSize();
        sender.sendMessage(ChatColor.GOLD + "生物群系缓存大小: " + cacheSize);
        
        return true;
    }

    private World getTargetWorld(CommandSender sender) {
        World world;
        
        if (sender instanceof Player) {
            world = ((Player) sender).getWorld();
        } else {
            // 如果发送者不是玩家，使用主世界
            world = plugin.getServer().getWorld(plugin.getConfig().getString("main-world-name", "world"));
            
            if (world == null) {
                sender.sendMessage(ChatColor.RED + "找不到主世界！请在配置中设置正确的主世界名称。");
                return null;
            }
        }
        
        // 检查是否只在主世界应用
        if (plugin.getConfig().getBoolean("main-world-only", true)) {
            String mainWorldName = plugin.getConfig().getString("main-world-name", "world");
            if (!world.getName().equals(mainWorldName)) {
                sender.sendMessage(ChatColor.RED + "此命令只能在主世界使用！");
                return null;
            }
        }
        
        return world;
    }
    
    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "WeatherEvent 命令使用方法：");
        sender.sendMessage(ChatColor.GOLD + "/weather clear " + ChatColor.WHITE + "- 设置晴朗天气");
        sender.sendMessage(ChatColor.GOLD + "/weather rain " + ChatColor.WHITE + "- 设置雨天");
        sender.sendMessage(ChatColor.GOLD + "/weather thunder " + ChatColor.WHITE + "- 设置雷暴天气");
        sender.sendMessage(ChatColor.GOLD + "/weather info " + ChatColor.WHITE + "- 查询当前天气状态");
        sender.sendMessage(ChatColor.GOLD + "/weather effects " + ChatColor.WHITE + "- 查看当前天气效果");
        sender.sendMessage(ChatColor.GOLD + "/weather reload " + ChatColor.WHITE + "- 重新加载配置");
        sender.sendMessage(ChatColor.GOLD + "/weather debug " + ChatColor.WHITE + "- 显示调试信息");
        sender.sendMessage(ChatColor.GOLD + "/weather stats " + ChatColor.WHITE + "- 显示统计信息");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return subCommands.stream()
                .filter(s -> s.startsWith(input))
                .filter(s -> {
                    // 根据权限过滤子命令
                    switch (s) {
                        case "clear":
                            return sender.hasPermission("weatherevent.command.clear");
                        case "rain":
                            return sender.hasPermission("weatherevent.command.rain");
                        case "thunder":
                            return sender.hasPermission("weatherevent.command.thunder");
                        case "info":
                            return sender.hasPermission("weatherevent.command.info");
                        case "effects":
                            return sender.hasPermission("weatherevent.command.effects");
                        case "reload":
                            return sender.hasPermission("weatherevent.command.reload");
                        case "debug":
                            return sender.hasPermission("weatherevent.command.debug");
                        case "stats":
                            return sender.hasPermission("weatherevent.command.stats");
                        default:
                            return true;
                    }
                })
                .toList();
        }
        return new ArrayList<>();
    }
}