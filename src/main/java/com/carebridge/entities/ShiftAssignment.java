package com.carebridge.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftAssignment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long shiftId;

	private Long userId;

	private Instant assignedAt;

	private Long assignedBy;

	@PrePersist
	void prePersist() {
		if (assignedAt == null) assignedAt = Instant.now();
	}
}
