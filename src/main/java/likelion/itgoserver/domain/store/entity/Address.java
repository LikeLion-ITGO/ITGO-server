package likelion.itgoserver.domain.store.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class Address {

    @Column(nullable = false)
    private String zipCode;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
   private String detail;

    @Column(nullable = false)
    private String dong;

    public String toRoadAddress() {
        return city + " " + district + " " + detail;
    }

    public void update(String zipCode, String city, String district, String detail, String dong) {
        this.zipCode = zipCode;
        this.city = city;
        this.district = district;
        this.detail = detail;
        this.dong = dong;
    }

}