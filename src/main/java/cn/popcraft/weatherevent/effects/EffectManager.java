package cn.popcraft.weatherevent.effects;

import cn.popcraft.weatherevent.WeatherEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * 效果管理器
 * 负责加载、注册和应用各种天气和时间效果
 */
public class EffectManager implements Listener {
    
    private final WeatherEvent plugin;
    private final Map<String, BaseEffect> effects;
    private BukkitTask effectTask;
    private int updateInterval;
    
    /**
     * 创建一个效果管理器
     * @param plugin 插件实例
     */
    public EffectManager(WeatherEvent plugin) {
        this.plugin = plugin;
        this.effects = new HashMap<>();
        this.updateInterval = 20; // 默认每秒更新一次
    }
    
    /**
     * 从配置中加载所有效果
     */
    public void loadEffects() {
        // 清除现有效果
        unregisterAllEffects();
        
        // 获取配置
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("effects");
        if (config == null) {
            plugin.getLogger().warning("配置中未找到效果部分，使用默认效果");
            registerDefaultEffects();
            return;
        }
        
        // 设置更新间隔
        updateInterval = plugin.getConfig().getInt("update-interval", 20);
        
        // 加载天气效果
        loadWeatherEffects(config);
        
        // 加载时间效果
        loadTimeEffects(config);
        
        // 启动效果更新任务
        startEffectTask();
        
        plugin.getLogger().info("已加载 " + effects.size() + " 个效果");
    }
    
    /**
     * 加载天气效果
     * @param config 配置部分
     */
    private void loadWeatherEffects(ConfigurationSection config) {
        // 雨天减速效果
        if (config.getBoolean("rain-slow.enabled", true)) {
            registerEffect(new RainSlowEffect(plugin, config.getConfigurationSection("rain-slow")));
        }
        
        // 晴天奖励效果
        if (config.getBoolean("sunny-bonus.enabled", true)) {
            registerEffect(new SunnyBonusEffect(plugin, config.getConfigurationSection("sunny-bonus")));
        }
        
        // 雷暴伤害效果
        if (config.getBoolean("thunder-damage.enabled", true)) {
            registerEffect(new ThunderDamageEffect(plugin, config.getConfigurationSection("thunder-damage")));
        }
        
        // 雨天效果
        if (config.getBoolean("rain.enabled", true)) {
            registerEffect(new RainEffect(plugin, config.getConfigurationSection("rain")));
        }
        
        // 雷暴效果
        if (config.getBoolean("thunder.enabled", true)) {
            registerEffect(new ThunderEffect(plugin, config.getConfigurationSection("thunder")));
        }
        
        // 晴天效果
        if (config.getBoolean("clear.enabled", true)) {
            registerEffect(new ClearEffect(plugin, config.getConfigurationSection("clear")));
        }
    }
    
    /**
     * 加载时间效果
     * @param config 配置部分
     */
    private void loadTimeEffects(ConfigurationSection config) {
        // 日出效果
        if (config.getBoolean("sunrise.enabled", true)) {
            registerEffect(new SunriseEffect(plugin, config.getConfigurationSection("sunrise")));
        }
        
        // 日落效果
        if (config.getBoolean("sunset.enabled", true)) {
            registerEffect(new SunsetEffect(plugin, config.getConfigurationSection("sunset")));
        }
        
        // 白天效果
        if (config.getBoolean("day.enabled", true)) {
            registerEffect(new DayEffect(plugin, config.getConfigurationSection("day")));
        }
        
        // 夜晚效果
        if (config.getBoolean("night.enabled", true)) {
            registerEffect(new NightEffect(plugin, config.getConfigurationSection("night")));
        }
    }
    
    /**
     * 注册默认效果
     */
    private void registerDefaultEffects() {
        // 注册默认的天气效果
        registerEffect(new RainSlowEffect(plugin, null));
        registerEffect(new SunnyBonusEffect(plugin, null));
        registerEffect(new ThunderDamageEffect(plugin, null));
        registerEffect(new RainEffect(plugin, null));
        registerEffect(new ThunderEffect(plugin, null));
        registerEffect(new ClearEffect(plugin, null));
        
        // 注册默认的时间效果
        registerEffect(new SunriseEffect(plugin, null));
        registerEffect(new SunsetEffect(plugin, null));
        registerEffect(new DayEffect(plugin, null));
        registerEffect(new NightEffect(plugin, null));
        
        // 启动效果更新任务
        startEffectTask();
    }
    
    /**
     * 注册一个效果
     * @param effect 要注册的效果
     */
    public void registerEffect(BaseEffect effect) {
        effects.put(effect.getId(), effect);
        plugin.getLogger().info("已注册效果: " + effect.getId() + " - " + effect.getDescription());
    }
    
    /**
     * 取消注册一个效果
     * @param effectId 效果ID
     */
    public void unregisterEffect(String effectId) {
        BaseEffect effect = effects.remove(effectId);
        if (effect != null) {
            plugin.getLogger().info("已取消注册效果: " + effectId);
        }
    }
    
    /**
     * 取消注册所有效果
     */
    public void unregisterAllEffects() {
        effects.clear();
        stopEffectTask();
    }
    
    /**
     * 启动效果更新任务
     */
    private void startEffectTask() {
        // 如果任务已经在运行，先停止它
        stopEffectTask();
        
        // 创建新的定时任务，定期更新所有效果
        effectTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateEffects, 20L, updateInterval);
    }
    
    /**
     * 停止效果更新任务
     */
    private void stopEffectTask() {
        if (effectTask != null && !effectTask.isCancelled()) {
            effectTask.cancel();
            effectTask = null;
        }
    }
    
    /**
     * 更新所有效果
     */
    private void updateEffects() {
        // 对每个世界应用效果
        for (World world : Bukkit.getWorlds()) {
            // 获取适用于当前世界的效果
            List<BaseEffect> applicableEffects = new ArrayList<>();
            for (BaseEffect effect : effects.values()) {
                if (effect.isApplicable(world)) {
                    applicableEffects.add(effect);
                }
            }
            
            // 对世界中的每个玩家应用效果
            for (Player player : world.getPlayers()) {
                for (BaseEffect effect : applicableEffects) {
                    effect.apply(player, world);
                }
                
                // 执行天气指令
                executeWeatherCommands(player, world);
            }
        }
    }
    
    /**
     * 执行天气相关指令
     * @param player 目标玩家
     * @param world 当前世界
     */
    private void executeWeatherCommands(Player player, World world) {
        // 获取当前天气类型
        boolean isRaining = world.hasStorm();
        boolean isThundering = world.isThundering();
        
        // 获取对应天气的配置
        ConfigurationSection weatherConfig = plugin.getConfig().getConfigurationSection("effects." + 
            (isThundering ? "thunder" : isRaining ? "rain" : "clear"));
        
        if (weatherConfig == null || !weatherConfig.getBoolean("enabled", true)) {
            return;
        }
        
        // 检查指令配置
        ConfigurationSection commandsConfig = weatherConfig.getConfigurationSection("commands");
        if (commandsConfig == null || !commandsConfig.getBoolean("enabled", true)) {
            return;
        }
        
        // 检查触发几率
        double chance = commandsConfig.getDouble("chance", 0.0);
        if (chance <= 0 || new Random().nextDouble() > chance) {
            return;
        }
        
        // 执行随机指令
        List<String> commands = commandsConfig.getStringList("list");
        if (!commands.isEmpty()) {
            executeRandomCommand(commands, player);
        }
    }
    
    /**
     * 随机执行一条指令
     * @param commands 指令列表
     * @param player 目标玩家
     */
    private void executeRandomCommand(List<String> commands, Player player) {
        if (commands == null || commands.isEmpty()) {
            return;
        }
        
        Random random = new Random();
        String command = commands.get(random.nextInt(commands.size()));
        executeCommand(command, player);
    }
    
    /**
     * 执行指令，替换玩家占位符
     * @param command 指令
     * @param player 目标玩家
     */
    private void executeCommand(String command, Player player) {
        if (command == null || command.isEmpty()) {
            return;
        }
        
        String processedCommand = command.replace("%player%", player.getName());
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), processedCommand);
    }
    
    /**
     * 获取所有注册的效果
     * @return 效果映射表
     */
    public Map<String, BaseEffect> getEffects() {
        return Collections.unmodifiableMap(effects);
    }
    
    /**
     * 获取指定ID的效果
     * @param effectId 效果ID
     * @return 效果实例，如果不存在则返回null
     */
    public BaseEffect getEffect(String effectId) {
        return effects.get(effectId);
    }
    
    /**
     * 当玩家加入服务器时
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // 玩家加入时，立即应用适用的效果
        Player player = event.getPlayer();
        World world = player.getWorld();
        
        for (BaseEffect effect : effects.values()) {
            if (effect.isApplicable(world)) {
                effect.apply(player, world);
            }
        }
    }
    
    /**
     * 当玩家退出服务器时
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // 玩家退出时，移除所有效果
        Player player = event.getPlayer();
        World world = player.getWorld();
        
        for (BaseEffect effect : effects.values()) {
            effect.remove(player, world);
        }
    }
    
    /**
     * 当玩家切换世界时
     */
    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World fromWorld = event.getFrom();
        World toWorld = player.getWorld();
        
        // 移除旧世界的效果
        for (BaseEffect effect : effects.values()) {
            if (effect.isApplicable(fromWorld)) {
                effect.remove(player, fromWorld);
            }
        }
        
        // 应用新世界的效果
        for (BaseEffect effect : effects.values()) {
            if (effect.isApplicable(toWorld)) {
                effect.apply(player, toWorld);
            }
        }
    }
    
    /**
     * 当天气改变时
     */
    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        // 天气改变时，更新效果
        updateEffects();
    }
    
    /**
     * 当雷暴状态改变时
     */
    @EventHandler
    public void onThunderChange(ThunderChangeEvent event) {
        // 雷暴状态改变时，更新效果
        updateEffects();
    }
}