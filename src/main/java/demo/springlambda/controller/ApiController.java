package demo.springlambda.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello! This is Spring Boot + AWS Lambda demo project.";
    }
}
