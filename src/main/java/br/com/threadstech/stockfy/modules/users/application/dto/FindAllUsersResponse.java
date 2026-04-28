package br.com.threadstech.stockfy.modules.users.application.dto;

import java.util.List;

public record FindAllUsersResponse<T>(
    List<T> content, int page, int size, long totalElements, int totalPages) {}
