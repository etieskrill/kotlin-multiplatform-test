@file:OptIn(ExperimentalForeignApi::class)

package io.github.etieskrill.test.kmp

import gl.GL_NO_ERROR
import gl.*
import glfw.glfwCreateWindow
import glfw.glfwInit
import kotlinx.cinterop.*

actual fun glInit() {
    glfwInit()
    glfwCreateWindow(600, 500, "Hello World!", null, null)
}

actual fun glGetError(): GLErrorType {
    return when (val error = glGetError!!().toInt()) {
        GL_NO_ERROR -> GLErrorType.NO_ERROR
        else -> error("Unexpected OpenGL error type: $error")
    }
}

actual fun glCreateProgram(): GLProgram? = glCreateProgram!!().toInt()
actual fun glLinkProgram(program: GLProgram) = glLinkProgram!!(program.toUInt())
actual fun glValidateProgram(program: GLProgram) = glValidateProgram!!(program.toUInt())
actual fun glUseProgram(program: GLProgram) = glUseProgram!!(program.toUInt())

actual fun glGetProgramParameter(program: GLProgram, type: GLProgramParameterType): Any? = memScoped {
    val value = alloc<Int>(0).ptr
    glGetProgramiv!!(
        program.toUInt(), when (type) {
            GLProgramParameterType.LINK_STATUS -> GL_LINK_STATUS
            else -> error("Unexpected program parameter: $type")
        }.toUInt(),
        value
    )
    return value.pointed.value
}

actual fun glGetProgramInfoLog(program: GLProgram): String? = memScoped {
    val size = alloc<Int>(0)
    val log = alloc<ByteVar>()

    glGetProgramInfoLog!!(program.toUInt(), 8192, size.ptr, log.ptr)

    return log.readValues(size.value).getBytes().decodeToString()
}

@OptIn(ExperimentalForeignApi::class)
actual fun glCreateShader(type: GLShaderType): GLShader? = glCreateShader!!(
    when (type) {
        GLShaderType.VERTEX -> GL_VERTEX_SHADER
        GLShaderType.FRAGMENT -> GL_FRAGMENT_SHADER
        else -> error("Unexpected shader type: $type")
    }.toUInt()
).toInt()

actual fun glShaderSource(shader: GLShader, source: String) = memScoped {
    val sources = allocPointerTo<ByteVar>().apply { value = source.cstr.ptr }
    glShaderSource!!(shader.toUInt(), 1, sources.ptr, alloc(source.length).ptr)
}

actual fun glCompileShader(shader: GLShader) = glCompileShader!!(shader.toUInt())
actual fun glAttachShader(program: GLProgram, shader: GLShader) = glAttachShader!!(program.toUInt(), shader.toUInt())

actual fun glGetShaderParameter(shader: GLShader, type: GLShaderParameterType): Any? = memScoped {
    val value = alloc<Int>(0)
    glGetShaderiv!!(
        shader.toUInt(), when (type) {
            GLShaderParameterType.COMPILE_STATUS -> GL_COMPILE_STATUS
        }.toUInt(), value.ptr
    )
    return value.value
}

actual fun glGetShaderInfoLog(shader: GLShader): String? = memScoped {
    val size = alloc<Int>(0)
    val log = alloc<ByteVar>()

    glGetProgramInfoLog!!(shader.toUInt(), 8192, size.ptr, log.ptr)

    return log.readValues(size.value).getBytes().decodeToString()
}

@OptIn(ExperimentalForeignApi::class)
actual fun glDrawArrays(mode: GLDrawMode, first: Int, count: Int) =
    glDrawArrays!!(
        when (mode) {
            GLDrawMode.TRIANGLES -> GL_TRIANGLES
            GLDrawMode.TRIANGLE_STRIP -> GL_TRIANGLE_STRIP
            else -> error("Unexpected draw mode: $mode")
        }.toUInt(), first, count
    )
