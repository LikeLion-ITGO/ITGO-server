package likelion.itgoserver.domain.share.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "share_image",
        uniqueConstraints = @UniqueConstraint(name="uk_share_seq", columnNames={"share_id","seq"}))
public class ShareImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "share_id", nullable = false)
    private Share share;

    @Column(nullable = false)
    private Integer seq;

    @Column(nullable = false, length = 512)
    private String objectKey;

    public void update(String key) {
        this.objectKey = key;
    }

    public void linkShare(Share share) {
        this.share = share;
    }

}