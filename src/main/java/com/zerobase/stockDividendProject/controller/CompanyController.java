package com.zerobase.stockDividendProject.controller;

import com.zerobase.stockDividendProject.constants.CacheKey;
import com.zerobase.stockDividendProject.model.Company;
import com.zerobase.stockDividendProject.persist.CompanyRepository;
import com.zerobase.stockDividendProject.persist.entity.CompanyEntity;
import com.zerobase.stockDividendProject.service.CompanyService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/company")
@AllArgsConstructor
public class CompanyController {


    private final CompanyService companyService;

    private final CacheManager redisCacheManager;

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
    @PreAuthorize("hasRole('READ')")
    public ResponseEntity<?> searchCompany(final Pageable pageable) {
        // 페이징 기능 구현
        Page<CompanyEntity> companies = this.companyService.getAllCompany(pageable);
        return ResponseEntity.ok(companies);
    }

    /*
     * 회사 저장 API
     */
    @PostMapping
    @PreAuthorize("hasRole('WRITE')")
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
    @DeleteMapping("/{ticker}")
    @PreAuthorize("hasRole('WRITE')")
    public ResponseEntity<?> deleteCompany(@PathVariable(value="ticker") String ticker) {
        String companyName = this.companyService.deleteCompany(ticker);
        this.clearFinanceCache(companyName);

        return ResponseEntity.ok(companyName);
    }


    public void clearFinanceCache(String companyName) {
        this.redisCacheManager.getCache(CacheKey.KEY_FINANCE).evict(companyName);
    }

}
