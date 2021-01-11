CREATE SEQUENCE bitmex_sequence
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1;

CREATE TABLE bitmex_account
(
    id         BIGINT NOT NULL UNIQUE,
    account_id BIGINT NOT NULL,
    currency   VARCHAR(20),
    version    BIGINT DEFAULT 0 NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE bitmex_transaction
(
    id                 BIGINT      NOT NULL UNIQUE,
    transaction_id     VARCHAR(40) NOT NULL,
    transaction_type   VARCHAR(20) NOT NULL,
    account_id         BIGINT,
    units              BIGINT,
    transaction_time   TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    price              float8,
    interest           float8,
    pnl                float8,
    lnk_transaction_id VARCHAR(20),
    instrument         VARCHAR(20),
    currency           VARCHAR(20),
    version            BIGINT DEFAULT 0 NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT f_bitmex_transaction_acc FOREIGN KEY (account_id) REFERENCES bitmex_account (id)
);

create
index i_jbitmex_transaction_acc_id on bitmex_transaction (account_id);