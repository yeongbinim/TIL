package com.example.outsourcing.domain.shop.repository;

import com.example.outsourcing.domain.shop.entity.QShop;
import com.example.outsourcing.domain.shop.entity.Shop;
import com.example.outsourcing.domain.user.entity.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ShopRepositoryImpl implements ShopRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Shop> searchByKeyword(String keyword) {
        QShop qShop = QShop.shop;
        QUser qUser = QUser.user;

        return queryFactory.selectFrom(qShop).join(qShop.user, qUser).fetchJoin()
            .where(qShop.name.containsIgnoreCase(keyword)
                .and(qShop.isDeleted.isFalse()))
            .fetch();
    }
}
