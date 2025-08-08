package cn.popcraft.weatherevent.message;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * 消息和音效处理器
 * 直接使用API发送标题、音效等，而不是通过命令
 */
public class MessageHandler {
    
    /**
     * 发送标题给玩家
     * @param player 玩家
     * @param titleConfig 标题配置
     */
    public static void sendTitle(Player player, Map<String, Object> titleConfig) {
        if (titleConfig == null || !titleConfig.containsKey("enabled") || !(boolean) titleConfig.get("enabled")) {
            return;
        }
        
        String title = (String) titleConfig.getOrDefault("title", "");
        String subtitle = (String) titleConfig.getOrDefault("subtitle", "");
        int fadeIn = ((Number) titleConfig.getOrDefault("fade-in", 10)).intValue();
        int stay = ((Number) titleConfig.getOrDefault("stay", 70)).intValue();
        int fadeOut = ((Number) titleConfig.getOrDefault("fade-out", 20)).intValue();
        
        // 替换占位符
        title = replacePlaceholders(title, player);
        subtitle = replacePlaceholders(subtitle, player);
        
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }
    
    /**
     * 发送动作栏消息给玩家
     * @param player 玩家
     * @param actionBarConfig 动作栏配置
     */
    public static void sendActionBar(Player player, Map<String, Object> actionBarConfig) {
        if (actionBarConfig == null || !actionBarConfig.containsKey("enabled") || !(boolean) actionBarConfig.get("enabled")) {
            return;
        }
        
        String message = (String) actionBarConfig.getOrDefault("message", "");
        
        // 替换占位符
        message = replacePlaceholders(message, player);
        
        // 使用Spigot API发送动作栏消息
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, 
                net.md_5.bungee.api.chat.TextComponent.fromLegacyText(message));
    }
    
    /**
     * 播放声音给玩家
     * @param player 玩家
     * @param soundConfig 声音配置
     */
    public static void playSound(Player player, Map<String, Object> soundConfig) {
        if (soundConfig == null || !soundConfig.containsKey("enabled") || !(boolean) soundConfig.get("enabled")) {
            return;
        }
        
        String soundName = (String) soundConfig.getOrDefault("resource", "entity.player.levelup");
        float volume = ((Number) soundConfig.getOrDefault("volume", 1.0f)).floatValue();
        float pitch = ((Number) soundConfig.getOrDefault("pitch", 1.0f)).floatValue();
        
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase().replace(".", "_"));
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            // 如果声音名称无效，尝试使用字符串形式
            player.playSound(player.getLocation(), soundName, volume, pitch);
        }
    }
    
    /**
     * 发送聊天消息给玩家
     * @param player 玩家
     * @param messageConfig 消息配置
     */
    public static void sendMessage(Player player, Map<String, Object> messageConfig) {
        if (messageConfig == null || !messageConfig.containsKey("enabled") || !(boolean) messageConfig.get("enabled")) {
            return;
        }
        
        String message = (String) messageConfig.getOrDefault("text", "");
        
        // 替换占位符
        message = replacePlaceholders(message, player);
        
        player.sendMessage(message);
    }
    
    /**
     * 替换消息中的占位符
     * @param message 原始消息
     * @param player 玩家
     * @return 替换后的消息
     */
    private static String replacePlaceholders(String message, Player player) {
        if (message == null) {
            return "";
        }
        
        return message.replace("%player%", player.getName())
                .replace("%player_name%", player.getName())
                .replace("%player_x%", String.valueOf(player.getLocation().getX()))
                .replace("%player_y%", String.valueOf(player.getLocation().getY()))
                .replace("%player_z%", String.valueOf(player.getLocation().getZ()))
                .replace("%player_health%", String.valueOf(player.getHealth()))
                .replace("%player_food%", String.valueOf(player.getFoodLevel()))
                .replace("%player_world%", player.getWorld().getName());
    }
}