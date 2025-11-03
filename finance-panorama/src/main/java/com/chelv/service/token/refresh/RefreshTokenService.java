package com.chelv.service.token.refresh;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chelv.entities.token.RefreshToken;
import com.chelv.entities.user.UserInfo;
import com.chelv.repository.token.RefreshTokenRepository;
import com.chelv.repository.user.UserRepository;

@Service
public class RefreshTokenService {

    @Autowired UserRepository userRepository;
    @Autowired RefreshTokenRepository refreshTokenRepository;

    public RefreshToken createRefreshToken(String username){
        UserInfo extractedUserInfo = userRepository.findByUsername(username);
        RefreshToken refreshToken  = RefreshToken.builder()
                                                 .userInfo(extractedUserInfo)
                                                 .token(UUID.randomUUID().toString())
                                                 .expiryDate(Instant.now().plusMillis(6_00_00_0))
                                                 .build();
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token){
        if(token.getExpiryDate().compareTo(Instant.now()) < 0){
            refreshTokenRepository.delete(token);
            throw new RuntimeException(token.getToken() + "Refresh token is expired. Please make a new login...!");
        }
        return token;
    }

    public Optional<RefreshToken> findByToken(String token){
        return refreshTokenRepository.findByToken(token);
    }
}
