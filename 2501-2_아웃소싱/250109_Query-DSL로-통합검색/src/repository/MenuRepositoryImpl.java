package com.example.outsourcing.domain.shop.repository;

import com.example.outsourcing.domain.shop.entity.Menu;
import com.example.outsourcing.domain.shop.entity.QMenu;
import com.example.outsourcing.domain.shop.entity.QShop;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MenuRepositoryImpl implements MenuRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Menu> searchByKeyword(String keyword) {
        QMenu qMenu = QMenu.menu;
        QShop qShop = QShop.shop;

        return queryFactory.selectFrom(qMenu)
            .join(qMenu.shop, qShop).fetchJoin()
            .where(qMenu.name.containsIgnoreCase(keyword)
                .or(qMenu.description.containsIgnoreCase(keyword))
                .and(qMenu.isDeleted.isFalse()))
            .fetch();
    }
}
