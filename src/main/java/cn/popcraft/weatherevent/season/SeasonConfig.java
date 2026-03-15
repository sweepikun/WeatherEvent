package cn.popcraft.weatherevent.season;

import cn.popcraft.weatherevent.config.DynamicParameter;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

/**
 * 季节配置类
 * 定义特定季节的效果和属性
 */
public class SeasonConfig {
    
    private final Season season;
    private boolean enabled;
    private List<PotionEffect> potionEffects;
    private List<String> commands;
    private double commandChance;
    private String message;
    private double messageChance;
    private String weatherBias; // 天气倾向：rain, clear, thunder
    private double weatherBiasStrength; // 天气倾向强度 (0-1)
    
    /**
     * 创建季节配置
     * @param season 季节
     */
    public SeasonConfig(Season season) {
        this.season = season;
        this.enabled = true;
        this.potionEffects = new ArrayList<>();
        this.commands = new ArrayList<>();
        this.commandChance = 0.0;
        this.message = "";
        this.messageChance = 1.0;
        this.weatherBias = "clear";
        this.weatherBiasStrength = 0.0;
    }
    
    /**
     * 从配置中创建季节配置
     * @param config 配置部分
     * @param season 季节
     * @return 季节配置
     */
    public static SeasonConfig fromConfig(ConfigurationSection config, Season season) {
        SeasonConfig seasonConfig = new SeasonConfig(season);
        
        if (config == null) {
            return seasonConfig;
        }
        
        seasonConfig.enabled = config.getBoolean("enabled", true);
        
        // 加载药水效果
        List<?> potionList = config.getList("potion-effects");
        if (potionList != null) {
            for (Object obj : potionList) {
                if (obj instanceof ConfigurationSection) {
                    ConfigurationSection potionSection = (ConfigurationSection) obj;
                    String type = potionSection.getString("type");
                    Object levelObj = potionSection.get("level");
                    Object durationObj = potionSection.get("duration");
                    
                    PotionEffectType effectType = PotionEffectType.getByName(type);
                    if (effectType != null) {
                        int level = DynamicParameter.parseIntParameter(levelObj);
                        int duration = DynamicParameter.parseIntParameter(durationObj);
                        seasonConfig.potionEffects.add(new PotionEffect(effectType, duration, level));
                    }
                }
            }
        }
        
        // 加载命令
        seasonConfig.commands = config.getStringList("commands");
        seasonConfig.commandChance = config.getDouble("command-chance", 0.1);
        
        // 加载消息
        seasonConfig.message = config.getString("message", "");
        seasonConfig.messageChance = config.getDouble("message-chance", 1.0);
        
        // 加载天气倾向
        seasonConfig.weatherBias = config.getString("weather-bias", "clear");
        seasonConfig.weatherBiasStrength = config.getDouble("weather-bias-strength", 0.0);
        
        return seasonConfig;
    }
    
    /**
     * 对玩家应用季节效果
     * @param player 玩家
     * @param world 世界
     */
    public void applyEffects(Player player, World world) {
        if (!enabled) return;
        
        // 应用药水效果
        for (PotionEffect effect : potionEffects) {
            player.addPotionEffect(effect, true); // force=true覆盖现有效果
        }
        
        // 发送消息
        if (!message.isEmpty() && Math.random() < messageChance) {
            player.sendMessage(season.getColor() + message);
        }
        
        // 执行命令
        if (!commands.isEmpty() && Math.random() < commandChance) {
            for (String command : commands) {
                String processedCommand = command.replace("%player%", player.getName())
                        .replace("%world%", world.getName())
                        .replace("%season%", season.getId());
                player.getServer().dispatchCommand(
                    player.getServer().getConsoleSender(), 
                    processedCommand
                );
            }
        }
    }
    
    /**
     * 移除季节效果
     * @param player 玩家
     */
    public void removeEffects(Player player) {
        for (PotionEffect effect : potionEffects) {
            player.removePotionEffect(effect.getType());
        }
    }
    
    // Getters
    public Season getSeason() {
        return season;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public List<PotionEffect> getPotionEffects() {
        return potionEffects;
    }
    
    public List<String> getCommands() {
        return commands;
    }
    
    public double getCommandChance() {
        return commandChance;
    }
    
    public String getMessage() {
        return message;
    }
    
    public double getMessageChance() {
        return messageChance;
    }
    
    public String getWeatherBias() {
        return weatherBias;
    }
    
    public double getWeatherBiasStrength() {
        return weatherBiasStrength;
    }
}
