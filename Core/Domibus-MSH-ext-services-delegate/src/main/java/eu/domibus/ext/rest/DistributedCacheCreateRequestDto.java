package eu.domibus.ext.rest;

/**
 * @author Cosmin Baciu
 * @since 5.1
 */
public class DistributedCacheCreateRequestDto {

    protected String cacheName;
    protected Integer cacheSize;
    protected Integer timeToLiveSeconds;
    protected Integer maxIdleSeconds;
    protected Integer nearCacheSize;
    protected Integer nearCacheTimeToLiveSeconds;
    protected Integer nearCacheMaxIdleSeconds;

    public DistributedCacheCreateRequestDto() {
    }

    public DistributedCacheCreateRequestDto(String cacheName, Integer cacheSize, Integer timeToLiveSeconds, Integer maxIdleSeconds, Integer nearCacheSize, Integer nearCacheTimeToLiveSeconds, Integer nearCacheMaxIdleSeconds) {
        this.cacheName = cacheName;
        this.cacheSize = cacheSize;
        this.timeToLiveSeconds = timeToLiveSeconds;
        this.maxIdleSeconds = maxIdleSeconds;
        this.nearCacheSize = nearCacheSize;
        this.nearCacheTimeToLiveSeconds = nearCacheTimeToLiveSeconds;
        this.nearCacheMaxIdleSeconds = nearCacheMaxIdleSeconds;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public Integer getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(Integer cacheSize) {
        this.cacheSize = cacheSize;
    }

    public Integer getTimeToLiveSeconds() {
        return timeToLiveSeconds;
    }

    public void setTimeToLiveSeconds(Integer timeToLiveSeconds) {
        this.timeToLiveSeconds = timeToLiveSeconds;
    }

    public Integer getMaxIdleSeconds() {
        return maxIdleSeconds;
    }

    public void setMaxIdleSeconds(Integer maxIdleSeconds) {
        this.maxIdleSeconds = maxIdleSeconds;
    }

    public Integer getNearCacheSize() {
        return nearCacheSize;
    }

    public void setNearCacheSize(Integer nearCacheSize) {
        this.nearCacheSize = nearCacheSize;
    }

    public Integer getNearCacheTimeToLiveSeconds() {
        return nearCacheTimeToLiveSeconds;
    }

    public void setNearCacheTimeToLiveSeconds(Integer nearCacheTimeToLiveSeconds) {
        this.nearCacheTimeToLiveSeconds = nearCacheTimeToLiveSeconds;
    }

    public Integer getNearCacheMaxIdleSeconds() {
        return nearCacheMaxIdleSeconds;
    }

    public void setNearCacheMaxIdleSeconds(Integer nearCacheMaxIdleSeconds) {
        this.nearCacheMaxIdleSeconds = nearCacheMaxIdleSeconds;
    }
}
