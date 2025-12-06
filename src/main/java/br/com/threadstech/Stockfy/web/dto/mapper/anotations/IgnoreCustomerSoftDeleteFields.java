package br.com.threadstech.stockfy.web.dto.mapper.anotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.mapstruct.Mapping;

@Retention(RetentionPolicy.CLASS)
@Mapping(target = "customers", ignore = true)
@Mapping(target = "deleted", ignore = true)
@Mapping(target = "deletedAt", ignore = true)
@Mapping(target = "deletedBy", ignore = true)
public @interface IgnoreCustomerSoftDeleteFields {}
