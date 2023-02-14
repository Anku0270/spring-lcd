package com.example.demo.service;

import com.example.demo.model.DeviceType;
import com.example.demo.utils.RunShellCommandFromJava.GifDecoder.GifFrame;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@NoArgsConstructor
public class SerialLoopService {
    private static final Logger logger = LoggerFactory.getLogger(SerialLoopService.class);
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    @Autowired
    protected SerialCommunication serialCommunication;
    private volatile InputStream currentInputStream;
    private List<GifFrame> gifFrames;
    private int defaultDelay = 40;
    private volatile boolean isPaused;
    private volatile boolean replayGif;
    private int GIFCOUNT = 0;

    public SerialLoopService(DeviceType device) {
        serialCommunication = new SerialCommunication(device);
    }

    public void setCurrentInputStream(InputStream inputStream) {
        this.currentInputStream = inputStream;
    }

    public void setDefaultDelay(int delay) {
        this.defaultDelay = delay;
    }

    public void pause() {
        this.replayGif = false;
        this.isPaused = true;
    }

    public void resume(Boolean replayGif) {
        this.isPaused = false;
        this.replayGif = replayGif;
        // Incase executorService is not running, we will restart it
        if(executorService.isShutdown()){
            start(replayGif);
        }
    }

    public void setGifFrames(List<GifFrame> gifFrames) {
        this.gifFrames = gifFrames;
    }

    public void sendImageOnly(InputStream is) {
        serialCommunication.runSerial(is);
    }

    public void start(boolean replayGif) {
        this.replayGif = replayGif;
        try {
            if(executorService.isShutdown()){
                executorService = Executors.newScheduledThreadPool(1);
            }
            logger.info("executorService isShutdown() : " + executorService.isShutdown());
            executorService.scheduleWithFixedDelay(this::myTask, 0, defaultDelay, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException("scheduleWithFixedDelay ERROR : " + e);
        }
    }

    public void stop() {
        executorService.shutdown();
        serialCommunication.clearScreen();
    }

    @SneakyThrows
    private synchronized void myTask() {
        if (!isPaused) {
            if (replayGif) {
                currentInputStream = gifFrames.get(GIFCOUNT).inputStream;
                serialCommunication.runSerial(currentInputStream);
                long extraDelay = gifFrames.get(GIFCOUNT).delay - defaultDelay;
                if (extraDelay > 0) {
                    wait(extraDelay + 100L);
                }
                if (GIFCOUNT == gifFrames.size() - 1) {
                    GIFCOUNT = 0;
                    logger.info("RESET GIFCOUNT");
                } else {
                    GIFCOUNT++;
                }
                logger.info("Replaying Gif COUNT : " + GIFCOUNT + "/" + gifFrames.size());
            } else {
                logger.info("currentInputStream");
                serialCommunication.runSerial(currentInputStream);
            }
        } else {
            logger.info("myTask paused");
        }
        logger.info("\nisPaused : " + isPaused +
                "\n replayGif : " + replayGif);
    }
}
