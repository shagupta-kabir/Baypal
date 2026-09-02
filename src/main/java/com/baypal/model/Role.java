package com.baypal.model;

// three kinds of accounts in this app:
// - USER: a regular wallet holder
// - ADMIN: can see every user/transaction and fund USER wallets from the admin panel
// - AUTHOR: sits above ADMIN - can freeze any ADMIN/USER account, fund ADMIN
//   wallets, and see platform-wide revenue and admin activity
public enum Role {
    USER,
    ADMIN,
    AUTHOR
}
