package com.chelv.pubsub.publish.eventPublisher;

import org.springframework.messaging.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import com.chelv.model.publisher.UserInfoEvent;
import com.chelv.model.user.UserInfoDTO;

@Service
public class UserInfoProducer {

    private final KafkaTemplate<String, UserInfoEvent> kafkaTemplate;

    @Value("${spring.kafka.topic.name}")
    private String TOPIC_NAME;

    @Autowired
    public UserInfoProducer(KafkaTemplate<String, UserInfoEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEventToKafka(UserInfoEvent userInfoDTO){
        Message<UserInfoEvent> message = MessageBuilder
                                            .withPayload(userInfoDTO)
                                            .setHeader(KafkaHeaders.TOPIC, TOPIC_NAME)
                                            .build();
        kafkaTemplate.send(message);
    }

    


}
