package com.drbaltar.testvortexspring.Services;

import com.drbaltar.testvortexspring.Models.DataTransferObjects.QuestionActionResponse;
import com.drbaltar.testvortexspring.Models.DataTransferObjects.QuestionQuery;
import com.drbaltar.testvortexspring.Models.DataTransferObjects.QuestionRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface QuestionService {
    QuestionActionResponse addNewQuestion(QuestionRequest newQuestion);

    Optional<QuestionQuery> approveQuestionByID(long questionID);

    Optional<QuestionQuery> findQuestionByID(long questionID);

    Optional<QuestionQuery> updateQuestionByID(long questionID, QuestionRequest updatedQuestion);

    Optional<QuestionQuery> deleteQuestionByID(long questionID);
}
