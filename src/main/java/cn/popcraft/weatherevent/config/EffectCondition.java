package cn.popcraft.weatherevent.config;

import java.util.List;

/**
 * 效果触发条件
 * 用于存储和检查效果的触发条件
 */
public class EffectCondition {
    private List<String> biomes;
    private Integer minHeight;
    private Integer maxHeight;
    private Integer minLight;
    private Integer maxLight;
    private List<String> weatherTypes; // 天气类型列表：clear, rain, thunder
    
    public EffectCondition() {
        // 默认构造函数
    }
    
    public List<String> getBiomes() {
        return biomes;
    }
    
    public void setBiomes(List<String> biomes) {
        this.biomes = biomes;
    }
    
    public Integer getMinHeight() {
        return minHeight;
    }
    
    public void setMinHeight(Integer minHeight) {
        this.minHeight = minHeight;
    }
    
    public Integer getMaxHeight() {
        return maxHeight;
    }
    
    public void setMaxHeight(Integer maxHeight) {
        this.maxHeight = maxHeight;
    }
    
    public Integer getMinLight() {
        return minLight;
    }
    
    public void setMinLight(Integer minLight) {
        this.minLight = minLight;
    }
    
    public Integer getMaxLight() {
        return maxLight;
    }
    
    public void setMaxLight(Integer maxLight) {
        this.maxLight = maxLight;
    }
    
    public List<String> getWeatherTypes() {
        return weatherTypes;
    }
    
    public void setWeatherTypes(List<String> weatherTypes) {
        this.weatherTypes = weatherTypes;
    }
}