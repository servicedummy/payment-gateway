package com.paymentservice.api.controller;

import com.paymentservice.api.entity.Order;
import com.paymentservice.api.service.OrderService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {//3rd party payment service integration - stripe
    @Autowired
    private OrderService orderService;
    @Value("${stripe.apikey}")
    private String stripeApiKey;

    @PostMapping("/payments")
    public ResponseEntity<?> createPaymentIntent(@RequestBody Order order) {
        try {//make payments
            Stripe.apiKey = stripeApiKey;

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(order.getAmount())
                    .setCurrency("usd")
                    .setDescription(order.getDescription())
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            Order createdOrder = orderService.saveOrder(order);//save order in my DB
            return ResponseEntity.ok(paymentIntent.getClientSecret());
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}