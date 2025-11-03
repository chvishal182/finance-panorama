package com.chelv.serdes.deserialization;

import com.chelv.model.ExpenseInfoEvent;
import com.chelv.model.dto.ExpenseInfoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class ExpenseDeserializer implements Deserializer<ExpenseInfoEvent> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Deserializer.super.configure(configs, isKey);
    }

    @Override
    public ExpenseInfoEvent deserialize(String s, byte[] bytes) {
        ObjectMapper objectMapper = new ObjectMapper();
        ExpenseInfoEvent expenseInfoEvent = null;

        try{
            expenseInfoEvent = objectMapper.readValue(bytes, ExpenseInfoEvent.class);
        }catch(Exception e){
            System.out.println("Error occured during the deserialization of data from kafka");
            e.printStackTrace();
        }

        return expenseInfoEvent;
    }

    @Override
    public void close() {
        Deserializer.super.close();
    }
}
