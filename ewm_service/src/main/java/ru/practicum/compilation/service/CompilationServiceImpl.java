package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.dao.CompilationRepository;
import ru.practicum.compilation.dao.CompilationsEventsDB;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.model.dto.CompilationDto;
import ru.practicum.compilation.model.dto.NewCompilationDto;
import ru.practicum.compilation.model.dto.UpdateCompilationRequest;
import ru.practicum.event.dao.EventRepository;
import ru.practicum.event.model.Event;
import ru.practicum.exception.model.BadRequestException;
import ru.practicum.exception.model.NotFoundException;
import ru.practicum.serviceutils.ServiceUtils;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {
    private final ServiceUtils utils;
    private final EventRepository eventRepository;
    private final CompilationRepository compilationRepository;
    private final CompilationsEventsDB compilationsEventsRepository;

    @Override
    @Transactional
    public CompilationDto adminAddNewCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = convertCompilationFromDto(newCompilationDto);
        log.info("Sending to repository request to save new compilation.");
        compilation = compilationRepository.save(compilation);
        log.info("Sending to repository request to save compilation events id's.");
        compilationsEventsRepository.saveCompilationEvents(compilation.getId(), newCompilationDto.getEvents());
        log.info("Sending to repository request to get new compilation events: {}.", newCompilationDto.getEvents());
        List<Event> compilationEvents = eventRepository.findAllById(newCompilationDto.getEvents());
        return convertCompilationToDto(compilation, compilationEvents);
    }

    @Override
    @Transactional
    public CompilationDto adminUpdateCompilation(long compilationId, UpdateCompilationRequest updateCompilationRequest) {
        if (updateCompilationRequest.getTitle() != null) {
            if (updateCompilationRequest.getTitle().isBlank()) {
                throw new BadRequestException("Title can not be empty.");
            }
            if (updateCompilationRequest.getTitle().length() > 50) {
                throw new BadRequestException("Event title length should not be above 50 symbols.");
            }
        }
        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Compilation with id " + compilationId + " does not present in repository."));
        if (updateCompilationRequest.getEvents() != null) {
            log.info("Sending to repository request to delete old compilation events id's.");
            compilationsEventsRepository.deleteCompilationEvents(compilationId);
            log.info("Sending to repository request to save compilation events id's: {}.", updateCompilationRequest.getEvents());
            compilationsEventsRepository.saveCompilationEvents(compilationId, updateCompilationRequest.getEvents());
        }
        compilation = updateCompilationObject(compilation, updateCompilationRequest);
        log.info("Sending to repository request to update compilation.");
        compilationRepository.save(compilation);
        List<Event> compilationEvents = eventRepository.findAllById(compilationsEventsRepository.getCompilationEvents(compilationId));
        return convertCompilationToDto(compilation, compilationEvents);
    }

    @Override
    @Transactional
    public List<CompilationDto> publicGetCompilations(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        if (pinned == null) {
            return compilationRepository.findAll(pageable).stream()
                    .map(compilation -> convertCompilationToDto(compilation, eventRepository.findAllById(compilationsEventsRepository.getCompilationEvents(compilation.getId()))))
                    .collect(Collectors.toList());
        }
        log.info("Sending to repository request to get Pinned({}) compilations.", pinned);
        return compilationRepository.findAllByPinnedIs(pinned, pageable).stream()
                .map(compilation -> convertCompilationToDto(compilation, eventRepository.findAllById(compilationsEventsRepository.getCompilationEvents(compilation.getId()))))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CompilationDto publicGetCompilationById(long compilationId) {
        log.info("Sending to repository request to get compilation with id {}.", compilationId);
        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Compilation with id " + compilationId + " does not present in repository."));
        List<Event> compilationEvents = compilationsEventsRepository.getCompilationEvents(compilationId).stream()
                .map(eventRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        return convertCompilationToDto(compilation, compilationEvents);
    }

    @Override
    @Transactional
    public void adminDeleteCompilation(long compilationId) {
        compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Compilation with id " + compilationId + " does not present in repository."));
        log.info("Sending to repository request to delete compilation with id {}.", compilationId);
        compilationRepository.deleteById(compilationId);
    }

    private Compilation convertCompilationFromDto(NewCompilationDto newCompilationDto) {
        if (newCompilationDto.getPinned() == null) {
            newCompilationDto.setPinned(false);
        }
        if (newCompilationDto.getEvents() == null) {
            newCompilationDto.setEvents(new ArrayList<>());
        }
        return Compilation.builder()
                .title(newCompilationDto.getTitle())
                .pinned(newCompilationDto.getPinned())
                .build();
    }

    private CompilationDto convertCompilationToDto(Compilation compilation, List<Event> events) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .events(events.stream()
                        .map(utils::convertEventToShortDto)
                        .collect(Collectors.toList()))
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }

    private Compilation updateCompilationObject(Compilation compilation, UpdateCompilationRequest updateCompilationRequest) {
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        return compilation;
    }
}
