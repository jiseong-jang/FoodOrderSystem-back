package com.mrdinner.service;

import com.mrdinner.dto.LoginRequest;
import com.mrdinner.dto.LoginResponse;
import com.mrdinner.dto.RegisterRequest;
import com.mrdinner.entity.*;
import com.mrdinner.repository.*;
import com.mrdinner.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 1) 아이디로 사용자 조회 (역할과 관계없이)
        User user = userRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("아이디 또는 비밀번호가 잘못되었습니다"));

        // 2) 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호 불일치");
        }

        user.setIsLoggedIn(true);
        userRepository.save(user);

        String token = tokenProvider.generateToken(user.getId(), user.getRole().name());

        return new LoginResponse(token, user.getId(), user.getRole(), "로그인 성공");
    }

    @Transactional
    public void logout(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
        user.setIsLoggedIn(false);
        userRepository.save(user);
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsById(request.getId())) {
            throw new RuntimeException("이미 존재하는 아이디입니다");
        }

        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new RuntimeException("비밀번호는 8자리 이상이어야 합니다");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다");
        }

        if (request.getPrivacyAgreed() == null || !request.getPrivacyAgreed()) {
            throw new RuntimeException("개인정보 활용 동의는 필수입니다");
        }

        Customer customer = new Customer();
        customer.setId(request.getId());
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setRole(UserRole.CUSTOMER);
        customer.setName(request.getName());
        customer.setAddress(request.getAddress());
        customer.setCardNumber(request.getCardNumber());
        customer.setCardExpiry(request.getCardExpiry());
        customer.setCardCVC(request.getCardCVC());
        customer.setCardHolderName(request.getCardHolderName());
        customer.setIsLoggedIn(false);

        customer = customerRepository.save(customer);

        // 장바구니 생성
        Cart cart = new Cart();
        cart.setCustomer(customer);
        cartRepository.save(cart);
    }

    public boolean checkIdDuplicate(String id) {
        return userRepository.existsById(id);
    }

    public boolean validatePassword(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }
}

