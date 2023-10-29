import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BaseTerrain {
    float[][] heightMap_Matrix_fromFile;
    float[] verticesArray_fromFile;
    float[] generated_vertices_array;
    int limit = 400; //limit^2*30, for limit=4000 -> 480m triangles, for limit=400 -> 4.8m triangles

    BaseTerrain() {
        try {
            verticesArray_fromFile = readElevationsFromFile("src\\test.txt");
            heightMap_Matrix_fromFile = new float[(int) Math.sqrt(verticesArray_fromFile.length)][(int) Math
                    .sqrt(verticesArray_fromFile.length)];
            int index = 0;
            for (int row = 0; row < Math.sqrt(verticesArray_fromFile.length); row++) {
                for (int col = 0; col < Math.sqrt(verticesArray_fromFile.length); col++) {
                    this.heightMap_Matrix_fromFile[row][col] = verticesArray_fromFile[index];
                    index++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        GenerateVertices();
    }

    public float[] readElevationsFromFile(String filePath) throws IOException {
        List<Float> floats = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                for (String part : parts) {
                    try {
                        float value = Float.parseFloat(part);
                        floats.add(value);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid float value: " + part);
                    }
                }
            }
        }

        // Convert the list of elevations to an array.
        float[] elevationArray = new float[floats.size()];
        for (int i = 0; i < elevationArray.length; i++) {
            elevationArray[i] = floats.get(i);
        }
        return elevationArray;
    }

    void GenerateVertices() {
        Random random = new Random();

        float[] generated_elevations = new float[(limit + 1) * (limit + 1)];
        float distance_between_points_xz = 0.3f;
        float distance_between_elevations = 0.25f;
        generated_elevations[0] = (float) (-3 + 6 * random.nextDouble());
        for (int i = 1; i < (limit + 1); i++)
            generated_elevations[i] = (float) (generated_elevations[i - 1]
                    + (random.nextDouble() * distance_between_elevations - 0.5f * distance_between_elevations));
        for (int i = 1; i < (limit + 1); i++) {
            generated_elevations[i * (limit + 1)
                    + 0] = (float) (generated_elevations[(i - 1) * (limit)]
                            + (random.nextDouble() * distance_between_elevations - 0.5f * distance_between_elevations));
            for (int j = 1; j < (limit + 1); j++) {
                generated_elevations[i * (limit + 1)
                        + j] = (float) ((generated_elevations[i * (limit + 1) + j - 1]
                                + generated_elevations[(i - 1) * (limit + 1) + j]) / 2
                                + (random.nextDouble() * distance_between_elevations
                                        - 0.5f * distance_between_elevations));
            }
        }

        float[] verts_xz = new float[(limit + 1)];
        for (int i = 0; i < (limit + 1); i++)
            verts_xz[i] = -distance_between_points_xz * (limit / 2 - 1) + i * distance_between_points_xz;

        float[] verticesArray = new float[(limit + 1) * (limit + 1) * 30];
        for (int i = 0; i < limit; i++) {
            for (int j = 0; j < limit; j++) {
                verticesArray[i * 30 * limit + j * 30 + 0] = verts_xz[i];
                verticesArray[i * 30 * limit + j * 30 + 1] = generated_elevations[i * (limit + 1) + j];
                verticesArray[i * 30 * limit + j * 30 + 2] = verts_xz[j];
                verticesArray[i * 30 * limit + j * 30 + 3] = 1.0f;
                verticesArray[i * 30 * limit + j * 30 + 4] = 0.0f;

                verticesArray[i * 30 * limit + j * 30 + 5] = verts_xz[i + 1];
                verticesArray[i * 30 * limit + j * 30 + 6] = generated_elevations[(i + 1) * (limit + 1) + j];
                verticesArray[i * 30 * limit + j * 30 + 7] = verts_xz[j];
                verticesArray[i * 30 * limit + j * 30 + 8] = 0.0f;
                verticesArray[i * 30 * limit + j * 30 + 9] = 1.0f;

                verticesArray[i * 30 * limit + j * 30 + 10] = verts_xz[i];
                verticesArray[i * 30 * limit + j * 30 + 11] = generated_elevations[i * (limit + 1) + j + 1];
                verticesArray[i * 30 * limit + j * 30 + 12] = verts_xz[j + 1];
                verticesArray[i * 30 * limit + j * 30 + 13] = 0.0f;
                verticesArray[i * 30 * limit + j * 30 + 14] = 0.0f;

                verticesArray[i * 30 * limit + j * 30 + 15] = verts_xz[i + 1];
                verticesArray[i * 30 * limit + j * 30 + 16] = generated_elevations[(i + 1) * (limit + 1) + j + 1];
                verticesArray[i * 30 * limit + j * 30 + 17] = verts_xz[j + 1];
                verticesArray[i * 30 * limit + j * 30 + 18] = 1.0f;
                verticesArray[i * 30 * limit + j * 30 + 19] = 0.0f;

                verticesArray[i * 30 * limit + j * 30 + 20] = verts_xz[i];
                verticesArray[i * 30 * limit + j * 30 + 21] = generated_elevations[i * (limit + 1) + j + 1];
                verticesArray[i * 30 * limit + j * 30 + 22] = verts_xz[j + 1];
                verticesArray[i * 30 * limit + j * 30 + 23] = 0.0f;
                verticesArray[i * 30 * limit + j * 30 + 24] = 1.0f;

                verticesArray[i * 30 * limit + j * 30 + 25] = verts_xz[i + 1];
                verticesArray[i * 30 * limit + j * 30 + 26] = generated_elevations[(i + 1) * (limit + 1) + j];
                verticesArray[i * 30 * limit + j * 30 + 27] = verts_xz[j];
                verticesArray[i * 30 * limit + j * 30 + 28] = 0.0f;
                verticesArray[i * 30 * limit + j * 30 + 29] = 0.0f;
            }
        }
        generated_vertices_array = verticesArray;
    }
}
