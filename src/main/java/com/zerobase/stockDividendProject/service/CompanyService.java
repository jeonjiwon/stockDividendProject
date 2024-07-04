package com.zerobase.stockDividendProject.service;

import com.zerobase.stockDividendProject.model.Company;
import com.zerobase.stockDividendProject.model.ScrapedResult;
import com.zerobase.stockDividendProject.persist.CompanyRepository;
import com.zerobase.stockDividendProject.persist.DividendRepository;
import com.zerobase.stockDividendProject.persist.entity.CompanyEntity;
import com.zerobase.stockDividendProject.persist.entity.DividendEntity;
import com.zerobase.stockDividendProject.scraper.Scraper;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CompanyService {

    private final Scraper YahooFinanceScraper;

    private final CompanyRepository companyRepository;

    private final DividendRepository dividendRepository;

    private final Trie trie;

    /*
    회사 및 배당금 저장
    */
    public Company save(String ticker) {
        boolean exists = this.companyRepository.existsByTicker(ticker);
        if(exists) {
            throw new RuntimeException("Already Exists ticker -> " + ticker);
        }
        return this.storeCompanyAndDividend(ticker);
    }

    private Company storeCompanyAndDividend(String ticker) {

        // ticker를 기준으로 회사를 스크래핑
        Company company = this.YahooFinanceScraper.scrapCompanyByTicker(ticker);
        if(ObjectUtils.isEmpty(company)) {
            throw new RuntimeException("Failed to scrap ticker -> " + ticker);
        }

        // 회사가 존재할 경우 회사의 배당금정보를 스크래핑
        ScrapedResult scrapedResult = this.YahooFinanceScraper.scrap(company);

        CompanyEntity companyEntity = this.companyRepository.save(new CompanyEntity(company));
        List<DividendEntity> dividendEntityList = scrapedResult.getDividends().stream()
                                                    .map(e-> new DividendEntity(companyEntity.getId(), e))
                                                    .collect(Collectors.toList());

        // 스크래핑 결과
        this.dividendRepository.saveAll(dividendEntityList);
        return company;

    }


    /*
    회사 조회 (전체 조회 - 페이징 처리)
    */
    public Page<CompanyEntity> getAllCompany(Pageable pageable) {
        return this.companyRepository.findAll(pageable);
    }


    /*
    [자동완성 기능] 회사명 데이터 적재  : Trie 자료구조 사용
    */
    public void addAutoCompleteKeyword(String keyword){
        this.trie.put(keyword, null);
    }


    /*
    [자동완성 기능] 자동완성 조회 : Trie 자료구조 사용
    */
    public List<String> autoComplete(String keyword) {
        return (List<String>) this.trie.prefixMap(keyword).keySet()
                                    .stream()
//                                    .limit(10)
                                    .collect(Collectors.toList());
    }
    
    /*
    [자동완성 기능] 회사명 데이터 삭제 :  : Trie 자료구조 사용
    */
    public void deleteAutoCompleteKeyword(String keyword){
        this.trie.remove(keyword);
    }

    /*
    [자동완성 기능] 자동완성 조회 : like 절 사용
    */
    public List<String> getCompanyNamesByKeyword(String keyword) {
        Pageable limit = PageRequest.of(0, 10);
        Page<CompanyEntity> companyEntities = this.companyRepository.findByNameStartingWithIgnoreCase(keyword, limit);
        return companyEntities.stream().map(e -> e.getName()).collect(Collectors.toList());
    }
    
}
