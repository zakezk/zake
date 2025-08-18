package com.zake.aicode;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import jakarta.annotation.Resource;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.zake.aicode.mapper")
@EnableCaching//开启缓存注解  Spring Data 缓存注解
public class ZakeAiCodeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZakeAiCodeApplication.class, args);
	}

}