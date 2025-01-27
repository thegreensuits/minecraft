package fr.thegreensuits.api;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fr.thegreensuits.api.config.RedisConfig;
import fr.thegreensuits.api.player.PlayerManager;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public abstract class TheGreenSuits implements Closeable {
  private static TheGreenSuits _INSTANCE;

  @Getter()
  private JedisPool jedisPool;
  @Getter()
  private ExecutorService executorService;

  public TheGreenSuits(RedisConfig redisConfig) {
    if (_INSTANCE != null) {
      throw new IllegalStateException("TheGreenSuits is already initialized");
    }
    _INSTANCE = this;

    this.executorService = Executors.newCachedThreadPool();

    this.setupRedis(redisConfig);
  }

  public static TheGreenSuits get() {
    if (_INSTANCE == null) {
      throw new IllegalStateException("TheGreenSuits is not initialized");
    }
    return _INSTANCE;
  }

  @Override()
  public void close() {
    this.executorService.shutdown();

    if (this.jedisPool != null && !this.jedisPool.isClosed())
      this.jedisPool.close();

    // - Interrupt network info api
    // TODO
  }

  public abstract PlayerManager getPlayerManager();

  private void setupRedis(RedisConfig redisConfig) {
    if (redisConfig.isEnabled()) {
      ClassLoader previous = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(Jedis.class.getClassLoader());

      JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
      this.jedisPool = new JedisPool(jedisPoolConfig, redisConfig.getHost(), redisConfig.getPort(), 3000,
          redisConfig.getPassword(),
          redisConfig.getDatabase());

      Thread.currentThread().setContextClassLoader(previous);

      try (Jedis jedis = this.jedisPool.getResource()) {
        jedis.ping();
        System.out.println("[TheGreenSuits] Redis connection established");
      } catch (Exception e) {
        System.err.println("[TheGreenSuits] Redis connection failed");
        e.printStackTrace();

        this.jedisPool.close();
      }
    }
  }
}
