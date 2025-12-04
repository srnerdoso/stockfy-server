package br.com.threadstech.stockfy.repository;

import br.com.threadstech.stockfy.entity.Product;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Interface que representa o repositório de produtos.
 *
 * @author threadstech
 * @see Product
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

  Optional<Product> findByBarCode(String barCode);

  Page<Product> findAllByName(String name, Pageable pageable);
}
