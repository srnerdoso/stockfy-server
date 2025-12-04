package br.com.threadstech.stockfy.web.dto.mapper;

import br.com.threadstech.stockfy.entity.Cart;
import br.com.threadstech.stockfy.entity.Payment;
import br.com.threadstech.stockfy.entity.Product;
import br.com.threadstech.stockfy.web.dto.CartCreateDto;
import br.com.threadstech.stockfy.web.dto.PaymentCreateDto;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

@Slf4j
public class PaymentMapper {

  private static final ModelMapper mapper;

  static {
    mapper = new ModelMapper();
    mapper.getConfiguration().setSkipNullEnabled(true);
  }

  public static Payment toPayment(PaymentCreateDto paymentDto, Set<Product> products) {
    Converter<Set<CartCreateDto>, Set<Cart>> cartConverter =
        ctx -> toCartsSet(ctx.getSource(), products);
    mapper
        .typeMap(PaymentCreateDto.class, Payment.class)
        .addMappings(m -> m.using(cartConverter).map(PaymentCreateDto::getCart, Payment::setCart));
    return mapper.map(paymentDto, Payment.class);
  }

  private static Cart toCart(CartCreateDto cartDto, Product product) {
    var mapper = new ModelMapper();
    var props =
        new PropertyMap<CartCreateDto, Cart>() {
          @Override
          protected void configure() {
            map().setProduct(product);
          }
        };
    mapper.addMappings(props);
    return mapper.map(cartDto, Cart.class);
  }

  private static Set<Cart> toCartsSet(Set<CartCreateDto> cart, Set<Product> products) {
    Map<Long, Product> productMap =
        products.stream().collect(Collectors.toMap(Product::getId, Function.identity()));

    return cart.stream()
        .map(
            cartDto -> {
              Product product = productMap.get(cartDto.getProductId());
              // TODO: Tratar exception
              if (product == null) {
                throw new IllegalArgumentException("Product not found: " + cartDto.getProductId());
              }
              return toCart(cartDto, product);
            })
        .collect(Collectors.toSet());
  }
}
