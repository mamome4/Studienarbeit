package com.example.studienarbeit_demo.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Binds;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;

import java.net.URL;
import java.util.UUID;

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
        dockerClient.killContainerCmd(container.getId()).exec();
    }

    public void createDockerContainerHttpRequest() {
        URL url = null;
        try {
            url = new URL("http://localhost:2375/containers/create");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        String jsonInputString = "{\n" +
                "    \"AttachStdout\": true,\n" +
                "    \"AttachStderr\": true,\n" +
                "    \"Tty\": false,\n" +
                "    \"Image\": \"maven:latest\",\n" +
                "    \"WorkingDir\": \"/usr/src/mymaven\",\n" +
                "    \"NetworkDisabled\": false,\n" +
                "    \"HostConfig\": {\n" +
                "        \"Binds\": [\n" +
                "            \"C:/Users/Maximilian Meier/IdeaProjects/Studienarbeit_Demo/EvaluationContent/CucumberTest/:/usr/src/mymaven\",\n" +
                "            \"C:/Users/Maximilian Meier/.m2:/root/.m2\"\n" +
                "        ]\n" +
                "    },\n" +
                "    \"Cmd\": [\n" +
                "        \"mvn\",\n" +
                "        \"test\"\n" +
                "    ]\n" +
                "}";
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection)url.openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        try(OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
