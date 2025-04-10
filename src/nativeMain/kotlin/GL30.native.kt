@file:OptIn(ExperimentalForeignApi::class)

package io.github.etieskrill.test.kmp

import gl.*
import glfw.*
import kotlinx.cinterop.*

lateinit var window: CPointer<cnames.structs.GLFWwindow> //FIXME forward-declared types land in cnames.*, and intellij does seemingly not like them one bit

actual fun init() {
    glfwSetErrorCallback(staticCFunction { error, description ->
        println("GLFW Error: $error, description: ${description?.toKString()}")
    })

    glfwInit()

//    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3) //FIXME adding these just breaks everything anew
//    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
//    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
//
//    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) //oh... that just exists, i guess

    window = glfwCreateWindow(600, 500, "Hello World!", null, null)!!

    glfwMakeContextCurrent(window)

    if (gladLoadGL() == 0) {
        throw RuntimeException("GLAD failed to load OpenGL functions")
    } else {
        println("Loaded OpenGL context version:")
        println("\t1.0: $GLAD_GL_VERSION_1_0")
        println("\t2.0: $GLAD_GL_VERSION_2_0")
        println("\t3.0: $GLAD_GL_VERSION_3_0")
        println("\t3.2: $GLAD_GL_VERSION_3_2")
        println("\t3.3: $GLAD_GL_VERSION_3_3")
    }

}

actual fun running(): Boolean = !glfwWindowShouldClose(window)

actual fun update(delta: Float) {
    glfwSwapBuffers(window)
    glfwPollEvents()
}

actual fun whileRunning(run: (delta: Float) -> Unit) {
    while (running()) run(0f)
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

actual fun glGetProgramParameter(program: GLProgram, type: GLProgramParameterType): Int? = memScoped {
    val value = alloc<Int>(-1)
    glGetProgramiv!!(
        program.toUInt(), when (type) {
            GLProgramParameterType.LINK_STATUS -> GL_LINK_STATUS
            GLProgramParameterType.VALIDATE_STATUS -> GL_VALIDATE_STATUS
        }.toUInt(),
        value.ptr
    )
    return value.value.takeIf { it != -1 }
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
    }.toUInt()
).toInt()

actual fun glShaderSource(shader: GLShader, source: String) = memScoped {
    val sources = allocPointerTo<ByteVar>().apply { value = source.cstr.ptr }
    glShaderSource!!(shader.toUInt(), 1, sources.ptr, alloc(source.length).ptr)
}

actual fun glCompileShader(shader: GLShader) = glCompileShader!!(shader.toUInt())
actual fun glAttachShader(program: GLProgram, shader: GLShader) = glAttachShader!!(program.toUInt(), shader.toUInt())

actual fun glGetShaderParameter(shader: GLShader, type: GLShaderParameterType): Int? = memScoped {
    val value = alloc<Int>(-1) //afaik, this is never a valid return value
    glGetShaderiv!!(
        shader.toUInt(), when (type) {
            GLShaderParameterType.COMPILE_STATUS -> GL_COMPILE_STATUS
        }.toUInt(), value.ptr
    )
    return value.value.takeIf { it != -1 }
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
        }.toUInt(), first, count
    )
