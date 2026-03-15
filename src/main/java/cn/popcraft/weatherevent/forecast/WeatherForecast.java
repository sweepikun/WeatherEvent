package cn.popcraft.weatherevent.forecast;

import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

/**
 * 天气预报类
 * 包含未来一段时间的天气预测
 */
public class WeatherForecast {
    
    private final World world;
    private final List<ForecastEntry> entries;
    
    /**
     * 创建天气预报
     * @param world 世界
     */
    public WeatherForecast(World world) {
        this.world = world;
        this.entries = new ArrayList<>();
    }
    
    /**
     * 添加预报条目
     * @param entry 预报条目
     */
    public void addEntry(ForecastEntry entry) {
        entries.add(entry);
    }
    
    /**
     * 获取预报条目
     * @param index 索引
     * @return 预报条目
     */
    public ForecastEntry getEntry(int index) {
        if (index >= 0 && index < entries.size()) {
            return entries.get(index);
        }
        return null;
    }
    
    /**
     * 获取所有预报条目
     * @return 预报条目列表
     */
    public List<ForecastEntry> getEntries() {
        return entries;
    }
    
    /**
     * 获取预报条目数量
     * @return 条目数量
     */
    public int getEntryCount() {
        return entries.size();
    }
    
    /**
     * 获取世界
     * @return 世界
     */
    public World getWorld() {
        return world;
    }
    
    /**
     * 格式化预报为字符串
     * @return 格式化后的预报
     */
    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("§6=== ").append(world.getName()).append(" 天气预报 ===\n");
        
        for (int i = 0; i < entries.size(); i++) {
            ForecastEntry entry = entries.get(i);
            sb.append("§e").append(i + 1).append(". ")
              .append(entry.getColor())
              .append(entry.getWeatherType().getDisplayName())
              .append(" §7- ")
              .append(entry.getTimeDescription())
              .append(" §7(可信度: ")
              .append(String.format("%.0f%%", entry.getConfidence() * 100))
              .append(")\n");
        }
        
        return sb.toString();
    }
}
