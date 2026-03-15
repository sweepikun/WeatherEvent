package cn.popcraft.weatherevent.season;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 季节变化事件
 * 当世界季节变化时触发
 */
public class SeasonChangeEvent extends Event {
    
    private static final HandlerList handlers = new HandlerList();
    
    private final World world;
    private final Season oldSeason;
    private final Season newSeason;
    
    /**
     * 创建季节变化事件
     * @param world 世界
     * @param oldSeason 旧季节
     * @param newSeason 新季节
     */
    public SeasonChangeEvent(World world, Season oldSeason, Season newSeason) {
        this.world = world;
        this.oldSeason = oldSeason;
        this.newSeason = newSeason;
    }
    
    /**
     * 获取世界
     * @return 世界
     */
    public World getWorld() {
        return world;
    }
    
    /**
     * 获取旧季节
     * @return 旧季节
     */
    public Season getOldSeason() {
        return oldSeason;
    }
    
    /**
     * 获取新季节
     * @return 新季节
     */
    public Season getNewSeason() {
        return newSeason;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
