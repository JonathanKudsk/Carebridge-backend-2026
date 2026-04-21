package com.carebridge.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "shift")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Shift {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "start_shift", nullable = false)
	private LocalDateTime startShift;

	@Column(name = "end_shift", nullable = false)
	private LocalDateTime endShift;

	@Column(name = "shift_type", nullable = false)
	private String shiftType;

	@Column(name = "location", nullable = false)
	private String location;

	@Column(name = "status", nullable = false)
	private String status;

	@Column(name = "plan_period_id", nullable = false)
	private Long planPeriodId;

	@Column(name = "assigned_user_id", nullable = false)
	private Long assignedUserId;

	@Column(name = "created_by", nullable = false)
	private Long createdBy;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt = LocalDateTime.now();
}