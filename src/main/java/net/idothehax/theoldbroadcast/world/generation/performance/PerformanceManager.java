package net.idothehax.theoldbroadcast.world.generation.performance;

import net.idothehax.theoldbroadcast.world.generation.config.TheOldStudioConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Performance optimization system for infinite Backrooms generation
 * Implements chunk caching, LOD control, and memory management
 */
public class PerformanceManager {

    private final Map<ChunkPos, ChunkGenerationData> chunkCache = new ConcurrentHashMap<>();
    private final Queue<ChunkPos> chunkQueue = new LinkedList<>();
    private final Set<ChunkPos> currentlyGenerating = ConcurrentHashMap.newKeySet();

    // Performance metrics
    private long totalGenerationTime = 0;
    private int chunksGenerated = 0;
    private long lastCleanupTime = System.currentTimeMillis();

    public synchronized boolean shouldGenerateChunk(ChunkPos chunkPos, WorldGenLevel level) {
        // Check if already cached or currently generating
        if (chunkCache.containsKey(chunkPos) || currentlyGenerating.contains(chunkPos)) {
            return false;
        }

        // Check LOD distance if enabled
        if (TheOldStudioConfig.ENABLE_LOD) {
            return isWithinLODDistance(chunkPos, level);
        }

        return true;
    }

    public synchronized void markChunkGenerationStart(ChunkPos chunkPos) {
        currentlyGenerating.add(chunkPos);
    }

    public synchronized void markChunkGenerationComplete(ChunkPos chunkPos, long generationTime) {
        currentlyGenerating.remove(chunkPos);

        ChunkGenerationData data = new ChunkGenerationData(
            System.currentTimeMillis(),
            generationTime,
            calculateComplexityScore(chunkPos)
        );

        chunkCache.put(chunkPos, data);
        chunkQueue.offer(chunkPos);

        // Update metrics
        totalGenerationTime += generationTime;
        chunksGenerated++;

        // Manage cache size
        if (chunkCache.size() > TheOldStudioConfig.CHUNK_CACHE_SIZE) {
            evictOldestChunk();
        }

        // Periodic cleanup
        if (System.currentTimeMillis() - lastCleanupTime > 30000) { // 30 seconds
            performCleanup();
        }
    }

    public ChunkGenerationData getChunkData(ChunkPos chunkPos) {
        return chunkCache.get(chunkPos);
    }

    public boolean isChunkCached(ChunkPos chunkPos) {
        return chunkCache.containsKey(chunkPos);
    }

    public double getAverageGenerationTime() {
        return chunksGenerated > 0 ? (double) totalGenerationTime / chunksGenerated : 0.0;
    }

    public int getCachedChunkCount() {
        return chunkCache.size();
    }

    private boolean isWithinLODDistance(ChunkPos chunkPos, WorldGenLevel level) {
        // For now, generate all chunks - in a real implementation,
        // you'd check distance from active players
        return true;
    }

    private int calculateComplexityScore(ChunkPos chunkPos) {
        // Calculate generation complexity based on chunk position
        // Higher scores indicate more complex generation requirements
        int x = chunkPos.x;
        int z = chunkPos.z;

        // Distance from origin affects complexity
        int distanceFromOrigin = Math.abs(x) + Math.abs(z);
        int complexity = Math.min(10, distanceFromOrigin / 10);

        // Add some randomness based on chunk coordinates
        long hash = ((long) x << 32) | (z & 0xFFFFFFFFL);
        Random posRandom = new Random(hash);
        complexity += posRandom.nextInt(3);

        return Math.max(1, Math.min(10, complexity));
    }

    private synchronized void evictOldestChunk() {
        ChunkPos oldest = chunkQueue.poll();
        if (oldest != null) {
            chunkCache.remove(oldest);
        }
    }

    private synchronized void performCleanup() {
        lastCleanupTime = System.currentTimeMillis();

        // Remove chunks older than 5 minutes
        long cutoffTime = lastCleanupTime - 300000;
        Iterator<Map.Entry<ChunkPos, ChunkGenerationData>> iterator = chunkCache.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<ChunkPos, ChunkGenerationData> entry = iterator.next();
            if (entry.getValue().generationTime < cutoffTime) {
                iterator.remove();
                chunkQueue.remove(entry.getKey());
            }
        }

        // Clear stuck generation markers
        currentlyGenerating.clear();
    }

    public void clearCache() {
        synchronized (this) {
            chunkCache.clear();
            chunkQueue.clear();
            currentlyGenerating.clear();
            totalGenerationTime = 0;
            chunksGenerated = 0;
        }
    }

    public static class ChunkGenerationData {
        public final long generationTime;
        public final long duration;
        public final int complexityScore;

        public ChunkGenerationData(long generationTime, long duration, int complexityScore) {
            this.generationTime = generationTime;
            this.duration = duration;
            this.complexityScore = complexityScore;
        }

        public boolean isExpired(long currentTime, long maxAge) {
            return currentTime - generationTime > maxAge;
        }
    }
}
