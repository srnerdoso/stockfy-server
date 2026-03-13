package br.com.threadstech.stockfy.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Table(name = "metrics_yearly")
public class MetricYearly extends Metric {}
