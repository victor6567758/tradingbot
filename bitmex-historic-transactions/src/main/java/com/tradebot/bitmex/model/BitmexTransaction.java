package com.tradebot.bitmex.model;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "bitmex_transaction")
public class BitmexTransaction extends BaseDomain {

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = true)
    private BitmexAccount account;

    @Column(name = "units", nullable = true)
    private Long units;

    @Column(name = "transaction_time")
    private Timestamp transactionTime;

    @Column(name = "price")
    private Double price;

    @Column(name = "interest")
    private Double interest;

    @Column(name = "pnl")
    private Double pnl;

    @Column(name = "lnk_transaction_id")
    private String linkedTransactionId;

    @Column(name = "instrument", nullable = false)
    private String instrument;
}
