package com.example.outsourcing.domain.search.controller;

import com.example.outsourcing.domain.search.dto.SearchResponseDto;
import com.example.outsourcing.domain.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/search")
    public ResponseEntity<SearchResponseDto> search(@RequestParam String keyword) {
        SearchResponseDto searchResult = searchService.searchAll(keyword);

        return ResponseEntity.ok(searchResult);
    }
}
