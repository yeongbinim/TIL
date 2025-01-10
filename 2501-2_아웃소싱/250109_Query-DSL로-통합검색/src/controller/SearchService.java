package com.example.outsourcing.domain.search.service;


import com.example.outsourcing.domain.search.dto.SearchResponseDto;
import com.example.outsourcing.domain.search.mapper.SearchMapper;
import com.example.outsourcing.domain.shop.entity.Menu;
import com.example.outsourcing.domain.shop.entity.Shop;
import com.example.outsourcing.domain.shop.repository.MenuRepository;
import com.example.outsourcing.domain.shop.repository.ShopRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ShopRepository shopRepository;
    private final MenuRepository menuRepository;


    public SearchResponseDto searchAll(String keyword) {
        List<Shop> shopList = shopRepository.searchByKeyword(keyword);
        List<Menu> menuList = menuRepository.searchByKeyword(keyword);

        return SearchMapper.toDto(shopList, menuList);
    }
}
