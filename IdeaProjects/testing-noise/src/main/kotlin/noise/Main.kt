package noise

import badlogic.SharedLibraryLoader
import org.lwjgl.glfw.Callbacks.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWKeyCallback
import org.lwjgl.glfw.GLFWvidmode
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GLContext
import org.lwjgl.system.MemoryUtil.*

var keyboardCallback: GLFWKeyCallback? = null
var errorCallback: GLFWErrorCallback? = null

fun init(): Long {
    errorCallback = errorCallbackPrint(System.err)
    glfwSetCallback(errorCallback)
    val initcode = glfwInit()
    println("Init code: $initcode")
    if (initcode != GL_TRUE)
        throw Exception("Unable to initialize glfw")

    glfwDefaultWindowHints()
    glfwWindowHint(GLFW_VISIBLE, GL_FALSE)
    glfwWindowHint(GLFW_RESIZABLE, GL_TRUE)

    val width = 300
    val height = 300

    val window = glfwCreateWindow(width, height, "Hello world!", NULL, NULL)
    if(window == NULL)
        throw Exception("Unable to initialize window")

    println("Here")

    keyboardCallback = object: GLFWKeyCallback() {
        override fun invoke(window: kotlin.Long, key: kotlin.Int, scancode: kotlin.Int, action: kotlin.Int, mods: kotlin.Int) {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, GL_TRUE)
            }
        }
    }
    glfwSetKeyCallback(window, keyboardCallback)

    println("here2")

    val videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor())

    println("here3")

    glfwSetWindowPos(window, GLFWvidmode.width(videoMode), GLFWvidmode.height(videoMode))

    println("here4")
    glfwMakeContextCurrent(window)
    println("here5")
    glfwSwapInterval(1)
    println("here6")
    glfwShowWindow(window)
    println("here7")

    return window
}

fun loop(window: Long) {
    GLContext.createFromCurrent()

    glClearColor(0f, 0f, 0f, 1f)

    while (glfwWindowShouldClose(window) == GL_FALSE) {
        glClear(GL_COLOR_BUFFER_BIT or GL_COLOR_BUFFER_BIT)

        glfwSwapBuffers(window)
        glfwPollEvents()
    }
}

fun main(args: Array<String>) {
    SharedLibraryLoader.load()
    println(glfwGetVersionString())
    try {
        val window = init()
        loop(window)

        glfwDestroyWindow(window)
        keyboardCallback?.release()
    } finally {
        glfwTerminate()
        errorCallback?.release()
    }
}