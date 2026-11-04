package com.quiz.interfaces;

/**
 * Interface for generating formatted reports from domain entities.
 */
public interface Reportable {
    String getReportTitle();
    String generateReportText();
}

