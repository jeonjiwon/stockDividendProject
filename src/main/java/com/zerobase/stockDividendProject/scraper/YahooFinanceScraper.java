package com.zerobase.stockDividendProject.scraper;

import com.zerobase.stockDividendProject.constants.Month;
import com.zerobase.stockDividendProject.model.Company;
import com.zerobase.stockDividendProject.model.Dividend;
import com.zerobase.stockDividendProject.model.ScrapedResult;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class YahooFinanceScraper implements Scraper {

	/*
	배당금 정보를 스크래핑하여 가져온다
	*/
	private static final String STATISTICS_URL = "https://finance.yahoo.com/quote/%s/history?period1=%d&period2=%d&interval=1mo";

	private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s?p=%s";

	private static final long START_TIME = 86400; // 60 * 60 * 24

	@Override
    public ScrapedResult scrap(Company company) {
		ScrapedResult scrapedResult = new ScrapedResult();
		scrapedResult.setCompany(company);
		try{
			long now = System.currentTimeMillis() / 1000;  //1970년부터 계산한 밀리세컨 시간

			String url = String.format(STATISTICS_URL, company.getTicker(), START_TIME, now);
			// https://finance.yahoo.com/quote/COKE/history/?period1=1688222391&period2=1719844726

			Connection connection = Jsoup.connect(url);
			Document document = connection.get();
			Elements parsingDivs = document.select("table.svelte-ewueuo");

			if (parsingDivs.isEmpty()) {
				throw new RuntimeException("[스크래핑실패] 배당금 테이블 요소를 찾을 수 없습니다.");
			}

			Element tableEle = parsingDivs.get(0);
			Element tbody = tableEle.children().get(1);

			List<Dividend> dividends = new ArrayList<>();
			for(Element e : tbody.children()) {
				String txt = e.text();
				if(!txt.endsWith("Dividend")) {
					continue;
				}

				String[] splits = txt.split(" ");
				int month = Month.strToNumber(splits[0]);
				int day = Integer.valueOf(splits[1].replace(",", ""));
				int year = Integer.valueOf(splits[2]);
				String dividend = splits[3];

				if(month < 0) {
					throw new RuntimeException("Unexpected Month enum value -> "+ splits[0]);
				}

				dividends.add(new Dividend(LocalDateTime.of(year, month, day, 0, 0), dividend));

			}
			scrapedResult.setDividends(dividends);

		} catch (IOException e) {
			// TODO
			e.printStackTrace();
		}
		return scrapedResult;
    }

	/*
	회사정보를 스크래핑해서 가져온다.
	*/
	@Override
	public Company scrapCompanyByTicker(String ticker) {
		String url = String.format(SUMMARY_URL, ticker, ticker);
		System.out.println(url);
		Connection connection = Jsoup.connect(url);
        try {
			Document document = connection.get();
			Element titleEle = document.getElementsByTag("h1").get(1);
			System.out.println(titleEle);
			String title = titleEle.text().split("\\(")[0].trim();

			return new Company(ticker, title);

        } catch (IOException e) {
			e.printStackTrace();
        }

		return null;
	}

}
