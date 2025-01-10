package com.example.outsourcing.domain.shop.repository;

import com.example.outsourcing.domain.shop.entity.QShop;
import com.example.outsourcing.domain.shop.entity.Shop;
import com.example.outsourcing.domain.user.entity.QUser;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ShopRepositoryImpl implements ShopRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Override
    public List<Shop> searchByKeyword(String keyword) {
        QShop qShop = QShop.shop;
        QUser qUser = QUser.user;

        return queryFactory.selectFrom(qShop)
            .join(qShop.user, qUser).fetchJoin()
            .where(qShop.isDeleted.isFalse()
                .and(createSearchCondition(qShop, keyword)))
            .fetch();
    }

    private BooleanExpression createSearchCondition(QShop qShop, String keyword) {
        if (isMySQL()) {
            // MySQL 에서는 사용자 정의 함수 사용
            NumberExpression<Double> searchCondition = Expressions.numberTemplate(Double.class,
                "match_1params_against({0}, {1})",
                qShop.name, keyword
            );
            return searchCondition.gt(0.0);
        } else {
            // 다른 DB 에서는 일반적인 문자열 포함 검사 사용
            return qShop.name.containsIgnoreCase(keyword);
        }
    }

    private boolean isMySQL() {
        return driverClassName != null && driverClassName.contains("mysql");
    }
}
