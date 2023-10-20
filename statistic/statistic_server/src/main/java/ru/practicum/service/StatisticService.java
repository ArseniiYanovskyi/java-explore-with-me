package ru.practicum.service;

import ru.practicum.dto.*;

import java.util.List;

public interface StatisticService {

    void saveStatisticInfo(StatisticInfoDto statisticInfoDto);

    List<StatisticAnswerDto> getStatisticInfoByParameters(StatisticRequestDto statisticRequestDto);

}
