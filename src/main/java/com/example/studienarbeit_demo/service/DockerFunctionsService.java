package com.example.studienarbeit_demo.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

@Service
public class DockerFunctionsService {

    public static void printResults(Process process) throws IOException {
        BufferedReader reder = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = "";
        while ((line = reder.readLine()) != null) {
            System.out.println((line));
        }
    }

    public void createDockerContainer(){
        UUID containerID = UUID.randomUUID();
        String path = "\"C:/Users/Maximilian Meier/IdeaProjects/CucumberTest\"";
        String startCommand = String.format("docker run --name %s -v %s:/usr/src/mymaven -w /usr/src/mymaven maven:latest mvn test", containerID, path);
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
        }
    }
}
