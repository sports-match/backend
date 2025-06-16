package com.srr.dto.mapstruct;

import com.srr.domain.Event;
import com.srr.domain.Tag;
import com.srr.dto.EventDto;
import me.zhengjie.base.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Chanheng
 * @date 2025-05-18
 **/
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class EventMapper implements BaseMapper<EventDto, Event> {

    @Override
    @Mapping(target = "tags", ignore = true)
    public abstract Event toEntity(EventDto dto);

    @Override
    @Mapping(target = "tags", expression = "java(this.toString(entity.getTags()))")
    public abstract EventDto toDto(Event entity);

    protected Set<String> toString(Set<Tag> tags) {
        return tags.stream().map(Tag::getName).collect(Collectors.toSet());
    }

}