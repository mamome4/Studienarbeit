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

    /**
    * Creates a Java Maven container to run Cucumber Tests in with the maven image marked "latest".
    * This function does not pull an image.
    * Therefor there sould already be an image downloaded with the "latest" tag for this function to execute correctly.
    *
    * @Param String projectPath     absolut or relativ path to the project that should be evaluated
    * @Param String m2Path          path to local m2 dependecy repository to prevent the container from having to download dependencies each time
    * @Param Boolean disableNetwork if set the network of the Docker container will be disabled
    * @Param int timeoutMin         sets the timeout for a docker container in minutes, after the timeout the container is stopped and an error message is returned
    * @return String of the terminal output from inside the container
    * Testfiles like surefire-reports can be found in the project directory where they would normaly be created.
    */
    public String createJavaContainer(
            String projectPath,
            String m2Path,
            boolean disableNetwork,
            int timeoutMin
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

        int statusCode;
        try {
            statusCode = dockerClient.waitContainerCmd(container.getId())
                    .exec(new WaitContainerResultCallback())
                    .awaitStatusCode(timeoutMin, TimeUnit.MINUTES);

        } catch (Exception e) {
            dockerClient.killContainerCmd(container.getId()).exec();
            return e.toString();
        }


        String loggedCMD = logImageCMD(container.getId());

        if(statusCode == 0) { dockerClient.removeContainerCmd(container.getId()).exec(); }

        return loggedCMD;
    }

    /**
    * Creates Java Maven container with the option to specify the image and if the image is
    * supposed to be pulled from Docker Hub. If an image is pulled said image has to be
    * a Maven image.
    *
    * @Param String image           specifies the image to be pulled for example: "maven:latest"
    * @Param Boolean pullImage      if set the function will pull the specified image
    * @Param String projectPath     absolut or relativ path to the project that should be evaluated
    * @Param String m2Path          path to local m2 dependecy repository to prevent the container from having to download dependencies each time
    * @Param Boolean disableNetwork if set the network of the Docker container will be disabled
    * @Param int timeoutMin         sets the timeout for a docker container in minutes, after the timeout the container is stopped and an error message is returned
    * @return String of the terminal output from inside the container
    * Testfiles like surefire-reports can be found in the project directory where they would normaly be created.
    */
    public String createJavaContainer(
            String image,
            boolean pullImage,
            String projectPath,
            String m2Path,
            boolean disableNetwork,
            int timeoutMin
    ) {
        if(pullImage) {
            try {
                pullImage(image);
            } catch (Exception e) {
                return e.toString();
            }
        }

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

        int statusCode;
        try {
            statusCode = dockerClient.waitContainerCmd(container.getId())
                    .exec(new WaitContainerResultCallback())
                    .awaitStatusCode(timeoutMin, TimeUnit.MINUTES);

        } catch (Exception e) {
            dockerClient.killContainerCmd(container.getId()).exec();
            return e.toString();
        }

        String loggedCMD = logImageCMD(container.getId());

        if(statusCode == 0) { dockerClient.removeContainerCmd(container.getId()).exec(); }

        return loggedCMD;
    }

    /**
    * Creates Python container to run unittests in with the python image marked "latest". This function does not pull an image.
    * Therefor there sould already be an image downloaded with the "latest" tag for this function to execute correctly.
    * Note that unittests must adhear to python unittest naming conventions.
    *
    * @Param String projectPath     absolut or relativ path to the project that should be evaluated
    * @Param Boolean disableNetwork if set the network of the Docker container will be disabled
    * @Param String testPath        relativ path of the directory containing the unittests within the project, for example: "./tests_unittests"
    * @Param int timeoutMin         sets the timeout for a docker container in minutes, after the timeout the container is stopped and an error message is returned
    * @return String of the terminal output from inside the container
    */
    public String createPythonContainer(
            String projectPath,
            boolean disableNetwork,
            String testsPath,
            int timeoutMin
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

        try {
            dockerClient.waitContainerCmd(container.getId())
                    .exec(new WaitContainerResultCallback())
                    .awaitStatusCode(timeoutMin, TimeUnit.MINUTES);

        } catch (Exception e) {
            dockerClient.killContainerCmd(container.getId()).exec();
            return e.toString();
        }

        String loggedCMD = logImageCMD(container.getId());
        dockerClient.removeContainerCmd(container.getId()).exec();

        return loggedCMD;
    }

    /**
    * Creates Python container with the option to specify the image and if the image is
    * supposed to be pulled from Docker Hub. If an image is pulled said image should be
    * a Python 2.7+ image.
    * Note that unittests must adhear to python unittest naming conventions.
    *
    * @Param String image           specifies the image to be pulled for example: "python:latest" or "python:3.9"
    * @Param Boolean pullImage      if set the function will pull the specified image
    * @Param String projectPath     absolut or relativ path to the project that should be evaluated
    * @Param Boolean disableNetwork if set the network of the Docker container will be disabled
    * @Param String testPath        relativ path of the directory containing the unittests within the project, for example: "./tests_unittests"
    * @Param int timeoutMin         sets the timeout for a docker container in minutes, after the timeout the container is stopped and an error message is returned
    * @return String of the terminal output from inside the container
    */
    public String createPythonContainer(
            String image,
            boolean pullImage,
            String projectPath,
            boolean disableNetwork,
            String testsPath,
            int timeoutMin
    ) {
        if(pullImage) {
            try {
                pullImage(image);
            } catch (Exception e) {
                return e.toString();
            }
        }

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

        try {
            dockerClient.waitContainerCmd(container.getId())
                    .exec(new WaitContainerResultCallback())
                    .awaitStatusCode(timeoutMin, TimeUnit.MINUTES);

        } catch (Exception e) {
            dockerClient.killContainerCmd(container.getId()).exec();
            return e.toString();
        }

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
