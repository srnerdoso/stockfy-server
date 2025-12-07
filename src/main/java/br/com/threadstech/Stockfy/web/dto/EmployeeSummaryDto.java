package br.com.threadstech.stockfy.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSummaryDto {
  private Long id;
  private String fullName;
  private String email;
  private String phoneNumber;
}
