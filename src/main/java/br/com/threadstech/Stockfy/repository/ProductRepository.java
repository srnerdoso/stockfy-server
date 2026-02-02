package br.com.threadstech.stockfy.repository;

import br.com.threadstech.stockfy.entity.Product;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Interface que representa o repositório de produtos.
 *
 * @author threadstech
 * @see Product
 */
public interface ProductRepository
    extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

  Optional<Product> findByBarCode(String barCode);

  Page<Product> findAllByName(String name, Pageable pageable);
}
