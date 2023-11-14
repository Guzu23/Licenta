import java.util.Random;

public class Chunk {
    static float distance_between_points_xz = 90.0f;
    int X;
    int Z;
    float[] generated_elevations_vertices_array;

    Chunk(int x, int z) {
        X = x;
        Z = z;
        generateElevations();
    }

    void generateElevations() {
        float[] generated_elevations = new float[289];
        Random random = new Random();
        for (int i = 0; i < 17; i++) {
            for (int j = 0; j < 17; j++) {
                if (i == 0 || i == 16 || j == 0 || j == 16)
                    generated_elevations[i * 17 + j] = 100;
                else
                    generated_elevations[i * 17 + j] = random.nextFloat() * 50 + 75;
            }
        }

        // 1/16, 16 is number of squares(^2) per chunk
        float dl = 0.0625f;
        float dL = 0.0625f;

        float[] verts_x = new float[18];
        float[] verts_z = new float[18];

        for (int i = 0; i < 17; i++) {
            //CENTERED
            verts_x[i] = i * 90 + 1440 * (X - 1);
            verts_z[i] = i * 90 + 1440 * (Z - 1);
        }

        float[] verticesArray = new float[9720];
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                verticesArray[i * 480 + j * 30 + 0] = verts_x[i];
                verticesArray[i * 480 + j * 30 + 1] = generated_elevations[i * 17 + j];
                verticesArray[i * 480 + j * 30 + 2] = verts_z[j];
                verticesArray[i * 480 + j * 30 + 3] = dl * i;
                verticesArray[i * 480 + j * 30 + 4] = 1 - dL * j;

                verticesArray[i * 480 + j * 30 + 5] = verts_x[i + 1];
                verticesArray[i * 480 + j * 30 + 6] = generated_elevations[(i + 1) * 17 + j];
                verticesArray[i * 480 + j * 30 + 7] = verts_z[j];
                verticesArray[i * 480 + j * 30 + 8] = dl * (i + 1);
                verticesArray[i * 480 + j * 30 + 9] = 1 - dL * j;

                verticesArray[i * 480 + j * 30 + 10] = verts_x[i];
                verticesArray[i * 480 + j * 30 + 11] = generated_elevations[i * 17 + j + 1];
                verticesArray[i * 480 + j * 30 + 12] = verts_z[j + 1];
                verticesArray[i * 480 + j * 30 + 13] = dl * i;
                verticesArray[i * 480 + j * 30 + 14] = 1 - dL * (j + 1);

                verticesArray[i * 480 + j * 30 + 15] = verts_x[i + 1];
                verticesArray[i * 480 + j * 30 + 16] = generated_elevations[(i + 1) * 17 + j + 1];
                verticesArray[i * 480 + j * 30 + 17] = verts_z[j + 1];
                verticesArray[i * 480 + j * 30 + 18] = dl * (i + 1);
                verticesArray[i * 480 + j * 30 + 19] = 1 - dL * (j + 1);

                verticesArray[i * 480 + j * 30 + 20] = verts_x[i];
                verticesArray[i * 480 + j * 30 + 21] = generated_elevations[i * 17 + j + 1];
                verticesArray[i * 480 + j * 30 + 22] = verts_z[j + 1];
                verticesArray[i * 480 + j * 30 + 23] = dl * i;
                verticesArray[i * 480 + j * 30 + 24] = 1 - dL * (j + 1);

                verticesArray[i * 480 + j * 30 + 25] = verts_x[i + 1];
                verticesArray[i * 480 + j * 30 + 26] = generated_elevations[(i + 1) * 17 + j];
                verticesArray[i * 480 + j * 30 + 27] = verts_z[j];
                verticesArray[i * 480 + j * 30 + 28] = dl * (i + 1);
                verticesArray[i * 480 + j * 30 + 29] = 1 - dL * j;
            }
        }
        generated_elevations_vertices_array = verticesArray;
    }
}