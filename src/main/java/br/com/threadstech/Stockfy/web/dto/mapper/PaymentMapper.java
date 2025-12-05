package br.com.threadstech.stockfy.web.dto.mapper;

import br.com.threadstech.stockfy.entity.Cart;
import br.com.threadstech.stockfy.entity.Customer;
import br.com.threadstech.stockfy.entity.Payment;
import br.com.threadstech.stockfy.entity.Product;
import br.com.threadstech.stockfy.service.CustomerService;
import br.com.threadstech.stockfy.service.PaymentService;
import br.com.threadstech.stockfy.service.ProductService;
import br.com.threadstech.stockfy.web.dto.CartCreateDto;
import br.com.threadstech.stockfy.web.dto.PaymentCreateDto;
import br.com.threadstech.stockfy.web.dto.mapper.anotations.IgnoreAuditFields;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
    componentModel = "spring",
    unmappedSourcePolicy = ReportingPolicy.ERROR,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    uses = {PaymentService.class, ProductService.class, CustomerService.class})
public abstract class PaymentMapper {

  @Autowired private PaymentService paymentService;
  @Autowired private ProductService productService;
  @Autowired private CustomerService customerService;

  @IgnoreAuditFields
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "customer", source = "customerId", qualifiedByName = "customerById")
  public abstract Payment toPayment(PaymentCreateDto paymentCreateDto);

  @Mapping(target = "product", source = "productId", qualifiedByName = "productById")
  public abstract Cart toCart(CartCreateDto cartCreateDto);

  @Named("customerById")
  public Customer customerById(Long customerId) {
    return customerId != null ? customerService.findById(customerId) : null;
  }

  @Named("productById")
  public Product productById(Long productId) {
    return productService.findById(productId);
  }

  @Named("cartsFromDtos")
  public Set<Cart> cartsFromDtos(Set<CartCreateDto> cartDtos) {
    Map<Long, Product> productMap =
        paymentService.getProductsFromCarts(cartDtos).stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));

    return cartDtos.stream()
        .map(
            dto -> {
              Cart cart = new Cart();
              cart.setProduct(productMap.get(dto.getProductId()));
              return cart;
            })
        .collect(Collectors.toSet());
  }
}
