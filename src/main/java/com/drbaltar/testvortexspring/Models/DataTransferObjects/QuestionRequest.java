package com.drbaltar.testvortexspring.Models.DataTransferObjects;

import com.drbaltar.testvortexspring.Models.QuestionType;
import com.drbaltar.testvortexspring.Models.Topic;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record QuestionRequest(
        QuestionType questionType,
        String questionDescription,
        String correctAnswer,
        Topic topic,
        ArrayList<String> incorrectAnswers
) {
    public QuestionRequest(QuestionType questionType,
                           String questionDescription,
                           String correctAnswer,
                           Topic topic) {
        this(questionType, questionDescription, correctAnswer, topic, null);
    }

    public QuestionRequest {
        switch (questionType) {
            case MULTIPLE_CHOICE -> {
                if (incorrectAnswers.size() < 3)
                    throw new IllegalArgumentException();
            }
            case TRUE_OR_FALSE -> {
                if (!(correctAnswer.equals("True") || correctAnswer.equals("False")))
                    throw new IllegalArgumentException();
            }
        }
    }
}
