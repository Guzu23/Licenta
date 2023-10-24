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
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class WindowPerspective implements GLEventListener {
	protected static GLWindow window;
	private FPSAnimator anim;
	private ShaderProgram shaderProgram;
	private IntBuffer VAO, VBO, tex;
	private TextureData texture;
	protected static Matrix4 model, view, projection;

	// ADD ZOOM IN-OUT = LOWER THE FOV
	// camera.POSITION IS RELEVANT FOR HITBOX LATER
	// Camera camera = new Camera(new float[]{0.0f, 0.0f, -5.0f}, 0.0f, 0.0f, 800,
	// 600);
	public static Camera camera = new Camera();
	public static int prevMouseX = Camera.screenSize.width / 2;
	public static int prevMouseY = Camera.screenSize.height / 2;
	Listener listener = new Listener();

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
		view.translate(camera.POSITION[0], camera.POSITION[1], camera.POSITION[2]);

		gl.glDeleteShader(vertexShader.id());
		gl.glDeleteShader(fragmentShader.id());

		float[] verticesArray = new float[] {
				// x, y, z, texCoords
				-0.5f, -0.5f, -0.5f, 0.0f, 0.0f,
				0.5f, -0.5f, -0.5f, 1.0f, 0.0f,
				0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
				0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
				-0.5f, 0.5f, -0.5f, 0.0f, 1.0f,
				-0.5f, -0.5f, -0.5f, 0.0f, 0.0f,

				-0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
				0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
				0.5f, 0.5f, 0.5f, 1.0f, 1.0f,
				0.5f, 0.5f, 0.5f, 1.0f, 1.0f,
				-0.5f, 0.5f, 0.5f, 0.0f, 1.0f,
				-0.5f, -0.5f, 0.5f, 0.0f, 0.0f,

				-0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
				-0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
				-0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
				-0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
				-0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
				-0.5f, 0.5f, 0.5f, 1.0f, 0.0f,

				0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
				0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
				0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
				0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
				0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
				0.5f, 0.5f, 0.5f, 1.0f, 0.0f,

				-0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
				0.5f, -0.5f, -0.5f, 1.0f, 1.0f,
				0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
				0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
				-0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
				-0.5f, -0.5f, -0.5f, 0.0f, 1.0f,

				-0.5f, 0.5f, -0.5f, 0.0f, 1.0f,
				0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
				0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
				0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
				-0.5f, 0.5f, 0.5f, 0.0f, 0.0f,
				-0.5f, 0.5f, -0.5f, 0.0f, 1.0f
		};
		FloatBuffer vertices = GLBuffers.newDirectFloatBuffer(verticesArray);

		VAO = IntBuffer.allocate(1);
		VBO = IntBuffer.allocate(1);
		tex = IntBuffer.allocate(1);

		gl.glGenVertexArrays(1, VAO);
		gl.glBindVertexArray(VAO.get(0));

		gl.glGenBuffers(1, VBO);
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, VBO.get(0));

		gl.glBufferData(GL.GL_ARRAY_BUFFER, vertices.limit() * GLBuffers.SIZEOF_FLOAT, vertices, GL.GL_STATIC_DRAW);
		gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 5 * GLBuffers.SIZEOF_FLOAT, 0);
		gl.glVertexAttribPointer(1, 2, GL.GL_FLOAT, false, 5 * GLBuffers.SIZEOF_FLOAT, 3 * GLBuffers.SIZEOF_FLOAT);
		gl.glEnableVertexAttribArray(0);
		gl.glEnableVertexAttribArray(1);

		try {
			texture = TextureIO.newTextureData(gl.getGLProfile(), new File("res/container2.png"), GL4.GL_TEXTURE_2D,
					GL4.GL_RGBA, false, "png");
		} catch (IOException e) {
			e.printStackTrace();
		}
		gl.glGenTextures(1, tex);
		gl.glBindTexture(GL4.GL_TEXTURE_2D, tex.get(0));

		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_S, GL4.GL_REPEAT);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_T, GL4.GL_REPEAT);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_LINEAR);
		gl.glTexImage2D(
				GL4.GL_TEXTURE_2D, 0, GL4.GL_RGBA,
				texture.getWidth(), texture.getHeight(), 0,
				GL4.GL_RGBA, GL4.GL_UNSIGNED_BYTE, texture.getBuffer());
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

		// model.loadIdentity();
		// long currMillis = System.currentTimeMillis();
		// model.rotate((float)(2 * Math.PI * (currMillis % 2000) / 2000.0), 1.0f, 1.0f,
		// 0.0f);

		gl.glUniformMatrix4fv(gl.glGetUniformLocation(shaderProgram.program(), "model"), 1, false, model.getMatrix(),
				0);
		gl.glUniformMatrix4fv(gl.glGetUniformLocation(shaderProgram.program(), "projection"), 1, false,
				projection.getMatrix(), 0);
		gl.glUniformMatrix4fv(gl.glGetUniformLocation(shaderProgram.program(), "view"), 1, false, view.getMatrix(), 0);

		gl.glBindVertexArray(VAO.get(0));
		gl.glDrawArrays(GL.GL_TRIANGLES, 0, 36);
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
		// GL4 gl = drawable.getGL().getGL4();
	}
}
