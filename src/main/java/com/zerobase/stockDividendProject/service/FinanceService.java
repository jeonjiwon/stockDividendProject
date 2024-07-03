package com.zerobase.stockDividendProject.service;

import com.zerobase.stockDividendProject.model.Company;
import com.zerobase.stockDividendProject.model.Dividend;
import com.zerobase.stockDividendProject.model.ScrapedResult;
import com.zerobase.stockDividendProject.persist.CompanyRepository;
import com.zerobase.stockDividendProject.persist.DividendRepository;
import com.zerobase.stockDividendProject.persist.entity.CompanyEntity;
import com.zerobase.stockDividendProject.persist.entity.DividendEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;

    private final DividendRepository dividendRepository;

    public ScrapedResult getDividendByCompanyName(String companyName) {

        // 1. 회사명을 기준으로 회사 정보를 조회
        CompanyEntity company = this.companyRepository.findByName(companyName)
                                    .orElseThrow(() -> new RuntimeException("존재하는 회사명입니다."));

        // 2. 조회된 회사ID로 배당금 내역을 조회
        List<DividendEntity> dividendEntities = this.dividendRepository.findAllByCompanyId(company.getId());

        // 3. 조회된 회사정보와 배당금 정보를 조합하여 결과 반환
//        List<Dividend> dividends = new ArrayList<>();
//        for(var entity : dividendEntities) {
//            dividends.add(Dividend.builder()
//                    .date(entity.getDate())
//                    .dividend(entity.getDividend())
//                    .build());
//        }

        List<Dividend> dividends = dividendEntities.stream()
                                                    .map( e-> Dividend.builder()
                                                            .date(e.getDate())
                                                            .dividend(e.getDividend())
                                                            .build())
                                                    .collect(Collectors.toList());

        return new ScrapedResult(Company.builder()
                .ticker(company.getTicker())
                .name(company.getName())
                .build(),
                null
        );
    }
}
