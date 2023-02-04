package com.example.studienarbeit_demo.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class DockerFunctionsService {
    DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .withDockerHost("tcp://localhost:2375")
            .build();
    DockerClient dockerClient = DockerClientBuilder
            .getInstance(config)
            .build();

    public String createJavaContainer(
            String projectPath,
            String m2Path,
            Boolean disableNetwork
    ) {
        HostConfig hostConfig = HostConfig.newHostConfig()
                .withBinds(
                        new Bind(projectPath, new Volume("/usr/src/mymaven")),
                        new Bind(m2Path, new Volume("/root/.m2")));

        CreateContainerResponse container = dockerClient.createContainerCmd("maven:latest")
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withWorkingDir("/usr/src/mymaven")
                .withNetworkDisabled(disableNetwork)
                .withHostConfig(hostConfig)
                .withCmd("mvn", "test").exec();
        dockerClient.startContainerCmd(container.getId()).exec();

        var statusCode = dockerClient.waitContainerCmd(container.getId())
                .exec(new WaitContainerResultCallback())
                .awaitStatusCode();

        String loggedCMD = logImageCMD(container.getId());

        if(statusCode == 0) { dockerClient.removeContainerCmd(container.getId()).exec(); }

        return loggedCMD;
    }

    public String createJavaContainer(
            String image,
            Boolean pullImage,
            String projectPath,
            String m2Path,
            Boolean disableNetwork
    ) {
        if(pullImage) { dockerClient.pullImageCmd(image); }
        HostConfig hostConfig = HostConfig.newHostConfig()
                .withBinds(
                        new Bind(projectPath, new Volume("/usr/src/mymaven")),
                        new Bind(m2Path, new Volume("/root/.m2")));

        CreateContainerResponse container = dockerClient.createContainerCmd("maven:latest")
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withWorkingDir("/usr/src/mymaven")
                .withNetworkDisabled(disableNetwork)
                .withHostConfig(hostConfig)
                .withCmd("mvn", "test").exec();
        dockerClient.startContainerCmd(container.getId()).exec();

        var statusCode = dockerClient.waitContainerCmd(container.getId())
                .exec(new WaitContainerResultCallback())
                .awaitStatusCode();

        String loggedCMD = logImageCMD(container.getId());

        if(statusCode == 0) { dockerClient.removeContainerCmd(container.getId()).exec(); }

        return loggedCMD;
    }

    public String createPythonContainer(
            String projectPath,
            Boolean disableNetwork,
            String testsPath
            ) {
        HostConfig hostConfig = HostConfig.newHostConfig()
                .withBinds(new Bind(projectPath, new Volume("/usr/src/app")));

        CreateContainerResponse container = dockerClient.createContainerCmd("python:latest")
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withWorkingDir("/usr/src/app")
                .withNetworkDisabled(disableNetwork)
                .withHostConfig(hostConfig)
                .withCmd("python", "-m", "unittest", "discover", testsPath)
                .exec();

        dockerClient.startContainerCmd(container.getId()).exec();

        var statusCode = dockerClient.waitContainerCmd(container.getId())
                .exec(new WaitContainerResultCallback())
                .awaitStatusCode();
        System.out.println(statusCode);

        String loggedCMD = logImageCMD(container.getId());
        dockerClient.removeContainerCmd(container.getId()).exec();

        return loggedCMD;
    }

    /**
    * Creates Python container with the option to specify the image and if the image is
    * supposed to be pulled from Docker Hub. If an image is pulled said image should be
    * a Python 2.0+ image.
    *
    * @Param String image           specifies the image to be pulled for example: "python:latest" or "python:3.9"
    * @Param Boolean pullImage      if set the function will pull the specified image
    * @Param String projectPath     absolut or relativ path to the project that should be evaluated
    * @Param Boolean disableNetwork if set the network of the Docker container will be disabled
    * @Param String testPath        relativ path of the directory containing the unittests within the project, for example: "./tests_unittests"
    */
    public String createPythonContainer(
            String image,
            Boolean pullImage,
            String projectPath,
            Boolean disableNetwork,
            String testsPath
    ) {
        if(pullImage) { dockerClient.pullImageCmd(image);}

        HostConfig hostConfig = HostConfig.newHostConfig()
                .withBinds(new Bind(projectPath, new Volume("/usr/src/app")));

        CreateContainerResponse container = dockerClient.createContainerCmd(image)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withWorkingDir("/usr/src/app")
                .withNetworkDisabled(disableNetwork)
                .withHostConfig(hostConfig)
                .withCmd("python", "-m", "unittest", "discover", testsPath)
                .exec();

        dockerClient.startContainerCmd(container.getId()).exec();

        dockerClient.waitContainerCmd(container.getId())
            .exec(new WaitContainerResultCallback())
            .awaitStatusCode();

        String loggedCMD = logImageCMD(container.getId());
        dockerClient.removeContainerCmd(container.getId()).exec();

        return loggedCMD;
    }

    private String logImageCMD(String dockerID) {
        LogContainerCallback logContainerCallback = new LogContainerCallback();
        LogContainerCmd logContainerCmd = dockerClient.logContainerCmd(dockerID)
                .withStdOut(true)
                .withStdErr(true)
                .withTimestamps(true);

        logContainerCmd.exec(logContainerCallback);
        try {
            logContainerCallback.awaitCompletion(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return logContainerCallback.toString();
    }

    private void pullImage(String image) throws Exception{
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
        pullImageCmd.exec(new PullImageResultCallback()).awaitCompletion();
    }
}
