package cn.popcraft.weatherevent;

import cn.popcraft.weatherevent.api.WeatherEventAPI;
import cn.popcraft.weatherevent.api.WeatherEventAPIImpl;
import cn.popcraft.weatherevent.commands.WeatherCommand;
import cn.popcraft.weatherevent.config.SharedEffectManager;
import cn.popcraft.weatherevent.disaster.DisasterManager;
import cn.popcraft.weatherevent.effects.BiomeWeatherManager;
import cn.popcraft.weatherevent.effects.EffectManager;
import cn.popcraft.weatherevent.forecast.WeatherForecastManager;
import cn.popcraft.weatherevent.listeners.PlayerListener;
import cn.popcraft.weatherevent.listeners.WeatherListener;
import cn.popcraft.weatherevent.season.SeasonManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * WeatherEvent 插件主类
 * 提供动态天气效果系统，让天气影响游戏玩法
 */
public class WeatherEvent extends JavaPlugin {
    
    private EffectManager effectManager;
    private SharedEffectManager sharedEffectManager;
    private BiomeWeatherManager biomeWeatherManager;
    private SeasonManager seasonManager;
    private DisasterManager disasterManager;
    private WeatherForecastManager weatherForecastManager;
    private WeatherEventAPIImpl api;
    
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
        
        // 初始化季节管理器
        seasonManager = new SeasonManager(this);
        seasonManager.loadFromConfig(getConfig().getConfigurationSection("seasons"));
        
        // 初始化灾害管理器
        disasterManager = new DisasterManager(this);
        disasterManager.loadFromConfig(getConfig().getConfigurationSection("disasters"));
        
        // 初始化天气预报管理器
        weatherForecastManager = new WeatherForecastManager(this);
        weatherForecastManager.loadFromConfig(getConfig().getConfigurationSection("weather-forecast"));
        
        // 初始化API
        api = new WeatherEventAPIImpl(this);
        
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(effectManager, this);
        getServer().getPluginManager().registerEvents(new WeatherListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(seasonManager, this);
        getServer().getPluginManager().registerEvents(disasterManager, this);
        
        // 注册命令
        getCommand("weather").setExecutor(new WeatherCommand(this));
        
        getLogger().info("WeatherEvent v1.3.0 插件已启用！");
    }
    
    /**
     * 显示ASCII艺术字
     */
    private void displayAsciiArt() {
        getLogger().info("");
        getLogger().info("§b██╗    ██╗§3███████╗ §9 █████╗ §1████████╗§5██╗  ██╗§d███████╗§6██████╗     §e███████╗§a██╗   ██╗§2███████╗§c███╗   ██╗§4████████╗");
        getLogger().info("§b██║    ██║§3██╔════╝§9██╔══██╗§1╚══██╔══╝§5██║  ██║§d██╔════╝§6██╔══██╗    §e██╔════╝§a██║   ██║§2██╔════╝§c████╗  ██║§4╚══██╔══╝");
        getLogger().info("§b██║ █╗ ██║§3█████╗  §9███████║   §1██║   §5███████║§d█████╗  §6██████╔╝    §e█████╗  §a██║   ██║§2█████╗  §c██╔██╗ ██║   §4██║   ");
        getLogger().info("§b██║███╗██║§3██╔══╝  §9██╔══██║   §1██║   §5██╔══██║§d██╔══╝  §6██╔══██╗    §e██╔══╝  §a╚██╗ ██╔╝§2██╔══╝  §c██║╚██╗██║   §4██║   ");
        getLogger().info("§b╚███╔███╔╝§3███████╗§9██║  ██║   §1██║   §5██║  ██║§d███████╗§6██║  ██║    §e███████╗ §a╚████╔╝ §2███████╗§c██║ ╚████║   §4██║   ");
        getLogger().info("§b ╚══╝╚══╝ §3╚══════╝§9╚═╝  ╚═╝   §1╚═╝   §5╚═╝  ╚═╝§d╚══════╝§6╚═╝  ╚═╝    §e╚══════╝  §a╚═══╝  §2╚══════╝§c╚═╝  ╚═══╝   §4╚═╝   ");
        getLogger().info("");
        getLogger().info("§aWeatherEvent §6已启动!");
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
        
        // 重新加载季节系统
        seasonManager.reload();
        
        // 重新加载灾害系统
        disasterManager.reload();
        
        // 重新加载天气预报系统
        weatherForecastManager.reload();
        
        getLogger().info("§a已重新加载天气效果和新系统！");
    }
    
    @Override
    public void onDisable() {
        // 取消注册所有效果
        if (effectManager != null) {
            effectManager.unregisterAllEffects();
        }
        
        // 停止天气预报更新任务
        if (weatherForecastManager != null) {
            weatherForecastManager.stop();
        }
        
        getLogger().info("§cWeatherEvent 插件已禁用！");
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
    
    /**
     * 获取季节管理器
     * @return 季节管理器实例
     */
    public SeasonManager getSeasonManager() {
        return seasonManager;
    }
    
    /**
     * 获取灾害管理器
     * @return 灾害管理器实例
     */
    public DisasterManager getDisasterManager() {
        return disasterManager;
    }
    
    /**
     * 获取天气预报管理器
     * @return 天气预报管理器实例
     */
    public WeatherForecastManager getWeatherForecastManager() {
        return weatherForecastManager;
    }
    
    /**
     * 获取API实例
     * @return API实例
     */
    public WeatherEventAPI getAPI() {
        return api;
    }
}