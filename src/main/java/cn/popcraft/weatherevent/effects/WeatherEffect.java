package cn.popcraft.weatherevent.effects;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import java.util.List;
import java.util.Map;

/**
 * 天气效果接口，定义所有效果类需要实现的方法
 */
public interface WeatherEffect {
    
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
    
    /**
     * 设置效果是否启用
     * @param enabled 是否启用
     */
    void setEnabled(boolean enabled);
    
    /**
     * 从配置中加载效果
     * @param config 配置部分
     */
    void loadFromConfig(ConfigurationSection config);
    
    /**
     * 应用效果到玩家
     * @param player 目标玩家
     * @param world 目标世界
     */
    void apply(Player player, World world);

    /**
     * 移除玩家的效果
     * @param player 目标玩家
     * @param world 目标世界
     */
    void remove(Player player, World world);

    /**
     * 检查效果是否适用于当前世界状态
     * @param world 目标世界
     * @return 是否适用
     */
    boolean isApplicable(World world);

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
     * 获取随机效果配置
     * @return 随机效果配置
     */
    Map<String, Object> getRandomEffects();

    /**
     * 获取命令配置
     * @return 命令配置
     */
    Map<String, Object> getCommands();
}