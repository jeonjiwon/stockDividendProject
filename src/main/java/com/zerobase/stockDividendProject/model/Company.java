package com.zerobase.stockDividendProject.model;

import lombok.*;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    private String ticker;

    private String name;

}
