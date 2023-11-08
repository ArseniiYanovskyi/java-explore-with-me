package ru.practicum.compilation.service;

import ru.practicum.compilation.model.dto.CompilationDto;
import ru.practicum.compilation.model.dto.NewCompilationDto;
import ru.practicum.compilation.model.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto adminAddNewCompilation(NewCompilationDto newCompilationDto);

    CompilationDto adminUpdateCompilation(long compilationId, UpdateCompilationRequest updateCompilationRequest);

    List<CompilationDto> publicGetCompilations(Boolean pinned, int from, int size);

    CompilationDto publicGetCompilationById(long compilationId);

    void adminDeleteCompilation(long compilationId);
}
