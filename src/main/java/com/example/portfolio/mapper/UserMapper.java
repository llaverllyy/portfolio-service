package com.example.portfolio.mapper;

import com.example.portfolio.dto.UserCreateDTO;
import com.example.portfolio.dto.UserDTO;
import com.example.portfolio.dto.UserUpdateDTO;
import com.example.portfolio.model.Skill;
import com.example.portfolio.model.User;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        uses = {JsonNullableMapper.class, ReferenceMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class UserMapper {

    public abstract User map(UserCreateDTO dto);

    @Mapping(target = "skillIds", source = "skills", qualifiedByName = "skillIds")
    public abstract UserDTO map(User model);

    @Mapping(target = "firstName", source = "firstName", qualifiedByName = "unwrap")
    @Mapping(target = "lastName", source = "lastName", qualifiedByName = "unwrap")
    @Mapping(target = "email", source = "email", qualifiedByName = "unwrap")
    @Mapping(target = "bio", source = "bio", qualifiedByName = "unwrap")
    public abstract void update(UserUpdateDTO dto, @MappingTarget User model);

    @Named("skillIds")
    protected Set<Long> mapSkills(Set<Skill> skills) {
        return skills == null ? Set.of()
                : skills.stream().map(Skill::getId).collect(Collectors.toSet());
    }
}