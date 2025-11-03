package com.chelv.utility;

import com.chelv.model.ExpenseInfoEvent;
import com.chelv.model.dto.ExpenseInfoDTO;

public class ExpenseInfoMapper {

    public static ExpenseInfoDTO fromEvent(ExpenseInfoEvent event){
        return ExpenseInfoDTO.builder()
                             .externalId(event.getExternalId())
                             .amount(event.getAmount())
                             .userId(event.getUserId())
                             .merchant(event.getMerchant())
                             .currency(event.getCurrency())
                             .createdAt(event.getCreatedAt())
                             .build();
    }
}
