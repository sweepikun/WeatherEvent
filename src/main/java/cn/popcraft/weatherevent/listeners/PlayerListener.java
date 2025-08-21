package cn.popcraft.weatherevent.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import cn.popcraft.weatherevent.WeatherEvent;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * 玩家事件监听器
 * 处理玩家相关的事件，如加入、离开服务器和切换世界
 */
public class PlayerListener implements Listener {
    
    private final WeatherEvent plugin;
    
    public PlayerListener(WeatherEvent plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // 当玩家加入时，立即检查并应用天气效果
        // 效果管理器会在下一个任务周期自动应用效果，这里不需要特别处理
        
        // 如果配置了加入消息，可以在这里发送天气状态信息
        if (plugin.getConfig().getBoolean("send-weather-info-on-join", true)) {
            sendWeatherInfo(player);
        }
        
        // 检查是否下雨并触发打滑效果
        checkRainSlip(player);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // 玩家离开时，效果管理器会自动清理他们的效果记录
        // 不需要特别处理
    }
    
    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        
        // 当玩家切换世界时，立即检查并应用新世界的天气效果
        // 效果管理器会在下一个任务周期自动应用效果，这里不需要特别处理
        
        // 如果配置了世界切换消息，可以在这里发送天气状态信息
        if (plugin.getConfig().getBoolean("send-weather-info-on-world-change", true)) {
            sendWeatherInfo(player);
        }
        
        // 检查是否下雨并触发打滑效果
        checkRainSlip(player);
    }
    
    /**
     * 向玩家发送当前天气和时间状态信息
     * @param player 目标玩家
     */
    /**
     * 检查是否下雨并随机触发打滑效果
     * @param player 目标玩家
     */
    private void checkRainSlip(Player player) {
        // 检查是否在主世界
        if (plugin.getConfig().getBoolean("main-world-only", true) && 
            !player.getWorld().getName().equals(plugin.getConfig().getString("main-world-name", "world"))) {
            return;
        }
        
        // 检查是否下雨
        if (player.getWorld().hasStorm()) {
            // 随机触发打滑效果（20%概率）
            if (Math.random() < 0.2) {
                // 打滑效果：击退玩家并发送提示消息
                player.setVelocity(player.getLocation().getDirection().multiply(-0.5).setY(0.2));
                player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfig().getString("messages.rain-slip", "&c哎呀！下雨天路滑，你摔倒了！")));
                
                // 播放配置的声音
                if (plugin.getConfig().isSet("effects.rain-slip-sound.sound")) {
                    String sound = plugin.getConfig().getString("effects.rain-slip-sound.sound");
                    float volume = (float) plugin.getConfig().getDouble("effects.rain-slip-sound.volume", 1.0);
                    float pitch = (float) plugin.getConfig().getDouble("effects.rain-slip-sound.pitch", 1.0);
                    player.playSound(player.getLocation(), sound, volume, pitch);
                }
            }
        }
    }
    
    private void sendWeatherInfo(Player player) {
        // 检查是否只在主世界发送信息
        if (plugin.getConfig().getBoolean("main-world-only", true) && 
            !player.getWorld().getName().equals(plugin.getConfig().getString("main-world-name", "world"))) {
            return;
        }
        
        // 获取当前天气状态
        boolean isRaining = player.getWorld().hasStorm();
        boolean isThundering = player.getWorld().isThundering();
        
        // 根据天气状态发送相应的消息
        String weatherInfo;
        if (isThundering) {
            weatherInfo = plugin.getConfig().getString("messages.weather-info-thunder", "当前天气：&5雷暴");
        } else if (isRaining) {
            weatherInfo = plugin.getConfig().getString("messages.weather-info-rain", "当前天气：&9下雨");
        } else {
            weatherInfo = plugin.getConfig().getString("messages.weather-info-clear", "当前天气：&e晴朗");
        }
        
        player.sendTitle("", org.bukkit.ChatColor.translateAlternateColorCodes('&', weatherInfo), 10, 70, 20);
        
        // 获取当前时间并发送时间信息
        if (plugin.getConfig().getBoolean("send-time-info", true)) {
            long time = player.getWorld().getTime();
            String timeInfo;
            
            if (time >= 0 && time < 1000) {
                timeInfo = plugin.getConfig().getString("messages.time-info-sunrise", "当前时间：&6日出");
            } else if (time >= 1000 && time < 13000) {
                timeInfo = plugin.getConfig().getString("messages.time-info-day", "当前时间：&e白天");
            } else if (time >= 13000 && time < 14000) {
                timeInfo = plugin.getConfig().getString("messages.time-info-sunset", "当前时间：&6日落");
            } else {
                timeInfo = plugin.getConfig().getString("messages.time-info-night", "当前时间：&1夜晚");
            }
            
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(org.bukkit.ChatColor.translateAlternateColorCodes('&', timeInfo)));
        }
    }
}