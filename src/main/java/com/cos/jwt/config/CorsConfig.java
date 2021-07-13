package com.cos.jwt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

	@Bean
	public CorsFilter corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true); // 내 서버가 응답할 때 json을 자바스크립트에서 처리할 수 있게 할지를 설정하는 것  (true: 응답 o/ false: 응답  x) 
		config.addAllowedOrigin("*"); // 모든 ip의 응답을 다 허용
		config.addAllowedHeader("*"); // 모든 헤더 허용
		config.addAllowedMethod("*"); // get,post,put,delete,patch 등 모든 요청 허용
		source.registerCorsConfiguration("/api/**", config); // api로 시작되는 모든 요청은 이 config 설정을 따르도록 설정
		return new CorsFilter(source);
	}
}
