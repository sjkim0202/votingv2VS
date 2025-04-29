package com.example.votingv2.dto;

import java.math.BigInteger;
import java.util.List;

public record VoteResultResponseDto(
        String title,
        List<String> items,
        List<BigInteger> counts
) {}
