package com.example.studienarbeit_demo.api;

import com.example.studienarbeit_demo.service.DockerFunctionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


@RequestMapping("api/v1/startContainer")
@RestController
public class DockerController {

    private final DockerFunctionsService dockerService;

    @Autowired
    public DockerController(DockerFunctionsService dockerService) {
        this.dockerService = dockerService;
    }

    @GetMapping(path = "java")
    public void startDockerContainer() throws IOException {
        dockerService.createDockerContainer();
    }

    @GetMapping(path = "python")
    public String startPythonContainer() throws IOException {
        String result = dockerService.createPythonContainer("python:latest",
                "C:/Users/Maximilian Meier/PycharmProjects/StudienarbeitDemo",
                true,
                true,
                false,
                "./tests_unittests");
        return result;
    }

    @GetMapping(path = "image")
    public void pullImage(@RequestParam String image) throws IOException {
        dockerService.pullImage(image);
    }
}
