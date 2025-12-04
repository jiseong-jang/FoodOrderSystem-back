package com.mrdinner.controller;

import com.mrdinner.dto.ApiResponse;
import com.mrdinner.dto.ItemDto;
import com.mrdinner.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {
    @Autowired
    private MenuService menuService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllItems() {
        try {
            List<ItemDto> items = menuService.getAllItems();
            return ResponseEntity.ok(ApiResponse.success("아이템 목록 조회 성공", items));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getItemById(@PathVariable Long id) {
        try {
            ItemDto item = menuService.getItemById(id);
            return ResponseEntity.ok(ApiResponse.success("아이템 조회 성공", item));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

