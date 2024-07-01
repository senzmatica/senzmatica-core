package com.magma.core.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

@Service
public class CompileCodeFileService {

    public Boolean compileCodefile(String fileName, String fileContent) {
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

            if (compileResult == 0) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            return false;
        }
    }

    private String readStream(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }
}
    

