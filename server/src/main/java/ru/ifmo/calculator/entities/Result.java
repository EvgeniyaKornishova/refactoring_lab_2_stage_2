package ru.ifmo.calculator.entities;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name="results")
public class Result {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Float value;

    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private User user;
}
