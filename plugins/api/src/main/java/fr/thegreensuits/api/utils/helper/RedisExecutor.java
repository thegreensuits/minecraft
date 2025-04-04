package fr.thegreensuits.api.utils.helper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.function.Function;

public class RedisExecutor {
    private final JedisPool jedisPool;

    public RedisExecutor(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * Executes a Redis command and returns a result.
     * This ensures the connection is properly managed.
     */
    public <T> T execute(Function<Jedis, T> function) {
        try (Jedis jedis = jedisPool.getResource()) {
            return function.apply(jedis);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Executes a Redis command that does not return a result.
     */
    public void executeVoid(Function<Jedis, Void> function) {
        try (Jedis jedis = jedisPool.getResource()) {
            function.apply(jedis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
