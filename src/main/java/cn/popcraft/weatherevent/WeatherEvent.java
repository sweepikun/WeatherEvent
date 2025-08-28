package cn.popcraft.weatherevent;

import cn.popcraft.weatherevent.commands.WeatherCommand;
import cn.popcraft.weatherevent.config.SharedEffectManager;
import cn.popcraft.weatherevent.effects.EffectManager;
import cn.popcraft.weatherevent.listeners.PlayerListener;
import cn.popcraft.weatherevent.listeners.WeatherListener;
import cn.popcraft.weatherevent.effects.BiomeWeatherManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * WeatherEvent 插件主类
 * 提供动态天气效果系统，让天气影响游戏玩法
 */
public class WeatherEvent extends JavaPlugin {
    
    private EffectManager effectManager;
    private SharedEffectManager sharedEffectManager;
    private BiomeWeatherManager biomeWeatherManager;
    
    @Override
    public void onEnable() {
        // 保存默认配置
        saveDefaultConfig();
        
        // 显示ASCII艺术字
        displayAsciiArt();
        
        // 初始化共享效果管理器
        sharedEffectManager = new SharedEffectManager(this);
        
        // 初始化生物群系天气管理器
        biomeWeatherManager = new BiomeWeatherManager(this);
        biomeWeatherManager.loadFromConfig(getConfig().getConfigurationSection("biomes"));
        
        // 初始化效果管理器
        effectManager = new EffectManager(this, biomeWeatherManager);
        
        // 加载效果
        effectManager.loadEffects();
        
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(effectManager, this);
        getServer().getPluginManager().registerEvents(new WeatherListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        // 注册命令
        getCommand("weather").setExecutor(new WeatherCommand(this));
        
        getLogger().info("WeatherEvent 插件已启用！");
    }
    
    /**
     * 显示ASCII艺术字
     */
    private void displayAsciiArt() {
        getLogger().info("");
        getLogger().info("██╗    ██╗███████╗ █████╗ ████████╗██╗  ██╗███████╗██████╗     ███████╗██╗   ██╗███████╗███╗   ██╗████████╗");
        getLogger().info("██║    ██║██╔════╝██╔══██╗╚══██╔══╝██║  ██║██╔════╝██╔══██╗    ██╔════╝██║   ██║██╔════╝████╗  ██║╚══██╔══╝");
        getLogger().info("██║ █╗ ██║█████╗  ███████║   ██║   ███████║█████╗  ██████╔╝    █████╗  ██║   ██║█████╗  ██╔██╗ ██║   ██║   ");
        getLogger().info("██║███╗██║██╔══╝  ██╔══██║   ██║   ██╔══██║██╔══╝  ██╔══██╗    ██╔══╝  ╚██╗ ██╔╝██╔══╝  ██║╚██╗██║   ██║   ");
        getLogger().info("╚███╔███╔╝███████╗██║  ██║   ██║   ██║  ██║███████╗██║  ██║    ███████╗ ╚████╔╝ ███████╗██║ ╚████║   ██║   ");
        getLogger().info(" ╚══╝╚══╝ ╚══════╝╚═╝  ╚═╝   ╚═╝   ╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝    ╚══════╝  ╚═══╝  ╚══════╝╚═╝  ╚═══╝   ╚═╝   ");
        getLogger().info("");
        getLogger().info("WeatherEvent 已启动!");
        getLogger().info("");
    }
    
    /**
     * 重新加载效果
     * 用于重新加载配置后更新效果
     */
    public void reloadEffects() {
        // 重新加载配置
        reloadConfig();
        
        // 重新初始化共享效果管理器
        sharedEffectManager = new SharedEffectManager(this);
        
        // 重新加载生物群系天气配置
        biomeWeatherManager.loadFromConfig(getConfig().getConfigurationSection("biomes"));
        
        // 重新加载效果
        effectManager.loadEffects();
        
        getLogger().info("已重新加载天气效果！");
    }
    
    @Override
    public void onDisable() {
        // 取消注册所有效果
        if (effectManager != null) {
            effectManager.unregisterAllEffects();
        }
        
        getLogger().info("WeatherEvent 插件已禁用！");
    }
    
    /**
     * 获取效果管理器
     * @return 效果管理器实例
     */
    public EffectManager getEffectManager() {
        return effectManager;
    }
    
    /**
     * 获取共享效果管理器
     * @return 共享效果管理器实例
     */
    public SharedEffectManager getSharedEffectManager() {
        return sharedEffectManager;
    }
    
    /**
     * 获取生物群系天气管理器
     * @return 生物群系天气管理器实例
     */
    public BiomeWeatherManager getBiomeWeatherManager() {
        return biomeWeatherManager;
    }
}