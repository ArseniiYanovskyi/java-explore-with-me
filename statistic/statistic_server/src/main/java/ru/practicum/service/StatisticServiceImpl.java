package ru.practicum.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dao.EndpointHitRepository;
import ru.practicum.dto.*;
import ru.practicum.mapper.StatisticMapper;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticServiceImpl implements StatisticService {
    private final EndpointHitRepository repository;

    @Override
    @Transactional
    public void saveStatisticInfo(StatisticInfoDto statisticInfoDto) {
        log.info("Sending to dao statistic information to add.");

        repository.saveStatisticInfo(StatisticMapper.convertToEndpointHit(statisticInfoDto));
    }

    @Override
    @Transactional
    public List<StatisticAnswerDto> getStatisticInfoByParameters(StatisticRequestDto statisticRequestDto) {
        log.info("Sending to dao request to get statistic information.");
        if (statisticRequestDto.getUnique()) {
            return repository.getUniqueIpStatistic(statisticRequestDto.getUris(), statisticRequestDto.getStart(), statisticRequestDto.getEnd());
        }
        return repository.getStatistic(statisticRequestDto.getUris(), statisticRequestDto.getStart(), statisticRequestDto.getEnd());
    }
}
