package com.mrdinner.controller;

import com.mrdinner.dto.ApiResponse;
import com.mrdinner.dto.MenuDto;
import com.mrdinner.entity.MenuType;
import com.mrdinner.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/menus")
public class MenuController {
    @Autowired
    private MenuService menuService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllMenus() {
        try {
            List<MenuDto> menus = menuService.getAllMenus();
            return ResponseEntity.ok(ApiResponse.success("메뉴 목록 조회 성공", menus));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getMenuById(@PathVariable Long id) {
        try {
            MenuDto menu = menuService.getMenuById(id);
            return ResponseEntity.ok(ApiResponse.success("메뉴 조회 성공", menu));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse> getMenuByType(@PathVariable MenuType type) {
        try {
            MenuDto menu = menuService.getMenuByType(type);
            return ResponseEntity.ok(ApiResponse.success("메뉴 조회 성공", menu));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

