package cn.popcraft.weatherevent.api;

import cn.popcraft.weatherevent.disaster.DisasterType;
import cn.popcraft.weatherevent.season.Season;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * 天气事件监听器接口
 * 允许其他插件监听天气相关事件
 */
public interface WeatherEventListener {
    
    /**
     * 当天气变化时调用
     * @param world 世界
     * @param oldWeather 旧天气
     * @param newWeather 新天气
     */
    default void onWeatherChange(World world, String oldWeather, String newWeather) {}
    
    /**
     * 当季节变化时调用
     * @param world 世界
     * @param oldSeason 旧季节
     * @param newSeason 新季节
     */
    default void onSeasonChange(World world, Season oldSeason, Season newSeason) {}
    
    /**
     * 当灾害开始时调用
     * @param world 世界
     * @param disasterType 灾害类型
     */
    default void onDisasterStart(World world, DisasterType disasterType) {}
    
    /**
     * 当灾害结束时调用
     * @param world 世界
     * @param disasterType 灾害类型
     */
    default void onDisasterEnd(World world, DisasterType disasterType) {}
    
    /**
     * 当玩家受到天气效果影响时调用
     * @param player 玩家
     * @param world 世界
     * @param effectId 效果ID
     */
    default void onPlayerWeatherEffect(Player player, World world, String effectId) {}
}
