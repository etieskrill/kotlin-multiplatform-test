import io.github.etieskrill.test.kmp.context
import io.github.etieskrill.test.kmp.glCreateProgram
import kotlinx.browser.window
import kotlinx.dom.appendElement
import kotlinx.dom.appendText
import org.khronos.webgl.WebGLProgram
import org.khronos.webgl.WebGLRenderingContext.Companion.COMPILE_STATUS
import org.khronos.webgl.WebGLRenderingContext.Companion.FRAGMENT_SHADER
import org.khronos.webgl.WebGLRenderingContext.Companion.LINK_STATUS
import org.khronos.webgl.WebGLRenderingContext.Companion.NO_ERROR
import org.khronos.webgl.WebGLRenderingContext.Companion.TRIANGLES
import org.khronos.webgl.WebGLRenderingContext.Companion.VALIDATE_STATUS
import org.khronos.webgl.WebGLRenderingContext.Companion.VERTEX_SHADER
import org.khronos.webgl.WebGLRenderingContextBase
import org.khronos.webgl.WebGLShader
import org.w3c.dom.Element
import org.w3c.dom.HTMLCanvasElement

fun Element.addWebGLCanvas() =
    try {
        addWebGLCanvasThrowing()
    } catch (e: WebGLException) {
        e.printStackTrace()
        window.alert(e.message)
    }

private fun Element.addWebGLCanvasThrowing() {
    appendElement("canvas") {
        this as HTMLCanvasElement

        width = 600
        height = 500

        val gl2 = getContext("webgl2") as? WebGL2RenderingContext
        if (gl2 == null) {
            appendText(
                "Could not get WebGL2 context${
                    if (getContext("webgl") != null) ", WebGL1 context exists though" else ""
                }"
            )
            return@appendElement
        }
//        val gl1 = getContext("webgl") as? WebGLRenderingContext

//        val gl = gl2
        context = gl

        val program = glCreateProgram()!!

        val vertexShader = gl.createShader(VERTEX_SHADER)!!
        gl.shaderGlslSource(
            vertexShader, """
            #version 300 es

            const vec2[3] vertices = vec2[3](vec2(0, 1), vec2(1, -1), vec2(-1, -1));
            
            out vec2 texCoord;

            void main()
            {
                gl_Position = vec4(vertices[gl_VertexID], 0, 1);
                texCoord = vertices[gl_VertexID];
            }
        """.trimIndent()
        )
        gl.compileShader(vertexShader)
        gl.checkCompilation(vertexShader, "vertex")
        gl.checkError()

        val fragmentShader = gl.createShader(FRAGMENT_SHADER)!!
        gl.shaderGlslSource(
            fragmentShader, """
            #version 300 es
            
            precision mediump float;
            
            in vec2 texCoord;

            out vec4 colour;

            void main()
            {
                colour = vec4(texCoord.x + 0.75, texCoord.y + 0.75, -texCoord.x - texCoord.y, 1);
            }
        """.trimIndent()
        )
        gl.compileShader(fragmentShader)
        gl.checkCompilation(fragmentShader, "fragment")
        gl.checkError()

        gl.attachShader(program, vertexShader)
        gl.attachShader(program, fragmentShader)
        gl.checkError()

        gl.linkProgram(program)
        gl.checkProgram(program)
        gl.checkError()
        gl.validateProgram(program)
        gl.checkValidateProgram(program)
        gl.checkError()

        gl.useProgram(program)

        gl.drawArrays(TRIANGLES, 0, 3)
    }
}

fun WebGLRenderingContextBase.checkCompilation(shader: WebGLShader, shaderName: String) {
    if (!getShaderParameter(shader, COMPILE_STATUS))
        throw WebGLException("Failed to compile $shaderName shader:\n${getShaderInfoLog(shader)!!}")
}

fun WebGLRenderingContextBase.checkProgram(program: WebGLProgram) {
    if (!getProgramParameter(program, LINK_STATUS))
        throw WebGLException("Failed to link program:\n${getProgramInfoLog(program)!!}")
}

fun WebGLRenderingContextBase.checkValidateProgram(program: WebGLProgram) {
    if (!getProgramParameter(program, VALIDATE_STATUS))
        throw WebGLException("Failed to validate program:\n${getProgramInfoLog(program)!!}")
}

fun WebGLRenderingContextBase.checkError() {
    val error = getError()
    if (error != NO_ERROR)
        throw WebGLException("WebGL error occurred: $error")
}

operator fun JsAny?.not() = !cast<JsBoolean>()!!.toBoolean()

@Suppress("unused")
annotation class Language(val name: String)

fun WebGLRenderingContextBase.shaderGlslSource(shader: WebGLShader, @Language("glsl") source: String) =
    shaderSource(shader, source)

inline fun <reified T : JsAny> JsAny?.cast(): T? =
    if (this is T) this else null

inline fun <reified T: JsAny> JsAny?.forceCast(): T? =
    if (this == null || this is T) this as? T
    else throw WebGLException("Failed to cast $this to ${T::class.simpleName}")

class WebGLException(override val message: String, cause: Throwable? = null) : RuntimeException(message, cause)
