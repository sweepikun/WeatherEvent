package cn.popcraft.weatherevent.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 连锁效果
 * 用于定义效果触发后的连锁反应
 */
public class ChainEffect {
    private double chance;
    private String effectId;
    
    public ChainEffect(double chance, String effectId) {
        this.chance = chance;
        this.effectId = effectId;
    }
    
    public double getChance() {
        return chance;
    }
    
    public String getEffectId() {
        return effectId;
    }
    
    /**
     * 从配置中创建连锁效果列表
     * @param config 配置列表
     * @return 连锁效果列表
     */
    public static List<ChainEffect> fromConfig(List<Map<String, Object>> config) {
        List<ChainEffect> chainEffects = new ArrayList<>();
        
        if (config == null || config.isEmpty()) {
            return chainEffects;
        }
        
        for (Map<String, Object> effectConfig : config) {
            double chance = ((Number) effectConfig.getOrDefault("chance", 0.0)).doubleValue();
            String effectId = (String) effectConfig.get("effect-id");
            
            if (effectId != null && !effectId.isEmpty()) {
                chainEffects.add(new ChainEffect(chance, effectId));
            }
        }
        
        return chainEffects;
    }
}