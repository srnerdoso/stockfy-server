package br.com.threadstech.stockfy.modules.product;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Interface que representa o repositório de produtos.
 *
 * @author threadstech
 * @see Product
 */
public interface ProductRepository extends JpaRepository<Product, Long> {}
