//package com.nhan.to_do_api.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDateTime;
//
//
//@Entity
//@Table(name = "refresh_tokens")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class RefreshToken {
//    @ManyToOne
//    @JoinColumn(name = "user_id")
//    private User user;
//    private Long id;
//    private Boolean revoked;
//    private LocalDateTime createAt;
//    private LocalDateTime expiredAt;
//    private LocalDateTime updateAt;
//}
