import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
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
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

@SuppressWarnings("serial")
public class Grafica3D extends Frame implements GLEventListener{
	private GLWindow window;
	private FPSAnimator anim;
	private ShaderProgram shaderProgram;
	private IntBuffer VAO, VBO, tex;
	private TextureData texture;
	private Matrix4 model, view, projection;
	private boolean w_pressed = false;
	private boolean s_pressed = false;
	private boolean a_pressed = false;
	private boolean d_pressed = false;
	private boolean space_pressed = false;
	private boolean shift_pressed = false;
	//ADD ZOOM IN-OUT = LOWER THE FOV
	//PLAYER_POSITION IS RELEVANT FOR HITBOX LATER
	//Camera camera = new Camera(new float[]{0.0f, 0.0f, -5.0f}, 0.0f, 0.0f, 800, 600);
	Camera camera = new Camera();
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		Grafica3D grafica3D = new Grafica3D();
	}
	
	public Grafica3D() {
		GLProfile glp  = GLProfile.getMaxProgrammableCore(true);
		GLCapabilities caps = new GLCapabilities(glp);
		caps.setDoubleBuffered(true);
		caps.setHardwareAccelerated(true);
		Display disp = NewtFactory.createDisplay("Demo");
		Screen screen = NewtFactory.createScreen(disp, 0);
		window = GLWindow.create(screen, caps);	
		window.requestFocus();

	    window.addKeyListener(new KeyAdapter(){
	    	
	    	@Override
	        public void keyPressed(KeyEvent e) {
	    	    handleKeyPress(e);
	        }

	        @Override
	        public void keyReleased(KeyEvent e) {
				handleKeyRelease(e);
	        }
	    });
	    window.addMouseListener(new MouseAdapter() {
	    	
	    	private int prevMouseX = window.getWidth() / 2;
	        private int prevMouseY = window.getHeight() / 2;
	        
	        @Override
	        public void mouseMoved(MouseEvent e) {

	            int deltaX = e.getX() - prevMouseX;
	            int deltaY = e.getY() - prevMouseY;

	            // Reposition the mouse cursor to the center of the window
	            window.warpPointer(window.getWidth() / 2, window.getHeight() / 2);

	            // Update the previous mouse position
	            prevMouseX = window.getWidth() / 2;
	            prevMouseY = window.getHeight() / 2;
	            
	            handleMouseMove(e, deltaX, deltaY);
	        }
	        @Override
	        public void mouseClicked(MouseEvent e) {
	        	
	        }
	        @SuppressWarnings("deprecation")
			@Override
	        public void mouseReleased(MouseEvent e) {
	        	//Release the confined pointer
	        	if (e.getButton() == InputEvent.BUTTON1_MASK) window.confinePointer(false); 
	            
	        }
	        @Override
	        public void mouseWheelMoved(MouseEvent e) {
	            int notches = (int) e.getRotation()[1];
	            if (notches < 0) {
	                // Scrolling down (toward the user)
	                // Handle scroll down action here
	            	camera.PLAYER_SPEED-=0.02f;
	            	if (camera.PLAYER_SPEED < 0) camera.PLAYER_SPEED=0.0f;
	            } else {
	                // Scrolling up (away from the user)
	                // Handle scroll up action here
	            	camera.PLAYER_SPEED+=0.02f;
	            }
	        }
	        @Override 
	        public void mouseEntered(MouseEvent e) {
	        	// Initialize the previous mouse position
	            prevMouseX = e.getX();
	            prevMouseY = e.getY();
	        }
	        @Override 
	        public void mouseExited(MouseEvent e) {
	        	
	        }
	        @SuppressWarnings("deprecation")
			@Override 
			public void mousePressed(MouseEvent e) {
	        	if (e.getButton() == InputEvent.BUTTON1_MASK) {
	                window.confinePointer(true); // Confine the pointer to the window
	            }
	        }
	        @Override
	        public void mouseDragged(MouseEvent e) {
	        	
	        }
	    });
	    
	    window.addGLEventListener(this);
		window.setSize(Camera.screenSize.width, Camera.screenSize.height);
		window.setTitle("Grafica 3D in Java");
		window.setVisible(true);

		anim = new FPSAnimator(window, 60);
		anim.start();
	}
	
    void updatePlayerPosition(float x, float y, float z) {
    	camera.PLAYER_POSITION[0]+=x;
    	camera.PLAYER_POSITION[1]+=y;
    	camera.PLAYER_POSITION[2]+=z;
    	view.translate(x, y, z);
    }
    
    
	void handleKeyPress(KeyEvent e) {
		if ((KeyEvent.AUTOREPEAT_MASK & e.getModifiers()) == 0) {
			if(e.getKeyCode() == KeyEvent.VK_W) w_pressed = true;
			if(e.getKeyCode() == KeyEvent.VK_S) s_pressed = true;
			if(e.getKeyCode() == KeyEvent.VK_A) a_pressed = true;
			if(e.getKeyCode() == KeyEvent.VK_D) d_pressed = true;
			if(e.getKeyCode() == KeyEvent.VK_SPACE) space_pressed = true;
			if(e.getKeyCode() == KeyEvent.VK_SHIFT) shift_pressed = true;
			if(e.getKeyCode() == KeyEvent.VK_ESCAPE) System.exit(0);
		}
	}
	void handleKeyRelease(KeyEvent e) {
		if ((KeyEvent.AUTOREPEAT_MASK & e.getModifiers()) == 0) {
			if(e.getKeyCode() == KeyEvent.VK_W) 	w_pressed = false;
			if(e.getKeyCode() == KeyEvent.VK_S) 	s_pressed = false;
			if(e.getKeyCode() == KeyEvent.VK_A) 	a_pressed = false;
			if(e.getKeyCode() == KeyEvent.VK_D) 	d_pressed = false;
			if(e.getKeyCode() == KeyEvent.VK_SPACE) space_pressed = false;
			if(e.getKeyCode() == KeyEvent.VK_SHIFT) shift_pressed = false;
		}
	}
	
	void handleMouseMove(MouseEvent e, int dx, int dy) {
		
	    // Update camera yaw and pitch angles based on mouse movement
	    float yawDelta = (float)Math.toRadians(-dx * camera.MOUSE_SENSITIVITY);
	    float pitchDelta = (float)Math.toRadians(-dy * camera.MOUSE_SENSITIVITY);
	    camera.updateYawPitch(yawDelta, pitchDelta);

	    Matrix4 horizontalMovement = new Matrix4();
	    //Rotate the camera angles
	    if (camera.pitch < camera.PITCH_MAX && camera.pitch > camera.PITCH_MIN )
	    {
	    	horizontalMovement.rotate(-pitchDelta, 1.0f, 0.0f, 0.0f);
	    	horizontalMovement.rotate(-yawDelta, 0.0f, (float)(Math.cos(camera.pitch)), (float)(-Math.sin(camera.pitch)));
	    }
	    else horizontalMovement.rotate((float)(-yawDelta), 0.0f, 0.0f, (float)(-Math.sin(camera.pitch)));

	    //Update the camera view
	    Matrix4 result = new Matrix4();
	    result.multMatrix(horizontalMovement);
	    result.multMatrix(view);
	    view = result;
	}
	
	
	@Override
	public void init(GLAutoDrawable drawable) {
		GL4 gl = drawable.getGL().getGL4();
		
		gl.glEnable(GL4.GL_DEPTH_TEST);
		gl.glViewport(0, 0, Camera.screenSize.width, Camera.screenSize.height);
		
		ShaderCode vertexShader = ShaderCode.create(gl, GL4.GL_VERTEX_SHADER, this.getClass(), "shaders", "shaders/bin", "vertex", true);
		vertexShader.compile(gl, System.err);
		vertexShader.defaultShaderCustomization(gl, true, true);
		
		ShaderCode fragmentShader = ShaderCode.create(gl, GL4.GL_FRAGMENT_SHADER, this.getClass(), "shaders", "shaders/bin", "fragment", true);
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
		
		projection.makePerspective((float)Math.toRadians(45.0), Camera.screenSize.width / Camera.screenSize.height, 0.1f, 100.0f);
		view.translate(3.0f, 0.0f, -5.0f);
		model.rotate((float)Math.toRadians(45.0), 1.0f, 1.0f, 0.0f);
		
		gl.glDeleteShader(vertexShader.id());
		gl.glDeleteShader(fragmentShader.id());
		
		float[] verticesArray = new float[]{
             //x,     y,    z,     texCoords
              -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,
               0.5f, -0.5f, -0.5f, 1.0f, 0.0f,
               0.5f,  0.5f, -0.5f, 1.0f, 1.0f,
               0.5f,  0.5f, -0.5f, 1.0f, 1.0f,
              -0.5f,  0.5f, -0.5f, 0.0f, 1.0f,
              -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,

              -0.5f, -0.5f,  0.5f, 0.0f, 0.0f,
               0.5f, -0.5f,  0.5f, 1.0f, 0.0f,
               0.5f,  0.5f,  0.5f, 1.0f, 1.0f,
               0.5f,  0.5f,  0.5f, 1.0f, 1.0f,
              -0.5f,  0.5f,  0.5f, 0.0f, 1.0f,
              -0.5f, -0.5f,  0.5f, 0.0f, 0.0f,

              -0.5f,  0.5f,  0.5f, 1.0f, 0.0f,
              -0.5f,  0.5f, -0.5f, 1.0f, 1.0f,
              -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
              -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
              -0.5f, -0.5f,  0.5f, 0.0f, 0.0f,
              -0.5f,  0.5f,  0.5f, 1.0f, 0.0f,

               0.5f,  0.5f,  0.5f, 1.0f, 0.0f,
               0.5f,  0.5f, -0.5f, 1.0f, 1.0f,
               0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
               0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
               0.5f, -0.5f,  0.5f, 0.0f, 0.0f,
               0.5f,  0.5f,  0.5f, 1.0f, 0.0f,

              -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
               0.5f, -0.5f, -0.5f, 1.0f, 1.0f,
               0.5f, -0.5f,  0.5f, 1.0f, 0.0f,
               0.5f, -0.5f,  0.5f, 1.0f, 0.0f,
              -0.5f, -0.5f,  0.5f, 0.0f, 0.0f,
              -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,

              -0.5f,  0.5f, -0.5f, 0.0f, 1.0f,
               0.5f,  0.5f, -0.5f, 1.0f, 1.0f,
               0.5f,  0.5f,  0.5f, 1.0f, 0.0f,
               0.5f,  0.5f,  0.5f, 1.0f, 0.0f,
              -0.5f,  0.5f,  0.5f, 0.0f, 0.0f,
              -0.5f,  0.5f, -0.5f, 0.0f, 1.0f
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
			texture = TextureIO.newTextureData(gl.getGLProfile(), new File("res/container2.png"), GL4.GL_TEXTURE_2D, GL4.GL_RGBA, false, "png");
		} catch(IOException e) {
			e.printStackTrace();
		}
		gl.glGenTextures(1, tex);
		gl.glBindTexture(GL4.GL_TEXTURE_2D, tex.get(0));
		
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_S, GL4.GL_REPEAT);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_T, GL4.GL_REPEAT);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_LINEAR);
		gl.glTexImage2D(
						GL4.GL_TEXTURE_2D, 	0, 						GL4.GL_RGBA,
						texture.getWidth(), texture.getHeight(),	0,
						GL4.GL_RGBA, 		GL4.GL_UNSIGNED_BYTE,	texture.getBuffer()
						);
		gl.glGenerateMipmap(GL4.GL_TEXTURE_2D);
		
		
		projection = new Matrix4();
		view = new Matrix4();
		model = new Matrix4();
		
		projection.makePerspective((float)Math.toRadians(45.0), Camera.screenSize.width / Camera.screenSize.height, 0.1f, 100.0f);
		view.translate(camera.PLAYER_POSITION[0], camera.PLAYER_POSITION[1], camera.PLAYER_POSITION[2]);
		model.rotate((float)Math.toRadians(45.0), 1.0f, 1.0f, 0.0f);
		
		gl.glDeleteShader(vertexShader.id());
		gl.glDeleteShader(fragmentShader.id());
		

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
			texture = TextureIO.newTextureData(gl.getGLProfile(), new File("res/container2.png"), GL4.GL_TEXTURE_2D, GL4.GL_RGBA, false, "png");
		} catch(IOException e) {
			e.printStackTrace();
		}
		gl.glGenTextures(1, tex);
		gl.glBindTexture(GL4.GL_TEXTURE_2D, tex.get(0));
		
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_S, GL4.GL_REPEAT);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_T, GL4.GL_REPEAT);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_LINEAR);
		gl.glTexImage2D(
						GL4.GL_TEXTURE_2D, 	0, 						GL4.GL_RGBA,
						texture.getWidth(), texture.getHeight(),	0,
						GL4.GL_RGBA, 		GL4.GL_UNSIGNED_BYTE,	texture.getBuffer()
						);
		gl.glGenerateMipmap(GL4.GL_TEXTURE_2D);
	}
	@Override
	public void dispose(GLAutoDrawable drawable) {
		GL4 gl = drawable.getGL().getGL4();
		gl.glDeleteVertexArrays(1, VAO);
		gl.glDeleteBuffers(1, VBO);
		anim.stop();
	}
	@Override
	public void display(GLAutoDrawable drawable) {
		//Move the camera
		float cos_yaw_times_player_speed = (float) (camera.PLAYER_SPEED*Math.cos(camera.yaw));
		float sin_yaw_times_player_speed = (float) (camera.PLAYER_SPEED*Math.sin(camera.yaw));
		if(w_pressed)		updatePlayerPosition(sin_yaw_times_player_speed, 0, cos_yaw_times_player_speed);
        if(s_pressed)		updatePlayerPosition(-sin_yaw_times_player_speed, 0, -cos_yaw_times_player_speed);
        if(a_pressed)		updatePlayerPosition(cos_yaw_times_player_speed, 0, -sin_yaw_times_player_speed);
        if(d_pressed) 		updatePlayerPosition(-cos_yaw_times_player_speed, 0, sin_yaw_times_player_speed);
        if(space_pressed)	updatePlayerPosition(0, -camera.PLAYER_SPEED, 0);
        if(shift_pressed)	updatePlayerPosition(0, camera.PLAYER_SPEED, 0);

		GL4 gl = drawable.getGL().getGL4();		
		gl.glUseProgram(shaderProgram.program());
		String hexColor = "#3f6fac"; // Orange color
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
		
		
		model.loadIdentity();
		//long currMillis = System.currentTimeMillis();
		//model.rotate((float)(2 * Math.PI * (currMillis % 2000) / 2000.0), 1.0f, 1.0f, 0.0f);
		
		gl.glUniformMatrix4fv(gl.glGetUniformLocation(shaderProgram.program(), "model"), 1, false, model.getMatrix(), 0);
		gl.glUniformMatrix4fv(gl.glGetUniformLocation(shaderProgram.program(), "projection"), 1, false, projection.getMatrix(), 0);
		gl.glUniformMatrix4fv(gl.glGetUniformLocation(shaderProgram.program(), "view"), 1, false, view.getMatrix(), 0);
		
		gl.glBindVertexArray(VAO.get(0));
		gl.glDrawArrays(GL.GL_TRIANGLES, 0, 36);
		
	}
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		//GL4 gl = drawable.getGL().getGL4();		
	}

}
