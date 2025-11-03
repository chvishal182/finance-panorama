package com.chelv.service.user;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.chelv.entities.user.UserInfo;
import com.chelv.model.publisher.UserInfoEvent;
import com.chelv.model.user.UserInfoDTO;
import com.chelv.pubsub.publish.eventPublisher.UserInfoProducer;
import com.chelv.repository.user.UserRepository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class EndUserDetailsServiceImplementation implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private UserInfoProducer userInfoProducer;

    public EndUserDetailsServiceImplementation(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserInfo user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("could not found user..!!");
        }

        return new EndUserDetails(user);
    }

    public String getUserByUsername(String userName){
        return Optional.of(userRepository.findByUsername(userName)).map(UserInfo::getUserId).orElse(null);
    }
    
    public Optional<UserInfo> checkIfUserExsists(UserInfoDTO userInfoDTO) {
        return Optional.ofNullable(userRepository.findByUsername(userInfoDTO.getUsername()));
    }

    public Boolean signUpUser(UserInfoDTO userInfoDTO) {
        if (checkIfUserExsists(userInfoDTO).isPresent()) {
            return false;
        }

        userInfoDTO.setPassword(
                passwordEncoder
                        .encode(userInfoDTO.getPassword()));

        System.out.println(userInfoDTO);
        String userId = UUID.randomUUID().toString();
        userRepository.save(
                new UserInfo(
                        userId,
                        userInfoDTO.getUsername(),
                        userInfoDTO.getPassword(),
                        new HashSet<>()));

        userInfoProducer.sendEventToKafka(buildUserInfoEvent(userInfoDTO, userId));
        return true;

    }

    private UserInfoEvent buildUserInfoEvent(UserInfoDTO uDTO, String userId) {
        return UserInfoEvent.builder()
                .userId(userId)
                .firstName(uDTO.getFirstName())
                .lastName(uDTO.getLastName())
                .email(uDTO.getEmail())
                .phoneNumber(uDTO.getPhoneNumber())
                .build();
    }

}
