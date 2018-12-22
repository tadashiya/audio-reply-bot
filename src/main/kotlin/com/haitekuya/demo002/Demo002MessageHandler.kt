package com.haitekuya.demo002

import com.linecorp.bot.client.LineMessagingClient
import com.linecorp.bot.model.ReplyMessage
import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.message.TextMessageContent
import com.linecorp.bot.model.message.AudioMessage
import com.linecorp.bot.model.message.TextMessage
import com.linecorp.bot.spring.boot.annotation.EventMapping
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler
import org.jetbrains.annotations.NotNull
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import java.nio.file.Files
import java.nio.file.Path
import java.util.*


@LineMessageHandler
class Demo002MessageHandler(val lineMessagingClient: LineMessagingClient, val demo002Properties: Demo002Properties) {

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
        val command = "youtube-dl -x --audio-format m4a -o /tmp/$tmpFileName $text"
        val process = Runtime.getRuntime().exec(command)
        process.waitFor()
        process.destroy()

        // Send reply message
        lineMessagingClient.replyMessage(ReplyMessage(replyToken, AudioMessage(demo002Properties.hostName + tmpFileName, 60000)))
    }

    private fun sendErrorMessage(replyToken: String, message: String) =
            lineMessagingClient.replyMessage(ReplyMessage(replyToken, TextMessage(message)))
}

@Controller
class Demo002Controller {

    @GetMapping("/audio/{file}.m4a", produces = ["audio/m4a"])
    @ResponseBody
    fun getAudio(@PathVariable file: String): ByteArray {
        val path = Path.of("/tmp/$file.m4a")
        if (!Files.exists(path)) {
            throw NotFoundException()
        }
        return Files.readAllBytes(path)
    }
}

@Validated
@Component
@ConfigurationProperties(prefix = "demo002")
class Demo002Properties {
    @NotNull
    lateinit var hostName: String
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFoundException : RuntimeException()
