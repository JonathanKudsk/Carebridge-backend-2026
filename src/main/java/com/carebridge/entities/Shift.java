package com.carebridge.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Shift {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalDateTime startShift;

	private LocalDateTime endShift;

	private String shiftType;

	private String location;

	private String status;

	private Long planPeriodId;

	private Long assignedUserId;

	private Long createdBy;

	private LocalDateTime createdAt = LocalDateTime.now();
}
