package com.blocksdecoded.dex.core.manager

import com.blocksdecoded.dex.core.adapter.FeeRatePriority
import com.blocksdecoded.dex.core.adapter.IAdapter
import com.blocksdecoded.dex.core.model.AuthData
import com.blocksdecoded.dex.core.model.Coin
import com.blocksdecoded.zrxkit.ZrxKit
import io.horizontalsystems.ethereumkit.core.EthereumKit
import io.reactivex.subjects.PublishSubject
import org.web3j.tx.gas.ContractGasProvider

interface IEthereumKitManager {
    val gasProvider: ContractGasProvider
    val ethereumKit: EthereumKit?

    fun ethereumKit(authData: AuthData): EthereumKit
    fun defaultKit(): EthereumKit
    fun refresh()
    fun unlink()
}

interface IZrxKitManager {
    fun zrxKit(): ZrxKit
}

interface IAdapterManager {
    val adapters: List<IAdapter>
    val adaptersUpdatedSignal: PublishSubject<Unit>

    fun refresh()
    fun initAdapters(coins: List<Coin>)
    fun stopKits()
}

interface IFeeRateProvider {
    fun ethereumGasPrice(priority: FeeRatePriority): Long
}

interface IWordsManager {
    var isBackedUp: Boolean
    var backedUpSignal: PublishSubject<Unit>
    
    fun validate(words: List<String>)
    fun generateWords(): List<String>
}

interface IAppLocalStorage {
    var currentLanguage: String?
    var isBackedUp: Boolean
    var isBiometricOn: Boolean
    var isLightModeOn: Boolean
    var iUnderstand: Boolean
    var baseCurrencyCode: String?
    var blockTillDate: Long?
    var failedAttempts: Int?
    var lockoutUptime: Long?
    var baseEthereumProvider: String?
    
    fun clear()
}