package ch.unisg.kafka.spring.service;

import ch.unisg.kafka.spring.model.Clicks;
import ch.unisg.kafka.spring.model.Gaze;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
public class ConsumerService {

    private final Logger logger = LoggerFactory.getLogger(getClass());


    @KafkaListener(topics = {"${spring.kafka.gazeEvents-topic}"}, containerFactory = "kafkaListenerJsonFactory", groupId = "group_id")
    public void consumeGazeEvent(Gaze gazeEvent) {

        logger.info("**** -> Consumed gaze event :: {}", gazeEvent);

        // consumed gaze events can be processed here ....

    }

    @KafkaListener(topics = {"${spring.kafka.clickEvents-topic}"}, containerFactory = "kafkaListenerJsonFactory", groupId = "group_id")
    public void consumeClickEvent(Clicks clickEvent) {

        logger.info("**** -> Consumed click event :: {}", clickEvent);

        // consumed click events can be processed here ....

    }

}
