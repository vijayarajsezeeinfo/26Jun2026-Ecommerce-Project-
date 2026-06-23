package com.ezeeinfo.config;

import java.util.List;

import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ezeeinfo.dto.UserDTO;

@Configuration
public class EhCacheConfig {

	@Bean
	public CacheManager cacheManager() {

		return CacheManagerBuilder.newCacheManagerBuilder().withCache("userCache", CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, UserDTO.class, ResourcePoolsBuilder.heap(100))).withCache("userListCache", CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, List.class, ResourcePoolsBuilder.heap(20))).build(true);
	}
}