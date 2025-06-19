package com.srr.utils;

import org.springframework.stereotype.Service;

@Service
public class RatingConverter {

    private static final double UBR_MIN = 3500.0;
    private static final double UBR_MAX = 8500.0;
    private static final double SRR_MIN = 800.0;
    private static final double SRR_MAX = 3000.0;

    
    public double ubrToSrr(double ubrRating) {
        double clamped = clamp(ubrRating, UBR_MIN, UBR_MAX);
        double ratio = (clamped - UBR_MIN) / (UBR_MAX - UBR_MIN);
        return SRR_MIN + ratio * (SRR_MAX - SRR_MIN);
    }

    
    public double srrToUbr(double srr) {
        double clamped = clamp(srr, SRR_MIN, SRR_MAX);
        double ratio = (clamped - SRR_MIN) / (SRR_MAX - SRR_MIN);
        return UBR_MIN + ratio * (UBR_MAX - UBR_MIN);
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
