package com.example.studienarbeit_demo.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.core.exec.LogContainerCmdExec;
import com.github.dockerjava.core.exec.LogSwarmObjectExec;
import com.github.dockerjava.core.exec.WaitContainerCmdExec;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class DockerFunctionsService {
    DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .withDockerHost("tcp://localhost:2375")
            .build();
    DockerClient dockerClient = DockerClientBuilder
            .getInstance(config)
            .build();

    public static void printResults(Process process) throws IOException {
        BufferedReader reder = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = "";
        while ((line = reder.readLine()) != null) {
            System.out.println((line));
        }
    }

    public void createDockerContainer() {
        /*UUID containerID = UUID.randomUUID();
        String path = "\"C:/Users/Maximilian Meier/IdeaProjects/CucumberTest\"";
        String startCommand = String.format("docker run --name %s -v %s:/usr/src/mymaven -v \"C:/Users/Maximilian Meier/.m2\":/root/.m2 -w /usr/src/mymaven maven:latest mvn test", containerID, path);
        String copyCommand = String.format("docker cp %s:/usr/src/mymaven/target/surefire-reports/ \"C:/Users/Maximilian Meier/IdeaProjects/Studienarbeit_Demo/EvaluationContent\"", containerID);
        String removeCommand = String.format("docker rm %s", containerID);
        System.out.println(startCommand);
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"cmd", "/c", startCommand});
            printResults(process);
            Runtime.getRuntime().exec(new String[]{"cmd", "/c", copyCommand});
            System.out.println(copyCommand);
            Runtime.getRuntime().exec(new String[]{"cmd", "/c", removeCommand});
            System.out.println("Evalutation Finished");
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }*/
        HostConfig hostConfig = HostConfig.newHostConfig()
                .withBinds(
                        new Bind("C:/Users/Maximilian Meier/IdeaProjects/Studienarbeit_Demo/EvaluationContent/CucumberTest/", new Volume("/usr/src/mymaven")),
                        new Bind("C:/Users/Maximilian Meier/.m2", new Volume("/root/.m2")));

        CreateContainerResponse container = dockerClient.createContainerCmd("maven:latest")
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withTty(false)
                .withWorkingDir("/usr/src/mymaven")
                .withNetworkDisabled(false)
                .withHostConfig(hostConfig)
                .withCmd("mvn", "test").exec();
        System.out.println(container.getId());
        dockerClient.startContainerCmd(container.getId()).exec();
        var statusCode = dockerClient.waitContainerCmd(container.getId())
                .exec(new WaitContainerResultCallback())
                .awaitStatusCode();
        System.out.println(statusCode);
        if(statusCode == 0){
            try {
                Runtime.getRuntime().exec(new String[]{"cmd", "/c",
                        String.format("docker cp %s:/usr/src/mymaven/target/surefire-reports/ \"C:/Users/Maximilian Meier/IdeaProjects/Studienarbeit_Demo/EvaluationContent\"", container.getId())});
                System.out.println("worked");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        dockerClient.removeContainerCmd(container.getId()).exec();
    }

    public String createPythonContainer(
            @Nullable String image,
            String projectPath,
            Boolean stdout,
            Boolean stderr,
            Boolean disableNetwork,
            String testsPath
            ) {
        projectPath = "C:/Users/Maximilian Meier/PycharmProjects/StudienarbeitDemo";
        if(image == null) {
            image = "python:latest";
        }
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);

        HostConfig hostConfig = HostConfig.newHostConfig()
                .withBinds(new Bind(projectPath, new Volume("/usr/src/app")));

        CreateContainerResponse container = dockerClient.createContainerCmd(image)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withTty(false)
                .withWorkingDir("/usr/src/app")
                .withNetworkDisabled(false)
                .withHostConfig(hostConfig)
                .withCmd("python", "-m", "unittest", "discover", testsPath)
                .exec();
        System.out.println(container.getId());

        dockerClient.startContainerCmd(container.getId()).exec();

        var statusCode = dockerClient.waitContainerCmd(container.getId())
                .exec(new WaitContainerResultCallback())
                .awaitStatusCode();
        System.out.println(statusCode);

        LogContainerCallback logContainerCallback = new LogContainerCallback();
        LogContainerCmd logContainerCmd = dockerClient.logContainerCmd(container.getId())
                .withStdOut(true)
                .withStdErr(true)
                .withTimestamps(true);

        logContainerCmd.exec(logContainerCallback);
        try {
            logContainerCallback.awaitCompletion(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(logContainerCallback.toString());
        dockerClient.removeContainerCmd(container.getId()).exec();

        return logContainerCallback.toString();
    }

    public void pullImage(String image) {
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
        try {
            pullImageCmd.exec(new PullImageResultCallback()).awaitCompletion();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
