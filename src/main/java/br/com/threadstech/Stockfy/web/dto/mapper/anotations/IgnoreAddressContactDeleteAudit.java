package br.com.threadstech.stockfy.web.dto.mapper.anotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.mapstruct.Mapping;

@Retention(RetentionPolicy.CLASS)
@Mapping(target = "address.deletedBy", ignore = true)
@Mapping(target = "address.deletedAt", ignore = true)
@Mapping(target = "contact.deletedBy", ignore = true)
@Mapping(target = "contact.deletedAt", ignore = true)
public @interface IgnoreAddressContactDeleteAudit {}
