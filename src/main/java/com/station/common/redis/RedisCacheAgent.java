package com.station.common.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.stereotype.Component;

@Component
public class RedisCacheAgent {
	private final RedisCache redisCache;
	
	@Autowired
	public  RedisCacheAgent(RedisCache redisCache, RedisClientTemplate redisClient) {
		this.redisCache = redisCache;
		redisCache.setName("station");
		redisCache.setRedisTemplate(redisClient);
	}
	
	public void refreshCache(Object key, Object value) {
		redisCache.put(key, value);
	}
	
	public Object getCache(Object key){
		ValueWrapper valueWrapper = redisCache.get(key);
		if (valueWrapper != null) {
			return valueWrapper.get();
		}
		return null;
	}
	
	public void clear(String key) {
		redisCache.evict(key);
	}
}
