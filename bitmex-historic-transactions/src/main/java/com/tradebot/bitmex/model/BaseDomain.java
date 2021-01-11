package com.tradebot.bitmex.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.SequenceGenerator;
import javax.persistence.Version;
import lombok.Data;

@Data
@MappedSuperclass
public class BaseDomain implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bitmex_generator")
    @SequenceGenerator(name = "bitmex_generator", sequenceName = "bitmex_sequence", allocationSize = 1)
    @Column(name = "id", unique = true, updatable = false, nullable = false)
    private Long id;

    @Version
    @Column(name = "version")
    private Long version;
}
