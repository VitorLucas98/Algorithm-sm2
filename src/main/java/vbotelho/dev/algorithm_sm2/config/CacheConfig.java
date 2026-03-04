package vbotelho.dev.algorithm_sm2.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory cf) {
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(5))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonValueSerializer()))
                .disableCachingNullValues();

        var cacheConfigs = Map.of(
                "studySession", cacheConfiguration.entryTtl(Duration.ofMinutes(5)),
                "deckStats",    cacheConfiguration.entryTtl(Duration.ofMinutes(10))
        );

        return RedisCacheManager.builder(cf)
                .cacheDefaults(cacheConfiguration)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }

    private RedisSerializer<Object> jsonValueSerializer() {
        var ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();
        var mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

        return new RedisSerializer<>() {
            @Override
            public byte[] serialize(Object value) throws SerializationException {
                if (value == null) return new byte[0];
                try {
                    return mapper.writeValueAsBytes(value);
                } catch (JsonProcessingException e) {
                    throw new SerializationException("Redis serialize error: " + e.getMessage(), e);
                }
            }

            @Override
            public Object deserialize(byte[] bytes) throws SerializationException {
                if (bytes == null || bytes.length == 0) return null;
                try {
                    return mapper.readValue(bytes, Object.class);
                } catch (IOException e) {
                    throw new SerializationException("Redis deserialize error: " + e.getMessage(), e);
                }
            }
        };
    }
}