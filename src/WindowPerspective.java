import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;

import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class WindowPerspective implements GLEventListener {
	private ExecutorService executor = Executors.newSingleThreadExecutor();

	protected static GLWindow window;
	private FPSAnimator anim;
	private ShaderProgram shaderProgram;
	private IntBuffer VAO, VBO, tex;

	protected static Matrix4 model, view, projection;
	// ADD ZOOM IN-OUT = LOWER/INCREASE THE FOV
	// camera.POSITION IS RELEVANT FOR HITBOX LATER
	public static Camera camera = new Camera();
	//BaseTerrain base = new BaseTerrain();
	public static int prevMouseX = Camera.screenSize.width / 2;
	public static int prevMouseY = Camera.screenSize.height / 2;
	Listener listener = new Listener();
	LoadedChunks loadedChunks = new LoadedChunks(0, 0);

	public WindowPerspective() {
		GLProfile glp = GLProfile.getMaxProgrammableCore(true);
		GLCapabilities caps = new GLCapabilities(glp);
		caps.setDoubleBuffered(true);
		caps.setHardwareAccelerated(true);
		Display disp = NewtFactory.createDisplay("Demo");
		Screen screen = NewtFactory.createScreen(disp, 0);

		window = GLWindow.create(screen, caps);
		window.setSize(Camera.screenSize.width, Camera.screenSize.height);
		window.setTitle("Grafica 3D in Java");
		window.addGLEventListener(this);
		window.setVisible(true);
		this.listener.addListeners();

		anim = new FPSAnimator(window, 60);
		anim.start();
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		/*
		double center_Lon = 27.572410;
		double center_lat = 47.174121;
		double delta_latitude = 0.000833333333;
		double delta_Longitude = 0.000833333333;
		double min_Lon = center_Lon - 8 * delta_Longitude;
		double max_lat = center_lat + 8 * delta_latitude;
		double max_Lon = center_Lon + 8 * delta_Longitude;
		double min_lat = center_lat - 8 * delta_latitude;
		System.out.println(min_Lon);
		System.out.println(max_lat);
		System.out.println(max_Lon);
		System.out.println(min_lat);
		*/
		GL4 gl = drawable.getGL().getGL4();

		gl.glEnable(GL4.GL_DEPTH_TEST);
		gl.glViewport(0, 0, Camera.screenSize.width, Camera.screenSize.height);

		ShaderCode vertexShader = ShaderCode.create(gl, GL4.GL_VERTEX_SHADER, this.getClass(), "shaders", "shaders/bin",
				"vertex", true);
		vertexShader.compile(gl, System.err);
		vertexShader.defaultShaderCustomization(gl, true, true);

		ShaderCode fragmentShader = ShaderCode.create(gl, GL4.GL_FRAGMENT_SHADER, this.getClass(), "shaders",
				"shaders/bin", "fragment", true);
		fragmentShader.compile(gl, System.err);
		fragmentShader.defaultShaderCustomization(gl, true, true);

		shaderProgram = new ShaderProgram();
		shaderProgram.init(gl);
		shaderProgram.add(vertexShader);
		shaderProgram.add(fragmentShader);
		shaderProgram.link(gl, System.err);

		projection = new Matrix4();
		view = new Matrix4();
		model = new Matrix4();

		projection.makePerspective((float) Math.toRadians(camera.FOV_DEGREE), camera.ASPECT_RATIO, camera.NEAR,
				camera.FAR);
		view.translate(camera.POSITION[0], -camera.POSITION[1], camera.POSITION[2]);

		gl.glDeleteShader(vertexShader.id());
		gl.glDeleteShader(fragmentShader.id());

		VAO = IntBuffer.allocate(1);
		VBO = IntBuffer.allocate(1);
		tex = IntBuffer.allocate(1);

		gl.glGenVertexArrays(1, VAO);
		gl.glBindVertexArray(VAO.get(0));

		gl.glGenBuffers(1, VBO);
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, VBO.get(0));

		gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 5 * GLBuffers.SIZEOF_FLOAT, 0);
		gl.glVertexAttribPointer(1, 2, GL.GL_FLOAT, false, 5 * GLBuffers.SIZEOF_FLOAT, 3 * GLBuffers.SIZEOF_FLOAT);
		gl.glEnableVertexAttribArray(0);
		gl.glEnableVertexAttribArray(1);

		loadedChunks.loadTextures(gl);

		gl.glGenTextures(1, tex);
		gl.glBindTexture(GL4.GL_TEXTURE_2D, tex.get(0));

		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_S, GL4.GL_REPEAT);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_T, GL4.GL_REPEAT);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_LINEAR);

		gl.glGenerateMipmap(GL4.GL_TEXTURE_2D);
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		camera.updateCameraPosition();

		GL4 gl = drawable.getGL().getGL4();
		gl.glUseProgram(shaderProgram.program());
		String hexColor = "#3f6fac";
		Color color = Color.decode(hexColor);
		float red = color.getRed() / 255.0f;
		float green = color.getGreen() / 255.0f;
		float blue = color.getBlue() / 255.0f;
		float alpha = 1.0f;
		gl.glClearColor(red, green, blue, alpha);
		gl.glClear(GL4.GL_COLOR_BUFFER_BIT | GL4.GL_DEPTH_BUFFER_BIT);

		gl.glActiveTexture(GL4.GL_TEXTURE0);
		gl.glBindTexture(GL4.GL_TEXTURE_2D, tex.get(0));
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram.program(), "tex"), 0);

		gl.glUniformMatrix4fv(gl.glGetUniformLocation(shaderProgram.program(), "model"), 1, false, model.getMatrix(),
				0);
		gl.glUniformMatrix4fv(gl.glGetUniformLocation(shaderProgram.program(), "projection"), 1, false,
				projection.getMatrix(), 0);
		gl.glUniformMatrix4fv(gl.glGetUniformLocation(shaderProgram.program(), "view"), 1, false, view.getMatrix(), 0);

		gl.glBindVertexArray(VAO.get(0));
		drawChunks(gl);
	}

	void updateChunks(GL4 gl) {
		if (Camera.CHUNK_X == loadedChunks.CENTER_CHUNK_X && Camera.CHUNK_Z == loadedChunks.CENTER_CHUNK_Z)
			return;
		if (Camera.CHUNK_Z + 1 == loadedChunks.CENTER_CHUNK_Z) {
			loadedChunks.updateNorth(gl);
			return;
		}
		if (Camera.CHUNK_Z - 1 == loadedChunks.CENTER_CHUNK_Z) {
			loadedChunks.updateSouth(gl);
			return;
		}
		if (Camera.CHUNK_X + 1 == loadedChunks.CENTER_CHUNK_X) {
			loadedChunks.updateWest(gl);
			return;
		}
		if (Camera.CHUNK_X - 1 == loadedChunks.CENTER_CHUNK_X) {
			loadedChunks.updateEast(gl);
			return;
		}
		loadedChunks = new LoadedChunks(Camera.CHUNK_X, Camera.CHUNK_Z);
		loadedChunks.loadTextures(gl);
	}

	void calculateLOD(GL4 gl, int i, int j) {
		int chunkX = i - LoadedChunks.render_distance;
		int chunkZ = j - LoadedChunks.render_distance;
		int lodLevel = (int) Math.sqrt((chunkX - Camera.CHUNK_X) * (chunkX - Camera.CHUNK_X)
				+ (chunkZ - Camera.CHUNK_Z) * (chunkZ - Camera.CHUNK_Z));

		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR_MIPMAP_LINEAR);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_LINEAR);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_BASE_LEVEL, lodLevel);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MAX_LEVEL, lodLevel);

		// Generate mipmaps
		gl.glGenerateMipmap(GL4.GL_TEXTURE_2D);
	}

	void drawChunks(GL4 gl) {
		Future<?> future = executor.submit(() -> updateChunks(gl));
		try {
			// Wait for the updateChunks method to complete (optional)
			future.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (int i = 0; i < LoadedChunks.render_distance * 2 + 1; i++) {
			for (int j = 0; j < LoadedChunks.render_distance * 2 + 1; j++) {
				//calculateLOD(gl, i, j);
				FloatBuffer vertices = GLBuffers
						.newDirectFloatBuffer(loadedChunks.loadedChunks[i][j].generated_elevations_vertices_array);
				gl.glBufferData(GL.GL_ARRAY_BUFFER, vertices.limit() * GLBuffers.SIZEOF_FLOAT, vertices,
						GL.GL_STATIC_DRAW);
				gl.glTexImage2D(
						GL4.GL_TEXTURE_2D, 0, GL4.GL_RGBA,
						loadedChunks.textures[i][j].getWidth(), loadedChunks.textures[i][j].getHeight(), 0,
						GL4.GL_RGBA, GL4.GL_UNSIGNED_BYTE, loadedChunks.textures[i][j].getBuffer());
				gl.glDrawArrays(GL.GL_TRIANGLES, 0, 1536);
			}
		}
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		GL4 gl = drawable.getGL().getGL4();
		gl.glDeleteVertexArrays(1, VAO);
		gl.glDeleteBuffers(1, VBO);
		anim.stop();
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
	}
}
