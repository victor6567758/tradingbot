package com.tradebot.bitmex.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Data
@Table(name = "bitmex_account")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY, region = "accounts")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BitmexAccount extends BaseDomain {

    @Column(name = "account_id", nullable = false, unique = true)
    private Long accountId;

    @Column(name = "currency", nullable = false, unique = true)
    private String accountCurrency;
}
