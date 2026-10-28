package com.example.portfolio.mapper;

import com.example.portfolio.dto.SkillCreateDTO;
import com.example.portfolio.dto.SkillDTO;
import com.example.portfolio.dto.SkillUpdateDTO;
import com.example.portfolio.model.Skill;
import org.mapstruct.*;

@Mapper(
        uses = {JsonNullableMapper.class, ReferenceMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class SkillMapper {

    public abstract Skill map(SkillCreateDTO dto);

    public abstract SkillDTO map(Skill model);

    @Mapping(target = "name", source = "name", qualifiedByName = "unwrap")
    public abstract void update(SkillUpdateDTO dto, @MappingTarget Skill model);
}