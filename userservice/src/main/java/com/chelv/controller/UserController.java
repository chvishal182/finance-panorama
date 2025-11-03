package com.chelv.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import com.chelv.model.UserInfoDTO;
import com.chelv.service.UserService;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/user/v1/upsert")
    public ResponseEntity<UserInfoDTO> upsert(@RequestBody UserInfoDTO userInfoDTO) {
        try {
            UserInfoDTO user = userService.upsertUser(userInfoDTO);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/users/v1/getUser")
    public ResponseEntity<UserInfoDTO> getUser(@RequestParam("user_id") String userId) {
        try {
            UserInfoDTO user = userService.getUser(userId);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
