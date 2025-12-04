package com.mrdinner.config;

import com.mrdinner.entity.Coupon;
import com.mrdinner.entity.DeliveryStaff;
import com.mrdinner.entity.Inventory;
import com.mrdinner.entity.Item;
import com.mrdinner.entity.KitchenStaff;
import com.mrdinner.entity.Menu;
import com.mrdinner.entity.MenuType;
import com.mrdinner.entity.UserRole;
import com.mrdinner.menu.MenuComposition;
import com.mrdinner.menu.MenuItemCode;
import com.mrdinner.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private ItemRepository itemRepository;


    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private KitchenStaffRepository kitchenStaffRepository;

    @Autowired
    private DeliveryStaffRepository deliveryStaffRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        initializeItems();
        initializeInventory();
        initializeMenus();
        initializeStaff();
        initializeCoupons();
        updateAllPasswords();
    }

    private void initializeItems() {
        for (MenuItemCode code : MenuItemCode.values()) {
            Item item = itemRepository.findByName(code.name())
                    .orElseGet(() -> {
                        Item newItem = new Item();
                        newItem.setName(code.name());
                        newItem.setStockQuantity(0);
                        return newItem;
                    });
            item.setName(code.name());
            item.setPrice(code.getUnitPrice());
            itemRepository.save(item);
        }
    }

    private void initializeInventory() {
        for (MenuItemCode code : MenuItemCode.values()) {
            Inventory inventory = inventoryRepository.findByItemName(code.name())
                    .orElseGet(() -> {
                        Inventory newInventory = new Inventory();
                        newInventory.setItemName(code.name());
                        newInventory.setQuantity(0);
                        return newInventory;
                    });
            // 수량은 여기서는 0으로 두고, 실제 수량은 주방에서 조절
            if (inventory.getQuantity() == null) {
                inventory.setQuantity(0);
            }
            inventoryRepository.save(inventory);
        }
    }

    private void initializeMenus() {
        for (MenuType type : MenuType.values()) {
            Menu menu = menuRepository.findByType(type).orElseGet(Menu::new);
            menu.setType(type);
            menu.setBasePrice(MenuComposition.getBasePrice(type));
            menu.getItems().clear();

            Map<MenuItemCode, Integer> composition = MenuComposition.getComposition(type);
            for (MenuItemCode code : composition.keySet()) {
                itemRepository.findByName(code.name()).ifPresent(menu.getItems()::add);
            }

            menuRepository.save(menu);
        }
    }

    private void initializeStaff() {
        String defaultPassword = "11111111";
        // 주방 직원 5명
        for (int i = 1; i <= 5; i++) {
            final int staffNum = i;
            String id = "kitchen" + staffNum;
            KitchenStaff staff = kitchenStaffRepository.findById(id).orElseGet(() -> {
                KitchenStaff newStaff = new KitchenStaff();
                newStaff.setId(id);
                newStaff.setRole(UserRole.KITCHEN_STAFF);
                newStaff.setIsLoggedIn(false);
                newStaff.setEmployeeId("K00" + staffNum);
                newStaff.setIsBusy(false);
                return newStaff;
            });
            // 비밀번호를 항상 "11111111"로 설정
            staff.setPassword(passwordEncoder.encode(defaultPassword));
            kitchenStaffRepository.save(staff);
        }

        // 배달 직원 5명
        for (int i = 1; i <= 5; i++) {
            final int staffNum = i;
            String id = "delivery" + staffNum;
            DeliveryStaff staff = deliveryStaffRepository.findById(id).orElseGet(() -> {
                DeliveryStaff newStaff = new DeliveryStaff();
                newStaff.setId(id);
                newStaff.setRole(UserRole.DELIVERY_STAFF);
                newStaff.setIsLoggedIn(false);
                newStaff.setEmployeeId("D00" + staffNum);
                newStaff.setIsBusy(false);
                return newStaff;
            });
            // 비밀번호를 항상 "11111111"로 설정
            staff.setPassword(passwordEncoder.encode(defaultPassword));
            deliveryStaffRepository.save(staff);
        }
    }

    private void initializeCoupons() {
        // 기본 쿠폰 초기화
        if (couponRepository.count() == 0) {
            List<Coupon> coupons = List.of(
                createCoupon("WELCOME10000", 10000),
                createCoupon("SAVE5000", 5000),
                createCoupon("SPECIAL20000", 20000)
            );
            couponRepository.saveAll(coupons);
        }
        
        // 단골 고객 쿠폰이 없으면 생성
        if (!couponRepository.findByCode("REGULAR10000").isPresent()) {
            Coupon regularCoupon = createCoupon("REGULAR10000", 10000);
            couponRepository.save(regularCoupon);
        }
    }

    private Coupon createCoupon(String code, int discountAmount) {
        Coupon coupon = new Coupon();
        coupon.setCode(code);
        coupon.setDiscountAmount(discountAmount);
        coupon.setIsValid(true);
        return coupon;
    }

    private void updateAllPasswords() {
        // 모든 사용자(고객, 주방 직원, 배달 직원)의 비밀번호를 "11111111"로 변경
        String newPassword = "11111111";
        String encodedPassword = passwordEncoder.encode(newPassword);

        // 모든 고객의 비밀번호 업데이트
        List<com.mrdinner.entity.Customer> customers = customerRepository.findAll();
        for (com.mrdinner.entity.Customer customer : customers) {
            customer.setPassword(encodedPassword);
            customerRepository.save(customer);
        }

        // 모든 주방 직원의 비밀번호 업데이트
        List<KitchenStaff> kitchenStaffList = kitchenStaffRepository.findAll();
        for (KitchenStaff staff : kitchenStaffList) {
            staff.setPassword(encodedPassword);
            kitchenStaffRepository.save(staff);
        }

        // 모든 배달 직원의 비밀번호 업데이트
        List<DeliveryStaff> deliveryStaffList = deliveryStaffRepository.findAll();
        for (DeliveryStaff staff : deliveryStaffList) {
            staff.setPassword(encodedPassword);
            deliveryStaffRepository.save(staff);
        }
    }
}

