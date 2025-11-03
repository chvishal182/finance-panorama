package com.chelv.controller;

import com.chelv.model.dto.ExpenseInfoDTO;
import com.chelv.service.ExpenseService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/expense/v1")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Autowired
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping(path = "/getExpense")
    public ResponseEntity<List<ExpenseInfoDTO>> getExpense(@RequestParam(value = "user_id") @NonNull String userId){
        try{
            List<ExpenseInfoDTO> expenseDtoList = expenseService.getExpenses(userId);
            return new ResponseEntity<>(expenseDtoList, HttpStatus.OK);
        }catch(Exception ex){
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(path="/addExpense")
    public ResponseEntity<Boolean> addExpenses(@RequestHeader(value = "X-User-Id") @NonNull String userId, ExpenseInfoDTO expenseInfoDTO){
        try{
            expenseInfoDTO.setUserId(userId);
            return new ResponseEntity<>(expenseService.createExpense(expenseInfoDTO), HttpStatus.OK);
        }catch (Exception ex){
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        }
    }

}
