package com.cos.jwt.filter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cos.jwt.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MyFilter1 implements Filter{

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
//		System.out.println("필터1");
//		// json데이터를 파싱하는 클래스
//		ObjectMapper om = new ObjectMapper();
//		// user class에 데이터를 담아준다
//		User user = om.readValue(request.getInputStream(), User.class);
//		System.out.println(user);
		chain.doFilter(request, response);
	}

}
