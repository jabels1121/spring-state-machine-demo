package com.jaybe.msscssm.services;

import com.jaybe.msscssm.domain.Payment;
import com.jaybe.msscssm.domain.PaymentEvent;
import com.jaybe.msscssm.domain.PaymentState;
import com.jaybe.msscssm.repositories.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;


@SpringBootTest
class PaymentServiceImplTest {

    @Autowired
    PaymentService paymentService;
    @Autowired
    PaymentRepository paymentRepository;
    Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.builder().amount(new BigDecimal("12.99")).build();
    }

    @Transactional
    @Test
    void preAuth() {
        var savedPayment = paymentService.newPayment(this.payment);

        System.out.println("Should be a NEW");
        System.out.println(savedPayment.getState().toString());

        paymentService.preAuth(savedPayment.getId());

        var preAuthPayment = paymentRepository.getOne(savedPayment.getId());

        System.out.println("Should be a PRE_AUTH OR PRE_AUTH_ERROR");
        System.out.println(preAuthPayment.getState().toString());

        System.out.println(preAuthPayment);
    }

    @Transactional
    @Test
    void authorizePaymentTest() throws InterruptedException {
        var payment = paymentService.newPayment(this.payment);

        System.out.println("Payment state should be a NEW");
        System.out.println(payment.getState().toString());

        var paymentId = payment.getId();
        var sm = paymentService.preAuth(paymentId);

        var stateId = sm.getState().getId();
        System.out.println("After PRE_AUTH state is - " + stateId);
        while (!stateId.equals(PaymentState.PRE_AUTH)) {
            System.out.println("In infinite loop for achievement the PRE_AUTH state.");
            var loopPayment = paymentService.newPayment(this.payment);
            System.out.println("Payment state should be a NEW");
            System.out.println(payment.getState().toString());
            stateId = paymentService.preAuth(loopPayment.getId()).getState().getId();
            TimeUnit.SECONDS.sleep(1);
        }
        System.out.println("After infinite loop state must be PRE_AUTH");
        System.out.println("State is - " + stateId);
        System.out.println("#".repeat(20));
        System.out.println("Trying to AUTHORIZE payment with paymentId=" + paymentId);
        sm = paymentService.authorizePayment(paymentId);
        System.out.println("State machine state after AUTHORIZING is - " + sm.getState().getId());
    }
}