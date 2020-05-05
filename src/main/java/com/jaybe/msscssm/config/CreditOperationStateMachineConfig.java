package com.jaybe.msscssm.config;

import com.jaybe.msscssm.domain.PaymentEvent;
import com.jaybe.msscssm.domain.PaymentState;
import com.jaybe.msscssm.services.PaymentServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;
import java.util.Random;

@Slf4j
@Configuration
@EnableStateMachineFactory
public class CreditOperationStateMachineConfig
        extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states)
            throws Exception {
        states.withStates()
                .initial(PaymentState.NEW)
                .states(EnumSet.allOf(PaymentState.class))
                .end(PaymentState.AUTH)
                .end(PaymentState.PRE_AUTH_ERROR)
                .end(PaymentState.AUTH_ERROR);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions)
            throws Exception {
        transitions
                // pre auth states, events and actions
                .withExternal().source(PaymentState.NEW).target(PaymentState.NEW)
                    .event(PaymentEvent.PRE_AUTHORIZE).action(paymentPreAuthAction())
                .and()
                .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH)
                    .event(PaymentEvent.PRE_AUTH_APPROVED)
                .and()
                .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH_ERROR)
                    .event(PaymentEvent.PRE_AUTH_DECLINE)
                .and()
                // authorize states, events and actions
                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.PRE_AUTH)
                    .event(PaymentEvent.AUTHORIZE).action(paymentAuthAction())
                .and()
                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.AUTH)
                    .event(PaymentEvent.AUTH_APPROVED)
                .and()
                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.AUTH_ERROR)
                    .event(PaymentEvent.AUTH_DECLINED);
    }

    private Action<PaymentState, PaymentEvent> paymentAuthAction() {
        return context -> {
            System.out.println("In pre auth action!");

            if (new Random().nextInt(10) < 8) {
                System.out.println("AUTH SUCCESS!");
                context.getStateMachine().sendEvent(
                        MessageBuilder.withPayload(PaymentEvent.AUTH_APPROVED)
                                .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,
                                        context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                                .build());
            } else {
                System.out.println("AUTH ERROR!");
                context.getStateMachine().sendEvent(
                        MessageBuilder.withPayload(PaymentEvent.AUTH_DECLINED)
                                .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,
                                        context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                                .build());
            }
        };
    }

    private Action<PaymentState, PaymentEvent> paymentPreAuthAction() {
        return context -> {
            System.out.println("In pre auth action!");

            if (new Random().nextInt(10) < 8) {
                System.out.println("Pre auth is APPROVED!");
                context.getStateMachine().sendEvent(
                        MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_APPROVED)
                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,
                                context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                        .build());
            } else {
                System.out.println("Pre auth is DECLINED");
                context.getStateMachine().sendEvent(
                        MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_DECLINE)
                                .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,
                                        context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                                .build());
            }
        };
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config)
            throws Exception {
        StateMachineListenerAdapter<PaymentState, PaymentEvent> adapter = new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
                log.info(String.format("stateChanged: from: %s, to: %s", from, to));
            }
        };

        config.withConfiguration().listener(adapter);
    }
}
