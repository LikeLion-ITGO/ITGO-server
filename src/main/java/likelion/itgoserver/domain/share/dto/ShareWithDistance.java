package likelion.itgoserver.domain.share.dto;

import likelion.itgoserver.domain.share.entity.Share;

public record ShareWithDistance(
        Share share,
        Double distanceKm
) {}