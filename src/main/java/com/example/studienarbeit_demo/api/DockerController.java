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

    @GetMapping(path = "java")
    public String startJavaContainer(String projectPath, String m2Path, boolean disableNetwork, int timeoutMin) {
        return dockerService.createJavaContainer(
                projectPath,
                m2Path,
                disableNetwork,
                timeoutMin);
    }
    @GetMapping(path = "javaPull")
    public String startJavaPullContainer(String image, boolean pullImage, String projectPath, String m2Path, boolean disableNetwork, int timeoutMin) {
        return dockerService.createJavaContainer(
                image,
                pullImage,
                projectPath,
                m2Path,
                disableNetwork,
                timeoutMin);
    }
    @GetMapping(path = "python")
    public String startPythonContainer(String projectPath, boolean disableNetwork, String testPath, int timeoutMin) {
        return dockerService.createPythonContainer(
                projectPath,
                disableNetwork,
                testPath,
                timeoutMin);
    }
    @GetMapping(path = "pythonPull")
    public String startPythonPullContainer(String image, boolean pullImage, String projectPath, boolean disableNetwork, String testPath, int timeoutMin) {
        return dockerService.createPythonContainer(
                image,
                pullImage,
                projectPath,
                disableNetwork,
                testPath,
                timeoutMin);
    }
}
