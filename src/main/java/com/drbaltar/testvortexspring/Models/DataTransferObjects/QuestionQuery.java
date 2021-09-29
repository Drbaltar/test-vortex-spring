package com.drbaltar.testvortexspring.Models.DataTransferObjects;

import com.drbaltar.testvortexspring.Models.QuestionType;
import com.drbaltar.testvortexspring.Models.Topic;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record QuestionQuery(
        Long id,
        QuestionType questionType,
        String questionDescription,
        String correctAnswer,
        Topic topic,
        ArrayList<String> incorrectAnswers
) {
}
