package dev.ahmedmourad.sherlock.domain.dagger.modules

import dagger.Binds
import dagger.Module
import dev.ahmedmourad.sherlock.domain.bus.Bus
import dev.ahmedmourad.sherlock.domain.bus.BusImpl
import dev.ahmedmourad.sherlock.domain.dagger.modules.qualifiers.CheckInternetConnectivityInteractorQualifier
import dev.ahmedmourad.sherlock.domain.dagger.modules.qualifiers.InternalApi
import dev.ahmedmourad.sherlock.domain.dagger.modules.qualifiers.ObserveInternetConnectivityInteractorQualifier
import dev.ahmedmourad.sherlock.domain.interactors.common.CheckInternetConnectivityInteractor
import dev.ahmedmourad.sherlock.domain.interactors.common.CheckInternetConnectivityInteractorImpl
import dev.ahmedmourad.sherlock.domain.interactors.common.ObserveInternetConnectivityInteractor
import dev.ahmedmourad.sherlock.domain.interactors.common.ObserveInternetConnectivityInteractorImpl

@Module
internal interface CommonModule {

    @Binds
    @ObserveInternetConnectivityInteractorQualifier
    fun bindObserveInternetConnectivityInteractor(
            interactor: ObserveInternetConnectivityInteractorImpl
    ): ObserveInternetConnectivityInteractor

    @Binds
    @CheckInternetConnectivityInteractorQualifier
    fun bindCheckInternetConnectivityInteractor(
            interactor: CheckInternetConnectivityInteractorImpl
    ): CheckInternetConnectivityInteractor

    @Binds
    @InternalApi
    fun bindBus(
            bus: BusImpl
    ): Bus
}
