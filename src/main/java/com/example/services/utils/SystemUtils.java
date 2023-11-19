package com.example.services.utils;

import com.example.utils.UnrecoverableError;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Arrays;

public class SystemUtils {
    public static void execInParent(String[] scriptPath) {
        assert scriptPath.length > 0;
        var modifiedScriptPath = scriptPath.clone();
        var parent = new File(modifiedScriptPath[0]).getParent();
        modifiedScriptPath[0] = Paths.get(modifiedScriptPath[0]).getFileName().toString();
        exec(parent, modifiedScriptPath);
    }

    public static void exec(String[] scriptPath) {
        exec(null, scriptPath);
    }

    public static void exec(String workingDirectory, String[] scriptPath) {
        ProcessBuilder pb = new ProcessBuilder(scriptPath);
        var tag = "";
        if (workingDirectory != null) {
            pb.directory(new java.io.File(workingDirectory));
            tag += workingDirectory + ":" + scriptPath[0];
            System.out.println(tag + ": Working directory set to " + workingDirectory);
        } else {
            tag += scriptPath[0];
        }
        pb.redirectErrorStream(true);

        System.out.println(tag + ": " + "Executing " + Arrays.toString(scriptPath));

        try {
            Process p = pb.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(tag + ": " + line);
            }

            p.waitFor();

            int exitValue = p.exitValue();
            if (exitValue == 0) {
                System.out.println(scriptPath[0] + ": " + "Execution of " + tag + " successful");
            } else {
                System.out.println(scriptPath[0] + ": " + "Execution of " + tag + " failed");
            }

            in.close();

        } catch (IOException e) {
            throw new UnrecoverableError(tag + ": " + "Failed to execute " + Arrays.toString(scriptPath), e);
        } catch (InterruptedException e) {
            throw new UnrecoverableError(tag + ": " + "Script execution interrupted " + Arrays.toString(scriptPath), e);
        }
    }
}
