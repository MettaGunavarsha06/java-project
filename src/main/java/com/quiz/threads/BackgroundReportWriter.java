package com.quiz.threads;

import com.quiz.interfaces.Reportable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Background Runnable task for exporting reports to files.
 * Demonstrates:
 * - Multithreading: implements Runnable
 * - File Handling: File, FileWriter, PrintWriter, BufferedReader, FileReader
 * - Checked Exception handling: IOException
 */
public class BackgroundReportWriter implements Runnable {

    private final Reportable reportable;
    private final String targetDirectory;

    public BackgroundReportWriter(Reportable reportable, String targetDirectory) {
        this.reportable = reportable;
        this.targetDirectory = targetDirectory != null ? targetDirectory : "reports";
    }

    @Override
    public void run() {
        if (reportable == null) return;
        try {
            writeReportToFile();
        } catch (IOException e) {
            System.err.println("[BackgroundReportWriter] Failed to write report: " + e.getMessage());
        }
    }

    public File writeReportToFile() throws IOException {
        File dir = new File(targetDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String safeName = reportable.getReportTitle()
                .replaceAll("[^a-zA-Z0-9.-]", "_")
                .toLowerCase();
        File file = new File(dir, safeName + "_" + timestamp + ".txt");

        // Write using FileWriter & PrintWriter
        try (FileWriter fw = new FileWriter(file);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(reportable.generateReportText());
            pw.flush();
        }

        System.out.println("[BackgroundReportWriter] Report successfully saved to: " + file.getAbsolutePath());
        return file;
    }

    /**
     * Reads a report file back from disk using BufferedReader & FileReader.
     */
    public static String readReportFromFile(File file) throws IOException {
        if (!file.exists()) {
            throw new IOException("Report file does not exist: " + file.getAbsolutePath());
        }

        StringBuilder sb = new StringBuilder();
        try (FileReader fr = new FileReader(file);
             BufferedReader br = new BufferedReader(fr)) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }
}

