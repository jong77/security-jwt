package com.cos.jwt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cos.jwt.model.User;

// CRUD 함수를 JpaRepository가 들고 있음
// @Repository라는 어노테이션이 없어도 IoC가 된다. 이유는 JpaRepository를 상속했기 때문.... 
// JpaRepository를 상속한 UserRepository는 bean으로 자동 등록된다.
public interface UserRepository extends JpaRepository<User, Long>{
	public User findByUsername(String username);
}
