package com.deliacte.dto;

public interface MonthlyCreatedProjection {
    Integer getYear();
    Integer getMonth();
    Long getTotalCreated();
}
