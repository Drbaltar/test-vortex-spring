package com.drbaltar.testvortexspring.Controllers;

import com.drbaltar.testvortexspring.Models.DataTransferObjects.QuestionActionResponse;
import com.drbaltar.testvortexspring.Models.DataTransferObjects.QuestionQuery;
import com.drbaltar.testvortexspring.Models.DataTransferObjects.QuestionRequest;
import com.drbaltar.testvortexspring.Models.QuestionType;
import com.drbaltar.testvortexspring.Models.Topic;
import com.drbaltar.testvortexspring.Services.QuestionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuestionController.class)
public class QuestionControllerTest {
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    MockMvc mvc;
    @MockBean
    QuestionService service;

    private static Optional<QuestionQuery> getTestQuestionQuery(long id) {
        var incorrectAnswers = new ArrayList<>(Arrays.asList(
                "Dog", "Cat", "Parrot"
        ));
        return Optional.of(new QuestionQuery(id,
                QuestionType.MULTIPLE_CHOICE,
                "What type of animal is Dumbo?",
                "Elephant",
                new Topic("Disney", "Characters"),
                incorrectAnswers));
    }

    private static QuestionRequest getMultipleChoiceRequest() {
        var incorrectAnswers = new ArrayList<>(Arrays.asList(
                "Dog", "Cat", "Parrot"
        ));
        return new QuestionRequest(QuestionType.MULTIPLE_CHOICE,
                "What type of animal is Dumbo?",
                "Elephant",
                new Topic("Disney", "Characters"),
                incorrectAnswers);
    }

    private static QuestionRequest getTrueOrFalseRequest() {
        return new QuestionRequest(QuestionType.TRUE_OR_FALSE,
                "Dumbo is an elephant.",
                "True",
                new Topic("Disney", "Characters"));
    }

    private static QuestionRequest getFillInTheBlankRequest() {
        return new QuestionRequest(QuestionType.FILL_IN_THE_BLANK,
                "Dumbo is an ________.",
                "Elephant",
                new Topic("Disney", "Characters"));
    }

    @Nested
    class AddNewQuestion {

        static Stream<QuestionRequest> getQuestionRequests() {
            return Stream.of(getMultipleChoiceRequest(), getTrueOrFalseRequest(), getFillInTheBlankRequest());
        }

        @ParameterizedTest
        @MethodSource("getQuestionRequests")
        void shouldCallSaveAndReturnSuccessMessageIfValidQuestion(QuestionRequest newQuestionRequest) throws Exception {
            when(service.addNewQuestion(any(QuestionRequest.class))).thenReturn(new QuestionActionResponse());
            var request = post("/api/questions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(newQuestionRequest));

            mvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(content().string("The question was successfully submitted and will be queued for approval!"));

            verify(service).addNewQuestion(eq(newQuestionRequest));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                """
                        {
                            "questionType": "Multiple Choice",
                            "questionDescription": "Who am I?",
                            "correctAnswer": "Me",
                            "topic": {
                                "subCategory": "Definition",
                                "majorCategory": "Me"
                            }
                        }
                        """,
                """
                        {
                            "questionType": "Multiple Choice",
                            "questionDescription": "Who am I?",
                            "correctAnswer": "Me",
                            "topic": {
                                "subCategory": "Definition",
                                "majorCategory": "Me"
                            }, "incorrectAnswers" : ["Test"]
                        }
                        """,
                """
                        {
                            "questionType": "True or False",
                            "questionDescription": "I am me?",
                            "correctAnswer": "Nope",
                            "topic": {
                                "subCategory": "Definition",
                                "majorCategory": "Me"
                            }
                        }
                        """
        })
        void shouldReturnBadRequestErrorIfRequestDoesNotConformToQuestionType(String requestJSON) throws Exception {
            var request = post("/api/questions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJSON);

            mvc.perform(request)
                    .andExpect(status().is4xxClientError());

            verify(service, times(0)).addNewQuestion(any());
        }
    }

    @Nested
    class GetQuestion {
        @ParameterizedTest
        @ValueSource(longs = {42, 10})
        void shouldFindQuestionByIDAndReturnResultsIfPresent(long questionID) throws Exception {
            var testQueryResults = getTestQuestionQuery(questionID);
            when(service.findQuestionByID(eq(questionID))).thenReturn(testQueryResults);
            var request = get("/api/questions/%d".formatted(questionID));

            mvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(testQueryResults.get())));

            verify(service, times(1)).findQuestionByID(eq(questionID));
        }

        @ParameterizedTest
        @ValueSource(longs = {42, 10})
        void shouldReturnErrorCodeIfNoQuestionWithInputID(long questionID) throws Exception {
            when(service.findQuestionByID(eq(questionID))).thenReturn(Optional.empty());
            var request = get("/api/questions/%d".formatted(questionID));

            mvc.perform(request)
                    .andExpect(status().is(404))
                    .andExpect(status().reason("Unable to find a question with that ID!"));

            verify(service, times(1)).findQuestionByID(eq(questionID));
        }
    }

    @Nested
    class DeleteQuestion {
        @ParameterizedTest
        @ValueSource(longs = {42, 10})
        void shouldDeleteQuestionByIDAndReturnResultsIfDeleted(long questionID) throws Exception {
            var testQueryResults = getTestQuestionQuery(questionID);
            when(service.deleteQuestionByID(eq(questionID))).thenReturn(testQueryResults);
            var request = delete("/api/questions/%d".formatted(questionID));

            mvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(content().string("Question %d was successfully deleted!".formatted(questionID)));

            verify(service, times(1)).deleteQuestionByID(eq(questionID));
        }

        @ParameterizedTest
        @ValueSource(longs = {42, 10})
        void shouldReturnErrorCodeIfNoQuestionWithInputID(long questionID) throws Exception {
            when(service.deleteQuestionByID(eq(questionID))).thenReturn(Optional.empty());
            var request = delete("/api/questions/%d".formatted(questionID));

            mvc.perform(request)
                    .andExpect(status().is(404))
                    .andExpect(status().reason("Unable to find a question with that ID!"));

            verify(service, times(1)).deleteQuestionByID(eq(questionID));
        }
    }

    @Nested
    class UpdateQuestion {
        @ParameterizedTest
        @ValueSource(longs = {42, 10})
        void shouldCallUpdateAndReturnSuccessMessageIfValidQuestion(long questionID) throws Exception {
            var testQuestionQuery = getMultipleChoiceRequest();
            when(service.updateQuestionByID(eq(questionID), any(QuestionRequest.class))).thenReturn(getTestQuestionQuery(questionID));
            var request = put("/api/questions/%d".formatted(questionID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(testQuestionQuery));

            mvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(content().string("Question %d was successfully updated!".formatted(questionID)));

            verify(service).updateQuestionByID(eq(questionID), eq(testQuestionQuery));
        }

        @ParameterizedTest
        @ValueSource(longs = {42, 10})
        void shouldReturnErrorCodeIfNoQuestionWithInputID(long questionID) throws Exception {
            var testQuestionQuery = getMultipleChoiceRequest();
            when(service.updateQuestionByID(eq(questionID), any(QuestionRequest.class))).thenReturn(Optional.empty());
            var request = put("/api/questions/%d".formatted(questionID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(testQuestionQuery));

            mvc.perform(request)
                    .andExpect(status().is(404))
                    .andExpect(status().reason("Unable to find a question with that ID!"));

            verify(service).updateQuestionByID(eq(questionID), eq(testQuestionQuery));
        }
    }

    @Nested
    class ApproveQuestion {
        @ParameterizedTest
        @ValueSource(longs = {42, 10})
        void shouldApproveQuestionAndReturnSuccessMessageIfQuestionUpdated(long questionID) throws Exception {
            when(service.approveQuestionByID(eq(questionID))).thenReturn(getTestQuestionQuery(questionID));
            var request = patch("/api/questions/approve/%d".formatted(questionID));

            mvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(content().string("Question %d was successfully approved!".formatted(questionID)));

            verify(service).approveQuestionByID(eq(questionID));
        }

        @ParameterizedTest
        @ValueSource(longs = {42, 10})
        void shouldReturnErrorCodeIfNoQuestionWithInputID(long questionID) throws Exception {
            when(service.approveQuestionByID(eq(questionID))).thenReturn(Optional.empty());
            var request = patch("/api/questions/approve/%d".formatted(questionID));

            mvc.perform(request)
                    .andExpect(status().is(404))
                    .andExpect(status().reason("Unable to find a question with that ID!"));

            verify(service).approveQuestionByID(eq(questionID));
        }
    }
}
