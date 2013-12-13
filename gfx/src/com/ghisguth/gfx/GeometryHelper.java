/**
 * This file is a part of sunlight project
 * Copyright (c) $today.year sunlight authors (see file `COPYRIGHT` for the license)
 */

package com.ghisguth.gfx;

public class GeometryHelper {

    private static final float[] quadVerticesArray = {
            // X, Y, Z, U, V
            1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 0.0f, 1.0f
    };

    private static final float[] spriteVerticesArray = {
            // X, Y, Z, U, V
            1.0f, 0.0f, -1.0f, 1.0f, 1.0f,
            0.0f, 0.0f, -0.5f, 0.0f, 1.0f,
            1.0f, 1.0f, -1.0f, 1.0f, 0.0f,
            0.0f, 1.0f, -1.0f, 0.0f, 0.0f
    };

    public static VertexBuffer createScreenQuad() {
        return new VertexBuffer(quadVerticesArray, new short[0], true);
    }

    public static VertexBuffer createSprite() {
        return new VertexBuffer(spriteVerticesArray, new short[0], true);
    }

    public static VertexBuffer createSphere(int horizontalResolution, int verticalResolution) {
        final int verticesCount = horizontalResolution * verticalResolution;
        final int indicesCount = horizontalResolution * 2 * (verticalResolution - 1);

        float[] vertices = new float[verticesCount * 5];
        short[] indices = new short[indicesCount];
        int index = 0;
        float radius = 1.0f;

        for (int j = 0; j < verticalResolution; ++j) {
            double v = (double) j / (verticalResolution - 1);
            double theta = v * Math.PI;
            double sinTheta = Math.sin(theta);
            double cosTheta = Math.cos(theta);

            for (int i = 0; i < horizontalResolution; ++i) {
                double u = (double) i / (horizontalResolution - 1);

                double phi = 2.0f * u * Math.PI;
                double sinPhi = Math.sin(phi);
                double cosPhi = Math.cos(phi);

                vertices[index + 0] = (float) (radius * sinTheta * cosPhi);
                vertices[index + 1] = (float) (radius * sinTheta * sinPhi);
                vertices[index + 2] = (float) (radius * cosTheta);
                vertices[index + 3] = (float) (u);
                vertices[index + 4] = (float) (v);
                index += 5;
            }
        }

        index = 0;

        for (int j = 0; j < verticalResolution - 1; ++j) {
            boolean dir = (j & 1) != 0;
            if (dir || true) {
                for (int i = 0; i < horizontalResolution; ++i) {
                    indices[index + 0] = (short) (j * horizontalResolution + i);
                    indices[index + 1] = (short) ((j + 1) * horizontalResolution + i);
                    index += 2;
                }
            } else {
                for (int i = horizontalResolution - 1; i >= 0; --i) {
                    indices[index + 0] = (short) (j * horizontalResolution + i);
                    indices[index + 1] = (short) ((j + 1) * horizontalResolution + i);
                    index += 2;
                }
            }
        }

        return new VertexBuffer(vertices, indices, true);
    }
}
