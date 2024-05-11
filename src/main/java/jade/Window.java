package jade;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    //    Penjelasan lengkap ada di  : https://www.lwjgl.org/guide
    private int width, height;
    private  String title;
    private long glfwWindow;
    public float r, g, b, a;
    private boolean fadeToBlack = false;
    private  static Window window = null;
    private static Scene currentScene;
    private ImGuiLayer imGuiLayer;

    private Window(){
        this.width = 1920;
        this.height= 1080;
        this.title ="PixelPivort";
        r = 1;
        b = 1;
        g = 1;
        a = 1;
    }
    public static  void changeScene (int newScene){
        switch (newScene){
            case 0:
                currentScene = new LevelEditorScene();
                currentScene.init();
                currentScene.start();
                break;
            case 1:
                currentScene = new LevelScene();
                currentScene.init();
                currentScene.start();
                break;
            default:
                assert false : "Unknown scene '" + newScene + "'";
                break;
        }
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

        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        this.imGuiLayer = new ImGuiLayer(glfwWindow);
        this.imGuiLayer.initImGui();

        Window.changeScene(0);
    }
    public void loop (){
        float beginTime = (float)glfwGetTime();
        float endTime ;
        float dt = -1.0f;

        currentScene.load();
        while (!glfwWindowShouldClose(glfwWindow)){
            glfwPollEvents();;

            glClearColor(r, g, b, a);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            if (dt >= 0){
                // System.out.println(dt);
                currentScene.update(dt);
            }

            this.imGuiLayer.update(dt, currentScene);

            glfwSwapBuffers(glfwWindow);

            endTime = (float)glfwGetTime();
            dt = endTime - beginTime;
            beginTime = endTime;
        }

        currentScene.saveExit();
    }

    public static int getWidth(){
        return get().width;
    }

    public static int getHeight(){
        return get().height;
    }

    public static void setWidth(int newWidth){
        get().width = newWidth;
    }

    public static void setHeight(int newHeight){
        get().height = newHeight;
    }

}