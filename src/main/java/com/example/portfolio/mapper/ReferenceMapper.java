package com.example.portfolio.mapper;

import com.example.portfolio.exception.ResourceNotFoundException;
import com.example.portfolio.model.BaseEntity;
import jakarta.persistence.EntityManager;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.TargetType;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class ReferenceMapper {

    @Autowired
    private EntityManager entityManager;

    public <T extends BaseEntity> T toEntity(Long id, @TargetType Class<T> entityClass) {
        if (id == null) {
            return null;
        }

        T entity = entityManager.find(entityClass, id);
        if (entity == null) {
            throw new ResourceNotFoundException(entityClass.getSimpleName() + " not found: " + id);
        }
        return entity;
    }
}