package br.com.threadstech.stockfy.security.jwt;

import br.com.threadstech.stockfy.entity.Employee;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

public class JwtUserDetails extends User {

  private final Employee employee;

  public JwtUserDetails(Employee employee) {
    super(
        employee.getContact().getEmail(),
        employee.getPassword(),
        AuthorityUtils.createAuthorityList(employee.getRole().getDeclaringClass().getName()));
    this.employee = employee;
  }

  public Long getId() {
    return employee.getId();
  }
}
