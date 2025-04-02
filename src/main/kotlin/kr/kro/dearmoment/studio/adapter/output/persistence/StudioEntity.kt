package kr.kro.dearmoment.studio.adapter.output.persistence

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import kr.kro.dearmoment.common.persistence.Auditable
import kr.kro.dearmoment.studio.domain.Studio
import kr.kro.dearmoment.studio.domain.StudioStatus
import org.hibernate.annotations.ColumnDefault
import java.util.UUID

@Entity
@Table(
    name = "studios",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id"])],
)
class StudioEntity(
    @Id
    @Column(name = "studio_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @Column(nullable = false)
    val userId: UUID,
    @Column
    @ColumnDefault(value = "0")
    val cast: Int,
    name: String,
    contact: String,
    studioIntro: String,
    artistsIntro: String,
    instagramUrl: String,
    kakaoChannelUrl: String,
    reservationNotice: String,
    cancellationPolicy: String,
    status: String,
    partnerShops: MutableSet<StudioPartnerShopEmbeddable>,
) : Auditable() {
    @Column(nullable = false)
    var name: String = name
        protected set

    @Column(nullable = false)
    var contact: String = contact
        protected set

    @Column(nullable = false)
    var studioIntro: String = studioIntro
        protected set

    @Column(nullable = false)
    var artistsIntro: String = artistsIntro
        protected set

    @Column(nullable = false)
    var instagramUrl: String = instagramUrl
        protected set

    @Column(nullable = false)
    var kakaoChannelUrl: String = kakaoChannelUrl
        protected set

    @Column
    var reservationNotice: String = reservationNotice
        protected set

    @Column
    var cancellationPolicy: String = cancellationPolicy
        protected set

    @Column
    var status: String = status
        protected set

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "studio_partner_shops",
        joinColumns = [JoinColumn(name = "studio_id")],
    )
    var partnerShops: MutableSet<StudioPartnerShopEmbeddable> = partnerShops
        protected set

    fun update(entity: StudioEntity) {
        name = entity.name
        contact = entity.contact
        studioIntro = entity.studioIntro
        artistsIntro = entity.artistsIntro
        instagramUrl = entity.instagramUrl
        kakaoChannelUrl = entity.kakaoChannelUrl
        reservationNotice = entity.reservationNotice
        cancellationPolicy = entity.cancellationPolicy
        status = entity.status

        partnerShops = entity.partnerShops.toMutableSet()
    }

    fun toDomain() =
        Studio(
            id = id,
            userId = userId,
            name = name,
            contact = contact,
            studioIntro = studioIntro,
            artistsIntro = artistsIntro,
            instagramUrl = instagramUrl,
            kakaoChannelUrl = kakaoChannelUrl,
            reservationNotice = reservationNotice,
            cancellationPolicy = cancellationPolicy,
            status = StudioStatus.from(status),
            partnerShops = partnerShops.map { it.toDomain() },
            isCasted = if (cast == 1) true else false,
        )

    companion object {
        fun from(domain: Studio) =
            StudioEntity(
                name = domain.name,
                userId = domain.userId,
                contact = domain.contact,
                studioIntro = domain.studioIntro,
                artistsIntro = domain.artistsIntro,
                instagramUrl = domain.instagramUrl,
                kakaoChannelUrl = domain.kakaoChannelUrl,
                reservationNotice = domain.reservationNotice,
                cancellationPolicy = domain.cancellationPolicy,
                status = domain.status.name,
                partnerShops =
                    domain.partnerShops.map {
                        StudioPartnerShopEmbeddable(it.category.name, it.name, it.urlLink)
                    }.toMutableSet(),
                cast = if (domain.isCasted) 1 else 0,
            )
    }
}
