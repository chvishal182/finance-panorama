package com.chelv.repository.user;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.chelv.entities.UserInfo;
import com.chelv.model.UserInfoDTO;

@Repository
public interface UserRepository extends CrudRepository<UserInfo, String> {   
    
    Optional<UserInfo> findByUserId(String userId);
} 
