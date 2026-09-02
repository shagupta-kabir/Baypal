package com.baypal.controller;

import com.baypal.dto.PaymentRequestDto;
import com.baypal.model.User;
import com.baypal.service.PaymentService;
import com.baypal.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final UserService userService;
    private final PaymentService paymentService;

    @GetMapping("/request")
    public String requestPage(Model model) {
        model.addAttribute("paymentRequestDto", new PaymentRequestDto());
        return "payment/request";
    }

    @PostMapping("/request")
    public String createRequest(Authentication authentication, @ModelAttribute PaymentRequestDto paymentRequestDto, Model model) {
        User requester = currentUser(authentication);
        paymentService.createRequest(requester, paymentRequestDto);
        model.addAttribute("success", "Request for $" + paymentRequestDto.getAmount() + " sent to " + paymentRequestDto.getPayerEmail());
        model.addAttribute("paymentRequestDto", new PaymentRequestDto());
        return "payment/request";
    }

    @GetMapping("/requests")
    public String requests(Authentication authentication, Model model) {
        User user = currentUser(authentication);
        model.addAttribute("incoming", paymentService.incomingFor(user));
        model.addAttribute("outgoing", paymentService.outgoingFor(user));
        return "payment/requests";
    }

    @PostMapping("/{id}/pay")
    public String pay(Authentication authentication, @PathVariable Long id) {
        paymentService.pay(id, currentUser(authentication));
        return "redirect:/payments/requests";
    }

    @PostMapping("/{id}/decline")
    public String decline(Authentication authentication, @PathVariable Long id) {
        paymentService.decline(id, currentUser(authentication));
        return "redirect:/payments/requests";
    }

    private User currentUser(Authentication authentication) {
        return userService.getByEmail(authentication.getName());
    }
}
