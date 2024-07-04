package com.zerobase.stockDividendProject.controller;

import com.zerobase.stockDividendProject.model.Company;
import com.zerobase.stockDividendProject.persist.CompanyRepository;
import com.zerobase.stockDividendProject.persist.entity.CompanyEntity;
import com.zerobase.stockDividendProject.service.CompanyService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/company")
@AllArgsConstructor
public class CompanyController {


    private final CompanyService companyService;

    /*
     * 자동검색 API
     */
    @GetMapping("/autocomplete/{keyword}")
    public ResponseEntity<?> autoComplete(@PathVariable(value="keyword") String keyword) {
//        var result = this.companyService.autoComplete(keyword);
        var result = this.companyService.getCompanyNamesByKeyword(keyword);
        return ResponseEntity.ok(result);
    }

    /*
     * 회사리스트 조회 API
     */
    @GetMapping
    public ResponseEntity<?> searchCompany(final Pageable pageable) {
        // 페이징 기능 구현
        Page<CompanyEntity> companies = this.companyService.getAllCompany(pageable);
        return ResponseEntity.ok(companies);
    }

    /*
     * 회사 저장 API
     */
    @PostMapping
    public ResponseEntity<?> addCompany(@RequestBody Company request) {
        String ticker = request.getTicker().trim();
        if(ObjectUtils.isEmpty(ticker)) {
            throw new RuntimeException("ticker is empty");
        }
        Company company = this.companyService.save(ticker);

        // 회사를 저장할 때마다 트라이에 해당 정보 저장
        this.companyService.addAutoCompleteKeyword(company.getName());

        return ResponseEntity.ok(company);
    }

    /*
     * 회사 삭제 API
     */
    @DeleteMapping
    public ResponseEntity<?> deleteCompany() {
        return null;
    }

}
