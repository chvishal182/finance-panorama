package com.chelv.controller;

import lombok.AllArgsConstructor;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.chelv.entities.token.RefreshToken;
import com.chelv.model.user.UserInfoDTO;
import com.chelv.response.token.JWTResponseDTO;
import com.chelv.service.token.jwt.JWTService;
import com.chelv.service.token.refresh.RefreshTokenService;
import com.chelv.service.user.EndUserDetailsServiceImplementation;

@RestController
@AllArgsConstructor
public class AuthController {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private EndUserDetailsServiceImplementation userDetailsService;

    @PostMapping("auth/v1/signup")
    public ResponseEntity SignUp(@RequestBody UserInfoDTO userInfoDto) {
        try {
            System.out.println(userInfoDto);
            Boolean isSignUped = userDetailsService.signUpUser(userInfoDto);
            if (Boolean.FALSE.equals(isSignUped)) {
                return new ResponseEntity<>("Already Exist", HttpStatus.BAD_REQUEST);
            }
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userInfoDto.getUsername());
            String jwtToken = jwtService.GenerateToken(userInfoDto.getUsername());
            return new ResponseEntity<>(
                    JWTResponseDTO.builder().accessToken(jwtToken).token(refreshToken.getToken()).build(),
                    HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>("Exception in User Service", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String userId = userDetailsService.getUserByUsername(authentication.getName());
            if (Objects.nonNull(userId)) {
                return ResponseEntity.ok(userId);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    }

}