import java.awt.Dimension;
import java.awt.Toolkit;

import com.jogamp.newt.event.MouseEvent;
import com.jogamp.opengl.math.Matrix4;

public class Camera {
	static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	float ASPECT_RATIO = screenSize.width / screenSize.height;
	float FOV_DEGREE = 45.0f;
	// float VERTICAL_FOV = (float) Math.toRadians(FOV_DEGREE);
	// float HORIZONTAL_FOV = 2 * (float) Math.atan((float) Math.tan(VERTICAL_FOV *
	// 0.5) * ASPECT_RATIO);
	float NEAR = 0.1f;
	float FAR = 20000.0f;
	float yaw = (float) Math.toRadians(0);
	float pitch = (float) Math.toRadians(0);
	float epsilon = 0.0001f;
	float PITCH_MAX = (float) (Math.PI / 2 - epsilon);
	float PITCH_MIN = (float) (-Math.PI / 2 + epsilon);
	float YAW_MAX = (float) (Math.PI - epsilon);
	float YAW_MIN = (float) (-Math.PI + epsilon);

	float PLAYER_SPEED = 30.0f;
	float MOUSE_SENSITIVITY = 0.05f;
	// POSITION IS RELEVANT FOR HITBOX LATER
	float[] POSITION = { 720.0f, 140.0f, 720.0f };
	static int CHUNK_X = 0;
	static int CHUNK_Z = 0;

	public Camera() {
		Camera.screenSize.width = 800;
		Camera.screenSize.height = 600;
		this.yaw = 0.0f;
		this.pitch = 0.0f;
	}

	//Another constructor for debugging (I will delete that later maybe)
	public Camera(float[] POSITION, float yaw, float pitch, int screenSize_width, int screenSize_height) {
		Camera.screenSize.width = screenSize_width;
		Camera.screenSize.height = screenSize_width;
		this.POSITION = POSITION;
		this.yaw = yaw;
		this.pitch = pitch;
	}

	void updateYawPitch(double yawDelta, double pitchDelta) {
		yaw += (float) yawDelta;
		pitch += (float) pitchDelta;

		// Limit the pitch and yaw angles within certain bounds
		if (yaw > YAW_MAX)
			yaw -= 2 * YAW_MAX;
		else if (yaw < YAW_MIN)
			yaw -= 2 * YAW_MIN;

		if (pitch > PITCH_MAX)
			pitch = PITCH_MAX;
		else if (pitch < PITCH_MIN)
			pitch = PITCH_MIN;
	}

	void translateCamera(float x, float y, float z) {
		POSITION[0] -= x;
		POSITION[1] -= y;
		POSITION[2] -= z;
		WindowPerspective.view.translate(x, y, z);
		CHUNK_X = (int) Math.floor(POSITION[0] / 1440);
		CHUNK_Z = (int) Math.floor(POSITION[2] / 1440);
	}

	void updateCameraPosition() {
		// Move the camera object
		float cos_yaw_times_player_speed = (float) (PLAYER_SPEED * Math.cos(yaw));
		float sin_yaw_times_player_speed = (float) (PLAYER_SPEED * Math.sin(yaw));
		if (Listener.w_pressed)
			translateCamera(sin_yaw_times_player_speed, 0, cos_yaw_times_player_speed);
		if (Listener.s_pressed)
			translateCamera(-sin_yaw_times_player_speed, 0, -cos_yaw_times_player_speed);
		if (Listener.a_pressed)
			translateCamera(cos_yaw_times_player_speed, 0, -sin_yaw_times_player_speed);
		if (Listener.d_pressed)
			translateCamera(-cos_yaw_times_player_speed, 0, sin_yaw_times_player_speed);
		if (Listener.space_pressed)
			translateCamera(0, -PLAYER_SPEED, 0);
		if (Listener.shift_pressed)
			translateCamera(0, PLAYER_SPEED, 0);

	}

	void printPosition() {
		System.out.println("X = " + POSITION[0]);
		System.out.println("Y = " + POSITION[1]);
		System.out.println("Z = " + POSITION[2]);
		System.out.println("CHUNK_X = " + CHUNK_X);
		System.out.println("CHUNK_Z = " + CHUNK_Z);
	}

	void updateCameraView(MouseEvent e) {
		int deltaX = e.getX() - WindowPerspective.prevMouseX;
		int deltaY = e.getY() - WindowPerspective.prevMouseY;

		// Reposition the mouse cursor to the center of the window
		WindowPerspective.window.warpPointer(screenSize.width / 2,
				screenSize.height / 2);

		// Update the previous mouse position
		WindowPerspective.prevMouseX = screenSize.width / 2;
		WindowPerspective.prevMouseY = screenSize.height / 2;
		// Update camera yaw and pitch angles based on mouse movement
		float yawDelta = (float) Math.toRadians(-deltaX * MOUSE_SENSITIVITY);
		float pitchDelta = (float) Math.toRadians(-deltaY * MOUSE_SENSITIVITY);
		updateYawPitch(yawDelta, pitchDelta);

		// Rotate the camera angles
		Matrix4 horizontalRotation = new Matrix4();
		if (pitch < PITCH_MAX && pitch > PITCH_MIN) {
			horizontalRotation.rotate(-yawDelta, 0.0f, (float) (Math.cos(pitch)), (float) (-Math.sin(pitch)));
			horizontalRotation.rotate(-pitchDelta, 1.0f, 0.0f, 0.0f);
		} else
			horizontalRotation.rotate((float) (-yawDelta), 0.0f, 0.0f, (float) (-Math.sin(pitch)));

		// Update the camera view
		Matrix4 result = new Matrix4();
		result.multMatrix(horizontalRotation);
		result.multMatrix(WindowPerspective.view);
		WindowPerspective.view = result;
	}
}
