package com.fridaytech.dex.data.storage

import com.fridaytech.dex.core.model.EnabledCoin
import com.fridaytech.dex.data.storage.dao.EnabledCoinsDao
import io.reactivex.Flowable
import java.util.concurrent.Executors

class EnabledCoinsStorage(
    private val enabledCoinsDao: EnabledCoinsDao
) : IEnabledCoinsStorage {
    private val executor = Executors.newSingleThreadExecutor()

    override fun enabledCoinsObservable(): Flowable<List<EnabledCoin>> =
        enabledCoinsDao.getEnabledCoins()

    override fun save(coins: List<EnabledCoin>) {
        executor.execute {
            enabledCoinsDao.deleteAll()
            enabledCoinsDao.insertCoins(coins)
        }
    }

    override fun deleteAll() {
        executor.execute { enabledCoinsDao.deleteAll() }
    }
}
