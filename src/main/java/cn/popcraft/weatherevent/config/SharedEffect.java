package cn.popcraft.weatherevent.config;

import cn.popcraft.weatherevent.WeatherEvent;
import cn.popcraft.weatherevent.effects.BaseWeatherEffect;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 共享效果类
 * 表示一个可重用的效果集合
 */
public class SharedEffect {
    private final WeatherEvent plugin;
    private final String id;
    private final List<PotionEffect> potionEffects = new ArrayList<>();
    private final Map<String, Object> commands = new HashMap<>();
    private final Map<String, Object> damage = new HashMap<>();
    private final Map<String, Object> sound = new HashMap<>();
    private final Map<String, Object> title = new HashMap<>();
    private final Map<String, Object> actionBar = new HashMap<>();
    private final Map<String, Object> message = new HashMap<>();

    public SharedEffect(WeatherEvent plugin, String id, ConfigurationSection config) {
        this.plugin = plugin;
        this.id = id;
        loadFromConfig(config);
    }

    /**
     * 从配置中加载效果
     * @param config 配置部分
     */
    private void loadFromConfig(ConfigurationSection config) {
        // 加载药水效果
        ConfigurationSection potionSection = config.getConfigurationSection("potion-effects");
        if (potionSection != null) {
            for (String key : potionSection.getKeys(false)) {
                ConfigurationSection effectConfig = potionSection.getConfigurationSection(key);
                if (effectConfig != null) {
                    String type = effectConfig.getString("type");
                    int level = effectConfig.getInt("level", 0);
                    int duration = effectConfig.getInt("duration", 100);
                    
                    PotionEffectType effectType = PotionEffectType.getByName(type);
                    if (effectType != null) {
                        potionEffects.add(new PotionEffect(effectType, duration, level));
                    }
                }
            }
        }

        // 加载命令
        ConfigurationSection commandsSection = config.getConfigurationSection("commands");
        if (commandsSection != null) {
            commands.put("chance", commandsSection.getDouble("chance", 1.0));
            commands.put("list", commandsSection.getStringList("list"));
        }

        // 加载伤害设置
        ConfigurationSection damageSection = config.getConfigurationSection("damage");
        if (damageSection != null) {
            damage.put("enabled", damageSection.getBoolean("enabled", false));
            damage.put("chance", damageSection.getDouble("chance", 0.0));
            damage.put("amount", damageSection.getDouble("amount", 1.0));
        }

        // 加载声音设置
        ConfigurationSection soundSection = config.getConfigurationSection("sound");
        if (soundSection != null) {
            sound.put("enabled", soundSection.getBoolean("enabled", false));
            sound.put("resource", soundSection.getString("resource", ""));
            sound.put("volume", soundSection.getDouble("volume", 1.0));
            sound.put("pitch", soundSection.getDouble("pitch", 1.0));
        }

        // 加载标题设置
        ConfigurationSection titleSection = config.getConfigurationSection("title");
        if (titleSection != null) {
            title.put("enabled", titleSection.getBoolean("enabled", false));
            title.put("text", titleSection.getString("text", ""));
            title.put("subtitle", titleSection.getString("subtitle", ""));
            title.put("color", titleSection.getString("color", "white"));
            title.put("fadeIn", titleSection.getInt("fadeIn", 10));
            title.put("stay", titleSection.getInt("stay", 70));
            title.put("fadeOut", titleSection.getInt("fadeOut", 20));
        }

        // 加载动作栏设置
        ConfigurationSection actionBarSection = config.getConfigurationSection("action-bar");
        if (actionBarSection != null) {
            actionBar.put("enabled", actionBarSection.getBoolean("enabled", false));
            actionBar.put("text", actionBarSection.getString("text", ""));
            actionBar.put("color", actionBarSection.getString("color", "white"));
        }

        // 加载消息设置
        ConfigurationSection messageSection = config.getConfigurationSection("message");
        if (messageSection != null) {
            message.put("enabled", messageSection.getBoolean("enabled", false));
            message.put("text", messageSection.getString("text", ""));
            message.put("color", messageSection.getString("color", "white"));
        }
    }

    /**
     * 应用效果到玩家
     * @param player 目标玩家
     */
    public void apply(Player player) {
        // 应用药水效果
        for (PotionEffect effect : potionEffects) {
            player.addPotionEffect(effect);
        }

        // 应用命令
        if (!commands.isEmpty()) {
            double chance = (double) commands.getOrDefault("chance", 1.0);
            if (Math.random() <= chance) {
                @SuppressWarnings("unchecked")
                List<String> commandList = (List<String>) commands.get("list");
                if (commandList != null) {
                    for (String cmd : commandList) {
                        String processedCmd = cmd.replace("%player%", player.getName());
                        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), processedCmd);
                    }
                }
            }
        }

        // 应用伤害
        if (!damage.isEmpty() && (boolean) damage.getOrDefault("enabled", false)) {
            double chance = (double) damage.getOrDefault("chance", 0.0);
            if (Math.random() <= chance) {
                double amount = (double) damage.getOrDefault("amount", 1.0);
                player.damage(amount);
            }
        }

        // 应用声音
        if (!sound.isEmpty() && (boolean) sound.getOrDefault("enabled", false)) {
            String resource = (String) sound.getOrDefault("resource", "");
            double volume = (double) sound.getOrDefault("volume", 1.0);
            double pitch = (double) sound.getOrDefault("pitch", 1.0);
            
            if (!resource.isEmpty()) {
                player.playSound(player.getLocation(), resource, (float) volume, (float) pitch);
            }
        }

        // 应用标题
        if (!title.isEmpty() && (boolean) title.getOrDefault("enabled", false)) {
            String text = (String) title.getOrDefault("text", "");
            String subtitle = (String) title.getOrDefault("subtitle", "");
            int fadeIn = (int) title.getOrDefault("fadeIn", 10);
            int stay = (int) title.getOrDefault("stay", 70);
            int fadeOut = (int) title.getOrDefault("fadeOut", 20);
            
            player.sendTitle(text, subtitle, fadeIn, stay, fadeOut);
        }

        // 应用动作栏
        if (!actionBar.isEmpty() && (boolean) actionBar.getOrDefault("enabled", false)) {
            String text = (String) actionBar.getOrDefault("text", "");
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text));
        }

        // 应用消息
        if (!message.isEmpty() && (boolean) message.getOrDefault("enabled", false)) {
            String text = (String) message.getOrDefault("text", "");
            player.sendMessage(text);
        }
    }

    public String getId() {
        return id;
    }
}