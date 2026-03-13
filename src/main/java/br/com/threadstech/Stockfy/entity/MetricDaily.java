package br.com.threadstech.stockfy.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Table(name = "metrics_daily")
public class MetricDaily extends Metric {}
