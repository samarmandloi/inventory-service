package com.pm.inventoryservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "reservation",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reservation_request_id",
                        columnNames = "request_id"
                )
        }
)
@Getter
@Setter
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "request_id",
            nullable = false,
            unique = true
    )
    private UUID requestId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "items",
            columnDefinition = "jsonb",
            nullable = false
    )
    private String items;
    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private ReservationStatus status;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @UpdateTimestamp
    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;
}