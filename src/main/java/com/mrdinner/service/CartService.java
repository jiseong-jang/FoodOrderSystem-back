package com.mrdinner.service;

import com.mrdinner.dto.*;
import com.mrdinner.entity.*;
import com.mrdinner.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mrdinner.menu.MenuItemCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private PriceCalculationService priceCalculationService;

    @Autowired
    private MenuService menuService;

    public CartDto getCart(String customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("고객을 찾을 수 없습니다"));

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCustomer(customer);
                    return cartRepository.save(newCart);
                });

        return convertToDto(cart);
    }

    @Transactional
    public CartItemDto addItem(String customerId, AddCartItemRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("고객을 찾을 수 없습니다"));

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCustomer(customer);
                    return cartRepository.save(newCart);
                });

        Menu menu = menuRepository.findById(request.getMenuId())
                .orElseThrow(() -> new RuntimeException("메뉴를 찾을 수 없습니다"));

        Map<String, Integer> normalizedQuantities = normalizeQuantities(request.getCustomizedQuantities());

        // 스타일 검증
        priceCalculationService.validateStyleForMenu(menu.getType(), request.getStyleType());

        // 가격 계산
        Integer subTotal = priceCalculationService.calculateSubTotal(
                request.getMenuId(),
                request.getStyleType(),
                normalizedQuantities,
                request.getQuantity()
        );

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setSelectedMenu(menu);
        cartItem.setSelectedStyle(request.getStyleType());
        cartItem.setCustomizedQuantities(normalizedQuantities);
        cartItem.setQuantity(request.getQuantity());
        cartItem.setSubTotal(subTotal);

        cartItem = cartItemRepository.save(cartItem);

        return convertToDto(cartItem);
    }

    @Transactional
    public CartItemDto updateItem(String customerId, Long cartItemId, Integer newQuantity) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("고객을 찾을 수 없습니다"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("장바구니 아이템을 찾을 수 없습니다"));

        if (!cartItem.getCart().getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("권한이 없습니다");
        }

        // 가격 재계산
        Integer subTotal = priceCalculationService.calculateSubTotal(
                cartItem.getSelectedMenu().getId(),
                cartItem.getSelectedStyle(),
                cartItem.getCustomizedQuantities(),
                newQuantity
        );

        cartItem.setQuantity(newQuantity);
        cartItem.setSubTotal(subTotal);
        cartItem = cartItemRepository.save(cartItem);

        return convertToDto(cartItem);
    }

    @Transactional
    public void removeItem(String customerId, Long cartItemId) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("고객을 찾을 수 없습니다"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("장바구니 아이템을 찾을 수 없습니다"));

        if (!cartItem.getCart().getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("권한이 없습니다");
        }

        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void clearCart(String customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("고객을 찾을 수 없습니다"));

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseThrow(() -> new RuntimeException("장바구니를 찾을 수 없습니다"));

        cartItemRepository.deleteAll(cart.getItems());
    }

    private CartDto convertToDto(Cart cart) {
        List<CartItemDto> items = cart.getItems().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        Integer totalPrice = items.stream()
                .mapToInt(CartItemDto::getSubTotal)
                .sum();

        return new CartDto(cart.getId(), items, totalPrice);
    }

    private CartItemDto convertToDto(CartItem cartItem) {
        MenuDto menuDto = menuService.getMenuById(cartItem.getSelectedMenu().getId());
        return new CartItemDto(
                cartItem.getId(),
                menuDto,
                cartItem.getSelectedStyle(),
                cartItem.getCustomizedQuantities(),
                cartItem.getQuantity(),
                cartItem.getSubTotal()
        );
    }

    private Map<String, Integer> normalizeQuantities(Map<String, Integer> original) {
        Map<String, Integer> normalized = new HashMap<>();
        if (original == null) {
            return normalized;
        }
        original.forEach((key, value) -> {
            MenuItemCode code = MenuItemCode.fromValue(key);
            if (code != null) {
                normalized.put(code.name(), value);
            }
        });
        return normalized;
    }
}

