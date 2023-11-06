package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.StatisticAnswerDto;
import ru.practicum.dto.StatisticInfoDto;
import ru.practicum.mapper.StatisticMapper;
import ru.practicum.service.StatisticService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StatisticController {
    private final StatisticService service;

    @PostMapping("/hit")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void saveStatisticInformation(@RequestBody StatisticInfoDto statisticInfoDto) {
        log.info("Received statistic information to save: {}", statisticInfoDto.toString());

        service.saveStatisticInfo(statisticInfoDto);
    }

    @GetMapping("/stats")
    @ResponseStatus(code = HttpStatus.OK)
    public List<StatisticAnswerDto> getStatisticInformation(@RequestParam String start, @RequestParam String end,
            @RequestParam(required = false) List<String> uris, @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("Received request to get statistic from {} to {} to URI: {} should be counted with unique IP: {}.",
                start, end, uris, unique);

        return service.getStatisticInfoByParameters(StatisticMapper.createStatisticRequestDto(start,end,uris,unique));
    }

    @GetMapping("/stats/{url}")
    @ResponseStatus(code = HttpStatus.OK)
    public StatisticAnswerDto getStatisticInformationByEventId(@PathVariable String url) {
        log.info("Received request to get statistic for endpoint {}.", url);

        return service.getStatisticForEndPoint(url);
    }
}
