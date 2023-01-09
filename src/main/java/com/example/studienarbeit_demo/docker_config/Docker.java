package com.example.studienarbeit_demo.docker_config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;

public class Docker {
    DefaultDockerClientConfig.Builder config
            = DefaultDockerClientConfig.createDefaultConfigBuilder();
    DockerClient dockerClient = DockerClientBuilder
            .getInstance()
            .build();
}
