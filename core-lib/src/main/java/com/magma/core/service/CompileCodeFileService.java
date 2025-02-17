package com.magma.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Scanner;

@Service
public class CompileCodeFileService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompileCodeFileService.class);

    public Boolean compileCodeFile(String fileName, String fileContent) {
        try {
            File tempDir = new File(System.getProperty("user.dir"));
            File javaFile = new File(tempDir, fileName);

            // Write the file content to the temporary Java file
            try (FileWriter writer = new FileWriter(javaFile)) {
                writer.write(fileContent);
            } catch (IOException e) {
                return false;
            }

            // Compile the Java file using 'javac'
            ProcessBuilder compileProcess = new ProcessBuilder("javac", javaFile.getAbsolutePath());
            compileProcess.directory(tempDir);
            compileProcess.redirectErrorStream(true);

            Process compile = compileProcess.start();

            InputStream compileInputStream = compile.getInputStream();
            String compileOutput = readStream(compileInputStream);
            int compileResult = compile.waitFor();

            if (compile.isAlive()) {
                LOGGER.debug("Compiler status: alive");
                compile.destroy();
            } else {
                LOGGER.debug("Compiler status: dead");
            }

            return compileResult == 0;
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Error compileCodeFile: ", e);

            return false;
        }
    }

    public String runDecoderFile(String fileName, String fileContent, Object payloadObj) {
        try {
            File tempDir = new File(System.getProperty("user.dir"));
            File javaFile = new File(tempDir, fileName);

            // Write the file content to the temporary Java file
            try (FileWriter writer = new FileWriter(javaFile)) {
                writer.write(fileContent);
            } catch (IOException e) {
                LOGGER.error("Error writing the Java file: " + e.getMessage());
                return "Error writing the Java file";
            }

            // Compile the Java file using 'javac'
            ProcessBuilder compileProcess = new ProcessBuilder("javac", javaFile.getAbsolutePath());
            compileProcess.directory(tempDir);
            compileProcess.redirectErrorStream(true);

            Process compile = compileProcess.start();

            InputStream compileInputStream = compile.getInputStream();
            String compileOutput = readStream(compileInputStream);
            int compileResult = compile.waitFor();

            if (compile.isAlive()) {
                LOGGER.debug("Compiler status: alive");
                compile.destroy();
            } else {
                LOGGER.debug("Compiler status: dead");
            }

            // Handle the compilation result
            if (compileResult == 0) {
                // Compilation was successful

                // Extract the class name from the file name (remove ".java" extension)
                String className = fileName.replace(".java", "");

                // Load the compiled class using reflection
                try {
                    // Load the class using the system ClassLoader
                    ClassLoader classLoader = URLClassLoader.newInstance(new URL[]{tempDir.toURI().toURL()});
                    Class<?> dynamicClass = Class.forName(className, true, classLoader);
                    Object dynamicInstance = dynamicClass.getDeclaredConstructor().newInstance();

                    // Find and invoke a method named 'convert' on the dynamic instance
                    Method convertMethod = dynamicClass.getDeclaredMethod("convert", Object.class);
                    Object result = convertMethod.invoke(dynamicInstance, payloadObj);

                    return result.toString();

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    return "Class not found: " + e.getMessage();
                } catch (Exception e) {
                    e.printStackTrace();
                    return "Error during reflection: " + e.getMessage();
                }
            } else {
                // Compilation failed
                System.err.println("Compilation failed");
                System.err.println(compileOutput); // Print the compilation output for debugging
                return "Compilation failed:\n" + compileOutput;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error during compilation and execution: " + e.getMessage();
        }
    }

    private String readStream(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }
}
