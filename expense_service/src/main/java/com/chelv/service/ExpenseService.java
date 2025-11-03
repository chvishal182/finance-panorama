package com.chelv.service;

import com.chelv.entities.Expense;
import com.chelv.model.dto.ExpenseInfoDTO;
import com.chelv.repository.ExpenseRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    ExpenseService(ExpenseRepository expenseRepository){
        this.expenseRepository = expenseRepository;
    }

    public void setCurrency(ExpenseInfoDTO expenseInfoDTO){
        if(Objects.isNull(expenseInfoDTO.getCurrency())){
            expenseInfoDTO.setCurrency("INR");
        }
    }

    public List<ExpenseInfoDTO> getExpenses(String userId){
        List<Expense> expensesOptional = expenseRepository.findByUserId(userId);
        return objectMapper
                .convertValue
                        (expensesOptional,
                         new TypeReference<List<ExpenseInfoDTO>>() {}
                        );
    }
    public boolean createExpense(ExpenseInfoDTO expenseInfoDTO){
        setCurrency(expenseInfoDTO);
        try{
            expenseRepository.save
                    (objectMapper.convertValue(expenseInfoDTO, Expense.class));
            return true;
        }catch (Exception e){
            System.out.println("Error in creating an expense: ");
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateExpense(ExpenseInfoDTO expenseInfoDTO){
        setCurrency(expenseInfoDTO);
        Optional<Expense>
                expenseFoundOptional = expenseRepository
                                        .findByUserIdAndExternalId(
                                                expenseInfoDTO.getUserId(),
                                                expenseInfoDTO.getExternalId()
                                        );

        if(expenseFoundOptional.isEmpty()){
            return false;
        }

        Expense expense = expenseFoundOptional.get();
        expense.setAmount(expenseInfoDTO.getAmount());
        expense.setMerchant(Strings.isNotBlank(expenseInfoDTO.getMerchant())?expenseInfoDTO.getMerchant():expense.getMerchant());
        expense.setCurrency(Strings.isNotBlank(expenseInfoDTO.getCurrency())?expenseInfoDTO.getMerchant():expense.getCurrency());
        expenseRepository.save(expense);
        return true;
    }




}
