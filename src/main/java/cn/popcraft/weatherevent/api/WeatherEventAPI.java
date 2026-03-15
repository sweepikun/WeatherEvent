package cn.popcraft.weatherevent.api;

import cn.popcraft.weatherevent.WeatherEvent;
import cn.popcraft.weatherevent.disaster.DisasterType;
import cn.popcraft.weatherevent.effects.BaseWeatherEffect;
import cn.popcraft.weatherevent.forecast.WeatherForecast;
import cn.popcraft.weatherevent.season.Season;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * WeatherEvent API接口
 * 允许其他插件与WeatherEvent交互
 */
public interface WeatherEventAPI {
    
    /**
     * 获取插件实例
     * @return 插件实例
     */
    WeatherEvent getPlugin();
    
    // ==================== 天气效果相关 ====================
    
    /**
     * 获取所有已注册的效果
     * @return 效果映射表
     */
    Map<String, BaseWeatherEffect> getEffects();
    
    /**
     * 获取指定ID的效果
     * @param effectId 效果ID
     * @return 效果实例，如果不存在返回null
     */
    BaseWeatherEffect getEffect(String effectId);
    
    /**
     * 注册自定义效果
     * @param effect 效果实例
     * @return 是否成功注册
     */
    boolean registerEffect(BaseWeatherEffect effect);
    
    /**
     * 取消注册效果
     * @param effectId 效果ID
     * @return 是否成功取消注册
     */
    boolean unregisterEffect(String effectId);
    
    /**
     * 对玩家应用效果
     * @param player 玩家
     * @param world 世界
     * @param effectId 效果ID
     * @return 是否成功应用
     */
    boolean applyEffect(Player player, World world, String effectId);
    
    /**
     * 移除玩家的效果
     * @param player 玩家
     * @param world 世界
     * @param effectId 效果ID
     * @return 是否成功移除
     */
    boolean removeEffect(Player player, World world, String effectId);
    
    // ==================== 季节系统相关 ====================
    
    /**
     * 检查季节系统是否启用
     * @return 是否启用
     */
    boolean isSeasonSystemEnabled();
    
    /**
     * 获取世界的当前季节
     * @param world 世界
     * @return 当前季节
     */
    Season getWorldSeason(World world);
    
    /**
     * 设置世界的季节
     * @param world 世界
     * @param season 季节
     */
    void setWorldSeason(World world, Season season);
    
    /**
     * 对玩家应用季节效果
     * @param player 玩家
     * @param world 世界
     */
    void applySeasonEffects(Player player, World world);
    
    // ==================== 灾害系统相关 ====================
    
    /**
     * 检查灾害系统是否启用
     * @return 是否启用
     */
    boolean isDisasterSystemEnabled();
    
    /**
     * 检查世界是否有活跃灾害
     * @param world 世界
     * @return 是否有活跃灾害
     */
    boolean hasActiveDisaster(World world);
    
    /**
     * 强制触发灾害
     * @param world 世界
     * @param type 灾害类型
     * @return 是否成功触发
     */
    boolean forceDisaster(World world, DisasterType type);
    
    /**
     * 停止世界的灾害
     * @param world 世界
     * @return 是否成功停止
     */
    boolean stopDisaster(World world);
    
    // ==================== 天气预报相关 ====================
    
    /**
     * 检查天气预报系统是否启用
     * @return 是否启用
     */
    boolean isWeatherForecastEnabled();
    
    /**
     * 获取世界的天气预报
     * @param world 世界
     * @return 天气预报
     */
    WeatherForecast getWeatherForecast(World world);
    
    /**
     * 强制更新天气预报
     * @param world 世界
     * @return 更新后的天气预报
     */
    WeatherForecast forceUpdateForecast(World world);
    
    // ==================== 事件系统相关 ====================
    
    /**
     * 注册天气事件监听器
     * @param listener 监听器
     */
    void registerWeatherListener(WeatherEventListener listener);
    
    /**
     * 取消注册天气事件监听器
     * @param listener 监听器
     */
    void unregisterWeatherListener(WeatherEventListener listener);
}
