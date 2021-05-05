package ru.nordbird.tfsmessenger.ui.channels.base

sealed class ChannelsUiEffect {

    class ActionError(val error: Throwable) : ChannelsUiEffect()

}
