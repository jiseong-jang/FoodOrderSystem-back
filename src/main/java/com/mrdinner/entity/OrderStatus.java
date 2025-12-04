package com.mrdinner.entity;

public enum OrderStatus {
    RECEIVED,   // 접수 완료
    COOKING,    // 조리 중
    DELIVERING, // 배달 중
    COMPLETED,  // 배달 완료
    CANCELLED,  // 고객 취소
    REJECTED    // 주방 거절
}

