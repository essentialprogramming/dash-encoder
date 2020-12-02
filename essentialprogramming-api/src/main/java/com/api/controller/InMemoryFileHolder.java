package com.api.controller;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class InMemoryFileHolder {

    private final Queue<String> inputList;

    public InMemoryFileHolder() {
        this.inputList = new ConcurrentLinkedQueue<>();
    }

    public Queue<String> getInputList() {
        return inputList;
    }



}
