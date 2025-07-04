package com.qelasticsearch.integration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiArgType<T, U, V> {
    private T first;
    private U second;
    private V third;
}