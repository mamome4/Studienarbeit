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
    public String startDockerContainer(String projectPath, String m2Path, Boolean disableNetwork) throws IOException {
        return dockerService.createJavaContainer(
                projectPath,
                m2Path,
                disableNetwork);
    }

    @GetMapping(path = "python")
    public String startPythonContainer(String image, Boolean pullImage, String projectPath, Boolean disableNetwork, String testPath) throws IOException {
        return dockerService.createPythonContainer(image,
                pullImage,
                projectPath,
                disableNetwork,
                testPath);
    }
}
