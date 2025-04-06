package io.github.etieskrill.test.kmp

expect fun glInit()

enum class GLErrorType { NO_ERROR }
expect fun glGetError(): GLErrorType

typealias GLProgram = Int
expect fun glCreateProgram(): GLProgram?
expect fun glLinkProgram(program: GLProgram)
expect fun glValidateProgram(program: GLProgram)
expect fun glUseProgram(program: GLProgram)

enum class GLProgramParameterType { LINK_STATUS, VALIDATE_STATUS }
expect fun glGetProgramParameter(program: GLProgram, type: GLProgramParameterType): Any?
expect fun glGetProgramInfoLog(program: GLProgram): String?

typealias GLShader = Int
enum class GLShaderType { VERTEX, FRAGMENT }
expect fun glCreateShader(type: GLShaderType): GLShader?
expect fun glShaderSource(shader: GLShader, source: String)
expect fun glCompileShader(shader: GLShader)
expect fun glAttachShader(program: GLProgram, shader: GLShader)

enum class GLShaderParameterType { COMPILE_STATUS }
expect fun glGetShaderParameter(shader: GLShader, type: GLShaderParameterType): Any?
expect fun glGetShaderInfoLog(shader: GLShader): String?

enum class GLDrawMode { TRIANGLES, TRIANGLE_STRIP }
expect fun glDrawArrays(mode: GLDrawMode, first: Int, count: Int)
