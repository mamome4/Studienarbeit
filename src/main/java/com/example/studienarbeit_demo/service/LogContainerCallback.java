package com.example.studienarbeit_demo.service;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kanstantsin Shautsou
 * https://github.com/docker-java/docker-java/blob/a135d21fb308014adfe3ad184557354689f528b1/docker-java/src/test/java/com/github/dockerjava/utils/LogContainerTestCallback.java
 */
public class LogContainerCallback extends ResultCallback.Adapter<Frame> {
    protected final StringBuffer log = new StringBuffer();

    List<Frame> collectedFrames = new ArrayList<>();

    boolean collectFrames = false;

    public LogContainerCallback() {
        this(false);
    }

    public LogContainerCallback(boolean collectFrames) {
        this.collectFrames = collectFrames;
    }

    @Override
    public void onNext(Frame frame) {
        if (collectFrames) collectedFrames.add(frame);
        log.append(new String(frame.getPayload()));
    }

    @Override
    public String toString() {
        return log.toString();
    }


    public List<Frame> getCollectedFrames() {
        return collectedFrames;
    }
}
