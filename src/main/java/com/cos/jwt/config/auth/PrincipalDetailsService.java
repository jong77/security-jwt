package com.cos.jwt.config.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cos.jwt.model.User;
import com.cos.jwt.repository.UserRepository;

import lombok.RequiredArgsConstructor;

//로그인 요청시 실행됨 (localhost:8080/login, 스프링시큐리티의 로그인 요청주소는 /login이다) form로그인을 쓰지 않기로 했으므로 /login에서 동작하지 않는다.
@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService{

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		System.out.println("PrincipalDetailService의 loadUserByUsername");
		User userEntity = userRepository.findByUsername(username);
		return new PrincipalDetails(userEntity);
	}
	
	
}
