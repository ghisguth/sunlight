/**
 * This file is a part of sunlight project
 * Copyright (c) $today.year sunlight authors (see file `COPYRIGHT` for the license)
 */

package com.ghisguth.gfx;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class VertexBuffer {
    public static final int FLOAT_SIZE_BYTES = 4;
    public static final int SHORT_SIZE_BYTES = 2;
    private static String TAG = "Sunlight";
    private FloatBuffer vertices;
    private ShortBuffer indices;
    private int verticesCount;
    private int indicesCount;
    private boolean hasUv;

    public VertexBuffer(float[] vertexArray, short[] indexArray, boolean hasUv) {
        this.hasUv = hasUv;
        vertices = ByteBuffer
                .allocateDirect(
                        vertexArray.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertices.put(vertexArray).position(0);

        indices = ByteBuffer
                .allocateDirect(
                        indexArray.length * SHORT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        indices.put(indexArray).position(0);

        verticesCount = vertexArray.length / getStride();
        indicesCount = indexArray.length;
    }

    private int getStride() {
        int stride = 3;

        if (hasUv) {
            stride += 2;
        }

        return stride;
    }

    public void bind(Program program, String position, String uv) {
        int offset = 0;
        int stride = getStride() * FLOAT_SIZE_BYTES;

        vertices.position(offset);
        GLES20.glVertexAttribPointer(program.getAttributeLocation(position), 3, GLES20.GL_FLOAT, false, stride, vertices);
        ErrorHelper.checkGlError(TAG, "glVertexAttribPointer " + position);
        GLES20.glEnableVertexAttribArray(program.getAttributeLocation(position));
        ErrorHelper.checkGlError(TAG, "glEnableVertexAttribArray " + position);

        offset += 3;

        if (hasUv) {
            vertices.position(offset);
            GLES20.glVertexAttribPointer(program.getAttributeLocation(uv), 2, GLES20.GL_FLOAT, false, stride, vertices);
            ErrorHelper.checkGlError(TAG, "glVertexAttribPointer " + uv);
            GLES20.glEnableVertexAttribArray(program.getAttributeLocation(uv));
            ErrorHelper.checkGlError(TAG, "glEnableVertexAttribArray " + uv);
        }
    }

    public void draw(int primitiveType) {
        if (indicesCount == 0) {
            GLES20.glDrawArrays(primitiveType, 0, verticesCount);
            ErrorHelper.checkGlError(TAG, "glDrawArrays");
        } else {
            GLES20.glDrawElements(primitiveType, indicesCount, GLES20.GL_UNSIGNED_SHORT, indices);
            ErrorHelper.checkGlError(TAG, "glDrawElements");
        }
    }

    public void draw(int primitiveType, int start, int count) {
        if (indicesCount == 0) {
            if (start < 0 || (start + count) > verticesCount) {
                String message = "VertexBuffer::draw called with invalid start/count: start=" + start + " count=" + count + " verticesCount=" + verticesCount;
                Log.e(TAG, message);
                throw new RuntimeException(message);
            }

            GLES20.glDrawArrays(primitiveType, start, count);
            ErrorHelper.checkGlError(TAG, "glDrawArrays");
        } else {
            Log.e(TAG, "VertexBuffer::draw called with start/count for indexed buffer");
            throw new RuntimeException("VertexBuffer::draw called with start/count for indexed buffer");
        }
    }

    public void unbind(Program program, String position, String uv) {
        GLES20.glDisableVertexAttribArray(program.getAttributeLocation(position));
        ErrorHelper.checkGlError(TAG, "glDisableVertexAttribArray " + position);

        if (hasUv) {
            GLES20.glDisableVertexAttribArray(program.getAttributeLocation(uv));
            ErrorHelper.checkGlError(TAG, "glDisableVertexAttribArray " + uv);
        }
    }

}
