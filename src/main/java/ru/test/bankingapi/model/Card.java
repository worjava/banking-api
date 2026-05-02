package ru.test.bankingapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Банковская карта с владельцем, сроком действия и балансом.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "cards",
        indexes = {
                @Index(name = "idx_cards_owner_status", columnList = "owner_id,status"),
                @Index(name = "idx_cards_owner_last_four", columnList = "owner_id,last_four"),
                @Index(name = "idx_cards_status_expiration", columnList = "status,expiration_date")
        }
)
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "encrypted_number", nullable = false, length = 512, updatable = false)
    private String encryptedNumber;

    @Column(name = "number_hash", nullable = false, unique = true, length = 64, updatable = false)
    private String numberHash;

    @Column(name = "last_four", nullable = false, length = 4, updatable = false)
    private String lastFour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false, updatable = false)
    private AppUser owner;

    @Column(name = "expiration_date", nullable = false, updatable = false)
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    @Column(nullable = false)
    private Long version;

    public static Card issue(
            String encryptedNumber,
            String numberHash,
            String lastFour,
            AppUser owner,
            LocalDate expirationDate,
            BigDecimal balance
    ) {
        Card card = new Card();
        card.encryptedNumber = encryptedNumber;
        card.numberHash = numberHash;
        card.lastFour = lastFour;
        card.owner = owner;
        card.expirationDate = expirationDate;
        card.status = CardStatus.ACTIVE;
        card.balance = balance == null ? BigDecimal.ZERO : balance;
        return card;
    }

    public void block() {
        this.status = CardStatus.BLOCKED;
    }

    public void activate() {
        this.status = CardStatus.ACTIVE;
    }

    public void expire() {
        this.status = CardStatus.EXPIRED;
    }

    public void debit(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public CardStatus resolveStatus(LocalDate today) {
        return expirationDate.isBefore(today) ? CardStatus.EXPIRED : status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card)) return false;
        return id != null && id.equals(((Card) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
