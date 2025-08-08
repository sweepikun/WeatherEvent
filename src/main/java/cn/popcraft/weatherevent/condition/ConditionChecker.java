package cn.popcraft.weatherevent.condition;

import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;

/**
 * 条件检查器
 * 用于检查各种触发条件是否满足
 */
public class ConditionChecker {

    /**
     * 检查生物群系条件
     * @param player 玩家
     * @param biomes 生物群系列表
     * @return 如果玩家所在生物群系在列表中，返回true；否则返回false
     */
    public static boolean checkBiome(Player player, List<String> biomes) {
        if (biomes == null || biomes.isEmpty()) {
            return true; // 如果没有指定生物群系，则条件满足
        }
        
        Location location = player.getLocation();
        Biome playerBiome = location.getBlock().getBiome();
        
        return biomes.stream()
                .anyMatch(biomeName -> playerBiome.name().equalsIgnoreCase(biomeName));
    }
    
    /**
     * 检查高度条件
     * @param player 玩家
     * @param minHeight 最小高度
     * @param maxHeight 最大高度
     * @return 如果玩家所在高度在范围内，返回true；否则返回false
     */
    public static boolean checkHeight(Player player, Integer minHeight, Integer maxHeight) {
        int y = player.getLocation().getBlockY();
        
        if (minHeight != null && y < minHeight) {
            return false;
        }
        
        if (maxHeight != null && y > maxHeight) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 检查光照条件
     * @param player 玩家
     * @param minLight 最小光照
     * @param maxLight 最大光照
     * @return 如果玩家所在位置的光照在范围内，返回true；否则返回false
     */
    public static boolean checkLight(Player player, Integer minLight, Integer maxLight) {
        int lightLevel = player.getLocation().getBlock().getLightLevel();
        
        if (minLight != null && lightLevel < minLight) {
            return false;
        }
        
        if (maxLight != null && lightLevel > maxLight) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 检查前置条件
     * @param player 玩家
     * @param prerequisites 前置条件配置
     * @return 如果满足前置条件，返回true；否则返回false
     */
    public static boolean checkPrerequisites(Player player, Map<String, Object> prerequisites) {
        if (prerequisites == null || prerequisites.isEmpty()) {
            return true; // 如果没有前置条件，则条件满足
        }
        
        String type = (String) prerequisites.get("type");
        if (type == null) {
            return true;
        }
        
        switch (type) {
            case "has_potion_effect":
                return checkPotionEffectPrerequisite(player, prerequisites);
            // 可以添加更多前置条件类型
            default:
                return true;
        }
    }
    
    /**
     * 检查药水效果前置条件
     * @param player 玩家
     * @param prerequisites 前置条件配置
     * @return 如果玩家有指定的药水效果，返回true；否则返回false
     */
    private static boolean checkPotionEffectPrerequisite(Player player, Map<String, Object> prerequisites) {
        String effectTypeName = (String) prerequisites.get("effect_type");
        if (effectTypeName == null) {
            return true;
        }
        
        PotionEffectType effectType = PotionEffectType.getByName(effectTypeName);
        if (effectType == null) {
            return true; // 如果效果类型无效，则条件满足
        }
        
        PotionEffect activeEffect = player.getPotionEffect(effectType);
        if (activeEffect == null) {
            return false; // 玩家没有该效果
        }
        
        // 检查效果等级
        if (prerequisites.containsKey("level")) {
            int requiredLevel = ((Number) prerequisites.get("level")).intValue();
            if (activeEffect.getAmplifier() != requiredLevel) {
                return false;
            }
        }
        
        return true;
    }
}