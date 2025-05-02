package com.example.votingv2.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user_blockchain_key")
public class UserBlockchainKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "private_key", nullable = false, length = 1000)
    private String privateKey;

    @Column(name = "user_id")
    private Long userId; // User의 id (nullable)

}
