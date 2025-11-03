package com.chelv.repository.token;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.chelv.entities.token.RefreshToken;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByToken(String token);

    
}
