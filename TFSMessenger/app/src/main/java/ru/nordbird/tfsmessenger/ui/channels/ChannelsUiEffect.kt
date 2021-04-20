package ru.nordbird.tfsmessenger.ui.channels

sealed class ChannelsUiEffect {

    class SearchStreamsError(val error: Throwable) : ChannelsUiEffect()

}
