package com.example.demo.utils;

import com.example.demo.controller.PanelController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RunShellCommandFromJava {
    Process process;
    private static final Logger logger = LoggerFactory.getLogger(RunShellCommandFromJava.class);
    ProcessBuilder processBuilder = new ProcessBuilder();
    private int currentGifDelay = 0;
    private boolean gifRunning = false;

    public void destroyCmd() {
        if(process!=null && process.isAlive()){
            process.destroy();
        }
    }
    public void runShCmd(String pathToShFile) {
        if (OSValidator.isWindows()) {
        } else {
            processBuilder.command(pathToShFile);
        }
        runProcess();
    }
    public void clearScreen(String blankFilePath, List<String> devices) {
        for (String device : devices) {
            processBuilder.command("bash", "-c", "cat " + blankFilePath + " > /dev/" + device);
            logger.error("\n\n\n\n\nbash ran : cat " + blankFilePath + " > /dev/" + device + "\n\n\n\n\n");
            runProcess();
            destroyCmd();
        }
    }
    public void runCmdForImage(String filePath, String deviceName) {
        if (OSValidator.isWindows()) {
        } else {
            logger.error("\n\n\n\n\nbash ran : cat " + filePath + " > /dev/" + deviceName + "\n\n\n\n\n");
            processBuilder.command("bash", "-c", "cat " + filePath + " > /dev/" + deviceName);
            runProcess();
        }
    }
    public void runCmdForGif(String fileName,String filePath, String deviceName) throws IOException {
        List<String> gifFrames = new ArrayList<>();
        if (OSValidator.isWindows()) {
        } else {
            final GifDecoder.GifImage gif = GifDecoder.read(filePath.getBytes(StandardCharsets.UTF_8));
            final int width = gif.getWidth();
            final int height = gif.getHeight();
            final int background = gif.getBackgroundColor();
            final int frameCount = gif.getFrameCount();
            for (int i = 0; i < frameCount; i++) {
                final BufferedImage img = gif.getFrame(i);
                final int delay = gif.getDelay(i);
                currentGifDelay = delay;
                File frame = new File( fileName +"_frame_" + i + ".png");
                ImageIO.write(img, "png", frame );
                gifFrames.add(frame.getAbsolutePath());
            }
            gifRunning = true;
            while (gifRunning){
                for (String gifFrame : gifFrames) {
                    processBuilder.command("bash", "-c", "cat " + gifFrame + " > /dev/" + deviceName);
                    runProcess();
                }
            }
        }
    }
    private void runProcess(){
        try {
            process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }
            int exitVal = process.waitFor();
            if (exitVal == 0) {
                System.out.println("Success!");
                System.out.println(output);
            } else {
                //abnormal...
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
