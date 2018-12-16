package com.haitekuya.demo002

import com.linecorp.bot.client.LineMessagingClient
import com.linecorp.bot.model.ReplyMessage
import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.message.TextMessageContent
import com.linecorp.bot.model.message.TextMessage
import com.linecorp.bot.spring.boot.annotation.EventMapping
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler

@LineMessageHandler
class DemoController(val lineMessagingClient: LineMessagingClient) {

    @EventMapping
    fun handleTextMessageEvent(event: MessageEvent<TextMessageContent>) {
        val text = event.message.text
        val replyToken = event.replyToken

        val replyText = when (text) {
            "test" -> "test dayo"
            "test2" -> "test2 dazo"
            else -> text
        }
        lineMessagingClient.replyMessage(ReplyMessage(replyToken, TextMessage(replyText)))
    }
}
