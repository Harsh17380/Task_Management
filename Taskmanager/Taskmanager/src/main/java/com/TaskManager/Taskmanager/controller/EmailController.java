package com.TaskManager.Taskmanager.controller;

import com.TaskManager.Taskmanager.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mail")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/send")
    public String testMail() {

        emailService.sendEmail(
                "tempmail2520@gmail.com",
                "CoreQueue Test",
                "Email service working successfully");

        return "Mail Sent";
    }
}
