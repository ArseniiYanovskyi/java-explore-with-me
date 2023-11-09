package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.model.dto.CompilationDto;
import ru.practicum.compilation.model.dto.NewCompilationDto;
import ru.practicum.compilation.model.dto.UpdateCompilationRequest;
import ru.practicum.compilation.service.CompilationService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CompilationController {
    private final CompilationService compilationService;

    @PostMapping("/admin/compilations")
    @ResponseStatus(code = HttpStatus.CREATED)
    public CompilationDto adminAddCompilation(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        log.info("Received request to add new compilation title: {}, pinned: {}, events: {}.", newCompilationDto.getTitle(), newCompilationDto.getPinned(), newCompilationDto.getEvents());
        return compilationService.adminAddNewCompilation(newCompilationDto);
    }

    @PatchMapping("/admin/compilations/{compId}")
    @ResponseStatus(code = HttpStatus.OK)
    public CompilationDto adminUpdateCompilation(@PathVariable(value = "compId") long compId, @RequestBody UpdateCompilationRequest updateCompilationRequest) {
        log.info("Received request to update compilation with id {}.", compId);
        return compilationService.adminUpdateCompilation(compId, updateCompilationRequest);
    }

    @GetMapping("/compilations")
    @ResponseStatus(code = HttpStatus.OK)
    public List<CompilationDto> publicGetCompilationList(@RequestParam(required = false) Boolean pinned, @RequestParam(defaultValue = "0") int from, @RequestParam(defaultValue = "10") int size) {
        log.info("Received request to get compilations.");
        return compilationService.publicGetCompilations(pinned, from, size);
    }

    @GetMapping("/compilations/{compId}")
    @ResponseStatus(code = HttpStatus.OK)
    public CompilationDto publicGetCompilationListById(@PathVariable long compId) {
        log.info("Received request to get compilation with id: {}.", compId);
        return compilationService.publicGetCompilationById(compId);
    }

    @DeleteMapping("/admin/compilations/{compId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void adminDeleteCompilation(@PathVariable(value = "compId") long compId) {
        log.info("Received request to delete compilation with id {}.", compId);
        compilationService.adminDeleteCompilation(compId);
    }
}
