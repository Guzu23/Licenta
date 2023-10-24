
import com.jogamp.newt.event.InputEvent;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;

public class Listener {
    protected static boolean w_pressed = false;
    protected static boolean s_pressed = false;
    protected static boolean a_pressed = false;
    protected static boolean d_pressed = false;
    protected static boolean space_pressed = false;
    protected static boolean shift_pressed = false;

    void addListeners() {
        WindowPerspective.window.requestFocus();
        WindowPerspective.window.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyRelease(e);
            }
        });
        WindowPerspective.window.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMove(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseReleased(e);
            }

            @Override
            public void mouseWheelMoved(MouseEvent e) {
                handleMouseWheelMoved(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                handleMouseEntered(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePressed(e);
            }
        });
    }

    void handleKeyPress(KeyEvent e) {
        if ((KeyEvent.AUTOREPEAT_MASK & e.getModifiers()) == 0) {
            if (e.getKeyCode() == KeyEvent.VK_W)
                w_pressed = true;
            if (e.getKeyCode() == KeyEvent.VK_S)
                s_pressed = true;
            if (e.getKeyCode() == KeyEvent.VK_A)
                a_pressed = true;
            if (e.getKeyCode() == KeyEvent.VK_D)
                d_pressed = true;
            if (e.getKeyCode() == KeyEvent.VK_SPACE)
                space_pressed = true;
            if (e.getKeyCode() == KeyEvent.VK_SHIFT)
                shift_pressed = true;
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                System.exit(0);
        }
    }

    void handleKeyRelease(KeyEvent e) {
        if ((KeyEvent.AUTOREPEAT_MASK & e.getModifiers()) == 0) {
            if (e.getKeyCode() == KeyEvent.VK_W)
                w_pressed = false;
            if (e.getKeyCode() == KeyEvent.VK_S)
                s_pressed = false;
            if (e.getKeyCode() == KeyEvent.VK_A)
                a_pressed = false;
            if (e.getKeyCode() == KeyEvent.VK_D)
                d_pressed = false;
            if (e.getKeyCode() == KeyEvent.VK_SPACE)
                space_pressed = false;
            if (e.getKeyCode() == KeyEvent.VK_SHIFT)
                shift_pressed = false;
        }
    }

    void handleMouseMove(MouseEvent e) {
        WindowPerspective.camera.updateCameraView(e);
    }

    void handleMouseWheelMoved(MouseEvent e) {
        int notches = (int) e.getRotation()[1];
        if (notches < 0) {
            // Scrolling down (toward the user)
            // Handle scroll down action here
            WindowPerspective.camera.PLAYER_SPEED -= 0.02f;
            if (WindowPerspective.camera.PLAYER_SPEED < 0)
                WindowPerspective.camera.PLAYER_SPEED = 0.0f;
        } else {
            // Scrolling up (away from the user)
            // Handle scroll up action here
            WindowPerspective.camera.PLAYER_SPEED += 0.02f;
        }
    }

    void handleMouseReleased(MouseEvent e) {
        // Release the confined pointer
        if (e.getButton() == InputEvent.BUTTON1_MASK)
            WindowPerspective.window.confinePointer(false);
    }

    void handleMouseEntered(MouseEvent e) {
        // Initialize the previous mouse position
        WindowPerspective.prevMouseX = e.getX();
        WindowPerspective.prevMouseY = e.getY();
    }

    void handleMousePressed(MouseEvent e) {
        if (e.getButton() == InputEvent.BUTTON1_MASK) {
            WindowPerspective.window.confinePointer(true); // Confine the pointer to the window
        }
    }
}
