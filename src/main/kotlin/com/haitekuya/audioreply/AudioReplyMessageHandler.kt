package com.haitekuya.audioreply

import com.linecorp.bot.client.LineMessagingClient
import com.linecorp.bot.model.ReplyMessage
import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.message.TextMessageContent
import com.linecorp.bot.model.message.AudioMessage
import com.linecorp.bot.model.message.TextMessage
import com.linecorp.bot.spring.boot.annotation.EventMapping
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.stream.Collectors


@LineMessageHandler
class AudioReplyMessageHandler(
    val lineMessagingClient: LineMessagingClient,
    val audioReplyProperties: AudioReplyProperties
) {

    @EventMapping
    fun handleTextMessageEvent(event: MessageEvent<TextMessageContent>) {
        val text = event.message.text
        val replyToken = event.replyToken
        val tmpFileName = UUID.randomUUID().toString() + ".m4a"

        // Check whether text is youtube url or not
        val regex = Regex("^https://www.youtube.com/watch\\?v=[A-Za-z0-9_-]{11}$")
        if (!regex.containsMatchIn(text)) {
            sendErrorMessage(replyToken, "Invalid youtube url")
            return
        }

        // Download movie and save as m4a audio file
        val process = Runtime.getRuntime().exec("youtube-dl -x --audio-format m4a -o /tmp/$tmpFileName $text")
        process.waitFor()
        process.destroy()

        // Make duration file by using ffprobe
        val command2 = arrayOf(
            "/bin/sh",
            "-c",
            "ffprobe -i /tmp/$tmpFileName -show_format -v quiet | grep duration | sed -n 's/duration=//p' > /tmp/$tmpFileName.duration"
        )
        val process2 = Runtime.getRuntime().exec(command2)
        process2.waitFor()
        process2.destroy()

        // Ignore after the decimal point
//        val duration = Files.readString(Path.of("/tmp/$tmpFileName.duration")).substringBefore(".")
        val duration = "60"

        // Send reply message by reply message (needs to reply in 30 seconds.)
        lineMessagingClient.replyMessage(
            ReplyMessage(
                replyToken,
                AudioMessage(audioReplyProperties.hostName + "/audio/" + tmpFileName, duration.toInt() * 1000)
            )
        )
    }

    private fun sendErrorMessage(replyToken: String, message: String) =
            lineMessagingClient.replyMessage(ReplyMessage(replyToken, TextMessage(message)))
}

@Controller
class AudioReplyController {

    @GetMapping("/audio/{file}.m4a", produces = ["audio/m4a"])
    @ResponseBody
    fun getAudio(@PathVariable file: String): ByteArray {
        val path = Path.of("/tmp/$file.m4a")
        if (!Files.exists(path)) {
            throw NotFoundException()
        }
        return Files.readAllBytes(path)
    }

    @GetMapping("/list")
    @ResponseBody
    fun getList() =
        Files.list(Path.of("/tmp"))
            .map { it.fileName.toString() }
            .collect(Collectors.joining("\n"))

    @GetMapping("/file/{file}")
    @ResponseBody
    fun getFile(@PathVariable file: String): ByteArray {
        val path = Path.of("/tmp/$file")
        if (!Files.exists(path)) {
            throw NotFoundException()
        }
        return Files.readAllBytes(path)
    }
}

@Component
@ConfigurationProperties(prefix = "audio.reply")
class AudioReplyProperties {
    lateinit var hostName: String
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFoundException : RuntimeException()
