DELETE FROM TRANSACTIONS;

DELE FROM WALLETS;

INSERT INTO WALLETS (
    ID, FULL_NAME, CPF, EMAIL, "PASSWORD", "TYPE", BALANCE
)
VALUES (
    1, 'Welington - User', 00106921347, 'wcfilho98@hotmail.com', '19210323', 1, 1000.00
);

INSERT INTO WALLETS (
    ID, FULL_NAME, CPF, EMAIL, "PASSWORD", "TYPE", BALANCE
)
VALUES (
    2, 'Doralicy - Lojista', 12345678900, 'dcmarialima@gmail.com', '23062022', 2, 1000.00
);