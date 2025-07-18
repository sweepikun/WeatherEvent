package cn.popcraft.weatherevent.commands;

import cn.popcraft.weatherevent.WeatherEvent;
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
import java.util.stream.Collectors;

/**
 * 天气命令处理类
 * 处理天气相关的命令
 */
public class WeatherCommand implements CommandExecutor, TabCompleter {
    
    private final WeatherEvent plugin;
    private final List<String> subCommands = Arrays.asList("clear", "rain", "thunder", "info", "effects", "reload");
    
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
        
        // 获取当前天气状态
        boolean isRaining = world.hasStorm();
        boolean isThundering = world.isThundering();
        
        // 发送天气信息
        sender.sendMessage(ChatColor.YELLOW + "世界 " + ChatColor.WHITE + world.getName() + ChatColor.YELLOW + " 的天气状态：");
        
        if (isThundering) {
            sender.sendMessage(ChatColor.DARK_PURPLE + "雷暴");
        } else if (isRaining) {
            sender.sendMessage(ChatColor.BLUE + "下雨");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "晴朗");
        }
        
        return true;
    }
    
    private boolean handleEffectsCommand(CommandSender sender) {
        if (!sender.hasPermission("weatherevent.command.effects")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令！");
            return true;
        }
        
        // 发送效果信息
        sender.sendMessage(ChatColor.YELLOW + "当前注册的效果：");
        
        // 获取所有注册的效果
        plugin.getEffectManager().getEffects().forEach((id, effect) -> {
            ChatColor color;
            
            // 根据效果类型设置颜色
            if (id.contains("rain")) {
                color = ChatColor.BLUE;
            } else if (id.contains("thunder")) {
                color = ChatColor.DARK_PURPLE;
            } else if (id.contains("sunny") || id.contains("day")) {
                color = ChatColor.YELLOW;
            } else if (id.contains("sunset")) {
                color = ChatColor.GOLD;
            } else if (id.contains("night")) {
                color = ChatColor.DARK_BLUE;
            } else {
                color = ChatColor.WHITE;
            }
            
            // 显示效果状态
            String status = effect.isEnabled() ? ChatColor.GREEN + "启用" : ChatColor.RED + "禁用";
            
            // 发送效果信息
            sender.sendMessage(color + effect.getId() + ": " + 
                               ChatColor.WHITE + effect.getDescription() + 
                               ChatColor.GRAY + " [" + status + ChatColor.GRAY + "]");
        });
        
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
                        default:
                            return true;
                    }
                })
                .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
}