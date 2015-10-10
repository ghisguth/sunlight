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

    public static VertexBuffer createCube(int resolution) {
        if (resolution < 1) {
            throw new RuntimeException("resolution cannot be less than 1");
        }

        final int horizontalResolution = resolution;
        final int verticalResolution = resolution;

        final int capVerticesCount = (resolution + 1) * (resolution + 1);
        final int sideVerticesCount = (resolution + 1) * (resolution - 1);
        final int otherSideVerticesCount = (resolution - 1) * (resolution - 1);

        final int verticesCount = 2 * capVerticesCount + 2 * sideVerticesCount + 2 * otherSideVerticesCount;
        final int indicesCount = 0;

        float[] vertices = new float[verticesCount * 5];
        short[] indices = new short[indicesCount];

        int index = 0;

        for (int j = 0; j < resolution + 1; ++j) {
            float v = ((float) j) / resolution;
            float y = -0.5f + v;

            for (int i = 0; i < resolution + 1; ++i) {
                float u = ((float) i) / resolution;
                float x = -0.5f + u;

                vertices[index + 0] = x;
                vertices[index + 1] = y;
                vertices[index + 2] = -0.5f;
                vertices[index + 3] = u;
                vertices[index + 4] = v;

                vertices[index + 0 + capVerticesCount * 5] = x;
                vertices[index + 1 + capVerticesCount * 5] = y;
                vertices[index + 2 + capVerticesCount * 5] = 0.5f;
                vertices[index + 3 + capVerticesCount * 5] = u;
                vertices[index + 4 + capVerticesCount * 5] = v;
                index += 5;
            }
        }

        index += capVerticesCount * 5;

        for (int j = 1; j < resolution; ++j) {
            float v = ((float) j) / resolution;
            float y = -0.5f + v;

            for (int i = 0; i < resolution + 1; ++i) {
                float u = ((float) i) / resolution;
                float x = -0.5f + u;

                vertices[index + 0] = x;
                vertices[index + 1] = -0.5f;
                vertices[index + 2] = y;
                vertices[index + 3] = u;
                vertices[index + 4] = v;

                vertices[index + 0 + sideVerticesCount * 5] = x;
                vertices[index + 1 + sideVerticesCount * 5] = 0.5f;
                vertices[index + 2 + sideVerticesCount * 5] = y;
                vertices[index + 3 + sideVerticesCount * 5] = u;
                vertices[index + 4 + sideVerticesCount * 5] = v;
                index += 5;
            }
        }

        index += sideVerticesCount * 5;

        for (int j = 1; j < resolution; ++j) {
            float v = ((float) j) / resolution;
            float y = -0.5f + v;

            for (int i = 1; i < resolution; ++i) {
                float u = ((float) i) / resolution;
                float x = -0.5f + u;

                vertices[index + 0] = -0.5f;
                vertices[index + 1] = x;
                vertices[index + 2] = y;
                vertices[index + 3] = u;
                vertices[index + 4] = v;

                vertices[index + 0 + otherSideVerticesCount * 5] = 0.5f;
                vertices[index + 1 + otherSideVerticesCount * 5] = x;
                vertices[index + 2 + otherSideVerticesCount * 5] = y;
                vertices[index + 3 + otherSideVerticesCount * 5] = u;
                vertices[index + 4 + otherSideVerticesCount * 5] = v;
                index += 5;
            }
        }

        return new VertexBuffer(vertices, indices, true);
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
