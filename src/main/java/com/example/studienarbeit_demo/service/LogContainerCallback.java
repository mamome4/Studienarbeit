package com.example.studienarbeit_demo.service;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kanstantsin Shautsou
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
