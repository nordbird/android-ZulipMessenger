package ru.nordbird.tfsmessenger.ui.profile.base

import ru.nordbird.tfsmessenger.ui.mvi.base.MviView
import ru.nordbird.tfsmessenger.ui.profile.ProfileState

interface ProfileView : MviView<ProfileState, ProfileUiEffect>