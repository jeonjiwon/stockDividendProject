package com.zerobase.stockDividendProject.scraper;

import com.zerobase.stockDividendProject.model.Company;
import com.zerobase.stockDividendProject.model.ScrapedResult;

public interface Scraper {

    Company scrapCompanyByTicker(String ticker);

    ScrapedResult scrap(Company company);

}
