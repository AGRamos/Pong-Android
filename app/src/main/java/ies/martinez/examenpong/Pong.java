package ies.martinez.examenpong;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.core.view.MotionEventCompat;

import java.util.ArrayList;
import java.util.Random;

public class Pong extends SurfaceView implements SurfaceHolder.Callback, SurfaceView.OnTouchListener {

    private SurfaceHolder holder;
    public BucleJuego bucle;
    private Activity actividad;

    private float deltaT;
    private float [] velocidadBola =new float[2];

    public final float SEGUNDOS_PANTALLA_PALETAS = 3F;
    public final float SEGUNDOS_PANTALLA_BOLA = 5F;
    public final float VELOCIDAD_HORIZONTAL;

    private Canvas canvas;
    public int AltoPantalla;
    public int AnchoPantalla;
    private int maxX, maxY;
    private int X, Y;

    public float anchoBola = 28;
    public float altoBola = 2;

    public RectF pala;
    public RectF pala2;
    public RectF bola;

    private float [] posicionPaletaArriba = new float[2];
    private float [] posicionPaletaAbajo = new float[2];
    private float [] posicionBola = new float[2];
    private float posicionInicialPaleta;
    private float anchoPaleta;
    private float altoPaleta;

    public boolean negativo;
    public boolean cambioPos = false;


    public Control[] controles;
    private final int PRIMERO = 0;
    private final int SEGUNDO = 1;
    private final int TERCERO = 2;
    private final int CUARTO = 3;
    /*Array de Touch */
    private ArrayList<Toque> toques = new ArrayList<Toque>();
    boolean hayToque = false;

    private Bitmap fondo; //Bitmap auxiliar para cargar en el array los recursos

    public Pong(Activity context) {
        super(context);
        actividad = context;
        holder = getHolder();
        holder.addCallback(this);
        CalculaTamañoPantalla();
        VELOCIDAD_HORIZONTAL = AnchoPantalla/ SEGUNDOS_PANTALLA_PALETAS /BucleJuego.MAX_FPS;
        controles = new Control[4];
    }

    public boolean ColisionNave(){
        float alto_mayor = altoBola > altoPaleta ? altoBola : (maxY*0.05f) - altoPaleta;
        //float alto_mayor=altoBola>altoPaleta?altoBola:altoPaleta;
        float ancho_mayor=anchoBola>anchoPaleta?anchoBola:anchoPaleta;
        float diferenciaX=Math.abs(posicionBola[X]-posicionPaletaArriba[X]);
        float diferenciaY=Math.abs(posicionBola[Y]-posicionPaletaArriba[Y]);
        return diferenciaX<=ancho_mayor &&diferenciaY<alto_mayor;
    }

    public boolean ColisionNavePaleta2() {
        float alto_mayor = altoBola > altoPaleta ? altoBola : (maxY*0.05f)- altoPaleta;
        float ancho_mayor = anchoBola > anchoPaleta ? anchoBola : anchoPaleta;
        float diferenciaX = Math.abs(posicionBola[X] - posicionPaletaAbajo[X]);
        float diferenciaY = Math.abs(posicionBola[Y] - posicionPaletaAbajo[Y]);
        return diferenciaX <=ancho_mayor && diferenciaY < alto_mayor;
    }

    public void actualizar(){

        posicionBola[Y] = posicionBola[Y] + deltaT * velocidadBola[Y];

        if (posicionBola[Y] >= maxY || posicionBola[Y] <= 0) {
            if (posicionBola[Y] >= maxY){
                Intent intent=new Intent();
                intent.putExtra("RESULTADO","Gana el de arriba");
                actividad.setResult(1,intent);
                actividad.finish();//finishing activity
            }
            else if (posicionBola[Y] <= 0){
                Intent intent=new Intent();
                intent.putExtra("RESULTADO","Gana el de abajo");
                actividad.setResult(1,intent);
                actividad.finish();//finishing activity
            }
            lanzarPelota();
            cambioPos = false;
        }

        if (posicionBola[X] >= maxX || posicionBola[X] <= 0){
            velocidadBola[X] = -1*(velocidadBola[X]);
        }

        if(ColisionNave() || ColisionNavePaleta2()) {
            velocidadBola[Y] = -1*(velocidadBola[Y]);
            cambioPos = true;
            randomVelocidad(X);
        }

        if (cambioPos){
            posicionBola[X] = posicionBola[X] + deltaT * velocidadBola[X];
        }

        if (controles[PRIMERO].pulsado){
            posicionPaletaArriba[X] = posicionPaletaArriba[X] - VELOCIDAD_HORIZONTAL;
        }
        if (controles[SEGUNDO].pulsado){
            posicionPaletaArriba[X] = posicionPaletaArriba[X] + VELOCIDAD_HORIZONTAL;
        }if (controles[TERCERO].pulsado){
            posicionPaletaAbajo[X] = posicionPaletaAbajo[X] - VELOCIDAD_HORIZONTAL;
        }if (controles[CUARTO].pulsado){
            posicionPaletaAbajo[X] = posicionPaletaAbajo[X] + VELOCIDAD_HORIZONTAL;
        }
    }

    public void renderizar(Canvas canvas) {
        if (canvas != null) {
            canvas.drawColor(Color.WHITE);


            Paint myPaint = new Paint();
            myPaint.setStyle(Paint.Style.STROKE);
            myPaint.setStrokeWidth(15);
            myPaint.setColor(Color.BLACK);

            canvas.drawLine(maxX / 2, 0, maxX / 2, maxY, myPaint);
            canvas.drawLine(0, maxY / 2, maxX, maxY / 2, myPaint);

            myPaint.setStyle(Paint.Style.FILL);
            myPaint.setColor(Color.RED);

            pala = new RectF(posicionPaletaArriba[X],posicionPaletaArriba[Y], posicionPaletaArriba[X] + anchoPaleta, posicionPaletaArriba[Y] + altoPaleta);
            pala2 = new RectF(posicionPaletaAbajo[X],posicionPaletaAbajo[Y], posicionPaletaAbajo[X] + anchoPaleta, posicionPaletaAbajo[Y] - altoPaleta);
            canvas.drawRect(pala, myPaint);
            canvas.drawRect(pala2, myPaint);
            canvas.drawCircle(posicionBola[X], posicionBola[Y], 50, myPaint);

            Paint myPaint2 = new Paint();
            myPaint2.setStyle(Paint.Style.FILL_AND_STROKE);
            myPaint2.setTextSize(100);
            myPaint2.setColor(Color.RED);

            canvas.drawText("1", maxX / 5, maxY / 4, myPaint2);
            canvas.drawText("2", maxX / 1.3F, maxY / 4, myPaint2);
            canvas.drawText("3", maxX / 5, maxY / 1.3f, myPaint2);
            canvas.drawText("4", maxX / 1.3F, maxY / 1.3f, myPaint2);

            if (hayToque) {
                synchronized (this) {
                    for (Toque t : toques) {
                        canvas.drawCircle(t.x, t.y, 100, myPaint);
                        Log.i("TAG", "ESPAÑA");
                        //canvas.drawText(t.index + "", t.x, t.y, myPaint2);
                    }
                }
            }
//            Paint myPaint2 = new Paint();
//            myPaint2.setStyle(Paint.Style.FILL);
//            myPaint2.setTextSize(50);
        }
    }

    public void CalculaTamañoPantalla() {
        if (Build.VERSION.SDK_INT > 13) {
            Display display = actividad.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            AnchoPantalla = size.x;
            AltoPantalla = size.y;
        } else {
            Display display = actividad.getWindowManager().getDefaultDisplay();
            AnchoPantalla = display.getWidth();  // deprecated
            AltoPantalla = display.getHeight();  // deprecated
        }
        Log.i(Pong.class.getSimpleName(), "alto:" + AltoPantalla + "," + "ancho:" + AnchoPantalla);
    }

    public void lanzarPelota() {
        posicionBola[X] = maxX/2;
        posicionBola[Y] = maxY/2;
        randomVelocidad(Y);

    }

    public void randomVelocidad(int pos){
        Random rnd = new Random();
        if(rnd.nextInt(2) == 0) {
            negativo = true;
        } else {
            negativo = false;
        }
        if(negativo) {
            velocidadBola[pos] = (velocidadBola[pos] * - (rnd.nextInt(2)+1));
        } else {
            velocidadBola[pos] = velocidadBola[pos];
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // se crea la superficie, creamos el game loop

        // Para interceptar los eventos de la SurfaceView
        getHolder().addCallback(this);
        Canvas c = holder.lockCanvas();
        maxX = c.getWidth();
        maxY = c.getHeight();
        holder.unlockCanvasAndPost(c);
        // creamos el game loop
        bucle = new BucleJuego(getHolder(), this);

        X = 0;
        Y = 1;

        deltaT=1f/BucleJuego.MAX_FPS;
        velocidadBola[Y]=maxY/SEGUNDOS_PANTALLA_BOLA;

        velocidadBola[X] = maxX/SEGUNDOS_PANTALLA_BOLA;

        anchoPaleta = maxX*0.2f;
        altoPaleta = 20;

        posicionInicialPaleta = maxX*0.4f;


        posicionPaletaArriba[X] = posicionInicialPaleta;
        posicionPaletaAbajo[X] = posicionInicialPaleta;

        posicionPaletaArriba[Y] = maxY*0.05f;
        posicionPaletaAbajo[Y] = maxY*0.95f;

        lanzarPelota();

        // Hacer la Vista focusable para que pueda capturar eventos
        CargaControles();
        setOnTouchListener(this);
        setFocusable(true);
        //comenzar el bucle
        bucle.start();
    }

    public void CargaControles() {
        float ancho = (maxX / 2);
        float alto = (maxY / 2);


        //flecha_izda
        controles[PRIMERO] = new Control(getContext(), 0, 0, ancho, alto);
        controles[PRIMERO].nombre = "PRIMERO";
        //flecha_derecha
        controles[SEGUNDO] = new Control(getContext(), ancho, 0, ancho, alto);
        controles[SEGUNDO].nombre = "SEGUNDO";

        //disparo
        controles[TERCERO] = new Control(getContext(), 0, alto, ancho, alto);
        controles[TERCERO].nombre = "TERCERO";

        controles[CUARTO] = new Control(getContext(), ancho, alto, ancho, alto);
        controles[CUARTO].nombre = "CUARTO";
    }



    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int index;
        int x, y;

        // Obtener el pointer asociado con la acción
        index = MotionEventCompat.getActionIndex(event);

        x = (int) MotionEventCompat.getX(event, index);
        y = (int) MotionEventCompat.getY(event, index);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                hayToque = true;

                synchronized (this) {
                    toques.add(index, new Toque(index, x, y));
                }

                //se comprueba si se ha pulsado
                for (int i = 0; i < 4; i++)
                    controles[i].comprueba_pulsado(x, y);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                synchronized (this) {
                    toques.remove(index);
                }

                //se comprueba si se ha soltado el botón
                for (int i = 0; i < 4; i++)
                    controles[i].comprueba_soltado(toques);
                break;

            case MotionEvent.ACTION_UP:
                synchronized (this) {
                    toques.clear();
                }
                hayToque = false;
                //se comprueba si se ha soltado el botón
                for (int i = 0; i < 4; i++)
                    controles[i].comprueba_soltado(toques);
                break;
        }

        return true;
    }
}
