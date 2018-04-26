package com.station.common.redis;

import java.util.Collection;

import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;

public class RedisCacheManager extends AbstractCacheManager {
	private Collection<? extends RedisCache> caches;

	/**
	 * 缓存参数的分隔符 
	 * 数组元素0=缓存的名称 
	 * 数组元素1=缓存过期时间单位
	 * 数组元素2=缓存过期时间
	 */
	private String separator = "#";

	enum Field {
		DAY, HOUR, MINUTE, SECOND
	}

	@Override
	protected Collection<? extends RedisCache> loadCaches() {
		return caches;
	}

	public void setCaches(Collection<? extends RedisCache> caches) {
		this.caches = caches;
	}

	@Override
	public Cache getCache(String name) {
		String[] cacheParams = name.split(this.separator);
		String cacheName = cacheParams[0];
		RedisCache cache = (RedisCache) super.getCache(cacheName);
		if (cacheParams.length >= 3) {
			cache.setExpireSec(getSeconds(cacheParams[1], cacheParams[2]));
		}
		return cache;
	}

	private Integer getSeconds(String field, String time) {
		Integer seconds = null;
		try {
			switch (Field.valueOf(field.toUpperCase())) {
			case DAY:
				seconds = Integer.valueOf(time) * 24 * 60 * 60;
				break;
			case HOUR:
				seconds = Integer.valueOf(time) * 60 * 60;
				break;
			case MINUTE:
				seconds = Integer.valueOf(time) * 60;
				break;
			default:
				seconds = Integer.valueOf(time);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return seconds;
	}

}
