package com.example.studienarbeit_demo.api;

import com.example.studienarbeit_demo.service.DockerFunctionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public String startDockerContainer() throws IOException {
        return dockerService.createJavaContainer(
                "C:/Users/Maximilian Meier/IdeaProjects/Studienarbeit_Demo/EvaluationContent/CucumberTest/",
                "C:/Users/Maximilian Meier/.m2",
                false);
    }

    @GetMapping(path = "python")
    public String startPythonContainer() throws IOException {
        return dockerService.createPythonContainer("python:latest",
                true,
                "C:/Users/Maximilian Meier/PycharmProjects/StudienarbeitDemo",
                false,
                "./tests_unittests");
    }
}
