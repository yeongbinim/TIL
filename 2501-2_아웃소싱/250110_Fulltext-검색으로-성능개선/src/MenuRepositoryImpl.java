package com.example.outsourcing.domain.shop.repository;

import com.example.outsourcing.domain.shop.entity.Menu;
import com.example.outsourcing.domain.shop.entity.QMenu;
import com.example.outsourcing.domain.shop.entity.QShop;
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
public class MenuRepositoryImpl implements MenuRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Override
    public List<Menu> searchByKeyword(String keyword) {
        QMenu qMenu = QMenu.menu;
        QShop qShop = QShop.shop;

        return queryFactory.selectFrom(qMenu)
            .join(qMenu.shop, qShop).fetchJoin()
            .where(qMenu.isDeleted.isFalse()
                .and(createSearchCondition(qMenu, keyword)))
            .fetch();
    }

    private BooleanExpression createSearchCondition(QMenu qMenu, String keyword) {
        if (isMySQL()) {
            // MySQL 에서는 사용자 정의 함수 사용
            NumberExpression<Double> searchCondition = Expressions.numberTemplate(Double.class,
                "match_2params_against({0}, {1}, {2})",
                qMenu.name, qMenu.description, keyword
            );
            return searchCondition.gt(0.0);
        } else {
            // 다른 DB 에서는 일반적인 문자열 포함 검사 사용
            return qMenu.description.containsIgnoreCase(keyword)
                .or(qMenu.name.containsIgnoreCase(keyword));
        }
    }

    private boolean isMySQL() {
        return driverClassName != null && driverClassName.contains("mysql");
    }
}
