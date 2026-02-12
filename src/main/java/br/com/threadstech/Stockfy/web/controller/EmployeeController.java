package br.com.threadstech.stockfy.web.controller;

import br.com.threadstech.stockfy.api.ApiPaths;
import br.com.threadstech.stockfy.entity.Employee;
import br.com.threadstech.stockfy.service.EmployeeService;
import br.com.threadstech.stockfy.web.doc.EmployeeControllerDoc;
import br.com.threadstech.stockfy.web.dto.EmployeeCreateDto;
import br.com.threadstech.stockfy.web.dto.EmployeeDetailDto;
import br.com.threadstech.stockfy.web.dto.EmployeeSummaryDto;
import br.com.threadstech.stockfy.web.dto.EmployeeUpdateDto;
import br.com.threadstech.stockfy.web.dto.PasswordUpdateDto;
import br.com.threadstech.stockfy.web.dto.mapper.EmployeeMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO: Trocar ou adicionar mais métodos que usam o contexto de autenticação do spring para
//       realizar recursos que precisam do id do usuário

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.EMPLOYEE)
public class EmployeeController implements EmployeeControllerDoc {

  private final EmployeeService employeeService;
  private final EmployeeMapper employeeMapper;

  @PostMapping
  public ResponseEntity<Void> save(@Valid @RequestBody EmployeeCreateDto employeeCreateDto) {
    Employee employee = employeeMapper.toEmployee(employeeCreateDto);
    employeeService.save(employee);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping
  public ResponseEntity<Page<EmployeeSummaryDto>> findAll(@PageableDefault Pageable pageable) {
    // FIXME: Esta é uma solução temporária para resolver problemas com lazyInitializationException
    //        no perfil de prod onde "open-in-view" está desativado. Mapper sendo passado para o
    //        método findAll deve ser repensado e corrigido em futuras atualizações. O código abaixo
    //        deve ser reescrito quando o problema for resolvido.

    //  Page<Employee> employees = employeeService.findAll(pageable, employeeMapper);
    //  Page<EmployeeSummaryDto> employeeSummaryDtos = employeeMapper.toPageSummary(employees);
    return ResponseEntity.ok(employeeService.findAll(pageable, employeeMapper));
  }

  @GetMapping("/{id}")
  public ResponseEntity<EmployeeDetailDto> findById(@PathVariable Long id) {
    Employee employee = employeeService.findById(id);
    EmployeeDetailDto employeeDetailDto = employeeMapper.toDetail(employee);
    return ResponseEntity.ok(employeeDetailDto);
  }

  @GetMapping("/{id}/cpf")
  public ResponseEntity<String> findCpfById(@PathVariable Long id) {
    return ResponseEntity.ok(employeeService.findCpfById(id));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<Void> updateById(
      @PathVariable Long id, @Valid @RequestBody EmployeeUpdateDto employeeUpdateDto) {
    employeeService.updateById(id, employeeUpdateDto, employeeMapper);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{id}/password")
  public ResponseEntity<Void> updatePasswordById(
      @PathVariable Long id, @Valid @RequestBody PasswordUpdateDto passwordUpdateDto) {
    employeeService.updatePasswordById(id, passwordUpdateDto);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteById(@PathVariable Long id) {
    employeeService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
