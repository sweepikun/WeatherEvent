package cn.popcraft.weatherevent.disaster;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 灾害配置类
 * 定义特定灾害的属性和行为
 */
public class DisasterConfig {
    
    private final DisasterType type;
    private boolean enabled;
    private double chance; // 触发几率
    private int durationSeconds; // 持续时间（秒）
    private int cooldownSeconds; // 冷却时间（秒）
    private double damagePerSecond; // 每秒伤害
    private double radius; // 影响半径
    private List<String> allowedWeathers; // 允许的天气类型
    private List<String> allowedBiomes; // 允许的生物群系
    private boolean nightOnly; // 是否只在夜晚触发
    private List<String> startCommands; // 开始命令
    private List<String> tickCommands; // 每秒命令
    private List<String> endCommands; // 结束命令
    private String warningMessage; // 警告消息
    
    /**
     * 创建灾害配置
     * @param type 灾害类型
     */
    public DisasterConfig(DisasterType type) {
        this.type = type;
        this.enabled = true;
        this.chance = 0.1;
        this.durationSeconds = 60;
        this.cooldownSeconds = 300;
        this.damagePerSecond = 1.0;
        this.radius = 50.0;
        this.allowedWeathers = Arrays.asList("rain", "thunder");
        this.allowedBiomes = new ArrayList<>();
        this.nightOnly = false;
        this.startCommands = new ArrayList<>();
        this.tickCommands = new ArrayList<>();
        this.endCommands = new ArrayList<>();
        this.warningMessage = "";
    }
    
    /**
     * 从配置中创建灾害配置
     * @param config 配置部分
     * @param type 灾害类型
     * @return 灾害配置
     */
    public static DisasterConfig fromConfig(ConfigurationSection config, DisasterType type) {
        DisasterConfig disasterConfig = new DisasterConfig(type);
        
        if (config == null) {
            return disasterConfig;
        }
        
        disasterConfig.enabled = config.getBoolean("enabled", true);
        disasterConfig.chance = config.getDouble("chance", 0.1);
        disasterConfig.durationSeconds = config.getInt("duration-seconds", 60);
        disasterConfig.cooldownSeconds = config.getInt("cooldown-seconds", 300);
        disasterConfig.damagePerSecond = config.getDouble("damage-per-second", 1.0);
        disasterConfig.radius = config.getDouble("radius", 50.0);
        disasterConfig.allowedWeathers = config.getStringList("allowed-weathers");
        disasterConfig.allowedBiomes = config.getStringList("allowed-biomes");
        disasterConfig.nightOnly = config.getBoolean("night-only", false);
        disasterConfig.startCommands = config.getStringList("start-commands");
        disasterConfig.tickCommands = config.getStringList("tick-commands");
        disasterConfig.endCommands = config.getStringList("end-commands");
        disasterConfig.warningMessage = config.getString("warning-message", "");
        
        return disasterConfig;
    }
    
    // Getters
    public DisasterType getType() {
        return type;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public double getChance() {
        return chance;
    }
    
    public int getDurationSeconds() {
        return durationSeconds;
    }
    
    public int getCooldownSeconds() {
        return cooldownSeconds;
    }
    
    public double getDamagePerSecond() {
        return damagePerSecond;
    }
    
    public double getRadius() {
        return radius;
    }
    
    public List<String> getAllowedWeathers() {
        return allowedWeathers;
    }
    
    public List<String> getAllowedBiomes() {
        return allowedBiomes;
    }
    
    public boolean isNightOnly() {
        return nightOnly;
    }
    
    public List<String> getStartCommands() {
        return startCommands;
    }
    
    public List<String> getTickCommands() {
        return tickCommands;
    }
    
    public List<String> getEndCommands() {
        return endCommands;
    }
    
    public String getWarningMessage() {
        return warningMessage;
    }
}
