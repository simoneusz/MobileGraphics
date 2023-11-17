package com.example.myapplication

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class Cube {
    private val vertexBuffer: FloatBuffer  // Buffer for vertex-array
    private val indexBuffer: ShortBuffer
    private val numFaces = 6
    private var colorHandle: Int = 0
    private val vertexShaderCode =
        "uniform mat4 uMVPMatrix;" +
                "attribute vec4 vPosition;" +
                "void main() {" +
                "  gl_Position = uMVPMatrix * vPosition;" +
                "}"

    private val fragmentShaderCode =
        "precision mediump float;" +
                "uniform vec4 vColor;" +
                "void main() {" +
                "  gl_FragColor = vColor;" +
                "}"
    private var MVPMatrixHandle: Int = 0
    private var positionHandle: Int = 0
    private val program: Int

    companion object {
        const val COORDS_PER_VERTEX = 3
    }

    private val vertexStride = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    private val colors = arrayOf(
        floatArrayOf(1.0f, 0.5f, 0.0f, 1.0f),  // 0. orange
        floatArrayOf(1.0f, 0.0f, 1.0f, 1.0f),  // 1. violet
        floatArrayOf(0.0f, 1.0f, 0.0f, 1.0f),  // 2. green
        floatArrayOf(0.0f, 0.0f, 1.0f, 1.0f),  // 3. blue
        floatArrayOf(1.0f, 0.0f, 0.0f, 1.0f),  // 4. red
        floatArrayOf(1.0f, 1.0f, 0.0f, 1.0f)   // 5. yellow
    )

    private val vertices = floatArrayOf(  // Vertices of the 6 faces
        // FRONT
        -1.0f, -1.0f,  1.0f,  // 0. left-bottom-front
        1.0f, -1.0f,  1.0f,  // 1. right-bottom-front
        -1.0f,  1.0f,  1.0f,  // 2. left-top-front
        -1.0f,  1.0f,  1.0f,  // 2. left-top-front
        1.0f, -1.0f,  1.0f,  // 1. right-bottom-front
        1.0f,  1.0f,  1.0f,  // 3. right-top-front
        // BACK
        1.0f, -1.0f, -1.0f,  // 6. right-bottom-back
        1.0f,  1.0f, -1.0f,  // 7. right-top-back
        -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
        -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
        -1.0f,  1.0f, -1.0f,  // 5. left-top-back
        1.0f,  1.0f, -1.0f,  // 7. right-top-back
        // LEFT
        -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
        -1.0f, -1.0f,  1.0f,  // 0. left-bottom-front
        -1.0f,  1.0f,  1.0f,  // 2. left-top-front
        -1.0f,  1.0f,  1.0f,  // 2. left-top-front
        -1.0f,  1.0f, -1.0f,  // 5. left-top-back
        -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
        // RIGHT
        1.0f, -1.0f,  1.0f,  // 1. right-bottom-front
        1.0f, -1.0f, -1.0f,  // 6. right-bottom-back
        1.0f,  1.0f, -1.0f,  // 7. right-top-back
        1.0f,  1.0f, -1.0f,  // 7. right-top-back
        1.0f,  1.0f,  1.0f,  // 3. right-top-front
        1.0f, -1.0f,  1.0f,  // 1. right-bottom-front
        // TOP
        -1.0f,  1.0f,  1.0f,  // 2. left-top-front
        1.0f,  1.0f,  1.0f,  // 3. right-top-front
        1.0f,  1.0f, -1.0f,  // 7. right-top-back
        1.0f,  1.0f, -1.0f,  // 7. right-top-back
        -1.0f,  1.0f, -1.0f,  // 5. left-top-back
        -1.0f,  1.0f,  1.0f,  // 2. left-top-front
        // BOTTOM
        -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
        1.0f, -1.0f, -1.0f,  // 6. right-bottom-back
        1.0f, -1.0f,  1.0f,   // 1. right-bottom-front
        1.0f, -1.0f,  1.0f,   // 1. right-bottom-front
        -1.0f, -1.0f,  1.0f,  // 0. left-bottom-front
        -1.0f, -1.0f, -1.0f  // 4. left-bottom-back
    )

    private val indices = shortArrayOf(
        0, 1, 3, 1, 2, 3,
        4, 5, 7, 5, 6, 7,
        8, 9, 11, 9, 10, 11,
        12, 13, 15, 13, 14, 15,
        16, 17, 19, 17, 18, 19,
        20, 21, 23, 21, 22, 23
    )

    // Constructor - Set up the buffers
    init {
        // Setup vertex-array buffer. Vertices in float. An float has 4 bytes
        val vbb = ByteBuffer.allocateDirect(vertices.size * 4)
        vbb.order(ByteOrder.nativeOrder()) // Use native byte order
        vertexBuffer = vbb.asFloatBuffer() // Convert from byte to float
        vertexBuffer.put(vertices)         // Copy data into buffer
        vertexBuffer.position(0)           // Rewind

        indexBuffer = ByteBuffer.allocateDirect(indices.size * 2).order(ByteOrder.nativeOrder()).asShortBuffer()
        indexBuffer.put(indices).position(0)

        program = GLES20.glCreateProgram()
        GLES20.glLinkProgram(program)
    }

    // Draw the shape
    fun draw(mvpMatrix: FloatArray) {
        GLES20.glUseProgram(program)

        positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer)

        MVPMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(MVPMatrixHandle, 1, false, mvpMatrix, 0)

        // Render all the faces
        for (face in 0 until numFaces) {
            // Set the color for each of the faces
            colorHandle = GLES20.glGetUniformLocation(program, "vColor")
            GLES20.glUniform4fv(colorHandle, 1, colors[face], 0)
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, indexBuffer)
        }

        GLES20.glDisableVertexAttribArray(positionHandle)
    }
}
