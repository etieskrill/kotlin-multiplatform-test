package io.github.etieskrill.test.kmp

import WebGL2RenderingContext
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.dom.appendElement
import org.khronos.webgl.WebGLProgram
import org.khronos.webgl.WebGLShader
import org.w3c.dom.HTMLCanvasElement

lateinit var context: WebGL2RenderingContext
lateinit var glContext: WebGLContext

class WebGLContext(
    val context: WebGL2RenderingContext,
) {
    val programIds: ObjectIdentifierMap<WebGLProgram> = ObjectIdentifierMap { context.isProgram(it) }
    val programs: Map<Int, WebGLProgram> get() = programIds.programs

    val shaderIds: ObjectIdentifierMap<WebGLShader> = ObjectIdentifierMap { context.isShader(it) }
    val shaders: Map<Int, WebGLShader> get() = shaderIds.programs

    class ObjectIdentifierMap<T>(
        private val map: MutableMap<T, Int> = mutableMapOf(),
        internal val programs: MutableMap<Int, T> = mutableMapOf(),
        private var counter: Int = 0,
        private val verify: (T) -> Boolean
    ) : MutableMap<T, Int> by map {

        fun add(element: T): Int {
            if (counter == Int.MAX_VALUE) throw WebGLException("Reached max context mapper counter value")
            put(element, counter)
            return counter++
        }

        override fun put(key: T, value: Int): Int? {
            if (!verify(key)) throw WebGLException("Supplied object is not an object in the current context")
            programs.put(value, key)
            return map.put(key, value)
        }

        override fun remove(key: T): Int? {
            programs.remove(map[key]!!)
            return map.remove(key)
        }
    }
}

actual fun init() {
    document.body!!.appendElement("canvas") {
        this as HTMLCanvasElement

        width = 600
        height = 500

        val gl = getContext("webgl2") as? WebGL2RenderingContext
            ?: error("Could not get WebGL2 rendering context")

        context = gl
        glContext = WebGLContext(gl)
    }
}

var initialised = false
actual fun running(): Boolean {
    if (!initialised) { //FIXME a little hack to make it work for now... apparently main has to terminate for this to work - so a callback function with a different thread, or what? come on WebGL, surprise me with another _grandiose_ api.
        initialised = true
        return true
    }
    return false
}

actual fun update(delta: Float) {} //TODO ... or perhaps actually nothing to do

actual fun whileRunning(run: (delta: Float) -> Unit) {
    fun loop() {
        run(0f)
        window.requestAnimationFrame { loop() } //not actually recursion, since callback frame is returned after call
    }
    window.requestAnimationFrame { loop() }
}

actual fun glGetError(): GLErrorType {
    val error = context.getError()
    return when (error) {
        WebGL2RenderingContext.NO_ERROR -> GLErrorType.NO_ERROR
        else -> error("Unknown WebGL error code: $error")
    }
}

actual fun glCreateProgram(): GLProgram? = context.createProgram()?.let { glContext.programIds.add(it) }
actual fun glLinkProgram(program: GLProgram) = context.linkProgram(glContext.programs[program]!!)
actual fun glValidateProgram(program: GLProgram) = context.validateProgram(glContext.programs[program]!!)
actual fun glUseProgram(program: GLProgram) = context.useProgram(glContext.programs[program]!!)

actual fun glGetProgramParameter(program: GLProgram, type: GLProgramParameterType): Int? =
    context.getProgramParameter(
        glContext.programs[program], when (type) {
            GLProgramParameterType.LINK_STATUS -> WebGL2RenderingContext.LINK_STATUS
            GLProgramParameterType.VALIDATE_STATUS -> WebGL2RenderingContext.VALIDATE_STATUS
        }
    )?.tryCastToInt()

actual fun glGetProgramInfoLog(program: GLProgram): String? = context.getProgramInfoLog(glContext.programs[program]!!)

actual fun glCreateShader(type: GLShaderType): GLShader? =
    context.createShader(
        when (type) {
            GLShaderType.VERTEX -> WebGL2RenderingContext.VERTEX_SHADER
            GLShaderType.FRAGMENT -> WebGL2RenderingContext.FRAGMENT_SHADER
        }
    )?.let { glContext.shaderIds.add(it) }

actual fun glShaderSource(shader: GLShader, source: String) = context.shaderSource(glContext.shaders[shader], source)
actual fun glCompileShader(shader: GLShader) = context.compileShader(glContext.shaders[shader])
actual fun glAttachShader(program: GLProgram, shader: GLShader) =
    context.attachShader(glContext.programs[program], glContext.shaders[shader])

actual fun glGetShaderParameter(shader: GLShader, type: GLShaderParameterType): Int? =
    context.getShaderParameter(
        glContext.shaders[shader], when (type) {
            GLShaderParameterType.COMPILE_STATUS -> WebGL2RenderingContext.COMPILE_STATUS
        }
    )?.tryCastToInt()

actual fun glGetShaderInfoLog(shader: GLShader): String? = context.getShaderInfoLog(glContext.shaders[shader])

actual fun glDrawArrays(mode: GLDrawMode, first: Int, count: Int) =
    context.drawArrays(
        when (mode) {
            GLDrawMode.TRIANGLES -> WebGL2RenderingContext.TRIANGLES
            GLDrawMode.TRIANGLE_STRIP -> WebGL2RenderingContext.TRIANGLE_STRIP
        }, first, count
    )

inline fun <reified T : JsAny> JsAny?.cast(): T? = this as? T

fun JsAny.tryCastToInt(): Int = when (this) {
    is JsNumber -> this.toInt()
    is JsBoolean -> if (this.toBoolean()) 1 else 0
    else -> error("Unknown program parameter type: \${this::class.simpleName}")
}

inline fun <reified T : JsAny> JsAny?.forceCast(): T =
    this as? T ?: throw WebGLException("Failed to cast $this to \${T::class.simpleName}")

class WebGLException(override val message: String, cause: Throwable? = null) : RuntimeException(message, cause)
