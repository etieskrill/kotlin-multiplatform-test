package io.github.etieskrill.test.kmp

import WebGL2RenderingContext
import forceCast
import kotlinx.browser.document
import kotlinx.dom.appendElement
import org.khronos.webgl.WebGLRenderingContextBase
import org.w3c.dom.HTMLCanvasElement

lateinit var context: WebGLRenderingContextBase

actual fun glInit() {
    document.body!!.appendElement("canvas") {
        this as HTMLCanvasElement

        width = 600
        height = 500

        val gl = getContext("webgl2") as? WebGL2RenderingContext
            ?: error("Could not get WebGL2 rendering context")

        context = gl
    }
}

actual fun glGetError(): GLErrorType {
    val error = context.getError()
    return when (error) {
        WebGLRenderingContextBase.NO_ERROR -> GLErrorType.NO_ERROR
        else -> error("Unknown WebGL error code: $error")
    }
}

actual fun glCreateProgram(): GLProgram? = context.createProgram().forceCast<JsNumber>()?.toInt()
actual fun glLinkProgram(program: GLProgram) = context.linkProgram(program.toJsNumber().forceCast())
actual fun glValidateProgram(program: GLProgram) = context.validateProgram(program.toJsNumber().forceCast())
actual fun glUseProgram(program: GLProgram) = context.useProgram(program.toJsNumber().forceCast())

actual fun glGetProgramParameter(program: GLProgram, type: GLProgramParameterType): Any? =
    context.getProgramParameter(
        program.toJsNumber().forceCast(), when (type) {
            GLProgramParameterType.LINK_STATUS -> WebGLRenderingContextBase.LINK_STATUS
            GLProgramParameterType.VALIDATE_STATUS -> WebGLRenderingContextBase.VALIDATE_STATUS
        }
    )?.let {
        when (it) {
            is JsNumber -> it.toDouble().roundIfInt()
            is JsBoolean -> it.toBoolean()
            else -> error("Unknown program parameter type: ${it::class.simpleName}")
        }
    }

actual fun glGetProgramInfoLog(program: GLProgram): String? =
    context.getProgramInfoLog(program.toJsNumber().forceCast())

actual fun glCreateShader(type: GLShaderType): GLShader? =
    context.createShader(
        when (type) {
            GLShaderType.VERTEX -> WebGLRenderingContextBase.VERTEX_SHADER
            GLShaderType.FRAGMENT -> WebGLRenderingContextBase.FRAGMENT_SHADER
        }
    ).forceCast<JsNumber>()?.toInt()

actual fun glShaderSource(shader: GLShader, source: String) =
    context.shaderSource(shader.toJsNumber().forceCast(), source)
actual fun glCompileShader(shader: GLShader) =
    context.compileShader(shader.toJsNumber().forceCast())
actual fun glAttachShader(program: GLProgram, shader: GLShader) =
    context.attachShader(program.toJsNumber().forceCast(), shader.toJsNumber().forceCast())

actual fun glGetShaderParameter(shader: GLShader, type: GLShaderParameterType): Any? =
    context.getShaderParameter(shader.toJsNumber().forceCast(), when (type) {
        GLShaderParameterType.COMPILE_STATUS -> WebGLRenderingContextBase.COMPILE_STATUS
        else -> error("Unknown shader parameter type: ${type.name}")
    })
actual fun glGetShaderInfoLog(shader: GLShader): String? =
    context.getShaderInfoLog(shader.toJsNumber().forceCast())

actual fun glDrawArrays(mode: GLDrawMode, first: Int, count: Int) =
    context.drawArrays(when (mode) {
        GLDrawMode.TRIANGLES -> WebGLRenderingContextBase.TRIANGLES
        GLDrawMode.TRIANGLE_STRIP -> WebGLRenderingContextBase.TRIANGLE_STRIP
        else -> error("Unknown draw mode: ${mode.name}")
    }, first, count)

private fun Double.roundIfInt() = if (this == this.toInt().toDouble()) this.toInt() else this
