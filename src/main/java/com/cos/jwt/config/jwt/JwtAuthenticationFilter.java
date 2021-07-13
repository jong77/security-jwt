package com.cos.jwt.config.jwt;

import java.io.IOException;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.cos.jwt.config.auth.PrincipalDetails;
import com.cos.jwt.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

// 스프링시큐리티에 UsernamePasswordAuthenticationFilter가 있음 --> id/pw를 쓰는 form기반 인증을 처리하는 필터
// /login 요청해서 username, password전송하면 (post)
// UsernamePasswordAuthenticationFilter가 동작하여 요청을 낚아채서 --> attemptAuthentication 함수를  실행함
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter{
	private final AuthenticationManager authenticationManager;

	// /login 요청을 하면 실행되는 함수
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		System.out.println("JwtAuthenticationFilter : 로그인 시도 중");
		
		// id, password를 받아서 정상인지 로그인 시도를 하면
		try {
//			BufferedReader br = request.getReader();
//			
//			String input = null;
//			while((input = br.readLine())!=null) {
//				System.out.println(input);
//			}
			
			// json데이터를 파싱하는 클래스
			ObjectMapper om = new ObjectMapper();
			// user class에 데이터를 담아준다
			User user = om.readValue(request.getInputStream(), User.class);
			System.out.println(user);
			
			// 유저 정보를 로그인을  시도한다. 폼 로그인은 자동으로 처리하지만 토큰방식은 토큰을 직접 만들어야 한다. 아래는 토큰을 만든것.
			UsernamePasswordAuthenticationToken authenticationToken =
					new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
			
			// authenticationManager에 토큰을 넣어서 던지면 인증을 해준다. 인증이 되면 authentication으로 받는다
			// authentication이 정상적으로 리턴되었다는 것은 DB의 username과 password가 일치한다는 뜻이다.
			// authentication에 담기는 것은 내 로그인 정보가 담긴다.
			// authendication이 실행될 때 PrincipalDetailsService의 loadUserByUsername() 함수가 실행된다.
			Authentication authentication = 
					authenticationManager.authenticate(authenticationToken);
			
			// 인증이 되면 authentication객체는 리턴할 때 session에 저장됨.
			// 굳이 JWT토큰을 사용하면서 세션을 사용할 이유가 없다. 그러나 권한 관리를 security가 대신 해주기 때문에 session에 넣어주는 것이다.
			// 그 다음 리턴을 해줘야 하는데 본 객체가 authentication을 리턴함으로 authentication을 리턴하면 된다
			PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
			System.out.println(principalDetails.getUser().getUsername());  // user정보가 있다는 것은 로그인이 정상적으로 되었다는 것.
			
			return authentication;
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("-------------------------------------------------------");
		// 이 때 authenticationManager로 로그인 시도를 하면 PrincipalDetailsService를 호출하게 됨.
		// 그러면 PrincipalDetailsService의 loadUserByUsername 함수가 자동으로 실행된다.
		// loadUserByUsername함수가 principalDetails를 리턴하게 되는데
		// principalDetails를 세션에 담고 (세션에 담는 이유는 권한관리를 위해서이다. 세션에 값이 있어야 시큐리티가 권한관리를 해준다. 권한 관리를 안하려면 세션에 담을 필요가 없다.)
		// JWT토큰을 만들어서 응답해주면 됨
		//return authentication; try/catch안에 authentication이 만들어졌으므로 그 안으로 이동.
		return null;
	}
	
	// attemptAuthentication실행 후 인증이 정상적으로 완료되었으면 successfulAuthentication함수가 실행된다.
	// 이 함수에서 JWT토큰을 만들어서 request요청한 사용자에게 JWT토큰을 response해주면 됨.
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		System.out.println("successfulAuthentication 실행됨 : 인증이 완료되었다는 뜻임");
		PrincipalDetails principalDetails = (PrincipalDetails) authResult.getPrincipal();
		
		// jwt토큰 만들기
		String jwtToken = JWT.create()
				.withSubject("cos토큰")
				.withExpiresAt(new Date(System.currentTimeMillis()+JwtProperties.EXPIRATION_TIME))
				.withClaim("id", principalDetails.getUser().getId())
				.withClaim("username", principalDetails.getUser().getUsername())
				.sign(Algorithm.HMAC512(JwtProperties.SECRET));
		
		response.addHeader(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX+jwtToken);
		// id/pw로 로그인 정상 처리되면
		// 서버는 세션id를 생성해서 쿠키에 세션id를 생성해서 응답한다.
		// 요청할 때마다 쿠키값에 있는 세션id를 들고 서버쪽으로 요청하기 때문에 
		// 서버는 세션id가 유효한지 판단해서 유효하면 인증이 필요한 페이지로 이동하게 하면 된다.
		
		// jwt는 id/pw로 로그인이 정상처리 되면
		// jwt토큰을 생성해서 클라이언트쪽으로 jwt토큰을 응답한다.
		
		// 클라이언트는 요청할 때마다 jwt토큰을 가지고 요청
		// 서버는 jwt토큰이 유효한지를 판단(필터를 만들어야함)
	}
}
