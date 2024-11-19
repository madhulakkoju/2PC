package org.cse535.loggers;


import lombok.NoArgsConstructor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor

public class LogUtils {

    String filePath = "log.txt";
    int portId;


    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSSSSS");


    public LogUtils(String fileName, int portId) {
        super();
        this.filePath = "Logs/"+portId+"-"+fileName+".txt";
        this.portId = portId;
        this.initializeLogger();
    }

    public LogUtils(int portId) {
        this.portId = portId;
        this.filePath = "Logs/Log-" + this.portId +".txt";
        this.initializeLogger();
    }

    public void initializeLogger() {
        try {
            File file = new File(filePath);
            boolean fileExists = file.exists();
            FileWriter fileWriter = new FileWriter(file,!file.exists());
            String text = "\nrewriting again new test 1234";
            if (fileExists) {
                fileWriter.close(); // Close the existing writer
                fileWriter = new FileWriter(filePath, false);
            }
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(" ");
            bufferedWriter.close();
        } catch (IOException e) {
            // Print an error message if an IOException occurs
            System.out.println("An error occurred while writing to the file: " + e.getMessage());
        }


    }

    public void log(String message) {

        LocalDateTime currentTime = LocalDateTime.now();
        String formattedTime = currentTime.format(formatter);

        try {
            File file = new File(filePath);
            FileWriter fileWriter = new FileWriter(file,file.exists());
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write("\n"+ message);
            bufferedWriter.close();
            //System.out.println("Text has been written to the file successfully.");
        } catch (IOException e) {
            // Print an error message if an IOException occurs
            System.out.println("An error occurred while writing to the file: " + e.getMessage());
        }

    }



}
