package br.com.threadstech.stockfy.repository.spec;

import br.com.threadstech.stockfy.entity.Product;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecs {

  public static Specification<Product> containsInName(String name) {
    return (root, query, cb) -> {
      String likeQuery = "%" + name.toLowerCase() + "%";
      Predicate namePredicate = cb.like(cb.lower(root.get("name")), likeQuery);
      return cb.or(namePredicate);
    };
  }
}
