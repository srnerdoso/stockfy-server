package br.com.threadstech.stockfy.modules.product;

import br.com.threadstech.stockfy.api.ApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("checkstyle:MissingJavadocType")
@RestController
@RequestMapping(ApiPaths.PRODUCT)
@RequiredArgsConstructor
public class ProductController {
  private final ProductService productService;

  /**
   * Salva um produto.
   *
   * <p>Http Status:
   *
   * <ul>
   *   <li>{@code 201 Created}: Se o produto for salvo com sucesso.
   *   <li>{@code 400 Bad Request}: Se o produto for inválido.
   * </ul>
   *
   * @return {@code ResponseEntity<Void>}:
   */
  @PostMapping
  public ResponseEntity<Void> save() {
    productService.save(new Product());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
