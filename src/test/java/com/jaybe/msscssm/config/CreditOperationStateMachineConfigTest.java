package com.jaybe.msscssm.config;

import com.jaybe.msscssm.domain.PaymentEvent;
import com.jaybe.msscssm.domain.PaymentState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CreditOperationStateMachineConfigTest {

    @Autowired
    StateMachineFactory<PaymentState, PaymentEvent> factory;

    @Test
    void testNewStateMachine() {
        var stateMachine = factory.getStateMachine(UUID.randomUUID());

        stateMachine.start();

        System.out.println(stateMachine.getState().getId());

        stateMachine.sendEvent(PaymentEvent.PRE_AUTHORIZE);

        System.out.println("State after PRE_AUTHORIZE event - " + stateMachine.getState().getId());

        stateMachine.sendEvent(PaymentEvent.PRE_AUTH_APPROVED);

        System.out.println(stateMachine.getState().getId());
    }
}