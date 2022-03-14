package ru.ifmo.calculator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ifmo.calculator.entities.Result;
import ru.ifmo.calculator.entities.User;
import ru.ifmo.calculator.repository.ResultRepository;

@Service
public class ResultService {
    @Autowired
    private ResultRepository resultRepository;

    public Result save(Result result){
        return resultRepository.save(result);
    }

    public Result findByNumber(Long number, User user) {
        Result result = resultRepository.getById(number);

        if (!result.getUser().getId().equals(user.getId()))
            return null;
        return result;
    }
}
