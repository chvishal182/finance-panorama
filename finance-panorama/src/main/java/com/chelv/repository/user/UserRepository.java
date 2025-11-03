package com.chelv.repository.user;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.chelv.entities.user.UserInfo;

@Repository
public interface UserRepository extends CrudRepository<UserInfo, String>{

    public UserInfo findByUsername(String username);

}
