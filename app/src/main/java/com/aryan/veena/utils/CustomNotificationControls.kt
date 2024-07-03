package com.aryan.veena.utils

import android.os.Bundle
import androidx.media3.session.CommandButton
import androidx.media3.session.SessionCommand
import com.aryan.veena.R

private const val REWIND_COMMAND = "REWIND_15"
private const val FORWARD_COMMAND = "FAST_FWD_15"
private const val CLOSE_COMMAND = "CLOSE"

enum class CustomNotificationControls(
    val customAction: String,
    val commandButton: CommandButton,
) {
    REWIND(
        customAction = REWIND_COMMAND,
        commandButton = CommandButton.Builder()
            .setDisplayName("Rewind")
            .setSessionCommand(SessionCommand(REWIND_COMMAND, Bundle()))
            .setIconResId(R.drawable.ic_rewind_10)
            .build(),
    ),
    FORWARD(
        customAction = FORWARD_COMMAND,
        commandButton = CommandButton.Builder()
            .setDisplayName("Forward")
            .setSessionCommand(SessionCommand(FORWARD_COMMAND, Bundle()))
            .setIconResId(R.drawable.ic_forward_10)
            .build(),
    ),
    CLOSE(
        customAction = CLOSE_COMMAND,
        commandButton = CommandButton.Builder()
            .setDisplayName("Close")
            .setSessionCommand(SessionCommand(CLOSE_COMMAND, Bundle()))
            .setIconResId(R.drawable.ic_close)
            .build(),
    )
}
