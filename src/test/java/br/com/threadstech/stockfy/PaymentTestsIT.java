package br.com.threadstech.stockfy;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.threadstech.stockfy.annotations.AdminTest;
import br.com.threadstech.stockfy.annotations.IntegrationTests;
import br.com.threadstech.stockfy.annotations.InventoryManagerTest;
import br.com.threadstech.stockfy.annotations.SalesAttendantTest;
import br.com.threadstech.stockfy.entity.Payment;
import br.com.threadstech.stockfy.enums.PaymentMethod;
import br.com.threadstech.stockfy.enums.PaymentStatus;
import br.com.threadstech.stockfy.repository.PaymentRepository;
import br.com.threadstech.stockfy.utils.DataGenUtils;
import br.com.threadstech.stockfy.utils.PaymentTestUtils;
import br.com.threadstech.stockfy.web.dto.CartCreateDto;
import br.com.threadstech.stockfy.web.dto.PaymentCreateDto;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@Slf4j
@IntegrationTests
@Sql(
    scripts = {
      "/sql/customer-contacts-insert.sql",
      "/sql/customer-addresses-insert.sql",
      "/sql/customers-insert.sql",
      "/sql/products-insert.sql",
      "/sql/payments-insert.sql"
    },
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class PaymentTestsIT {

  @Autowired MockMvc mockMvc;
  @Autowired PaymentRepository paymentRepository;
  @Autowired EntityManager entityManager;

  @Nested
  class Pay {
    @SalesAttendantTest
    public void shouldCreatePaymentWithSalesAttendantWithReturnStatusCreated() throws Exception {
      PaymentCreateDto paymentDto =
          PaymentTestUtils.validPaymentCreateDto(PaymentMethod.CASH, PaymentStatus.PENDING);
      String paymentJson = DataGenUtils.toJson(paymentDto);
      mockMvc
          .perform(
              post(PaymentTestUtils.payPath())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(paymentJson))
          .andDo(print())
          .andExpect(status().isCreated());

      Payment payment =
          entityManager
              .createQuery(
                  """
                SELECT p
                FROM Payment p
                LEFT JOIN FETCH p.cart
                """,
                  Payment.class)
              .getResultList()
              .getFirst();

      assertThat(payment).isNotNull();
      assertThat(payment.getCustomer()).isNotNull();
      assertThat(payment.getCustomer().getId()).isEqualTo(paymentDto.getCustomerId());
      assertThat(payment.getPaymentMethod().name()).isEqualTo(paymentDto.getPaymentMethod());
      assertThat(payment.getPaymentStatus().name()).isEqualTo(paymentDto.getPaymentStatus());
      assertThat(payment.getTotal()).isEqualByComparingTo(paymentDto.getTotal());
      assertThat(payment.getCart()).hasSize(paymentDto.getCart().size());
      payment
          .getCart()
          .forEach(
              paymentCart -> {
                CartCreateDto dtoCart =
                    paymentDto.getCart().stream()
                        .filter(c -> c.getProductId().equals(paymentCart.getProduct().getId()))
                        .findFirst()
                        .orElseThrow();

                assertThat(paymentCart.getQuantity()).isEqualByComparingTo(dtoCart.getQuantity());

                assertThat(paymentCart.getPaymentValue())
                    .isEqualByComparingTo(dtoCart.getPaymentValue());
              });
    }

    @AdminTest
    public void shouldCreatePaymentWithAdminWithReturnStatusCreated() throws Exception {
      PaymentCreateDto paymentDto =
          PaymentTestUtils.validPaymentCreateDto(PaymentMethod.CASH, PaymentStatus.PENDING);
      String paymentJson = DataGenUtils.toJson(paymentDto);
      mockMvc
          .perform(
              post(PaymentTestUtils.payPath())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(paymentJson))
          .andDo(print())
          .andExpect(status().isCreated());

      Payment payment =
          entityManager
              .createQuery(
                  """
                SELECT p
                FROM Payment p
                LEFT JOIN FETCH p.cart
                """,
                  Payment.class)
              .getResultList()
              .getFirst();

      assertThat(payment).isNotNull();
      assertThat(payment.getCustomer()).isNotNull();
      assertThat(payment.getCustomer().getId()).isEqualTo(paymentDto.getCustomerId());
      assertThat(payment.getPaymentMethod().name()).isEqualTo(paymentDto.getPaymentMethod());
      assertThat(payment.getPaymentStatus().name()).isEqualTo(paymentDto.getPaymentStatus());
      assertThat(payment.getTotal()).isEqualByComparingTo(paymentDto.getTotal());
      assertThat(payment.getCart()).hasSize(paymentDto.getCart().size());
      payment
          .getCart()
          .forEach(
              paymentCart -> {
                CartCreateDto dtoCart =
                    paymentDto.getCart().stream()
                        .filter(c -> c.getProductId().equals(paymentCart.getProduct().getId()))
                        .findFirst()
                        .orElseThrow();

                assertThat(paymentCart.getQuantity()).isEqualByComparingTo(dtoCart.getQuantity());

                assertThat(paymentCart.getPaymentValue())
                    .isEqualByComparingTo(dtoCart.getPaymentValue());
              });
    }

    @SalesAttendantTest
    public void shouldCreatePaymentWithReturnStatusBadRequest() throws Exception {
      String invalidPaymentJson = PaymentTestUtils.invalidPaymentCreateJson();
      String nullFieldsPaymentJson = PaymentTestUtils.nullFieldsPaymentCreateJson();

      mockMvc
          .perform(
              post(PaymentTestUtils.payPath())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(invalidPaymentJson))
          .andDo(print())
          .andExpect(status().isBadRequest());
      mockMvc
          .perform(
              post(PaymentTestUtils.payPath())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(nullFieldsPaymentJson))
          .andDo(print())
          .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldCreatePaymentWithReturnStatusUnauthorized() throws Exception {
      mockMvc.perform(post(PaymentTestUtils.payPath())).andExpect(status().isUnauthorized());
    }

    @InventoryManagerTest
    public void shouldCreatePaymentWithInventoryManagerWithReturnStatusForbidden()
        throws Exception {
      mockMvc.perform(post(PaymentTestUtils.payPath())).andExpect(status().isForbidden());
    }
  }

  @Nested
  class Refund {
    long paymentId = 100;
    long paymentIdInvalidRefundDateBetween = 200;
    long paymentIdInvalidPaymentStatus = 300;

    @SalesAttendantTest
    public void shouldRefundPaymentWithReturnStatusNoContent() throws Exception {
      mockMvc
          .perform(post(PaymentTestUtils.refundPath(paymentId)))
          .andExpect(status().isNoContent());

      Payment payment = paymentRepository.findById(paymentId).orElse(null);
      assertThat(payment).isNotNull();
      assertThat(payment.getPaymentStatus().name()).isEqualTo(PaymentStatus.REFUNDED.name());
    }

    @Test
    public void shouldRefundPaymentWithReturnStatusUnauthorized() throws Exception {
      mockMvc
          .perform(post(PaymentTestUtils.refundPath(paymentId)))
          .andExpect(status().isUnauthorized());
    }

    @InventoryManagerTest
    public void shouldRefundPaymentWithInventoryManagerWithReturnStatusForbidden()
        throws Exception {
      mockMvc
          .perform(post(PaymentTestUtils.refundPath(paymentId)))
          .andExpect(status().isForbidden());
    }

    @SalesAttendantTest
    public void shouldRefundPaymentWithReturnStatusNotFound() throws Exception {
      mockMvc
          .perform(post(PaymentTestUtils.refundPath(15616511961981L)))
          .andExpect(status().isNotFound());
    }

    @SalesAttendantTest
    public void shouldRefundPaymentWithReturnStatusUnprocessableEntity() throws Exception {
      mockMvc
          .perform(post(PaymentTestUtils.refundPath(paymentIdInvalidRefundDateBetween)))
          .andExpect(status().isUnprocessableContent());
      mockMvc
          .perform(post(PaymentTestUtils.refundPath(paymentIdInvalidPaymentStatus)))
          .andExpect(status().isUnprocessableContent());
    }
  }
}
