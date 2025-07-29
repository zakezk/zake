package com.zake.aicode;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zake.aicode.mapper")
public class ZakeAiCodeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZakeAiCodeApplication.class, args);
	}

}