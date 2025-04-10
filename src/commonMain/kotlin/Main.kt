package io.github.etieskrill.test.kmp

fun main() {
    try {
        helloTriangle()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun helloTriangle() {
    init()

    val program = glCreateProgram()!!

    val vertexShader = glCreateShader(GLShaderType.VERTEX)!!
    glShaderSource(
        //TODO figure out why non-es versions do NOT work
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
    glCompileShader(vertexShader)
    checkCompilation(vertexShader, "vertex")
    checkError()

    val fragmentShader = glCreateShader(GLShaderType.FRAGMENT)!!
    glShaderSource(
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
    glCompileShader(fragmentShader)
    checkCompilation(fragmentShader, "fragment")
    checkError()

    glAttachShader(program, vertexShader)
    glAttachShader(program, fragmentShader)
    checkError()

    glLinkProgram(program)
    checkProgram(program)
    checkError()
    glValidateProgram(program)
    checkValidateProgram(program)
    checkError()

    glUseProgram(program)

    whileRunning { delta ->
        update(delta)
        glDrawArrays(GLDrawMode.TRIANGLES, 0, 3)
    }
}

fun checkCompilation(shader: GLShader, shaderName: String) {
    if (!glGetShaderParameter(shader, GLShaderParameterType.COMPILE_STATUS))
        throw IllegalStateException("Failed to compile $shaderName shader:\n${glGetShaderInfoLog(shader)!!}")
}

fun checkProgram(program: GLProgram) {
    if (!glGetProgramParameter(program, GLProgramParameterType.LINK_STATUS))
        throw IllegalStateException("Failed to link program:\n${glGetProgramInfoLog(program)!!}")
}

fun checkValidateProgram(program: GLProgram) {
    if (!glGetProgramParameter(program, GLProgramParameterType.VALIDATE_STATUS))
        throw IllegalStateException("Failed to validate program:\n${glGetProgramInfoLog(program)!!}")
}

fun checkError() {
    val error = glGetError()
    if (error != GLErrorType.NO_ERROR)
        throw IllegalStateException("OpenGL error occurred: $error")
}

inline operator fun <reified T> Comparable<T>?.not() = when (this) {
    is Boolean -> !this
    is Int -> this == 0
    is Long -> this == 0L
    is Float -> this == 0f
    is Double -> this == 0.0
    null -> false
    else -> throw IllegalArgumentException("Unexpected return type: ${T::class.simpleName}")
}
