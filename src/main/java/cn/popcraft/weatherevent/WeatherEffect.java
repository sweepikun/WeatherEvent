package cn.popcraft.weatherevent;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.Map;

/**
 * 天气效果接口
 */
public interface WeatherEffect {
    
    /**
     * 应用天气效果到玩家
     * @param player 玩家
     * @param world 世界
     */
    void apply(Player player, World world);
    
    /**
     * 从玩家移除天气效果
     * @param player 玩家
     * @param world 世界
     */
    void remove(Player player, World world);
    
    /**
     * 获取效果名称
     * @return 效果名称
     */
    String getName();
    
    /**
     * 获取药水效果列表
     * @return 药水效果列表
     */
    List<PotionEffect> getPotionEffects();
    
    /**
     * 获取随机效果映射
     * @return 随机效果映射
     */
    Map<String, Object> getRandomEffects();
    
    /**
     * 获取命令映射
     * @return 命令映射
     */
    Map<String, Object> getCommands();
    
    /**
     * 检查效果是否适用于指定世界
     * @param world 世界
     * @return 是否适用
     */
    boolean isApplicable(World world);
    
    /**
     * 获取效果ID
     * @return 效果ID
     */
    String getId();
    
    /**
     * 获取效果描述
     * @return 效果描述
     */
    String getDescription();
    
    /**
     * 检查效果是否启用
     * @return 是否启用
     */
    boolean isEnabled();
}