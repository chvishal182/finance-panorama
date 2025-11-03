package com.chelv.pubsub.subscribe;

import com.chelv.model.dto.ExpenseInfoDTO;
import com.chelv.utility.ExpenseInfoMapper;
import org.springframework.stereotype.Service;

import com.chelv.model.ExpenseInfoEvent;
import com.chelv.service.ExpenseService;

import org.springframework.kafka.annotation.KafkaListener;

@Service
public class ExpenseServiceConsumer {

    private final ExpenseService expenseService;

    public ExpenseServiceConsumer(ExpenseService expenseService){
        this.expenseService = expenseService;
    }

    @KafkaListener(topics = "${spring.kafka.topic.name}", groupId= "${spring.kafka.consumer.group-id}")
    public void listen(ExpenseInfoEvent expenseInfoEvent){
        try{
            ExpenseInfoDTO expenseInfoDTO = ExpenseInfoMapper.fromEvent(expenseInfoEvent);
            expenseService.createExpense(expenseInfoDTO);
        }catch (Exception e){
            System.out.println("Error in consuming the message from topic:expense_service ");
            e.printStackTrace();
        }
    }
}
