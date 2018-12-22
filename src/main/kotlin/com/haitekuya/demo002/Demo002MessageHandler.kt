package com.haitekuya.demo002

import com.linecorp.bot.client.LineMessagingClient
import com.linecorp.bot.model.ReplyMessage
import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.message.TextMessageContent
import com.linecorp.bot.model.message.TextMessage
import com.linecorp.bot.spring.boot.annotation.EventMapping
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler
import java.util.*

@LineMessageHandler
class Demo002MessageHandler(val lineMessagingClient: LineMessagingClient) {

    @EventMapping
    fun handleTextMessageEvent(event: MessageEvent<TextMessageContent>) {
        val text = event.message.text
        val replyToken = event.replyToken

        // check text
        val regex = Regex("^https://www.youtube.com/watch\\?v=\\w{3,15}$")

        if (!regex.containsMatchIn(text)) {
            sendErrorMessage(replyToken, "Invalid youtube url")
            return
        }

        // Download Movie
        val tmpFileName = UUID.randomUUID().toString() + ".m4a"
        val command = "youtube-dl -x --audio-format m4a -o $tmpFileName $text"
        val process = Runtime.getRuntime().exec(command)
        process.waitFor()
        process.destroy()

        // Upload to firebase

        // Send reply message
        lineMessagingClient.replyMessage(ReplyMessage(replyToken, TextMessage("ok")))
    }

    private fun sendErrorMessage(replyToken: String, message: String) =
            lineMessagingClient.replyMessage(ReplyMessage(replyToken, TextMessage(message)))
}
