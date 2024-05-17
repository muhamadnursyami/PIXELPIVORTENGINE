package jade;
import observers.EventSystem;
import observers.Observer;
import observers.events.Event;
import observers.events.EventType;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import scenes.LevelEditorSceneInitializer;
import scenes.SceneInitializer;
import scenes.Scene;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import renderer.*;
import util.AssetPool;
public class Window  implements Observer{
    //    Penjelasan lengkap ada di  : https://www.lwjgl.org/guide
    private int width, height;
    private  String title;
    private long glfwWindow;
    private ImGuiLayer imguiLayer;
    private Framebuffer framebuffer;

    private PickingTexture pickingTexture;
    private boolean runtimePlaying = false;

    private  static Window window = null;

    private static Scene currentScene;

    private Window(){
        this.width = 1920;
        this.height= 1080;
        this.title = "Jade";
        EventSystem.addObserver(this);
    }


    public static void changeScene(SceneInitializer sceneInitializer) {
        if (currentScene != null) {
            currentScene.destroy();
        }

        getImguiLayer().getPropertiesWindow().setActiveGameObject(null);
        currentScene = new Scene(sceneInitializer);
        currentScene.load();
        currentScene.init();
        currentScene.start();
    }

    public static  Window get(){
        if(Window.window == null){
            Window.window = new Window();
        }
        return  Window.window;
    }

    public static Scene getScene() {
        return get().currentScene;
    }
    public void  run(){
        System.out.println("Hello LWGJL" + Version.getVersion() + "!");

        init();
        loop();

        // Free the memory
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        // Terminate GLFW and the free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();

    }
    public void init(){
//        Mengsetup error pada layar window dan memberi pesan kesalahan
        GLFWErrorCallback.createPrint(System.err).set();

//        Inisialisasi GLFW
        if(!glfwInit()){
            throw  new IllegalStateException("tidak dapat memulai GLFW");
        }


//        Konfigurasi GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);

//        Membuat sebuah window
        glfwWindow = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);

        if (glfwWindow == NULL){
            throw new IllegalStateException("Gagal untuk membuat  sebuah window GLFW");
        }

        glfwSetCursorPosCallback(glfwWindow, MouseListener::mousePosCallback);
        glfwSetMouseButtonCallback(glfwWindow, MouseListener::mouseButtonCallback);
        glfwSetScrollCallback(glfwWindow, MouseListener::mouseScrollCallback);
        glfwSetKeyCallback(glfwWindow, KeyListener::keyCallback);

        glfwSetWindowSizeCallback(glfwWindow, (w, newWidth, newHeight) -> {
            Window.setWidth(newWidth);
            Window.setHeight(newHeight);
        });

//        Membuat  sebuah OpenGl Context Current
        glfwMakeContextCurrent(glfwWindow);
//        Mengaktifkan  v-sync
        glfwSwapInterval(1);
//        Membuat jendela  menjadi nampak
        glfwShowWindow(glfwWindow);

// Baris ini penting untuk interoperasi LWJGL dengan GLFW
// Konteks OpenGL, atau konteks apa pun yang dikelola secara eksternal.
// LWJGL mendeteksi konteks terkini di thread saat ini,
// membuat instance GLCapabilities dan membuat OpenGL
// binding tersedia untuk digunakan.
        GL.createCapabilities();

//        Mengaktifkan BLEND
        glEnable(GL_BLEND);
//        Menghapus background hitam pada objek, dengan mengaktifkan alpha
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);




        this.framebuffer = new Framebuffer(3840, 2160);

        this.pickingTexture = new PickingTexture(3840, 2160);
        glViewport(0, 0, 3840, 2160);

        this.imguiLayer = new ImGuiLayer(glfwWindow, pickingTexture);
        this.imguiLayer.initImGui();

        Window.changeScene(new LevelEditorSceneInitializer());
    }
    public void loop (){

        float beginTime = (float)glfwGetTime();
        float endTime ;
        float dt = -1.0f;

        Shader defaultShader = AssetPool.getShader("assets/shaders/default.glsl");
        Shader pickingShader = AssetPool.getShader("assets/shaders/pickingShader.glsl");

        while (!glfwWindowShouldClose(glfwWindow)){
            glfwPollEvents();;

// Render pass 1. Render to picking texture
            glDisable(GL_BLEND);
            pickingTexture.enableWriting();

            glViewport(0, 0, 3840, 2160);
            glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            Renderer.bindShader(pickingShader);
            currentScene.render();



            pickingTexture.disableWriting();
            glEnable(GL_BLEND);

            // Render pass 2. Render actual game

            DebugDraw.beginFrame();
            this.framebuffer.bind();
            glClearColor(1, 1, 1, 1);
            glClear(GL_COLOR_BUFFER_BIT);

//
            if (dt >= 0){
                DebugDraw.draw();
                Renderer.bindShader(defaultShader);
                if (runtimePlaying) {
                    currentScene.update(dt);
                } else {
                    currentScene.editorUpdate(dt);
                }
                currentScene.render();
            }

            this.framebuffer.unbind();
            this.imguiLayer.update(dt,currentScene);
            glfwSwapBuffers(glfwWindow);
            MouseListener.endFrame();
            endTime = (float)glfwGetTime();
            dt = endTime - beginTime;
            beginTime = endTime;
        }
    }

    public static int getWidth() {
        return get().width;
    }

    public static int getHeight() {
        return get().height;
    }

    public static void setWidth(int newWidth) {
        get().width = newWidth;
    }

    public static void setHeight(int newHeight) {
        get().height = newHeight;
    }


    public static Framebuffer getFramebuffer() {
        return get().framebuffer;
    }

    public static float getTargetAspectRatio() {
        return 16.0f / 9.0f;
    }

    public static ImGuiLayer getImguiLayer() {
        return get().imguiLayer;
    }

    @Override
    public void onNotify(GameObject object, Event event) {
        switch (event.type) {
            case GameEngineStartPlay:
                this.runtimePlaying = true;
                currentScene.save();
                Window.changeScene(new LevelEditorSceneInitializer());
                break;
            case GameEngineStopPlay:
                this.runtimePlaying = false;
                Window.changeScene(new LevelEditorSceneInitializer());
                break;
            case LoadLevel:
                Window.changeScene(new LevelEditorSceneInitializer());
            case SaveLevel:
                currentScene.save();
        }
    }

}