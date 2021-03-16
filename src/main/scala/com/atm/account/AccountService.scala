package com.atm.account

import org.apache.kafka.streams.state.ReadOnlyKeyValueStore

class AccountService(val balanceByAccountStore: ReadOnlyKeyValueStore[String, Double]) {

  def hasBalance(account: String, value: Double, t: String): Boolean = {
    var balance: Double = balanceByAccountStore.get(account)
    println(s"balance: ${balance}, value: ${value}")
    balance = if (balance.isNaN) 0
    else balance
    val value1 = if(t.equals("Debit")) -value else value
    balance + value1 >= 0
  }

  def noBalance(account: String, value: Double, t: String): Boolean = {
    var balance = balanceByAccountStore.get(account)
    println(s"balance: ${balance}, value: ${value}")
    balance = if (balance.isNaN) 0
    else balance
    val value1 = if(t.equals("Debit")) -value else value
    balance + value1 < 0
  }
}
