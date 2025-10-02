// File: src/main/java/org/syos/application/dto/ReportDTO.java
package org.syos.application.dto;

import java.util.Map;
import java.time.LocalDate;

/**
 * Generic Data Transfer Object for reports.
 */
public class ReportDTO {
    private String reportType;
    private LocalDate reportDate;
    private Map<String, Object> data;
    
    public ReportDTO() {}
    
    public ReportDTO(String reportType, LocalDate reportDate, Map<String, Object> data) {
        this.reportType = reportType;
        this.reportDate = reportDate;
        this.data = data;
    }
    
    // Getters and setters
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    
    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }
    
    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
}
