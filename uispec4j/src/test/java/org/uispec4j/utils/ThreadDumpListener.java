package org.uispec4j.utils;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class ThreadDumpListener implements ITestListener {


    public ThreadDumpListener() {

    }

    private ThreadDumpListener(int timeOut) {
        this();
        System.out.println("thread dump will trigger in " + timeOut + " millis");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                killSelf();
            }
        }, timeOut);
    }

    private static ThreadDumpListener theInstance = null;

    private static synchronized ThreadDumpListener scheduleThreadDumpIfEnabled() {
        int timeOut = getTimeoutParameter();
        if (theInstance == null && timeOut != 0)
            theInstance = new ThreadDumpListener(timeOut);
        return theInstance;
    }

    private static int getTimeoutParameter() {
        try {
            return Integer.parseInt(System.getProperty("threadDumpTimeout", "0"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void killSelf() {
        Vector<String> commands = new Vector<String>();
        commands.add("/bin/bash");
        commands.add("-c");
        commands.add("kill -3 $PPID");
        ProcessBuilder pb = new ProcessBuilder(commands);

        try {
            Process pr = pb.start();
            pr.waitFor();
            if (pr.exitValue() == 0) {
                BufferedReader outReader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                String line = outReader.readLine();
                do {
                    System.out.println("Kill: " + line);
                    line = outReader.readLine();
                } while (line != null);
            } else {
                System.err.println("Error while getting sending kill");
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        scheduleThreadDumpIfEnabled();
    }

    @Override
    public void onTestSuccess(ITestResult result) {

    }

    @Override
    public void onTestFailure(ITestResult result) {

    }

    @Override
    public void onTestSkipped(ITestResult result) {

    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {

    }

    @Override
    public void onStart(ITestContext context) {

    }

    @Override
    public void onFinish(ITestContext context) {

    }
}