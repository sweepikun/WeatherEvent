package cn.popcraft.weatherevent.manager;

import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 生物群系缓存管理器
 * 用于优化生物群系检测性能，避免频繁的生物群系查询
 */
public class BiomeCacheManager {
    private final Map<UUID, BiomeCacheEntry> biomeCache;
    private final Logger logger;
    private static final long CACHE_EXPIRE_TIME = 5000; // 5秒缓存过期时间
    private static final int CACHE_CLEAN_INTERVAL = 60000; // 60秒清理一次过期缓存
    private long lastCacheCleanTime = 0;
    
    public BiomeCacheManager(Logger logger) {
        this.biomeCache = new ConcurrentHashMap<>();
        this.logger = logger;
        
        // 定期清理过期缓存
        Thread cacheCleaner = new Thread(this::cleanExpiredCache);
        cacheCleaner.setDaemon(true);
        cacheCleaner.start();
    }
    
    /**
     * 获取玩家当前位置的生物群系（带缓存）
     * @param player 玩家
     * @return 生物群系
     */
    public Biome getPlayerBiome(Player player) {
        UUID playerId = player.getUniqueId();
        Location location = player.getLocation();
        
        // 检查缓存
        BiomeCacheEntry cachedEntry = biomeCache.get(playerId);
        long currentTime = System.currentTimeMillis();
        
        // 清理过期缓存（定期执行）
        if (currentTime - lastCacheCleanTime > CACHE_CLEAN_INTERVAL) {
            cleanExpiredCache();
            lastCacheCleanTime = currentTime;
        }
        
        // 如果缓存有效且位置相近，返回缓存结果
        if (cachedEntry != null && !cachedEntry.isExpired(currentTime) && 
            isLocationClose(location, cachedEntry.getLocation())) {
            return cachedEntry.getBiome();
        }
        
        // 获取新的生物群系并更新缓存
        Biome currentBiome = location.getBlock().getBiome();
        biomeCache.put(playerId, new BiomeCacheEntry(currentBiome, location, currentTime));
        
        return currentBiome;
    }
    
    /**
     * 检查两个位置是否相近（在同一区域内）
     * @param loc1 位置1
     * @param loc2 位置2
     * @return 是否相近
     */
    private boolean isLocationClose(Location loc1, Location loc2) {
        // 如果不在同一世界，返回false
        if (!loc1.getWorld().equals(loc2.getWorld())) {
            return false;
        }
        
        // 检查坐标差异是否在合理范围内（4个方块内）
        return Math.abs(loc1.getBlockX() - loc2.getBlockX()) <= 4 &&
               Math.abs(loc1.getBlockY() - loc2.getBlockY()) <= 4 &&
               Math.abs(loc1.getBlockZ() - loc2.getBlockZ()) <= 4;
    }
    
    /**
     * 清理过期缓存
     */
    private void cleanExpiredCache() {
        long currentTime = System.currentTimeMillis();
        biomeCache.entrySet().removeIf(entry -> entry.getValue().isExpired(currentTime));
    }
    
    /**
     * 清除特定玩家的缓存
     * @param player 玩家
     */
    public void clearPlayerCache(Player player) {
        biomeCache.remove(player.getUniqueId());
    }
    
    /**
     * 清除所有缓存
     */
    public void clearAllCache() {
        biomeCache.clear();
    }
    
    /**
     * 获取缓存大小（用于调试）
     * @return 缓存大小
     */
    public int getCacheSize() {
        return biomeCache.size();
    }
    
    /**
     * 生物群系缓存条目
     */
    private static class BiomeCacheEntry {
        private final Biome biome;
        private final Location location;
        private final long timestamp;
        
        public BiomeCacheEntry(Biome biome, Location location, long timestamp) {
            this.biome = biome;
            this.location = location.clone(); // 克隆位置以避免外部修改
            this.timestamp = timestamp;
        }
        
        public Biome getBiome() {
            return biome;
        }
        
        public Location getLocation() {
            return location;
        }
        
        public boolean isExpired(long currentTime) {
            return currentTime - timestamp > CACHE_EXPIRE_TIME;
        }
    }
}