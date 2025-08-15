package com.exam_platform.ace.controller;

import com.exam_platform.ace.entity.Question;
import com.exam_platform.ace.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class ImageResourceController {

    private final QuestionService questionService;

    public ImageResourceController(@Autowired QuestionService questionService) {
        this.questionService = questionService;
    }

    @ResponseBody
    @GetMapping("/image/{examId}/{paperName}/{number}")
    public byte[] getImageResource(@PathVariable Long examId, @PathVariable String paperName, @PathVariable Integer number) {
        Question question = questionService.fetchQuestionById(examId, paperName, number);
        if (question == null)
            return null;
        return question.getImageData();
    }
}
