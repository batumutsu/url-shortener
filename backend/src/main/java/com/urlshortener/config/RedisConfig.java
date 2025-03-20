package com.urlshortener.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class RedisConfig {

  private final JedisPool jedisPool;

  public RedisConfig(
      @Value("${spring.redis.host}") String redisHost,
      @Value("${spring.redis.port}") int redisPort) {
    JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(128);
    poolConfig.setMaxIdle(128);
    poolConfig.setMinIdle(16);
    poolConfig.setTestOnBorrow(true);
    poolConfig.setTestOnReturn(true);
    poolConfig.setTestWhileIdle(true);
    this.jedisPool = new JedisPool(poolConfig, redisHost, redisPort);
  }

  /**
   * Get a value from Redis by key
   */
  public String get(String key) {
    try (Jedis jedis = jedisPool.getResource()) {
      return jedis.get(key);
    }
  }

  /**
   * Set a key-value pair with expiration time in seconds
   */
  public void setex(String key, int seconds, String value) {
    try (Jedis jedis = jedisPool.getResource()) {
      jedis.setex(key, seconds, value);
    }
  }

  /**
   * Increment a key's value
   */
  public Long incr(String key) {
    try (Jedis jedis = jedisPool.getResource()) {
      return jedis.incr(key);
    }
  }

  /**
   * Set a key with value only if the key doesn't exist
   */
  public Long setnx(String key, String value) {
    try (Jedis jedis = jedisPool.getResource()) {
      return jedis.setnx(key, value);
    }
  }

  /**
   * Set a key's expiration time
   */
  public Long expire(String key, int seconds) {
    try (Jedis jedis = jedisPool.getResource()) {
      return jedis.expire(key, seconds);
    }
  }

  /**
   * Delete a key
   */
  public Long del(String key) {
    try (Jedis jedis = jedisPool.getResource()) {
      return jedis.del(key);
    }
  }
}