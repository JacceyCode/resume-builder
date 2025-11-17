package com.jaccey.resumebuilderapi.controller;

import com.jaccey.resumebuilderapi.document.Payment;
import com.jaccey.resumebuilderapi.service.PaymentService;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.jaccey.resumebuilderapi.util.AppConstants.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(PAYMENT_CONTROLLER)
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping(CREATE_ORDER)
    public ResponseEntity<?> createOrder(@RequestBody Map<String, String> request,
                                         Authentication authentication) throws RazorpayException {
        String planType = request.get("planType");
        if(!PREMIUM.equalsIgnoreCase(planType)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid plan type"));
        }

        Payment payment = paymentService.createOrder(authentication.getPrincipal(), planType);

        Map<String, Object> response = Map.of(
                "orderId", payment.getRazorpayOrderId(),
                "amount", payment.getAmount(),
                "currency", payment.getCurrency(),
                "receipt", payment.getReceipt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(VERIFY_PAYMENT)
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> request) throws RazorpayException {
        String razorpayOrderId = request.get("razorpay_order_id");
        String razorpayPaymentId = request.get("razorpay_payment_id");
        String razorpaySignature = request.get("razorpay_signature");

        if (Objects.isNull(razorpayOrderId) ||
                Objects.isNull(razorpayPaymentId) ||
                Objects.isNull(razorpaySignature)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Missing required payment parameters"));
        }

        boolean isValid = paymentService.verifyPayment(razorpayOrderId, razorpayPaymentId, razorpaySignature);

        if (isValid) {
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Payment verified successfully", "status", "success"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Payment verification failed"));
        }
    }

    @GetMapping(PAYMENT_HISTORY)
    public ResponseEntity<?> getPaymentHistory(Authentication authentication) {
        List<Payment> payments = paymentService.getUserPayments(authentication.getPrincipal());

        return ResponseEntity.status(HttpStatus.OK).body(payments);
    }

    @GetMapping(GET_ORDER_DETAILS)
    public ResponseEntity<?> getOrderDetails(@PathVariable String orderId) {
        Payment paymentDetails = paymentService.getPaymentDetails(orderId);

        return ResponseEntity.status(HttpStatus.OK).body(paymentDetails);
    }
}
