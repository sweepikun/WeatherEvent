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
    private int delay; // 延迟触发时间（tick）
    private Map<String, Object> conditions; // 触发条件
    
    public ChainEffect(double chance, String effectId) {
        this.chance = chance;
        this.effectId = effectId;
        this.delay = 0; // 默认无延迟
        this.conditions = null;
    }
    
    public ChainEffect(double chance, String effectId, int delay, Map<String, Object> conditions) {
        this.chance = chance;
        this.effectId = effectId;
        this.delay = delay;
        this.conditions = conditions;
    }
    
    public double getChance() {
        return chance;
    }
    
    public String getEffectId() {
        return effectId;
    }
    
    public int getDelay() {
        return delay;
    }
    
    public Map<String, Object> getConditions() {
        return conditions;
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
                int delay = effectConfig.containsKey("delay") ? 
                    ((Number) effectConfig.get("delay")).intValue() : 0;
                Map<String, Object> conditions = (Map<String, Object>) effectConfig.get("conditions");
                
                chainEffects.add(new ChainEffect(chance, effectId, delay, conditions));
            }
        }
        
        return chainEffects;
    }
    
    /**
     * 从配置映射创建连锁效果列表
     * @param config 配置映射
     * @return 连锁效果列表
     */
    public static List<ChainEffect> fromConfigMap(Map<String, Map<String, Object>> config) {
        List<ChainEffect> chainEffects = new ArrayList<>();
        
        if (config == null || config.isEmpty()) {
            return chainEffects;
        }
        
        for (Map.Entry<String, Map<String, Object>> entry : config.entrySet()) {
            Map<String, Object> effectConfig = entry.getValue();
            double chance = ((Number) effectConfig.getOrDefault("chance", 0.0)).doubleValue();
            String effectId = (String) effectConfig.get("effect-id");
            
            if (effectId != null && !effectId.isEmpty()) {
                int delay = effectConfig.containsKey("delay") ? 
                    ((Number) effectConfig.get("delay")).intValue() : 0;
                Map<String, Object> conditions = (Map<String, Object>) effectConfig.get("conditions");
                
                chainEffects.add(new ChainEffect(chance, effectId, delay, conditions));
            }
        }
        
        return chainEffects;
    }
}