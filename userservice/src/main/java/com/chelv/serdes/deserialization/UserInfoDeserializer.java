package com.chelv.serdes.deserialization;

import org.apache.kafka.common.serialization.Deserializer;

import com.chelv.model.UserInfoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserInfoDeserializer implements Deserializer<UserInfoDTO> {

    @Override
    public UserInfoDTO deserialize(String arg0, byte[] arg1) {
        ObjectMapper objectMapper = new ObjectMapper();
        UserInfoDTO user = null;
        try {
            user = objectMapper.readValue(arg1, UserInfoDTO.class);
        } catch (Exception e) {
            System.out.println("Cannt Deserialize");
            e.printStackTrace();
        } 

        return user;
    }

}
