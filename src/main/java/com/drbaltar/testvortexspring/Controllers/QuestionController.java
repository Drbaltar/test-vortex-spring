package com.drbaltar.testvortexspring.Controllers;

import com.drbaltar.testvortexspring.Models.DataTransferObjects.QuestionQuery;
import com.drbaltar.testvortexspring.Models.DataTransferObjects.QuestionRequest;
import com.drbaltar.testvortexspring.Services.QuestionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService service;

    public QuestionController(QuestionService service) {
        this.service = service;
    }

    @PostMapping
    public String addNewQuestion(@RequestBody QuestionRequest newQuestion) {
        service.addNewQuestion(newQuestion);
        return "The question was successfully submitted and will be queued for approval!";
    }

    @GetMapping("/{id}")
    public QuestionQuery getQuestionByID(@PathVariable long id) {
        var queryResults = service.findQuestionByID(id);
        return queryResults.orElseThrow(QuestionNotFoundException::new);
    }

    @PutMapping("/{id}")
    public String updateQuestionByID(@PathVariable long id, @RequestBody QuestionRequest updatedQuestion) {
        var updatedRecord = service.updateQuestionByID(id, updatedQuestion);
        return getSuccessMessage(id, updatedRecord, "update");
    }

    @DeleteMapping("/{id}")
    public String deleteQuestionByID(@PathVariable long id) {
        var deletedRecord = service.deleteQuestionByID(id);
        return getSuccessMessage(id, deletedRecord, "delete");
    }

    @PatchMapping("/approve/{id}")
    public String approveQuestionByID(@PathVariable long id) {
        var approvedRecord = service.approveQuestionByID(id);
        return getSuccessMessage(id, approvedRecord, "approve");
    }

    private String getSuccessMessage(@PathVariable long id, Optional<QuestionQuery> returnedRecord, String action) {
        if (returnedRecord.isPresent())
            return "Question %d was successfully %sd!".formatted(id, action);
        else
            throw new QuestionNotFoundException();
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Unable to find a question with that ID!")
    public static class QuestionNotFoundException extends RuntimeException {

    }
}
