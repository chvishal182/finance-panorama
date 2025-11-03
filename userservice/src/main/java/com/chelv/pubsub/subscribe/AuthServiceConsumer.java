package com.chelv.pubsub.subscribe;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.chelv.model.UserInfoDTO;
import com.chelv.model.UserInfoEvent;
import com.chelv.service.UserService;
import com.chelv.utility.UserInfoMapper;

@Service
public class AuthServiceConsumer {

    private final UserService userService;

    public AuthServiceConsumer(UserService userService) {
        this.userService = userService;
    }

    /*
     * private final Counter kafkaEventsCounter;
     * 
     * public AuthServiceConsumer() {
     * kafkaEventsCounter = Counter.build()
     * .name("kafka_events_received_total")
     * .help("Total number of Kafka events received")
     * .register();
     * }
     */

    @KafkaListener(topics = "testing_json", groupId = "userinfo-consumer-group")
    public void listen(UserInfoEvent eventData) {
        try {
            UserInfoDTO dto = UserInfoMapper.fromEvent(eventData);
            // make it transactional
            userService.upsertUser(dto);
        } catch (Exception e) {
            System.out.println("AuthServiceConsumer: Exception is thrown while consuming kafka event");
        }
    }
}
