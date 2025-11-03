package com.chelv.serdes.serialization;


import com.chelv.model.publisher.UserInfoEvent;
import com.chelv.model.user.UserInfoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

public class UserInfoSerializer implements Serializer<UserInfoEvent>{

    @Override
    public byte[] serialize(String arg0, UserInfoEvent arg1) {
        byte[] returnValue = null;

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            returnValue = objectMapper.writeValueAsString(arg1).getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnValue;

    }

}
