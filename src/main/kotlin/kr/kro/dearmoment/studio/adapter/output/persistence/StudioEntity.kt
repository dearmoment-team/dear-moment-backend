package kr.kro.dearmoment.studio.adapter.output.persistence

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import kr.kro.dearmoment.common.converter.BlankToSpaceConverter
import kr.kro.dearmoment.common.persistence.Auditable
import kr.kro.dearmoment.product.adapter.out.persistence.PartnerShopEmbeddable
import kr.kro.dearmoment.product.domain.model.option.PartnerShop
import kr.kro.dearmoment.product.domain.model.option.PartnerShopCategory
import kr.kro.dearmoment.studio.domain.Studio
import kr.kro.dearmoment.studio.domain.StudioStatus
import org.hibernate.annotations.ColumnDefault
import java.util.UUID

@Entity
@Table(
    name = "studios",
)
class StudioEntity(
    @Id
    @Column(name = "studio_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @Column(nullable = false)
    val userId: UUID,
    name: String,
    contact: String,
    studioIntro: String,
    artistsIntro: String,
    instagramUrl: String,
    kakaoChannelUrl: String,
    reservationNotice: String = "",
    cancellationPolicy: String = "",
    status: String,
    partnerShops: MutableSet<PartnerShopEmbeddable>,
    isCasted: Boolean = false,
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
    @Convert(converter = BlankToSpaceConverter::class)
    var reservationNotice: String = reservationNotice
        protected set

    @Column
    @Convert(converter = BlankToSpaceConverter::class)
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
    var partnerShops: MutableSet<PartnerShopEmbeddable> = partnerShops
        protected set

    @Column
    @ColumnDefault(value = "0")
    var cast: Int = if (isCasted) 1 else 0
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
        cast = entity.cast
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
            partnerShops =
                partnerShops.map {
                    PartnerShop(
                        category = it.category ?: PartnerShopCategory.ETC,
                        name = it.name,
                        link = it.link,
                    )
                },
            isCasted = cast == 1,
        )

    companion object {
        fun from(domain: Studio) =
            StudioEntity(
                id = domain.id,
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
                        PartnerShopEmbeddable(it.category, it.name, it.link)
                    }.toMutableSet(),
                isCasted = domain.isCasted,
            )
    }
}
