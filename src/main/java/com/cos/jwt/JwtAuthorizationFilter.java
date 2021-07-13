package com.cos.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.cos.jwt.config.auth.PrincipalDetails;
import com.cos.jwt.config.jwt.JwtProperties;
import com.cos.jwt.model.User;
import com.cos.jwt.repository.UserRepository;

// 시큐리티는 필터를 가지고 있는데 그 필터중에 BasicAuthenticationFilter라는 것이 있음
// 권한이나 인증이 필요한 특정 주소를 요청했을 때 위 필터를 무조건 타게 되어 있음
// 만약 권한이나 인증이 필요하지 않은 요청은 이 필터를 타지 않음
public class JwtAuthorizationFilter extends BasicAuthenticationFilter{

	private UserRepository userRepository;
	
	public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserRepository userRepository) {
		super(authenticationManager);
		this.userRepository = userRepository;
	}
	
	// 인증이나 권한이 필요한 요청이 있을 때 이 필터를 타게 된다
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		//super.doFilterInternal(request, response, chain); 주석처리  하지 않으면 doFilterInternal가 실행되면서  응답을 한 번 더하게 된다. 
		System.out.println("인증이나 권한이 필요한 주소 요청이 됨.");
		
		String jwtHeader = request.getHeader("Authorization");
		System.out.println("jwtHeader : " + jwtHeader);
		
		// header값 확인
		if(jwtHeader == null || !jwtHeader.startsWith(JwtProperties.TOKEN_START_STRING)) {
			chain.doFilter(request, response);
			return;
		}
		
		// 토큰이 날아왔으면 JWT토큰을 검증해서 정상적인 사용자인지 확인
		String jwtToken = request.getHeader("Authorization").replace(JwtProperties.TOKEN_PREFIX, "");
		
		// .verify는 서명하는 것. 서명이 정상적으로 되었으면 username이 들어오게 됨.
		String username = 
				JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(jwtToken).getClaim("username").asString();
		
		// username이 null이 아니면 서명이 정상적으로 됨
		if(username!=null) {
			User userEntity = userRepository.findByUsername(username);
			
			PrincipalDetails principalDetails = new PrincipalDetails(userEntity);
			
			// 로그인 진행 없이 토큰 인증으로 authentication객체를 만듦.
			// JWT토큰 서명을 통해서 서명이 정상이면 Authentication객체를 만들어준다.
			Authentication authentication = 
					new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
			
			// 시큐리티를 저장할 수 있는 세션공간을 찾은것이다.
			//강제로 시큐리티의 세션에 접근하여 Authentication객체를 저장
			SecurityContextHolder.getContext().setAuthentication(authentication);
			
			// 마지막으로 다시 필터를 타게 만들면 된다.
			chain.doFilter(request, response);
		}
	}

}
