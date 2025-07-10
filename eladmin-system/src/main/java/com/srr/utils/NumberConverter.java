package com.srr.utils;

public class NumberConverter {

    public static Long doubleToLong(Double value) {
        return value != null ? value.longValue() : null;
    }

}
