package com.ezeeinfo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

	@Bean
	public JedisConnectionFactory jedisConnectionFactory() {
		RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("localhost", 6379);

		return new JedisConnectionFactory(config);
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate(JedisConnectionFactory connectionFactory) {

		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
		template.afterPropertiesSet();
		return template;
	}
}