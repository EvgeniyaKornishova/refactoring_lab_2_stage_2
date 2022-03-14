package ru.ifmo.calculator.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.calculator.data.ResultInDto;
import ru.ifmo.calculator.entities.Result;
import ru.ifmo.calculator.entities.User;
import ru.ifmo.calculator.service.ResultService;
import ru.ifmo.calculator.service.UserService;

import java.security.Principal;

@RestController
@RequestMapping("/results")
public class CalculationResultController {
    private final ResultService resultService;
    private final UserService userService;

    public CalculationResultController(ResultService resultService, UserService userService){
        this.resultService = resultService;
        this.userService = userService;
    }

    @GetMapping("/{result_id}")
    public ResponseEntity<Float> getResult(
            Principal principal,
            @PathVariable("result_id") Long resultId
    ){
        User user = userService.findByUsername(principal.getName());
        if (user == null)
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);

        Result result = resultService.findByNumber(resultId, user);
        if (result == null)
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);

        return new ResponseEntity<Float>(result.getValue(), HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Long> appendResult(Principal principal, @RequestBody ResultInDto resultInDto){
        User user = userService.findByUsername(principal.getName());
        if (user == null)
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);

        Result result = new Result();
        result.setValue(resultInDto.getValue());
        result.setUser(user);

        resultService.save(result);

        return new ResponseEntity<Long>(result.getId(), HttpStatus.CREATED);
    }
}
