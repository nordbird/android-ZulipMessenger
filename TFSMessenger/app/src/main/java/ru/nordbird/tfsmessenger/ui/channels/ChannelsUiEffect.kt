package ru.nordbird.tfsmessenger.ui.channels

sealed class ChannelsUiEffect {

    class ActionError(val error: Throwable) : ChannelsUiEffect()

}
