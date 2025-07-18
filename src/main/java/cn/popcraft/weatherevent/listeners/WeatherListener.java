package cn.popcraft.weatherevent.listeners;

import cn.popcraft.weatherevent.WeatherEvent;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

/**
 * 天气事件监听器
 * 监听天气变化事件并通知效果管理器
 */
public class WeatherListener implements Listener {
    
    private final WeatherEvent plugin;
    
    public WeatherListener(WeatherEvent plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        FileConfiguration config = plugin.getConfig();
        World world = event.getWorld();
        boolean isRaining = event.toWeatherState();
        
        // 检查是否只监听主世界
        if (config.getBoolean("main-world-only", true)) {
            String mainWorldName = config.getString("main-world-name", "world");
            if (!world.getName().equals(mainWorldName)) {
                return;
            }
        }
        
        // 不再需要显式通知效果管理器，因为它现在自己监听事件
        
        // 检查是否启用了天气变化通知
        if (config.getBoolean("notify-weather-changes", true)) {
            String message;
            if (isRaining) {
                message = ChatColor.translateAlternateColorCodes('&', 
                    config.getString("messages.rain-start", "&9开始下雨了！"));
            } else {
                message = ChatColor.translateAlternateColorCodes('&', 
                    config.getString("messages.rain-stop", "&e雨停了，天气转晴！"));
            }
            
            // 通知所有在该世界的玩家
            for (Player player : world.getPlayers()) {
                player.sendMessage(message);
            }
        }
    }
    
    @EventHandler
    public void onThunderChange(ThunderChangeEvent event) {
        FileConfiguration config = plugin.getConfig();
        World world = event.getWorld();
        boolean isThundering = event.toThunderState();
        
        // 检查是否只监听主世界
        if (config.getBoolean("main-world-only", true)) {
            String mainWorldName = config.getString("main-world-name", "world");
            if (!world.getName().equals(mainWorldName)) {
                return;
            }
        }
        
        // 不再需要显式通知效果管理器，因为它现在自己监听事件
        
        // 检查是否启用了天气变化通知
        if (config.getBoolean("notify-weather-changes", true)) {
            String message;
            if (isThundering) {
                message = ChatColor.translateAlternateColorCodes('&', 
                    config.getString("messages.thunder-start", "&5雷暴开始了，小心闪电！"));
            } else {
                message = ChatColor.translateAlternateColorCodes('&', 
                    config.getString("messages.thunder-stop", "&e雷暴结束了！"));
            }
            
            // 通知所有在该世界的玩家
            for (Player player : world.getPlayers()) {
                player.sendMessage(message);
            }
        }
    }
}