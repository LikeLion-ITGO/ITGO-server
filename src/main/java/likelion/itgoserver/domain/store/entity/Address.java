package likelion.itgoserver.domain.store.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;

@Embeddable
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    @Column(nullable = false)
    private String roadAddress;

    @Column(nullable = false)
    private String dong; // 동네 정보 (ex. 공릉동, 하계동, 중계동)

    @Column(nullable = false)
    @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
    private Double latitude;

    @Column(nullable = false)
    @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
    private Double longitude;

    public void update(String roadAddress, String dong, Double latitude, Double longitude) {
        this.roadAddress = roadAddress;
        this.dong = dong;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}