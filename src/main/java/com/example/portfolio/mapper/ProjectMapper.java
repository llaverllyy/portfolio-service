package com.example.portfolio.mapper;

import com.example.portfolio.dto.ProjectCreateDTO;
import com.example.portfolio.dto.ProjectDTO;
import com.example.portfolio.dto.ProjectUpdateDTO;
import com.example.portfolio.model.Project;
import com.example.portfolio.model.Skill;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        uses = {JsonNullableMapper.class, ReferenceMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class ProjectMapper {

    @Mapping(target = "owner", source = "ownerId")
    public abstract Project map(ProjectCreateDTO dto);

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "skillIds", source = "skills", qualifiedByName = "skillIds")
    public abstract ProjectDTO map(Project model);

    @Mapping(target = "owner", source = "ownerId", qualifiedByName = "unwrap")
    @Mapping(target = "slug", source = "slug", qualifiedByName = "unwrap")
    @Mapping(target = "name", source = "name", qualifiedByName = "unwrap")
    @Mapping(target = "description", source = "description", qualifiedByName = "unwrap")
    public abstract void update(ProjectUpdateDTO dto, @MappingTarget Project model);

    @Named("skillIds")
    protected Set<Long> mapSkills(Set<Skill> skills) {
        return skills == null ? Set.of()
                : skills.stream().map(Skill::getId).collect(Collectors.toSet());
    }
}