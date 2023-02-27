# lab03Part2 - Kafka-Spring with The Eye Tracking Use-Case

- This lab is based on _Lab03Part1-kafka-spring_ (https://github.com/scs-edpo/lab03-kafka-spring) and _Lab02Part2kafka-EyeTracking_ (https://github.com/scs-edpo/lab02Part2-kafka-EyeTracking). 
- We extend _[Lab03Part1-kafka-spring](https://github.com/scs-edpo/lab03-kafka-spring)_ with the eye tracking use case introduced in _[Lab02Part2kafka-EyeTracking](https://github.com/scs-edpo/lab02Part2-kafka-EyeTracking)_. Herein, we adapt the existing source-code to implement the eye tracking and clicks tracking producers and consumers using Kafka and Spring
- The procedure to run the code is similar to _Lab03Part1-kafka-spring_. We recommend importing the project to  IntelliJ and let the IDE handle everything
- Note that only the new procedures are described in this lab

## Running the Lab

The root folder has the [docker-compose.yml](docker-compose.yml) file with the Kafka configuration from labs 01 and 02.

Simply run 
  ```
  $ docker-compose up
  ```
to start Kafka.

You can run the main java file [SpringBootKafkaApplication.java](/src/main/java/ch/unisg/kafka/spring/SpringBootKafkaApplication.java) of the Spring Boot project directly via your IDE
or you can run the project via Maven, e.g., by running
  ```
  $ mvn spring-boot:run
  ```
on the Terminal.

### Code Snippets

1. #### Maven Dependencies
Same as in _[Lab03Part1-kafka-spring](https://github.com/scs-edpo/lab03-kafka-spring)_

2. #### Properties file
   Some properties are in the **[application.yml](src/main/resources/application.yml)** file, e.g., bootstrap servers, group id and topics.  
   Here we have two topics to publish and consume data.

   > gazeEvents-topic (for gaze events) 
   
   >clickEvents-topic (for click events)

   **[src/main/resources/application.yml](src/main/resources/application.yml)**
     ```
     spring:
       kafka:
         consumer:
           bootstrap-servers: localhost:9092
           group-id: group_id
   
         producer:
           bootstrap-servers: localhost:9092
   
         gazeEvents-topic: gazeEvents-topic
         clickEvents-topic: clickEvents-topic
     ```

3. #### Model classes
    
   We have two model classes: **Gaze** and **Clicks**

   Both model classes will be used to create objects that will publish to the aferomentioned Kafka topics (gazeEvents-topic, clickEvents-topic) using `KafkaTemplate` and consume using `@KafkaListener` from either of the topics.  
   **[ch.unisg.kafka.spring.model.Gaze.java](src/main/java/ch/unisg/kafka/spring/model/Gaze.java)**
    ```Java
   public class Gaze implements Serializable {

    int eventID;
    long timestamp;
    int xPosition; // position of the gaze within the x-coordinate of the screen
    int yPosition; // position of the gaze within the y-coordinate of the screen
    int pupilSize; // size of the eye pupil as captured by the eye-tracker
   
    // Constructor, Getter and Setter
   
   }
    ```
   **[ch.unisg.kafka.spring.model.Clicks.java](src/main/java/ch/unisg/kafka/spring/model/Clicks.java)**
    ```Java
   public class Clicks implements Serializable {

    int eventID;
    long timestamp;
    int xPosition; // position of the click within the x-coordinate of the screen
    int yPosition; // position of the click within the y-coordinate of the screen
    String clickedElement;

    // Constructor, Getter and Setter
   
   }
    ```

4. #### Kafka Configuration
Same as in _[Lab03Part1-kafka-spring](https://github.com/scs-edpo/lab03-kafka-spring)_

5. #### Rest Controller

The Rest API Controller in **[ch.unisg.kafka.spring.controllers.RestController.java](src/main/java/ch/unisg/kafka/spring/controllers/RestController.java)** allows for 2 types of GET requests allowing respectively to start the eye tracking and click tracking producers as shown in the following code fragments (see endpoints in Section [API Endpoints](###API Endpoints)):

**For eye tracking**
```Java
@GetMapping(value = "/eyeTracking")
public String eyeTrackingCall(@RequestParam("action") String action) {
        String output = "";

        if(action.equals("start") & !singleEyeTrackingThreadStart) {
        // starting eye-tracking in a new thread if not already started
        Thread eyeTrackingThread =  new Thread(() -> {
        producerService.startEyeTracker();
        });
        eyeTrackingThread.start();
        singleEyeTrackingThreadStart = true;
        output = "Eye-tracker successfully started!";
        }
        else if(singleEyeTrackingThreadStart) {
        output = "Eye-tracker already running";
        }
        else {
        output = "Unknown action";
        }

        return output;
        }

```
        
**For clicks tracking**
```Java
@GetMapping(value = "/clickTracking")
public String clickTrackingCall(@RequestParam("action") String action) {
        String output = "";

        if(action.equals("start") & !singleClickTrackingThreadStart) {
        // starting clicks tracking in a new thread if not already started
        Thread clickTrackingThread =  new Thread(() -> {
        producerService.startClicksTracker();
        });
        clickTrackingThread.start();
        singleClickTrackingThreadStart = true;
        output = "Started recording user clicks!";
        }
        else if(singleClickTrackingThreadStart) {
        output = "the recording of users clicks is already running";
        }
        else {
        output = "Unknown action";
        }

        return output;
        }

```


6. #### Publishing Messages to Kafka Topics
 - In the **[ch.unisg.kafka.spring.service.ProducerService.java](src/main/java/ch/unisg/kafka/spring/service/ProducerService.java)**, two topics are defined
 
 ```Java
    @Value("${spring.kafka.gazeEvents-topic}")
    private String gazeEventsTopic;

    @Value("${spring.kafka.clickEvents-topic}")
    private String clickEventsTopic;
    
```

 - In addition, two `KafkaTemplate`s used for sending to gaze and click events are autowired.

```Java
    @Autowired
    private KafkaTemplate<String, Gaze> kafkaTemplateGaze;

    @Autowired
    private KafkaTemplate<String, Clicks> kafkaTemplateClick;
    
```
   
 - The gaze and clicks events are generated in the same way as explained in _[Lab02Part2kafka-EyeTracking](https://github.com/scs-edpo/lab02Part2-kafka-EyeTracking)_ 
 - These events are published following the same procedure as explained in _[Lab03Part1-kafka-spring](https://github.com/scs-edpo/lab03-kafka-spring)_
   

 - The procedure for generating gaze events and sending them:
     
   ```Java
     // Define a counter which will be used as an eventID
     int counter = 0;

     while(true) {

            // sleep for 8 ms
            try {
                Thread.sleep(8);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // generate a random gaze event using constructor  Gaze(int eventID, long timestamp, int xPosition, int yPosition, int pupilSize)
            Gaze gazeEvent = new Gaze(counter, System.nanoTime(), getRandomNumber(0, 1920), getRandomNumber(0, 1080), getRandomNumber(3, 4));
            //log
            logger.info("#### -> Publishing gaze event :: {}", gazeEvent);
            // send gaze event
            kafkaTemplateGaze.send(gazeEventsTopic, gazeEvent);

            counter++;
     }
   ```

 - The procedure for generating click events and sending them:

   ```Java
 
        // Define a counter which will be used as an eventID
        int counter = 0;

        while(true) {

            // sleep for a random time interval between 500 ms and 5000 ms
            try {
                Thread.sleep(getRandomNumber(500, 5000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // generate a random click event using constructor  Clicks(int eventID, long timestamp, int xPosition, int yPosition, String clickedElement)
            Clicks clickEvent = new Clicks(counter,System.nanoTime(), getRandomNumber(0, 1920), getRandomNumber(0, 1080), "EL"+getRandomNumber(1, 20));

            // send gaze event
            kafkaTemplateClick.send(clickEventsTopic, clickEvent);

            logger.info("#### -> Publishing click event :: {}",clickEvent);

            // increment counter i.e., eventID
            counter++;

        }

   ```

7. #### Consuming Messages from a Kafka Topic
 - In the **ch.unisg.kafka.spring.service.ConsumerService.java** class, we are consuming messages from topics using the `@KafkaListener` annotation.
 - The approach is the same as the one explained in _[Lab03Part1-kafka-spring](https://github.com/scs-edpo/lab03-kafka-spring)_


 - Gaze events consumer
    ```Java
   @KafkaListener(topics = {"${spring.kafka.gazeEvents-topic}"}, containerFactory = "kafkaListenerJsonFactory", groupId = "group_id")
   public void consumeGazeEvent(Gaze gazeEvent) {

       logger.info("**** -> Consumed gaze event :: {}", gazeEvent);

       // consumed gaze events can be processed here ....

   }
     ```
 - Clicks events consumer
     ```Java
     @KafkaListener(topics = {"${spring.kafka.clickEvents-topic}"}, containerFactory = "kafkaListenerJsonFactory", groupId = "group_id")
     public void consumeClickEvent(Clicks clickEvent) {

         logger.info("**** -> Consumed click event :: {}", clickEvent);

         // consumed click events can be processed here ....

     }
     ```
  
###API Endpoints

> Starting eye tracking: **GET Mapping** http://localhost:8080/kafka/eyeTracking?action=start

> Starting clicks tracking: **GET Mapping** http://localhost:8080/kafka/clickTracking?action=start
