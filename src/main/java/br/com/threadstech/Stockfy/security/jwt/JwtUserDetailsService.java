package br.com.threadstech.stockfy.security.jwt;

import br.com.threadstech.stockfy.entity.Employee;
import br.com.threadstech.stockfy.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtUserDetailsService implements UserDetailsService {

  private final EmployeeService employeeService;
  private final JwtUtils jwtUtils;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Employee employee = employeeService.findByEmail(username);
    employee.setPassword(employee.getPassword());
    return new JwtUserDetails(employee);
  }

  public JwtToken getTokenAuthenticated(Long userId, String username, String role) {
    return jwtUtils.createToken(userId, username, role);
  }
}
