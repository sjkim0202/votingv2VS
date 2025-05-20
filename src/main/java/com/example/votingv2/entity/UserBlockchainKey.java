package com.example.votingv2.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.AllArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "user_blockchain_key")
public class UserBlockchainKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "private_key", nullable = false, length = 1000)
    private String privateKey;

    @Column(name = "public_key", nullable = false, length = 1000)
    private String publicKey;

    @Column(name = "address", nullable = false, length = 100)
    private String address;
}
