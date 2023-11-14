import java.util.Random;

//NOT NEEDED 
public class BaseTerrain {
    float[] imported_elevations_vertices_array;
    float[] generated_vertices_array;
    int limit = 44; //limit^2*30, for limit=4000 -> 480m triangles, for limit=400 -> 4.8m triangles

    BaseTerrain() {
        generateRandomElevations();
    }

    void generateRandomElevations() {
        Random random = new Random();
        float[] generated_elevations = new float[(limit + 1) * (limit + 1)];
        float distance_between_points_xz = 90.0f;
        generated_elevations[0] = (float) (-3 + 6 * random.nextDouble());
        for (int i = 1; i < (limit + 1); i++)
            generated_elevations[i] = (float) (generated_elevations[i - 1]
                    + (random.nextDouble() * distance_between_points_xz - 0.5f * distance_between_points_xz));
        for (int i = 1; i < (limit + 1); i++) {
            generated_elevations[i * (limit + 1)
                    + 0] = (float) (generated_elevations[(i - 1) * (limit)]
                            + (random.nextDouble() * distance_between_points_xz - 0.5f * distance_between_points_xz));
            for (int j = 1; j < (limit + 1); j++) {
                generated_elevations[i * (limit + 1)
                        + j] = (float) ((generated_elevations[i * (limit + 1) + j - 1]
                                + generated_elevations[(i - 1) * (limit + 1) + j]) / 2
                                + (random.nextDouble() * distance_between_points_xz
                                        - 0.5f * distance_between_points_xz));
            }
        }

        float dx = 1 / (limit - 1);
        float dy = 1 / (limit - 1);

        float[] verts_xz = new float[(limit + 1)];
        for (int i = 0; i < (limit + 1); i++)
            verts_xz[i] = -distance_between_points_xz * (limit / 2 - 1) + i * distance_between_points_xz;

        float[] verticesArray = new float[(limit + 1) * (limit + 1) * 30];
        limit--;
        for (int i = 0; i < limit; i++) {
            for (int j = 0; j < limit; j++) {
                verticesArray[i * 30 * limit + j * 30 + 0] = verts_xz[i];
                verticesArray[i * 30 * limit + j * 30 + 1] = generated_elevations[i * (limit + 1) + j];
                verticesArray[i * 30 * limit + j * 30 + 2] = verts_xz[j];
                verticesArray[i * 30 * limit + j * 30 + 3] = dy * i;
                verticesArray[i * 30 * limit + j * 30 + 4] = 1 - dx * j;

                verticesArray[i * 30 * limit + j * 30 + 5] = verts_xz[i + 1];
                verticesArray[i * 30 * limit + j * 30 + 6] = generated_elevations[(i + 1) * (limit + 1) + j];
                verticesArray[i * 30 * limit + j * 30 + 7] = verts_xz[j];
                verticesArray[i * 30 * limit + j * 30 + 8] = dy * (i + 1);
                verticesArray[i * 30 * limit + j * 30 + 9] = 1 - dx * j;

                verticesArray[i * 30 * limit + j * 30 + 10] = verts_xz[i];
                verticesArray[i * 30 * limit + j * 30 + 11] = generated_elevations[i * (limit + 1) + j + 1];
                verticesArray[i * 30 * limit + j * 30 + 12] = verts_xz[j + 1];
                verticesArray[i * 30 * limit + j * 30 + 13] = dy * i;
                verticesArray[i * 30 * limit + j * 30 + 14] = 1 - dx * (j + 1);

                verticesArray[i * 30 * limit + j * 30 + 15] = verts_xz[i + 1];
                verticesArray[i * 30 * limit + j * 30 + 16] = generated_elevations[(i + 1) * (limit + 1) + j + 1];
                verticesArray[i * 30 * limit + j * 30 + 17] = verts_xz[j + 1];
                verticesArray[i * 30 * limit + j * 30 + 18] = dy * (i + 1);
                verticesArray[i * 30 * limit + j * 30 + 19] = 1 - dx * (j + 1);

                verticesArray[i * 30 * limit + j * 30 + 20] = verts_xz[i];
                verticesArray[i * 30 * limit + j * 30 + 21] = generated_elevations[i * (limit + 1) + j + 1];
                verticesArray[i * 30 * limit + j * 30 + 22] = verts_xz[j + 1];
                verticesArray[i * 30 * limit + j * 30 + 23] = dy * i;
                verticesArray[i * 30 * limit + j * 30 + 24] = 1 - dx * (j + 1);

                verticesArray[i * 30 * limit + j * 30 + 25] = verts_xz[i + 1];
                verticesArray[i * 30 * limit + j * 30 + 26] = generated_elevations[(i + 1) * (limit + 1) + j];
                verticesArray[i * 30 * limit + j * 30 + 27] = verts_xz[j];
                verticesArray[i * 30 * limit + j * 30 + 28] = dy * (i + 1);
                verticesArray[i * 30 * limit + j * 30 + 29] = 1 - dx * j;
            }
        }
        generated_vertices_array = verticesArray;
    }
}
