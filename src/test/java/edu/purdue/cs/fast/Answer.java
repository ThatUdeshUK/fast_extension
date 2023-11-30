package edu.purdue.cs.fast;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Answer {
    public List<Integer> answers;

    public Answer(Integer... answers) {
        this.answers = Arrays.stream(answers).collect(Collectors.toList());
    }
}
