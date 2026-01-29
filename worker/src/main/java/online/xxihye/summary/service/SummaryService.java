package online.xxihye.summary.service;

import lombok.RequiredArgsConstructor;
import online.xxihye.summary.domain.Summary;
import online.xxihye.summary.repository.SummaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SummaryService {

    private final SummaryRepository repository;

    @Transactional
    public Summary saveSummary(String inputHash, String summaryText){
        return repository.save(Summary.createSummary(inputHash, summaryText));
    }


}
