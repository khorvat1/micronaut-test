package com.example.controller

import com.example.dto.DummyResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO


@Controller
class ImgController {

    @Get("/gif")
    fun getGif(): DummyResponse {

        val inputStream = this.javaClass.classLoader.getResourceAsStream("Drawing-1.gif").readAllBytes()
        val imageInputStream = ByteArrayInputStream(inputStream)
        val bufferedImage = ImageIO.read(imageInputStream)

        val convertedBufferedImage = BufferedImage(
                bufferedImage.width,
                bufferedImage.height,
                BufferedImage.TYPE_3BYTE_BGR
        )

        convertedBufferedImage.createGraphics().drawRenderedImage(bufferedImage, null)
        val asd = (convertedBufferedImage.raster.dataBuffer as DataBufferByte).data

        return DummyResponse("gif", "second", null)
    }

    @Get("/tiff")
    fun getTiff(): DummyResponse {

        val inputStream = this.javaClass.classLoader.getResourceAsStream("Drawing-1.tiff").readAllBytes()
        val imageInputStream = ByteArrayInputStream(inputStream)
        val bufferedImage = ImageIO.read(imageInputStream)

        val convertedBufferedImage = BufferedImage(
                bufferedImage.width,
                bufferedImage.height,
                BufferedImage.TYPE_3BYTE_BGR
        )

        convertedBufferedImage.createGraphics().drawRenderedImage(bufferedImage, null)
        val asd = (convertedBufferedImage.raster.dataBuffer as DataBufferByte).data

        return DummyResponse("tiff", "second", null)
    }
}