package com.loopers.domain.ranking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RankingService {
    private final ProductMetricMonthlyRepository productMetricMonthlyRepository;
    private final ProductMetricWeeklyRepository productMetricWeeklyRepository;

    @Transactional(readOnly = true)
    public Page<ProductMetricWeeklyModel> getWeeklyListWithPage(RankingCommand.SearchWeeklyRanking command){
        return productMetricWeeklyRepository.findAllByPaging(command.page(), command.size(), command.startDate(), command.endDate());
    }

    @Transactional(readOnly = true)
    public Page<ProductMetricMonthlyModel> getMonthlyListWithPage(RankingCommand.SearchMonthlyRanking command){
        return productMetricMonthlyRepository.findAllByPaging(command.page(), command.size(), command.startDate(), command.endDate());
    }



}
