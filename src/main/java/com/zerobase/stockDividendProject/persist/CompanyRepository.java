package com.zerobase.stockDividendProject.persist;

import com.zerobase.stockDividendProject.persist.entity.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {

    boolean existsByTicker(String ticker);

    Optional<CompanyEntity> findByName(String name);

    // 사용은 안하지먄 이렇게도 가능
    Optional<CompanyEntity> findByTicker(String ticker);
}
