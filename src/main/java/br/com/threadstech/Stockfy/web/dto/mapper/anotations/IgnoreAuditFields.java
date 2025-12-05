package br.com.threadstech.stockfy.web.dto.mapper.anotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.mapstruct.Mapping;

@Retention(RetentionPolicy.CLASS)
@Mapping(target = "createdAt", ignore = true)
@Mapping(target = "updatedAt", ignore = true)
@Mapping(target = "createdBy", ignore = true)
@Mapping(target = "updatedBy", ignore = true)
public @interface IgnoreAuditFields {}
