package com.quiz.service;

import com.quiz.exception.DatabaseException;
import com.quiz.exception.ResourceNotFoundException;
import com.quiz.model.Quiz;
import com.quiz.model.Result;
import com.quiz.threads.BackgroundReportWriter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service managing File Handling and Report generation.
 * Demonstrates:
 * - Java File Handling (File, FileWriter, FileReader, BufferedReader)
 * - Checked Exception handling (IOException, DatabaseException)
 * - Integration with domain models implementing Reportable
 */
public class ReportService {

    private final ResultService resultService;
    private final QuizService quizService;
    private final String reportDirectory = "reports";

    public ReportService(ResultService resultService, QuizService quizService) {
        this.resultService = resultService;
        this.quizService = quizService;
    }

    public File exportResultReport(int attemptId) throws DatabaseException, ResourceNotFoundException, IOException {
        Result result = resultService.getResultDetails(attemptId);
        BackgroundReportWriter writer = new BackgroundReportWriter(result, reportDirectory);
        return writer.writeReportToFile();
    }

    public File exportQuizSpecification(int quizId) throws DatabaseException, ResourceNotFoundException, IOException {
        Quiz quiz = quizService.getQuizWithQuestions(quizId);
        BackgroundReportWriter writer = new BackgroundReportWriter(quiz, reportDirectory);
        return writer.writeReportToFile();
    }

    public String readReportContent(String fileName) throws IOException {
        File file = new File(reportDirectory, fileName);
        return BackgroundReportWriter.readReportFromFile(file);
    }

    public List<String> listGeneratedReports() {
        List<String> list = new ArrayList<>();
        File dir = new File(reportDirectory);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".txt") || name.endsWith(".ser"));
            if (files != null) {
                for (File f : files) {
                    list.add(f.getName());
                }
            }
        }
        return list;
    }
}

