package com.srr.dto.mapstruct;

import com.srr.domain.Tag;
import com.srr.dto.TagDto;
import me.zhengjie.base.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TagMapper extends BaseMapper<TagDto, Tag> {

}
