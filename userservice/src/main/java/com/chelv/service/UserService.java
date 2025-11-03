package com.chelv.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.chelv.entities.UserInfo;
import com.chelv.model.UserInfoDTO;
import com.chelv.repository.user.UserRepository;
import com.chelv.utility.UserInfoMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    
    private final UserRepository userRepository;

    private final RedisTemplate<String, UserInfoDTO> redisTemplate;

    private static final String CACHE_PREFIX = "user:";

    /*
     * @Transactional
     * public UserInfoDTO upsertUser(UserInfoDTO userInfoDTO) {
     * UnaryOperator<UserInfo> updateUser = user -> {
     * user.setFirstName(userInfoDTO.getFirstName());
     * user.setLastName(userInfoDTO.getLastName());
     * user.setEmail(userInfoDTO.getEmail());
     * user.setPhoneNumber(userInfoDTO.getPhoneNumber());
     * user.setProfilePicture(userInfoDTO.getProfilePicture());
     * return userRepository.save(user);
     * };
     * 
     * Supplier<UserInfo> createUser = () -> {
     * return userRepository
     * .save(UserInfoMapper.toEntity(userInfoDTO));
     * };
     * 
     * UserInfo userInfo = userRepository
     * .findByUserId(userInfoDTO.getUserId())
     * .map(updateUser)
     * .orElseGet(createUser);
     * 
     * return UserInfoMapper.toDTO(userInfo);
     * 
     * }
     */

    @Transactional
    public UserInfoDTO upsertUser(UserInfoDTO userInfoDTO) {
        // Check if user exists
        UserInfo userInfo = userRepository.findByUserId(userInfoDTO.getUserId())
                .map(existingUser -> {
                    // Update fields
                    existingUser.setFirstName(userInfoDTO.getFirstName());
                    existingUser.setLastName(userInfoDTO.getLastName());
                    existingUser.setEmail(userInfoDTO.getEmail());
                    existingUser.setPhoneNumber(userInfoDTO.getPhoneNumber());
                    existingUser.setProfilePicture(userInfoDTO.getProfilePicture());
                    return existingUser;
                })
                .orElseGet(() -> {
                    // Create new user
                    return UserInfoMapper.toEntity(userInfoDTO);
                });

        // Save in DB
        UserInfo savedUser = userRepository.save(userInfo);

        // Update Redis cache
        String cacheKey = CACHE_PREFIX + savedUser.getUserId();
        redisTemplate.opsForValue().set(cacheKey, UserInfoMapper.toDTO(savedUser));

        return UserInfoMapper.toDTO(savedUser);
    }

    public UserInfoDTO getUser(String userId) throws Exception {

        String cacheKey = CACHE_PREFIX + userId;

        UserInfoDTO cachedUser = redisTemplate.opsForValue().get(cacheKey);
        System.out.println(cachedUser);
        if (cachedUser != null) {
            return cachedUser;
        }

        Optional<UserInfo> userInfoOptional = userRepository
                .findByUserId(userId);

        if (userInfoOptional.isEmpty()) {
            throw new Exception("User not found");
        }

        UserInfo userInfo = userInfoOptional.get();

        redisTemplate.opsForValue().set(cacheKey, UserInfoMapper.toDTO(userInfo));

        return UserInfoMapper.toDTO(userInfo);
    }
}
