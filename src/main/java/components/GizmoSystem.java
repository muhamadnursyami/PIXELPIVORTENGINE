package components;

import jade.KeyListener;
import jade.Window;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_E;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;

public class GizmoSystem extends Component {
    private Spritesheet gizmos;
    private int usingGizmo = 0;

    public GizmoSystem(Spritesheet gizmoSprites) {
        gizmos = gizmoSprites;
    }

    @Override
    public void start() {
        gameObject.addComponent(new TranslateGizmo(gizmos.getSprite(1),
                Window.getImguiLayer().getPropertiesWindow()));
        gameObject.addComponent(new ScaleGizmo(gizmos.getSprite(2),
                Window.getImguiLayer().getPropertiesWindow()));
    }

    @Override
    public void editorUpdate(float dt) {
        if (usingGizmo == 0) {
            gameObject.getComponent(TranslateGizmo.class).setUsing();
            gameObject.getComponent(ScaleGizmo.class).setNotUsing();
        } else if (usingGizmo == 1) {
            gameObject.getComponent(TranslateGizmo.class).setNotUsing();
            gameObject.getComponent(ScaleGizmo.class).setUsing();
        }

//        UNTUK MODE GESER KANAN KIRI ATAU BAWAH/ TRANSLATE PRESS E
        if (KeyListener.isKeyPressed(GLFW_KEY_E)) {
            usingGizmo = 0;

        }
        //        UNTUK MODE BESAR KECIL /SCALE  PRESS R
        else if (KeyListener.isKeyPressed(GLFW_KEY_R)) {
            usingGizmo = 1;
        }
    }
}