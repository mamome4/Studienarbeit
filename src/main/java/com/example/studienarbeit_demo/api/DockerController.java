package com.example.studienarbeit_demo.api;

import com.example.studienarbeit_demo.service.DockerFunctionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("api/v1/startContainer")
@RestController
public class DockerController {

    private final DockerFunctionsService dockerService;

    @Autowired
    public DockerController(DockerFunctionsService dockerService) {
        this.dockerService = dockerService;
    }

    @GetMapping
    public void startDockerContainer() {
        dockerService.createDockerContainer();
    }
}
