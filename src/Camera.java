import java.awt.Dimension;
import java.awt.Toolkit;

public class Camera {
	static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();	
	//float ASPECT_RATIO = screenSize.width / screenSize.height;
	//float FOV_DEGREE = 50.0f;
	//float VERTICAL_FOV = (float)Math.toRadians(FOV_DEGREE);
	//float HORIZONTAL_FOV = 2* (float)Math.atan((float)Math.tan(VERTICAL_FOV*0.5) * ASPECT_RATIO);
	float NEAR = 0.1f;
	float FAR = 2000.0f;
	
	float yaw = (float)Math.toRadians(0);
	float pitch = (float)Math.toRadians(0);
	float epsilon = 0.0001f;
	float PITCH_MAX = (float) (Math.PI/2 - epsilon);
	float PITCH_MIN = (float) (-Math.PI/2 + epsilon);
	float YAW_MAX = (float) (Math.PI - epsilon);
	float YAW_MIN = (float) (-Math.PI + epsilon);
	
	float PLAYER_SPEED = 0.1f;
	float MOUSE_SENSITIVITY = 0.05f;
	//PLAYER_POSITION IS RELEVANT FOR HITBOX LATER
	float[] PLAYER_POSITION = {0.0f, 0.0f, 0.0f};;
	
	public Camera() {
		Camera.screenSize.width = 800;
		Camera.screenSize.height = 600;
		this.PLAYER_POSITION[0] = 0.0f;
		this.PLAYER_POSITION[1] = 0.0f;
		this.PLAYER_POSITION[2] = -7.5f;
		this.yaw = 0.0f;
		this.pitch = 0.0f;
	}
	
	public Camera(float[] PLAYER_POSITION, float yaw, float pitch, int screenSize_width, int screenSize_height){
		Camera.screenSize.width = screenSize_width;
		Camera.screenSize.height = screenSize_width;
		this.PLAYER_POSITION = PLAYER_POSITION;
		this.yaw = yaw;
		this.pitch = pitch;
	}

    public void updateYawPitch(double yawDelta, double pitchDelta) {
        yaw += (float) yawDelta;
        pitch += (float) pitchDelta;

        // Limit the pitch and yaw angles within certain bounds
        if (yaw>YAW_MAX) yaw-=2*YAW_MAX;
        else if(yaw<YAW_MIN) yaw-=2*YAW_MIN;
        
        if (pitch > PITCH_MAX) pitch = PITCH_MAX;
        else if (pitch < PITCH_MIN) pitch = PITCH_MIN;
        
        //System.out.println(yaw);
        //System.out.println(pitch);
    }
}
