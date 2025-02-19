package fr.thegreensuits.api;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;

import fr.thegreensuits.api.config.RedisConfig;
import fr.thegreensuits.api.server.manager.ServerManager;
import lombok.Getter;
import lombok.Setter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public abstract class TheGreenSuits implements Closeable {
  private static TheGreenSuits _INSTANCE;

  @Getter()
  @Setter()
  private String serverId;

  @Getter()
  private JedisPool jedisPool;
  @Getter()
  private ExecutorService executorService;
  @Getter()
  private ServerManager serverManager;

  public TheGreenSuits(String serverId) {
    this(serverId, new RedisConfig(true, "redis", 6379));
  }

  public TheGreenSuits(String serverId, RedisConfig redisConfig) {
    if (_INSTANCE != null) {
      throw new IllegalStateException("TheGreenSuits is already initialized");
    }
    _INSTANCE = this;

    this.setupRedis(redisConfig);

    this.serverId = serverId;
    this.executorService = Executors.newCachedThreadPool();
    this.serverManager = new ServerManager(this);
  }

  public abstract Logger getLogger();

  /**
   * Setup Redis connection pool
   * 
   * @param redisConfig
   */
  private void setupRedis(RedisConfig redisConfig) {
    if (redisConfig.isEnabled()) {
      ClassLoader previous = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(Jedis.class.getClassLoader());

      JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
      this.jedisPool = new JedisPool(jedisPoolConfig, redisConfig.getHost(), redisConfig.getPort(), 3000);

      Thread.currentThread().setContextClassLoader(previous);

      try (Jedis jedis = this.jedisPool.getResource()) {
        jedis.ping();
        this.getLogger().info("Redis connection established");
      } catch (Exception e) {
        this.getLogger().info("Redis connection failed");
        e.printStackTrace();

        this.jedisPool.close();
      }
    }
  }

  @Override()
  public void close() {
    this.executorService.shutdown();

    if (this.jedisPool != null && !this.jedisPool.isClosed())
      this.jedisPool.close();
  }

  public static TheGreenSuits get() {
    if (_INSTANCE == null) {
      throw new IllegalStateException("TheGreenSuits is not initialized");
    }
    return _INSTANCE;
  }
}
