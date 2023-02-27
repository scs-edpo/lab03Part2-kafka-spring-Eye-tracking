package ch.unisg.kafka.spring.controllers;

import ch.unisg.kafka.spring.model.Gaze;
import ch.unisg.kafka.spring.service.ProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@org.springframework.web.bind.annotation.RestController
@RequestMapping(value = "/kafka")
public class RestController {

    Boolean singleEyeTrackingThreadStart = false;
    Boolean singleClickTrackingThreadStart = false;

    @Autowired
    private ProducerService<Gaze> producerService;

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

    @GetMapping(value = "/clickTracking")
    public String ClickTrackingCall(@RequestParam("action") String action) {
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






}