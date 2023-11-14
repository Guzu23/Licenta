import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

public class LoadedChunks {
    static int render_distance = 3;
    int CENTER_CHUNK_X = 0;
    int CENTER_CHUNK_Z = 0;
    TextureData[][] textures = new TextureData[render_distance * 2
            + 1][render_distance * 2 + 1];
    TextureData defaultTexture;

    Chunk[][] loadedChunks = new Chunk[render_distance * 2 + 1][render_distance * 2 + 1];

    LoadedChunks(int X, int Z) {
        for (int i = 0; i < render_distance * 2 + 1; i++) {
            for (int j = 0; j < render_distance * 2 + 1; j++) {
                loadedChunks[i][j] = new Chunk(j - render_distance + X, i - render_distance + Z);
            }
        }
        CENTER_CHUNK_X = X;
        CENTER_CHUNK_Z = Z;
    }

    //Update chunks and textures in a certain direction for efficiency
    void updateNorth(GL4 gl) {
        CENTER_CHUNK_Z--;
        for (int i = render_distance * 2; i >= 1; i--) {
            for (int j = 0; j < render_distance * 2 + 1; j++) {
                loadedChunks[i][j] = loadedChunks[i - 1][j];
                textures[i][j] = textures[i - 1][j];
            }
        }
        for (int j = 0; j < render_distance * 2 + 1; j++) {
            loadedChunks[0][j] = new Chunk(j - render_distance + CENTER_CHUNK_X, 0 - render_distance + CENTER_CHUNK_Z);
            try {
                String texturePath = "res/" + (j - render_distance + Camera.CHUNK_X) + "_"
                        + (0 - render_distance + Camera.CHUNK_Z)
                        + ".png";
                Path path = FileSystems.getDefault().getPath(texturePath);
                if (Files.exists(path))
                    textures[0][j] = TextureIO.newTextureData(gl.getGLProfile(),
                            new File("res/" + (j - render_distance + Camera.CHUNK_X) + "_"
                                    + (0 - render_distance + Camera.CHUNK_Z)
                                    + ".png"),
                            GL4.GL_TEXTURE_2D,
                            GL4.GL_RGBA, false, "png");
                else
                    textures[0][j] = defaultTexture;
            } catch (IOException e) {
            }
        }
    }

    void updateSouth(GL4 gl) {
        for (int i = 0; i < render_distance * 2; i++) {
            for (int j = 0; j < render_distance * 2 + 1; j++) {
                loadedChunks[i][j] = loadedChunks[i + 1][j];
                textures[i][j] = textures[i + 1][j];
            }
        }
        CENTER_CHUNK_Z++;
        for (int j = 0; j < render_distance * 2 + 1; j++) {
            loadedChunks[render_distance * 2][j] = new Chunk(j - render_distance + CENTER_CHUNK_X,
                    render_distance * 2 - render_distance + CENTER_CHUNK_Z);
            try {
                String texturePath = "res/" + (j - render_distance + Camera.CHUNK_X) + "_"
                        + (render_distance * 2 - render_distance + Camera.CHUNK_Z)
                        + ".png";
                Path path = FileSystems.getDefault().getPath(texturePath);
                if (Files.exists(path))
                    textures[render_distance * 2][j] = TextureIO.newTextureData(gl.getGLProfile(),
                            new File(texturePath),
                            GL4.GL_TEXTURE_2D,
                            GL4.GL_RGBA, false, "png");
                else
                    textures[render_distance * 2][j] = defaultTexture;
            } catch (IOException e) {
            }
        }
    }

    void updateWest(GL4 gl) {
        for (int i = 0; i <= render_distance * 2; i++) {
            for (int j = render_distance * 2; j >= 1; j--) {
                loadedChunks[i][j] = loadedChunks[i][j - 1];
                textures[i][j] = textures[i][j - 1];
            }
        }
        CENTER_CHUNK_X--;
        for (int i = 0; i < render_distance * 2 + 1; i++) {
            loadedChunks[i][0] = new Chunk(0 - render_distance + CENTER_CHUNK_X, i - render_distance + CENTER_CHUNK_Z);
            try {
                String texturePath = "res/" + (0 - render_distance + Camera.CHUNK_X) + "_"
                        + (i - render_distance + Camera.CHUNK_Z)
                        + ".png";
                Path path = FileSystems.getDefault().getPath(texturePath);
                if (Files.exists(path))
                    textures[i][0] = TextureIO.newTextureData(gl.getGLProfile(),
                            new File(texturePath),
                            GL4.GL_TEXTURE_2D,
                            GL4.GL_RGBA, false, "png");
                else
                    textures[i][0] = defaultTexture;
            } catch (IOException e) {
            }
        }
    }

    void updateEast(GL4 gl) {
        for (int i = 0; i <= render_distance * 2; i++) {
            for (int j = 0; j < render_distance * 2; j++) {
                loadedChunks[i][j] = loadedChunks[i][j + 1];
                textures[i][j] = textures[i][j + 1];
            }
        }
        CENTER_CHUNK_X++;
        for (int i = 0; i < render_distance * 2 + 1; i++) {
            loadedChunks[i][render_distance * 2] = new Chunk(render_distance * 2 - render_distance + CENTER_CHUNK_X,
                    i - render_distance + CENTER_CHUNK_Z);
            try {
                String texturePath = "res/" + (render_distance * 2 - render_distance + Camera.CHUNK_X) + "_"
                        + (i - render_distance + Camera.CHUNK_Z)
                        + ".png";
                Path path = FileSystems.getDefault().getPath(texturePath);
                if (Files.exists(path))
                    textures[i][render_distance * 2] = TextureIO.newTextureData(gl.getGLProfile(),
                            new File(texturePath),
                            GL4.GL_TEXTURE_2D,
                            GL4.GL_RGBA, false, "png");
                else
                    textures[i][render_distance * 2] = defaultTexture;
            } catch (IOException e) {
            }
        }
    }

    void loadTextures(GL4 gl) {
        try {
            defaultTexture = TextureIO.newTextureData(gl.getGLProfile(),
                    new File("res/minecraft_grass.png"),
                    GL4.GL_TEXTURE_2D,
                    GL4.GL_RGBA, false, "png");
        } catch (IOException e) {
        }
        for (int i = 0; i < render_distance * 2 + 1; i++) {
            for (int j = 0; j < render_distance * 2 + 1; j++) {
                try {
                    String texturePath = "res/" + (j - render_distance + Camera.CHUNK_X) + "_"
                            + (i - render_distance + Camera.CHUNK_Z)
                            + ".png";
                    Path path = FileSystems.getDefault().getPath(texturePath);
                    if (Files.exists(path))
                        textures[i][j] = TextureIO.newTextureData(gl.getGLProfile(),
                                new File(texturePath),
                                GL4.GL_TEXTURE_2D,
                                GL4.GL_RGBA, false, "png");
                    else
                        textures[i][j] = defaultTexture;
                } catch (IOException e) {
                }
            }
        }
    }
}
