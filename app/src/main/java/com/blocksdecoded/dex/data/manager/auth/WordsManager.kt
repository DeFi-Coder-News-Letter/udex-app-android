package com.blocksdecoded.dex.data.manager.auth

import com.blocksdecoded.dex.data.manager.IWordsManager
import com.blocksdecoded.dex.data.shared.IAppPreferences
import io.horizontalsystems.hdwalletkit.Mnemonic
import io.reactivex.subjects.PublishSubject

class WordsManager(private val preferences: IAppPreferences) :
    IWordsManager {

    override var isBackedUp: Boolean
        get() = preferences.isBackedUp
        set(value) {
            preferences.isBackedUp = value
            backedUpSignal.onNext(Unit)
        }

    override var backedUpSignal = PublishSubject.create<Unit>()

    @Throws(Mnemonic.MnemonicException::class)
    override fun validate(words: List<String>) {
        Mnemonic().validate(words)
    }

    override fun generateWords() : List<String> = Mnemonic().generate()

}