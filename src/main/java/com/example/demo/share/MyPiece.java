package com.example.demo.share;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;


public class MyPiece implements Comparable<MyPiece>, Cloneable
{
	public double[] coordX;
	public double[] coordY;
	private double[] coriX;
	private double[] coriY;
	private int vertices;
	private int area;
	private int xmin;
	private int xmax;
	private int ymin;
	private int ymax;
	private int ancho;
	private int alto;
	private int numero_Pieza;
	private double rotada;

	public String strPid = "&";
	public List<Integer> listofPiece = new ArrayList<>();

	public void setStrPid(String strPid){
		this.strPid = strPid;
	}

	public String getStrPid(){
		return this.strPid;
	}


	public List<MyPiece> child = new ArrayList<>();

	public List<MyPiece> getChild() {
		return child;
	}

	public void setChild(List<MyPiece> child) {
		this.child = child;
	}

	public MyPiece(){

	}

	public MyPiece(double[] coordenadas){
		this.vertices = coordenadas.length/2;
		int n = this.vertices;
		this.coordX = new double[n];
		this.coordY = new double[n];
		this.coriX = new double[n];
		this.coriY = new double[n];
		for(int i=0;i<n*2;i+=2){
			this.coordX[i/2]=coordenadas[i];
			this.coordY[i/2]=coordenadas[i+1];
			this.coriX[i/2]=coordenadas[i];
			this.coriY[i/2]=coordenadas[i+1];
		}
		this.ancho = this.xmax-this.xmin;
		this.alto = this.ymax-this.ymin;
		this.area = this.calculaArea();
		this.rotada = 0;
	}

	public MyPiece(Integer[] coordenadas){
		this.vertices = coordenadas.length/2;
		int n = this.vertices;
		this.coordX = new double[n];
		this.coordY = new double[n];
		this.coriX = new double[n];
		this.coriY = new double[n];
		for(int i=0;i<n*2;i+=2){
			this.coordX[i/2]=coordenadas[i];
			this.coordY[i/2]=coordenadas[i+1];
			this.coriX[i/2]=coordenadas[i];
			this.coriY[i/2]=coordenadas[i+1];
		}
		this.ancho = this.xmax-this.xmin;
		this.alto = this.ymax-this.ymin;
		this.area = this.calculaArea();
		this.rotada = 0;
	}


	public int getArea() {
		return area;
	}

	public void setArea(int area) {
		this.area = area;
	}

	public void setnumber(int pnumber){
		numero_Pieza = pnumber;
	}

	public int getnumber(){
		return numero_Pieza;
	}

	public int getvertices(){
		return vertices;
	}

	public int getxsize(){
		return xmax - xmin;
	}

	public int getysize(){
		return ymax - ymin;
	}

	public int getTotalSize(){
		return area;
	}


	public double getRectangularidad(){
		return (double)area/(double)(alto*ancho);
	}

    public boolean isNonConvex(){
    	if(anguloMayor() > 180)
    	{
    		return true;
    	}
    	return false;
    }



	public void moveDistance( int dist, int dir ){
		switch(dir)
		{
			case 1:
				for(int i=0; i<vertices; i++)
				{
					coordY[i] += dist;
				}
				break;
			case 2:
				for(int i=0; i<vertices; i++)
				{
					coordY[i] -= dist;
				}
				break;
			case 3:
				for(int i=0; i<vertices; i++)
				{
					coordX[i] -= dist;
				}
				break;
			case 4:
				for(int i=0; i<vertices; i++)
				{
					coordX[i] += dist;
				}
				break;
		}
	}



	public void rotate(double angulo){
		double radianes = toRadians(angulo + rotada);
		double coseno = cos(radianes);
		double seno = sin(radianes);
		int tempXmin = xmin;
		int tempYmin = ymin;
		double tempX, tempY;
		for(int i=0; i<vertices; i++)
		{
			tempX = coriX[i];
			tempY = coriY[i];
			coordX[i] = ( ( (double)(tempX)*coseno
					- (double)(tempY)*seno  ));
			coordY[i] = ( ( (double)(tempX)*seno
					+ (double)(tempY)*coseno));
		}

		rotada += angulo;
	}

	public void rotateCori(double angulo){
		double radianes = toRadians(angulo);
		double coseno = cos(radianes);
		double seno = sin(radianes);
		int tempXmin = xmin;
		int tempYmin = ymin;
		double tempX, tempY;
		for(int i=0; i<vertices; i++)
		{
			tempX = coriX[i];
			tempY = coriY[i];
			coriX[i] = ( ( tempX*coseno
					- (double)(tempY)*seno  ));
			coriY[i] = ( ( (double)(tempX)*seno
					+ (double)(tempY)*coseno));
		}

		rotada += angulo;
	}


	public void desRotar(){
		int tempXmin = xmin;
		int tempYmin = ymin;
		for(int i=0; i<vertices; i++)
		{
			coordX[i] = coriX[i];
			coordY[i] = coriY[i];
		}
		rotada = 0;
	}

	public double isRotated(){
		return rotada;
	}


	public double anguloMayor()
	{
		double mayor = 0;
		double[] angulosInt = new double[vertices];
		for(int i=0; i<vertices; i++)
		{
			if(angulosInt[i] > mayor)
			{
				mayor = angulosInt[i];
			}
		}
		return mayor;
	}


	private int calculaArea(){
		int n = this.vertices;
		int suma = 0;
		for(int i=0;i<n-1;i++) {
			suma+=this.coordX[i]*this.coordY[i+1]-
			this.coordY[i]*this.coordX[i+1];
		}
		int i=n-1;
		suma+=this.coordX[i]*this.coordY[0]-
		this.coordY[i]*this.coordX[0];
		suma = Math.abs(suma)/2;
		return suma;
	}


   public int numVertice(int[] punto)
   {
		for (int i=0; i < vertices; i++)
		{
			if(punto[0] == coordX[i] && punto[1] == coordY[i])
			{
				return i;
			}
		}

      return -1;
    }

	public double getRotada() {
		return rotada;
	}

	public void setRotada(double rotada) {
		this.rotada = rotada;
	}

	@Override
	public int compareTo(MyPiece p) {
		int area0 = this.getTotalSize();
		int area1 = p.getTotalSize();
		if(area0 > area1 ){
			return 1;
		}
		else if(area0 == area1){
			return 0;
		}
		return -1;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}