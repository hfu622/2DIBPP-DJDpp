package com.example.demo.utils;

import lombok.Data;

import java.util.List;

@Data
public class InputData {
    private Integer width;
    private Integer height;
    private List<List<Points>> pieces;
    private Integer num;
}