package com.loopers.utils;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.Type;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class DatabaseCleanUp implements InitializingBean {

    @PersistenceContext
    private EntityManager entityManager;

    private final List<String> tableNames = new ArrayList<>();

    @Override
    public void afterPropertiesSet() {
        entityManager.getMetamodel().getEntities().stream()
                .map(Type::getJavaType)
                .filter(clazz -> clazz.isAnnotationPresent(Entity.class)) // 엔티티인지 확인
                .map(clazz -> clazz.getAnnotation(Table.class))             // @Table 애너테이션 가져오기
                .filter(Objects::nonNull)                                   // null 제거
                .map(Table::name)                                           // name 추출
                .forEach(tableNames::add);                                  // 리스트에 추가
    }

    @Transactional
    public void truncateAllTables() {
        entityManager.flush();
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();

        for (String table : tableNames) {
            entityManager.createNativeQuery("TRUNCATE TABLE `" + table + "`").executeUpdate();
        }

        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
    }
}
