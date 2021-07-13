package com.cos.jwt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

import com.cos.jwt.JwtAuthorizationFilter;
import com.cos.jwt.config.jwt.JwtAuthenticationFilter;
import com.cos.jwt.filter.MyFilter3;
import com.cos.jwt.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter{
	
	private final CorsFilter corsFilter;
	private final UserRepository userRepository;
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.addFilterBefore(new MyFilter3(), BasicAuthenticationFilter.class);  // security filter가 가장 먼저 실행되지만 그보다 내가 만든 필터가 먼저 실행되게 하려면 before를 해준다.
		http.csrf().disable();
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션을  사용하지 않겠다 
		.and()
		.addFilter(corsFilter) // cross origin정책의 경우 @CrossOrigin은 로그인같이 인증이 필요없는 경우. 인증이 필요한 경우 시큐리티 필터에 등록해야 한다.
		.formLogin().disable()
		.httpBasic().disable() // 맨 위에서부터 여기까지 Bearer방식을 쓰기 위한 기본  설정
		.addFilter(new JwtAuthenticationFilter(authenticationManager())) // 로그인시 필요한 파람이 있다 -> AuthenticationManager(UsernameAuthenticationFilter가  매니저를 통해서 로그인을 진행한다.)
		.addFilter(new JwtAuthorizationFilter(authenticationManager(), userRepository)) // 로그인시 필요한 파람이 있다 -> AuthenticationManager(UsernameAuthenticationFilter가  매니저를 통해서 로그인을 진행한다.)
		.authorizeRequests()
		.antMatchers("/api/v1/user/**")
		.access("hasRole('ROLE_USER') or hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
		.antMatchers("/api/v1/manager/**")
		.access("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
		.antMatchers("/api/v1/admin/**")
		.access("hasRole('ROLE_ADMIN')")
		.anyRequest().permitAll();
	}

}
