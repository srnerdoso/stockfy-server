package br.com.threadstech.stockfy.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.threadstech.stockfy.entity.Product;

/**
 * Interface que representa o repositório de produtos.
 *
 * @author threadstech
 * @see Product
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

  Optional<Product> findByBarCode(String barCode);
}
