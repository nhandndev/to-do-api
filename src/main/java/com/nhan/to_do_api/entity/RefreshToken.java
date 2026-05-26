package com.nhan.to_do_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
   @Id
   private Long id;
   private String token;
   private Boolean revoked;
   private LocalDateTime expiryAt;
   private LocalDateTime createAt;
   private LocalDateTime updatedAt;
   @ManyToOne
   @JoinColumn(name = "user_id")
   private User user;
}
