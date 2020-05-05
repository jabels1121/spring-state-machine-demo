package com.jaybe.msscssm.services;

import com.jaybe.msscssm.domain.Payment;
import com.jaybe.msscssm.domain.PaymentEvent;
import com.jaybe.msscssm.domain.PaymentState;
import org.springframework.statemachine.StateMachine;

public interface PaymentService {

    Payment newPayment(Payment payment);

    StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId);

    StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId);

    StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId);
}
