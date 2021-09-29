package com.drbaltar.testvortexspring.Models;

import com.fasterxml.jackson.annotation.JsonValue;

public enum QuestionType {
    MULTIPLE_CHOICE("Multiple Choice"),
    TRUE_OR_FALSE("True or False"),
    FILL_IN_THE_BLANK("Fill-in-the-Blank");

    @JsonValue
    private final String readableForm;

    QuestionType(String readableForm) {
        this.readableForm = readableForm;
    }

    public String getReadableForm() {
        return readableForm;
    }
}
